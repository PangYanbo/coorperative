package process;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jp.ac.ut.csis.pflow.geom.Mesh;

public class NodeAssignment {
	
	public static HashMap<String,ArrayList<String>> MeshToNode(File in) throws IOException{
		HashMap<String,ArrayList<String>>mesh_node = new HashMap<String,ArrayList<String>>();
		
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = br.readLine();
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			Integer gid = Integer.valueOf(tokens[0]);
			String source = tokens[1];
			String target = tokens[2];
			Double source_x = Double.valueOf(tokens[3]);
			Double source_y = Double.valueOf(tokens[4]);
			Double target_x = Double.valueOf(tokens[5]);
			Double target_y = Double.valueOf(tokens[6]);
			
			
			if(source_x<140&&source_x>139&&source_y>35.33&&source_y<36){
				Mesh mesh_s = new Mesh(3, source_x, source_y);
				
				if(!mesh_node.containsKey(mesh_s.getCode())){
					ArrayList<String> node = new ArrayList<String>();
					node.add(source);
					mesh_node.put(mesh_s.getCode(), node);
				}else{
					if(!mesh_node.get(mesh_s.getCode()).contains(source)){
						mesh_node.get(mesh_s.getCode()).add(source);
					}
				}
			}
//			if(target_x<140&&target_x>139&&target_y>35.33&&target_y<36){
//				Mesh mesh_t = new Mesh(3, target_x, target_y);
//			
//				if(!mesh_node.containsKey(mesh_t.getCode())){
//					node = new ArrayList<String>();
//					node.add(target);
//					mesh_node.put(mesh_t.getCode(), node);
//					
//				}else{
//					if(!mesh_node.get(mesh_t.getCode()).contains(target)){
//						mesh_node.get(mesh_t.getCode()).add(target);
//					}
//				}
//			}
		}
	
		return mesh_node;
	}
	
	public static void main(String[] args) throws IOException{
		File in = new File("D:/drmallroad2503.csv");
		HashMap<String,ArrayList<String>>mesh_node = MeshToNode(in);
		for(String mesh:mesh_node.keySet()){
			for(String node:mesh_node.get(mesh)){
				System.out.println(node);
			}
		}
		
	}
	
}
