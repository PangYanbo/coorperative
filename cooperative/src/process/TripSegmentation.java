package process;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



import java.util.Map;


import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;



import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.math.LongRange;;

public class TripSegmentation {
	private long _windowsize;
	private long _stepsize;
	private double _h1;
	private double _h2;
	private int _N;
	
	private List<Point>_staypoints;
	private List<Trip>_trips;
	
	public TripSegmentation(long window_width,long step_width,double band_width, double conv, int N){
		_windowsize = window_width*60*1000;
		_stepsize = step_width*60*1000;
		_h1 = band_width;
		_h2 = conv;
		_N = N;
		
		_staypoints = null;
		_trips = null;
	}
	
	public TripSegmentation(){
		this(20,5,4000d,1000d,5);
	}
	
	public void segment(List<Point>trac){
		List<TimeSegment>status = checkStatusWithinTimeSegment(trac);
		Map<Point,List<Point>>staycluster  = extractStayCluster(status);
		_staypoints = extractStayPointSequence(staycluster);
		_trips = extractTripSequence(trac, _staypoints);
	}
	
	public void segment(List<Point> trac,List<Point> staypoints) {
		// set stay points ////////////////////////////////
		_staypoints = staypoints;
		// extract trips //////////////////////////////////
		_trips = extractTripSequence(trac,staypoints);
	}
	


	private class TimeSegment{
		private Date _ts;
		private Date _te;
		private boolean _stay;
		private List<Point>_staypoints;
		
		private TimeSegment(Date ts, Date te){
			_ts = ts;
			_te = te;
			_stay = false;
			_staypoints = new ArrayList<Point>();
		}
		
		private boolean within(Date t){
			return _ts.compareTo(t)<0&&t.compareTo(_te)<0;
		}
		
		private boolean before(Date t){
			return _te.before(t);
		}
		
		private void add(Point stay){
			_staypoints.add(stay);
		}
		
		private List<Point> listStayPoints() {
			return _staypoints;
		}
		
		private void setStay(Boolean stay) {
			_stay = stay;
		}
		
		private boolean isStay() {
			return _stay;
		}		
	}
	
	public List<Point> listStayPoints() {
		return _staypoints;
	}
	
	public List<Trip> listTrips() {
		return _trips;
	}
	
	private List<TimeSegment> checkStatusWithinTimeSegment(List<Point> trac){
		if(trac.isEmpty()){
			return  null;
			}
	
		List<TimeSegment>segments = new ArrayList<TimeSegment>();
		LongRange timerange = getTimeRange(trac);
		long ts = timerange.getMinimumLong();
		long te = timerange.getMinimumLong()+_windowsize;
		
		while(te<timerange.getMaximumLong()){
			TimeSegment  segment = new TimeSegment(new Date(ts),new Date(te));
			List<Point> range   = new ArrayList<Point>();
			
			for(Point p:trac) {
				if( segment.within(p.getTimeStamp()) ) { 
					range.add(p);
					}
				if( segment.before(p.getTimeStamp()) ) { 
					break;
					}
			}
			
			if( range.size() >= _N ) {	// clustering requires N or more points. 
				Map<Point,List<Point>> res = invokeMeanShift(range,_h1,_h2);
				segment.setStay(res.size()==1); 
				for(Point p:res.keySet()) { 
					segment.add(p);
					}
			}
			segments.add(segment);
			// increment ////////////////////////
			ts += _stepsize;
			te += _stepsize;
		}
		return segments;	
	}
	
	private List<Point> extractStayPointSequence(Map<Point,List<Point>> staycluster) {
		// sort by time order of mean point
		Map<Point,Point> temp = new TreeMap<Point,Point>();	
		for(Entry<Point,List<Point>> entry:staycluster.entrySet()) {
			Point key  = entry.getKey();
			List<Point> list = entry.getValue();
			if( list.size() >= _N ) {
				key.setTimeSpan(null,null);
				for(Point p:list) { temp.put(p,key);  }
			}
		}
		// determine stay place and intervals /////////////
		List<Point> stays = new ArrayList<Point>();
		Point prev = null;
		Point stay = null;
		for(Entry<Point,Point> entry:temp.entrySet()) {
			Point segment   = entry.getKey();
			Point stayplace = entry.getValue();
//			Date d = getCenterDate(segment.getDtStart(),segment.getDtEnd());
			if( prev == null || !prev.equals(stayplace) ) { 
				stay = new Point(segment.getStart(),segment.getEnd(),stayplace.getLat(),stayplace.getLon());
				stay.setTimeSpan(segment.getStart(), segment.getEnd());
//				stay = new STPoint(d,d,stayplace.getLon(),stayplace.getLat());
				stays.add(stay);
			}
			else {
//				stay.setTimeSpan(stay.getDtStart(),d);
				stay.setTimeSpan(stay.getStart(),segment.getEnd());
			}
			prev = stayplace;
		}
		return stays;
	}

