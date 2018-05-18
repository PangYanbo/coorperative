package process;

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

import SigSpatial.Agent;
import SigSpatial.GpsProcess;
import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class PTProcess {

	public static void main(String[] args) throws Exception{
		//HashMap:mesh_id,did,point
		
		File file = new File("D:/training data/pflow.csv");
		HashMap<String,ArrayList<STPoint>>id_points = new HashMap<String,ArrayList<STPoint>>();
		HashMap<String,HashMap<Integer,ArrayList<STPoint>>>id_slot_points = new HashMap<String,HashMap<Integer,ArrayList<STPoint>>>();
		HashMap<String,LinkedHashMap<Integer,String>>id_traj = new HashMap<String,LinkedHashMap<Integer,String>>();
		HashMap<String,HashMap<String,LinkedHashMap<Integer,String>>>mesh_id_traj = new HashMap<String,HashMap<String,LinkedHashMap<Integer,String>>>();
		PTintoMap(file,id_points);
		SortToSlot(id_points,id_slot_points);
//		UserCheck(id_slot_points);
		for(String id:id_slot_points.keySet()){
			LinkedHashMap<Integer,String>traj = new LinkedHashMap<Integer,String>();
			traj = generateTraj(id_slot_points.get(id));
			id_traj.put(id, traj);
		}
		for(String id:id_traj.keySet()){
			String initial = id_traj.get(id).get(12);
			if(mesh_id_traj.containsKey(initial)){
				mesh_id_traj.get(initial).put(id, id_traj.get(id));
			}else{
				HashMap<String,LinkedHashMap<Integer,String>>idTraj = new HashMap<String,LinkedHashMap<Integer,String>>();
				idTraj.put(id, id_traj.get(id));
				mesh_id_traj.put(initial,idTraj);
			}
		}
		HashMap<String,Integer>mesh_pop = new HashMap<String, Integer>();
		for(String mesh_id:mesh_id_traj.keySet()){
			mesh_pop.put(mesh_id, mesh_id_traj.get(mesh_id).size());
		}
		System.out.println(mesh_id_traj.size());
		writeout(mesh_id_traj,"D:/training data/mesh_pt.csv");

	}
	
	public static void PTintoMap(File in,HashMap<String, ArrayList<STPoint>> id_points) throws IOException, ParseException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		int count = 0;
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			String id = tokens[0];
			count = Integer.valueOf(id);
			if(count%3==0){
				Double lat = Double.parseDouble(tokens[9]);
				Double lon = Double.parseDouble(tokens[8]);
				if(lat>=35.33&&lat<=36&&lon>=139&&lon<=140){	
					Date dt = SDF_TS2.parse(tokens[5]);
					String time = SDF_TS.format(dt);
					STPoint point = new STPoint(dt,lon,lat);
					if(!id_points.containsKey(id)){
						ArrayList<STPoint>points = new ArrayList<STPoint>();
						points.add(point);
						id_points.put(id, points);
					}else{
						id_points.get(id).add(point);
					}
				}
			}
		}
		br.close();
	}
	
	public static void SortToSlot(HashMap<String,ArrayList<STPoint>>id_points,HashMap<String,HashMap<Integer,ArrayList<STPoint>>>id_slot_points){
		long startTime = System.currentTimeMillis();
		GpsProcess.UserCheck(id_points);
		System.out.println(id_points.size()+" available ID for this day");
		for(String id: id_points.keySet()){
		
				for(int i=0;i<id_points.get(id).size();i++){
					STPoint point = id_points.get(id).get(i);
					int timeslot = GpsProcess.ConvertToTimeSlot(point.getTimeStamp());
					if(id_slot_points.containsKey(id)){
						if(id_slot_points.get(id).containsKey(timeslot)){
							id_slot_points.get(id).get(timeslot).add(point);
						}else{
							ArrayList<STPoint>points = new ArrayList<STPoint>();
							points.add(point);
							id_slot_points.get(id).put(timeslot, points);
						}
					}else{
						ArrayList<STPoint>points = new ArrayList<STPoint>();
						points.add(point);
						HashMap<Integer,ArrayList<STPoint>>slot_points = new HashMap<Integer,ArrayList<STPoint>>();
						slot_points.put(timeslot, points);
						id_slot_points.put(id, slot_points);
					}
				}	
				for(Integer t:id_slot_points.get(id).keySet()){
					Collections.sort(id_slot_points.get(id).get(t));
				}
			
		}
		long endTime = System.currentTimeMillis();
		System.out.println(id_slot_points.size());
		System.out.println("finished sorting data into slot, caculating time is: "+(endTime-startTime)+"ms");
	}
	
	public static void UserCheck(HashMap<String,HashMap<Integer,ArrayList<STPoint>>>id_slot_points){
		HashMap<String,HashMap<Integer,ArrayList<STPoint>>>map2 = new HashMap<String,HashMap<Integer,ArrayList<STPoint>>>();
		for(String id:id_slot_points.keySet()){
			if(id_slot_points.get(id).size()>=8){
				continue;
			}else{
				map2.put(id, id_slot_points.get(id));
			}
		}
		for(String id:map2.keySet()){
			id_slot_points.remove(id);
		}
		System.out.println(id_slot_points.size()+" available ID for this day");
	}
	
	public static LinkedHashMap<Integer,String> generateTraj(HashMap<Integer,ArrayList<STPoint>>slot_points){
		HashMap<Integer,String>temp = new HashMap<Integer,String>();
		LinkedHashMap<Integer,String>traj = new LinkedHashMap<Integer,String>();
		ArrayList<Integer>slot = new ArrayList<Integer>();
		for(int i = 0;i<48;i++){
			if(slot_points.containsKey(i)){
				slot.add(i);
				STPoint endPoint =slot_points.get(i).get(slot_points.get(i).size()-1);
				Mesh mesh = new Mesh(3,endPoint.getLon(),endPoint.getLat());
				temp.put(i, mesh.getCode());
//				System.out.println(i+","+mesh.getCode());
			}
		}
		//for the head: before 06:00
		if(slot.get(0)>12){
			for(int i =12;i<slot.get(0);i++){
				traj.put(i, temp.get(slot.get(0)));
			}
		}
		//fill the slot gap
		for(int i =0;i<slot.size()-1;i++){
			int t1 = slot.get(i);
			int t2 = slot.get(i+1);
			if(t2>t1+1){
				if(temp.get(t1).equals(temp.get(t2))){
					for(int j=t1;j<t2;j++){
						traj.put(j,temp.get(t1));
					}
				}else{
					Mesh mesh1 = new Mesh(temp.get(t1));
					Mesh mesh2 = new Mesh(temp.get(t2));
					LonLat p1 = mesh1.getCenter();
					LonLat p2 = mesh2.getCenter();
					LonLat middle = new LonLat((p1.getLon()+p2.getLon())*0.5,(p1.getLat()+p2.getLat())*0.5);
					Mesh mesh3 = new Mesh(3,middle.getLon(),middle.getLat());
					traj.put(t1, temp.get(t1));
					traj.put(t1+1, mesh3.getCode());
					traj.put(t1+2, temp.get(t2));
					if(t2>t1+2){
						for(int j = t1+3;j<t2;j++){
							traj.put(j,temp.get(t2));
						}
					}
				}
			}else{
				traj.put(t1,temp.get(t1));
				traj.put(t2,temp.get(t2));
			}
		}
		traj.put(slot.get(slot.size()-1), temp.get(slot.get(slot.size()-1)));
		//tail
		if(slot.get(slot.size()-1)<47){
			for(int i =slot.get(slot.size()-1);i<=47;i++){
				traj.put(i, temp.get(slot.get(slot.size()-1)));
			}
		}
		//cut logs before 6:00
		if(slot.get(0)<12){
			for(int i = slot.get(0);i<12;i++){
				traj.remove(i);
			}
		}
		return traj;
	}
	
	public static void writeout(HashMap<String,HashMap<String,LinkedHashMap<Integer,String>>>mesh_id_traj,String path) throws IOException{
		System.out.println("Start to write out files");
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		String action = null;
		for(String meshid:mesh_id_traj.keySet()){
			for(String id:mesh_id_traj.get(meshid).keySet()){
				for(Integer t:mesh_id_traj.get(meshid).get(id).keySet()){
					if(t<47){
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
	
	protected static final SimpleDateFormat SDF_TS = new SimpleDateFormat("HH:mm:ss");
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");
	
}
