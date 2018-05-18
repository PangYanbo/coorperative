package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import process.GeometryChecker;

public class GeoCheckerSpeed {
	
	public static void main (String[] args) throws IOException{
		File   shpdir  = new File("C:/Users/PangYanbo/Desktop/Tokyo/Rail/");
		//File   shpdir  = new File("C:/Users/PangYanbo/Desktop/Tokyo/");
		GeometryChecker inst = new GeometryChecker(shpdir);
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader("D:/training data/pflow.csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/testchecker.csv"));
		String line = null;
		long startTime = System.currentTimeMillis();
		while((line=br.readLine())!=null){
			count++;
			if(count%1000==0){
				System.out.println(count);
			}
			if(count==10000){
				break;
			}
			String tokens[] = line.split(",");
			String id = tokens[0];
			Double lon = Double.valueOf(tokens[8]);
			Double lat = Double.valueOf(tokens[9]);
			
				bw.write(id+","+lon+","+lat+","+inst.checkOverlap(lon, lat));
				bw.newLine();
			
		}
		long endTime = System.currentTimeMillis();
		System.out.println("finished reading one line: "+(endTime-startTime)+"ms");
		br.close();
		bw.close();
	}
	
}
