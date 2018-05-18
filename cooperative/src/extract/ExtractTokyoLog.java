package extract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import IDExtract.ID_Extract_Tools;
import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.STPoint;
import process.GeometryChecker;

public class ExtractTokyoLog {

	private static void readfile(File in, File shpdir, HashMap<String,ArrayList<STPoint>>id_points, String type) throws IOException, ParseException{
	//	GeometryChecker inst = new GeometryChecker(shpdir); //calculated in the userfilter to be faster
		
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
				if(tokens.length>=5&&!tokens[4].equals("null")){
					String id = type.equals("1")?tokens[0]:tokens[1];
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
	
//	public static void UserCheck(HashMap<String,ArrayList<STPoint>>id_points,Set<String>idList){
//		long startTime = System.currentTimeMillis();
//		HashMap<String,ArrayList<STPoint>>map2 = new HashMap<String,ArrayList<STPoint>>();
//		for(String id:id_points.keySet()){
//			if(id_points.get(id).size()>=20){
//				
//
//				HashSet<Integer>slot = new HashSet<Integer>();
//				Collections.sort(id_points.get(id));
//				for(STPoint point:id_points.get(id)){
//					int time = DateToSec(point.getTimeStamp());
//					slot.add(time/1800);
//				}
//				if(slot.size()>=8){
//					continue;
//				}else{
//					map2.put(id, id_points.get(id));
//				}
//			}
//		}
//		for(String key:map2.keySet()){
//			id_points.remove(key);
//		}
//		idList = id_points.keySet();
//		long endTime = System.currentTimeMillis();
//		System.out.println("finished filtering "+id_points.size()+" userlogs, calculating time is: "+(endTime-startTime)+"ms");
//	}
	
	public static void UserFilter(File shpdir,Integer threhold,HashMap<String,ArrayList<STPoint>>id_points){
		GeometryChecker inst = new GeometryChecker(shpdir);
		
		long startTime = System.currentTimeMillis();
		HashMap<String,ArrayList<STPoint>>map2 = new HashMap<String,ArrayList<STPoint>>();
		for(String id:id_points.keySet()){
			if(id_points.get(id).size()>=threhold){
				Collections.sort(id_points.get(id));
				
				Double lon = id_points.get(id).get(0).getLon();
				Double lat = id_points.get(id).get(0).getLat();
				
				LonLat ori = new LonLat(lon,lat);
				
				Double lon2 = id_points.get(id).get(id_points.get(id).size()-1).getLon();
				Double lat2 = id_points.get(id).get(id_points.get(id).size()-1).getLat();
				
				LonLat dest = new LonLat(lon2,lat2);
				
				if(inst.checkOverlap(lon, lat)&&inst.checkOverlap(lon2, lat2)){
					if(ori.distance(dest)<1000){
						HashSet<Integer>slot = new HashSet<Integer>();
						for(STPoint point:id_points.get(id)){
							int time = DateToSec(point.getTimeStamp());
							slot.add(time/1800);
						}
						if(slot.size()>=8){
							continue;
						}else{
							map2.put(id, id_points.get(id));
						}
					}
				}else{
					map2.put(id, id_points.get(id));
				}		
			}else{
				map2.put(id, id_points.get(id));
			}
		}
		for(String key:map2.keySet()){
			id_points.remove(key);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("finished checking "+id_points.size()+" userlogs, calculating time is: "+(endTime-startTime)+"ms");
	}
	
	public static void writeout(File out, File shpdir, HashMap<String,ArrayList<STPoint>>id_points) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		GeometryChecker inst = new GeometryChecker(shpdir);
		long startTime = System.currentTimeMillis();
		for(String id: id_points.keySet()){
			for(int i =0; i<id_points.get(id).size();i++){
				STPoint p = id_points.get(id).get(i);
				bw.write(id+","+SDF_TS2.format(p.getTimeStamp())+","+p.getLon()+","+p.getLat()+","+inst.checkOverlap(p.getLon(), p.getLat()));
				bw.newLine();
			}
		}
		bw.close();
		long endTime = System.currentTimeMillis();
		System.out.println("finished writing out, the time is: "+(endTime-startTime)+"ms");
	}
	
	public static void main(String[] args) throws IOException, ParseException{
		HashMap<String,ArrayList<STPoint>>id_points = new HashMap<String,ArrayList<STPoint>>();
		File   shpdir  = new File("/home/t-iho/Data/Tokyo/");
		File   railbuffer  = new File("/home/t-iho/Data/Railbuffer/");
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte start date in format of yyyyMMdd");
		int sdate = Integer.valueOf(in.nextLine());
		System.out.println("Type in thte end date in format of yyyyMMdd");
		int edate = Integer.valueOf(in.nextLine());
		for(int date = sdate;date<=edate;date++){
			id_points = new HashMap<String,ArrayList<STPoint>>();
			File gpsfile = new File("/home/t-iho/tmp/data_"+date+".csv");
			File out = new File("/home/t-iho/Result/rawdata/TokyoLog"+date+".csv");
			System.out.println("Type 1 is user id, 2 is day id");
			String type = in.nextLine();
			System.out.println("Type in the filtering threhold, i.e. at least 30 logs a day. Integer");
			Integer threhold = Integer.valueOf(in.nextLine());
			readfile(gpsfile,shpdir,id_points,type);
			UserFilter(shpdir,threhold,id_points);
			writeout(out,railbuffer,id_points);
		}
		
		
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
