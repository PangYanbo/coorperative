package InverseReinforcementLearning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

import jp.ac.ut.csis.pflow.geom.Mesh;

public class GenerateMovie {
	
	/*
	 *this class is used for generating movie(for mobmap) from state trajectories  
	 * 
	 * 
	 * */
	
	protected static final SimpleDateFormat SDF  = new SimpleDateFormat("HH:mm:ss");
	
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Type in file path");
		String path = in.nextLine();
		File f = new File(path);  
        File[] files = f.listFiles(); 
        ArrayList<File> list = new ArrayList<File>();  
        File out = new File("/home/t-iho/Result/MovieForMesh5339.csv");
        int count = 1;
        for (File file : files){
        	if(file.isFile()){
        		list.add(file);	
        	}
        }  
		for(File file:list){
			GenerateCoord(file,out,count);
			count++;
		}
	}
	
	public static void GenerateCoord(File in, File out, int count) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		FileWriter writer = new FileWriter(out,true);
		String line = br.readLine();
		String prev_mesh_id = "";
		String prev_action = "";
		double prev_lon=-1;
		double prev_lat=-1;
		while ((line = br.readLine()) != null) {  
			String[] tokens = line.split(",");
			String id = String.valueOf(count);
			Integer time_slot = Integer.valueOf(tokens[0]);
			String action = tokens[2];
			String mesh_id = tokens[1];
			if(!mesh_id.equals("53394526")){break;}
			if(!prev_mesh_id.equals(mesh_id)){
				Integer sec = time_slot*1800+(int)RandomNumber(0.0,600.0);
				String sDateTime = secToTime(sec);
				Mesh mesh = new Mesh(mesh_id);
				Double lat = mesh.getCenter().getLat()+RandomNumber((-mesh.getHeightInDegree().doubleValue()*0.5),(mesh.getHeightInDegree().doubleValue()*0.5));
				Double lon = mesh.getCenter().getLon()+RandomNumber((-mesh.getWidthInDegree().doubleValue()*0.5),(mesh.getWidthInDegree().doubleValue()*0.5));
				writer.write(id+","+sDateTime+","+lon+","+lat+"\r\n");
				System.out.println(id+","+time_slot+","+sDateTime+","+lon+","+lat+","+mesh_id);  
				prev_action = action;
				prev_mesh_id = mesh_id;
				prev_lon = lon;
				prev_lat = lat;
			}else{
				Integer sec = time_slot*1800+(int)RandomNumber(0.0,600.0);
				String sDateTime = secToTime(sec);
				writer.write(id+","+sDateTime+","+prev_lon+","+prev_lat+"\r\n");
				System.out.println(id+","+time_slot+","+sDateTime+","+prev_lon+","+prev_lat+","+mesh_id); 
			}
        }  
		br.close();
		writer.close();
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
                timeStr ="2016-05-17 "+unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);  
            } else {  
                hour = minute / 60;  
                if (hour > 99)  
                    return "23:59:59";  
                minute = minute % 60;  
                second = time - hour * 3600 - minute * 60;  
                timeStr ="2016-05-17 "+unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);  
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
