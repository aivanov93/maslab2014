package fakerobot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import robot.sensors.RobotEnviroment;

import vision.detector.VisionDetector;

import math.geom2d.conic.Circle2D;

import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.Polyline2D;
import math.geom2d.polygon.Rectangle2D;
import math.geom2d.polygon.SimplePolygon2D;
import math.geom2d.AffineTransform2D;
import math.geom2d.Angle2D;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;

/**
 * Represents the playing map
 */
public class RobotSimulator implements RobotEnviroment {
	private Polyline2D maze;
	private List<Circle2D> redBalls = new ArrayList<Circle2D>();
	private List<Circle2D> greenBalls = new ArrayList<Circle2D>();
	private List<LineSegment2D> reactors = new ArrayList<LineSegment2D>();
	private List<LineSegment2D> yellowWalls = new ArrayList<LineSegment2D>();
	private LineSegment2D silo;

	private SimplePolygon2D position = Polygons2D.createOrientedRectangle(
			new Point2D(5, 5), 7, 5, 0);

	// information to get from the robot
	private double odometryDistance = 0;
	private double odometryAngle = 0;

	private int ballsCollected = 0;
	private int redBallsCollected = 0;
	private int greenBallsCollected = 0;

	private double angleOfView = 50 * Math.PI / 180;
	private double clock = 0.1;// in seconds

	private double noiseForward = 0.2;
	private double noiseAngle = 0.05;
	private double noiseIR = 0.1;

	private double maxSpeed = 8;
	private double maxAngularSpeed = Math.PI / 2 / 10;

	private Random noiseGenerator = new Random();

	private double length, width;

	// useful things
	LineSegment2D frontSide;
	StraightLine2D slaveL;
	Ray2D direction, cameraRay;
	Point2D head, center;
	double robotAngle, cameraAngle;
	Iterator<Point2D> points;
	MapGUI mygui;

	/**
	 * Constructor
	 * 
	 * @param mazeXcoords
	 *            coordinates of maze vertices !first and last vertex have to be
	 *            the same to close the maze
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
	public RobotSimulator(double[] mazeXcoords, double[] mazeYcoords,
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

		mygui = new MapGUI(this);
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
			double startY, double angle) {
		position = Polygons2D.createOrientedRectangle(new Point2D(startX,
				startY), height, width, angle);
		length = height;
		this.width = width;
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

	double mazeIntersect(Point2D source, Ray2D ray) {
		points = maze.intersections(ray).iterator();
		double minDistance = Double.MAX_VALUE;
		Point2D closestPoint = new Point2D();

		while (points.hasNext()) {
			Point2D point = points.next();

			if (source.distance(point) < minDistance)
				closestPoint = point;
			minDistance = source.distance(point);
		}

		return source.distance(closestPoint);
	}

	/**
	 * Simulates the robot movement
	 * 
	 * @param speed
	 * @param angularSpeed
	 */
	public void move(double speedC, double angularSpeedC) {

		updatePosition();
		// calculate actual speed
		double v = speedC * maxSpeed;
		double w = angularSpeedC * maxAngularSpeed;

		// calculate the movement according to y and x
		double x, y;
		if (w == 0)
			w = 0.00000001;

		// hardcore math model for movement
		y = noisedForward(v / w
				* (Math.cos(robotAngle) - Math.cos(robotAngle + w)));
		x = noisedForward(v / w
				* (Math.sin(robotAngle + w) - Math.sin(robotAngle)));

		// calculate the required translation and translation
		double rotation = noisedAngle(w);

		// actually move the robot
		position = Polygons2D.createOrientedRectangle(new Point2D(center.x()
				+ x, center.y() + y), length, width, robotAngle + rotation);

		// update odometry
		odometryDistance = noisedForward(Math.sqrt(x * x + y * y));
		odometryAngle = noisedAngle(rotation);

		// check if the robots collected any red balls
		for (int i = 0; i < redBalls.size(); i++) {
			Circle2D redBall = redBalls.get(i);
			boolean touched = redBall
					.intersections(
							new LineSegment2D(position.vertex(2), position
									.vertex(1))).iterator().hasNext();
			if (touched) {
				redBallsCollected++;
				ballsCollected++;
				redBalls.remove(i);
			}
		}

		// check if the robots collected any green balls
		for (int i = 0; i < greenBalls.size(); i++) {
			Circle2D greenBall = greenBalls.get(i);
			boolean touched = greenBall
					.intersections(
							new LineSegment2D(position.vertex(2), position
									.vertex(1))).iterator().hasNext();
			if (touched) {
				greenBallsCollected++;
				ballsCollected++;
				greenBalls.remove(i);
			}
		}

		mygui.update();
	}

	/**
	 * Simulates the release of balls
	 * 
	 * @param howMany
	 */
	public void dumpRedBalls(int howMany) {
		redBallsCollected -= howMany;
	}

	/**
	 * Simulates the release of balls
	 * 
	 * @param howMany
	 */
	public void dumpGreenBalls(int howMany) {
		greenBallsCollected -= howMany;
	}

