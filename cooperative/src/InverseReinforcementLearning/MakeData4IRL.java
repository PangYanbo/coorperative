package InverseReinforcementLearning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import IDExtract.ID_Extract_Tools;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class MakeData4IRL {

	/*
	 * extract gps logs from long term records
	 * sort logs into format [id|day|points]
	 * write out ids who have more than 7 days records in a month
	 * 
	 * */
	
	
	public static void main(String[] args) throws IOException, ParseException{  
		String idListPath = "/home/t-iho/Data/id.csv";
		ArrayList<String>idList = getIdList(idListPath);
		String path ="/home/t-iho/grid/0/tmp/hadoop-ktsubouc/";
		File f = new File(path);  
        File[] files = f.listFiles(); 
        System.out.println("type in daycodes, i.e.(weekday, weekend, all)");
        Scanner in = new Scanner(System.in);
        String mode = in.nextLine();
        ArrayList<String> daycodes = new ArrayList<String>();
        if(mode.equals("weekday")){
			daycodes.add("1");daycodes.add("2");daycodes.add("3");daycodes.add("4");daycodes.add("5");
		}
		else if(mode.equals("weekend")){
			daycodes.add("6");daycodes.add("7");
		}
		else if(mode.equals("all")){
			daycodes.add("1");daycodes.add("2");daycodes.add("3");daycodes.add("4");daycodes.add("5");daycodes.add("6");daycodes.add("7");
		}
		else{
			System.out.println("weekend or weekday or all??");
		}
        
        HashMap<String,HashMap<String,ArrayList<STPoint>>>id_day_point = new HashMap<String, HashMap<String,ArrayList<STPoint>>>(5000000);
        
        for(File file:files){
        	String youbi = (new SimpleDateFormat("u")).format(SDF_TS4.parse(file.getName().substring(5, 13)));
        	if(daycodes.contains(youbi)){
        		readfiles(file,idList,id_day_point);      	
        		System.out.println("Start reading "+file.getName());
        	} 	
        }
        writeout(id_day_point);
	}
	
	
	public static ArrayList<String>getIdList(String path) throws IOException{
		ArrayList<String>idList = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line =null;
		while((line=br.readLine())!=null){
			String tokens[] = line.split("\t");
			Double lat = Double.parseDouble(tokens[1]);
			Double lon = Double.parseDouble(tokens[2]);
			if(lat>=35.33&&lat<=36&&lon>=139&&lon<=140){
				if(!idList.contains(tokens[0]))
					idList.add(tokens[0]);
			}
		}
		System.out.println("Extrat "+idList.size()+" ids");
		return idList;
	}
	
	public static void readfiles(File in, ArrayList<String>id_list, HashMap<String,HashMap<String,ArrayList<STPoint>>>id_day_point) throws IOException, ParseException{
		String day = in.getName().substring(12, 14);
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		String prevline = null;
		while((line=br.readLine())!=null){
			if(ID_Extract_Tools.SameLogCheck(line,prevline)==true){
				String[] tokens = line.split("\t");
				if(tokens.length>=5){
					if(!tokens[4].equals("null")){
						String id = tokens[0];
						if(!id.equals("null")){
							if(id_list.contains(id)){
								Double lat = Double.parseDouble(tokens[2]);
								Double lon = Double.parseDouble(tokens[3]);
								Date dt = SDF_TS2.parse(tokens[4]);
//								String day = SDF_TS3.format(dt);
								String time = SDF_TS.format(dt);
								Integer t = converttoSecs(time);
								STPoint p = new STPoint(dt,lon,lat);
								//mesh no 5339
								if(lat>=35.33&&lat<=36&&lon>=139&&lon<=140){
									if(id_day_point.containsKey(id)){
										if(id_day_point.get(id).containsKey(day)){
											id_day_point.get(id).get(day).add(p);
									}
										else{
											ArrayList<STPoint> list = new ArrayList<STPoint>();
											list.add(p);
											id_day_point.get(id).put(day, list);
										}
									}
									else{
										ArrayList<STPoint> list = new ArrayList<STPoint>();
										list.add(p);
										HashMap<String, ArrayList<STPoint>> tempmap = new HashMap<String, ArrayList<STPoint>>();
										tempmap.put(day, list);
										id_day_point.put(id, tempmap);
									}
									}
								}
							}
						}
					}
				}
				prevline = line;
			}
		
		br.close();
		System.out.println("finish sorting map "+in.getName()+" into id_day_point with "+id_day_point.size()+" ids");
	 }
	
	 public static void writeout(HashMap<String,HashMap<String,ArrayList<STPoint>>>id_day_point) throws IOException{
      	BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/Data/forIRL/id_day_point_mesh5339.csv"));
      	for(String _id:id_day_point.keySet()){
      		
      			for(String _day:id_day_point.get(_id).keySet()){
          			Collections.sort(id_day_point.get(_id).get(_day));
    				for(int i =0; i < id_day_point.get(_id).get(_day).size(); i++)
    				{
    					bw.write(_id+","+SDF_TS2.format(id_day_point.get(_id).get(_day).get(i).getTimeStamp())+","+id_day_point.get(_id).get(_day).get(i).getLon()+","+id_day_point.get(_id).get(_day).get(i).getLat());
    					bw.newLine();
    				}
          		}
      		
      		System.out.println("finishi writing "+_id+" logs with number of "+id_day_point.get(_id).size());
      	}
      	System.out.println("All id's logs are stored in files with number of "+id_day_point.size());
      	bw.close();
     }
	
	 public static boolean checkIDisAvailable(HashMap<String,ArrayList<STPoint>>day_points){
		 int count = 0;
		 for(String _day:day_points.keySet()){
			 if(day_points.get(_day).size()>10){
				 count++;
			 }
		 }
		 if(count>=3){
			 return true;
		 }else{
			 return false;
		 }
	 }
	 
	 
//	 public static boolean numberofSlots(ArrayList<STPoint>points){
//		 HashSet<Integer>set = new HashSet<Integer>();
//		 Integer count = 0;
//		 for(int i = 0; i < points.size(); i++){
//			 count = convertoSecs(points.get(i).getTimeStamp());
//			 
//		 }
//		 return true;
//	 }
	 
	 private static HashMap<String,ArrayList<STPoint>> sortMapByValues(HashMap<String, ArrayList<STPoint>> aMap) {

	        Set<Entry<String,ArrayList<STPoint>>> mapEntries = aMap.entrySet();

	        System.out.println("Values and Keys before sorting ");
	        for(Entry<String,ArrayList<STPoint>> entry : mapEntries) {
	            System.out.println(entry.getValue() + " - "+ entry.getKey());
	        }

	        // used linked list to sort, because insertion of elements in linked list is faster than an array list. 
	        List<Entry<String,ArrayList<STPoint>>> aList = new LinkedList<Entry<String,ArrayList<STPoint>>>(mapEntries);

	        // sorting the List
	        Collections.sort(aList, new Comparator<Entry<String,ArrayList<STPoint>>>() {

	            @Override
	            public int compare(Entry<String, ArrayList<STPoint>> ele1,
	                    Entry<String, ArrayList<STPoint>> ele2) {
	            
	                return ele1.getKey().compareTo(ele2.getKey());
	            }
	        });

	        // Storing the list into Linked HashMap to preserve the order of insertion. 
	        HashMap<String,ArrayList<STPoint>> aMap2 = new LinkedHashMap<String, ArrayList<STPoint>>();
	        for(Entry<String,ArrayList<STPoint>> entry: aList) {
	            aMap2.put(entry.getKey(), entry.getValue());
	        }

	        return aMap2;

	    }
	 
	 public static int converttoSecs(String time){
			String[] tokens = time.split(":");
			int hour = Integer.parseInt(tokens[0]);
			int min  = Integer.parseInt(tokens[1]);
			int sec  = Integer.parseInt(tokens[2]);

			int totalsec = hour*3600+min*60+sec;		
			return totalsec;
		} 
	 
	 
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
	protected static final SimpleDateFormat SDF_TS4 = new SimpleDateFormat("yyyyMMdd");
}
