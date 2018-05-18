package process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.STPoint;
import DataModify.Over8TimeSlots;
import StayPointDetection.StayPointGetter2;
import Motif.MotifNumber;
import Motif.SPFinder;

public class SPforIRL {

	/**
	 * @param 
	 * args[0] : infile 
	 * args[1] : yyyymmdd of disaster
	 * args[2] : name of disaster
	 *
	 */
	public static void main(String args[]) throws IOException, ParseException{

		/*
		 * test file
		 */
//		String in = "c:/users/yabetaka/Desktop/dataforExp.csv";

//		String in = args[0];
//		executeMotif(in, args[1],args[2]);

//		System.out.println("Type in the date");
		Scanner in = new Scanner(System.in);
//		String day = in.nextLine();
//		
		String filepath = "/home/t-iho/Data/grid/0/tmp/ktsubouc/gps_20150511.csv";
		
		System.out.println("Type in the radius");
		String r = in.nextLine();
		System.out.println("Type in the threshold");
		String threshold = in.nextLine();
		
		executeMotif(filepath,"/home/t-iho/",Double.parseDouble(r),Double.parseDouble(threshold));
	}

//	public static void main(String args[]) throws IOException, ParseException{
//		
//		String filepath = "D:/training data/pflow.csv";
//		
//		executeMotifPT(filepath,"D:/");
//	}	
	
	
	
	public static void executeMotif(String in, String path, double r, double threshold) throws IOException, ParseException{
		
		HashMap<String,ArrayList<STPoint>> id_SPs = StayPointGetter2.getSPs2(new File(in), r, threshold);

		HashMap<String, HashMap<String, ArrayList<STPoint>>> map = SPFinder.intomapY2(in,"weekday"); 
		HashMap<String, ArrayList<String>> id_days = Over8TimeSlots.OKAY_id_days(in);
		HashMap<String, ArrayList<STPoint>> trajectory = generate_trajectory(map, id_SPs, id_days); //[id|day|motifnumber]

		writeout(trajectory,path+"id_day_motifs_"+r+"_"+threshold+".csv");
			//	motifPercentage(id_day_motif, path+"motif%s.csv");
	}

//public static void executeMotifPT(String in, String path) throws IOException, ParseException{
//	
//	HashMap<String,HashMap<LonLat,Integer>> id_SPs = StayPointGetter.getSPsPT(new File(in));
//
//	HashMap<String,ArrayList<LonLat>> map = SPFinder.intomapPT(in); 
//    HashMap<String, HashMap<String, Integer>> id_day_motif = getID_day_motifPT(map, id_SPs); //[id|day|motifnumber]
//
//	writeout(id_day_motif,path+"id_day_motifs_"+500+"_"+300+".csv");
//			motifPercentage(id_day_motif, path+"motif%s.csv");
//}

public static HashMap<String, HashMap<String,Integer>> getID_day_motifPT
(HashMap<String, ArrayList<LonLat>> map, HashMap<String,HashMap<LonLat,Integer>> id_SPs){
	HashMap<String, HashMap<String,Integer>> res = new HashMap<String, HashMap<String,Integer>>();
	int count = 0;
	for(String id : map.keySet()){
		System.out.println(id+"!!!!!!!!!!!!!!!!!!");
		ArrayList<Integer> locchain = new ArrayList<Integer>();
		HashMap<String,Integer> temp = new HashMap<String,Integer>();
			if(id_SPs.get(id)!=null){
				for(LonLat loc:map.get(id)){
					for(LonLat sp:id_SPs.get(id).keySet()){
						if(loc.distance(sp)<10){
//							System.out.println(id_SPs.get(id).get(sp));
							locchain.add(id_SPs.get(id).get(sp));
							break;
						}
					}
				}
					count++;
					if(count%10000==0){
						System.out.println("#done " + count + " IDs");
					}
					System.out.println(locchain.size());
					Integer motif = MotifNumber.motifs(locchain);
					temp.put("ss", motif);
					res.put(id, temp);
			}
		
	}
	return res;
}


//	public static HashMap<String, HashMap<String,Integer>> getID_day_motif2
//	(HashMap<String, HashMap<String, ArrayList<LonLat>>> map, HashMap<String,ArrayList<STPoint>> id_SPs, HashMap<String, ArrayList<String>> id_days){
//		HashMap<String, HashMap<String,Integer>> res = new HashMap<String, HashMap<String,Integer>>();
//		int count = 0;
//		for(String id : map.keySet()){
//			HashMap<String,Integer> temp = new HashMap<String,Integer>();
//			for(String day : map.get(id).keySet()){
//				if(id_SPs.get(id)!=null){
//					if(id_days.containsKey(id)&&(id_days.get(id).contains(day))){
//						ArrayList<Integer> temp_locchain = getLocChain2(map.get(id).get(day), id_SPs.get(id));
//						ArrayList<Integer> locchain = continueChecker(temp_locchain);
//						count++;
//						if(count%10000==0){
//							System.out.println("#done " + count + " ID*days");
//						}
//						Integer motif = MotifNumber.motifs(locchain);
//						temp.put(day, motif);
//						res.put(id, temp);
//					}
//				}
//			}
//		}
//		return res;
//	}

