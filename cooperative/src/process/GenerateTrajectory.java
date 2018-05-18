package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.List;
import java.util.Scanner;

import IDExtract.ID_Extract_Tools;
import MobilityAnalyser.Tools;
import StayPointDetection.StayPointGetter2;
import StayPointDetection.StayPointTools;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class GenerateTrajectory {
/*
 * generate daily trajectories for specific person
 * input: GPS log files, id list
 * output: trajectory for specific person in format id/day/time_slot/mesh_id/action/reward
 */
	
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
	protected static final SimpleDateFormat SDF_TS4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void main(String args[]) throws IOException, NumberFormatException, ParseException{
//		File state = new File("/home/t-iho/Data/StayPointforstate.csv");
//		ArrayList<String>idList = GetIdList(state);
//		System.out.println("#done getting id list"+idList.size());
		
		ArrayList<String>idList = new ArrayList<String>();
		idList.add("D5CDE434-898C-4AAD-8F02-770DE7931500");
		idList.add("50CAC98E-93CF-4A02-A41D-5333797E16C4");
		
//		for(int i = 0; i < idList.size(); i++){
//			System.out.println(idList.get(i));
//		}
		System.out.println("Type in the filename(date)");
		Scanner in = new Scanner(System.in);
		String fn = in.nextLine();
		System.out.println("Type in the radius");
		String r = in.nextLine();
		System.out.println("Type in the threshold");
		String threshold = in.nextLine();
		String logs = "/home/t-iho/Data/grid/0/tmp/ktsubouc/"+fn+".csv";
		String path = "/home/t-iho/Data/trajectories/";
	extractTrajectory(logs, path, idList,Double.parseDouble(r),Double.parseDouble(threshold));
	}

	
	public static ArrayList<String>GetIdList(File in) throws IOException{
		ArrayList<String>idList = new ArrayList<>();
		HashMap<String,Integer>id_count = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		int count = 0;
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			String id = tokens[0];
			if(!id_count.containsKey(id)){
				count++;
				id_count.put(id, count);
			}else{
				count = id_count.get(id);
				count++;
				id_count.put(id, count);
			}
			count = 0;
		}
		for(String _id : id_count.keySet()){
			if(id_count.get(_id)>5){
				idList.add(_id);
			}
		}
		br.close();
		return idList;
	}
	
public static void extractTrajectory(String in, String path, ArrayList<String>_ids,double r, double threshold) throws IOException, ParseException{
		
		HashMap<String,ArrayList<STPoint>> id_SPs = StayPointGetter2.getSPsforIRL(new File(in), _ids, r, threshold);
		
		HashMap<String, HashMap<String, ArrayList<STPoint>>> map = intomap(in,"all",_ids); 
		HashMap<String, LinkedHashMap<Date,STPoint>> trajectory = generate_state(map, id_SPs); //[id|day|motifnumber]
		for(String id:trajectory.keySet()){
		writeoutbyid(trajectory.get(id),path+id+".csv");
		}
	}

	

