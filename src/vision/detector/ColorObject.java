package vision.detector;

import global.Constants;

import java.awt.Color;

import math.geom2d.Point2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;

public class ColorObject {

	public static enum Type {
		RedBall, GreenBall, Silo, Reactor, YellowWall, LeftWall, RightWall, CenterWall,
	};

	private StraightLine2D line;

	private double angleToCenter;
	private double distance;
	private double angle;
	private double x;
	private double y;
	private Point2D alligningCarrot;
	private double xGoal, yGoal, angleGoal;
	private Type type;
	boolean left;

	public ColorObject(Type type, double dist, double angle, double x, double y) {
		this.distance = dist;
		this.angle = angle;
		this.type = type;
		this.x = x;
		this.y = y;
	}

	public ColorObject(Type type, double dist, double angle, double x,
			double y, double angleGoal, double xGoal, double yGoal) {
		this.distance = dist;
		this.angle = angle;
		this.type = type;
		this.x = x;
		this.y = y;
		this.xGoal = xGoal;
		this.yGoal = yGoal;
		this.angleGoal = angleGoal;
	}

	public ColorObject(Type type, double dist, double angle, double x,
			double y, boolean left, StraightLine2D wall) {
		this.distance = dist;
		this.angle = angle;
		this.type = type;
		this.left = left;
		this.x = x;
		this.y = y;
		this.line = wall;
	}

	public double goalX() {
		return xGoal;
	}

	public double goalY() {
		return yGoal;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public double angleToCenter() {
		return Constants.formatAngle(angleGoal);
	}

	public void makeCarrot(double dist, double wallDist) {
		Point2D carrot = Carrot.getCarrot(line, dist, wallDist, left);
		x = carrot.y();
		y = carrot.x();
	}

	public void makeAligningCarrot() {
		double dist;
		double wallDist;
		if (this.type == Type.Reactor) {
			dist = Constants.reactorAlligningOffset;
		} else
			dist = Constants.siloAlligningOffset;
		wallDist = distance * Math.sin(Math.PI/4.5);
		alligningCarrot = Carrot.getAlligningCarrot(type, line,
				new Point2D(x, y), dist, wallDist, left);
	
	}

	public double carrotAngle() {
		return Math.atan2(x, y);
	}
	
	public double carrotX(){
		return alligningCarrot.y();
	}
	
	public double carrotY(){
		return alligningCarrot.x();
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

	public boolean onLeft() {
		return left;
	}

	public StraightLine2D line() {
		return line;
	}

}
