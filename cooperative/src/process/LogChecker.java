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
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import IDExtract.ID_Extract_Tools;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class LogChecker {
	
	public static void main(String[] args) throws IOException, ParseException{
		HashMap<String,ArrayList<STPoint>>did_point = new HashMap<String,ArrayList<STPoint>>();
		HashMap<String,ArrayList<STPoint>>uid_point = new HashMap<String,ArrayList<STPoint>>();
		Scanner in = new Scanner(System.in);
		System.out.println("type in the date");
		String date = in.nextLine();
		String path = "/home/t-iho/grid/0/tmp/hadoop-ktsubouc/data_"+date+".csv";
		String out = "/home/t-iho/grid/0/tmp/hadoop-ktsubouc/counter"+date+".csv";
		File file = new File(path);
		File writeout = new File(out);
		System.out.println(writeout.getName());
		System.out.println(path);
		LogReader(file,did_point,uid_point);
		LogCounter(writeout,did_point,uid_point);
	}
	
	public static void LogReader(File in,HashMap<String,ArrayList<STPoint>>did_point,HashMap<String,ArrayList<STPoint>>uid_point) throws IOException, ParseException{
		System.out.println("processing "+in.getName());
		HashMap<Integer,Integer>hour_count = new HashMap<Integer,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		String prevline = null;
		while((line=br.readLine())!=null){
			if(ID_Extract_Tools.SameLogCheck(line,prevline)==true){
				String[] tokens = line.split("\t");
				if(tokens.length>=5&&!tokens[4].equals("null")){
					String uid = tokens[0];
					String did = tokens[1];
					Double lat = Double.parseDouble(tokens[2]);
					Double lon = Double.parseDouble(tokens[3]);
					Date dt = SDF_TS2.parse(tokens[4]);
					Integer hour = Integer.valueOf(SDF_TS.format(dt));
					if(!hour_count.containsKey(hour)){
						hour_count.put(hour, 1);
					}else{
						int temp = hour_count.get(hour);
						temp +=1;
						hour_count.put(hour, temp);
					}
					
				
				}
			}
			prevline=line;
		}
		br.close();
		for(Integer hour:hour_count.keySet()){
			System.out.println(hour+","+hour_count.get(hour));
		}
		System.out.println("finish sorting file "+in.getName()+" into map");
	}
	
	public static void hourlyLog(HashMap<String,ArrayList<STPoint>>did_point){
		HashMap<Integer,Integer>hour_count = new HashMap<Integer,Integer>();
		for(String id:did_point.keySet()){
			
		}
	}

	public static void LogCounter(File out, HashMap<String,ArrayList<STPoint>>did_point,HashMap<String,ArrayList<STPoint>>uid_point) throws IOException{
		Integer count_did = did_point.size();
		Integer count_uid = uid_point.size();
		Integer[] count_log = new Integer[10000];
		for(int i = 0; i < 10000; i++){
			count_log[i] = 0;
		}
		for(String did:did_point.keySet()){
			count_log[did_point.get(did).size()]++;
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.write("file has "+"daily id"+","+count_did);
		bw.newLine();
		bw.write("file has "+"daily id"+","+count_uid);
		bw.newLine();
		for(int i = 0; i < 1000; i++){
			bw.write(i+","+count_log[i]);
			bw.newLine();
		}
		bw.close();
	}

	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
}
