package PTdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;



public class kddi2011 {

	/** WGS84Ç≈ÇÃê‘ìπîºåa				*/	private static final double	WGS84_EQUATOR_RADIUS = 6378137;
	/** WGS84Ç≈ÇÃã…îºåa				*/	private static final double WGS84_POLAR_RADIUS   = 6356752.314245;
	/** WGS84Ç≈ÇÃó£êSó¶ÇÃÇQèÊ			*/	private static final double WGS84_ECCENTRICITY_2 = (WGS84_EQUATOR_RADIUS * WGS84_EQUATOR_RADIUS - 
																							WGS84_POLAR_RADIUS   * WGS84_POLAR_RADIUS  ) 
																							/ 
																							(WGS84_EQUATOR_RADIUS*WGS84_EQUATOR_RADIUS); 
	
	
	
	private static Date toDate(String str){
		final List<String>dateFormats = new ArrayList<String>();
		dateFormats.add("yyyy-MM-dd HH:mm:SS");
		dateFormats.add("yyyy-MM-dd HH:mm");
		dateFormats.add("yyyy/MM/dd HH:mm");
		dateFormats.add("yyyy-MM-dd hh:mm");
		dateFormats.add("yyyy-MM-dd H:mm");
		dateFormats.add("yyyy-MM-dd h:mm");
		dateFormats.add("yyyy/MM/dd h:mm");
		dateFormats.add("yyyy/MM/dd hh:mm");
		for(String format:dateFormats){
		try {
			return (new SimpleDateFormat(format)).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
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
		case 1: Transport = "Train";break;
		case 2: Transport = "Bus";break;
		case 3: Transport = "Vehicle";break;
		case 4: Transport = "Vehicle";break;
		case 5: Transport = "Bycicle";break;
		case 6: Transport = "Bike";break;
		case 7: Transport = "Walk";break;
		case 8: Transport = "Others";break;
		}
		return Transport;
	}
	
	
	public static void recursion(String root, Vector<String> vecFile) {  
        File file = new File(root);  
        File[] subFile = file.listFiles();  
        for (int i = 0; i < subFile.length; i++) {  
            if (subFile[i].isDirectory()) {  
                recursion(subFile[i].getAbsolutePath(), vecFile);  
            } else {  
                String filename = subFile[i].getName();  
                vecFile.add(subFile[i].getAbsolutePath());  
            }  
        }  
    }  
	
	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String filepath = "D:/training data/KDDI/#201111.CDR-data";
//		String filepath2 = "D:/training data/stationLL.csv";
//		
//		File datadir = new File("D:/training data/KDDI/#201111.CDR-data");	// input data folder(max_activity_uid/avg_activity_uid)
//		File outdir  = new File("D:/training data/KDDI/#201111.CDR-data");	//output folder
		
		
		try{
			Vector<String> vecFile = new Vector<String>();  
	        recursion("D:/training data/KDDI/#201111.CDR-data", vecFile);  
	        BufferedWriter bw = new BufferedWriter(new FileWriter("D:/training data/KDDI/#201111.CDR-data/aab8216h/trip.csv"));
	        bw.write("Pid,TransMode,StartTime,EndTime,TripTime,isNearStation,TripPurpose,TripDistance,avgSpeed");
			bw.newLine();
			
	        for (String fileName : vecFile) {  
	            System.out.println(fileName);  
	            if(fileName.contains("stay.csv")){
	            	BufferedReader br = new BufferedReader(new FileReader(fileName));
        			BufferedReader br2 = new BufferedReader(new FileReader("D:/training data/stationLL.csv"));
        			
        	
        			
//        			bw.write("pid,trip_no,subtrip_no,sex_code,age_code,address_code,office,dep_zone,arr_zone,facility,arrive_facility,work_code,purpose_code,mfactor,mfactor2,transport,trans_num,deptime,deplon,deplat,arrtime,arrlon,arrlat,route_type,dep_station,arr_station");

        			
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
        			
        			Double deplon = null;
        			Double deplat = null;
        			Date depTime = null;
        			String startTime = null;
        			
        			while((line=br.readLine())!=null){
        				long t1 = System.currentTimeMillis();
        				String tokens[] = line.split(",",-1);
        				if(tokens[5].equals("")){
        					continue;
        				}
        				String pid = tokens[1];
        				String _startTime = tokens[2];
        				String _endTime = tokens[3];
        				int purpose_code = Integer.valueOf(tokens[5]);
        				double minLat = Double.valueOf(tokens[7]);
        				double minLon = Double.valueOf(tokens[8]);
        				double maxLat = Double.valueOf(tokens[9]);
        				double maxLon = Double.valueOf(tokens[10]);
        				int transport = Integer.valueOf(tokens[11]);
        							
        			
        				if(transport==-1){
        					startTime = _endTime;
        					depTime = toDate(_endTime);
        					deplon = 0.5*(minLon+maxLon);
        					deplat = 0.5*(minLat+maxLat);
        					continue;
        				}
        				
        				String endTime = _startTime;
        				Date arrTime = toDate(_startTime);
        				String transmode = summarytransport(transport);
        				Double arrlon = 0.5*(minLon+maxLon);
        				Double arrlat = 0.5*(minLat+maxLat);
        		
        				point dep = new point(deplon,deplat);
        				point arr = new point(arrlon,arrlat);
        				
        				Double tripdistance = distance(deplat,deplon,arrlat,arrlon);
        				Double triptime = (arrTime.getTime()-depTime.getTime())/60000d;
        				Double avgspeed = tripdistance/(triptime*60);			
        				
        		
        				
        				boolean matchstation = dep.nearstation(Station)&&arr.nearstation(Station);
        			
        				String log = pid+","+transmode+","+startTime+","+endTime+","+triptime+","+matchstation+","+purpose_code+","+tripdistance+","+avgspeed;
        			
        				startTime = _endTime;
        				depTime = toDate(_endTime);
        				deplon = 0.5*(minLon+maxLon);
        				deplat = 0.5*(minLat+maxLat);
        				
        				bw.write(log);
        				bw.newLine();
      
//        				long t2 = System.currentTimeMillis();
//        				long t3 = (t2 - t1) / 1000;
//        				
//        				System.out.println(String.format("  Time : %s", t3));
        				}
        			
        			br.close();
        		
	        }
	           
	        }
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

