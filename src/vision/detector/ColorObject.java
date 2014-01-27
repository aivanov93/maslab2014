package vision.detector;

import java.awt.Color;

import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;

public class ColorObject {

	public static enum Type {
		RedBall, GreenBall, Silo, Reactor, YellowWall, LeftWall, RightWall, CenterWall,
	};
	
	private StraightLine2D line;

	private double distance;
	private double angle;
	private double x;
	private double y;
	private Type type;
	boolean left;

	public ColorObject(Type type, double dist, double angle, double x, double y) {
		this.distance = dist;
		this.angle = angle;
		this.type = type;
		this.x=x;
		this.y=y;
	}
	
	public ColorObject(Type type, double dist, double angle, double x, double y, boolean left) {
		this.distance = dist;
		this.angle = angle;
		this.type = type;
		this.left=left;
		this.x=x;
		this.y=y;
	}

	public double x(){
		return x;
	}
	
	public double y(){
		return y;
	}
	
	public double carrotAngle(){
		return new Ray2D(0,0,y,x).horizontalAngle();
	}
	public double distance() {
		return distance;
	}

	public double angle() {
		return angle;
	}

	public Type type() {
		return type;
	}
	
	public boolean onLeft(){
		return left;
	}
	
	public StraightLine2D line(){
		return line;
	}

}
