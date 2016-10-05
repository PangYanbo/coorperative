package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;





public class StayPoint {

	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * test for TripSegmentation
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] days = {"20150501","20150502","20150503","20150504","20150505","20150506","20150507",
				"20150508","20150509","20150510","20150511","20150512","20150513","20150514",
				"20150515","20150516","20150517","20150518","20150519","20150520","20150521",
				"20150522","20150523","20150524","20150525","20150526","20150527","20150528","20150529","20150530","20150531"};
		
		Map<String,Agent>userlog = new LinkedHashMap<String,Agent>();
		List<Point>records = null;
		TripSegmentation detector = new TripSegmentation(20,4,500d,200d,5);
		
		for(int index =0;index<days.length;index++){
				String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+days[index]+".csv";
				System.out.println(filepath);
			
			
			try {
				// open file reader
				BufferedReader br = new BufferedReader(new FileReader(filepath));
				
				
				// remove header line
				String line = br.readLine();
				
				while( (line=br.readLine())!=null){ 
					
					
					String[] tokens  = line.split("	",-1);	// split line with comma "	"
					
					//System.out.println(tokens[0]+","+tokens[1]+","+tokens[2]+","+tokens[3]+","+tokens[4]);
					
					if(tokens[0].length()*tokens[1].length()*tokens[2].length()*tokens[3].length()*tokens[4].length()!=0){
						String uid = tokens[0];
						if(uid!="null"){
							String did = tokens[1];
							Double lat = Double.parseDouble(tokens[2]);
							Double lon = Double.parseDouble(tokens[3]);
							Date date = toDate(tokens[4]);
							Point points = new Point(lat,lon,date);
								
							if(!userlog.containsKey(uid)){
							    records = new ArrayList<Point>();
								userlog.put(uid, new Agent(uid,did,records));
							}
							if(userlog.get(uid).record.contains(points))
								{continue;}
							userlog.get(uid).record.add(points);
						}
					}
				}
		//output sample
			
		
				
			// close file reader
			br.close();
			// static
			
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + filepath);
		}
		catch(IOException e) {
			System.out.println(e);
		}
		
		}	
		
		int[] stayPoints = new int[201];
		for(int i=0;i<200;i++){
			stayPoints[i]=0;
		}
		
	TreeSet<String>sortedKey = new TreeSet<String>(userlog.keySet());
						
		for(String _uid:sortedKey){
			
			 Comparator<Point> comparator = new Comparator<Point>(){  
		            public int compare(Point p1, Point p2) { 
		                    return p1.compareTo(p2);  
		                }  
			 };
			 Collections.sort(userlog.get(_uid).record,comparator);
			
			 BufferedWriter bw;
			 try {
				 bw = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+"May"+"staypoint.csv"));
				 bw.write("uid,did,stayPoint index,timestamp,lon,lat");
				 bw.newLine();
				 
			            int tripsequence =0;
						detector.segment(userlog.get(_uid).record);
						userlog.get(_uid).staypoint= detector.listStayPoints();
//						userlog.get(_uid).trips=detector.listTrips();
						if(userlog.get(_uid).staypoint.size()<200){
							stayPoints[userlog.get(_uid).staypoint.size()]++;
						}
						for(Point point:userlog.get(_uid).staypoint){
								tripsequence++;
								String tripline= userlog.get(_uid).Uid+","
												+userlog.get(_uid).Did+","
												+tripsequence+","
												+point.getTimeStamp()+","
												+point.getLat()+","
												+point.getLon();					
								bw.write(tripline);
								bw.newLine();
							}
							

						BufferedWriter bw2 = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+"May"+"stastic.csv"));
						
						bw2.write("stayPoints numbers,user counts");
						bw2.newLine();
						
						for(int i=0;i<60;i++){
							bw2.write(i+","+stayPoints[i]);
							bw2.newLine();
						}
						bw2.close();	
						bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
					
					
						
			}
		
	}

}
