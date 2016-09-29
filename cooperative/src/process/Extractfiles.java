package process;

import java.io.File;
import java.util.Scanner;

public class Extractfiles {

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
	
	public static void main(String[] args){
		System.out.println("Enter the date");
		Scanner in = new Scanner(System.in);
		String day = in.nextLine();
		extractfile(day);
	}
	
}
