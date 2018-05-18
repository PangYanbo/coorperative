package readfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import jp.ac.ut.csis.pflow.geom.STPoint;

public class ReadGPSFile {
	
	public static void ReadKDDIGPS(HashMap<String,ArrayList<STPoint>>id_points, File in) throws ParseException, Exception, IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = br.readLine();
		long startTime = System.currentTimeMillis();
		int count = 0;
		
		while((line=br.readLine())!=null){
			count++;
			if(count%100000==0){
				System.out.println("scanned "+count+" lines");
			}
		
		
			String tokens[] = line.split(",");
			
			String id = tokens[1];
			Double lat = Double.parseDouble(tokens[5]);
			Double lon = Double.parseDouble(tokens[4]);
			
			Date dt = SDF_TS4.parse(tokens[2]);
			STPoint point = new STPoint(dt,lon,lat);
			if(!id_points.containsKey(id)){
				ArrayList<STPoint>points = new ArrayList<STPoint>();
				points.add(point);
				id_points.put(id, points);
			}else{
				id_points.get(id).add(point);
			}				
			Collections.sort(id_points.get(id));
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("finished reading files with "+id_points.size()+" users in tokyo area using" +(endTime-startTime)+"ms");
		br.close();
	}
	
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format
	protected static final SimpleDateFormat SDF_TS4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