	public static HashMap<String, ArrayList<STPoint>> generate_trajectory
	(HashMap<String, HashMap<String, ArrayList<STPoint>>> map, HashMap<String,ArrayList<STPoint>> id_SPs, HashMap<String, ArrayList<String>> id_days){
		HashMap<String, ArrayList<STPoint>> res = new HashMap<String, ArrayList<STPoint>>();
		int count = 0;
		for(String id : map.keySet()){
			HashMap<String,Integer> temp = new HashMap<String,Integer>();
			for(String day : map.get(id).keySet()){
				if(id_SPs.get(id)!=null){
					if(id_days.containsKey(id)&&(id_days.get(id).contains(day))){
						ArrayList<STPoint> temp_locchain = getLocChain2(map.get(id).get(day), id_SPs.get(id));
						ArrayList<STPoint> locchain = continueChecker(temp_locchain);
						count++;
						if(count%10000==0){
							System.out.println("#done " + count + " ID*days");
						}
						res.put(id, locchain);
					}
				}
			}
		}
		return res;
	}
	
	
//	public static ArrayList<Integer> getLocChain(ArrayList<LonLat> list){
//		HashMap<Integer,LonLat> temp = new HashMap<Integer,LonLat>();
//		ArrayList<Integer> res = new ArrayList<Integer>();
//		res.add(1);
//		temp.put(1,list.get(0));
//		int count = 2;
//		for(int i = 1; i<list.size(); i++){
//			if(overlapchecker(temp,list.get(i))==0){ //new point
//				res.add(count);
//				temp.put(count, list.get(i));
//				count++;
//			}
//			else{
//				res.add(overlapchecker(temp,list.get(i)));
//			}
//		}
//		if(res.get(res.size()-1)!=1){
//			res.add(1);
//		}
//		return res;
//	}

	public static ArrayList<STPoint> getLocChain2(ArrayList<STPoint> list, ArrayList<STPoint> id_SPs){
		HashMap<Integer,STPoint> temp = new HashMap<Integer,STPoint>();
		ArrayList<STPoint> res = new ArrayList<STPoint>();
		//		System.out.println("id_SP: " + id_SPs);
		int count = 1;
		for(int i = 0; i<list.size(); i++){
			for(STPoint sp : id_SPs){
				if(list.get(i).distance(sp)<500){
					if(overlapchecker(temp,sp).getTimeStamp()==null){ //new point
						res.add(sp);
						temp.put(count,sp);
						count++;
					}
					else{
						res.add(overlapchecker(temp,sp));
					}
					break;
				}
			}
		}
		res.add(list.get(0));
		return res;
	}

	public static STPoint overlapchecker(HashMap<Integer,STPoint> map, STPoint point){
		if(map.size()>0){
			for(Integer i : map.keySet()){
				if(map.get(i).distance(point)<1000){
					return map.get(i);
				}
			}
		}
		return new STPoint();
	}

	public static ArrayList<STPoint> continueChecker(ArrayList<STPoint> locchain){
		ArrayList<STPoint> res = new ArrayList<STPoint>();
		STPoint prev = new STPoint();
		for(STPoint i : locchain){
			if(!i.equals(prev)){
				res.add(i);
			}
			prev = i;
		}
		return res;
	}

	public static void motifPercentage(HashMap<String, HashMap<String,Integer>> map, String out) throws IOException{
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

	public static File writeout(HashMap<String, ArrayList<STPoint>> map, String path) throws IOException{
		File out = new File(path);
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(String id : map.keySet()){
			for(int i=0; i<map.get(id).size();i++){
//				int motifnum = map.get(id).get(day);
//				String z = "1";
//			
				bw.write(id + "," + map.get(id).get(i).getDtStart() + "," + map.get(id).get(i).getDtEnd() + "," + map.get(id).get(i).getLon()+','+map.get(id).get(i).getLat() );
				bw.newLine();
			}
		}
		bw.close();
		return out;
	}

}
