package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.google.common.collect.ImmutableList;
import jp.ac.ut.assim.Assimilator;
import jp.ac.ut.assim.IAController;
import jp.ac.ut.assim.utils.GenericEvaluation;
import jp.ac.ut.csis.pflow.dbi.PgLoader;
import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.routing2.res.Node;
import jp.ac.ut.pflow.example.input.CHYPersonTrip;
import jp.ac.ut.pflow.example.input.DrmBicycleNetworkLoader;
import jp.ac.ut.pflow.example.input.DrmVehicleNetworkLoader;
import jp.ac.ut.pflow.example.input.DrmWalkNetworkLoader;
import jp.ac.ut.pflow.example.input.PTAgentLoader;
import jp.ac.ut.pflow.example.input.RailNetworkLoader;
import jp.ac.ut.pflow.example.output.BasicWriter;
import jp.ac.ut.pflow.example.output.FixedFile;
import jp.ac.ut.pflow.example.output.WriterFilter;
import jp.ac.ut.pflow.sim.Simulator;
import jp.ac.ut.pflow.sim.agent.Agent;
import jp.ac.ut.pflow.sim.ctrl.ITrafficLogic;
import jp.ac.ut.pflow.sim.ctrl.QueuingLogic;
import jp.ac.ut.pflow.sim.ctrl.TrafficController;
import jp.ac.ut.pflow.sim.network.ENetwork;
import jp.ac.ut.pflow.sim.network.SNetwork;

/**
 *縲�
 * @author T.KASHIYAMA@IIS. UT.
 * @since 2014/07/31
 */
public class Controller implements IAController{
	/** 髢句ｧ区凾髢�					*/	private long mStartTime;
	/** 邨ゆｺ�譎る俣					*/	private long mEndTime;
	/** 						*/	private int mNumLoopStep;
	/** 繧ｷ繝溘Η繝ｬ繝ｼ繧ｿ				*/	private Simulator mSimulator;
	/** 						*/	private TrafficController mTrafficController;
	/** 						*/	private SNetwork mNetwork;
	/** 						*/	private List<Agent> mListAgents;
	/** 						*/	private Set<Long> mTargetMesh4s;
	
	/** 						*/	private int mHour;
	/** 						*/	private IODTrip mODTrip;
	
	/** 						*/	private int MODE = 2;
	
	public int load() {
		int InitialHour = (MODE!=1) ? 3 : 0;
		//
		this.mStartTime = InitialHour * 3600 * 1000;
		this.mEndTime = mStartTime + (24-InitialHour) * 3600 * 1000;
		this.mNumLoopStep = 720;
		this.mHour = InitialHour;
		
		// DBConnection Pool
		PgLoader pgLoader = new PgLoader(
				Pgsqlhost,
				Pgsqlport,
				Pgsqlid,
				Pgsqlpw,
				Pgsqldb,
				Pgsqlencoding);
		
		// NetworkLoader 
		mNetwork = new SNetwork();
		new DrmWalkNetworkLoader(
				"data2503.drmallroad", pgLoader, NetworkRect).load(mNetwork);
		new DrmBicycleNetworkLoader(
				"data2503.drmallroad", pgLoader, NetworkRect).load(mNetwork);
		new DrmVehicleNetworkLoader(
				"data2503.drmbaseroad", pgLoader, NetworkRect).load(mNetwork);
		new RailNetworkLoader(
				"railway.railnetwork", pgLoader, NetworkRect).load(mNetwork);	
		
		// 
		mTargetMesh4s = loadMeshList(new File(PATH_TARGET_MESH4));
		
		// AgentLoader
		if (MODE == 1){
			PTAgentLoader agentLoader = new PTAgentLoader(
			new CHYPersonTrip(), "od_pt_chukyo.pflow", pgLoader);	
			
			mListAgents = agentLoader.load(mNetwork, mStartTime, mEndTime);
		}else if(MODE == 2){
			mListAgents = PopDistribution.generateAgents(new File(PATH_POPULATION));	
		}
		
		// check area
		Iterator<Agent> iter = mListAgents.iterator();
		while(iter.hasNext()){
			Agent agent = (Agent)iter.next();
			int nodeid = agent.getTrip().getDepId();
			Node node = mNetwork.getSNode(nodeid);
			if (node != null){
				Mesh mesh4 = new Mesh(4, node.getLon(), node.getLat());
				if (mTargetMesh4s.contains(Long.valueOf(mesh4.getCode()))){
					continue;
				}
			}
			iter.remove();
		}
		
		// Network Pool
		pgLoader.close();
		
		// Logics
		Map<ENetwork, ITrafficLogic> mapLogics = new HashMap<ENetwork, ITrafficLogic>();
		mapLogics.put(ENetwork.TRAIN, new QueuingLogic());
		mapLogics.put(ENetwork.WALK, new QueuingLogic());
		mapLogics.put(ENetwork.VEHICLE, new QueuingLogic());
		mapLogics.put(ENetwork.BICYCLE, new QueuingLogic());
		
		// network controller
		mTrafficController = new TrafficController(
				mStartTime, mNetwork, mListAgents, mapLogics);	
		
//		// writer	
//		MeshWriter meshWriter = new MeshWriter(new StepFile(new File(TempDirectory), "a"), 5);
//		WriterFilter meshfilter = new WriterFilter(mNumLoopStep, meshWriter);
//		mTrafficController.addFilter(meshfilter);
		

		FixedFile fixedFile = new FixedFile(new File(TempDirectory, "result.txt"));
		BasicWriter basicWriter = new BasicWriter(fixedFile, 0);
		mTrafficController.addFilter(new WriterFilter(1, basicWriter));
		
		// simulator
		mSimulator = new Simulator(mTrafficController, mEndTime, mNumLoopStep);
		mTrafficController.initialize();

		// OD trip
		switch (MODE){
		case 2:
			//(mODTrip = new OpenPTTripTKY(mNetwork, mListAgents)).initialize();break;
			(mODTrip = new OpenPTTripCHU(mNetwork, mListAgents)).initialize();break;
		}
		
		System.out.println("initialize end");
		return 0;
	}
		
