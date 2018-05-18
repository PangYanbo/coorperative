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
		
		File out = new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_" + date);
		out.renameTo(new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_" + date+".csv"));
		File out2 =   new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_" + date);
	    out2.renameTo(new File("/home/t-iho/grid/0/tmp/ktsubouc/gps_" + date+".csv"));
	}
	
	public static void main(String[] args){
		System.out.println("Enter the year and month (e.g.:201604)");
		Scanner in = new Scanner(System.in);
		String year_month = in.nextLine();
		System.out.println("Enter the number of days(7 days as 7)");
		String days = in.nextLine();
		for(int i = 1; i <= Integer.valueOf(days); i++){
			String day =(i<10)? "0"+Integer.toString(i):Integer.toString(i);
			String date = year_month+day;
			extractfile(date);
		}
	}
	
}