	/**
	 * Simulates the vision processing
	 * 
	 * @param robot
	 *            our robot
	 */
	public void updateCamera(VisionDetector detector) {

		// calculate different position arguments
		updatePosition();

		// now with small angle steps we look if we see something
		for (int k = -100; k < 100; k++) {

			cameraAngle = robotAngle + angleOfView / 100.0 * k;
			cameraRay = new Ray2D(head, Angle2D.formatAngle(cameraAngle));

			// calculate the distance of the ray to the first wall
			// this is to avoid seeing objects behind walls
			double distanceToMazeWall = this.mazeIntersect(head, cameraRay);

			// check if the ray intersects any red balls
			for (Circle2D redBall : redBalls) {
				boolean touched;
				touched = redBall.intersections(cameraRay).iterator().hasNext();
				if (touched) {
					Point2D ballIntersection = redBall.intersections(cameraRay)
							.iterator().next();
					double distanceToBall = ballIntersection.distance(head);

					// check if the object is not behind a wall
					if (distanceToBall < distanceToMazeWall
							&& distanceToBall < 90) {
						detector.sawBall(Color.red, distanceToBall, cameraAngle);
					}
				}
			}

			// check green balls
			for (Circle2D greenBall : greenBalls) {
				boolean touched;
				touched = greenBall.intersections(cameraRay).iterator()
						.hasNext();
				if (touched) {
					Point2D ballIntersection = greenBall
							.intersections(cameraRay).iterator().next();
					double distanceToBall = ballIntersection.distance(head);
					if (distanceToBall < distanceToMazeWall
							&& distanceToBall < 90) {
						detector.sawBall(Color.green, distanceToBall,
								cameraAngle);
					}
				}
			}

			// check reactors
			for (LineSegment2D reactor : reactors) {
				boolean touched;
				touched = reactor.intersections(cameraRay).iterator().hasNext();
				if (touched) {
					Point2D reactorIntersection = reactor
							.intersections(cameraRay).iterator().next();
					double distanceToReactor = reactorIntersection
							.distance(head);
					if (distanceToReactor < distanceToMazeWall
							&& distanceToReactor < 90) {
						detector.sawRectangle(Color.green, distanceToReactor,
								cameraAngle);
					}
				}
			}

			// check yellow walls
			for (LineSegment2D yellow : yellowWalls) {
				boolean touched;
				touched = yellow.intersections(cameraRay).iterator().hasNext();
				if (touched) {
					Point2D yellowIntersection = yellow
							.intersections(cameraRay).iterator().next();
					double distanceToYellowWall = yellowIntersection
							.distance(head);
					if (distanceToYellowWall < distanceToMazeWall
							&& distanceToYellowWall < 90) {
						detector.sawRectangle(Color.yellow,
								distanceToYellowWall, cameraAngle);
					}
				}

				// check silo

				touched = silo.intersections(cameraRay).iterator().hasNext();
				if (touched) {
					Point2D siloIntersection = silo.intersections(cameraRay)
							.iterator().next();
					double distanceToYellowWall = siloIntersection
							.distance(head);
					if (distanceToYellowWall < distanceToMazeWall
							&& distanceToYellowWall < 90) {
						detector.sawRectangle(Color.red, distanceToYellowWall,
								cameraAngle);
					}
				}
			}
		}

	}

	/**
	 * update IR readings
	 */

	public void updateReadings(double[] readings) {
		Iterator<Point2D> vertices = position.vertices().iterator();

		updatePosition();

		// corners
		Point2D v1 = position.vertex(2);
		Point2D v2 = position.vertex(1);
		Point2D v3 = position.vertex(0);
		Point2D v4 = position.vertex(3);

		// create rays for each of the sensors
		List<Ray2D> irs = new ArrayList<Ray2D>();
		List<Point2D> sources = new ArrayList<Point2D>();
		irs.add(new Ray2D(center, v1));
		sources.add(head);
		irs.add(new Ray2D(center, head));
		sources.add(head);
		irs.add(new Ray2D(center, v2));
		sources.add(head);
		irs.add(new Ray2D(v1, v2));
		sources.add(v2);
		irs.add(new Ray2D(v4, v3));
		sources.add(v3);
		irs.add(new Ray2D(head, center));
		sources.add(Point2D.midPoint(v3, v4));
		irs.add(new Ray2D(v3, v4));
		sources.add(v4);
		irs.add(new Ray2D(v2, v1));
		sources.add(v1);

		// calculate and update readings
		for (int i = 0; i < 8; i++) {

			double distanceToMazeWall = this.mazeIntersect(sources.get(i),
					irs.get(i));
			if (distanceToMazeWall > 120 || distanceToMazeWall < 10) {
				distanceToMazeWall = -1.0;
			}

			readings[i] = noisedIR(distanceToMazeWall);
		}

	}

	/**
	 * Simulates odometry readings
	 * 
	 * @return odometry readings
	 */
	public SimpleEntry<Double, Double> getOdometry() {
		return new SimpleEntry(odometryDistance, odometryAngle);
	}

	public int ballsCollected() {
		ballsCollected = 0;
		return ballsCollected;
	}

	/**
	 * Tells how many red balls where collected
	 * 
	 * @return number of balls
	 */
	public int redBallsCollected() {
		return redBallsCollected;
	}

	/**
	 * Tells how many red balls where collected
	 * 
	 * @return number of balls
	 */
	public int greenBallsCollected() {
		return greenBallsCollected;
	}

	/**
	 * draws the current state of the map
	 * 
	 * @param window
	 */
	public void draw(Graphics2D g) {
		updatePosition();
		// Graphics2D g = (Graphics2D) gg;
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
		g.setColor(Color.red);
		g.setStroke(new BasicStroke(30));
		head.draw(g);
		System.out.println("printed");

	}

}
