package extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import MobilityAnalyser.Tools;
import jp.ac.ut.csis.pflow.geom.STPoint;
import process.GeometryChecker;

public class ExtractOD {
	
	public static ArrayList<String> zone = new ArrayList<>(Arrays.asList("Adachi Ku","Arakawa Ku","Bunkyo Ku","Chiyoda Ku","Chiyoda Ku","Chuo Ku","Edogawa Ku","Itabashi Ku","Katsushika Ku","Kita Ku","Koto Ku","Meguro Ku","Minato Ku","Nakano Ku","Ota Ku","Setagaya Ku","Shibuya Ku","Shinagawa Ku","Shinjuku Ku","Suginami Ku","Sumida Ku","Taito Ku","Toshima Ku","Others"));
	
	public static void ZonalFlow_raw(File shpdir,HashMap<String,HashMap<String,HashMap<Integer,Integer>>>ori_des_time_count,HashMap<String,ArrayList<STPoint>>id_points){
		GeometryChecker inst = new GeometryChecker(shpdir);
		STPoint prevPoint = new STPoint();
		int usercount = 0;
		for(String id:id_points.keySet()){
			usercount++;
			if(usercount%1000==0){
				System.out.println("finish "+usercount+" users");
			}
			prevPoint = new STPoint();
			for(STPoint point:id_points.get(id)){
				if(!prevPoint.isValid()){
					prevPoint = point;
					continue;
				}
				String origin = inst.listOverlaps("laa",prevPoint.getLon(), prevPoint.getLat()).size()==0?"Others":inst.listOverlaps("laa",prevPoint.getLon(), prevPoint.getLat()).get(0);
				String destination = inst.listOverlaps("laa",point.getLon(), point.getLat()).size()==0?"Others":inst.listOverlaps("laa",point.getLon(), point.getLat()).get(0);;
				origin = zone.contains(origin)?origin:"Others";
				destination  = zone.contains(destination)?destination:"Others";
//				if(zone.contains(origin)&&zone.contains(destination)){
					if(!origin.equals(destination)){
						Integer slot = Tools.converttoSecs(SDF_TS.format(prevPoint.getTimeStamp()))/3600;
						if(ori_des_time_count.containsKey(origin)){
							if(ori_des_time_count.get(origin).containsKey(destination)){
								if(ori_des_time_count.get(origin).get(destination).containsKey(slot)){
									int count = ori_des_time_count.get(origin).get(destination).get(slot)+1;
									ori_des_time_count.get(origin).get(destination).put(slot, count);
								}else{
									ori_des_time_count.get(origin).get(destination).put(slot, 1);
								}
							}else{
								HashMap<Integer,Integer>slot_count = new HashMap<Integer,Integer>();
								slot_count.put(slot,1);
								ori_des_time_count.get(origin).put(destination, slot_count);
							}
						}else{
							HashMap<Integer,Integer>slot_count = new HashMap<Integer,Integer>();
							HashMap<String,HashMap<Integer,Integer>>destination_slot_count = new HashMap<String,HashMap<Integer,Integer>>();
							slot_count.put(slot,1);
							destination_slot_count.put(destination, slot_count);
							ori_des_time_count.put(origin, destination_slot_count);
						}
					}
//				}
				
				prevPoint = point;
			}
		}
	}
	
	public static void ZonalFlow_SP(File shpdir,HashMap<String,HashMap<String,HashMap<Integer,Integer>>>ori_des_time_count,HashMap<String,ArrayList<STPoint>>id_points){
		/*
		 * used for stay point version
		 * */
		GeometryChecker inst = new GeometryChecker(shpdir);
		STPoint prevPoint = new STPoint();
		int usercount = 0;
		for(String id:id_points.keySet()){
			usercount++;
			if(usercount%1000==0){
				System.out.println("finish "+usercount+" users");
			}
			prevPoint = new STPoint();
			for(STPoint point:id_points.get(id)){
				if(!prevPoint.isValid()){
					prevPoint = point;
					continue;
				}
				String origin = inst.listOverlaps("laa",prevPoint.getLon(), prevPoint.getLat()).size()==0?"Others":inst.listOverlaps("laa",prevPoint.getLon(), prevPoint.getLat()).get(0);
				String destination = inst.listOverlaps("laa",point.getLon(), point.getLat()).size()==0?"Others":inst.listOverlaps("laa",point.getLon(), point.getLat()).get(0);;
				origin = zone.contains(origin)?origin:"Others";
				destination  = zone.contains(destination)?destination:"Others";
//				if(zone.contains(origin)&&zone.contains(destination)){
					
						Integer slot = Tools.converttoSecs(SDF_TS.format(prevPoint.getDtEnd()))/3600;
						if(ori_des_time_count.containsKey(origin)){
							if(ori_des_time_count.get(origin).containsKey(destination)){
								if(ori_des_time_count.get(origin).get(destination).containsKey(slot)){
									int count = ori_des_time_count.get(origin).get(destination).get(slot)+1;
									ori_des_time_count.get(origin).get(destination).put(slot, count);
								}else{
									ori_des_time_count.get(origin).get(destination).put(slot, 1);
								}
							}else{
								HashMap<Integer,Integer>slot_count = new HashMap<Integer,Integer>();
								slot_count.put(slot,1);
								ori_des_time_count.get(origin).put(destination, slot_count);
							}
						}else{
							HashMap<Integer,Integer>slot_count = new HashMap<Integer,Integer>();
							HashMap<String,HashMap<Integer,Integer>>destination_slot_count = new HashMap<String,HashMap<Integer,Integer>>();
							slot_count.put(slot,1);
							destination_slot_count.put(destination, slot_count);
							ori_des_time_count.put(origin, destination_slot_count);
						}			
//				}			
				prevPoint = point;
			}
		}
	}
	
	public static void writeout(File out,HashMap<String,HashMap<String,HashMap<Integer,Integer>>>ori_des_time_count) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.write("origin,destination,00:00-01:00,01:00-02:00,02:00-03:00,03:00-04:00,04:00-05:00,05:00-06:00,06:00-07:00,07:00-08:00,08:00-09:00,09:00-10:00,10:00-11:00,11:00-12:00,12:00-13:00,13:00-14:00,14:00-15:00,15:00-16:00,16:00-17:00,17:00-18:00,18:00-19:00,19:00-20:00,20:00-21:00,21:00-22:00,22:00-23:00,23:00-24:00");
		bw.newLine();
		for(String origin:ori_des_time_count.keySet()){
			for(String destination:ori_des_time_count.get(origin).keySet()){
				bw.write(origin+","+destination+",");
				for(int i = 0; i <= 23; i++){
					if(ori_des_time_count.get(origin).get(destination).containsKey(i)){
						bw.write(ori_des_time_count.get(origin).get(destination).get(i)+",");
					}else{
						bw.write(0+",");
					}
				}
				bw.newLine();
			}
		}
		bw.close();
	}
}
