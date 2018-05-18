package test;

import jp.ac.ut.pflow.sim.routing.RouteSearcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import jp.ac.ut.csis.pflow.dbi.PgLoader;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.routing2.logic.Dijkstra;
import jp.ac.ut.csis.pflow.routing2.res.Node;
import jp.ac.ut.csis.pflow.routing2.res.Route;
import jp.ac.ut.pflow.example.input.DrmBicycleNetworkLoader;
import jp.ac.ut.pflow.example.input.DrmVehicleNetworkLoader;
import jp.ac.ut.pflow.example.input.DrmWalkNetworkLoader;
import jp.ac.ut.pflow.example.input.GenericTrip;
import jp.ac.ut.pflow.example.input.RailNetworkLoader;
import jp.ac.ut.pflow.sim.Simulator;
import jp.ac.ut.pflow.sim.agent.ETransport;
import jp.ac.ut.pflow.sim.ctrl.TrafficController;
import jp.ac.ut.pflow.sim.network.SNetwork;
import jp.ac.ut.pflow.sim.network.SSNetwork;

public class RouteTest {
	/** 髢句ｧ区凾髢�					*/	private long mStartTime;
	/** 邨ゆｺ�譎る俣					*/	private long mEndTime;
	/** 						*/	private int mNumLoopStep;
	/** 繧ｷ繝溘Η繝ｬ繝ｼ繧ｿ				*/	private Simulator mSimulator;
	/** 						*/	private TrafficController mTrafficController;
	/** 						*/	private static SNetwork mNetwork;
	
	
	/** PostgreSQL host			*/	private static final String Pgsqlhost = "localhost";
	/** PostgreSQL port			*/	private static final int    Pgsqlport = 5432;
	/** PostgreSQL ID			*/	private static final String Pgsqlid = "postgres";
	/** PostgreSQL PW			*/	private static final String Pgsqlpw = "pyb1989327";
	/** PostgreSQL DB			*/	private static String Pgsqldb = "simulator";
	/** PostgreSQL encoding		*/	private static final String Pgsqlencoding = "UTF-8";
	/** Flow Directory			*/	private static String TempDirectory = "C:/Users/PangYanbo/Desktop/simulator";
									private static double[] NetworkRect = {138.446, 34.867, 140.895, 36.746};

									private static final double COST_TOLERANCE = 6 * 3600 * 1000;
									
	public static void main(String[] args) throws NumberFormatException, IOException{
		long startTime = System.currentTimeMillis();
		PgLoader pgLoader = new PgLoader(
				Pgsqlhost,
				Pgsqlport,
				Pgsqlid,
				Pgsqlpw,
				Pgsqldb,
				Pgsqlencoding);
		
		mNetwork = new SNetwork();
		new DrmWalkNetworkLoader(
				"data2503.drmallroad", pgLoader, NetworkRect).load(mNetwork);
		new DrmBicycleNetworkLoader(
				"data2503.drmallroad", pgLoader, NetworkRect).load(mNetwork);
		new DrmVehicleNetworkLoader(
				"data2503.drmbaseroad", pgLoader, NetworkRect).load(mNetwork);
		new RailNetworkLoader(
				"railway.railnetwork", pgLoader, NetworkRect).load(mNetwork);	
		
		Dijkstra dijkstra = new Dijkstra();
		GenericTrip mPersonTrip =  new GenericTrip();
		
		BufferedReader br = new BufferedReader(new FileReader("D:/training data/pflow.csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/testchecker.csv"));
		String line = null;
		
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",");
			String id = tokens[0];
			
			int transport = Integer.valueOf(tokens[7]);
			
			Double ori_lon = Double.valueOf(tokens[8]);
			Double ori_lat = Double.valueOf(tokens[9]);

			Double dest_lon = Double.valueOf(tokens[10]);
			Double dest_lat = Double.valueOf(tokens[11]);
			
			if(transport!=97){
				ETransport tcode = mPersonTrip.getSimCode(transport);
				
				SSNetwork subnetwork = mNetwork.getNetwork(tcode.getNetwork());
				Node source = dijkstra.getNearestNode(subnetwork, ori_lon, ori_lat,500);
				Node target = dijkstra.getNearestNode(subnetwork, dest_lon, dest_lat,500);
				
			
				
				Route route = null;
				route = dijkstra.getRoute(subnetwork, source, target);
				if(route != null && route.getCost() <= COST_TOLERANCE) 
				{
				System.out.println(route.listLinks());} 
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("finished reading one line: "+(endTime-startTime)+"ms");
	}
}
