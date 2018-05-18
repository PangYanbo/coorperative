package generate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class UbiModify {
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Type in the GPS file date");
		String date = in.nextLine();
		
		File gpsfile = new File("/home/t-iho/Result/trainingdata/trainingdata"+date+".csv");
		File out = new File("/home/t-iho/Result/trainingdata/Modifiedtrainingdata"+date+".csv");
		
		BufferedReader br = new BufferedReader(new FileReader(gpsfile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		
		String line = null;
		String prev_id = null;
		String prev_slot = null;
		String prev_line = null;
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			String id = tokens[0];
			if(!id.equals(prev_id)&&prev_slot!="47"&&prev_line!=null){
				String[] tokens2= prev_line.split(",");
				bw.write(tokens2[0]+","+"47"+","+tokens[2]+","+tokens[3]+","+tokens[4]);
				bw.newLine();
			}
			bw.write(line);
			bw.newLine();
			prev_line = line;
			prev_slot = tokens[1];
			prev_id = tokens[0];
		}
		br.close();
		bw.close();
	}
}
