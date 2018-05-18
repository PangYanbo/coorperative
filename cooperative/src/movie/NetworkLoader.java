package movie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import jp.ac.ut.csis.pflow.routing2.logic.Dijkstra;
import jp.ac.ut.csis.pflow.routing2.logic.DrmLinkCost;
import jp.ac.ut.csis.pflow.routing2.res.DrmLink;
import jp.ac.ut.csis.pflow.routing2.res.Link;
import jp.ac.ut.csis.pflow.routing2.res.Network;
import jp.ac.ut.csis.pflow.routing2.res.Node;
import jp.ac.ut.csis.pflow.routing2.res.Route;

public class NetworkLoader {
	
	public Network loadrail(File file){
		Network network = new Network();
		int count = 0;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while ((line = br.readLine()) != null){
				count++;
				if(count%1000000==0){
					System.out.println(count);
				}
				
				String[] tokens = line.split(",");
				
				String linkid = String.valueOf(tokens[0]);
				String source = String.valueOf(tokens[1]);
				String target = String.valueOf(tokens[2]);
				
				
				double lon1 = Double.valueOf(tokens[3]);
				double lat1 = Double.valueOf(tokens[4]);
				double lon2 = Double.valueOf(tokens[5]);
				double lat2 = Double.valueOf(tokens[6]);
				
				//if((lon1<140&&lon1>139&&lat1>35.33&&lat1<36)||(lon2<140&&lon2>139&&lat2>35.33&&lat2<36)){
					int road_type = Integer.valueOf(tokens[7]);
					int lane_num = Integer.valueOf(tokens[8]);
					double length = Double.valueOf(tokens[9]);

					Node srcNode = network.hasNode(source) ? network.getNode(source) : new Node(source, lon1, lat1);
					Node trgNode = network.hasNode(target) ? network.getNode(target) : new Node(target, lon2, lat2);
					
					DrmLink link = new DrmLink(linkid, srcNode, trgNode,length, length, false,road_type,0,lane_num);
					network.addLink(link);
				//}
				
			}
			br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return network;
	}
	
	public Network load(File file){
		Network network = new Network();
		int count = 0;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while ((line = br.readLine()) != null){
				count++;
				if(count%1000000==0){
					System.out.println(count);
				}
				
				String[] tokens = line.split(",");
				
				String linkid = String.valueOf(tokens[0]);
				String source = String.valueOf(tokens[1]);
				String target = String.valueOf(tokens[2]);
				
				
				double lon1 = Double.valueOf(tokens[3]);
				double lat1 = Double.valueOf(tokens[4]);
				double lon2 = Double.valueOf(tokens[5]);
				double lat2 = Double.valueOf(tokens[6]);
				
				if((lon1<140&&lon1>139&&lat1>35.33&&lat1<36)||(lon2<140&&lon2>139&&lat2>35.33&&lat2<36)){
					int road_type = Integer.valueOf(tokens[7]);
					int lane_num = Integer.valueOf(tokens[8]);
					double length = Double.valueOf(tokens[9]);

					Node srcNode = network.hasNode(source) ? network.getNode(source) : new Node(source, lon1, lat1);
					Node trgNode = network.hasNode(target) ? network.getNode(target) : new Node(target, lon2, lat2);
					
					DrmLink link = new DrmLink(linkid, srcNode, trgNode,length, length, false,road_type,0,lane_num);
					network.addLink(link);
				}
				
			}
			br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return network;
	}
	
	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		System.out.println("start");
//		// load
//		Network network = (new NetworkLoader()).load(new File("D:/drmallroad2503.csv"));
//		System.out.println(network.listLinks().size());
//		// routing
//		double x0,x1,y0,y1;
//		x0 = x1 = y0 = y1 = 0;
//		// set transport mode and do route-search
//		Dijkstra routing = new Dijkstra(new DrmLinkCost(DrmLinkCost.Mode.WALK));
//		Route route = routing.getRoute(network, x0, y0, x1, y1);
//		for (Link link : route.listLinks()){
//			//result
//			System.out.println(link.getLinkID());
//		}
//		System.out.println("end");
//	}
}
