package process;

import java.util.ArrayList;
import java.util.List;

public class Agent{
	String Uid;
	String Did;
	List<Point>record= new ArrayList<Point>();
	List<Trip>trips = new ArrayList<>();
	
	public Agent(String Uid,String Did,List<Point>record){
		this.Uid=Uid;
		this.Did=Did;
		this.record=record;
	}
	
	public List<Point> getRecord(){
		return record;
	}
	
	public int counttrips(){
		return trips.size();
	}
}
