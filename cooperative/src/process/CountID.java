package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CountID {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Enter the date");
		Scanner in = new Scanner(System.in);
		String day = in.nextLine();
		
		String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+".csv";;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+"IDCount.csv"));
			String line = br.readLine();
			
			List<String>UIDlist = new ArrayList<String>();
			
			while( (line=br.readLine())!=null){ 
				
				String[] tokens  = line.split("	",-1);	// split line with comma "	"

					String uid = tokens[0];
					String did = tokens[1];

					if(uid!="null"&&!UIDlist.contains(uid)){
						UIDlist.add(uid);
						bw.write(uid+","+did);
						bw.newLine();
					}			
			}
			System.out.println(UIDlist.size());
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
