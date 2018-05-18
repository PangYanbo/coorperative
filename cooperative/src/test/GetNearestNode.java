package test;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jp.ac.ut.csis.pflow.dbi.PgLoader;
import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.Mesh;

public class GetNearestNode {

	public static void main(String[] args){
		File infile = new File ("D:/training data/pflow.csv");
		File outfile = new File ("C:/Users/PangYanbo/Desktop/simulator/a1.csv"); 

		PgLoader pgLoader = new PgLoader(
				"localhost",
				5432,
				"postgres",
				"pyb1989327",
				"simulator",
				"UTF8");
		
		Connection con = pgLoader.getConnection();
		try{
			long startTime = System.currentTimeMillis();
			BufferedReader br = new BufferedReader(new FileReader(infile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
			int count = 0;
			String line = null;
			while ((line = br.readLine()) != null){
				count++;
				if(count%10000==0){
					System.out.println(count);
				}
				String[] tokens = line.split(",");
				int id = Integer.valueOf(tokens[0]);
				double lon = Double.valueOf(tokens[8]);
				double lat = Double.valueOf(tokens[9]);
				String nodeid = getNode(con, new LonLat(lon, lat));
				if (nodeid != null){
					bw.write(String.format("%d,%s", id, nodeid));
					bw.newLine();
				}
			}
			br.close();
			bw.close();
			long endTime = System.currentTimeMillis();
			System.out.println("finished reading one line: "+(endTime-startTime)+"ms");
		}
		catch(FileNotFoundException xx) {
			System.out.println("File not found 1");
		}
		catch(IOException xxx) {
			System.out.println(xxx);
		}
		pgLoader.close();
		System.out.println("end");
	}


	private static String generateSql(LonLat in, double Buffer) {
		// TODO BBOX size should be modifiable ////////////
		double minx  =  in.getLon()-Buffer;
		double miny  =  in.getLat()-Buffer;
		double maxx  =  in.getLon()+Buffer;
		double maxy  =  in.getLat()+Buffer;
		String point =  String.format("ST_SetSRID(ST_MakePoint(%f,%f),4326)",in.getLon(),in.getLat());
		String bbox  =  String.format("ST_SetSRID(ST_MakeBox2D(ST_MakePoint(%f,%f),ST_MakePoint(%f,%f)),4326) ",minx,miny,maxx,maxy);
		String sql   =  String.format("SELECT id, ST_Distance_Sphere(geom,%s) as dist ",point) +
				String.format("FROM data2503.drmallroad_node ") +
				String.format("WHERE ST_Intersects(geom,%s) ",bbox) +
				String.format("ORDER BY dist LIMIT 1;");
		return sql;
	}

	public static String getNode(Connection con, LonLat point) {
		Statement stmt = null;
		ResultSet res  = null;
		String node = null;
		try {
			con.setAutoCommit(true);
			stmt = con.createStatement();
			String sql = generateSql(point, Mesh.LAT_HEIGHT_MESH6.doubleValue());
			res        = stmt.executeQuery(sql);
			if( res.next() ) {
				node = res.getString("id");
			}
			else{
				res.close();
				String sql2 = generateSql(point,(Mesh.LAT_HEIGHT_MESH5.doubleValue()));
				res = stmt.executeQuery(sql2);
				if(res.next()){
					node = res.getString("id");
				}
				else{
					res.close();
					String sql3 = generateSql(point,(Mesh.LAT_HEIGHT_MESH3.doubleValue()));
					res = stmt.executeQuery(sql3);
					if(res.next()){
						node = res.getString("id");
					}
					else{
						res.close();
						String sql5 = generateSql(point,(Mesh.LAT_HEIGHT_MESH2.doubleValue()*2));
						res = stmt.executeQuery(sql5);
						if(res.next()){
							node = res.getString("id");
						}
						else{
							res.close();
							String sql4 = generateSql(point,(Mesh.LAT_HEIGHT_MESH1.doubleValue()*5));
							res = stmt.executeQuery(sql4);
							if(res.next()){
								node = res.getString("id");
							}
						}
					}
				}
			}
			res.close();
		}
		catch(SQLException exp) { exp.printStackTrace(); }
		finally {
			try { if( stmt != null ) { stmt.close(); } }
			catch(SQLException exp) { exp.printStackTrace(); }
		}
		return node;
	}

}
