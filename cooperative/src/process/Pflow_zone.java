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
import java.util.Arrays;
import java.util.Date;

import jp.ac.ut.csis.pflow.geom.STPoint;

public class Pflow_zone {
	
	public static ArrayList<String> zone = new ArrayList<>(Arrays.asList("Adachi Ku","Arakawa Ku","Bunkyo Ku","Chiyoda Ku","Chiyoda Ku","Chuo Ku","Edogawa Ku","Itabashi Ku","Katsushika Ku","Kita Ku","Koto Ku","Meguro Ku","Minato Ku","Nakano Ku","Ota Ku","Setagaya Ku","Shibuya Ku","Shinagawa Ku","Shinjuku Ku","Suginami Ku","Sumida Ku","Taito Ku","Toshima Ku","Others"));
	
	public static void main(String[] args) throws IOException, ParseException{
		File   shpdir  = new File("C:/Users/PangYanbo/Desktop/Tokyo/TokyoZone/");
		GeometryChecker inst = new GeometryChecker(shpdir);
		
		
		BufferedReader br = new BufferedReader(new FileReader("D:/training data/pflow.csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/pflow_zone.csv"));
		String line = null;
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",");
			
			if(tokens[7]!="97"){
				Date dt = SDF_TS2.parse(tokens[5]);
				Date dt2 = SDF_TS2.parse(tokens[6]);
				
				Double ori_lon = Double.valueOf(tokens[8]);
				Double ori_lat = Double.valueOf(tokens[9]);
				STPoint ori_point = new STPoint(dt,ori_lon,ori_lat);
				
				Double dest_lon = Double.valueOf(tokens[10]);
				Double dest_lat = Double.valueOf(tokens[11]);
				STPoint dest_point = new STPoint(dt2,dest_lon,dest_lat);
				
				String origin = inst.listOverlaps("laa",ori_point.getLon(), ori_point.getLat()).size()==0?"Others":inst.listOverlaps("laa",ori_point.getLon(), ori_point.getLat()).get(0);
				String destination = inst.listOverlaps("laa",dest_point.getLon(), dest_point.getLat()).size()==0?"Others":inst.listOverlaps("laa",dest_point.getLon(), dest_point.getLat()).get(0);;
				origin = zone.contains(origin)?origin:"Others";
				destination  = zone.contains(destination)?destination:"Others";
				
				bw.write(line+","+origin+","+destination);
				bw.newLine();
			}
			
		}
		br.close();
		bw.close();
	}
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
}
