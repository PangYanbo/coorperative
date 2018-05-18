package test;

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
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;

import SigSpatial.Agent;
import SigSpatial.GpsProcess;
import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;
import jp.ac.ut.csis.pflow.geom.Trip;

public class PTtest {

	public static void main(String args[]) throws IOException, ParseException{
		System.out.println("generate Traj now");
		File file = new File("D:/training data/pflow.csv");
		HashMap<String,ArrayList<Trip>>id_trips = new HashMap<String,ArrayList<Trip>>();
		HashMap<String,HashMap<Integer,ArrayList<STPoint>>>id_slot_points = new HashMap<String,HashMap<Integer,ArrayList<STPoint>>>();
		HashMap<String,LinkedHashMap<Integer,String>>id_traj = new HashMap<String,LinkedHashMap<Integer,String>>();
		HashMap<String,HashMap<String,LinkedHashMap<Integer,String>>>mesh_id_traj = new HashMap<String,HashMap<String,LinkedHashMap<Integer,String>>>();
		System.out.println("generate Traj now");
		SortToMap(file,id_trips);
//		SortToSlot(id_trips,id_slot_points);
//		UserCheck(id_slot_points);
		System.out.println("generate Traj now");
		generateTraj(id_trips);
		//ForTraining(file,id_trips);
		for(String id:id_slot_points.keySet()){
			LinkedHashMap<Integer,String>traj = new LinkedHashMap<Integer,String>();
			id_traj.put(id, traj);
		}

		HashMap<String,Integer>mesh_pop = new HashMap<String, Integer>();
		for(String mesh_id:mesh_id_traj.keySet()){
			mesh_pop.put(mesh_id, mesh_id_traj.get(mesh_id).size());
		}
		HashMap<Integer,String>agentList = new HashMap<Integer,String>();
		agentList = Agent.generateAgent_gps(mesh_pop,36);
		HashMap<Integer,ArrayList<String>>id_targetList = new HashMap<Integer,ArrayList<String>>();
		Agent.writeout(id_targetList,"D:/training data/targetList.csv");
		for(Integer agentId:agentList.keySet()){
			ArrayList<String>targetList = new ArrayList<String>();
			//here existing a problem...................................................................................... no training data,no agent
			if(mesh_id_traj.containsKey(agentList.get(agentId))){
				targetList = Agent.getTarget(agentList.get(agentId), mesh_id_traj);
			}
			id_targetList.put(agentId, targetList);
		}
		writeout(mesh_id_traj,"D:/training data/PTexpDatainslot.csv");
		Agent.writeout(id_targetList,"D:/training data/PTagentlist.csv");
	}
	
	public static void SortToMap(File in,HashMap<String,ArrayList<Trip>>id_trips) throws IOException, ParseException{
		long startTime = System.currentTimeMillis();
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		while((line=br.readLine())!=null){
				String tokens[] = line.split(",");
				String id = tokens[0];
				if(Integer.valueOf(id)>=10000){
					break;
				}
				Double ori_lat = Double.parseDouble(tokens[9]);
				Double ori_lon = Double.parseDouble(tokens[8]);	
				Date ori_dt = SDF_TS2.parse(tokens[5]);
				STPoint origin = new STPoint(ori_dt,ori_lon,ori_lat);
				Double dest_lat = Double.parseDouble(tokens[11]);
				Double dest_lon = Double.parseDouble(tokens[10]);	
				Date dest_dt = SDF_TS2.parse(tokens[6]);
				STPoint destination = new STPoint(dest_dt,dest_lon,dest_lat);
				String mode = null;
				if(tokens[7].equals("2")||tokens[7].equals("3")||tokens[7].equals("4")){
					mode = "walk";
				}else if(tokens[7].equals("5")||tokens[7].equals("6")||tokens[7].equals("7")||tokens[7].equals("8")||tokens[7].equals("9")||tokens[7].equals("10")){
					mode = "vehicle";
				}else if(tokens[7].equals("11")||tokens[7].equals("12")){
					mode = "train";
				}else if(tokens[7].equals("97")){
					mode = "stay";
				}else if(tokens[7].equals("1")){
					mode = "walk";
				}
				else{
					mode = "other";
				}
				
				Trip trip = new Trip(origin,destination,mode);
				if(!id_trips.containsKey(id)){
					ArrayList<Trip>trips = new ArrayList<Trip>();
					trips.add(trip);
					id_trips.put(id, trips);
				}else{
					id_trips.get(id).add(trip);
				}
		}
		br.close();
		long endTime = System.currentTimeMillis();
		System.out.println("finished sorting data into map, caculating time is: "+(endTime-startTime)+"ms");
		System.out.println("total user in this area is: "+id_trips.size());
	}
	
