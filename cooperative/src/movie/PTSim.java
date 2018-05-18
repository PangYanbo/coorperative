package movie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import jp.ac.ut.csis.pflow.routing2.logic.Dijkstra;
import jp.ac.ut.csis.pflow.routing2.logic.DrmLinkCost;
import jp.ac.ut.csis.pflow.routing2.res.Link;
import jp.ac.ut.csis.pflow.routing2.res.Network;
import jp.ac.ut.csis.pflow.routing2.res.Node;
import jp.ac.ut.csis.pflow.routing2.res.Route;
import process.NodeAssignment;

public class PTSim {
/*
 * this class is used for simulate the location chain generated from UbiIRL
 * 
 * Input: id, discrete time, start(mesh code), end, transportation mode
 * Output: id, time stamp, lon, lat, mode 
 * 
 * function: route research, node assignment
 * */
	
	public static void main(String[] args) throws IOException, ParseException{
		
		Random random = new Random();
		
		System.out.println("Type in the files date:");
		Scanner in = new Scanner(System.in);
		
		String date = in.nextLine();
		
//		File input = new File("/home/t-iho/Result/trainingdata/trainingdata"+date+".csv");
//		File out = new File("/home/t-iho/Result/sim/simTrajctory"+date+".csv");
		
		File input = new File("D:/OpenPFLOW/OpenPFLOWslot.csv");
		File out = new File("D:/OpenPFLOW/OpenPFLOWmovie.csv");
		
//		File drm = new File("/home/t-iho/Data/network/drmbaseroad.csv");
//		File rail = new File("/home/t-iho/Data/network/railnetwork.csv");
		
		File drm = new File("D:/drmbaseroad.csv");
		File rail = new File("D:/railnetwork.csv");
		
		HashMap<String,ArrayList<String>>mesh_node = NodeAssignment.MeshToNode(drm);
		HashMap<String,ArrayList<String>>train_mesh_node = NodeAssignment.MeshToNode(rail);
		
		
		BufferedReader br = new BufferedReader(new FileReader(input));
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		
		String line = null;
		String previd = null;
		String prevmode = null;
		Node prevtarget = new Node("1");
		
		String currentDay = date.substring(0, 4)+"-"+date.substring(4,6)+"-"+date.substring(6,8);
		
		long currentDate = SDF_TS2.parse(currentDay+" 06:00:00").getTime();
		
		Network network = (new NetworkLoader()).load(drm);
		Network railnet = (new NetworkLoader()).loadrail(rail);
		
		
		HashMap<Link,HashMap<Integer,Integer>>link_hour_count = new HashMap<Link,HashMap<Integer,Integer>>();
		
		int count=0;
		
		
		while((line=br.readLine())!=null){
			
			count++;
			if(count%10000==0){
				System.out.println(count);
			}
			String[] tokens = line.split(",");
			
			String id = tokens[0];
			try{
			if(!id.equals(previd)){
				currentDate = SDF_TS2.parse("2008-10-01 06:00:00").getTime()+random.nextInt(1800000);
			}
			Integer slot = Integer.valueOf(tokens[1]);
			String origin = tokens[2];
			String destination = tokens[3];
			String mode = tokens[4];
			
			
				if(!mode.equals("train")&&mesh_node.containsKey(origin)&&mesh_node.containsKey(destination)&&mesh_node.get(destination).size()>=1&&mesh_node.get(origin).size()>=1){
					
					Node source = prevtarget.getNodeID().equals("1")?network.getNode(chooseNode(mesh_node,origin)):prevtarget;
					Node target = mode.equals("stay")?source:network.getNode(chooseNode(mesh_node,destination));
					
					if(("train").equals(prevmode)){
						source = network.getNode(chooseNode(mesh_node,origin));
					}
				
					if((!mode.equals("stay")&&source.equals(target))){
						source = prevtarget.getNodeID().equals("1")?network.getNode(chooseNode(mesh_node,origin)):prevtarget;
						target = mode.equals("stay")?source:network.getNode(chooseNode(mesh_node,destination));
//						j++;
//						System.out.println(j);
					}
					
					if(mode.equals("stay")){
						while(currentDate<SDF_TS2.parse("2008-10-01 06:00:00").getTime()+(slot-11)*1800000){
							currentDate = SDF_TS2.parse("2008-10-01 06:00:00").getTime()+(slot-11)*1800000 + random.nextInt(1800*1000);
						}
						bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+source.getLon()+","+source.getLat()+","+mode);
						bw.newLine();
					}
					if(mode.equals("walk")){Dijkstra routing = new Dijkstra();
					Route route = routing.getRoute(network, source, target);
					
					if(route==null){
						bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+source.getLon()+","+source.getLat()+","+mode);
						bw.newLine();
			
						
						currentDate += source.distance(target)*1000/1.4;
						
						
					}else if(source.distance(target)>2500){
						bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+source.getLon()+","+source.getLat()+","+mode);
						bw.newLine();
						
						currentDate += 1800*1000;
					}else{
						Iterator<Node> iter = route.listNodes().iterator();
						for (Link link : route.listLinks()){
							if(iter.hasNext()){
								Node node = iter.next();
								bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+node.getLon()+","+node.getLat()+","+mode);
								bw.newLine();
							}
							//result
							currentDate += link.getCost()*1000/1.4;
						}
					}
					}
					if(mode.equals("bike")){
						Dijkstra routing = new Dijkstra(new DrmLinkCost(DrmLinkCost.Mode.WALK));
						Route route = routing.getRoute(network, source, target);
						if(route.listNodes().size()==0){
							bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+source.getLon()+","+source.getLat()+","+mode);
							bw.newLine();
							bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+target.getLon()+","+target.getLat()+","+mode);
							bw.newLine();
							for (Link link : route.listLinks()){
								
								//result
								currentDate += link.getCost()*1000/1.4;
							}
						}else{
							Iterator<Node> iter = route.listNodes().iterator();
							for (Link link : route.listLinks()){
								if(iter.hasNext()){
									Node node = iter.next();
									bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+node.getLon()+","+node.getLat()+","+mode);
									bw.newLine();
								}
								//result
								currentDate += link.getCost()*1000/2.8;
							}
						}
					}
					if(mode.equals("vehicle")){
						Dijkstra routing = new Dijkstra(new DrmLinkCost(DrmLinkCost.Mode.VEHICLE));
						Route route = routing.getRoute(network, source, target);
						if(route==null){
							bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+source.getLon()+","+source.getLat()+","+mode);
							bw.newLine();
							bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+target.getLon()+","+target.getLat()+","+mode);
							bw.newLine();
																						//result
							currentDate += source.distance(target)*1000/11.1;
							
						}else{
							Iterator<Node> iter = route.listNodes().iterator();
							
							for (Link link : route.listLinks()){
								Integer hour = Integer.valueOf(SDF_TS.format(new Date(currentDate)));
								if(!link_hour_count.containsKey(link)){
									HashMap<Integer,Integer>hour_count = new HashMap<Integer,Integer>();
									hour_count.put(hour,1);
									link_hour_count.put(link, hour_count);
								}else if(!link_hour_count.get(link).containsKey(hour)){
									HashMap<Integer,Integer>hour_count = new HashMap<Integer,Integer>();
									hour_count.put(hour,1);
									link_hour_count.get(link).put(hour,1);
								}else{
									int temp = link_hour_count.get(link).get(hour)+1;
									link_hour_count.get(link).put(hour,temp);
								}
								if(iter.hasNext()){
									Node node = iter.next();
									bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+node.getLon()+","+node.getLat()+","+mode);
									bw.newLine();
								}
								//result
								currentDate += link.getCost()*1000/11.1;
							}
						}
						
					}
					prevtarget = target;
				}
				if(mode.equals("train")&&train_mesh_node.containsKey(origin)&&train_mesh_node.containsKey(destination)&&train_mesh_node.get(destination).size()>=1&&train_mesh_node.get(origin).size()>=1){
					Node source = railnet.getNode(chooseNode(train_mesh_node,origin));
					Node target = railnet.getNode(chooseNode(train_mesh_node,destination));
					
					//System.out.println("source "+ railnet.hasNode(source.getNodeID())+source.getNodeID());
					//System.out.println("target "+ railnet.hasNode(target.getNodeID())+target.getNodeID());
					
					if(source.equals(target)){
						source = railnet.getNode(chooseNode(train_mesh_node,origin));
						target = railnet.getNode(chooseNode(train_mesh_node,destination));
					}
					
					Dijkstra routing = new Dijkstra(new DrmLinkCost(DrmLinkCost.Mode.VEHICLE));
					Route route = routing.getRoute(railnet, source, target);
					
					System.out.println(route);
					
					if(route==null){
						bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+source.getLon()+","+source.getLat()+","+mode);
						bw.newLine();
						bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+target.getLon()+","+target.getLat()+","+mode);
						bw.newLine();
						currentDate += source.distance(target)*1000/11.4;
					}else{
						Iterator<Node> iter = route.listNodes().iterator();
						for (Link link : route.listLinks()){
							if(iter.hasNext()){
								Node node = iter.next();
								bw.write(id +"," + SDF_TS2.format(new Date(currentDate))+","+node.getLon()+","+node.getLat()+","+mode);
								bw.newLine();
							}
							//result
							//System.out.println("link cost "+link.getCost());
							currentDate += link.getCost()*60*1000;
						}
					}
					
					prevtarget = target;
					prevmode = mode;
				}
				
			}catch(IllegalArgumentException e){
				System.out.println("bound must be positive");
			}catch(StringIndexOutOfBoundsException e){
				System.out.println("bound must be positive");
			}
