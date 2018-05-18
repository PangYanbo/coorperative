package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jp.ac.ut.csis.pflow.geom.GeometryChecker;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class Kumamoto {
	public static String home = "/home/t-iho/"; // please change this path to your root path

	public static void main(String[] args) throws IOException{

		File out = new File(home+"kumamoto-out.csv");
		HashMap<String, HashMap<String, HashMap<String,Integer>>> code_day_hour_count = new HashMap<String, HashMap<String, HashMap<String,Integer>>>();
		HashMap<String, Double> day_magfac = new HashMap<String,Double>();
		
		for(int i=13; i<=20; i++){
			String date = "201604"+String.valueOf(i);
			System.out.println(date);
			File gpsfile = new File(home+"/grid/0/tmp/hadoop-ktsubouc/data_"+date+".csv"); // please change this path to gps file path
			readfile(gpsfile, code_day_hour_count, date, day_magfac);
			System.out.println("finished reading gpsfile:"+date);
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.write("code,hour,pop13,pop14,pop15,pop16,pop17,pop18,pop19,pop20");
		bw.newLine();
		for(String code: code_day_hour_count.keySet()){
		//	bw.write(code+",");
			for(int i=0; i<=23; i++){
				bw.write(code+","+i+",");
				for(int j=13; j<=20; j++){
					String date = "201604"+String.valueOf(j);
					//Double pop =String.valueOf(code_day_hour_count.get(code).get(date).get(String.valueOf(i))).isEmpty() ? 0 : Double.parseDouble(String.valueOf(code_day_hour_count.get(code).get(date).get(String.valueOf(i))))*day_magfac.get(date);
					Double pop = 0.0;
					if(!code_day_hour_count.get(code).containsKey(date)){
						pop = 0.0;
					}else {
						if(!code_day_hour_count.get(code).get(date).containsKey((i<10)?"0"+String.valueOf(i):String.valueOf(i))){
							pop = 0.0;
						}else{
							pop = Double.parseDouble(String.valueOf(code_day_hour_count.get(code).get(date).get((i<10)?"0"+String.valueOf(i):String.valueOf(i))))*day_magfac.get(date);
							}
					}
					bw.write(pop+",");
				}
				bw.newLine();
			}
			bw.newLine();
		}
		bw.close();
	}
	
	public static void zonerecognization(ArrayList<STPoint>points,File shp){
		GeometryChecker gc = new GeometryChecker(shp);
		for(STPoint point:points){
			
		}
	}

	
	
	public static void readfile(File in, HashMap<String, HashMap<String, HashMap<String,Integer>>> code_day_hour_count, 
			String date, HashMap<String, Double> day_magfac) throws IOException{ // returns: <meshcode, <hour, pop> >
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		int allcount = 0;
		while((line=br.readLine())!=null){
			String[] tokens = line.split("\t");
			String id = tokens[0];
			if(id.length()>0){
				String time = tokens[4];
				String hour = time.split("T")[1].split(":")[0];
//				if(hour.equals("00")||hour.equals("01")||hour.equals("02")||hour.equals("03")||hour.equals("04")||hour.equals("05")||hour.equals("06")||hour.equals("1")||hour.equals("2")||hour.equals("3")||hour.equals("4")||hour.equals("5")||hour.equals("6"))
//				{
//					System.out.println(hour);
//				}
				Double lon = Double.parseDouble(tokens[3]);
				Double lat = Double.parseDouble(tokens[2]);
				if((lon>130.15327144)&&(lon<131.37912596)){
					if((lat>32.10863477)&&(lat<33.2791093)){
						allcount++;
						Mesh mesh = new Mesh(4,lon,lat);
						String code = mesh.getCode();
						if(code_day_hour_count.containsKey(code)){
							if(code_day_hour_count.get(code).containsKey(date)){
								if(code_day_hour_count.get(code).get(date).containsKey(hour)){
									int count = code_day_hour_count.get(code).get(date).get(hour)+1;
									code_day_hour_count.get(code).get(date).put(hour, count);
						//			System.out.println("hour,count++");
								}
								else{
									code_day_hour_count.get(code).get(date).put(hour, 1);
						//			System.out.println("hour,1");
								}
							}
							else{
								HashMap<String,Integer> temp = new HashMap<String,Integer>();
								temp.put(hour, 1);
								code_day_hour_count.get(code).put(date, temp);
						//		System.out.println("date,temp");
							}
						}
						else{
							HashMap<String,Integer> temp = new HashMap<String,Integer>();
							temp.put(hour, 1);
							HashMap<String, HashMap<String,Integer>> temp2 = new HashMap<String, HashMap<String,Integer>>();
							temp2.put(date, temp);
							code_day_hour_count.put(code, temp2);
//							System.out.println("code,temp2");
						}
					}
				}
			}
		}
		br.close();
		
		Double magfac = 1820000d/(double)allcount;
		day_magfac.put(date, magfac);
		System.out.println("code_day_hour_count with size of:"+code_day_hour_count.size());
	}

}
