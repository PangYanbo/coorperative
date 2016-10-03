package process;

import java.util.ArrayList;
import java.util.List;

public class Trip{
	Point pstart;
	Point pend;
	List<Point>trajectory = new ArrayList<Point>();
	
	public Trip(Point _pstart,Point _pend,List<Point>_trajectory){
		this.pstart=_pstart;
		this.pend=_pend;
		this.trajectory=_trajectory;
	}
	
	public Trip(List<Point>_trajectory){
//		this.pstart = _trajectory.get(0);
//		this.pend = _trajectory.get(_trajectory.size()-1);
		this.trajectory = _trajectory; 
	}
	
	public Point getOrigin(){
		return this.trajectory.get(0);
	}
	
	public Point getDestination(){
		return this.trajectory.get(this.trajectory.size()-1);
	}
	
	
	public double getDistance(){
		
			return this.getOrigin().distance(this.getDestination());
	
	}
	
	public double getTriptime(){
		return  (this.getDestination().getTimeStamp().getTime()-this.getOrigin().getTimeStamp().getTime())/(60*1000);
	}
	
	public double getSpeed(){
		return this.getDistance()/(this.getTriptime()*60);
	}
	
	public List<Point> getTrajectory(){
		return this.trajectory;
	}
	
	public String toString() {
		 return String.format("[Trajectory=%s]",getTrajectory());
	    }
}