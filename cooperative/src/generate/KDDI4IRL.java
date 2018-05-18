package generate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import MobilityAnalyser.Tools;
import SigSpatial.GpsProcess;
import extract.ExtractStayPoint;
import extract.ExtractTrips;
import jp.ac.ut.csis.pflow.geom.Mesh;
import jp.ac.ut.csis.pflow.geom.STPoint;
import jp.ac.ut.csis.pflow.geom.Trip;
import process.GeometryChecker;
import readfiles.ReadBufferPoint;
import readfiles.ReadGPSFile;

public class KDDI4IRL {
	
	public static void getFiles(String path) {  
		  
        File file = new File(path);  
        if (file.exists()) {  
            File[] files = file.listFiles();  
            if (files.length == 0) {  
                System.out.println("1");  
                return;  
            } else {  
                for (File file2 : files) {  
                    if (file2.isDirectory()) {    
                        getFiles(file2.getAbsolutePath());  
                    } else if(filenames.contains(file2.getName())){
                    	filelist.add(file2.getAbsolutePath()); 
                    }  
                }  
            }  
        } else {  
            System.out.println("4");  
        }  
    }  
	
	public static void main(String[] args) throws ParseException, IOException, Exception{
		
		File   railbuffer  = new File("C:/Users/PangYanbo/Desktop/Tokyo/Rail/");
		String filepath = "D://training data//KDDI//#201111.CDR-data//";
		getFiles(filepath);
		
		Scanner in = new Scanner(System.in);
		
		System.out.println("Type in the time threhold in secs");
		Double timeThres = Double.valueOf(in.nextLine());
		System.out.println("Type in the distance threhold in meters");
		Double distThres = Double.valueOf(in.nextLine());
		
		for(String filename:filelist){
			HashMap<String,ArrayList<STPoint>>id_points = new HashMap<String,ArrayList<STPoint>>();
			HashMap<String,ArrayList<Trip>>id_trips = new HashMap<String,ArrayList<Trip>>();
			HashMap<String,ArrayList<STPoint>>id_sps = new HashMap<String,ArrayList<STPoint>>();
			ArrayList<STPoint>sps = new ArrayList<STPoint>();
			
			System.out.println(filename.substring(0, filename.length()-4));
			
			File gpsfile = new File(filename);
			File withbuffer = new File(filename.substring(0, filename.length()-4)+"withbuffer.csv");
			ReadGPSFile.ReadKDDIGPS(id_points,gpsfile);
			writeout(withbuffer,railbuffer,id_points);
			
			id_points = new HashMap<String,ArrayList<STPoint>>();
//			File gpsfile = new File(withbuffer);
			ReadBufferPoint.ReadBufferPoint(id_points, withbuffer);
			
			for(String id:id_points.keySet()){
				sps = ExtractStayPoint.StayPointDetection(timeThres,distThres,id_points.get(id));
				ArrayList<Trip>trips = new ArrayList<Trip>();
				trips = ExtractTrips.SplitTrips(id_points.get(id),sps);
				id_trips.put(id,trips);
				id_sps.put(id, sps);
				System.out.println(sps);
			}
			
			
			
			String path = filename.substring(0, filename.length()-4)+"slot.csv";
			System.out.println(path);
			File out = new File(path);
			GenerateTrainingData.generate(out,id_sps,id_trips);
		
		}
	}
	
	public static void writeout(File out, File shpdir, HashMap<String,ArrayList<STPoint>>id_points) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		GeometryChecker inst = new GeometryChecker(shpdir);
		long startTime = System.currentTimeMillis();
		for(String id: id_points.keySet()){
			for(int i =0; i<id_points.get(id).size();i++){
				STPoint p = id_points.get(id).get(i);
				bw.write(id+","+SDF_TS2.format(p.getTimeStamp())+","+p.getLon()+","+p.getLat()+","+inst.checkOverlap(p.getLon(), p.getLat()));
				bw.newLine();
			}
		}
		bw.close();
		long endTime = System.currentTimeMillis();
		System.out.println("finished writing out, the time is: "+(endTime-startTime)+"ms");
	}
	
	private static ArrayList<String>filelist = new ArrayList<String>();
	
	private static ArrayList<String> filenames = new ArrayList<String>(Arrays.asList("1.csv","2.csv","3.csv","4.csv","5.csv","6.csv","7.csv","8.csv","9.csv","10.csv","11.csv","12.csv","13.csv","14.csv","15.csv","16.csv","17.csv","18.csv","19.csv","20.csv","21.csv",
			"22.csv","23.csv","24.csv","25.csv","26.csv","27.csv","28.csv","29.csv","30.csv","31.csv"));  
	
	
	public static int ConvertToTimeSlot(Date t){
		int secs = Tools.converttoSecs(SDF_TS.format(t));//interval 30min
		return secs/1800;
	}
	
	protected static final SimpleDateFormat SDF_TS  = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//change time format
	protected static final SimpleDateFormat SDF_TS3 = new SimpleDateFormat("dd");//change time format

}
