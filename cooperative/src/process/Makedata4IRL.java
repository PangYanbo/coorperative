package process;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import IDExtract.ID_Extract_Tools;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class Makedata4IRL {

	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
	public static final String GPSdeeppath = "/home/t-iho/Data/grid/0/tmp/ktsubouc/gps_";
	
	public static ArrayList<String>getIdList(String path) throws IOException{
		ArrayList<String>idList = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line =null;
		while((line=br.readLine())!=null){
			String tokens[] = line.split("\t");
			if(!idList.contains(tokens[0]))
				idList.add(tokens[0]);
		}
		return idList;
	}
	
	public static HashMap<String,HashMap<String,ArrayList<STPoint>>>id_day_point(String path, ArrayList<String>id_list) throws IOException, ParseException{
		HashMap<String,HashMap<String,ArrayList<STPoint>>>id_day_point = new HashMap<String, HashMap<String,ArrayList<STPoint>>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = null;
		String prevline = null;
		while((line=br.readLine())!=null){
			if(ID_Extract_Tools.SameLogCheck(line,prevline)==true){
				String[] tokens = line.split("\t");
				if(tokens.length>=5){
					if(!tokens[4].equals("null")){
						String id = tokens[0];
						if(!id.equals("null")){
							if(id_list.contains(id)){
								Double lat = Double.parseDouble(tokens[2]);
								Double lon = Double.parseDouble(tokens[3]);
								Date dt = SDF_TS2.parse(tokens[4]);
								String day = SDF_TS3.format(dt);
								String time = SDF_TS.format(dt);
								STPoint p = new STPoint(dt,lon,lat);
								//mesh no 5339
								if(lat>=35.33&&lat<=36&&lon>=139&&lon<=140){
									if(id_day_point.containsKey(id)){
										if(id_day_point.get(id).containsKey(day)){
											id_day_point.get(id).get(day).add(p);
									}
										else{
											ArrayList<STPoint> list = new ArrayList<STPoint>();
											list.add(p);
											id_day_point.get(id).put(day, list);
										}
									}
									else{
										ArrayList<STPoint> list = new ArrayList<STPoint>();
										list.add(p);
										HashMap<String, ArrayList<STPoint>> tempmap = new HashMap<String, ArrayList<STPoint>>();
										tempmap.put(day, list);
										id_day_point.put(id, tempmap);
									}
								}
						}
							}
						}
					}
				}
				prevline = line;
			}
		br.close();
		System.out.println("finish sorting map into id_day_point");
		return id_day_point;
	}
	
	public static void writefile(HashMap<String,HashMap<String,ArrayList<STPoint>>>id_day_point, String path) throws IOException{
		for(String _id:id_day_point.keySet()){
			if(id_day_point.get(_id).size()>=10){	
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path+_id+".csv")));
				for(String _day:id_day_point.get(_id).keySet()){
					Collections.sort(id_day_point.get(_id).get(_day));
					for(int i =0; i < id_day_point.get(_id).get(_day).size(); i++)
					{
						bw.write(_id+","+SDF_TS2.format(id_day_point.get(_id).get(_day).get(i).getTimeStamp())+","+id_day_point.get(_id).get(_day).get(i).getLon()+","+id_day_point.get(_id).get(_day).get(i).getLat());
						bw.newLine();
					}
				}
				bw.close();
			}
		}
	}
	
	
	
	
	
	public static void main(String args[]) throws IOException, Exception{
		String idListPath = "/home/t-iho/Data/id.csv";
		ArrayList<String>idList = getIdList(idListPath);
		Scanner in = new Scanner(System.in);
		String ymd = in.nextLine();
		String path = "/home/t-iho/Data/grid/0/tmp/ktsubouc/gps_201505alldata.csv";
		String dicts = "/home/t-iho/Data/forIRL";
		
		HashMap<String,HashMap<String,ArrayList<STPoint>>>ids_days_points = id_day_point(path,idList);
		writefile(ids_days_points,dicts);
		
		
		
		}	
}
	
