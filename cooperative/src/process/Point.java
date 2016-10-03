package process;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.LongRange;

public class Point implements Comparable<Point>{
	Double lat;
	Double lon;
	Boolean isStay;
	Date time=null;     //timestamp
	Date _start;       //starttime
	Date _end;
	
	/** WGS84Ç≈ÇÃê‘ìπîºåa				*/	private static final double	WGS84_EQUATOR_RADIUS = 6378137;
	/** WGS84Ç≈ÇÃã…îºåa				*/	private static final double WGS84_POLAR_RADIUS   = 6356752.314245;
	/** WGS84Ç≈ÇÃó£êSó¶ÇÃÇQèÊ			*/	private static final double WGS84_ECCENTRICITY_2 = (WGS84_EQUATOR_RADIUS * WGS84_EQUATOR_RADIUS - 
																							WGS84_POLAR_RADIUS   * WGS84_POLAR_RADIUS  ) 
																							/ 
																							(WGS84_EQUATOR_RADIUS*WGS84_EQUATOR_RADIUS); 
	
	public Point(Date start, Date end, double lat, double lon){
		this.lat=lat;
		this.lon=lon;
		setTimeSpan(start, end);
	}
	
	public Point(double lat, double lon){
		this.lat=lat;
		this.lon=lon;
	}
	
	public Point(Double _lat,Double _lon,Date _time){
		this.lat=_lat;
		this.lon=_lon;
		setTimeStamp(_time);
	}
	
	public void setTimeSpan(Date start, Date end) {
		_start = start;
		_end   = end;
	}
	
	public void setTimeStamp(Date time) {
		setTimeSpan(time, time);
	}
	
	public boolean isTimeStamp() {
		return _start != null && _end != null && _start.equals(_end);
	}
	
	public boolean isTimeSpan() {
		return _start != null && _end != null && !_start.equals(_end);
	}
	
	public Date getTimeStamp() {
		return isTimeStamp() ? _start : null;
	}
	
	public Double getLat(){
		return lat;
	}
	
	public Double getLon(){
		return lon;
	}
	
	public void setstay(){
		this.isStay=true;
	}
	
	public void setmove(){
		this.isStay = false;
	}
	
	
	public Date getStart(){
		return _start;
	}

	public Date getEnd(){
		return _end;
	}
	
	public boolean intersects(Date ts, Date te) {
		return	_start != null && _end != null && ts != null && te != null &&
				new LongRange(_start.getTime(), _end.getTime()).overlapsRange(new LongRange(ts.getTime(), te.getTime()));
	}
	
	public boolean intersects(Date t){
		return _start!=null&&_end!=null&&t!=null&&new LongRange(_start.getTime(), _end.getTime()).containsLong(t.getTime());
	}
	
	public int compareTo(Point p) {
		Date t0 = isTimeStamp()   ? getTimeStamp()   : getStart();
		Date t1 = p.isTimeStamp() ? p.getTimeStamp() : p.getStart();
		
		return t0.compareTo(t1);
	}
	
    public double distance(Point p) {
    	double a  = WGS84_EQUATOR_RADIUS;
		double e2 = WGS84_ECCENTRICITY_2;
		double dy = Math.toRadians(this.lat - p.lat); // p0.getLat()  - p1.getLat());
		double dx = Math.toRadians(this.lon - p.lon); // p0.getLon()  - p1.getLon());
		double cy = Math.toRadians((this.lat + p.lat)/2d); // (p0.getLat() + p1.getLat()) / 2d);
		double m  = a * (1-e2);
		double sc = Math.sin(cy);
		double W  = Math.sqrt(1d-e2*sc*sc);
		double M  = m/(W*W*W);
		double N  = a/W;
		
		double ym = dy*M;
		double xn = dx*N*Math.cos(cy);
		
		return Math.sqrt(ym*ym + xn*xn);
    }
    
    public String toString() {
    	if( isTimeSpan() ) {
    		return String.format("%s - %s (%f,%f)", _start, _end, getLon(), getLat());
    	}
    	else {
    		return String.format("%s (%f,%f)", _start, getLon(), getLat());
    	}
    }

	public boolean isPolygonContainsPoint(List<Point> mPoints) {
		int nCross = 0;  
        for (int i = 0; i < mPoints.size(); i++) {  
            Point p1 = mPoints.get(i);  
            Point p2 = mPoints.get((i + 1) % mPoints.size());  
          
            if (p1.getLon() == p2.getLat()) 
                continue;  
            
            if (this.getLon() < Math.min(p1.getLon(), p2.getLon()))  
                continue;  
            
            if (this.getLon() >= Math.max(p1.getLon(), p2.getLon()))  
                continue;  
        
            double x = (double) (this.getLon() - p1.getLon()) * (double) (p2.getLat() - p1.getLat())  
                    / (double) (p2.getLon() - p1.getLon()) + p1.getLat();  
            if (x > this.getLat())  
                nCross++; 
        }  
      
        return (nCross % 2 == 1);  
	}



	
}
