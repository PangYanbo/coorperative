package process;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class PrefectureFilter {

	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void Prefecture(String _filepath){
		try{
			BufferedReader br = new BufferedReader(new FileReader(_filepath));
			String line = null;
			
			Polygon p =null;
			
			while((line=br.readLine())!=null){
				String tokens[]=line.split(",");
				int x = (int)(Double.parseDouble(tokens[0])*10000);
				int y = (int)(Double.parseDouble(tokens[1])*10000);
				p.addPoint(x, y);
			}
			
			}
			catch(FileNotFoundException e) {
				System.out.println("File not found: " + _filepath);
			}
			catch(IOException e) {
				System.out.println(e);
			}
	}
	
	
	public static boolean contains(Polygon _p,double _x,double _y){
		return _p.contains(_x, _y);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String Prefecturepath = "D:\training data\ring.csv";
		Prefecture(Prefecturepath);
		
		System.out.println("Enter the data");
		Scanner in = new Scanner(System.in);
		String date = in.nextLine();
		
		String filepath="/home/t-iho/grid/0/tmp/ktsubouc/gps_"+date+".csv";
		
		try{		
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line = null;
		
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",");
			String uid = tokens[0];
			String did = tokens[1];
			Double lat = Double.parseDouble(tokens[2]);
			Double lon = Double.parseDouble(tokens[3]);
			Date time = toDate(tokens[4]);

		}
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + filepath);
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

}