//			catch(NullPointerException e){
//				System.out.println("NullPointerException");
//			}
			previd = id;
		}
		
		BufferedWriter bw2 = new BufferedWriter(new FileWriter("D:/OpenPFLOW/linkVolume.csv"));
		bw2.write("linkid,06:00,07:00,08:00,09:00,10:00,11:00,12:00,13:00,14:00,15:00,16:00,17:00,18:00,19:00,20:00,21:00,22:00,23:00");
		bw2.newLine();
		for(Link link:link_hour_count.keySet()){
			bw2.write(link.getLinkID()+",");
			for(int i=6;i<24;i++){
				if(link_hour_count.get(link).containsKey(i)){
					bw2.write(link_hour_count.get(link).get(i)+",");		
				}else{
					bw2.write(0+",");
				}
			}
			bw2.newLine();
		}
		
		br.close();
		bw.close();
		bw2.close();
		System.out.println(count);
	}
	
	private static String chooseNode(HashMap<String,ArrayList<String>>mesh_node,String mesh){

			Random random = new Random();
//			System.out.println(mesh+" has "+mesh_node.get(mesh).size()+" nodes");
//			System.out.println(mesh_node.get(mesh));
			int s = random.nextInt(mesh_node.get(mesh).size());
			return mesh_node.get(mesh).get(s);
		
		
	}
	
	
	protected static final SimpleDateFormat SDF_TS = new SimpleDateFormat("HH");
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("yyyyMMdd");
}