	@Override
	public int initialize() {
		return load();
	}
	
	private static Set<Long> loadMeshList(File file){
		Set<Long> ret = new HashSet<Long>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null){
				ret.add(Long.valueOf(line));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if (br != null)br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unused")
	private static String getFlowFileName(Date date, int hour){
		return String.format("%s%02d0000.csv", new SimpleDateFormat("yyyyMMdd").format(date), hour);
	}
	
	@SuppressWarnings("unused")
	private static String getWarpFileName(Date date, int hour){
		return String.format("w%s%02d0000.csv", new SimpleDateFormat("yyyyMMdd").format(date), hour);
	}
	
	private Map<Long, Double> getObsData(int hour){
		File file = new File(PATH_ZDC, String.format("zdc_%02d.csv", hour));
		Map<Long, Double> mapObs = ZDCLoaderFromPG.load(file);
		for (Iterator<Long> i = mapObs.keySet().iterator(); i.hasNext();){
			if (!mTargetMesh4s.contains(i.next())){
				i.remove();
			}
		}
		return mapObs;
	}

    private Map<Long, Double> getMeshDensity(List<Agent> listAgents, int meshLevel){
    	Map<Long, Double> map = new HashMap<Long, Double>();
    	for (Agent agent : listAgents){
    		LonLat lonlat = agent.getCurrentPosition();
    		if (lonlat != null){
	            double lon = lonlat.getLon();
	            double lat = lonlat.getLat();
	            Mesh mesh = new Mesh(meshLevel, lon, lat);
	            long meshCode = Long.parseLong(mesh.getCode());
	            double count = map.containsKey(meshCode) ? map.get(meshCode) : 0d;
	            map.put(meshCode, count + agent.getMfactor());
    		}
        }
        return map;
    }
    
	public boolean next() {
		boolean next = false;
		System.out.println("assimilator next");
		try{
			// MovingModel
			if (MODE != 1){
				mTrafficController.clear();
				System.out.println("Current Hour: " + (mHour % 24));
				mODTrip.updateAgent(mHour % 24);
				mTrafficController.reEntry();
			}
			
			// obs
			Map<Long, Double> mapObs = getObsData(mHour);
			System.out.println("ObsSize:" + mapObs.size());
			if (mapObs.size() > 0){
//				if (false)
//				{
//					// adjust obs
//					for (Agent agent : mListAgents){
//						LonLat lonlat = mNetwork.getSNode(agent.getCurrentNode());
//						if (lonlat != null){
//							Mesh mesh = new Mesh(MeshLevel, lonlat.getLon(), lonlat.getLat());
//							long meshCode = Long.valueOf(mesh.getCode());
//							if (!mapObs.containsKey(meshCode)){
//								mapObs.put(meshCode, 0d);
//							}
//						}
//					}
//					
//					// assimilate
//					Pair<Map<Long, Set<Long>>, KfDto> nudgingFlowAndKfDtoPair = NudgeFlowCalculator.calculateNudgingFLow(
//							ImmutableList.copyOf(mListAgents), ImmutableMap.copyOf(mapObs));
//					ImmutableMap<Long, Set<Long>> meshMovingAgentsMap = ImmutableMap.copyOf(nudgingFlowAndKfDtoPair.getFirst());
//					Map<Long, Long> mapMeshs = AssimilatorUtil.getAgentMovingMeshcode(meshMovingAgentsMap);
//					// Convert mesh to node
//					Network network = mNetwork.getNetwork(ENetwork.WALK);
//					Map<Long, Integer> mapNodes = new HashMap<Long, Integer>();
//					for (Map.Entry<Long, Long> entry : mapMeshs.entrySet()){
//						int nodeId = RandomNearestNode.getNode(network, entry.getValue());
//						if (nodeId > 0){
//							mapNodes.put(entry.getKey(), nodeId);
//						}
//					}
//					// Warp
//					mTrafficController.clear();
//					WarpOperation.execute(mListAgents, mapNodes);
//					mTrafficController.reEntry();
//					System.out.println(String.format("ObsSize: %d, WarpSize: %d", mapObs.size(), mapMeshs.size()));
//				}
				
				// evaluation
				ImmutableList<Agent> listAgents = ImmutableList.copyOf(mListAgents);
				Map<Long, Double> mapAgents = getMeshDensity(listAgents, MeshLevel);
				{
					double[][] arryMesh = GenericEvaluation.getTargetList(mapObs, mapAgents);
			        double scale = GenericEvaluation.scale(arryMesh[0], arryMesh[1]);        
			        System.out.println(String.format("[2]Evaluation NumAgents-SCALE(sim/obs)-RMSE-CORR: %d	%d	%f	%f	%f  %f  %f  %f",
			        		listAgents.size(),
			        		arryMesh[0].length,
			        		GenericEvaluation.sum(arryMesh[0]),
			        		GenericEvaluation.sum(arryMesh[1]),
			        		scale,
			                GenericEvaluation.rmse(arryMesh[0], arryMesh[1]),
			                GenericEvaluation.rmspe(arryMesh[0], arryMesh[1]),
			                GenericEvaluation.corr(arryMesh[0], arryMesh[1])
			        ));
				}
			}else{
				System.out.println("No observation data");
			}
			
			// simulator
			next = mSimulator.next();

			mHour++;
		}catch(Exception e){
			e.printStackTrace();
		}
		// 
		return next;
	}
	
	/** PostgreSQL host			*/	private static final String Pgsqlhost = "localhost";
	/** PostgreSQL port			*/	private static final int    Pgsqlport = 5432;
	/** PostgreSQL ID			*/	private static final String Pgsqlid = "postgres";
	/** PostgreSQL PW			*/	private static final String Pgsqlpw = "task4TH";
	/** PostgreSQL DB			*/	private static String Pgsqldb = "open_pflow";
	/** PostgreSQL encoding		*/	private static final String Pgsqlencoding = "UTF-8";
	/** Flow Directory			*/	private static String TempDirectory = "/home/ubuntu/Desktop/out";
//									private static double[] NetworkRect = {138.446, 34.867, 140.895, 36.746};
									private static double[] NetworkRect = {136.169, 34.413, 137.887, 35.924};
	
									private static int MeshLevel = 4;
//	/** 						*/	private static final String PATH_TARGET_MESH4 = "/home/ubuntu/workspace/sekilab/data/kanto/mesh4_list.csv";
//	/** 						*/	private static final String PATH_POPULATION = "/home/ubuntu/workspace/sekilab/data/kanto/population_20.csv";
//	/** 						*/	private static final String PATH_ZDC = "/home/ubuntu/workspace/sekilab/data/kanto/zdc";
	
	/** 						*/	private static final String PATH_TARGET_MESH4 = "/home/ubuntu/workspace/sekilab/data/chubu/mesh4_list.csv";
	/** 						*/	private static final String PATH_POPULATION = "/home/ubuntu/workspace/sekilab/data/chubu/population_20.csv";
	/** 						*/	private static final String PATH_ZDC = "/home/ubuntu/workspace/sekilab/data/chubu/zdc";
	
	public static int execute(){
		// init logger
		try {
			File dir = new File(TempDirectory);
			for (File file : dir.listFiles()){
				if (file.isFile()){
					file.delete();
				}
			}
			// Assimilator Controller
			Controller controller = new Controller();
			// Assimilator
			new Assimilator(controller).assimilate();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("controller start");		
		execute();
		System.out.println("controller end");
	}
}
