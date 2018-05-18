package process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

public class ForMotif {
	
	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected static final SimpleDateFormat SDF_TS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	public static void main(String[] args){
		String[] days = {"20150508","20150509","20150510","20150511","20150512","20150513","20150514"};
		
	
		
		Map<String,Agent>userlog = new LinkedHashMap<String,Agent>();
		List<Point>records = null;
		
		for(int index =0;index<days.length;index++){
				String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+days[index]+".csv";
				System.out.println(filepath);
			
			
			try {
				// open file reader
				BufferedReader br = new BufferedReader(new FileReader(filepath));
				
				
				// remove header line
				String line = br.readLine();
				
				Date predate = null;
				
				while( (line=br.readLine())!=null){ 
					
					
					String[] tokens  = line.split("	",-1);	// split line with comma "	"
					
					//System.out.println(tokens[0]+","+tokens[1]+","+tokens[2]+","+tokens[3]+","+tokens[4]);
					
					if(tokens[0].length()!=0&&tokens[1].length()!=0&&tokens[2].length()!=0&&tokens[3].length()!=0&&tokens[4].length()!=0){
						String uid = tokens[0];
						if(!uid.equals("null")&&uid!=null){
							String did = tokens[1];
							Double lat = Double.parseDouble(tokens[2]);
							Double lon = Double.parseDouble(tokens[3]);
							Date date = toDate(tokens[4]);
							Point points = new Point(lat,lon,date);
							
							//kanto
							if(!points.inLocation(138.40, 34.90, 140.87, 37.16)){continue;}
								
							if(!userlog.containsKey(uid)){
							    records = new ArrayList<Point>();
								userlog.put(uid, new Agent(uid,did,records));
						//		System.out.println("Create userlog "+uid);
							}
							if(!date.equals(predate))
							{						
								userlog.get(uid).record.add(points);
								}
						
							predate = date;
						//	System.out.println("user "+uid+" added a point");
						}
					}
				}
		 System.out.println("finish reading");
			
		
				
			// close file reader
			br.close();
			// static
			
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + filepath);
		}
		catch(IOException e) {
			System.out.println(e);
		}
		
		}	
		
		int[] stayPoints = new int[201];
		for(int i=0;i<200;i++){
			stayPoints[i]=0;
		}
		
	TreeSet<String>sortedKey = new TreeSet<String>(userlog.keySet());
			 try {
				 BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_201505.csv"));				 
					for(String _uid:sortedKey){
						for(Point point:userlog.get(_uid).record){
				
								String tripline= userlog.get(_uid).Uid+","
												+point.getLat()+","
												+point.getLon()+","
												+SDF_TS.format(point.getTimeStamp());					
								bw.write(tripline);
								bw.newLine();
					//			System.out.println(tripline);
							}			
			            }
						
					bw.close();
					}
			 catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}					
						
		
		
	}

}
