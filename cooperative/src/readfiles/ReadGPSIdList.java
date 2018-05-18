package readfiles;

import java.io.BufferedReader;
import java.io.File;
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

public class ReadGPSIdList {
	
	public static void readfile(File in, File shpdir, HashMap<String,ArrayList<STPoint>>id_points, ArrayList<String>id_List) throws IOException, ParseException{
	//	GeometryChecker inst = new GeometryChecker(shpdir);
		
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		String prevline = null;
		long startTime = System.currentTimeMillis();
		int count = 0;
		int count2 = 0;
		while((line=br.readLine())!=null){
			count++;
			if(count%100000==0){
				System.out.println("scanned "+count+" lines");
			}
			if(SameLogCheck(line,prevline)==true){
				String tokens[] = line.split("\t");
				if(tokens.length>=5&&!tokens[4].equals("null")&&id_List.contains(tokens[0])){
					String id = tokens[0];
					Double lat = Double.parseDouble(tokens[2]);
					Double lon = Double.parseDouble(tokens[3]);
					if(lat>=35.33&&lat<=36&&lon>=139&&lon<=140){
					//	if(inst.checkOverlap(lon, lat)){
						count2++;
						if(count2%100000==0){
							System.out.println("processed "+count2+" lines");
						}
						Date dt = SDF_TS2.parse(tokens[4]);
						STPoint point = new STPoint(dt,lon,lat);
						if(!id_points.containsKey(id)){
							ArrayList<STPoint>points = new ArrayList<STPoint>();
							points.add(point);
							id_points.put(id, points);
						}else{
							id_points.get(id).add(point);
						}
			//			}
					}
				}
			}	
			prevline = line;
		}
		for(String id:id_points.keySet()){
			String pre_date = null;
			Collections.sort(id_points.get(id));
			ArrayList<STPoint>temp = new ArrayList<STPoint>();
			for(STPoint point:id_points.get(id)){
				String date = SDF_TS2.format(point.getTimeStamp());
				if(!date.equals(pre_date)){
					temp.add(point);
				}
				pre_date = date;
			}
			id_points.put(id, temp);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("finished reading files with "+id_points.size()+" users in tokyo area using" +(endTime-startTime)+"ms");
		br.close();
	}
	
	public static boolean SameLogCheck(String line, String prevline){
		if(line.equals(prevline)){
			return false;
		}
		else{
			return true;
		}
	}
	
	public static int DateToSec(Date t){
		int sec = 0;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(t);
		sec = calendar.get(Calendar.HOUR_OF_DAY)*3600+calendar.get(Calendar.MINUTE)*60+calendar.get(Calendar.SECOND);
		return sec;
	}
	
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
	public static final String GPSdeeppath = "/home/t-iho/Data/grid/0/tmp/ktsubouc/gps_";

	
}
