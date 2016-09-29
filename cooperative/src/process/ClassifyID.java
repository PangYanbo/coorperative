package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

public class ClassifyID {
	
	public static void extractfile(String date){
		ProcessBuilder pb = new ProcessBuilder("tar",
				"zxvf","/tmp/bousai_data/gps_"+date+".tar.gz", 
                "-C","/home/t-iho/");
		pb.inheritIO();
		try{
			Process process = pb.start();
			process.waitFor();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		File out = new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date);
		out.renameTo(new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date+".csv"));
		File out2 =   new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date);
	    out2.renameTo(new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date+".csv"));
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Enter the date");
		Scanner in = new Scanner(System.in);
		String date = in.nextLine();
//		extractfile(date);
		
		//String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date+".csv";
		
		String filepath = "C:/Users/Šâ”Ž/Desktop/Programing  training/lesson02/02_java_sample_20150423/sample-data.csv";
		try {
			// open file reader
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date+"filter"+".csv"));
			// remove header line
			String line = br.readLine();

			HashMap<String,HashMap<String,String>>map = new HashMap<String,HashMap<String,String>>();
					
			while( (line=br.readLine())!=null){ 
				String[] tokens    = line.split("	",-1);	// split line with comma ","
				if(!tokens[0].equals("null")){
					String lonlat = tokens[2]+","+tokens[3];
					
					if(map.get(tokens[0])==null){
						HashMap<String,String>innerMap = new HashMap<String,String>();
						innerMap.put(tokens[4], lonlat);
						map.put(tokens[0], innerMap);
					}else{
						map.get(tokens[0]).put(tokens[4], lonlat);
					}
				}						
			}	
		
			for(String id:map.keySet()){
				HashMap<String, String>log=map.get(id);
				TreeSet<String>sortedkey=new TreeSet<String>(log.keySet());
				for(String time:sortedkey){
					String idline = id+","+time+","+log.get(time);
					bw.write(idline);
					bw.newLine();
				//	System.out.println(id+","+time+","+log.get(time));
				}			
			}
			
			// close file reader
			br.close();
			bw.close();
			// output result
			
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + filepath);
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

}
