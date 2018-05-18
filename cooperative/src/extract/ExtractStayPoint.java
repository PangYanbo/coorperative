package extract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class ExtractStayPoint {
	
	public static LonLat ComputeMeanCoord(ArrayList<STPoint>points, int i, int j){
		Double lon = 0.0;
		Double lat = 0.0;
		for(int _i=i;_i<=j;_i++){
			lon += points.get(_i).getLon();
			lat += points.get(_i).getLat();
		}
		LonLat mean = new LonLat(lon/(j-i+1),lat/(j-i+1));
		return mean;
	}
	
	public static ArrayList<STPoint>StayPointDetection(Double timeThres,Double distThres,ArrayList<STPoint>points){
	//	System.out.println("points size is "+points.size());
		ArrayList<STPoint>SPs = new ArrayList<STPoint>();
	//	Double distThres = 500.0;
	//	Double timeThres = 30*60.00;
		int i = 0;
		while(i<points.size()){
			int j = i+1;
			while(j<points.size()){
				Double dist = points.get(i).distance(points.get(j));
				if(dist > distThres){
					Date time_i = points.get(i).getTimeStamp();
					Date time_j = points.get(j).getTimeStamp();
					Double deltaT = (double)(time_j.getTime()-time_i.getTime())/1000.0;
					//System.out.println(points.get(i)+","+dist+","+deltaT+".................................");
					if (deltaT > timeThres){
						//System.out.println(points.get(i)+","+dist+","+deltaT+",,,,,,,,,,,,,,,,,,,,,,,,,,,");
						Double lat = ComputeMeanCoord(points,i,j-1).getLat();
						Double lon = ComputeMeanCoord(points,i,j-1).getLon();
						STPoint sp = new STPoint(time_i,points.get(j-1).getTimeStamp(),lon,lat);
						SPs.add(sp);
					}
					i = j-1;
					break;
				}
				j++;
			}
			if(i==points.size()-1){
				//System.out.println("points size "+points.size());
				//System.out.println("SP size :::::::::::::::::::::::::::::::::::::"+SPs.size());
				if(SPs.size()==0){
					Double lat = ComputeMeanCoord(points,0,points.size()-1).getLat();
					Double lon = ComputeMeanCoord(points,0,points.size()-1).getLon();
					STPoint sp = new STPoint(points.get(0).getTimeStamp(),points.get(points.size()-1).getTimeStamp(),lon,lat);
					SPs.add(sp);
				}
				if(SPs.get(SPs.size()-1).distance(points.get(i))>500){
					SPs.add(points.get(i));
//					System.out.println("last point");
//					System.out.println(points.get(i));	
				}
			}	
			i ++;
		}
		Collections.sort(SPs);
		//System.out.println("finished detecting stay point");
		return SPs;
	}
	
}
