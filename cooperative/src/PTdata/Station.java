package PTdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Station {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
String filepath = "D:/training data/kanto-station.csv";
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/stationLL.csv"));
			
			bw.write("lon,lat");
			bw.newLine();
			
			String line = br.readLine();
			
			while((line=br.readLine())!=null){
				String tokens[] = line.split(",",-1);
				
				
				Double lon = Double.valueOf(tokens[9]);		
				Double lat = Double.valueOf(tokens[10]);
				
				String example = lon+","+lat;
				bw.write(example);
				bw.newLine();
			
			}
			br.close();
			bw.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + filepath);
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	

}
