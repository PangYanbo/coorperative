package process;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import PTdata.point;

public class PrefectureFilter {

	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd HH:mm:SS")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void Prefecture(String _filepath, Polygon _p){
	
		try{
			BufferedReader br = new BufferedReader(new FileReader(_filepath));
			String line = null;
			while((line=br.readLine())!=null){
				String tokens[]=line.split(",",-1);
				int x = (int)(Double.parseDouble(tokens[0])*10000);
				int y = (int)(Double.parseDouble(tokens[1])*10000);
				_p.addPoint(x, y);
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
		String Prefecturepath = "D:/training data/ring.csv";
		
//		System.out.println("Enter the data");
//		Scanner in = new Scanner(System.in);
//		String date = in.nextLine();
		
		String filepath="D:/training data/KDDI/2_data/1_gps/2_0005.csv";
		
		try{		
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		BufferedReader br2 = new BufferedReader(new FileReader("input-data/ring.csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/TokyoPT/2-0005filter.csv"));
		String line2 = null;
		List<Point>boundaries = new ArrayList<Point>();
		
		while((line2=br2.readLine())!=null){
			String tokens[] = line2.split(",");
			double lon = Double.parseDouble(tokens[0]);
			double lat = Double.parseDouble(tokens[1]);
			boundaries.add(new Point(lat,lon));
		}
		br2.close();
		
		String line = br.readLine();
		while((line=br.readLine())!=null){
			String tokens[] = line.split(",",-1);
			String pid = tokens[1];
			Date date = toDate(tokens[2]);
			Double lat = Double.parseDouble(tokens[5]);
			Double lon = Double.parseDouble(tokens[4]);
			
			Point p = new Point(lat,lon);
			 
			if(p.isPolygonContainsPoint(boundaries)){
				bw.write(lat+","+lon);
				bw.newLine();
			}
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
