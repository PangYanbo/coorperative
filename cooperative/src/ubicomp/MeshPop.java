package ubicomp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class MeshPop {
	public static void gps_pop(String path, HashMap<String,HashMap<Integer,Double>>mesh_hour_pop) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = null;
		while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 String meshid = tokens[2];
			 Integer hour = Integer.valueOf(tokens[1])/2;
			 if(!mesh_hour_pop.containsKey(meshid)){
				 HashMap<Integer,Double>hour_pop = new HashMap<Integer,Double>();
				 hour_pop.put(hour, 0.5);
				 mesh_hour_pop.put(meshid, hour_pop);
				 //System.out.println("new mesh "+meshid);
			 }else{
				 if(!mesh_hour_pop.get(meshid).containsKey(hour)){
					 mesh_hour_pop.get(meshid).put(hour, 0.5);
					// System.out.println(meshid+" hour "+ hour);
				 }else{
					 Double temp = mesh_hour_pop.get(meshid).get(hour)+0.5;
					 mesh_hour_pop.get(meshid).put(hour, temp);
					// System.out.println(meshid+" hour "+ hour+ "pop"+ temp);
				 }
			 }
		}
		br.close();
	}
	
	public static void writepop(String out, HashMap<String,HashMap<Integer,Double>>mesh_hour_pop) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(out)));
		bw.write("meshid,06:00,07:00,08:00,09:00,10:00,11:00,12:00,13:00,14:00,15:00,16:00,17:00,18:00,19:00,20:00,21:00,22:00,23:00");
		bw.newLine();
		for(String meshid:mesh_hour_pop.keySet()){
			bw.write(meshid+",");
			for(int i = 6;i<24;i++){
				if(mesh_hour_pop.get(meshid).containsKey(i)){
					bw.write(mesh_hour_pop.get(meshid).get(i)+",");
				}else{
					bw.write("0.0"+",");
				}
			}
			bw.newLine();
		}
		bw.close();
	}
	
	public static void main(String args[]) throws IOException{

		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte target date in format of yyyyMMdd");
		String date = in.nextLine();
		HashMap<String,HashMap<Integer,Double>>gps_mesh_hour_pop = new HashMap<String,HashMap<Integer,Double>>();
//		gps_pop("/home/t-iho/Result/trainingdata/trainingdata"+date+".csv",gps_mesh_hour_pop);
//		writepop("/home/t-iho/Result/trainingdata/mesh_pop"+date+".csv",gps_mesh_hour_pop);
		gps_pop("D:/training data/PTtraj3.csv",gps_mesh_hour_pop);
		writepop("D:/pt_mesh_pop"+date+".csv",gps_mesh_hour_pop);
	}
}
