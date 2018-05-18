package SigSpatial;

import java.io.IOException;
import java.util.Random;
import java.util.HashMap;
import java.util.Scanner;

import jp.ac.ut.csis.pflow.geom.Mesh;

public class FastEMD {
	
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte target date in format of yyyyMMdd");
		String date = in.nextLine();
		HashMap<String,HashMap<Integer,Double>>gps_mesh_hour_pop = new HashMap<String,HashMap<Integer,Double>>();
		HashMap<String,HashMap<Integer,Double>>agent_mesh_hour_pop = new HashMap<String,HashMap<Integer,Double>>();
		Result.gps_pop("/home/t-iho/Sig/expData"+date+"inslot.csv",gps_mesh_hour_pop);
		Result.agent_pop("/home/t-iho/Result/movement/"+date+"/",agent_mesh_hour_pop);
		
		for(int hour =6;hour<=23;hour++){
		
			HashMap<String,Double>agent = new HashMap<String,Double>();
			HashMap<String,Double>gps = new HashMap<String,Double>();
			
			double gps_total = 0.0;
			double agent_total = 0.0;
			
			for(String mesh:gps_mesh_hour_pop.keySet()){
				if(gps_mesh_hour_pop.get(mesh).get(hour)!=null){
					gps.put(mesh, gps_mesh_hour_pop.get(mesh).get(hour));
				}else{
					gps.put(mesh, 0.0);
				}
			}
			System.out.println(gps.size());
			for(String mesh:agent_mesh_hour_pop.keySet()){
				if(agent_mesh_hour_pop.get(mesh).get(hour)!=null){
				agent.put(mesh, agent_mesh_hour_pop.get(mesh).get(hour));
				}else{
					agent.put(mesh, 0.0);
				}
			}
			System.out.println(agent.size());
			for(String mesh:gps.keySet()){
				if(!agent.containsKey(mesh)){
					agent.put(mesh, 0.0);
				}
				//System.out.println(agent.get(mesh));
				agent_total += agent.get(mesh);
			}
			for(String mesh:agent.keySet()){
				if(!gps.containsKey(mesh)){
					gps.put(mesh, 0.0);
				}
				gps_total += gps.get(mesh);
			}		
		
		
		
			int N = gps.size();
		    
		
			double[] P = new double[N];
			double[] Q = new double[N];
			double[][] C= new double[N][N];
			int i = 0;
			for(String mesh:gps.keySet()){
				int j = 0;
				Mesh mesh_p = new Mesh(mesh);
				for(String mesh2:agent.keySet()){
					Mesh mesh_q = new Mesh(mesh2);
					P[i]=gps.get(mesh)/gps_total;
					Q[j]=agent.get(mesh2)/agent_total;
					C[i][j]= mesh_p.getCenter().distance(mesh_q.getCenter());
					j++;
				}
				i++;
			}
			double extra_mass_penalty= -1;
	        double dist= emd_hat.dist_gd_metric(P,Q,C,extra_mass_penalty,null);
	        System.out.print("Distance in hour "+i+" ==");
	        System.out.println(dist);
		}
	}
}

