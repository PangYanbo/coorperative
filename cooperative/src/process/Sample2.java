package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


public class Sample2 {

	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
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
	
	/**
	 * test for TripSegmentation
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println("Enter the date");
		Scanner in = new Scanner(System.in);
		String day = in.nextLine();
//		extractfile(day);
		
		String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+".csv";;
		try {
			// open file reader
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+"trip.csv"));
			// remove header line
			String line = br.readLine();

			List<Agent>agents = new ArrayList<Agent>();
			List<Point>records = null;
			String preid =null;	
			
			TripSegmentation detector = new TripSegmentation(120,30,100d,50d,1);
			
			while( (line=br.readLine())!=null){ 
				String[] tokens  = line.split("	",-1);	// split line with comma "	"
//				for(int i=0;i<tokens.length;i++){
//					System.out.println(tokens[i]);
//				}

					String uid = tokens[0];
					String did = tokens[1];
					Double lat = Double.parseDouble(tokens[2]);
					Double lon = Double.parseDouble(tokens[3]);
					Date date = toDate(tokens[4]);
					Point points = new Point(lat,lon,date);
					
			//		System.out.println(uid+","+did+","+lat+","+lon+","+date+","+points.lat+","+points.lon);
				
					
					if(uid!=null&&did!=null&&lat!=null&&lon!=null&&date!=null){
				
						if(!uid.equals(preid)){
					    records = new ArrayList<Point>();
						agents.add(new Agent(uid,did,records));
						}
						records.add(points);
					}						
				preid = uid;
				
			}
		//output sample
			
			int[] countTrips = new int[20];
			for(int i=0;i<20;i++){
				countTrips[i]=0;
			}
			
			for(Agent users:agents){
				int tripsequence = 0;
				if(users.record.size()>10){
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
				}				
				}
			
			// close file reader
			br.close();
			bw.close();
			// statics
			System.out.println("Counts of IDs:"+agents.size());
			for(int i=0;i<20;i++){
				System.out.println(i+":"+countTrips[i]);
			}
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + filepath);
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

}
