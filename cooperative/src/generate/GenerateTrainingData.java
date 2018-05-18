package generate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import MobilityAnalyser.Tools;
import SigSpatial.GpsProcess;
import extract.ExtractStayPoint;
import extract.ExtractTrips;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;
import jp.ac.ut.csis.pflow.geom.Trip;
import readfiles.ReadBufferPoint;

public class GenerateTrainingData {
/*
 * this class is used for generate training data for ubicomp
 * 
 * 
 * */
	
	public static void main(String[] args) throws ParseException, IOException, Exception{
		HashMap<String,ArrayList<STPoint>>id_points = new HashMap<String,ArrayList<STPoint>>();
		HashMap<String,ArrayList<Trip>>id_trips = new HashMap<String,ArrayList<Trip>>();
		HashMap<String,ArrayList<STPoint>>id_sps = new HashMap<String,ArrayList<STPoint>>();
		ArrayList<STPoint>sps = new ArrayList<STPoint>();
		
		Scanner in = new Scanner(System.in);
		System.out.println("Type in the GPS file date");
		String date = in.nextLine();
		
		System.out.println("Type in the time threhold in secs");
		Double timeThres = Double.valueOf(in.nextLine());
		System.out.println("Type in the distance threhold in meters");
		Double distThres = Double.valueOf(in.nextLine());
		
		File gpsfile = new File("/home/t-iho/Result/rawdata/TokyoLog"+date+".csv");
		
		ReadBufferPoint.ReadBufferPoint(id_points, gpsfile);
		
		for(String id:id_points.keySet()){
			sps = ExtractStayPoint.StayPointDetection(timeThres,distThres,id_points.get(id));
			ArrayList<Trip>trips = new ArrayList<Trip>();
			trips = ExtractTrips.SplitTrips(id_points.get(id),sps);
			id_trips.put(id,trips);
			id_sps.put(id, sps);
		}
		
		System.out.println("Type in the process you want to do: '1' is generate training data, '2' extract part of training data");
		String process = in.nextLine();
		if(process.equals("1")){
			String path = "/home/t-iho/Result/trainingdata/trainingdata"+date+".csv";
			File out = new File(path);
			generate(out,id_sps,id_trips);
		}else if(process.equals("2")){
			
			System.out.println("Type in the percentage");
			Integer percentage = Integer.valueOf(in.nextLine());
			String path = "/home/t-iho/Result/trainingdata/trainingdata"+percentage+"%"+date+".csv";
			File out = new File(path);
			
			generatepercentage(out,percentage,id_sps,id_trips);
		}
		
		
	}
	
	public static ArrayList<Trip>mergeTrip(ArrayList<Trip>trips) throws IOException{
		ArrayList<Trip>mTrips = new ArrayList<Trip>();
		HashMap<Integer,ArrayList<Trip>>slot_trips = new HashMap<Integer,ArrayList<Trip>>();
		for(int i=0;i<48;i++){
			ArrayList<Trip>_trips = new ArrayList<Trip>();
			slot_trips.put(i, _trips);
		}
		for(int i = 0; i<trips.size();i++){
			Integer t1 = GpsProcess.ConvertToTimeSlot(trips.get(i).getStartPoint().getTimeStamp());
			slot_trips.get(t1).add(trips.get(i));
		}
		for(int i=12;i<48;i++){
			if(slot_trips.get(i).size()>1){
				Set<String>modes = new HashSet<String>();
				STPoint origin = new STPoint();
				STPoint dest = new STPoint();
				for(int j = 0;j<slot_trips.get(i).size();j++){
					origin = slot_trips.get(i).get(0).getStartPoint();
					if(!slot_trips.get(i).get(j).getMode().equals("stay")){
						modes.add(slot_trips.get(i).get(j).getMode2());
						//System.out.println(slot_trips.get(i).get(j).getMode());
						dest = slot_trips.get(i).get(j).getEndPoint();
					}
				}
				
				if(modes.contains("vehicle")){
					Trip mtrip = new Trip(origin,dest,"vehicle");
					mTrips.add(mtrip);
				}else if(modes.contains("train")){
					Trip mtrip = new Trip(origin,dest,"train");
					mTrips.add(mtrip);
				}else{
					Trip mtrip = new Trip(origin,dest,"walk");
					mTrips.add(mtrip);
				}
			
			}else if(slot_trips.get(i).size()==1){
				mTrips.add(slot_trips.get(i).get(0));
			}
		}
		return mTrips;
	}
	
	public static void generate(File out,HashMap<String,ArrayList<STPoint>>id_staypoints,HashMap<String,ArrayList<Trip>>id_trips) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		int count = 0;
		
		int vehicle = 0;
		int train = 0;
		int walk = 0;
		
		for(String id:id_trips.keySet()){
			ArrayList<Trip>trips = mergeTrip(id_trips.get(id));
			id_trips.put(id, trips);
		}
		
