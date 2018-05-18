package SigSpatial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class Agent {

	public static HashMap<Integer,String>generateAgent_gps(HashMap<String,Integer>mesh_pop,Integer rate){
		HashMap<Integer,String>id_meshid = new HashMap<Integer,String>();
		int id = 1;
		for(String mesh_id:mesh_pop.keySet()){
			if(mesh_pop.get(mesh_id)>=15){
				int count = (mesh_pop.get(mesh_id)/rate>1)?mesh_pop.get(mesh_id)/rate:1;
				for(int i=0;i<count;i++){
					id_meshid.put(id, mesh_id);
					id++;
				}	
			}
		}
		return id_meshid;
	}
	
	public static HashMap<Integer,String>generateAgent(String path, Integer rate) throws IOException{
		HashMap<Integer,String>id_meshid = new HashMap<Integer,String>();
		HashMap<String,Integer>mesh_pop = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",");
			String mesh = tokens[0];
			Integer pop = Integer.valueOf(tokens[1]);
			mesh_pop.put(mesh, pop);
		}
		br.close();
		int id = 1;
		for(String mesh_id:mesh_pop.keySet()){
			int count = (mesh_pop.get(mesh_id)/rate>1)?mesh_pop.get(mesh_id)/rate:1;
			for(int i=0;i<count;i++){
				id_meshid.put(id, mesh_id);
				id++;
			}
		}
		System.out.println("finished generating agents");
		return id_meshid;
	}
	
public static ArrayList<String>getTarget(String meshid,HashMap<String,HashMap<String,LinkedHashMap<Integer,String>>>sorted_SPs){
		
		ArrayList<String>targetList = new ArrayList<String>();
		ArrayList<String>allList = new ArrayList<String>();
		Mesh center = new Mesh(meshid);
		int count = 15;
		allList.addAll(sorted_SPs.get(center.getCode()).keySet());
		while(targetList.size()<count&&allList.size()>1){
			Random rand = new Random();
			int index = rand.nextInt(allList.size()-1);
			targetList.add(allList.get(index));
			allList.remove(index);
		}
		return targetList;
	}
	
	
//	public static ArrayList<String>getTarget(String meshid,HashMap<String,HashMap<String,LinkedHashMap<State,String>>>sorted_SPs){
//		
//		ArrayList<String>targetList = new ArrayList<String>();
//		ArrayList<String>neighborList = new ArrayList<String>();
//		Mesh center = new Mesh(meshid);
//		List<Mesh>neighbors = center.list8Neighbors();
//		int count = 15;
//		neighbors.add(center);
//		for(Mesh mesh:neighbors){
//			if(sorted_SPs.containsKey(mesh.getCode())){
//				neighborList.addAll(sorted_SPs.get(mesh.getCode()).keySet());
//			}
//		}
//		while(targetList.size()<count&&neighborList.size()>1){
//			Random rand = new Random();
//			int index = rand.nextInt(neighborList.size()-1);
//			targetList.add(neighborList.get(index));
//			neighborList.remove(index);
//		}
//		return targetList;
//	}
	
	public static void writeout(HashMap<Integer,ArrayList<String>>id_targetList,String path) throws IOException{
		//an extra comma existing at the end of each line
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		for(Integer agentid:id_targetList.keySet()){
			for(String target:id_targetList.get(agentid)){
				bw.write(target+",");
			}
			bw.newLine();
		}
		bw.close();
	}
}
