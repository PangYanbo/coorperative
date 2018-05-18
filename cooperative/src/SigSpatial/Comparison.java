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

import jp.ac.ut.csis.pflow.geom.Mesh;

public class Comparison {
	
	public static void distCompare(String gpsPath, String agentPath) throws IOException{
	//	 HashMap<String,HashMap<Integer,HashMap<Integer,String>>>id_day_state = new HashMap<String,HashMap<Integer,HashMap<Integer,String>>>();
		 HashMap<String,HashMap<String,Double>>agent_day_distance = new HashMap<String, HashMap<String,Double>>();
		 HashMap<String,HashMap<String,Double>>gps_day_distance = new HashMap<String, HashMap<String,Double>>();
		 File agentf = new File(agentPath);  
	     File[] agentfiles = agentf.listFiles();
	     for(File file:agentfiles){
	    	 if(file.isFile()){
	    		 BufferedReader br = new BufferedReader(new FileReader(file));
		        	String line = br.readLine();
		        	String tokens[] = line.split(",");
		        	String initial_time = tokens[0];
		        	String initial_mesh = tokens[2];
		        	HashMap<String,Double>temp = new HashMap<String,Double>();
		        	HashMap<String,Double>temp2 = new HashMap<String,Double>();
		        	String agentId = file.getName().substring(17).replace(".csv", "");
		        	File gpsFile = new File(gpsPath+"StateSpace_"+agentId+".csv");
		        	String gpsDay = findDay(gpsFile,initial_time,initial_mesh);
		        	String day = "fake_day";
		        	double agentDist = agentDist(file);
		        	double gpsDist = gpsDist(gpsFile,gpsDay);
		        	temp.put(day, agentDist);
		        	temp2.put(day, gpsDist);
		        	agent_day_distance.put(agentId, temp);
		        	gps_day_distance.put(agentId, temp2);
		        	br.close(); 
	    	 }
	        }
	     distwriteout(agent_day_distance,"/home/t-iho/Data/forIRL/Result/agentDist.csv");
	     distwriteout(gps_day_distance,"/home/t-iho/Data/forIRL/Result/gpsDist.csv");
	}
	
	public static void locCompare(String gpsPath, String agentPath) throws IOException{
		 HashMap<String,HashMap<String,Integer>>agent_day_sps = new HashMap<String, HashMap<String,Integer>>();
		 HashMap<String,HashMap<String,Integer>>gps_day_sps = new HashMap<String, HashMap<String,Integer>>();
		 File agentf = new File(agentPath);  
	     File[] agentfiles = agentf.listFiles();
	     for(File file:agentfiles){
	    	 if(file.isFile()){
	    		 BufferedReader br = new BufferedReader(new FileReader(file));
		        	String line = br.readLine();
		        	String tokens[] = line.split(",");
		        	String initial_time = tokens[0];
		        	String initial_mesh = tokens[2];
		        	HashMap<String,Integer>temp = new HashMap<String,Integer>();
		        	HashMap<String,Integer>temp2 = new HashMap<String,Integer>();
		        	String agentId = file.getName().substring(17).replace(".csv", "");
		        	File gpsFile = new File(gpsPath+"StateSpace_"+agentId+".csv");
		        	String gpsDay = findDay(gpsFile,initial_time,initial_mesh);
		//        	String day = "fake_day";
		        	Integer agentLoc = agentLoc(file);
		        	Integer gpsLoc = gpsLoc(gpsFile,gpsDay);
		        	temp.put(gpsDay, agentLoc);
		        	temp2.put(gpsDay, gpsLoc);
		        	agent_day_sps.put(agentId, temp);
		        	gps_day_sps.put(agentId, temp2);
		        	br.close(); 
	    	 }
	        }
	     locwriteout(agent_day_sps,"/home/t-iho/Data/forIRL/Result/agentLoc.csv");
	     locwriteout(gps_day_sps,"/home/t-iho/Data/forIRL/Result/gpsLoc.csv");
	}
	
