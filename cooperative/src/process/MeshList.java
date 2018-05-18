package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import jp.ac.ut.csis.pflow.geom.*;

public class MeshList {

	public static void main(String[] args){

		try {
			// open file reader
			String filepath = "D:/IRL/Data/mesh_list.csv";
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/IRL/Data/mesh_listsorted.csv"));
			String line = null;
			while((line = br.readLine())!=null){
				String[] tokens = line.split(",");
				String mesh_no = tokens[4];
				Integer office_no = Integer.valueOf(tokens[1]);
				Integer employee_no = Integer.valueOf(tokens[2]);
				Mesh mesh = new Mesh(mesh_no);
				LonLat coord = mesh.parseMeshCode(mesh_no);
				
				String out = tokens[0]+","+tokens[1]+","+tokens[2]+","+tokens[3]+","+tokens[4]+","+coord.getLat()+","+coord.getLon();
				bw.write(out);
				bw.newLine();
			}
			br.close();
			bw.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: ");
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	
}
