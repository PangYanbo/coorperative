package SigSpatial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.Mesh;

public class Result {
	
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
	
	public static void agent_pop(String path, HashMap<String,HashMap<Integer,Double>>mesh_hour_pop) throws IOException{
		File agentf = new File(path);  
        File[] agentfiles = agentf.listFiles();
        int count = 0;
        for(File file:agentfiles){
        	if(file.isFile()){
        		BufferedReader br = new BufferedReader(new FileReader(file));
            	String line = null;
            	while((line = br.readLine())!=null){
            		String tokens[] = line.split(",");
                	Integer hour = Integer.valueOf(tokens[0])/2;
                	if(hour == 6){count++;}
                	String meshid = tokens[1];
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
        }
        System.out.println("6:00 counts: "+count);
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
	
	public static void write_scatter(String out, HashMap<String,HashMap<Integer,Double>>mesh_hour_pop1,HashMap<String,HashMap<Integer,Double>>mesh_hour_pop2) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(out)));
		for(String meshid:mesh_hour_pop1.keySet()){
			bw.write(meshid+",");
			for(int i = 6;i<24;i++){
				if(mesh_hour_pop1.get(meshid).containsKey(i)){
					bw.write(mesh_hour_pop1.get(meshid).get(i)+",");
				}else{
					bw.write("0.0"+",");
				}
			}
			bw.newLine();
		}
		bw.close();
	}
	
	public static void pt_rg(String path, HashMap<String,Double>id_rg)throws IOException{
		HashMap<String,ArrayList<LonLat>>id_logs=new HashMap<String,ArrayList<LonLat>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = null;
		while((line = br.readLine())!=null){
			String tokens[] = line.split(",");
			String uid = tokens[0];
			LonLat point = new LonLat(Double.valueOf(tokens[9]),Double.valueOf(tokens[8]));
			if(!id_logs.containsKey(uid)){
				ArrayList<LonLat>logs = new ArrayList<LonLat>();
				logs.add(point);
				id_logs.put(uid, logs);
			 }else{
				 id_logs.get(uid).add(point);
			 }
		}
		for(String uid : id_logs.keySet()){
			Double rg = calculateRG(id_logs.get(uid));
			id_rg.put(uid, rg);
		}
		br.close();
	}
	