	public static Integer agentLoc(File in) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		HashSet<String>spList = new HashSet<String>();
		while((line = br.readLine())!=null){
			String tokens[] = line.split(",");
			String sp  = tokens[2];
			spList.add(sp);
		}
		br.close();
		return spList.size();
	}
	
	public static Integer gpsLoc(File in, String day) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		HashSet<String>spList = new HashSet<String>();
		while((line = br.readLine())!=null){
			String tokens[] = line.split(",");
			if(tokens[1].equals(day)){
				String sp  = tokens[3];
				spList.add(sp);
			}
		}
		br.close();
		return spList.size();
	}
	
	public static Double agentDist(File in) throws IOException{
		double distance = 0.0;
		String prev_mesh = "null";
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		
		while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 Mesh mesh = new Mesh(tokens[2]);
			 if(!tokens[2].equals(prev_mesh)&&!prev_mesh.equals("null")){
				 Mesh prev = new Mesh(prev_mesh);
				 distance += mesh.getCenter().distance(prev.getCenter());
				 System.out.println(distance);
				 prev_mesh = tokens[2];
			 }
			 if(prev_mesh.equals("null")){
				 prev_mesh = tokens[2];
			 }	 
		}
		br.close();
		return distance;
	}
	public static Double gpsDist(File in,String day) throws IOException{
		double distance = 0.0;
		String prev_mesh = "null";
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 String Day = tokens[1];
			 if(Day.equals(day)){
				 Mesh mesh = new Mesh(tokens[3]);
				 if(!tokens[3].equals(prev_mesh)&&!prev_mesh.equals("null")){
					 Mesh prev = new Mesh(prev_mesh);
					 distance += mesh.getCenter().distance(prev.getCenter());
					 System.out.println(distance);
					 prev_mesh = tokens[3];
				 }
				 if(prev_mesh.equals("null")){
					 prev_mesh = tokens[3];
				 }	 
			 }			 
		}
		br.close();
		return distance;
	}
	
	public static void spCompare(String gpsPath, String agentPath) throws NumberFormatException, IOException{
		 BufferedReader br = new BufferedReader(new FileReader(gpsPath));
		 String line = null;
		 Mesh prev_mesh = new Mesh();
		 while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 String uid = tokens[0];
			 Integer day = Integer.valueOf(tokens[1]);
			 Integer timeslot = Integer.valueOf(tokens[2]);
			 Mesh mesh = new Mesh(tokens[3]);
			 if(!prev_mesh.getCode().equals(null)){
				
			 }
		 }
		 br.close();
	}
	
	public static void motifCompare(String agentPath,String gpsPath) throws IOException{
		File agentf = new File(agentPath);  
        File[] agentfiles = agentf.listFiles();
        HashMap<String,HashMap<String,Integer>>agentId_day_motif = new HashMap<String,HashMap<String,Integer>>();
        HashMap<String,HashMap<String,Integer>>gpsId_day_motif = new HashMap<String,HashMap<String,Integer>>();
        for(File file:agentfiles){
        	if(file.isFile()){
        		BufferedReader br = new BufferedReader(new FileReader(file));
            	String line = br.readLine();
            	String tokens[] = line.split(",");
            	String initial_time = tokens[0];
            	String initial_mesh = tokens[2];
            	HashMap<String,Integer>temp = new HashMap<String,Integer>();
            	HashMap<String,Integer>temp2 = new HashMap<String,Integer>();
            	String agentId = file.getName().substring(17).replace(".csv", "");
            	File gpsFile = new File(gpsPath+"StateSpace_"+agentId+".csv");
            	String gpsDay = findDay(gpsFile,initial_time,initial_mesh);
            	String day = "fake_day";
            	int agentMotif = motifFinder_agent(file);
            	int gpsMotif = motifFinder_gps(gpsFile,gpsDay);
            	temp.put(day, agentMotif);
            	temp2.put(day, gpsMotif);
            	agentId_day_motif.put(agentId, temp);
            	gpsId_day_motif.put(agentId, temp2);
            	br.close();	
        	}
        }
        
        motifWriteout(agentId_day_motif,"/home/t-iho/Data/forIRL/Result/agentMotif.csv");
        motifWriteout(gpsId_day_motif,"/home/t-iho/Data/forIRL/Result/gpsMotif.csv");
	}
	
	
	public static void motifagent(String agentPath) throws IOException{
		File agentf = new File(agentPath);  
        File[] agentfiles = agentf.listFiles();
        HashMap<String,HashMap<String,Integer>>agentId_day_motif = new HashMap<String,HashMap<String,Integer>>();
        for(File file:agentfiles){
        	if(file.isFile()){
        		BufferedReader br = new BufferedReader(new FileReader(file));
            	String line = br.readLine();
            	String tokens[] = line.split(",");
            	String initial_time = tokens[0];
            	String initial_mesh = tokens[2];
            	HashMap<String,Integer>temp = new HashMap<String,Integer>();
            	HashMap<String,Integer>temp2 = new HashMap<String,Integer>();
            	String agentId = file.getName().substring(17).replace(".csv", "");
            	String day = "fake_day";
            	int agentMotif = motifFinder_agent(file);
 //           	int gpsMotif = motifFinder_gps(gpsFile,gpsDay);
            	temp.put(day, agentMotif);
//            	temp2.put(day, gpsMotif);
            	agentId_day_motif.put(agentId, temp);
//            	gpsId_day_motif.put(agentId, temp2);
            	br.close();	
        	}
        }
        
        motifWriteout(agentId_day_motif,"/home/t-iho/Result/agentMotif.csv");
 //       motifWriteout(gpsId_day_motif,"/home/t-iho/Data/forIRL/Result/gpsMotif.csv");
	}
	
	public static String findDay(File in, String time,String mesh) throws IOException{
		String day = null;
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = br.readLine();
		String tokens[] = line.split(",");
		day = tokens[1];
		while((line = br.readLine())!=null){
			tokens = line.split(",");
			day = tokens[1];
			String timeslot = tokens[2];
			String mesh_id = tokens[3];
			if(time==timeslot&&mesh==mesh_id){
				break;
			}
		}
		br.close();
		return day;
	}
	
	public static void locwriteout(HashMap<String,HashMap<String,Integer>>id_day_sps,String out) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(String id:id_day_sps.keySet()){
			for(String day: id_day_sps.get(id).keySet()){
				bw.write(id+","+day+","+id_day_sps.get(id).get(day));
				bw.newLine();
			}
		}
		bw.close();
	}
	
	public static void distwriteout(HashMap<String,HashMap<String,Double>>id_day_dist,String out) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(String id:id_day_dist.keySet()){
			for(String day: id_day_dist.get(id).keySet()){
				bw.write(id+","+day+","+id_day_dist.get(id).get(day));
				bw.newLine();
			}
		}
		bw.close();
	}
	
	public static void motifWriteout(HashMap<String,HashMap<String,Integer>>map, String out) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(out)));
		HashMap<Integer,Double> res = new HashMap<Integer,Double>();
		HashMap<Integer,Integer> temp = new HashMap<Integer,Integer>();
		int count = 0;
		for(String id : map.keySet()){
			for(String day : map.get(id).keySet()){
				Integer motif = map.get(id).get(day);
				if(temp.containsKey(motif)){
					int counter = temp.get(motif);
					counter = counter + 1;
					temp.put(motif, counter);
				}
				else{
					temp.put(motif, 1);
				}
				count++;
			}
		}
		for(Integer m : temp.keySet()){
			Double wariai = (double)temp.get(m)/(double)count;
			res.put(m, wariai);
			bw.write(m +","+wariai*100);
			bw.newLine();
		}
		System.out.println("#done calculating motifs");
		bw.close();
	}
	
	public static Integer motifFinder_agent(File file) throws IOException{
		 int count = 1;
		 ArrayList<Integer>res = new ArrayList<Integer>();
		 HashMap<String,Integer>sp_count = new HashMap<String,Integer>();
		 BufferedReader br = new BufferedReader(new FileReader(file));
		 String line = null;
		 int m = 0;
		 HashMap<String,Integer>sp_temp = new HashMap<String,Integer>();
		 while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 String sp = tokens[2];
			 if(m ==0){
				 sp_temp.put(sp, 1);
			 }
			 m++;
			 if(!sp_temp.containsKey(sp)){
				 sp_temp.put(sp, 0);
			 }else{
				 int temp = sp_temp.get(sp)+1;
				 sp_temp.put(sp, temp);
				// System.out.println("+1");
			 }
		 }
		 br.close();
		 BufferedReader br2 = new BufferedReader(new FileReader(file));
		 while((line = br2.readLine())!=null){
			 String tokens[] = line.split(",");
			 String sp = tokens[2];
			 if(!sp_count.containsKey(sp)){
				 if(sp_temp.get(sp)>0){
					 sp_count.put(sp, count);
					 count++;	 
				 }
			 }
			 if(sp_count.containsKey(sp)){
				 if(!sp_count.get(sp).equals(null)){
					 res.add(sp_count.get(sp));
				 }
			 }
			 
			
			// System.out.println(sp_count.get(sp));
		 }
		 br2.close();
		 return motifs(continueChecker(res));
	}
	
	public static Integer motifFinder_gps(File in,String _day) throws IOException{
		int count = 1;
		 ArrayList<Integer>res = new ArrayList<Integer>();
		 HashMap<String,Integer>sp_count = new HashMap<String,Integer>();
		 BufferedReader br = new BufferedReader(new FileReader(in));
		 String line = null;
		 while((line = br.readLine())!=null){
			 String tokens[] = line.split(",");
			 String day = tokens[1];
			 if(day.equals(_day)){
				 String sp = tokens[3];
				 if(!sp_count.containsKey(sp)){
					 sp_count.put(sp, count);
					 count++;
				 }
				 res.add(sp_count.get(sp));
			 }
		 }
		 br.close();
		 return motifs(continueChecker(res));
	}
	
	public static ArrayList<Integer>continueChecker(ArrayList<Integer>locchain){
		ArrayList<Integer> res = new ArrayList<Integer>();
		int prev = 99;
		for(Integer i : locchain){
			if(i!=prev){
				res.add(i);
			}
			prev = i;
		}
		return res;
	}
	
	public static Integer motifs(ArrayList<Integer> locchain){
		if(locchain.size()==1){
			return 1;
		}
		else if(locchain.size()==2){
		//	System.out.println("2 nodes... something wrong");
			return 99;
		}
		else if(locchain.size()==3){
			return 2; 
		}
		else if(locchain.size()==4){
			return 4;
		}
		else if(locchain.size()==5){
			if(locchain.get(3)==4){
				return 7;
			}
			else if(locchain.get(2)==3){
				return 5;
			}
			else{
				return 3;
			}
		}
		else if(locchain.size()==6){
			if(locchain.get(4)==5){
				return 11;
			}
			else{
				return 6;
			}
		}
		else if(locchain.size()==9){
			return 16;
		}
		else if(locchain.size()==8){
			if(locchain.contains(6)){
				if((locchain.get(3)==1)||(locchain.get(4)==1)){
					return 17;
				}
				else{
					return 14;
				}
			}
			else{
				return 12;
			}
		}
		else if(locchain.size()==7){
			if(locchain.contains(6)){
				return 15;
			}
			else if(locchain.get(3)==1){
				return 13;
			}
			else if(locchain.contains(5)){
				return 10;
			}
			else if(locchain.get(2)==1&&locchain.get(4)==1){
				return 8;
			}
			else{
				return 9;
			}
		}
		else{
		//	System.out.println(locchain);
			return 0;
		}
	}
	
	public static void main(String args[]) throws IOException{
//		String gpsPath = "/home/t-iho/Data/forIRL/mesh5339/Transportation/";
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte target date in format of yyyyMMdd");
		String date = in.nextLine();
		String agentPath = "/home/t-iho/Result/movement/"+date+"/";
//		distCompare(gpsPath,agentPath);
		motifagent(agentPath);
//		locCompare(gpsPath,agentPath);
	}
}
