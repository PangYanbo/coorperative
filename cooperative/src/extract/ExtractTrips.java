package extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.lang.ObjectUtils;

import jp.ac.ut.csis.pflow.geom.STPoint;
import jp.ac.ut.csis.pflow.geom.Trip;
import readfiles.ReadBufferPoint;

public class ExtractTrips {
	
	public static void main(String[] args) throws Exception{
		HashMap<String,ArrayList<STPoint>>id_points = new HashMap<String,ArrayList<STPoint>>();
		HashMap<String,ArrayList<Trip>>id_trips = new HashMap<String,ArrayList<Trip>>();
		ArrayList<STPoint>sps = new ArrayList<STPoint>();
		
		Scanner in = new Scanner(System.in);
		System.out.println("Type in the GPS file date");
		String date = in.nextLine();
		
		System.out.println("Type in the time threhold in secs");
		Double timeThres = Double.valueOf(in.nextLine());
		System.out.println("Type in the distance threhold in meters");
		Double distThres = Double.valueOf(in.nextLine());
		
		File gpsfile = new File("/home/t-iho/Result/rawdata/TokyoLog"+date+".csv");
		
		ReadBufferPoint.ReadBufferPoint(id_points, gpsfile);
		
		for(String id:id_points.keySet()){
			sps = ExtractStayPoint.StayPointDetection(timeThres,distThres,id_points.get(id));
			ArrayList<Trip>trips = new ArrayList<Trip>();
			trips = SplitTrips(id_points.get(id),sps);
			id_trips.put(id,trips);
		}
		
		String path = "/home/t-iho/Result/trainingdata/Trip"+date+".csv";
		File out = new File(path);
		
		writeout(out,id_trips);
		in.close();
	}
	
	public static ArrayList<Trip>SplitPFLOWTrips(ArrayList<STPoint>trac){
		System.out.println(trac);
		ArrayList<Trip>trips = new ArrayList<Trip>();
		Trip trip = null;
		STPoint prev_point = null;
		for(STPoint p:trac){
			if(prev_point==null||!p.getMode().equals(prev_point.getMode())){
				if(prev_point!=null){System.out.println(p.getMode()+","+prev_point.getMode());};
				trip=new Trip(new ArrayList<STPoint>());
				trip.setMode(p.getMode());
				trip.getTrajectory().add(p);
				trips.add(trip);
			}else{
				trip.getTrajectory().add(p);
			}
			prev_point = p;
		}
		return trips;
	}
	
	public static ArrayList<Trip>SplitTrips(ArrayList<STPoint>trac,ArrayList<STPoint>staypoints){
		ArrayList<Trip>trips = new ArrayList<Trip>();
		Trip trip = null;
		STPoint prev_stay = null;
		for(STPoint p:trac){
			STPoint curr_stay = null;
			for(STPoint stay:staypoints){
				if(stay.intersects(p.getTimeStamp())){ 
					curr_stay = stay; 
					break; 
				}
			}
			// move
			if( curr_stay == null ) {
				if( trip == null ) {
					trips.add( trip=new Trip(new ArrayList<STPoint>())); 	// new trip
					if( prev_stay != null ) {
						trip.getTrajectory().add( new STPoint(prev_stay.getDtEnd(),prev_stay.getLon(),prev_stay.getLat()) ); 
					}
				}
				trip.getTrajectory().add(p);
			}
			// stay
			else {
				if( prev_stay != null && !ObjectUtils.equals(prev_stay,curr_stay) ) {
					// case trip points exist between stays
					if( trip != null ) {
						trip.getTrajectory().add(new STPoint(curr_stay.getDtStart(),curr_stay.getLon(),curr_stay.getLat()));
					}
					// case no trip points exist between stays, [caution] there may be OD time inconsistency
					else if ( prev_stay.getDtEnd().before(curr_stay.getDtStart()) ) {
						trip = new Trip(new ArrayList<STPoint>());
						trip.getTrajectory().add(new STPoint(prev_stay.getDtEnd(),prev_stay.getLon(),prev_stay.getLat()));
						trip.getTrajectory().add(new STPoint(curr_stay.getDtStart(),curr_stay.getLon(),curr_stay.getLat()));
						trips.add(trip);
					}
				}
				prev_stay = curr_stay;
				trip = null;
			}
		}
		return trips;
	}
	
	public static void writeout(File out,HashMap<String,ArrayList<Trip>>id_trips) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		int count = 0;
		for(String id:id_trips.keySet()){
			for(Trip trip:id_trips.get(id)){
				count++;
				bw.write(id+","+count+","+SDF_TS2.format(trip.getStartTime())+","+trip.getStartPoint().getLon()+","+trip.getStartPoint().getLat()+","+SDF_TS2.format(trip.getEndTime())+","+trip.getEndPoint().getLon()+","+trip.getEndPoint().getLat()+","+trip.getMode());
				bw.newLine();
			}
		}
		bw.close();
	}
	
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
}
