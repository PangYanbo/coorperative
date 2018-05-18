package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.ac.ut.csis.pflow.geom.Mesh;
import process.GeometryChecker;

public class PflowRoute {
	
	public static void main(String[] args) throws NumberFormatException, IOException{
		List<String> l = new ArrayList<String>();
		 l.add("aa");
		 l.add("bb");
		 l.add("cc");
		 Iterator iter = l.iterator();
		 int count =0;
		 while(count<10){
			 count++;
			 if(iter.hasNext()){
				 String str = (String)iter.next();
				  System.out.println(str);
			 } 
		 }
		 
		
		 
		File   shpdir  = new File("C:/Users/PangYanbo/Desktop/Tokyo/shp");
		GeometryChecker inst = new GeometryChecker(shpdir);
		
		BufferedReader br = new BufferedReader(new FileReader("D:/training data/pflow.csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/testchecker.csv"));
		String line = null;
		long startTime = System.currentTimeMillis();
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",");
			String id = tokens[0];
	
			Double ori_lon = Double.valueOf(tokens[8]);
			Double ori_lat = Double.valueOf(tokens[9]);
			Mesh origin = new Mesh(3,ori_lon,ori_lat);
			Double dest_lon = Double.valueOf(tokens[10]);
			Double dest_lat = Double.valueOf(tokens[11]);
		}
	}
	
}