	public static void SortToSlot(HashMap<String,ArrayList<Trip>>id_trips,HashMap<String,HashMap<Integer,ArrayList<STPoint>>>id_slot_points){
		for(String id: id_trips.keySet()){
			for(int i=0;i<id_trips.get(id).size();i++){
				Trip trip = id_trips.get(id).get(i);
				int timeslot = GpsProcess.ConvertToTimeSlot(trip.getStartPoint().getTimeStamp());
				int d_timeslot = GpsProcess.ConvertToTimeSlot(trip.getEndPoint().getTimeStamp());
				if(id_slot_points.containsKey(id)){
					if(id_slot_points.get(id).containsKey(timeslot)){
						id_slot_points.get(id).get(timeslot).add(trip.getStartPoint());
					}else{
						ArrayList<STPoint>points = new ArrayList<STPoint>();
						points.add(trip.getStartPoint());
						id_slot_points.get(id).put(timeslot, points);
					}
					if(id_slot_points.get(id).containsKey(d_timeslot)){
						id_slot_points.get(id).get(timeslot).add(trip.getEndPoint());
					}else{
						ArrayList<STPoint>points = new ArrayList<STPoint>();
						points.add(trip.getEndPoint());
						id_slot_points.get(id).put(timeslot, points);
					}
				}else{
					ArrayList<STPoint>points = new ArrayList<STPoint>();
					points.add(trip.getStartPoint());
					HashMap<Integer,ArrayList<STPoint>>slot_points = new HashMap<Integer,ArrayList<STPoint>>();
					slot_points.put(timeslot, points);
					id_slot_points.put(id, slot_points);
				}
				
			}
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
		for(int i=0;i<48;i++){
			int count = 0;
			if(slot_trips.get(i).size()>1){
				Set<String>modes = new HashSet<String>();
				STPoint origin = new STPoint();
				STPoint dest = new STPoint();
				String mode = null;
				for(int j = 0;j<slot_trips.get(i).size();j++){
					origin = slot_trips.get(i).get(0).getStartPoint();
					if(!slot_trips.get(i).get(j).getMode().equals("stay")){
						modes.add(slot_trips.get(i).get(j).getMode());
						dest = slot_trips.get(i).get(j).getEndPoint();
						count++;
					}
				}
				
				if(modes.contains("vehicle")){
					mode = "vehicle";
				}else if(modes.contains("train")){
					mode = "train";
				}else{
					mode = "walk";
				}
				
				Trip mtrip = new Trip(origin,dest,mode);
				mTrips.add(mtrip);
			}else if(slot_trips.get(i).size()==1){
				mTrips.add(slot_trips.get(i).get(0));
			}
		}
		return mTrips;
	}
	
	public static void generateTraj(HashMap<String,ArrayList<Trip>>id_trips) throws IOException{
		System.out.println("generate Traj now");
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/PTtraj3.csv"));
		for(String id:id_trips.keySet()){	
			for(int i = 0; i<id_trips.get(id).size();i++){
				
				ArrayList<Trip>trips = mergeTrip(id_trips.get(id));
				
				Integer t1 = GpsProcess.ConvertToTimeSlot(trips.get(i).getStartPoint().getTimeStamp());
				Integer t2 = GpsProcess.ConvertToTimeSlot(trips.get(i).getEndPoint().getTimeStamp());
				Mesh start = new Mesh(3,trips.get(i).getStartPoint().getLon(),trips.get(i).getStartPoint().getLat());
				Mesh end = new Mesh(3,trips.get(i).getEndPoint().getLon(),trips.get(i).getEndPoint().getLat());
			//	Mesh next_start = new Mesh(3,id_trips.get(id).get(i+1).pstart.getLon(),id_trips.get(id).get(i+1).pstart.getLat());
				if(i==0&&t1>12){
					for(int j =12;j<t1;j++){
						bw.write(id+","+j+","+start.getCode()+","+end.getCode()+","+trips.get(i).getMode2());
						bw.newLine();
					}
				}
				if(t2==t1){
					
				}
				if(t2==t1+1){
					if(t1>=12){
					bw.write(id+","+t1+","+start.getCode()+","+end.getCode()+","+trips.get(i).getMode2());
					bw.newLine();}
				}
				if(t2>t1+1){
					trips.get(i).getMode2();
					if("stay".equals(id_trips.get(id).get(i).getMode2())){
						for(int j=t1;j<t2;j++){
							if(j>=12){
								int k = j-12;
								while(k>0){
									bw.write(id+","+k+","+start.getCode()+","+end.getCode()+","+trips.get(i).getMode());
									bw.newLine();
									k--;
								}
							bw.write(id+","+j+","+start.getCode()+","+end.getCode()+","+trips.get(i).getMode2());
							bw.newLine();}
						}
					}else{
						if(t1>=12){
		
						Random r = new Random();
						
						int t3 = Math.abs(r.nextInt() % (t2-t1))+t1;
						//LonLat middle = new LonLat((p1.getLon()+p2.getLon())*0.5,(p1.getLat()+p2.getLat())*0.5);
						//Mesh mesh3 = new Mesh(3,middle.getLon(),middle.getLat());
						for(int j = t1;j<t3;j++){
							bw.write(id+","+j+","+start.getCode()+","+start.getCode()+","+"stay");
							bw.newLine();
						}
						bw.write(id+","+t3+","+start.getCode()+","+end.getCode()+","+trips.get(i).getMode2());
						bw.newLine();
						for(int j = t3+1;j<t2;j++){
							bw.write(id+","+j+","+end.getCode()+","+end.getCode()+","+"stay");
							bw.newLine();
							}
						}
					}
				}
				if(i==trips.size()-1&&t2<47){
					for(int j = t2;j<48;j++){
						bw.write(id+","+j+","+end.getCode()+","+end.getCode()+","+"stay");
						bw.newLine();
					}
				}
			}
		}
		bw.close();
	}
	
	public static void writeout(HashMap<String,HashMap<String,LinkedHashMap<Integer,String>>>mesh_id_traj,String path) throws IOException{
		System.out.println("Start to write out files");
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		String action = null;
		for(String meshid:mesh_id_traj.keySet()){
			for(String id:mesh_id_traj.get(meshid).keySet()){
				for(Integer t:mesh_id_traj.get(meshid).get(id).keySet()){
					if(t<=47){
						if(mesh_id_traj.get(meshid).get(id).get(t).equals(mesh_id_traj.get(meshid).get(id).get(t+1))){
							action = "Stay";
						}else{
							action = mesh_id_traj.get(meshid).get(id).get(t+1);
						}
					}
					if(t ==47){action = "Stay";}
					bw.write(meshid+","+id+","+t+","+mesh_id_traj.get(meshid).get(id).get(t)+","+action);
					bw.newLine();
				}
			}
		}
		bw.close();
	}
	
	
	public static void ForTraining(File in,HashMap<String,ArrayList<Trip>>id_trips) throws IOException, ParseException{
	
		GeometryChecker inst = new GeometryChecker(new File("C:/Users/PangYanbo/Desktop/Tokyo/shp"));
		
		BufferedReader br = new BufferedReader(new FileReader(in));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/PTtraj3.csv"));
		String line = null;
		Integer prev_t = -1;
		String prev_id = null;
		while((line=br.readLine())!=null){
				
				String tokens[] = line.split(",");
				
				String id = tokens[0];
				
				if(prev_id!=id){
					prev_t=-1;
					prev_id = id;
				}
				Double ori_lat = Double.parseDouble(tokens[9]);
				Double ori_lon = Double.parseDouble(tokens[8]);	
				Date ori_dt = SDF_TS2.parse(tokens[5]);
				Integer t1 = GpsProcess.ConvertToTimeSlot(ori_dt);
				Mesh origin = new Mesh(3,ori_lon,ori_lat);
				Double dest_lat = Double.parseDouble(tokens[11]);
				Double dest_lon = Double.parseDouble(tokens[10]);
				
				if(ori_lat>=35.33&&ori_lat<=36&&ori_lon>=139&&ori_lon<=140){
					if(inst.checkOverlap(ori_lon, ori_lat)){
						if(dest_lat>=35.33&&dest_lat<=36&&dest_lon>=139&&dest_lon<=140){
							if(inst.checkOverlap(dest_lon, dest_lat)){
								Date dest_dt = SDF_TS2.parse(tokens[6]);
								Integer t2 = GpsProcess.ConvertToTimeSlot(dest_dt);
								Mesh destination = new Mesh(3,dest_lon,dest_lat);
								String mode = null;
								if(tokens[7].equals("2")||tokens[7].equals("3")||tokens[7].equals("4")){
									mode = "walk";
								}else if(tokens[7].equals("5")||tokens[7].equals("6")||tokens[7].equals("7")||tokens[7].equals("8")||tokens[7].equals("9")||tokens[7].equals("10")){
									mode = "vehicle";
								}else if(tokens[7].equals("11")||tokens[7].equals("12")){
									mode = "train";
								}else if(tokens[7].equals("97")){
									mode = "stay";
								}else if(tokens[7].equals("1")){
									mode = "walk";
								}
								else{
									mode = "other";
								}
								bw.write(id+","+t1+","+origin.getCode()+","+destination.getCode()+","+mode);
								bw.newLine();
								if(mode.equals("stay")){
									bw.write(id+","+t2+","+origin.getCode()+","+destination.getCode()+","+mode);
									bw.newLine();
								}
								
							}
						}
					}
				}	
		}
		br.close();
		bw.close();
	}
	protected static final SimpleDateFormat SDF_TS = new SimpleDateFormat("HH:mm:ss");
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");
}
