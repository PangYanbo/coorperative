package process;
import jp.ac.ut.csis.pflow.geom.*;
import jp.ac.ut.csis.pflow.geom.LonLat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.*;

public class StatetoCoor {
	
	protected static final SimpleDateFormat SDF  = new SimpleDateFormat("HH:mm:ss");
	
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Type the id of kddi data");
		String id = in.nextLine();
		BufferedReader br = new BufferedReader(new FileReader("D:/"+id+"/simulated_trajectory.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/"+id+"/trip.csv"));
		String line = null;
		String prev_mesh_id = "";
		String prev_action = "";
		double prev_lon=-1;
		double prev_lat=-1;
		while ((line = br.readLine()) != null) {  
			String[] tokens = line.split(" ");
			Integer time_slot = Integer.valueOf(tokens[0]);
			String action = tokens[1];
			String mesh_id = tokens[2];
			if(!prev_mesh_id.equals(mesh_id)){
				Integer sec = time_slot*1800+(int)RandomNumber(0.0,1800.0);
				String sDateTime = secToTime(sec);
				Mesh mesh = new Mesh(mesh_id);
				Double lat = mesh.getCenter().getLat()+RandomNumber((-mesh.getHeightInDegree().doubleValue()*0.5),(mesh.getHeightInDegree().doubleValue()*0.5));
				Double lon = mesh.getCenter().getLon()+RandomNumber((-mesh.getWidthInDegree().doubleValue()*0.5),(mesh.getWidthInDegree().doubleValue()*0.5));
				bw.write(id+","+sDateTime+","+lon+","+lat);
				bw.newLine();
				System.out.println(id+","+time_slot+","+sDateTime+","+lon+","+lat+","+mesh_id);  
				prev_action = action;
				prev_mesh_id = mesh_id;
				prev_lon = lon;
				prev_lat = lat;
			}else{
				Integer sec = time_slot*1800+(int)RandomNumber(0.0,1800.0);
				String sDateTime = secToTime(sec);
				bw.write(id+","+sDateTime+","+prev_lon+","+prev_lat);
				bw.newLine();
				System.out.println(id+","+time_slot+","+sDateTime+","+prev_lon+","+prev_lat+","+mesh_id); 
			}
	
		
        }  
		br.close();
		bw.close();
	}
	
	public static double RandomNumber(Double min,Double max){
		return Math.random()*(max-min)+min;
	}
	
	public static String secToTime(int time) {  
        String timeStr = null;  
        int hour = 0;  
        int minute = 0;  
        int second = 0;  
        if (time <= 0)  
            return "00:00";  
        else {  
            minute = time / 60;  
            if (minute < 60) {  
                second = time % 60;  
                timeStr = unitFormat(minute) + ":" + unitFormat(second);  
            } else {  
                hour = minute / 60;  
                if (hour > 99)  
                    return "99:59:59";  
                minute = minute % 60;  
                second = time - hour * 3600 - minute * 60;  
                timeStr ="2013/02/22 "+unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);  
            }  
        }  
        return timeStr;  
    }  
  
    public static String unitFormat(int i) {  
        String retStr = null;  
        if (i >= 0 && i < 10)  
            retStr = "0" + Integer.toString(i);  
        else  
            retStr = "" + i;  
        return retStr;  
    }  
}
