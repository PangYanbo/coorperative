package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.Mesh;
import process.GeometryChecker;

public class Mesh2Zone {
	
	public static String convertMesh_Zone(GeometryChecker inst, Mesh mesh){
		LonLat point = mesh.getPoint();
		String zone = inst.listOverlaps("laa",point.getLon(), point.getLat()).size()==0?"Others":inst.listOverlaps("laa",point.getLon(), point.getLat()).get(0);
		return zone;
	}
	
	public static void main(String[] args) throws IOException{
		File   shpdir  = new File("/home/t-iho/Data/Tokyo/");
		//File   shpdir  = new File("C:/Users/PangYanbo/Desktop/Tokyo/TokyoZone/");
		GeometryChecker inst = new GeometryChecker(shpdir);
		File input = null;
		File output = null;
		
		String path = null;
		
		Scanner in = new Scanner(System.in);
		
		System.out.println("Type in the date in format of yyyyMMdd");
		Integer date = Integer.valueOf(in.nextLine());
		
		System.out.println("Type 1 process synthetic data, Type 2 process training data");
		String type = in.nextLine();
		if(type.equals("1")){
			path = "/home/t-iho/Result/UbiResult/Synthetic"+date+"/all.csv";
			input = new File(path);
			output = new File("/home/t-iho/Result/UbiResult/Synthetic"+date+"/all_zone.csv");
			BufferedReader br = new BufferedReader(new FileReader(input));
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			String line = null;
			while((line=br.readLine())!=null){
				String tokens[] = line.split(",");
				if(tokens[2]!="null"&&tokens[3]!="null"&&tokens[2]!=null&&tokens[3]!=null){
					Mesh origin = new Mesh(tokens[2]);
					Mesh destination = new Mesh(tokens[3]);
					String ori_zone = convertMesh_Zone(inst,origin);
					String dest_zone = convertMesh_Zone(inst,destination);
					bw.write(line+","+ori_zone+","+dest_zone);
					bw.newLine();
				}
			}
			br.close();
			bw.close();
		}else{
			path =  "/home/t-iho/Result/trainingdata/trainingdata"+date+".csv";
			input = new File(path);
			output = new File("/home/t-iho/Result/trainingdata/trainingdata"+date+"_zone.csv");
			BufferedReader br = new BufferedReader(new FileReader(input));
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			String line = null;
			while((line=br.readLine())!=null){
				String tokens[] = line.split(",");
				if(tokens[2]!="null"&&tokens[3]!="null"&&tokens[2]!=null&&tokens[3]!=null){
					Mesh origin = new Mesh(tokens[2]);
					Mesh destination = new Mesh(tokens[3]);
					String ori_zone = convertMesh_Zone(inst,origin);
					String dest_zone = convertMesh_Zone(inst,destination);
					bw.write(line+","+ori_zone+","+dest_zone);
					bw.newLine();
				}
			}
			br.close();
			bw.close();
		}
		
		
		
	
		
//		for(int i = 0; i<43; i++){
//			String path = "D:/ClosePFLOW/53393580/" + String.valueOf(i) + "/expert1.csv";
//			File input = new File(path);
//			File output = new File("D:/ClosePFLOW/53393580/" + String.valueOf(i) + "/expert1_zone.csv");
//			BufferedReader br = new BufferedReader(new FileReader(input));
//			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
//			String line = null;
//			while((line=br.readLine())!=null){
//				String tokens[] = line.split(",");
//				Mesh origin = new Mesh(tokens[2]);
//				Mesh destination = new Mesh(tokens[3]);
//				String ori_zone = convertMesh_Zone(inst,origin);
//				String dest_zone = convertMesh_Zone(inst,destination);
//				bw.write(line+","+ori_zone+","+dest_zone);
//				bw.newLine();
//			}
//			br.close();
//			bw.close();
//		}
//		
	}
}
