package generate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

public class DoubleCheck {
	public static void main(String[] args) throws ParseException, IOException, Exception{
		
		File in = new File("D:/training data/PT_commuter_irl.csv");
		File out = new File("D:/training data/PT_commuter_irl_revised.csv");
		
		BufferedReader br = new BufferedReader(new FileReader(in));
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		String line = br.readLine();
		String prevtokens[] = line.split(",");
		while((line=br.readLine())!=null){
			
			String tokens[] = line.split(",");
			if(!tokens[1].equals(prevtokens[1])){
				bw.write(line);
				bw.newLine();
				prevtokens = tokens;
			}
				
		}
	}
}
