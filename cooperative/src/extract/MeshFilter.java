package extract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MeshFilter {
	public static void LonLatFilter(File in, File out,Double minLat, Double minLon, Double maxLat, Double maxLon) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		String line = null;
		int count = 0;
		while((line=br.readLine())!=null){
			count++;
			if(count%100000==0){
				System.out.println("scanned "+count+" lines");
			}
			String tokens[] = line.split("\t");
			if(tokens.length>=5&&!tokens[4].equals("null")){
				Double lat = Double.parseDouble(tokens[2]);
				Double lon = Double.parseDouble(tokens[3]);
				if(lat>=minLat&&lat<=maxLat&&lon>=minLon&&lon<=maxLon){
					bw.write(line);
					bw.newLine();
				}
			}
		}
		br.close();
		bw.close();
	}
	
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte target date in format of yyyyMMdd");
		String date = in.nextLine();
		File input = new File("/home/t-iho/grid/0/tmp/hadoop-ktsubouc/data_"+date+".csv");
		File output = new File("/home/t-iho/grid/0/tmp/hadoop-ktsubouc/Tokyodata_"+date+".csv");
		
		System.out.println("Type in the LonLat bondary: minLat");
		Double minLat = Double.valueOf(in.nextLine());
		System.out.println("Type in the LonLat bondary: minLon");
		Double minLon = Double.valueOf(in.nextLine());
		System.out.println("Type in the LonLat bondary: maxLat");
		Double maxLat = Double.valueOf(in.nextLine());
		System.out.println("Type in the LonLat bondary: maxLon");
		Double maxLon = Double.valueOf(in.nextLine());
		LonLatFilter(input,output,minLat,minLon,maxLat,maxLon);
	}
}
