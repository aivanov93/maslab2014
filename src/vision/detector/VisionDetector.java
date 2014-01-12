package vision.detector;
import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.opencv.core.Point;

import vision.detector.ColorObject.Type;


public class VisionDetector {
	
	HashMap<Color,Boolean>  sawBall=new HashMap<Color,Boolean>();
	HashMap<Color,Boolean>  sawLabel=new HashMap<Color,Boolean>();
	HashMap<Color,SimpleEntry<Double,Double>>  ball=new HashMap<Color,SimpleEntry<Double,Double>>();
	HashMap<Color,SimpleEntry<Double,Double>>  label=new HashMap<Color,SimpleEntry<Double,Double>>();
	
	//point in polar coordinates!!
	HashMap<Color,Point>  ballCoordinates=new HashMap<Color,Point>();
	HashMap<Color,Point>  labelCoordinates=new HashMap<Color,Point>();
	
	HashMap<Type,ColorObject>  objects;
	
	public VisionDetector(){
		 objects=new HashMap<Type,ColorObject>();
	}
	
	public void reset(){
		objects.clear();
	}
	
	/**
	 * check if there was a similar object seen and if yes see if this one is closer
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
	
	public synchronized void sawBall(Color color, double dist, double angle){
		Type type;
		if (color==Color.red){
			type=Type.RedBall;
		} else {
			type=Type.GreenBall;
		}	
		putObject(new ColorObject(type, dist,angle));		
	}
		
	public synchronized void sawRectangle(Color color, double dist, double angle){
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
}
