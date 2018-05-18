package readfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import jp.ac.ut.csis.pflow.geom.STPoint;

public class ReadBufferPoint {
	
	public static void ReadBufferPoint(HashMap<String,ArrayList<STPoint>>id_points, File in) throws ParseException, Exception, IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		long startTime = System.currentTimeMillis();
		int count = 0;
		
		while((line=br.readLine())!=null){
			count++;
			if(count%100000==0){
				System.out.println("scanned "+count+" lines");
			}
		
		
			String tokens[] = line.split(",");
			
			String id = tokens[0];
			Double lat = Double.parseDouble(tokens[3]);
			Double lon = Double.parseDouble(tokens[2]);
			
			boolean inbuffer = Boolean.parseBoolean(tokens[4]);
			
			Date dt = SDF_TS2.parse(tokens[1]);
			STPoint point = new STPoint(dt,lon,lat,inbuffer);
			if(!id_points.containsKey(id)){
				ArrayList<STPoint>points = new ArrayList<STPoint>();
				points.add(point);
				id_points.put(id, points);
			}else{
				id_points.get(id).add(point);
			}				
			
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("finished reading files with "+id_points.size()+" users in tokyo area using" +(endTime-startTime)+"ms");
		br.close();
	}
	
	public static void ReadModePoint(HashMap<String,ArrayList<STPoint>>id_points, File in) throws ParseException, Exception, IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		long startTime = System.currentTimeMillis();
		int count = 0;
		
		HashMap<String,ArrayList<STPoint>>id_p = new HashMap<String,ArrayList<STPoint>>();
		
		while((line=br.readLine())!=null&&count<100000){
			count++;
			if(count%100000==0){
				System.out.println("scanned "+count+" lines");
			}
		
			String tokens[] = line.split(",");
			if(tokens.length>3){
				String id = tokens[0];
				Double lat = Double.parseDouble(tokens[3]);
				Double lon = Double.parseDouble(tokens[2]);
				
				String mode = convertmode(tokens[4]);
				
				Date dt = SDF_TS4.parse(tokens[1]);
				STPoint point = new STPoint(dt,lon,lat,mode);
				if(!id_p.containsKey(id)){
					ArrayList<STPoint>points = new ArrayList<STPoint>();
					points.add(point);
					id_p.put(id, points);
				}else{
					id_p.get(id).add(point);
				}				
			}
		}
		
		int num = 0;
		for(String id:id_p.keySet()){
			
			
			if(num%50==0){
				System.out.println("put on id in %50+ "+num);
				id_points.put(id, id_p.get(id));
				Collections.sort(id_points.get(id));
			}
			
			num = num + 1;
		}
		id_p.clear();
		long endTime = System.currentTimeMillis();
		System.out.println("finished reading files with "+id_points.size()+" users in tokyo area using" +(endTime-startTime)+"ms");
		br.close();
	}

	
	public static void ReadKDDIPoint(HashMap<String,ArrayList<STPoint>>id_points, File in) throws ParseException, Exception, IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		long startTime = System.currentTimeMillis();
		int count = 0;
		
		while((line=br.readLine())!=null){
			count++;
			if(count%100000==0){
				System.out.println("scanned "+count+" lines");
			}
		
		
			String tokens[] = line.split(",");
			
			String id = tokens[1];
			Double lat = Double.parseDouble(tokens[5]);
			Double lon = Double.parseDouble(tokens[4]);
			
			boolean inbuffer = Boolean.parseBoolean(tokens[4]);
			
			Date dt = SDF_TS2.parse(tokens[1]);
			STPoint point = new STPoint(dt,lon,lat,inbuffer);
			if(!id_points.containsKey(id)){
				ArrayList<STPoint>points = new ArrayList<STPoint>();
				points.add(point);
				id_points.put(id, points);
			}else{
				id_points.get(id).add(point);
			}				
			
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("finished reading files with "+id_points.size()+" users in tokyo area using" +(endTime-startTime)+"ms");
		br.close();
	}
	
	
	public static int DateToSec(Date t){
		int sec = 0;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(t);
		sec = calendar.get(Calendar.HOUR_OF_DAY)*3600+calendar.get(Calendar.MINUTE)*60+calendar.get(Calendar.SECOND);
		return sec;
	}
	
	public static String convertmode(String num){
		String mode = null;
		if(num.equals("1")){
			mode = "walk";
		}else if(num.equals("2")){
			mode = "vehicle";
		}else if(num.equals("3")){
			mode = "train";
		}else if(num.equals("4")){
			mode = "walk";
		}else if(num.equals("99")){
			mode = "stay";
		}
		return mode;
	}
	
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
	protected static final SimpleDateFormat SDF_TS4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
