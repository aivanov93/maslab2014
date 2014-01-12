package fakerobot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import robot.main.Robot;

import vision.detector.VisionDetector;

import math.geom2d.conic.Circle2D;
import math.geom2d.line.Line2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.polygon.Polyline2D;
import math.geom2d.polygon.Rectangle2D;
import math.geom2d.Angle2D;
import math.geom2d.Point2D;

/**
 * Represents the playing map
 */
public class Map {
	Polyline2D maze;
	List<Circle2D> redBalls = new ArrayList<Circle2D>();
	List<Circle2D> greenBalls = new ArrayList<Circle2D>();
	List<LineSegment2D> reactors = new ArrayList<LineSegment2D>();
	List<LineSegment2D> yellowWalls = new ArrayList<LineSegment2D>();
	LineSegment2D silo;

	// useful things
	LineSegment2D frontSide;
	StraightLine2D slaveL;
	Ray2D direction, cameraRay;
	Point2D head;
	double robotAngle, cameraAngle;
	Iterator<Point2D> points;

	/**
	 * Constructor
	 * 
	 * @param mazeXcoords
	 *            coordinates of maze vertices
	 * @param mazeYcoords
	 * @param redBallXcoords
	 *            coordinates of red balls
	 * @param redBallYcoords
	 * @param greenBallXcoords
	 *            coordinates of green balls
	 * @param greenBallYcoords
	 * @param ballRadius
	 *            ball radius
	 * @param reactors
	 *            a list of indexes of line segments in the maze that represent
	 *            reactors
	 * @param silo
	 *            index of the line segment that represents the silo
	 * @param yellowWalls
	 *            the same thing
	 */
	public Map(double[] mazeXcoords, double[] mazeYcoords,
			double[] redBallXcoords, double[] redBallYcoords,
			double[] greenBallXcoords, double[] greenBallYcoords,
			double ballRadius, int[] reactors, int silo, int[] yellowWalls) {

		maze = new Polyline2D(mazeXcoords, mazeYcoords);
		for (int i = 0; i < redBallXcoords.length; i++) {
			redBalls.add(new Circle2D(redBallXcoords[i], redBallYcoords[i],
					ballRadius));
		}
		for (int i = 0; i < greenBallXcoords.length; i++) {
			greenBalls.add(new Circle2D(greenBallXcoords[i],
					greenBallYcoords[i], ballRadius));
		}
		for (int i = 0; i < reactors.length; i++) {
			this.reactors.add(maze.edge(reactors[i]));
		}
		for (int i = 0; i < yellowWalls.length; i++) {
			this.reactors.add(maze.edge(reactors[i]));
		}
		this.silo = maze.edge(silo);
	}
	
	//finds the distance to the closest intersection of the maze with the ray
	double mazeIntersect(Ray2D ray){
		points=maze.intersections(ray).iterator();
		Point2D source=ray.firstPoint();
		double minDistance=Double.MAX_VALUE;
		Point2D closestPoint=new Point2D();
		for (Point2D point=points.next(); point!=null; point=points.next()){
			if (source.distance(point)<minDistance) closestPoint=point;
		}
		
		return source.distance(closestPoint);
	}

	/**
	 * Simulates the vision processing
	 * 
	 * @param detector
	 *            the result of the vision
	 * @param robot
	 *            our robot
	 */
	public void checkColors(VisionDetector detector, FakeRobot robot) {
		// get the front side of the robot
		frontSide = robot.locate().edges().iterator().next();

		// direction's line is the median of the front side
		slaveL = frontSide.getMedian();
		robotAngle = slaveL.horizontalAngle();

		// the head of the robot
		head = frontSide.intersection(slaveL);

		// we find the direction as a ray
		direction = new Ray2D(head, robotAngle);

		// now with small angle steps we look if we see something
		for (int k = -100; k < 100; k++) {
			
			cameraAngle=robotAngle+robot.angleOfView()/100.0*k;
			cameraRay=new Ray2D(head, Angle2D.formatAngle(cameraAngle));
			
			double distanceToMazeWall=this.mazeIntersect(cameraRay);
			for (Circle2D redBall:redBalls){
				Point2D ballIntersection=redBall.intersections(cameraRay).iterator().next();
				double distanceToBall=ballIntersection.distance(head);
				if (distanceToBall<distanceToMazeWall){
					detector.sawBall(Color.red, distanceToBall, cameraAngle);
				}
			}
			
			for (Circle2D greenBall:greenBalls){
				Point2D ballIntersection=greenBall.intersections(cameraRay).iterator().next();
				double distanceToBall=ballIntersection.distance(head);
				if (distanceToBall<distanceToMazeWall){
					detector.sawBall(Color.green, distanceToBall, cameraAngle);
				}
			}
		}
	}

}
