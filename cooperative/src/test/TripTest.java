package test;

import java.util.ArrayList;

import jp.ac.ut.csis.pflow.geom.STPoint;
import jp.ac.ut.csis.pflow.geom.Trip;

public class TripTest {
	
	public static void main(String[] args){
		ArrayList<STPoint>trajectory = new ArrayList<STPoint>();
		
		STPoint p = new STPoint();
		trajectory.add(p);
		System.out.print(trajectory);
		Trip trip = new Trip(trajectory);
		System.out.print(trip.getTrajectory());
		boolean inrail = Boolean.parseBoolean("true");
		System.out.println(inrail);
	}
	
}
