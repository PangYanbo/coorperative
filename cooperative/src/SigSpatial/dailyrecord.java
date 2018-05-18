package SigSpatial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import MobilityAnalyser.Tools;

public class dailyrecord {
	
	public static void main(String[] args) throws IOException, ParseException{
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte target date in format of yyyyMMdd");
		String date = in.nextLine();
		Integer[] hour_log = new Integer[24];
		for(int i =0;i<24;i++){
			hour_log[i]=0;
		}
		BufferedReader br = new BufferedReader(new FileReader("/home/t-iho/grid/0/tmp/hadoop-ktsubouc/data_"+date+".csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/Result/japan.csv"));
		String line = null;
		String prevline = null;
		while((line=br.readLine())!=null){
			if(SameLogCheck(line,prevline)==true){
				String tokens[] = line.split("\t");
				if(!tokens[4].equals("null")){
					String did = tokens[1];
					Double lat = Double.parseDouble(tokens[2]);
					Double lon = Double.parseDouble(tokens[3]);
					String time = tokens[4];
					Integer hour = Integer.valueOf(time.split("T")[1].split(":")[0]);
					hour_log[hour]+=1;
					if(lat>=35.33&&lat<=36&&lon>=139&&lon<=140){	
						if(hour==8){
							bw.write(lat+","+lon);
							bw.newLine();	
						}
					}
					
				}
			}
		}
		br.close();
		bw.close();
		for(int i=0;i<24;i++){
			System.out.println("hour "+i+": "+hour_log[i]);
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
	
	public static int ConvertToTimeSlot(Date t){
		int secs = Tools.converttoSecs(SDF_TS.format(t));//interval 30min
		return secs/1800;
	}
	
	protected static final SimpleDateFormat SDF_TS = new SimpleDateFormat("HH:mm:ss");
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");
}
