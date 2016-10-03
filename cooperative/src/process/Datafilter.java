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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;


public class Datafilter {

	private static Date toDate(String str){
		try {
			return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Enter the date");
		Scanner in = new Scanner(System.in);
		String day = in.nextLine();

		Map<String,Agent>userlog = new LinkedHashMap<String,Agent>();
		
		String filepath = "/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+".csv";;
		try {
			// open file reader
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/t-iho/grid/0/tmp/ktsubouc/gps_"+day+"filter.csv"));
			BufferedReader br2 = new BufferedReader(new FileReader("input-data/ring.csv"));
			String line2 = null;
			List<Point>boundaries = new ArrayList<Point>();
			
			while((line2=br2.readLine())!=null){
				String tokens[] = line2.split(",");
				double lon = Double.parseDouble(tokens[0]);
				double lat = Double.parseDouble(tokens[1]);
				boundaries.add(new Point(lat,lon));
			}
			br2.close();
			
			// remove header line
			String line = br.readLine();

//			List<Agent>agents = new ArrayList<Agent>();
			List<Point>records = null;
//			String preid = null;	
			int countid=0;
		
			while( (line=br.readLine())!=null){ 
			
				String[] tokens  = line.split("	",-1);	// split line with comma "	"
//				for(int i=0;i<tokens.length;i++){
//					System.out.println(tokens[i]);
//				}

				if(tokens[0].length()!=0&&tokens[0]!=null&&tokens[0]!="null"){
					String uid = tokens[0];
					String did = tokens[1];
					Double lat = Double.parseDouble(tokens[2]);
					Double lon = Double.parseDouble(tokens[3]);
					Date date = toDate(tokens[4]);
					Point points = new Point(lat,lon,date);
				
			//		System.out.println(uid+","+did+","+lat+","+lon+","+date+","+points.lat+","+points.lon);

					//Tokyo prefecture GPS
					if(points.isPolygonContainsPoint(boundaries)){
						if(!userlog.containsKey(did)){
							countid++;
						    records = new ArrayList<Point>();
							userlog.put(did, new Agent(uid,did,records));
						}
	//					records.add(points);
			//avoid the same point
						if(!userlog.get(did).record.contains(date))
						{
							userlog.get(did).record.add(points);
						}					
	//					preid=did;
			}
				}
			}
		//output sample
			
			int[] countLogs = new int[101];
			for(int i=0;i<=100;i++){
				countLogs[i]=0;
			}
			
			TreeSet<String>sortedKey = new TreeSet<String>(userlog.keySet());
				
			
			for(String _did:sortedKey){
				if(userlog.get(_did).record.size()<101)
				{
					countLogs[userlog.get(_did).record.size()]++;
				}
				
				 Comparator<Point> comparator = new Comparator<Point>(){  
			            public int compare(Point p1, Point p2) { 
			                    return p1.compareTo(p2);  
			                }  
				 };
				 Collections.sort(userlog.get(_did).record,comparator);
				 
				for(Point points:userlog.get(_did).record){
						//if(users.record.size()>=20){
							String tripline= userlog.get(_did).Uid+","
											+userlog.get(_did).Did+","
											+points.lat+","
											+points.lon+","
											+points.getTimeStamp();
					//		System.out.println(tripline);
							bw.write(tripline);
							bw.newLine();
					//	}
					}
									
//					for(Point point:users.record){
//						String idline = users.Uid+","+users.Did+","+point.time+","+point.lat+","+point.lon;
//						bw.write(idline);
//						bw.newLine();
//					//	System.out.println(id+","+time+","+log.get(time));
//					}	
				}
		//	}
		
			
			// close file reader
			br.close();
			bw.close();
			// statics
			System.out.println("Counts of Agents:"+userlog.size());
			System.out.println("Counts of IDs:"+countid);
			for(int i=0;i<101;i++){
				System.out.println(i+":"+countLogs[i]);
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
