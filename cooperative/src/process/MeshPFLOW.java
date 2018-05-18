package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import jp.ac.ut.csis.pflow.geom.Mesh;

public class MeshPFLOW {
	
	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("D:/training data/pflow.csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/pflowMesh.csv"));
		String line = null;
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",");
			String mode = null;
			if(tokens[7]=="2"||tokens[7]=="3"||tokens[7]=="4"){
				mode = "bike";
			}else if(tokens[7]=="5"||tokens[7]=="6"||tokens[7]=="7"||tokens[7]=="8"||tokens[7]=="9"||tokens[7]=="10"){
				mode = "vehicle";
			}else if(tokens[7]=="11"||tokens[7]=="12"){
				mode = "train";
			}else if(tokens[7]=="97"){
				mode = "stay";
			}else if(tokens[7]=="1"){
				mode = "walk";
			}else{
				mode = "others";
			}
			Double ori_lon = Double.valueOf(tokens[8]);
			Double ori_lat = Double.valueOf(tokens[9]);
			Mesh origin = new Mesh(3,ori_lon,ori_lat);
			Double dest_lon = Double.valueOf(tokens[10]);
			Double dest_lat = Double.valueOf(tokens[11]);
			Mesh destination = new Mesh(3,dest_lon,dest_lat);
			bw.write(tokens[0]+","+tokens[1]+","+tokens[2]+","+tokens[3]+","+tokens[4]+","+tokens[5]+","+tokens[6]+","+mode+","+origin+","+destination+","+tokens[12]+","+tokens[13]+","+tokens[14]+","+tokens[15]+","+tokens[16]);
			bw.newLine();
		}
		bw.close();
		br.close();
	}
}

