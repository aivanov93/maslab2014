package vision.detector;

import global.Constants;

import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opencv.core.Point;

import vision.detector.ColorObject.Type;

public class VisionDetector {

	HashMap<Type, ColorObject> objects;
	int[] dWall=new int[320];

	public VisionDetector() {
		objects = new HashMap<Type, ColorObject>();
	}

	public VisionDetector(HashMap<Type, ColorObject> objects) {
		this.objects =new HashMap<Type, ColorObject>();
	   
	}
	
	public VisionDetector clone(){
		return new VisionDetector();
	}

	public void reset() {
		objects.clear();
	}

	/**
	 * Saves the given detected object if needed Checks if there was a similar
	 * object seen and if yes looks if this one is closer
	 * 
	 * @param object
	 */
	public void putObject(ColorObject object) {
		Type type = object.type();
		double distance = object.distance();
		if (objects.get(type) != null) {
			if (objects.get(type).distance() < distance) {
				objects.put(type, object);
			}
		} else {
			objects.put(type, object);
		}
	}

	/**
	 * Analyzes a new ball seen
	 * 
	 * @param color
	 * @param dist
	 * @param angle
	 */
	public  void sawBall(Color color, double dist, double angle) {
		if (angle > Math.PI)
			angle -= Math.PI * 2;
		Type type;
		if (color == Color.red) {
			type = Type.RedBall;
		} else {
			type = Type.GreenBall;
		}
		putObject(new ColorObject(type, dist, angle));
	}

	public  void sawRectangle(Color color, double dist, double angle) {
		if (angle > Math.PI)
			angle -= Math.PI * 2;
		Type type;
		if (color == Color.red) {
			type = Type.Silo;
		} else if (color == Color.green) {
			type = Type.Reactor;
		} else {
			type = Type.YellowWall;
		}
		putObject(new ColorObject(type, dist, angle));
	}
	
	
	public int clamp(int i){
		return Math.max(Math.min(i, 319), 0);
	}
	
	public void foundWalls(int[] wallHeight){
		List<Integer> corners=new ArrayList<Integer>();
		for (int i=5; i<wallHeight.length-5;i++){
			dWall[i]=wallHeight[i]-wallHeight[i-1];
		}
		for (int i=2; i<wallHeight.length;i++){
			if (Math.abs(dWall[i]-dWall[i-1])>4){
				corners.add(i-1);
			}
		}
		System.out.println(corners);
	}
	
	/* ********************************
	 * *****Information getters********
	 * ********************************/

	public  boolean seesSomething() {
		return !objects.isEmpty();
	}

	public boolean seesBall() {
		return objects.get(Type.RedBall) != null
				|| objects.get(Type.GreenBall) != null;
	}

	public boolean seesBigBall() {
		ColorObject bigBall = biggestBall();
		if (bigBall == null) {
			return false;
		} else {
			return bigBall.distance() < Constants.siloBallDistance;
		}

	}

	public  ColorObject biggestBall() {
		ColorObject bigBall = null;
		if (seesRedBall()) {
			bigBall = redBall();
		}

		if (seesGreenBall()) {
			if (bigBall == null)
				bigBall = greenBall();
			else if (bigBall.distance() < greenBall().distance())
				bigBall = greenBall();
		}
		return bigBall;
	}

	public  boolean seesRedBall() {
		return objects.get(Type.RedBall) != null;
	}

	public  boolean seesGreenBall() {
		return objects.get(Type.GreenBall) != null;
	}

	public  boolean seesSilo() {
		return objects.get(Type.Silo) != null;
	}

	public boolean seesReactor() {
		return objects.get(Type.Reactor) != null;
	}

	public  boolean seesYellowWall() {
		return objects.get(Type.YellowWall) != null;
	}

	public  ColorObject redBall() {
		return objects.get(Type.RedBall);
	}

	public ColorObject greenBall() {
		return objects.get(Type.GreenBall);
	}

	public  ColorObject silo() {
		return objects.get(Type.Silo);
	}

	public ColorObject reactor() {
		return objects.get(Type.Reactor);
	}

	public ColorObject yellowWall() {
		return objects.get(Type.YellowWall);
	}
}
