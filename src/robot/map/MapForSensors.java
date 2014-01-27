package robot.map;

import global.Constants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import robot.sensors.RangeSensors;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

/**
 * Represents a map of the world and is used for calculating the real readings
 * from a certain position
 * 
 */
public class MapForSensors {
	RangeSensors sensors;

	private List<LineSegment2D> walls;

	Circle2D position;
	Point2D head, center;
	Ray2D direction;
	double robotAngle;

	public MapForSensors(List<LineSegment2D> walls, RangeSensors sensors) {
		this.walls = walls;
		this.sensors = sensors;
	}

	/**
	 * Calculates the main parts of the robot based on position
	 */
	public void updatePosition() {

		// the head of the robot
		center = position.center();		
		direction = new Ray2D(center, robotAngle);
		head = position.intersections(direction).iterator().next();		
	}

	/**
	 * finds the distance from the source to the closest wall of the maze in the
	 * direction of the ray
	 * 
	 * @param source
	 * @param ray
	 * @return the distance to the closes wall
	 */

	private double mazeIntersect(Point2D source, Ray2D ray) {

		double minDistance = Double.MAX_VALUE;
		Point2D closestPoint = new Point2D();

		for (LineSegment2D wall : walls) {
			Point2D point = ray.intersection(wall);
			if (point != null) {
				if (source.distance(point) < minDistance)
					closestPoint = point;
				minDistance = source.distance(point);
			}
		}

		return source.distance(closestPoint);
	}
	
	 public static double gaussian(double x) {
	        return Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
	    }
	
	 public static double gaussian(double x, double mu, double sigma) {
	        return gaussian((x - mu) / sigma) / sigma;
	  }

	/**
	 * Calculates the probability of the robot being at this position
	 * Uses the current sonars readings and the real readings based on position
	 * 
	 * @param x current x
	 * @param y current y
	 * @param angle current angle
	 * @return the probability of being in this position
	 */
	
	public double getProbability(double x, double y, double angle) {

		double probability = 1.0;

		position = new Circle2D(new Point2D(x, y),
				Constants.robotRadius);
		updatePosition();
		
		int uncertainty=0;
		// calculate and update readings
		for (int i = 0; i < Constants.numberOfIRs; i++) {
			Ray2D direction=new Ray2D(position.center(), Constants.irDirections.get(i)+angle);
			Point2D source=position.intersections(direction).iterator().next();
			double predictedReading = this.mazeIntersect(source,
					direction);
			double worldReading=sensors.get(i);
			if (worldReading==Constants.maxIRreading+1 && predictedReading==Constants.maxIRreading+1) uncertainty++;
			if (worldReading==1 && predictedReading==1) uncertainty++;
			double error = gaussian( worldReading, predictedReading, 1);
			//System.out.println("probability "+error+ "for readings "+worldReading+ " "+predictedReading);
			probability *=  error;
		
		}
		if (uncertainty>3) probability=0.000;
		return probability;

	}
	
	public void draw(Graphics2D g){

		g.setStroke(new BasicStroke(3));
		g.setColor(Color.black);
		for (LineSegment2D wall:walls){
			wall.draw(g);
		}
	}

}