	private List<Trip> extractTripSequence(List<Point> trac,List<Point> staypoints) {
		// result data ////////////////////////////////////a
		List<Trip> trips = new ArrayList<Trip>();
		
		Trip    trip      = null;
		Point prev_stay = null;
		for(Point p:trac) {
			// determine whether status is stay or move ///
			Point curr_stay = null;
			for(Point stay:staypoints) {
				if( stay.intersects(p.getTimeStamp()) )
				{ curr_stay = stay;
					break; }
			}
			// move
			if( curr_stay == null ) {
				if( trip == null ) { 
					trips.add( trip=new Trip(new ArrayList<Point>()) ); 	// new trip
					if( prev_stay != null ) {
						trip.trajectory.add( new Point(prev_stay.getLat(),prev_stay.getLon(),prev_stay.getEnd()) ); 
					}
				}
				trip.trajectory.add(p);
			}
			// stay
			else {
				if( prev_stay != null && !ObjectUtils.equals(prev_stay,curr_stay) ) {
					// case trip points exist between stays
					if( trip != null ) {
						trip.trajectory.add(new Point(curr_stay.getLat(),curr_stay.getLon(),curr_stay.getStart()));
					}
					// case no trip points exist between stays, [caution] there may be OD time inconsistency
					else if ( prev_stay.getEnd().before(curr_stay.getStart()) ) {
						trip = new Trip(new ArrayList<Point>());
						trip.trajectory.add(new Point(prev_stay.getLat(),prev_stay.getLon(),prev_stay.getEnd()));
						trip.trajectory.add(new Point(curr_stay.getLat(),curr_stay.getLon(),curr_stay.getStart()));
						trips.add(trip);
					}
				}
				prev_stay = curr_stay;
				trip = null;
			}
		}		
		// return result //////////////////////////////////
		return trips;
	}
	
	
	public Map<Point,List<Point>>invokeMeanShift(List<Point>data, double h1, double h2){
		Map<Point,List<Point>>result = new Hashtable<Point,List<Point>>();
		int N = data.size();
		for(int i=0;i<N;i++){
			Point mean = new Point(data.get(i).getLat(),data.get(i).getLon(),null);
			while(true){
				double numx = 0d;
				double numy = 0d;
				double din = 0d;
				for(int j=0;j<N;j++){
					Point p = data.get(j);
					double k = mean.distance(p)<=h1?1:0;
					numx += k * p.getLon();
					numy += k * p.getLat();
					din  += k;
				}
				Point m = new Point(numy/din,numx/din,null);
				if( mean.distance(m) < h2 ) { mean = m; break; }
				mean = m;
			}
			List<Point> cluster = null;
			for(Point p:result.keySet()) {
				if( mean.distance(p) < h2 ) { 
					cluster = result.get(p);
					break; }
			}
			if( cluster == null ) {
				cluster = new ArrayList<Point>();
				result.put(mean,cluster);
			}
			cluster.add(data.get(i));
		}
		Comparator<Point> comp = new Comparator<Point>() {
			public int compare(Point a,Point b) {
				Date ts = a.isTimeSpan() ? a.getStart() : a.getTimeStamp();
				Date te = b.isTimeSpan() ? b.getEnd()   : b.getTimeStamp();
				return ts.compareTo(te);
			}
		};
	
		for(Point p:result.keySet()) {
			List<Point> cluster = result.get(p);
			Collections.sort(cluster,comp);
			Point p0 = cluster.get(0);
			Point p1 = cluster.get(cluster.size()-1);
			
			p.setTimeSpan(p0.isTimeSpan()?p0.getStart():p0.getTimeStamp(),p1.isTimeSpan()?p1.getEnd():p1.getTimeStamp());
		}
		return result;
	}
	
	
	private Map<Point,List<Point>> extractStayCluster(List<TimeSegment> segments) {
		// aggregate all mean points from each time segment
		Set<Point> points = new TreeSet<Point>();
		for(TimeSegment segment:segments) {
			if( segment.isStay() ) { points.addAll(segment.listStayPoints()); }
		}
		// extract stay places ////////////////////////////
		return invokeMeanShift(new ArrayList<Point>(points),_h1,_h2);	
	}
	
	private LongRange getTimeRange(List<Point> trac) {
		LongRange range = null;
		if( trac != null && !trac.isEmpty() ) {
			Calendar cal = Calendar.getInstance();
			// start time ///////////////////////
			cal.setTime(trac.get(0).getTimeStamp());
			cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE),0,0,0);
			cal.set(Calendar.MILLISECOND,0);
			Date ts = cal.getTime();
			// end time /////////////////////////
			cal.setTime(trac.get(trac.size()-1).getTimeStamp());
			cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE),23,59,59);
			cal.set(Calendar.MILLISECOND,999);
			Date te = cal.getTime();
			// time range ///////////////////////
			range = new LongRange(ts.getTime(),te.getTime());
		}
		return range;
	}
	
	
}