//	public static HashMap<String,ArrayList<LonLat>>id_logs(String[] days, ArrayList<String>ids) throws IOException, ParseException{
//		HashMap<String,ArrayList<STPoint>>day_logs = new HashMap<String,ArrayList<STPoint>>();
//		HashMap<String,ArrayList<STPoint>>day_SPs = new HashMap<String,ArrayList<STPoint>>();
//		
//		for(int i = 0; i < days.length; i++){
//			String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+days[i]+".csv";
//			System.out.println(filepath);
//			
//			BufferedReader br = new BufferedReader(new FileReader(filepath));
//			String line = null;
//			while((line=br.readLine())!=null){ 
//				String[] tokens = line.split("\t");
//				if(tokens.length>=5){
//				String uid = tokens[1];
//				if(ids.contains(uid)){
//					Double lon = Double.parseDouble(tokens[3]);
//					Double lat = Double.parseDouble(tokens[2]);
//					String dt = tokens[4];
//					String day = dt.substring(8,10);
//					Date date = SDF_TS2.parse(dt);
//					STPoint p = new STPoint(date,lon,lat);
//					if(day_logs.containsKey(day)){
//						day_logs.get(day).add(p);
//					}else{
//						ArrayList<STPoint> list = new ArrayList<STPoint>();
//						list.add(p);
//						day_logs.put(day, list);
//						}
//					}
//				}
//			}
//		}
//		int count = 0;
//		for(String _day:day_logs.keySet()){
//			ArrayList<STPoint> SPlist = getStayPointsUni(day_logs.get(id),r,threshold);
//			if(SPlist.size()>0){
//				day_SPs.put(_day, SPlist);
//			}
//			count++;
//			if(count%1000==0){
//				System.out.println("#done getting SPs of " + count);
//			}
//		}
//		
//		return id_logs;
//	}
	
	public static HashMap<String, LinkedHashMap<Date,STPoint>> generate_state
	(HashMap<String, HashMap<String, ArrayList<STPoint>>> map, HashMap<String,ArrayList<STPoint>> id_SPs){
		HashMap<String, LinkedHashMap<Date,STPoint>> res = new HashMap<String, LinkedHashMap<Date,STPoint>>();
		int count = 0;
		for(String id : map.keySet()){
			for(String day : map.get(id).keySet()){
				if(id_SPs.get(id)!=null){
					//	ArrayList<STPoint> temp_locchain = getLocChain2(map.get(id).get(day), id_SPs.get(id));
					//	ArrayList<STPoint> locchain = continueChecker(temp_locchain);
					LinkedHashMap<Date,STPoint>state_list = getStateList(map.get(id).get(day), id_SPs.get(id));
					LinkedHashMap<Date,STPoint> statechain = continueChecker(state_list);
						count++;
						if(count%10000==0){
							System.out.println("#done " + count + " ID*days");
						}
						res.put(id, statechain);
				}
			}
		}
		return res;
	}

	public static LinkedHashMap<Date,STPoint> getStateList(ArrayList<STPoint> list, ArrayList<STPoint> id_SPs){
		LinkedHashMap<Integer,STPoint> temp = new LinkedHashMap<Integer,STPoint>();
		LinkedHashMap<Date,STPoint> res = new LinkedHashMap<Date,STPoint>();
		//		System.out.println("id_SP: " + id_SPs);
		Collections.sort(list);
		int count = 1;
		for(int i = 0; i<list.size(); i++){
		//	System.out.println(list.get(i).getTimeStamp());
			for(STPoint sp : id_SPs){
			//	System.out.println(sp.getDtStart());
				if(list.get(i).distance(sp)<500){
					if(overlapchecker(temp,sp).getTimeStamp()==null){ //new point
						res.put(list.get(i).getTimeStamp(),sp);
						temp.put(count,sp);
						count++;
					}
					else{
						res.put(list.get(i).getTimeStamp(),overlapchecker(temp,sp));
					}
					break;
				}
			}
		}
	//	res.add(list.get(0));
		return res;
	}

	
	public static STPoint overlapchecker(LinkedHashMap<Integer,STPoint> map, STPoint point){
		if(map.size()>0){
			for(Integer i : map.keySet()){
				if(map.get(i).distance(point)<1000){
					return map.get(i);
				}
			}
		}
		return new STPoint();
	}

	public static LinkedHashMap<Date,STPoint> continueChecker(HashMap<Date,STPoint> locchain){
		LinkedHashMap<Date,STPoint> res = new LinkedHashMap<Date,STPoint>();
		STPoint prev = new STPoint();
		Date pretime = null;
		for(Date t : locchain.keySet()){
			if(!locchain.get(t).equals(prev)&&!t.equals(pretime)){
				if(pretime!=null){
					res.put(pretime,prev);
//					System.out.println(pretime);
				}
				res.put(t,locchain.get(t));
			}
			prev = locchain.get(t);
			pretime = t;
		}
		return res;
	}
		
	public static HashMap<String, HashMap<String, ArrayList<STPoint>>> intomap(String in, String mode, ArrayList<String>_id) throws IOException, ParseException{
		ArrayList<String> daycodes = new ArrayList<String>();
		if(mode.equals("weekday")){
			daycodes.add("1");daycodes.add("2");daycodes.add("3");daycodes.add("4");daycodes.add("5");
		}
		else if(mode.equals("weekend")){
			daycodes.add("6");daycodes.add("7");
		}
		else if(mode.equals("all")){
			daycodes.add("1");daycodes.add("2");daycodes.add("3");daycodes.add("4");daycodes.add("5");daycodes.add("6");daycodes.add("7");
		}
		else{
			System.out.println("weekend or weekday or all??");
		}
		int count=0;
		File infile = new File(in);
		BufferedReader br = new BufferedReader(new FileReader(infile));
		HashMap<String, HashMap<String, ArrayList<STPoint>>> res = new HashMap<String, HashMap<String, ArrayList<STPoint>>>();
		String line = null;
		String prevline =null;
		while((line=br.readLine())!=null){
			if(ID_Extract_Tools.SameLogCheck(line,prevline)==true){
			String[] tokens = line.split("\t");
			if(tokens.length>=5){
				System.out.println(line);
			String id = tokens[0];
			if(_id.contains(id)){
				System.out.println(id);
			String dt = tokens[4];
			Date date = SDF_TS2.parse(dt);
			String youbi = (new SimpleDateFormat("u")).format(date);
			if(daycodes.contains(youbi)){
				String day = dt.substring(8,10);
				Double lon = Double.parseDouble(tokens[3]);
				Double lat = Double.parseDouble(tokens[2]);
				if(res.containsKey(id)){
					if(res.get(id).containsKey(day)){
						res.get(id).get(day).add(new STPoint(date,lon,lat));
						System.out.println("1111111111");
					}
					else{
						ArrayList<STPoint> smap = new ArrayList<STPoint>();
						smap.add(new STPoint(date,lon,lat));
						res.get(id).put(day, smap);
					}
				}
				else{
					ArrayList<STPoint> map2 = new ArrayList<STPoint>();
					map2.add(new STPoint(date,lon,lat));
					HashMap<String, ArrayList<STPoint>> map3 = new HashMap<String, ArrayList<STPoint>>();
					map3.put(day, map2);
					res.put(id, map3);
				}
				count++;
				if(count%1000000==0){
					System.out.println("#done sorting " + count);
				}
			}
			}	
			}
		}
			prevline = line;
				}
		br.close();
		return res;
	}
	
	public static HashMap<String, ArrayList<String>> OKAY_id_days(String in) throws IOException, ParseException{
		HashMap<String, ArrayList<String>> id_days = new HashMap<String, ArrayList<String>>();
		HashMap<String, HashMap<String, ArrayList<Integer>>> id_day_array =  id_day_arrayTime(in);
		for(String id : id_day_array.keySet()){
			for(String day : id_day_array.get(id).keySet()){
				if(numberofSlots(id_day_array.get(id).get(day))==true){
					if(id_days.containsKey(id)){
						id_days.get(id).add(day);
					}
					else{
						ArrayList<String> list = new ArrayList<String>();
						list.add(day);
						id_days.put(id, list);
					}
				}
			}
		}
		return id_days;
	}
	
	public static HashMap<String, HashMap<String, ArrayList<Integer>>> id_day_arrayTime(String in) throws IOException, ParseException{
		File infile = new File(in);
		HashMap<String, HashMap<String, ArrayList<Integer>>> id_day_points = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
		BufferedReader br = new BufferedReader(new FileReader(infile));
		String line = null;
		br.readLine();
		while ((line=br.readLine())!=null){
			String[] tokens  = line.split("\t");
			if(tokens.length>=4){
			String id = tokens[0];
			Date dt = SDF_TS2.parse(tokens[4]);
			String day = SDF_TS3.format(dt);
			String time = SDF_TS.format(dt);
			Integer t = StayPointTools.converttoSecs(time);
			if(id_day_points.containsKey(id)){
				if(id_day_points.get(id).containsKey(day)){
					id_day_points.get(id).get(day).add(t);
				}
				else{
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(t);
					id_day_points.get(id).put(day, list);
				}
			}
			else{
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(t);
				HashMap<String, ArrayList<Integer>> tempmap = new HashMap<String, ArrayList<Integer>>();
				tempmap.put(day, list);
				id_day_points.put(id, tempmap);
			}
		}
		}
		br.close();			
		return id_day_points;
	}
	
	public static boolean numberofSlots(ArrayList<Integer> list){
		HashSet<Integer> set = new HashSet<Integer>();
		Integer sho = 0;
		for(Integer t : list){
			sho = t/1800;
			set.add(sho);
		}
		if(set.size()>=8){
			return true;
		}
		else{
			return false;
		}
	}
	
	public static String extractId(ArrayList<String>idlist){
		String objectId = null;
		
		return objectId;
	}
	
	public static int converttotimeslot(Date t){
		int secs = Tools.converttoSecs(SDF_TS.format(t));//interval 30min
		return secs/1800;
	}
	
	public static File writeout(HashMap<String, LinkedHashMap<Date,STPoint>> map, String path) throws IOException{
		File out = new File(path);
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(String id : map.keySet()){
			for(Date t : map.get(id).keySet()){
				Mesh mesh = new Mesh(3,map.get(id).get(t).getLon(),map.get(id).get(t).getLat());
				if(t!=null){
				bw.write(id + "," +converttotimeslot(t)+ "," + mesh.getCode());
				//map.get(id).get(t).getLon()+','+map.get(id).get(t).getLat());
				bw.newLine();
				}
			}
		}
		bw.close();
		return out;
	}
	
	public static File writeoutbyid(LinkedHashMap<Date,STPoint> map, String path) throws IOException{
		File out = new File(path);
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			for(Date t : map.keySet()){
				Mesh mesh = new Mesh(3,map.get(t).getLon(),map.get(t).getLat());
				if(t!=null){
				bw.write(converttotimeslot(t)+ "," + mesh.getCode());
				//map.get(id).get(t).getLon()+','+map.get(id).get(t).getLat());
				bw.newLine();
				}
			}
		bw.close();
		return out;
	}
	
}
