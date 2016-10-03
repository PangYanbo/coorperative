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
import java.util.Date;
import java.util.List;





public class TestTripSegment {

	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd HH:mm:SS")).parse(str);
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
		

		
		String filepath = "D:/training data/KDDI/2_data/1_gps/2_0005.csv";
		try {
			// open file reader
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/TokyoPT/2-0005trip.csv"));
			bw.write("uid,tripid,start time,end time,start lon,start lat,end lon,end lat,trip distance,trip time,average speed");
			bw.newLine();
			// remove header line
			String line = br.readLine();

			List<Agent>agents = new ArrayList<Agent>();
			List<Point>records = null;
			String preid =null;	
			
			TripSegmentation detector = new TripSegmentation(20,4,500d,200d,2);
			
			while( (line=br.readLine())!=null){ 
				String[] tokens  = line.split(",",-1);	// split line with comma "	"
					int did = 9999;
					String pid = tokens[1];
//					int tripid = Integer.valueOf(tokens[1]);
//					int subtripid = Integer.valueOf(tokens[2]);
					Date date = toDate(tokens[2]);
					Double lat = Double.parseDouble(tokens[5]);
					Double lon = Double.parseDouble(tokens[4]);
			
					Point points = new Point(lat,lon,date);
					
			//		System.out.println(uid+","+did+","+lat+","+lon+","+date+","+points.lat+","+points.lon);
					
						if(!pid.equals(preid)){
					    records = new ArrayList<Point>();
						agents.add(new Agent(String.valueOf(pid),String.valueOf(did),records));
						}
						records.add(points);
											
				preid = pid;
				
			}
		//output sample
			
			int[] countTrips = new int[201];
			for(int i=0;i<200;i++){
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
			// statics
			System.out.println("Counts of IDs:"+agents.size());
			for(int i=0;i<60;i++){
				System.out.println(i+":"+countTrips[i]);
			}
			
			List<Point> stays = detector.listStayPoints();
			for(Point stay:stays) {
				System.out.println("\t" + stay);
			}
			
			List<Trip> trips = detector.listTrips();
			for(Trip trip:trips) {
				System.out.println("\t" + trip);
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
