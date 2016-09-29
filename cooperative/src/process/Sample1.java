package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Sample1 {
//	private static Date toDate(String str){
//		
//			ParsePosition pos= new ParsePosition(0);
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			Date datetime=formatter.parse(str,pos);
//		Timestamp ts =null;
//		if(datetime!=null){
//		ts=new Timestamp(datetime.getTime());
//		}
//		return ts;
//	}
	
	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy/MM/dd HH:mm")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static double rad(double d){
		return d*Math.PI/180.0;
	}
	
	public static double distance(Point p1,Point p2) {
		double lat1= rad(p1.lat);
		double lat2= rad(p2.lat);
		double lon1= rad(p1.lon);
		double lon2= rad(p2.lon);
		return 6371*1000*Math.acos(Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)+Math.sin(lat1)*Math.sin(lat2));	
    }
	
	
	public static void extractfile(String date){
		ProcessBuilder pb = new ProcessBuilder("tar",
				"zxvf","/tmp/bousai_data/gps_"+date+".tar.gz", 
                "-C","/home/t-iho/");
		pb.inheritIO();
		try{
			Process process = pb.start();
			process.waitFor();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		File out = new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date);
		out.renameTo(new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date+".csv"));
		File out2 =   new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date);
	    out2.renameTo(new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date+".csv"));
	}
	
	
	public static void checkstaypoints(List<Point>_record){
		for(int i=_record.size()-1;i>=1;i--){
			if(((_record.get(i).time.getTime()-_record.get(i-1).time.getTime()))/1000.0>2&&distance(_record.get(i),_record.get(i-1))<50){
				_record.get(i).isStay=true;
			}else{
				_record.get(i).isStay=false;
			}
		}
		_record.get(0).isStay=true;
	}
	
	public static List<Trip> segment(List<Point>_record){
//		Point origin=null;
//		Point destination=null;
		Trip segment = null;
		List<Point>tripsegmentation=new ArrayList<Point>();
		List<Trip>trips=new ArrayList<Trip>();
		for(int i=_record.size()-1;i>=1;i--){
			if(!_record.get(i).isStay){
				tripsegmentation.add(_record.get(i));
				if(_record.get(i-1).isStay){
					segment = new Trip(tripsegmentation.get(0),_record.get(i-1),tripsegmentation);
					trips.add(segment);
					tripsegmentation.clear();
				}
				}
//			else if(!_record.get(i-1).isStay){
//				origin = tripsegmentation.get(tripsegmentation.size()-1);
//				destination = tripsegmentation.get(0);
//				segment = new Trip(origin,destination,tripsegmentation);
//				trips.add(segment);
//				tripsegmentation.clear();}
			else{
				continue;
			}
		}
		return trips;
	}
	


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Enter the date");
		//Scanner in = new Scanner(System.in);
		//String day = in.nextLine();
		//extractfile(day);
		
		//String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+".csv";
		
		
		String filepath = "C:/Users/Šâ”Ž/Desktop/Programing  training/lesson02/02_java_sample_20150423/sample-data.csv";
		try {
			// open file reader
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Šâ”Ž/Desktop/Programing  training/lesson02/02_java_sample_20150423/sampleID.csv"));
			// remove header line
			String line = br.readLine();

			List<Agent>agents = new ArrayList<Agent>();
			List<Point>records = null;
			String preid =null;	
			
			TripSegmentation detector = new TripSegmentation(2,1,500d,1000d,2);
			
			while( (line=br.readLine())!=null){ 
				String[] tokens  = line.split(",",-1);	// split line with comma "	"
//				for(int i=0;i<tokens.length;i++){
//					System.out.println(tokens[i]);
//				}
				if(!tokens[0].equals("null")){
					String uid = tokens[0];
					String did = tokens[1];
					Double lat = Double.parseDouble(tokens[3]);
					Double lon = Double.parseDouble(tokens[4]);
					Date date = toDate(tokens[2]);
					Point points = new Point(lat,lon,date);
					
					System.out.println(uid+","+did+","+lat+","+lon+","+date+","+points.lat+","+points.lon);
				
					
					if(uid!=null&&did!=null&&lat!=null&&lon!=null&&date!=null){
				
						if(uid.equals(preid)==false){
					    records = new ArrayList<Point>();
						agents.add(new Agent(uid,did,records));
						}
						records.add(points);
					}						
				preid = uid;
				}
			}
	
			int[] countTrips = new int[20];
			for(int i=0;i<20;i++){
				countTrips[i]=0;
			}
			
			//output sample
			for(Agent users:agents){
				int tripsequence = 0;
				detector.segment(users.record);
				users.trips=detector.listTrips();
				countTrips[users.trips.size()]++;
				for(Trip trip:users.trips){
						tripsequence++;
						String tripline= users.Uid+","
										+tripsequence+","
										+trip.getOrigin().getTimeStamp()+","
										+trip.getDestination().getTimeStamp()+","
										+trip.getOrigin().lat+","
										+trip.getOrigin().lon+","
										+trip.getDestination().lat+","
										+trip.getDestination().lon+","
										+trip.getDistance()+"m";
						bw.write(tripline);
						bw.newLine();
					}
									
//					for(Point point:users.record){
//						String idline = users.Uid+","+users.Did+","+point.time+","+point.lat+","+point.lon;
//						bw.write(idline);
//						bw.newLine();
//					//	System.out.println(id+","+time+","+log.get(time));
//					}	
				}
		//	}
		
			
			// close file reader
			br.close();
			bw.close();
			// statics
			System.out.println("Counts of IDs:"+agents.size());
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + filepath);
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
}