		for(String id:id_staypoints.keySet()){
			Integer prevSlot = 0;
			Integer lastSlot = 0;
			String lastMesh = null;
			String prevMesh = null;
			
			count++;
			if(count%1000==0){
				System.out.println("already processed "+count+" ids");
			}
			
			for(STPoint sp:id_staypoints.get(id)){
				
				if(prevMesh!=null&&prevSlot+1<ConvertToTimeSlot(sp.getDtStart())){
					for(int i = prevSlot+1;i<ConvertToTimeSlot(sp.getDtStart());i++){
						bw.write(id+","+i+","+prevMesh+","+prevMesh+","+"stay" );
						bw.newLine();
					}
				}
				
				Integer startSlot = ConvertToTimeSlot(sp.getDtStart());
				Integer endSlot = ConvertToTimeSlot(sp.getDtEnd());
				Mesh mesh = new Mesh(3, sp.getLon(),sp.getLat());
				
				if(startSlot<12){
					if(endSlot >=12){
						for(int i = 12;i < endSlot; i++){
							bw.write(id+","+i+","+mesh.getCode()+","+mesh.getCode()+","+"stay" );
							bw.newLine();
						}
					}
				}else{
					if(lastSlot==0){
						for(int i =12; i < endSlot; i++){
							bw.write(id+","+i+","+mesh.getCode()+","+mesh.getCode()+","+"stay" );
							bw.newLine();
						}
					}else{
						for(int i = startSlot; i < endSlot; i++){
							bw.write(id+","+i+","+mesh.getCode()+","+mesh.getCode()+","+"stay" );
							bw.newLine();
						}
					}
				}
				
				lastSlot = endSlot;
				lastMesh = mesh.getCode();
				
				if(id_trips.containsKey(id)){
					for(Trip trip:id_trips.get(id)){
						if(sp.getDtEnd().equals(trip.getStartTime())){
							Mesh start = new Mesh(3, trip.getStartPoint().getLon(),trip.getStartPoint().getLat());	
							Mesh end = new Mesh(3, trip.getEndPoint().getLon(),trip.getEndPoint().getLat());
							bw.write(id+","+ConvertToTimeSlot(trip.getStartTime())+","+start.getCode()+","+end.getCode()+","+trip.getMode());
							bw.newLine();
							prevSlot = ConvertToTimeSlot(trip.getStartTime());
							prevMesh = end.getCode();
							if(trip.getMode()=="walk"){
								walk++;
							}else if(trip.getMode()=="vehicle"){
								vehicle++;
							}else if(trip.getMode()=="train"){
								train++;
							}
						}
					}
				}	
			}
			
			if(lastSlot!=47){
				for(int i = lastSlot;i <= 47; i++){
					bw.write(id+","+i+","+lastMesh+","+ lastMesh +","+"stay" );
					bw.newLine();
				}
			}
		}
		
		bw.close();
		System.out.println("count of walk trip is "+walk);
		System.out.println("count of vehicle trip is "+vehicle);
		System.out.println("count of train trip is "+train);
	}
	
	public static void generatepercentage(File out, Integer percentage, HashMap<String,ArrayList<STPoint>>id_staypoints,HashMap<String,ArrayList<Trip>>id_trips) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		int count = 0;	
		
		for(String id:id_staypoints.keySet()){
			Integer prevSlot = 0;
			Integer lastSlot = 0;
			String lastMesh = null;
			String prevMesh = null;
			
			count++;
			if(count%(100/percentage)==0){
				for(STPoint sp:id_staypoints.get(id)){
					
					if(prevMesh!=null&&prevSlot+1<ConvertToTimeSlot(sp.getDtStart())){
						for(int i = prevSlot+1;i<ConvertToTimeSlot(sp.getDtStart());i++){
							bw.write(id+","+i+","+prevMesh+","+prevMesh+","+"stay" );
							bw.newLine();
						}
					}
					
					Integer startSlot = ConvertToTimeSlot(sp.getDtStart());
					Integer endSlot = ConvertToTimeSlot(sp.getDtEnd());
					Mesh mesh = new Mesh(3, sp.getLon(),sp.getLat());
					
					if(startSlot<12){
						if(endSlot >=12){
							for(int i = 12;i < endSlot; i++){
								bw.write(id+","+i+","+mesh.getCode()+","+mesh.getCode()+","+"stay" );
								bw.newLine();
							}
						}else if(endSlot<12){
							continue;
						}
					}else if(startSlot>=12){
						if(lastSlot==0){
							for(int i =12; i < endSlot; i++){
								bw.write(id+","+i+","+mesh.getCode()+","+mesh.getCode()+","+"stay" );
								bw.newLine();
							}
						}else{
							for(int i = startSlot; i < endSlot; i++){
								bw.write(id+","+i+","+mesh.getCode()+","+mesh.getCode()+","+"stay" );
								bw.newLine();
							}
						}
					}
					
					lastSlot = endSlot;
					lastMesh = mesh.getCode();
					
					if(id_trips.containsKey(id)){
						for(Trip trip:id_trips.get(id)){
							if(sp.getDtEnd().equals(trip.getStartTime())){
								Mesh start = new Mesh(3, trip.getStartPoint().getLon(),trip.getStartPoint().getLat());	
								Mesh end = new Mesh(3, trip.getEndPoint().getLon(),trip.getEndPoint().getLat());
								bw.write(id+","+ConvertToTimeSlot(trip.getStartTime())+","+start.getCode()+","+end.getCode()+","+trip.getMode());
								bw.newLine();
								prevSlot = ConvertToTimeSlot(trip.getStartTime());
								prevMesh = end.getCode();
							}
						}
					}	
				}
				
				if(lastSlot!=47){
					for(int i = lastSlot;i <= 47; i++){
						bw.write(id+","+i+","+lastMesh+","+ lastMesh +","+"stay" );
						bw.newLine();
					}
				}
			}
			
		}
		
		bw.close();
	}
	
	public static int ConvertToTimeSlot(Date t){
		int secs = Tools.converttoSecs(SDF_TS.format(t));//interval 30min
		return secs/1800;
	}
	
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
}
