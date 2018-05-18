package SigSpatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class Extent {
	
	public static ArrayList<Double>getExtent(HashMap<String, ArrayList<STPoint>>map){
		ArrayList<Double>extent =new ArrayList<Double>();
		ArrayList<Double>Lat = new ArrayList<Double>();
		ArrayList<Double>Lon = new ArrayList<Double>();
		for(String id : map.keySet()){
			for(STPoint point : map.get(id)){
				Lat.add(point.getLat());
				Lon.add(point.getLon());
			}
		}
		double minLat = Collections.min(Lat);extent.add(minLat);
		double minLon = Collections.min(Lon);extent.add(minLon);
		double maxLat = Collections.max(Lat);extent.add(maxLat);
		double maxLon = Collections.max(Lon);extent.add(maxLon);
		return extent;
	}
	
	public static ArrayList<String>MeshList(ArrayList<Double>extent){//get meshList in level 3 in a rectangle
		ArrayList<String>MeshList = new ArrayList<String>();
		double minLat = extent.get(0);
		double minLon = extent.get(1);
		double maxLat = extent.get(2)+0.00833;
		double maxLon = extent.get(3)+0.0125; 
		double temp_lat = minLat;
		double temp_lon = minLon;
		while(temp_lat <= maxLat ){
			while(temp_lon <= maxLon){
				Mesh mesh = new Mesh(3,temp_lon,temp_lat);
				MeshList.add(mesh.getCode());
				temp_lon += 0.0125;
			}
			temp_lon = minLon;
			temp_lat += 0.008333;
		}
		return MeshList;
	}
}
