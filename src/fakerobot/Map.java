package fakerobot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import vision.detector.VisionDetector;

import math.geom2d.conic.Circle2D;

import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.polygon.Polyline2D;
import math.geom2d.polygon.Rectangle2D;
import math.geom2d.AffineTransform2D;
import math.geom2d.Angle2D;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;

/**
 * Represents the playing map
 */
public class Map {
	private Polyline2D maze;
	private List<Circle2D> redBalls = new ArrayList<Circle2D>();
	private List<Circle2D> greenBalls = new ArrayList<Circle2D>();
	private List<LineSegment2D> reactors = new ArrayList<LineSegment2D>();
	private List<LineSegment2D> yellowWalls = new ArrayList<LineSegment2D>();
	private LineSegment2D silo;

	private Rectangle2D position = new Rectangle2D(2, 2, 5, 5);;
	private double odometryDistance = 0;
	private double odometryAngle = 0;
	private double angleOfView = 50 * Math.PI / 180;
	private double clock = 0.1;// in seconds
	private double noiseForward = 1;
	private double noiseAngle = 1;
	private double noiseIR = 1;
	private double maxSpeed = 3;
	private double maxAngularSpeed = Math.PI / 2;
	private Random noiseGenerator = new Random();

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
			this.yellowWalls.add(maze.edge(reactors[i]));
		}
		this.silo = maze.edge(silo);
	}

	/**
	 * Sets the angle of view of the camera
	 * 
	 * @param angleOfView
	 */
	public void setAngleOfView(double angleOfView) {
		this.angleOfView = angleOfView;
	}

	/**
	 * Initialize the position of the robots
	 * 
	 * @param width
	 *            robot's width
	 * @param height
	 *            robot's height
	 * @param startX
	 *            starting x position
	 * @param startY
	 *            starting y position
	 */
	public void setLocation(double width, double height, double startX,
			double startY) {
		position = new Rectangle2D(width, height, startY, startY);
	}

	public double noisedIR(double value) {
		return value + noiseGenerator.nextGaussian() * noiseIR;
	}

	public double noisedForward(double value) {
		return value + noiseGenerator.nextGaussian() * noiseForward;
	}

	public double noisedAngle(double value) {
		return value + noiseGenerator.nextGaussian() * noiseAngle;
	}

	/**
	 * finds the distance to the closest wall of the maze
	 * 
	 * @param ray
	 * @return the distance to the closes wall
	 */

	double mazeIntersect(Ray2D ray) {
		points = maze.intersections(ray).iterator();
		Point2D source = ray.firstPoint();
		double minDistance = Double.MAX_VALUE;
		Point2D closestPoint = new Point2D();
		for (Point2D point = points.next(); point != null; point = points
				.next()) {
			if (source.distance(point) < minDistance)
				closestPoint = point;
		}

		return source.distance(closestPoint);
	}

	/**
	 * Simulates the robot movement
	 * 
	 * @param speed
	 * @param angularSpeed
	 */
	public void move(double speed, double angularSpeed) {

		// calculate actual speed
		double v = Math.max(speed, maxSpeed);
		double w = Math.max(angularSpeed, maxAngularSpeed);

		// calculate the movement according to y and x
		double y = noisedForward(v / w * (1 - Math.cos(w * clock)));
		double x = noisedForward(v / w * (1 - Math.cos(w * clock)));

		// calculate the required translation and translation
		double rotation = noisedAngle(w * clock);
		Vector2D translation = new Vector2D(x, y);

		// actually move the robot
		AffineTransform2D movement = AffineTransform2D
				.createTranslation(translation);
		position.transform(movement);
		movement = AffineTransform2D.createRotation(rotation);
		position.transform(movement);

		// update odometry
		odometryDistance = noisedForward(Math.sqrt(x * x + y * y));
		odometryAngle = noisedAngle(rotation);
	}

	/**
	 * Simulates the vision processing
	 * 
	 * @param robot
	 *            our robot
	 */
	public void checkColors(VisionDetector detector) {

		// get the front side of the robot
		frontSide = position.edges().iterator().next();

		// direction's line is the median of the front side
		slaveL = frontSide.getMedian();
		robotAngle = slaveL.horizontalAngle();

		// the head of the robot
		head = frontSide.intersection(slaveL);

		// we find the direction as a ray
		direction = new Ray2D(head, robotAngle);

		// now with small angle steps we look if we see something
		for (int k = -100; k < 100; k++) {

			cameraAngle = robotAngle + angleOfView / 100.0 * k;
			cameraRay = new Ray2D(head, Angle2D.formatAngle(cameraAngle));

			// calculate the distance of the ray to the first wall
			// this is to avoid seeing objects behind walls
			double distanceToMazeWall = this.mazeIntersect(cameraRay);

			// check if the ray intersects any red balls
			for (Circle2D redBall : redBalls) {
				Point2D ballIntersection = redBall.intersections(cameraRay)
						.iterator().next();
				double distanceToBall = ballIntersection.distance(head);

				// check if the object is not behind a wall
				if (distanceToBall < distanceToMazeWall) {
					detector.sawBall(Color.red, distanceToBall, cameraAngle);
				}
			}

			// check green balls
			for (Circle2D greenBall : greenBalls) {
				Point2D ballIntersection = greenBall.intersections(cameraRay)
						.iterator().next();
				double distanceToBall = ballIntersection.distance(head);
				if (distanceToBall < distanceToMazeWall) {
					detector.sawBall(Color.green, distanceToBall, cameraAngle);
				}
			}

			// check reactors
			for (LineSegment2D reactor : reactors) {
				Point2D reactorIntersection = reactor.intersections(cameraRay)
						.iterator().next();
				double distanceToReactor = reactorIntersection.distance(head);
				if (distanceToReactor < distanceToMazeWall) {
					detector.sawRectangle(Color.green, distanceToReactor,
							cameraAngle);
				}
			}

			// check yellow walls
			for (LineSegment2D yellow : yellowWalls) {
				Point2D yellowIntersection = yellow.intersections(cameraRay)
						.iterator().next();
				double distanceToYellowWall = yellowIntersection.distance(head);
				if (distanceToYellowWall < distanceToMazeWall) {
					detector.sawRectangle(Color.yellow, distanceToYellowWall,
							cameraAngle);
				}
			}

			// check silo
			Point2D siloIntersection = silo.intersections(cameraRay).iterator()
					.next();
			double distanceToYellowWall = siloIntersection.distance(head);
			if (distanceToYellowWall < distanceToMazeWall) {
				detector.sawRectangle(Color.red, distanceToYellowWall,
						cameraAngle);
			}
		}
	}

	/**
	 * update IR readings
	 */

	public void updateIRs(List<Double> readings) {
		Iterator<Point2D> vertices = position.vertices().iterator();
		// get the front side of the robot
		frontSide = position.edges().iterator().next();

		// direction's line is the median of the front side
		slaveL = frontSide.getMedian();
		robotAngle = slaveL.horizontalAngle();

		// get the needed vertices

		// the head of the robot
		head = frontSide.intersection(slaveL);

		// corners
		Point2D v1 = vertices.next();
		Point2D v2 = vertices.next();
		Point2D v3 = vertices.next();
		Point2D v4 = vertices.next();
		Point2D robotCenter = position.centroid();

		// create rays for each of the sensors
		List<Ray2D> irs = new ArrayList<Ray2D>();
		irs.add(new Ray2D(robotCenter, v1));
		irs.add(new Ray2D(robotCenter, head));
		irs.add(new Ray2D(robotCenter, v2));
		irs.add(new Ray2D(v1, v2));
		irs.add(new Ray2D(v4, v3));
		irs.add(new Ray2D(head, robotCenter));
		irs.add(new Ray2D(v3, v4));
		irs.add(new Ray2D(v2, v1));

		// calculate and update readings
		for (int i = 0; i < 8; i++) {
			double distanceToMazeWall = this.mazeIntersect(irs.get(i));
			readings.set(i, noisedIR(distanceToMazeWall));
		}

	}
	
	/**
	 * Simulates odometry readings
	 * @return odometry readings
	 */
	public SimpleEntry<Double,Double> getOdometry(){
		return new SimpleEntry(odometryDistance, odometryAngle);
	}

	/**
	 * draws the current state of the map
	 * 
	 * @param window
	 */
	public void draw(JPanel window) {
		Graphics2D g = (Graphics2D) window.getGraphics();
		g.setColor(Color.blue);
		g.setStroke(new BasicStroke(3));
		maze.draw(g);

		// draw red stuff
		g.setColor(Color.red);
		for (Circle2D redBall : redBalls) {
			redBall.draw(g);
		}
		silo.draw(g);

		// draw green stuff
		g.setColor(Color.green);
		for (Circle2D greenBall : greenBalls) {
			greenBall.draw(g);
		}
		for (LineSegment2D reactor : reactors) {
			reactor.draw(g);
		}

		// draw yellow stuff
		g.setColor(Color.yellow);
		for (LineSegment2D wall : yellowWalls) {
			wall.draw(g);
		}

		// draw the robot
		g.setColor(Color.black);
		position.draw(g);
		head.draw(g);

	}

}
