package SigSpatial;

import java.util.Date;

import jp.ac.ut.csis.pflow.geom.STPoint;

public class State {
	
	private Date _date=null;
	private STPoint _point= new STPoint();
	
	public State(Date date,STPoint point){
		_date = date;
		_point = point;
	}
	
	public State(){
		
	}
	
	public Date getTimeStamp(){
		return _date;
	}
	
	public STPoint getLocation(){
		return _point;
	}
}
