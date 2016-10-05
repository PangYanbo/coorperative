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





public class YahooTripSegmentation {

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
		System.out.println("Enter the date");
		Scanner in = new Scanner(System.in);
		String day = in.nextLine();

		Map<String,Agent>userlog = new LinkedHashMap<String,Agent>();
		
		String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+".csv";
		try {
			// open file reader
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+"trip.csv"));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+"tripstastic.csv"));
			bw.write("uid,tripid,start time,end time,start lon,start lat,end lon,end lat,trip distance,trip time,average speed");
			bw.newLine();
			bw2.write("trip numbers,counts");
			bw2.newLine();
			// remove header line
			String line = br.readLine();

			List<Point>records = null;
			
			TripSegmentation detector = new TripSegmentation(20,4,500d,200d,2);
			
			while( (line=br.readLine())!=null){ 
				
				String[] tokens  = line.split("	",-1);	// split line with comma "	"
				if(tokens[0].length()!=0&&tokens[0]!=null&&tokens[0]!="null"){
					String uid = tokens[0];
					if(!uid.equals("null")){
					String did = tokens[1];
					Double lat = Double.parseDouble(tokens[2]);
					Double lon = Double.parseDouble(tokens[3]);
					Date date = toDate(tokens[4]);
					Point points = new Point(lat,lon,date);
						
				//		System.out.println(uid+","+did+","+lat+","+lon+","+date+","+points.lat+","+points.lon);
						
					if(!userlog.containsKey(did)){
					    records = new ArrayList<Point>();
						userlog.put(did, new Agent(uid,did,records));
					}
					if(userlog.get(did).record.contains(points))
					{continue;}
					userlog.get(did).record.add(points);
					}
				}
			}
		//output sample
			
			int[] countTrips = new int[201];
			for(int i=0;i<200;i++){
				countTrips[i]=0;
			}
			
		TreeSet<String>sortedKey = new TreeSet<String>(userlog.keySet());
				
			
			for(String _did:sortedKey){
				
				 Comparator<Point> comparator = new Comparator<Point>(){  
			            public int compare(Point p1, Point p2) { 
			                    return p1.compareTo(p2);  
			                }  
				 };
				 Collections.sort(userlog.get(_did).record,comparator);
				 
					int tripsequence =0;
						if(userlog.get(_did).record.size()>=10&&userlog.get(_did).Uid!=null&&userlog.get(_did).Uid!="null"){
							detector.segment(userlog.get(_did).record);
							userlog.get(_did).trips=detector.listTrips();
							countTrips[userlog.get(_did).trips.size()]++;
							for(Trip trip:userlog.get(_did).trips){
									tripsequence++;
									String tripline= userlog.get(_did).Uid+","
													+tripsequence+","
													+trip.getOrigin().getTimeStamp()+","
													+trip.getDestination().getTimeStamp()+","
													+trip.getOrigin().lat+","
													+trip.getOrigin().lon+","
													+trip.getDestination().lat+","
													+trip.getDestination().lon+","
													+String.format("%.2f", trip.getDistance())+"m"+","
													+String.format("%.2f", trip.getTriptime())+"min"+","
													+String.format("%.2f", trip.getSpeed())+"m/s";
										
									bw.write(tripline);
									bw.newLine();
								}
						}
					
									
				}
				
			// close file reader
			br.close();
			bw.close();
			// static
			for(int i=0;i<60;i++){
				bw2.write(i+","+countTrips[i]);
				bw2.newLine();
			}
			bw2.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + filepath);
		}
		catch(IOException e) {
			System.out.println(e);
		}
		
		
	}

}
