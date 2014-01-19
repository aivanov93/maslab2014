package robot.map;

import global.Constants;

import java.util.ArrayList;
import java.util.List;

import robot.sensors.IRSensors;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

public class MapForSensors {
	IRSensors sensors;

	private List<LineSegment2D> walls;
	
	SimplePolygon2D position;
	Point2D head, center;
	Ray2D direction;
	double robotAngle;

	public MapForSensors(List<LineSegment2D> walls, IRSensors sensors) {
		this.walls = walls;
		this.sensors = sensors;
	}

	public void updatePosition() {

		// the head of the robot
		head = Point2D.midPoint(position.vertex(1), position.vertex(2));
		center = position.centroid();

		// we find the direction as a ray
		direction = new Ray2D(center, head);
		robotAngle = direction.horizontalAngle();
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

	public double getProbability(double x, double y, double angle) {
		
		double probability=1.0;
	
		position = Polygons2D.createOrientedRectangle(
				new Point2D(x, y), Constants.robotLength, Constants.robotWidth,
				angle);
		updatePosition();
		// corners
		Point2D vLeftTop = position.vertex(2);
		Point2D vRightTop = position.vertex(1);
		Point2D vRightBottom = position.vertex(0);
		Point2D vLeftBottom = position.vertex(3);
		Point2D vRightMiddle=Point2D.midPoint(vRightTop, vRightBottom);
		Point2D vLeftMiddle=Point2D.midPoint(vLeftBottom, vLeftTop);

		// create rays for each of the sensors
		List<Ray2D> irs = new ArrayList<Ray2D>();
		List<Point2D> sources = new ArrayList<Point2D>();
		irs.add(new Ray2D(center, vLeftTop));
		sources.add(head);
		irs.add(new Ray2D(center, head));
		sources.add(head);
		irs.add(new Ray2D(center, vRightTop));
		sources.add(head);
		irs.add(new Ray2D(center, vRightMiddle));
		sources.add(vRightMiddle);
		irs.add(new Ray2D(head, center));
		sources.add(Point2D.midPoint(vRightBottom, vLeftBottom));
		irs.add(new Ray2D(center, vLeftMiddle));
		sources.add(vLeftMiddle);

		// calculate and update readings
		for (int i = 0; i < irs.size(); i++) {

			double distanceToMazeWall = this.mazeIntersect(sources.get(i),
					irs.get(i));
		//	System.out.println("*******sensors"+distanceToMazeWall+" "+sensors.get(i));
			if (sensors.get(i)>0){
				double error=Math.abs(distanceToMazeWall-sensors.get(i))/sensors.get(i);
				probability*=(1-error);		
			}
		}
		return probability;

	}

}
