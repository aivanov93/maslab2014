package vision.detector;
import global.Constants;

import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.opencv.core.Point;

import vision.detector.ColorObject.Type;


public class VisionDetector {
	
	HashMap<Type,ColorObject>  objects;
	
	public VisionDetector(){
		 objects=new HashMap<Type,ColorObject>();
	}
	
	public void reset(){
		objects.clear();
	}
	
	/**
	 * Saves the given detected object if needed
	 * Checks if there was a similar object seen and if yes looks if this one is closer
	 * @param object
	 */
	public synchronized void putObject(ColorObject object){
		Type type=object.type();
		double distance=object.distance();
		if (objects.get(type)!=null){
			if (objects.get(type).distance()<distance){
				objects.put(type, object);
			} 
		} else {
			objects.put(type, object);
		}
	}
	
	/**
	 * Analyzes a new ball seen
	 * @param color 
	 * @param dist
	 * @param angle
	 */
	public synchronized void sawBall(Color color, double dist, double angle){
		if (angle>Math.PI) angle-=Math.PI*2;
		Type type;
		if (color==Color.red){
			type=Type.RedBall;
		} else {
			type=Type.GreenBall;
		}	
		putObject(new ColorObject(type, dist,angle));		
	}
		
	public synchronized void sawRectangle(Color color, double dist, double angle){
		if (angle>Math.PI) angle-=Math.PI*2;
		Type type;
		if (color==Color.red){
			type=Type.Silo;
		} else if (color==Color.green) {
			type=Type.Reactor;
		} else {
			type=Type.YellowWall;
		}		
		putObject(new ColorObject(type, dist,angle));	
	}	
	
	public synchronized boolean seesSomething(){
		return !objects.isEmpty();
	}
	
	public synchronized boolean seesBall(){
		return objects.get(Type.RedBall)!=null || objects.get(Type.GreenBall)!=null;
	}
	
	public synchronized boolean seesBigBall(){
		ColorObject bigBall=biggestBall();
		if (bigBall==null){
			return false;
		} else {
			return bigBall.distance()<Constants.siloBallDistance;
		}
		
	}
	
	public synchronized ColorObject biggestBall(){
		ColorObject bigBall=null;
		if (seesRedBall()){
			bigBall=redBall();
		}

		if (seesGreenBall()){
			if (bigBall==null) bigBall=greenBall();
			else if (bigBall.distance()<greenBall().distance())	bigBall=greenBall();
		}
		return bigBall;
	}
	
	public synchronized boolean seesRedBall(){
		return objects.get(Type.RedBall)!=null;
	}
	
	public synchronized boolean seesGreenBall(){
		return objects.get(Type.GreenBall)!=null;
	}
	
	public synchronized boolean seesSilo(){
		return objects.get(Type.Silo)!=null;
	}
	
	public synchronized boolean seesReactor(){
		return objects.get(Type.Reactor)!=null;
	}
	
	public synchronized boolean seesYellowWall(){
		return objects.get(Type.YellowWall)!=null;
	}
	public synchronized ColorObject redBall(){
		return objects.get(Type.RedBall);
	}
	
	public synchronized ColorObject greenBall(){
		return objects.get(Type.GreenBall);
	}

	public synchronized ColorObject silo(){
		return objects.get(Type.Silo);
	}

	public synchronized ColorObject reactor(){
		return objects.get(Type.Reactor);
	}
	
	public synchronized ColorObject yellowWall(){
		return objects.get(Type.YellowWall);
	}
}
