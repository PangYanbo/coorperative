package ubicomp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class linkVolume {
	public static void CompareLinkVolume(String date1, String date2, String hour) throws IOException{
		String path1 = "/home/t-iho/Result/sim/linkVolume"+date1+".csv";
		String path2 = "/home/t-iho/Result/sim/linkVolume"+date2+".csv";
		
		HashMap<String,Double>link_volume1 = new HashMap<String,Double>();
		HashMap<String,Double>link_volume2 = new HashMap<String,Double>();
		
		BufferedReader br = new BufferedReader(new FileReader(path1));
		BufferedReader br2 = new BufferedReader(new FileReader(path2));
		
		String out = "/home/t-iho/Result/sim/compareVolume"+date1+date2+"in_"+hour+".csv";
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		
		String line = br.readLine();
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			String linkid = tokens[0];
			Double volume = Double.valueOf(tokens[Integer.valueOf(hour)]);
			link_volume1.put(linkid, volume);
		}
		
		String line2 = br2.readLine();
		while((line2=br2.readLine())!=null){
			String[] tokens = line2.split(",");
			String linkid = tokens[0];
			Double volume = Double.valueOf(tokens[Integer.valueOf(hour)]);
			link_volume2.put(linkid, volume);
		}
		for(String link:link_volume1.keySet()){
			if(link_volume2.containsKey(link)){
				bw.write(link+","+link_volume1.get(link)+","+link_volume2.get(link));
				bw.newLine();
			}
		}
		bw.close();
		br.close();
		br2.close();
	}
	
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Type in thte first date in format of yyyyMMdd");
		String date1 = in.nextLine();
		System.out.println("Type in thte second date in format of yyyyMMdd");
		String date2 = in.nextLine();
		System.out.println("Type in thte hour be compared");
		String hour = in.nextLine();
		CompareLinkVolume(date1,date2,hour);
	}
}
