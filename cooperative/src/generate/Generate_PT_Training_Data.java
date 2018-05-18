package generate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import MobilityAnalyser.Tools;
import SigSpatial.GpsProcess;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;
import jp.ac.ut.csis.pflow.geom.Trip;

public class Generate_PT_Training_Data {
	
	public static void main(String[] args) throws IOException, ParseException{
		
		HashMap<String,ArrayList<Trip>>id_trips = new HashMap<String,ArrayList<Trip>>();
		HashMap<String,ArrayList<STPoint>>id_sps = new HashMap<String,ArrayList<STPoint>>();
		
		File in = new File("D:/training data/PT_housewife.csv");
		
		ReadPTPoint(in, id_sps, id_trips);
		
		for(String id:id_sps.keySet()){
			Collections.sort(id_sps.get(id));
			if(id.equals("1462")){
				System.out.println(id_sps.get(id));
				System.out.println(id_trips.get(id));
			}
		}
	
		
		String path = "D:/training data/PT_housewife_irl.csv";
		File out = new File(path);
		generate(out,id_sps,id_trips);
	}
	
	public static void ReadPTPoint(File in, HashMap<String,ArrayList<STPoint>>id_sps, HashMap<String,ArrayList<Trip>>id_trips) throws IOException, ParseException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		long startTime = System.currentTimeMillis();
		int count = 0;
		String prevLine[] = br.readLine().split(",");
		String lasttripfirstline[] = prevLine;
		HashSet<String>mode_list = new HashSet<String>();
		
		while((line=br.readLine())!=null){
			count++;
			if(count%100000==0){
				System.out.println("scanned "+count+" lines");
			}
		
		
			String tokens[] = line.split(",");
			mode_list.add(tokens[13]);
			
			if(!tokens[10].equals(prevLine[10])||!tokens[0].equals(prevLine[0])){
				
				if(prevLine[13].equals("97")){
					String id = prevLine[0];
					
					Double lat = Double.parseDouble(prevLine[5]);
					Double lon = Double.parseDouble(prevLine[4]);
					
					Date dtstart = SDF_TS4.parse(lasttripfirstline[3]);
					Date dtend = SDF_TS4.parse(prevLine[3]);
					STPoint point = new STPoint(dtstart,dtend,lon,lat);
					
					if(prevLine[0].equals("1462")){
						System.out.println(point);
					}
					
					if(!id_sps.containsKey(id)){
						ArrayList<STPoint>points = new ArrayList<STPoint>();
						points.add(point);
						id_sps.put(id, points);
					}else{
						id_sps.get(id).add(point);
					}		
				}else{
					String id = prevLine[0];
					Double latstart = Double.parseDouble(lasttripfirstline[5]);
					Double lonstart = Double.parseDouble(lasttripfirstline[4]);
					
					Double latend = Double.parseDouble(prevLine[5]);
					Double lonend = Double.parseDouble(prevLine[4]);
					
					Date dtstart = SDF_TS4.parse(lasttripfirstline[3]);
					Date dtend = SDF_TS4.parse(prevLine[3]);
					
					STPoint start = new STPoint(dtstart,lonstart,latstart);
					STPoint end = new STPoint(dtend,lonend,latend);
					
					String mode = null;
					
					
					if(mode_list.contains("11")||mode_list.contains("12")){
						mode = "train";
					}else if(mode_list.contains("2")||mode_list.contains("3")||mode_list.contains("4")||mode_list.contains("5")||mode_list.contains("6")||mode_list.contains("7")||mode_list.contains("8")||mode_list.contains("9")||mode_list.contains("10")){
						mode = "vehicle";
					}else{
						mode = "walk";
					}
					Trip trip = new Trip(start, end, mode);
					mode_list = new HashSet<String>();
					
					if(!id_trips.containsKey(id)){
						ArrayList<Trip>trips = new ArrayList<Trip>();
						trips.add(trip);
						id_trips.put(id, trips);
					}else{
						id_trips.get(id).add(trip);
					}		
				}
				lasttripfirstline = tokens;
			}
			prevLine = tokens;	
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("finished reading files with "+id_trips.size()+" users in tokyo area using" +(endTime-startTime)+"ms");
		br.close();
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
			
//			System.out.println(id+","+trips.toString());
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
				
//				if(id.equals("1462")){
//					System.out.println(sp);
//					if(id_trips.containsKey("1462")){
//						for(Trip trip:id_trips.get(id)){
//							if(sp.getDtEnd().equals(trip.getStartTime())){
//								System.out.println(trip);
//								}
//							}
//					}
//					
//				}
				
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
							bw.write(id+","+ConvertToTimeSlot(trip.getStartTime())+","+start.getCode()+","+end.getCode()+","+trip.getMode2());
							bw.newLine();
							prevSlot = ConvertToTimeSlot(trip.getStartTime());
							prevMesh = end.getCode();
							if(trip.getMode()=="walk"){
								walk++;
							}else if(trip.getMode2()=="vehicle"){
								vehicle++;
							}else if(trip.getMode2()=="train"){
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
								bw.write(id+","+ConvertToTimeSlot(trip.getStartTime())+","+start.getCode()+","+end.getCode()+","+trip.getMode2());
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
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
	protected static final SimpleDateFormat SDF_TS4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

}
