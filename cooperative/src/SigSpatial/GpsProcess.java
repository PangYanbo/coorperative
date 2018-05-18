package SigSpatial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Scanner;

import MobilityAnalyser.Tools;
import analysis.StayPoint;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class GpsProcess {
	/*
	 * process gps data into user profile
	 * each user's profile was saved in an individual csv file
	 * 
	 * 
	 * 
	 * */
	
	public static void main(String[] args) throws Exception{
		//HashMap:mesh_id,did,point
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte target date in format of yyyyMMdd");
		String date = in.nextLine();
		HashMap<String,ArrayList<STPoint>>id_points = new HashMap<String,ArrayList<STPoint>>();
		HashMap<String,HashMap<String,LinkedHashMap<State,String>>>sorted_SPs = new HashMap<String,HashMap<String,LinkedHashMap<State,String>>>();
		File file = new File("/home/t-iho/grid/0/tmp/hadoop-ktsubouc/data_"+date+".csv");
		SortToMap(file,id_points);
		UserCheck(id_points); //check each id is rich enough, and sort point list into time order
		int count  = 0;
		long startTime = System.currentTimeMillis();
		for(String id:id_points.keySet()){
			count++;
			if(count%1000==0){
				System.out.println("finished extracting "+count+"ids' SPs");
			}
				ArrayList<STPoint>SPs = StayPoint.StayPointDetection(id_points.get(id));
				LinkedHashMap<State,String>traj = GenerateTrajs(SPs);
				Mesh mesh = new Mesh(3,SPs.get(0).getLon(),SPs.get(0).getLat());
				if(sorted_SPs.containsKey(mesh.getCode())){
					sorted_SPs.get(mesh.getCode()).put(id, traj);
				}else{
					HashMap<String,LinkedHashMap<State,String>>id_traj = new HashMap<String,LinkedHashMap<State,String>>();
					id_traj.put(id, traj);
					sorted_SPs.put(mesh.getCode(), id_traj);
				}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("finished generating trajectories, caculating time is: "+(endTime-startTime)+"ms");

		writeout(sorted_SPs,"/home/t-iho/Sig/expData"+date+".csv");
		HashMap<Integer,String>agentList = new HashMap<Integer,String>();
		HashMap<Integer,ArrayList<String>>id_targetList = new HashMap<Integer,ArrayList<String>>();
		// file here is kokusei census mesh population distribution file
		agentList = Agent.generateAgent("/home/t-iho/Sig/kokusei.txt",100);
		long startTime2 = System.currentTimeMillis();
		for(Integer agentId:agentList.keySet()){
			ArrayList<String>targetList = new ArrayList<String>();
			//here existing a problem...................................................................................... no training data,no agent
			if(sorted_SPs.containsKey(agentList.get(agentId))){
				targetList = Agent.getTarget(agentList.get(agentId), sorted_SPs);
			}
			id_targetList.put(agentId, targetList);
		}
		long endTime2 = System.currentTimeMillis();
		System.out.println("finished generating agents, caculating time is: "+(endTime2-startTime2)+"ms");
		Agent.writeout(id_targetList,"/home/t-iho/Sig/agentlist"+date+".csv");
	}
	
	public static void SortToMap(File in,HashMap<String,ArrayList<STPoint>>id_points) throws IOException, ParseException{
		long startTime = System.currentTimeMillis();
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		String prevline = null;
		while((line=br.readLine())!=null){
			if(SameLogCheck(line,prevline)==true){
				String tokens[] = line.split("\t");
				if(!tokens[4].equals("null")){
					String did = tokens[1];
					Double lat = Double.parseDouble(tokens[2]);
					Double lon = Double.parseDouble(tokens[3]);
					if(lat>=35.33&&lat<=36&&lon>=139&&lon<=140){	
						Date dt = SDF_TS2.parse(tokens[4]);
						String time = SDF_TS.format(dt);
						STPoint point = new STPoint(dt,lon,lat);
						if(!id_points.containsKey(did)){
							ArrayList<STPoint>points = new ArrayList<STPoint>();
							points.add(point);
							id_points.put(did, points);
						}else{
							id_points.get(did).add(point);
						}
					}
				}
			}
			prevline = line;
		}
		br.close();
		long endTime = System.currentTimeMillis();
		System.out.println("finished sorting data into map, caculating time is: "+(endTime-startTime)+"ms");
	}
	
	
	public static LinkedHashMap<State,String> GenerateTrajs(ArrayList<STPoint>SPs) throws ParseException{
		//output:state action pairs[timeslot,meshid,action]
		LinkedHashMap<State,String>traj = new LinkedHashMap<State,String>();
		String action="null";
		State prev_state = new State();
		String prev_action = null;
		for(STPoint sp:SPs){
			if(prev_state.getTimeStamp()==null){
					if(sp.isTimeSpan()){
						State state = new State(sp.getDtStart(),sp);
						traj.put(state,"Stay");
						prev_action ="Stay";
						prev_state = new State(sp.getDtEnd(),sp);
					}else{
						prev_state = new State(sp.getTimeStamp(),sp);
						prev_action = "move";
					}
			}else{
				if(prev_action == "Stay"){
					Mesh mesh = new Mesh(3,sp.getLon(),sp.getLat());
					action = mesh.getCode();   
					traj.put(prev_state,action);
				}else{
					Mesh mesh = new Mesh(3,sp.getLon(),sp.getLat());
					action = mesh.getCode();   
					traj.put(prev_state,action);
				}
					
				
					if(sp.isTimeSpan()){
						State state = new State(sp.getDtStart(),sp);
						traj.put(state, "Stay");
						prev_state = new State(sp.getDtEnd(),sp);
						prev_action = "Stay";
					}else{
						prev_state= new State(sp.getTimeStamp(),sp);
						prev_action = "move";
					}				
			}
		}
		if(prev_state.getTimeStamp()!=null){
			traj.put(prev_state,"Stay");	
		}
		return traj;
	}
	
//	public static LinkedHashMap<Date,STPoint> GenerateTrajs(ArrayList<STPoint>points,ArrayList<STPoint>SPs) throws ParseException{
//		//output:state action pairs[id|list(state,action)]
//		LinkedHashMap<Integer,STPoint> temp = new LinkedHashMap<Integer,STPoint>();
//		LinkedHashMap<Date,STPoint> res = new LinkedHashMap<Date,STPoint>();
//		int count = 1;
//		for(int i = 0;i<points.size();i++){
//			for(STPoint sp:SPs){
//				if(points.get(i).distance(sp)<500){
//					if(overlapchecker(temp,sp).getTimeStamp()==null){ //new point
//						res.put(points.get(i).getTimeStamp(),sp);
//						temp.put(count,sp);
//						count++;
//					}
//					else{
//						res.put(points.get(i).getTimeStamp(),overlapchecker(temp,sp));
//					}
//					break;
//				}else if(i==0){
//					res.put(points.get(0).getTimeStamp(),points.get(0));
//				}else if(i==points.size()-1){
//					res.put(points.get(i).getTimeStamp(),points.get(0));
//				}
//			}
//		}
//		res = continueChecker(res);
//		return res;
//	}
	
	public static STPoint overlapchecker(LinkedHashMap<Integer,STPoint> map, STPoint sp){
		if(map.size()>0){
			for(Integer i : map.keySet()){
				if(map.get(i).distance(sp)<1000){
					return map.get(i);
				}
			}
		}
		return new STPoint();
	}
	
	public static LinkedHashMap<Date,STPoint> continueChecker(HashMap<Date,STPoint> locchain) throws ParseException{
		LinkedHashMap<Date,STPoint> res = new LinkedHashMap<Date,STPoint>();
		STPoint prev = new STPoint();
		Date pretime = null;
		Date endtime = SDF_TS.parse("00:00:00");
		for(Date t : locchain.keySet()){
			if(!locchain.get(t).equals(prev)&&!t.equals(pretime)){
				if(pretime!=null){
					res.put(pretime,prev);
				}
				res.put(t,locchain.get(t));
				endtime = t;
			}
			prev = locchain.get(t);
			pretime = t;
		}
		if(ConvertToTimeSlot(endtime)<47){
			res.put(SDF_TS.parse("23:59:59"),prev);
			System.out.println("1234");
		}
		return res;
	}
	
	public static void CheckCoverage(String path, HashMap<String,LinkedList<STPoint>>id_points) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/t-iho/mesh_count.csv")));
		HashMap<String,Integer>mesh_count = new HashMap<String,Integer>();
		String line = br.readLine();
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",");
			String id = tokens[0];
			if(!mesh_count.containsKey(id)){
				mesh_count.put(id, 1);
			}
		}
		br.close();
		for(String id:id_points.keySet()){
			Double lon = id_points.get(id).get(0).getLon();
			Double lat = id_points.get(id).get(0).getLat();
			Mesh mesh = new Mesh(3,lon,lat);
			if(mesh_count.containsKey(mesh.getCode())){
				int oldValue = mesh_count.get(mesh.getCode());
				mesh_count.put(mesh.getCode(), oldValue+1);
			}else{
				mesh_count.put(mesh.getCode(), 1);
			}
		}
		for(String mesh:mesh_count.keySet()){
			bw.write(mesh+","+mesh_count.get(mesh));
			bw.newLine();
		}
		bw.close();
		System.out.println("finished checking coverages");
	}
	
	public static void UserCheck(HashMap<String,ArrayList<STPoint>>id_points){
		long startTime = System.currentTimeMillis();
		HashMap<String,ArrayList<STPoint>>map2 = new HashMap<String,ArrayList<STPoint>>();
		for(String id:id_points.keySet()){
			if(id_points.get(id).size()>=20){
				HashSet<Integer>slot = new HashSet<Integer>();
				Collections.sort(id_points.get(id));
				for(STPoint point:id_points.get(id)){
					int time = DateToSec(point.getTimeStamp());
					slot.add(time/1800);
				}
				if(slot.size()>=8){
					continue;
				}else{
					map2.put(id, id_points.get(id));
				}
			}
		}
		for(String key:map2.keySet()){
			id_points.remove(key);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("finished checking "+id_points.size()+" userlogs, calculating time is: "+(endTime-startTime)+"ms");
	}
	
	public static void writeout(HashMap<String,HashMap<String,LinkedHashMap<State,String>>>sortedmap,String path) throws IOException{
		System.out.println("Start to write out files");
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		for(String mesh_id:sortedmap.keySet()){
			for(String uid:sortedmap.get(mesh_id).keySet()){
				for(State state:sortedmap.get(mesh_id).get(uid).keySet()){
					Integer timestamp = ConvertToTimeSlot(state.getTimeStamp());
					Mesh mesh = new Mesh(3,state.getLocation().getLon(),state.getLocation().getLat());
					
					bw.write(mesh_id+","+uid+","+timestamp+","+mesh.getCode()+","+sortedmap.get(mesh_id).get(uid).get(state));
					bw.newLine();
				}
			}
		}
		bw.close();
	}
	
	public static int DateToSec(Date t){
		int sec = 0;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(t);
		sec = calendar.get(Calendar.HOUR_OF_DAY)*3600+calendar.get(Calendar.MINUTE)*60+calendar.get(Calendar.SECOND);
		return sec;
	}
	
	public static boolean SameLogCheck(String line, String prevline){
		if(line.equals(prevline)){
			return false;
		}
		else{
			return true;
		}
	}
	
	public static int ConvertToTimeSlot(Date t){
		int secs = Tools.converttoSecs(SDF_TS.format(t));//interval 30min
		return secs/1800;
	}
	
	protected static final SimpleDateFormat SDF_TS = new SimpleDateFormat("HH:mm:ss");
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");
}
