package simulator;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.ac.ut.csis.pflow.dbi.PgLoader;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.routing2.res.Node;
import jp.ac.ut.pflow.example.input.CHYPersonTrip;
import jp.ac.ut.pflow.example.input.DrmBicycleNetworkLoader;
import jp.ac.ut.pflow.example.input.DrmVehicleNetworkLoader;
import jp.ac.ut.pflow.example.input.DrmWalkNetworkLoader;
import jp.ac.ut.pflow.example.input.GenericAgentLoader;
import jp.ac.ut.pflow.example.input.GenericTrip;
import jp.ac.ut.pflow.example.input.PTAgentLoader;
import jp.ac.ut.pflow.example.input.RailNetworkLoader;
import jp.ac.ut.pflow.example.input.TKYPersonTrip;
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

public class SimulatorTest implements IAController {
	/** 髢句ｧ区凾髢�					*/	private long mStartTime;
	/** 邨ゆｺ�譎る俣					*/	private long mEndTime;
	/** 						*/	private int mNumLoopStep;
	/** 繧ｷ繝溘Η繝ｬ繝ｼ繧ｿ				*/	private Simulator mSimulator;
	/** 						*/	private TrafficController mTrafficController;
	/** 						*/	private SNetwork mNetwork;
	/** 						*/	private List<Agent> mListAgents;

	
	public int load() {
		//
		this.mStartTime = 0;
		this.mEndTime = 8 * 3600 * 1000;
		this.mNumLoopStep = 720;
		
		// DBConnection Pool
		PgLoader pgLoader = new PgLoader(
				Pgsqlhost,
				Pgsqlport,
				Pgsqlid,
				Pgsqlpw,
				Pgsqldb,
				Pgsqlencoding);
		System.out.println("start load network");
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
		System.out.println("finish load network");
//csvagentloader
		File file = new File("D:/training data/pflow.csv");
		GenericAgentLoader agentLoader = new GenericAgentLoader(new GenericTrip(), file);
//		PTAgentLoader agentLoader = new PTAgentLoader(
//		new TKYPersonTrip(), "pt_kanto.pflow", pgLoader);	
		mListAgents = agentLoader.load(mNetwork, mStartTime, mEndTime);
		System.out.println("finish load agents");
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
		
		//
		FixedFile fixedFile = new FixedFile(new File(TempDirectory, "result.txt"));
		BasicWriter basicWriter = new BasicWriter(fixedFile, 0);
		mTrafficController.addFilter(new WriterFilter(1, basicWriter));
		
		// simulator
		mSimulator = new Simulator(mTrafficController, mEndTime, mNumLoopStep);
		mTrafficController.initialize();

		System.out.println("initialize end");
		return 0;
	}
	
	@Override
	public int initialize() {
		return load();
	}
	
	
	
	/** PostgreSQL host			*/	private static final String Pgsqlhost = "localhost";
	/** PostgreSQL port			*/	private static final int    Pgsqlport = 5432;
	/** PostgreSQL ID			*/	private static final String Pgsqlid = "postgres";
	/** PostgreSQL PW			*/	private static final String Pgsqlpw = "pyb1989327";
	/** PostgreSQL DB			*/	private static String Pgsqldb = "simulator";
	/** PostgreSQL encoding		*/	private static final String Pgsqlencoding = "UTF-8";
	/** Flow Directory			*/	private static String TempDirectory = "C:/Users/PangYanbo/Desktop/simulator";
									private static double[] NetworkRect = {138.446, 34.867, 140.895, 36.746};

									
	
	public static void main(String[] args){
		SimulatorTest st = new SimulatorTest();
		new Assimilator(st).assimilate();
	}

	@Override
	public boolean next() {
		boolean next = false;
		System.out.println("assimilator next");
		try{		
			// simulator
			next = mSimulator.next();
		}catch(Exception e){
			e.printStackTrace();
		}
		// 
		return next;
	}
}
