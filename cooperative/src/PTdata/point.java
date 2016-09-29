package PTdata;

import java.util.List;

public class point {
	
	/** WGS84Ç≈ÇÃê‘ìπîºåa				*/	private static final double	WGS84_EQUATOR_RADIUS = 6378137;
	/** WGS84Ç≈ÇÃã…îºåa				*/	private static final double WGS84_POLAR_RADIUS   = 6356752.314245;
	/** WGS84Ç≈ÇÃó£êSó¶ÇÃÇQèÊ			*/	private static final double WGS84_ECCENTRICITY_2 = (WGS84_EQUATOR_RADIUS * WGS84_EQUATOR_RADIUS - 
																							WGS84_POLAR_RADIUS   * WGS84_POLAR_RADIUS  ) 
																							/ 
																							(WGS84_EQUATOR_RADIUS*WGS84_EQUATOR_RADIUS); 
	
	
	
		double lon;
		double lat;
		
		public point(double _lon,double _lat){
			this.lon = _lon;
			this.lat = _lat;
		}
		
		public void setlon(double _lon){
			this.lon=_lon;
		}
		
		public void setlat(double _lat){
			this.lat=_lat;
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
		
		
		public boolean nearstation(List<point>stations){
			boolean nearStation = false;
			for(int i =0;i<=stations.size()-1;i++){
				point P = stations.get(i);
				if(distance(this.lat,this.lon,P.lat,P.lon)<700){
					nearStation = true;
					break;
				}
			}
			return nearStation;
		}

}
