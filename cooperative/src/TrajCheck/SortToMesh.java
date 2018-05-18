package TrajCheck;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Scanner;

import SigSpatial.GpsProcess;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;
import jp.ac.ut.csis.pflow.geom.Trip;
import process.PTSample;

public class SortToMesh {
	
	public static void main(String args[]) throws IOException, ParseException{
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte target date in format of yyyyMMdd");
		String date = in.nextLine();
		File file = new File("/home/t-iho/Result/trainingdata/trainingdata"+date+".csv");
		HashMap<String,ArrayList<Trip>>id_trips = new HashMap<String,ArrayList<Trip>>();
		HashMap<String,HashMap<Integer,ArrayList<STPoint>>>id_slot_points = new HashMap<String,HashMap<Integer,ArrayList<STPoint>>>();
		HashMap<String,LinkedHashMap<Integer,String>>id_traj = new HashMap<String,LinkedHashMap<Integer,String>>();
		System.out.println("generate Traj now");
		PTSample.SortToMap(file,id_trips);
		generateTraj(date, id_trips);
	}
	
	public static void generateTraj(String date, HashMap<String,ArrayList<Trip>>id_trips) throws IOException{
		System.out.println("generate Traj now");
		
		for(String id:id_trips.keySet()){	
			
			ArrayList<Trip>trips = PTSample.mergeTrip(id_trips.get(id));
			
			if(trips.size()>0){
				String mesh = new Mesh(3,trips.get(0).getStartPoint().getLon(),trips.get(0).getStartPoint().getLat()).getCode();
				File path = new File("/home/t-iho/Result/trainingdata/"+date+"/"+mesh+"/");
				if(!path.exists()){
					path.mkdirs();
					System.out.println(path);
				}
				BufferedWriter bw = new BufferedWriter(new FileWriter(path+"/"+id+".csv"));
				
				for(int i = 0; i<trips.size();i++){
					
					Integer t1 = GpsProcess.ConvertToTimeSlot(trips.get(i).getStartPoint().getTimeStamp());
					Integer t2 = GpsProcess.ConvertToTimeSlot(trips.get(i).getEndPoint().getTimeStamp());
					Mesh start = new Mesh(3,trips.get(i).getStartPoint().getLon(),trips.get(i).getStartPoint().getLat());
					Mesh end = new Mesh(3,trips.get(i).getEndPoint().getLon(),trips.get(i).getEndPoint().getLat());
				//	Mesh next_start = new Mesh(3,id_trips.get(id).get(i+1).pstart.getLon(),id_trips.get(id).get(i+1).pstart.getLat());
					if(t1<12&&t2<12){
						continue;
					}
					if(t1<12&&t2>12){
						for(int j =12;j<t2;j++){
							bw.write(id+","+j+","+start.getCode()+","+start.getCode()+","+"stay");
							bw.newLine();
						}
					}
					if(i==0&&t1>12){
						for(int j =12;j<t1;j++){
							bw.write(id+","+j+","+start.getCode()+","+start.getCode()+","+"stay");
							bw.newLine();
						}
					}
					if(t2==t1+1){
						if(t1>=12){
						bw.write(id+","+t1+","+start.getCode()+","+end.getCode()+","+trips.get(i).getMode2());
						bw.newLine();
						}
					}
					if(t2>t1+1){
						trips.get(i).getMode2();
						if("stay".equals(trips.get(i).getMode2())){
							for(int j=t1;j<t2;j++){
								if(j>=12){
							
								bw.write(id+","+j+","+start.getCode()+","+end.getCode()+","+trips.get(i).getMode2());
								bw.newLine();
										
								}
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
				bw.close();
			}
	
		}
	}
	
}
