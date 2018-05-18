package movie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import jp.ac.ut.csis.pflow.geom.Mesh;

public class GenerateIRLTrajectory {

		
		/*
		 *this class is used for generating movie(for mobmap) from state trajectories  
		 * 
		 * 
		 * */
		
		protected static final SimpleDateFormat SDF  = new SimpleDateFormat("HH:mm:ss");
		
		public static void main(String[] args) throws IOException{
			
			File in = new File("D:/training data/PTtraj.csv");
	        File out = new File("D:/training data/ODchain4PT.csv");
	     

			GenerateCoord(in,out);
				
		}
		
		public static void GenerateCoord(File in, File out) throws IOException{
			BufferedReader br = new BufferedReader(new FileReader(in));
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {  
				String[] tokens = line.split(",");
				String id = tokens[0];
				Integer time_slot = Integer.valueOf(tokens[1]);
				System.out.println(tokens[4]);
				String mode = tokens[4];
				Mesh origin = new Mesh(tokens[2]);
				Mesh destination = new Mesh(tokens[3]);
				Integer sec = time_slot*1800+(int)RandomNumber(0.0,600.0);
				String sDateTime = secToTime(sec);
				
				Double ori_lat = origin.getCenter().getLat()+RandomNumber((-origin.getHeightInDegree().doubleValue()*0.5),(origin.getHeightInDegree().doubleValue()*0.5));
				Double ori_lon = origin.getCenter().getLon()+RandomNumber((-origin.getWidthInDegree().doubleValue()*0.5),(origin.getWidthInDegree().doubleValue()*0.5));
				Double dest_lat = destination.getCenter().getLat()+RandomNumber((-origin.getHeightInDegree().doubleValue()*0.5),(origin.getHeightInDegree().doubleValue()*0.5));
				Double dest_lon = destination.getCenter().getLon()+RandomNumber((-origin.getWidthInDegree().doubleValue()*0.5),(origin.getWidthInDegree().doubleValue()*0.5));
				bw.write(id+","+sDateTime+","+ori_lon+","+ori_lat+","+dest_lon+","+dest_lat+","+mode);
				bw.newLine();
				//System.out.println(id+","+time_slot+","+sDateTime+","+lon+","+lat+","+mesh_id);  
				
				
				
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
	                timeStr =unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);  
	            } else {  
	                hour = minute / 60;  
	                if (hour > 99)  
	                    return "23:59:59";  
	                minute = minute % 60;  
	                second = time - hour * 3600 - minute * 60;  
	                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);  
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


