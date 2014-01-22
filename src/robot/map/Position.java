package robot.map;

import global.Constants;

import java.util.Random;

import math.geom2d.Angle2D;
import math.geom2d.Point2D;
import math.geom2d.line.Ray2D;

import robot.sensors.Odometry;
import straightedge.geom.KPoint;

public class Position {

	private Random sampler=new Random();
	private double weight;
	private double x;
	private double y;
	private double angle;
	
	public Position(double x, double y, double angle, double weight){
		this.x=x;
		this.y=y;
		this.angle=angle;
		this.weight=weight;
	}
	
	public double x(){
		return x;
	}
	
	public double y(){
		return y;
	}
	
	public double angle(){
		return angle;
	}
	
	public double weight() {
		return weight;
	}
	
	public void motionUpdate(Odometry odometry){
		x=x+odometry.xMoved()+sampler.nextGaussian()*odometry.xMoved()*0.6;
		y=y+odometry.yMoved()+sampler.nextGaussian()*odometry.yMoved()*0.6;
		angle=angle+odometry.angleMoved()+sampler.nextGaussian()*odometry.angleMoved()*0.6;
	}
	
	public void setWeight(double weight){
		this.weight=weight;
	}
	
	public boolean isClose(KPoint point){
		double x1=point.x, y1=point.y;
		return Math.sqrt((x1-x)*(x1-x)+(y1-y)*(y1-y))<Constants.closeEnough;
	}
	
	public double distance(KPoint point){
		double x1=point.x, y1=point.y;
		return Math.sqrt((x1-x)*(x1-x)+(y1-y)*(y1-y));
	}
	
	public double angle(KPoint point){
		Point2D source=new Point2D(x, y);
		Ray2D ray=new Ray2D(source, new Point2D(point.x, point.y));
		Ray2D ray2d=new Ray2D(x,y,angle);
		double angle1=Angle2D.angle(ray2d, ray);
		return Constants.formatAngle(angle1);
	}
	
	public Point2D toPoint2D(){
		return new Point2D(x, y);
	}
	
	public Ray2D toRay2D(){
		return new Ray2D(x, y, angle);
	}
	
	public Position clone(){
		return new Position(x, y, angle, weight);
	}
}
