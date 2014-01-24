package vision.detector;

import game.StateMachine.WallSide;
import global.Constants;

import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import math.geom2d.Point2D;
import math.geom2d.line.Line2D;
import math.geom2d.line.Ray2D;

import org.opencv.core.Point;

import vision.LeastSquares;
import vision.Linearization;
import vision.NewImageProcessor;
import vision.detector.ColorObject.Type;

public class VisionDetector {
	
	HashMap<Type, ColorObject> objects;
	int[] wallCoordinates=new int[Constants.picWidth];
	int[] dWall=new int[Constants.picWidth];
	Line2D wall=null;
	Point2D[] wallPoints=new Point2D[Constants.picWidth];
	
	public VisionDetector() {
		objects = new HashMap<Type, ColorObject>();
	}

	public VisionDetector(HashMap<Type, ColorObject> objects) {
		this.objects =(HashMap<Type, ColorObject>)objects.clone();
	   
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
	public  void sawBall(Color color, double x, double y) {
		Point2D transform=new Point2D();//Linearization.linearize((int)x, (int)y);
		Type type;
		if (color == Color.red) {
			type = Type.RedBall;
		} else {
			type = Type.GreenBall;
		}
		double dist=Math.sqrt(transform.x()*transform.x()+transform.y()*transform.y());
		double angle=Math.atan2(transform.y(), transform.x());
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
		wallCoordinates=wallHeight;
		List<Integer> corners=new ArrayList<Integer>();
		for (int i=3; i<wallHeight.length;i++){
			if (Math.abs(wallHeight[i]-wallHeight[i-2])<Math.abs(wallHeight[i-1]-wallHeight[i])){
				wallHeight[i-1]=(wallHeight[i]+wallHeight[i-2])/2;
			}
		}
		for (int i=5; i<wallHeight.length-5;i++){
			dWall[i]=wallHeight[i]-wallHeight[i-1];
		}
		for (int i=2; i<wallHeight.length;i++){
			if (Math.abs(dWall[i]-dWall[i-1])>4){
				corners.add(i-1);
			}
		}
		
		wall=findGoodPixels(4, 40);		
		makeWall(wall,Type.LeftWall);
		wall=findGoodPixels(600, 640);
		
		makeWall(wall,Type.RightWall);
		wall=findGoodPixels(200, 300);
		
		makeWall(wall,Type.CenterWall);
		System.out.println(corners);
	}
	
	public void makeWall(Line2D wall, Type type){
		Point2D origin=new Point2D(0,0);
		double angle=wall.horizontalAngle();
		Ray2D perpendicular1=new Ray2D(0.0,0.0,angle+Math.PI/2);
		Ray2D perpendicular2=new Ray2D(0.0,0.0,angle-Math.PI/2);
		Point2D intersection=perpendicular1.intersection(wall);
		if (intersection==null){
			intersection=perpendicular2.intersection(wall);
		}
		ColorObject wallObj=new ColorObject(type, intersection.distance(0, 0), angle);
		objects.put(type, wallObj);
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
	
	/**
	 * Finds pixels that could belong to a line
	 * @param start
	 * @param end
	 * @return
	 */
	public Line2D findGoodPixels(int start, int end){
		List<Integer> goodWallPixels=new ArrayList<Integer>();
		for (int i=start; i<end; i++){
			if (wallCoordinates[i]>0 && dWall[i]-dWall[i-1]<3){
				goodWallPixels.add(i);
			}
			if (dWall[i]-dWall[i-1]>30 && dWall[i+1]-dWall[i]<4 && dWall[i+2]-dWall[i+1]<4){
				break;
			}
		}
		List<Point2D> pointsToFit=new ArrayList<Point2D>();
		for (int i=0; i<goodWallPixels.size(); i++){
			pointsToFit.add(wallPoints[goodWallPixels.get(i)]);
		}
		if (goodWallPixels.size()>15){
			Line2D wall=LeastSquares.fitLine(pointsToFit);
			return wall;
		}
		return null;
	}
	
	public boolean seesWallLeft(){
		return objects.get(Type.LeftWall) != null;
	}
	
	public boolean seesWallRight(){
		return objects.get(Type.RightWall) != null;
	}
	
	public boolean seesWallCenter(){
		return objects.get(Type.CenterWall) != null;
	}
	
	
	
	public ColorObject leftWall(){
		return objects.get(Type.LeftWall);
	}
	
	public ColorObject rightWall(){
		return objects.get(Type.RightWall);
	}
	
	public ColorObject centerWall(){
		return objects.get(Type.CenterWall);
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
