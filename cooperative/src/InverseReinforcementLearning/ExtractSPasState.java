package InverseReinforcementLearning;

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
import java.util.LinkedHashMap;
import java.util.Scanner;

import DataModify.Over8TimeSlots;
import IDExtract.ID_Extract_Tools;
import MobilityAnalyser.Tools;
import Motif.SPFinder;
import StayPointDetection.StayPointTools;
import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class ExtractSPasState {
	
	public static void main(String[] args) throws IOException, ParseException{
Scanner type = new Scanner(System.in);
		
		System.out.println("Type in the radius");
		Double r = Double.parseDouble(type.nextLine());
		System.out.println("Type in the threshold");
		Double threshold = Double.parseDouble(type.nextLine());
		String path ="/home/t-iho/Data/forIRL/id_day_point_mesh5339.csv";
		File file = new File(path);
		
        HashMap<String, HashMap<String, ArrayList<STPoint>>> map = sortLogintoMap(file,"all"); 
    	HashMap<String, HashMap<String,ArrayList<STPoint>>> id_SPs = getStayPoint(map, r, threshold);
    	HashMap<String, ArrayList<String>> id_days = Over8TimeSlots.IRL_id_days(file);
    	HashMap<String, HashMap<String,LinkedHashMap<Date,STPoint>>> trajectory = generate_state(map, id_SPs, id_days); //[id|day|motifnumber]
    		
    	writeout(trajectory);
    		
//    	HashMap<String, ArrayList<STPoint>>allpoints = YMap(file);
//    		
//    	BufferedWriter bw2 = new BufferedWriter(new FileWriter("/home/t-iho/Data/forIRL/"+"mesh_list"+file.getName(),false));
	}

	
	public static HashMap<String, HashMap<String,ArrayList<STPoint>>> getStayPoint(HashMap<String,HashMap<String,ArrayList<STPoint>>>alldatamap, double r, double threshold) 
			throws NumberFormatException, ParseException, IOException{

		HashMap<String,ArrayList<STPoint>> day_sps = new HashMap<String,ArrayList<STPoint>>();
		//		System.out.println(alldatamap.size());
		System.out.println("#done sorting all data into maps");

		HashMap<String,HashMap<String,ArrayList<STPoint>>> res = new HashMap<String, HashMap<String,ArrayList<STPoint>>>();
		for(String id:alldatamap.keySet()){
			for(String day:alldatamap.get(id).keySet()){
				ArrayList<STPoint> SPlist = getStayPointsUni(alldatamap.get(id).get(day),r,threshold);
				if(SPlist.size()>0){
					day_sps.put(day, SPlist);
				}
			}
			res.put(id, day_sps);
			day_sps = new HashMap<String,ArrayList<STPoint>>();
		}
		return res;
	}
	
	public static HashMap<String, HashMap<String,LinkedHashMap<Date,STPoint>>> generate_state //id_day_date_point
	(HashMap<String, HashMap<String, ArrayList<STPoint>>> map, HashMap<String,HashMap<String,ArrayList<STPoint>>> id_SPs, HashMap<String, ArrayList<String>> id_days) throws ParseException{
		HashMap<String, HashMap<String,LinkedHashMap<Date,STPoint>>> res = new HashMap<String, HashMap<String,LinkedHashMap<Date,STPoint>>>();
		HashMap<String,LinkedHashMap<Date,STPoint>>day_state = new HashMap<String,LinkedHashMap<Date,STPoint>>();
		for(String id : map.keySet()){
			for(String day : map.get(id).keySet()){
				if(id_SPs.get(id).size()>0){
					if(id_days.containsKey(id)&&id_days.get(id).contains(day)){
						System.out.println("id contains"+day);
							if(id_SPs.get(id).get(day)!=null){
								if(id_SPs.get(id).get(day).size()>=2){
									LinkedHashMap<Date,STPoint>state_list = getStateList(map.get(id).get(day), id_SPs.get(id).get(day));
									LinkedHashMap<Date,STPoint> statechain = continueChecker(state_list);
									day_state.put(day, statechain);
									}
								}	
							}
				}
			}
			res.put(id, day_state);
			day_state = new HashMap<String,LinkedHashMap<Date,STPoint>>();
		}
		return res;
	}

	
	public static ArrayList<STPoint> getStayPointsUni(ArrayList<STPoint> list, double h, double e) throws ParseException{
		HashMap<LonLat, ArrayList<STPoint>> map = new HashMap<LonLat, ArrayList<STPoint>>();
		map = clustering2dUni(list,h,e);
		ArrayList<STPoint> Cutmap = cutbyPointsbyStayTime(map);
		if(Cutmap.size()>0){
			ArrayList<STPoint> resmap = matome(Cutmap);
			return resmap;
		}
		else{
			return Cutmap;
		}
	}
	
	public static ArrayList<STPoint> matome(ArrayList<STPoint> list){
		ArrayList<STPoint> res = new ArrayList<STPoint>();
		res.add(list.get(0));
		for(STPoint point : list){
			int count = 0;
			for(LonLat p : res){
				if(point.distance(p)<1500){
					count++;
				}
			}
			if(count==0){
				res.add(point);
			}
		}
		return res;
	}
	
	public static ArrayList<STPoint> cutbyPointsbyStayTime(HashMap<LonLat, ArrayList<STPoint>> in) throws ParseException{
		ArrayList<STPoint> res = new ArrayList<STPoint>();
		for(LonLat p : in.keySet()){
			if(in.get(p).size()>5){
				if(overtenmins(in.get(p))==true){  //TODO change to "if staytime > 10mins"
					Collections.sort(in.get(p));
				
					    STPoint point = new STPoint(in.get(p).get(0).getTimeStamp(),in.get(p).get(in.get(p).size()-1).getTimeStamp(),p.getLon(),p.getLat());
					//	System.out.println(in.get(p).get(0).getTimeStamp()+","+in.get(p).get(in.get(p).size()-1).getTimeStamp()+","+p.getLon()+","+p.getLat());
					    res.add(point);
						
				}
			}
		}
		return res;
	}

	public static boolean overtenmins(ArrayList<STPoint> list){
		ArrayList<Integer> sortedtime = new ArrayList<Integer>();
		for(STPoint p : list){
			Date d = p.getTimeStamp();
			Integer day = Integer.valueOf(SDF_TS3.format(d))*86400;
			Integer time = StayPointTools.converttoSecs(SDF_TS.format(d));
			sortedtime.add(day+time);
		}
		Collections.sort(sortedtime);
		Integer starttime = 0;
		//		System.out.println("sorted array : " + sortedtime);
		if(sortedtime.size()>2){
			for(int i = 0; i<sortedtime.size(); i++){
				if((sortedtime.get(i)-starttime>1800)&&(sortedtime.get(i)-starttime<43200)){
					return true;
				}
				else if((sortedtime.get(i)-starttime>=43200)){
					starttime = sortedtime.get(i);
				}
			}
		}
		return false;
	}
	
	public static HashMap<LonLat,ArrayList<STPoint>> clustering2dUni(ArrayList<STPoint> data, double r, double e) {
		HashMap<LonLat,ArrayList<STPoint>> result = new HashMap<LonLat,ArrayList<STPoint>>();
		int N = data.size();
//				System.out.println("#number of points : "+N);

		for(STPoint point:data) {
			// seek mean value //////////////////
			LonLat mean = new LonLat(point.getLon(),point.getLat());

			//loop from here for meanshift
			while(true) {
				double numx = 0d;
				double numy = 0d;
				double din = 0d;
				for(int j=0;j<N;j++) {
					LonLat p = new LonLat(data.get(j).getLon(),data.get(j).getLat());
					double k = 0;
					if(p.distance(mean)<r){
						k = 1;
					}
					numx += k * p.getLon();
					numy += k * p.getLat();
					din  += k;
				}
				LonLat m = new LonLat(numx/din,numy/din);
				if( mean.distance(m) < e ) { mean = m; break; }
				mean = m;
			}
			//			System.out.println("#mean is : " + mean);
			// make cluster /////////////////////
			ArrayList<STPoint> cluster = null;
			for(LonLat p:result.keySet()) {
				if( mean.distance(p) < e ) { cluster = result.get(p); break; }
			}
			if( cluster == null ) {
				cluster = new ArrayList<STPoint>();
				result.put(mean,cluster);
			}
			cluster.add(point);
		}
		return result;
	}
	//sort logs into map
	public static HashMap<String, HashMap<String, ArrayList<STPoint>>> sortLogintoMap(File in, String mode) throws IOException, ParseException{
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
		BufferedReader br = new BufferedReader(new FileReader(in));
		HashMap<String, HashMap<String, ArrayList<STPoint>>> res = new HashMap<String, HashMap<String, ArrayList<STPoint>>>();
		String line = null;
		String prevline =null;
		while((line=br.readLine())!=null){
			if(ID_Extract_Tools.SameLogCheck(line,prevline)==true){
			String[] tokens = line.split(",");
			String id = tokens[0];
			String dt = tokens[1];
			Date date = SDF_TS2.parse(dt);
			String youbi = (new SimpleDateFormat("u")).format(date);
			if(daycodes.contains(youbi)){
				String day =dt.substring(5,7)+dt.substring(8,10);
				System.out.println(day+"---------------------------------------------");
				Double lon = Double.parseDouble(tokens[2]);
				Double lat = Double.parseDouble(tokens[3]);
				if(res.containsKey(id)){
					if(res.get(id).containsKey(day)){
						res.get(id).get(day).add(new STPoint(date,lon,lat));
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
			prevline = line;
				}
		br.close();
		return res;
	}
	
	public static LinkedHashMap<Date,STPoint> getStateList(ArrayList<STPoint> list, ArrayList<STPoint> id_SPs){
		LinkedHashMap<Integer,STPoint> temp = new LinkedHashMap<Integer,STPoint>();
		LinkedHashMap<Date,STPoint> res = new LinkedHashMap<Date,STPoint>();
				System.out.println("id_SP: " + id_SPs);
		Collections.sort(list);
		int count = 1;
		for(int i = 0; i<list.size(); i++){
//			System.out.println(list.get(i).getTimeStamp());
			for(STPoint sp : id_SPs){
//				System.out.println(sp.getDtStart());
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

	public static LinkedHashMap<Date,STPoint> continueChecker(LinkedHashMap<Date,STPoint> locchain) throws ParseException{
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
		if(converttotimeslot(endtime)<47){
			res.put(SDF_TS.parse("23:59:59"),prev);
			System.out.println("1234");
		}
		return res;
	}
	
	public static ArrayList<String>MeshList(ArrayList<Double>extent){//get meshList in level 3 in a rectangle
		ArrayList<String>MeshList = new ArrayList<String>();
		double minLat = extent.get(0);
		double minLon = extent.get(1);
		double maxLat = extent.get(2)+0.00833;
		double maxLon = extent.get(3)+0.0125; 
		double temp_lat = minLat;
		double temp_lon = minLon;
		while(temp_lat <= maxLat ){
			while(temp_lon <= maxLon){
				Mesh mesh = new Mesh(3,temp_lon,temp_lat);
				MeshList.add(mesh.getCode());
				temp_lon += 0.0125;
			}
			temp_lon = minLon;
			temp_lat += 0.008333;
		}
		return MeshList;
	}
	
//	public static File writeout(HashMap<String, HashMap<String,LinkedHashMap<Date,STPoint>>> map, String path) throws IOException{
//		File out = new File(path);
//		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
//
//		for(String id : map.keySet()){
//			for(String day:map.get(id).keySet()){
//				for(Date t : map.get(id).get(day).keySet()){
//					Mesh mesh = new Mesh(3,map.get(id).get(day).get(t).getLon(),map.get(id).get(day).get(t).getLat());
//					if(t!=null){
//					bw.write(id + "," +day+","+converttotimeslot(t)+ "," + mesh.getCode());
//					//map.get(id).get(t).getLon()+','+map.get(id).get(t).getLat());
//					bw.newLine();
//					}
//				}
//			}
//		}
//		bw.close();
//		return out;
//	}
	
	public static int converttotimeslot(Date t){
		int secs = Tools.converttoSecs(SDF_TS.format(t));//interval 30min
		return secs/1800;
	}
	
	public static void writeout(HashMap<String, HashMap<String,LinkedHashMap<Date,STPoint>>> trajectory) throws IOException{
		for(String id:trajectory.keySet()){
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/Data/forIRL/mesh5339/Transportation/StateSpace_"+id+".csv"));
			for(String day:trajectory.get(id).keySet()){
				for(Date date:trajectory.get(id).get(day).keySet()){
					Mesh mesh = new Mesh(3,trajectory.get(id).get(day).get(date).getLon(),trajectory.get(id).get(day).get(date).getLat());
					if(date!=null){
					bw.write(id + "," +day+","+converttotimeslot(date)+ "," + mesh.getCode());
					//map.get(id).get(t).getLon()+','+map.get(id).get(t).getLat());
					bw.newLine();
					}
				}
			}
			bw.close();
		}
	}
	
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
}
