package PTdata;

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



public class ForDecisiontree {
	
	/** WGS84Ç≈ÇÃê‘ìπîºåa				*/	private static final double	WGS84_EQUATOR_RADIUS = 6378137;
	/** WGS84Ç≈ÇÃã…îºåa				*/	private static final double WGS84_POLAR_RADIUS   = 6356752.314245;
	/** WGS84Ç≈ÇÃó£êSó¶ÇÃÇQèÊ			*/	private static final double WGS84_ECCENTRICITY_2 = (WGS84_EQUATOR_RADIUS * WGS84_EQUATOR_RADIUS - 
																							WGS84_POLAR_RADIUS   * WGS84_POLAR_RADIUS  ) 
																							/ 
																							(WGS84_EQUATOR_RADIUS*WGS84_EQUATOR_RADIUS); 
	
	
	
	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd HH:mm:SS")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Double distance(double _deplat, double _deplon, double _arrlat, double _arrlon){
		double a  = WGS84_EQUATOR_RADIUS;
		double e2 = WGS84_ECCENTRICITY_2;
		double dy = Math.toRadians(_arrlat - _deplat); // p0.getLat()  - p1.getLat());
		double dx = Math.toRadians(_arrlon - _deplon); // p0.getLon()  - p1.getLon());
		double cy = Math.toRadians((_deplat + _arrlat)/2d); // (p0.getLat() + p1.getLat()) / 2d);
		double m  = a * (1-e2);
		double sc = Math.sin(cy);
		double W  = Math.sqrt(1d-e2*sc*sc);
		double M  = m/(W*W*W);
		double N  = a/W;
		
		double ym = dy*M;
		double xn = dx*N*Math.cos(cy);
		
		return Math.sqrt(ym*ym + xn*xn);
	}
	
	public static String summarytransport(int _transport){
		String Transport = null;
		switch(_transport){
		case 1: Transport = "Walk";break;
		case 2: Transport = "bicycle";break;
		case 3: Transport = "Auto Bicycle";break;
		case 4: Transport = "Auto Bicycle";break;
		case 5: Transport = "Vehicle";break;
		case 6: Transport = "Vehicle";break;
		case 7: Transport = "Vehicle";break;
		case 8: Transport = "Vehicle";break;
		case 9: Transport = "Vehicle";break;
		case 10: Transport = "Vehicle";break;
		case 11: Transport = "Train";break;
		case 12: Transport = "Train";break;
		case 13: Transport = "Ferry";break;
		case 14: Transport = "Airplane";break;
		case 15: 
		case 97:
		case 99:
		}
		return Transport;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filepath = "D:/training data/pflow.csv";
		String filepath2 = "D:/training data/stationLL.csv";
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedReader br2 = new BufferedReader(new FileReader(filepath2));
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/pftrainingwalkspeedrestriction.csv"));
			
			bw.write("transport,distance,time,nearstation");
			bw.newLine();
			
			String line = br.readLine();
			String line2 = br2.readLine();
			
			List<point>Station = new ArrayList<point>();
			
			while((line2=br2.readLine())!=null){
				String tokens[] = line2.split(",");
				double lon = Double.parseDouble(tokens[0]);
				double lat = Double.parseDouble(tokens[1]);
				Station.add(new point(lon,lat));
			}
			br2.close();
			
			while((line=br.readLine())!=null){
				String tokens[] = line.split(",",-1);
				
				int pid = Integer.valueOf(tokens[0]);
				int trip_no = Integer.valueOf(tokens[1]);
				int subtrip_no = Integer.valueOf(tokens[2]);
				double mfactor = Double.valueOf(tokens[3]);
				double mfactor2 = Double.valueOf(tokens[4]);
				Date deptime = toDate(tokens[5]);
				Date arrtime = toDate(tokens[6]);
				int transport = Integer.valueOf(tokens[7]);
				Double deplon = Double.parseDouble(tokens[8]);		
				Double deplat = Double.parseDouble(tokens[9]);
				Double arrlon = Double.parseDouble(tokens[10]);
				Double arrlat = Double.parseDouble(tokens[11]);
				int id = Integer.valueOf(tokens[12]);
		
				if(transport!=97&&deptime!=arrtime&&transport!=99&&transport!=15){
				Double tripdistance = distance(deplat,deplon,arrlat,arrlon);
				Double triptime = (arrtime.getTime()-deptime.getTime())/1000d;
				Double avgspeed = tripdistance/triptime;			
				
				String transmode = summarytransport(transport);
				
				point dep = new point(deplon,deplat);
				point arr = new point(arrlon,arrlat);
				
				
				//unite transport mode
				
				boolean matchstation = dep.nearstation(Station)&&arr.nearstation(Station);

				//data filter
				
				if(transmode.equals("Walk")&&avgspeed>2){
					continue;
//				}else if(transmode.equals("Train")&&avgspeed<1.3){
//					continue;
				}else
				{
//				if(avgspeed<50&&avgspeed>0.3){
				String example = transmode+","+tripdistance+","+triptime+","+matchstation;
				bw.write(example);
				bw.newLine();
				}
//				}
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