	public static void gps_rg(String path, HashMap<String,Double>id_rg) throws IOException{
		HashMap<String,ArrayList<LonLat>>id_logs=new HashMap<String,ArrayList<LonLat>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = null;
		while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 String uid = tokens[1];
			 Mesh mesh = new Mesh(tokens[3]);
			 if(!id_logs.containsKey(uid)){
				ArrayList<LonLat>logs = new ArrayList<LonLat>();
				logs.add(mesh.getCenter());
				id_logs.put(uid, logs);
			 }else{
				 id_logs.get(uid).add(mesh.getCenter());
			 }
		}
			for(String uid : id_logs.keySet()){
				Double rg = calculateRG(id_logs.get(uid));
				id_rg.put(uid, rg);
			}
			br.close();
	}
	
	public static void agent_rg(String path, HashMap<String,Double>agentid_rg) throws IOException{
		File agentf = new File(path);  
        File[] agentfiles = agentf.listFiles();
        HashMap<String,ArrayList<LonLat>>id_logs=new HashMap<String,ArrayList<LonLat>>();
        for(File file:agentfiles){
        	if(file.isFile()){
        		BufferedReader br = new BufferedReader(new FileReader(file));
        		String uid = file.getName().substring(0, 20);
        		String line = br.readLine();
            	while((line = br.readLine())!=null){
            		String tokens[] = line.split(",");
            		Mesh mesh = new Mesh(tokens[1]);
            		if(!id_logs.containsKey(uid)){
        				ArrayList<LonLat>logs = new ArrayList<LonLat>();
        				logs.add(mesh.getCenter());
        				id_logs.put(uid, logs);
        			 }else{
        				 id_logs.get(uid).add(mesh.getCenter());
        			 }
            	}
            	br.close();
        	}
        }
        for(String uid:id_logs.keySet()){
        	Double rg = calculateRG(id_logs.get(uid));
			agentid_rg.put(uid, rg);
        }
	}
	
	public static void writerg(String out, HashMap<String,Double>id_rg) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(out)));
		bw.write("id,rg");
		bw.newLine();
		for(String id:id_rg.keySet()){
			bw.write(id+","+id_rg.get(id));
			bw.newLine();
		}
		bw.close();
	}
	
	public static Double calculateRG(ArrayList<LonLat> list){
		Double tempsum_lon = 0d;
		Double tempsum_lat = 0d;

		for(LonLat p : list){
		Double lon = p.getLon();
		Double lat = p.getLat();
		tempsum_lon = tempsum_lon + lon;
		tempsum_lat = tempsum_lat + lat;
		}
		Double avg_lon = tempsum_lon/(double)list.size();
		Double avg_lat = tempsum_lat/(double)list.size();
		LonLat center = new LonLat(avg_lon,avg_lat);

		Double tempsum = 0d;
		for(LonLat p : list){
		Double distance = p.distance(center);
		Double distance_2 = Math.pow(distance, 2);
		tempsum = tempsum + distance_2;
		}

		Double RG = Math.pow((tempsum/(double)list.size()), 0.5);
		return RG;
		}
	
	public static void pt_sp(String path, HashMap<String, Integer>id_spcounts)throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line =null;
		HashSet<LonLat>splist = new HashSet<LonLat>();
		LonLat prevsp = new LonLat();
		String previd = null;
		int temp = 1;
		while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 String id = tokens[0];
			 LonLat sp = new LonLat(Double.valueOf(tokens[9]),Double.valueOf(tokens[8]));
			 if(!id_spcounts.containsKey(id)){
				 id_spcounts.put(id, 0);
				 splist = new HashSet<LonLat>();
			 }else{
				 if(sp.equals(prevsp)){
					 temp += 1;
				 }else{
					 if(temp>0){
						 splist.add(prevsp);
						 id_spcounts.put(previd,splist.size());
						 temp = 1;
					 }
				 }
			 }
			 prevsp = sp;
			 previd = id;
		}
	}
	
	public static void gps_sp(String path,HashMap<String,Integer>id_spcounts) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = null;
		String prevmesh = null;
		String previd = null;
		HashSet<String>sp = new HashSet<String>();
		int temp = 1;
		while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 String id = tokens[1];
			 String meshid = tokens[3];
			 if(!id_spcounts.containsKey(id)){
				 id_spcounts.put(id, 0);
				 sp = new HashSet<String>();
			 }else{
				 if(meshid.equals(prevmesh)){
					 temp += 1;
				 }else{
					 if(temp>0){
						 sp.add(prevmesh);
						 id_spcounts.put(previd,sp.size());
						 temp = 1;
					 }
				 }
			 }
			 prevmesh = meshid;
			 previd = id;
		}
	}
	
	public static void agent_sp(String path,HashMap<String,Integer>agent_spcounts) throws IOException{
		File agentf = new File(path);  
        File[] agentfiles = agentf.listFiles();
        for(File file:agentfiles){
        	if(file.isFile()){
        		BufferedReader br = new BufferedReader(new FileReader(file));
        		String uid = file.getName().substring(0, 20);
        		String line = br.readLine();
            	String prevmesh = null;
            	int temp = 1;
            	HashSet<String>sp = new HashSet<String>();
            	while((line = br.readLine())!=null){
            		String tokens[] = line.split(",");
            		String mesh = tokens[1];
            		if(mesh.equals(prevmesh)){
            			temp += 1;
            		}else{
            			if(temp>0){
            				sp.add(prevmesh);
            				temp = 1;
            			}
            		}
            		prevmesh = mesh;
            	}
            	agent_spcounts.put(uid, sp.size());
        		sp = new HashSet<String>();
        		br.close();
        	}
        }
	}
	
	public static void writesp(String out,HashMap<String,Integer>id_spcounts) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(out)));
		bw.write("id, sp counts");
		bw.newLine();
		for(String id:id_spcounts.keySet()){
			bw.write(id+","+id_spcounts.get(id));
			bw.newLine();
		}
		bw.close();
	}
	
	public static Double pearson1(HashMap<String,Double>pop1,HashMap<String,Double>pop2){
		double pearson = 0.0;
		double sum_pop1 = 0.0;
		double sum_pop2 = 0.0;
		double sum_pop1_square = 0.0;
		double sum_pop2_square = 0.0;
		double sum_pop1pop2 = 0.0;
		//System.out.println(pop1.size()+","+pop2.size());
		for(String meshid:pop1.keySet()){
			if(pop2.containsKey(meshid)){
				sum_pop1 += pop1.get(meshid);
				sum_pop2 += pop2.get(meshid);
				sum_pop1_square += pop1.get(meshid)*pop1.get(meshid);
				sum_pop2_square += pop2.get(meshid)*pop2.get(meshid);
				sum_pop1pop2 += pop1.get(meshid)*pop2.get(meshid);
			}else{
				sum_pop1 += pop1.get(meshid);
				sum_pop2 += 0;
				sum_pop1_square += pop1.get(meshid)*pop1.get(meshid);
				sum_pop2_square += 0;
				sum_pop1pop2 += 0;
			}
		}
		//System.out.println(sum_pop1+","+sum_pop2+","+sum_pop1_square+sum_pop2_square+sum_pop1pop2);
		pearson = (sum_pop1pop2-sum_pop1*sum_pop2/pop1.size())/Math.sqrt((sum_pop1_square-Math.pow(sum_pop1,2)/pop1.size())*(sum_pop2_square-Math.pow(sum_pop2,2)/pop1.size()));
		return pearson;
	}
	
	public static HashMap<Integer,HashMap<String,Double>>convert_hash(HashMap<String,HashMap<Integer,Double>>mesh_hour_pop){
		HashMap<Integer,HashMap<String,Double>>hour_mesh_pop = new HashMap<Integer,HashMap<String,Double>>();
		HashMap<String,Double>temp = new HashMap<String,Double>();
		for(int i=6; i<=23;i++){
			temp = new HashMap<String,Double>();
			for(String meshid:mesh_hour_pop.keySet()){
				if(mesh_hour_pop.get(meshid).containsKey(i)){
					temp.put(meshid, mesh_hour_pop.get(meshid).get(i));
					//System.out.println(i+","+meshid+","+mesh_hour_pop.get(meshid).get(i));
				}else{
					temp.put(meshid, 0.0);
				}
			}
			hour_mesh_pop.put(i, temp);
		}
		return hour_mesh_pop;
	}
	
	public static void main(String args[]) throws IOException{
//		HashMap<String,Double>pt_rg = new HashMap<String,Double>();
//		HashMap<String,Integer>pt_sps = new HashMap<String,Integer>();
//		pt_rg("D:/training data/pflow.csv",pt_rg);
//		pt_sp("D:/training data/pflow.csv",pt_sps);
//		writerg("D:/training data/ptrg.csv",pt_rg);
//		writesp("D:/training data/ptsps.csv",pt_sps);
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte target date in format of yyyyMMdd");
		String date = in.nextLine();
		HashMap<String,HashMap<Integer,Double>>gps_mesh_hour_pop = new HashMap<String,HashMap<Integer,Double>>();
	//	HashMap<String,HashMap<Integer,Double>>agent_mesh_hour_pop = new HashMap<String,HashMap<Integer,Double>>();
		HashMap<String,HashMap<Integer,Double>>pt_mesh_hour_pop = new HashMap<String,HashMap<Integer,Double>>();
	//	HashMap<String,Double>gps_rg = new HashMap<String,Double>();
	//	HashMap<String,Double>agent_rg = new HashMap<String,Double>();
	//	HashMap<String,Integer>gps_sps = new HashMap<String,Integer>();
	//	HashMap<String,Integer>agent_sps = new HashMap<String,Integer>();
		gps_pop("/home/t-iho/Result/trainingdata/trainingdata"+date+".csv",gps_mesh_hour_pop);
		gps_pop("/home/t-iho/Result//UbiResult/Synthetic"+date+"/"+"all.csv",pt_mesh_hour_pop);
		//agent_pop("home/t-iho/Result/UbiResult/Synthetic"+date+"/",pt_mesh_hour_pop);
		HashMap<Integer,HashMap<String,Double>>gps_hour_mesh_pop = convert_hash(gps_mesh_hour_pop);
		HashMap<Integer,HashMap<String,Double>>pt_hour_mesh_pop = convert_hash(pt_mesh_hour_pop);
	//	HashMap<Integer,HashMap<String,Double>>agent_hour_mesh_pop = convert_hash(agent_mesh_hour_pop);
		System.out.println("pearson correlation coefficient 1");
		for(int i = 6;i<24;i++){
			System.out.println(pearson1(pt_hour_mesh_pop.get(i),gps_hour_mesh_pop.get(i)));
		}
		System.out.println("pearson correlation coefficient 2");
		for(int i = 6;i<24;i++){
			System.out.println(pearson1(gps_hour_mesh_pop.get(i),pt_hour_mesh_pop.get(i)));
		}
//		gps_rg("/home/t-iho/Sig/expData"+date+"inslot.csv", gps_rg);
//		agent_rg("/home/t-iho/Result/movement/"+date+"/",agent_rg);
//		gps_sp("/home/t-iho/Sig/expData"+date+"inslot.csv", gps_sps);
//		agent_sp("/home/t-iho/Result/movement/"+date+"/",agent_sps);
//		writepop("/home/t-iho/Sig/"+date+"gpspop.csv",gps_mesh_hour_pop);
//		writepop("/home/t-iho/Sig/"+date+"agentpop.csv",agent_mesh_hour_pop);
//		writerg("/home/t-iho/Sig/"+date+"gpsrg.csv",gps_rg);
//		writerg("/home/t-iho/Sig/"+date+"agentrg.csv",agent_rg);
//		writesp("/home/t-iho/Sig/"+date+"gpssps.csv",gps_sps);
//		writesp("/home/t-iho/Sig/"+date+"agentsps.csv",agent_sps);
	}
}
