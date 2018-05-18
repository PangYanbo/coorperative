package PTdata;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import jp.ac.ut.csis.pflow.geom.Mesh;

public class ConvertToMesh {
	
	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("D:/training data/pflow.csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/pflowMesh.csv"));
		String line = null;
		ArrayList<String> meshlist = new ArrayList<String>();
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",");
			String id = tokens[0];
			if(Integer.valueOf(id)>=10000){
				break;
			}
			String mode = null;
			if(tokens[7].equals("2")||tokens[7].equals("3")||tokens[7].equals("4")){
				mode = "bike";
			}else if(tokens[7].equals("5")||tokens[7].equals("6")||tokens[7].equals("7")||tokens[7].equals("8")||tokens[7].equals("9")||tokens[7].equals("10")){
				mode = "vehicle";
			}else if(tokens[7].equals("11")||tokens[7].equals("12")){
				mode = "train";
			}else if(tokens[7].equals("97")){
				mode = "stay";
			}else if(tokens[7].equals("1")){
				mode = "walk";
			}
			else{
				mode = "other";
			}
			Double ori_lon = Double.valueOf(tokens[8]);
			Double ori_lat = Double.valueOf(tokens[9]);
			Mesh origin = new Mesh(3,ori_lon,ori_lat);
			Double dest_lon = Double.valueOf(tokens[10]);
			Double dest_lat = Double.valueOf(tokens[11]);
			Mesh destination = new Mesh(3,dest_lon,dest_lat);
			bw.write(tokens[0]+","+tokens[1]+","+tokens[2]+","+tokens[3]+","+tokens[4]+","+tokens[5]+","+tokens[6]+","+mode+","+origin.getCode()+","+destination.getCode()+","+tokens[12]+","+tokens[13]+","+tokens[14]+","+tokens[15]+","+tokens[16]);
			bw.newLine();
			if(!meshlist.contains(origin.getCode())){
				meshlist.add(origin.getCode());
			}
			if(!meshlist.contains(destination.getCode())){
				meshlist.add(destination.getCode());
			}
		}
		bw.close();
		br.close();
		System.out.println(meshlist.size());
	}
}
