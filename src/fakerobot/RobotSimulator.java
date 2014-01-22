package fakerobot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import robot.map.Position;
import robot.sensors.RangeSensors;
import robot.sensors.Odometry;
import robot.sensors.RobotEnviroment;
import game.StateMachine.State;
import global.Constants;

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
 * Imitates the playing enviroment. Allows odometry readings, vision imitation,
 * sensor readings and robot movements. 
 */
public class RobotSimulator implements RobotEnviroment {
	/**
	 * Maze objects
	 */
	private List<LineSegment2D> maze;
	private List<Circle2D> redBalls = new ArrayList<Circle2D>();
	private List<Circle2D> greenBalls = new ArrayList<Circle2D>();
	private List<LineSegment2D> reactors = new ArrayList<LineSegment2D>();
	private List<LineSegment2D> yellowWalls = new ArrayList<LineSegment2D>();
	private LineSegment2D silo;

	/**
	 * Represents the current position of the robot
	 */
	private Circle2D position;

	
	/**
	 *  Odometry readings
	 */
	private double xMoved = 0;
	private double yMoved = 0;
	private double angleMoved = 0;

	/**
	 * Balls related counters
	 */
	private int ballsCollected = 0;
	private int redBallsCollected = 0;
	private int greenBallsCollected = 0;

	/**
	 * Imitates the angle of view of the camera
	 */
	private double angleOfView = 50 * Math.PI / 180;
	
	private double maxSpeed =6  * Constants.clock / 100f; //4
	private double maxAngularSpeed = Math.PI / 2 / 10 * Constants.clock / 100f; //2

	private Random noiseGenerator = new Random();

	private double radius;

	
	private HashMap<LineSegment2D, SimpleEntry<Integer, Integer>> reactorBalls = new HashMap<LineSegment2D, SimpleEntry<Integer, Integer>>();

	// useful things
	LineSegment2D frontSide;
	StraightLine2D slaveL;
	Ray2D direction, cameraRay;
	Point2D head, center;
	double robotAngle, cameraAngle;
	Iterator<Point2D> points;

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
	public RobotSimulator(List<LineSegment2D> walls, LineSegment2D silo,
			List<LineSegment2D> reactors, List<LineSegment2D> yellowWalls,
			double[] redBallXcoords, double[] redBallYcoords,
			double[] greenBallXcoords, double[] greenBallYcoords,
			double ballRadius) {

		maze = walls;
		for (int i = 0; i < redBallXcoords.length; i++) {
			redBalls.add(new Circle2D(redBallXcoords[i], redBallYcoords[i],
					ballRadius));
		}
		for (int i = 0; i < greenBallXcoords.length; i++) {
			greenBalls.add(new Circle2D(greenBallXcoords[i],
					greenBallYcoords[i], ballRadius));
		}
		this.reactors = reactors;
		this.yellowWalls = yellowWalls;
		this.silo = silo;

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
	public void setLocation(double radius, double startX,
			double startY, double angle) {
		position = new Circle2D(new Point2D(startX,
				startY), radius);
		robotAngle=angle;
		this.radius = radius;
	}

	public void update() {
	};

	public double noised(double value) {
		return value + noiseGenerator.nextGaussian() * value * 0.1;
	}

	public void updatePosition() {

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
		for (LineSegment2D wall : maze) {
			Point2D point = ray.intersection(wall);
			if (point != null) {
				if (source.distance(point) < minDistance)
					closestPoint = point;
				minDistance = source.distance(point);
			}
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
		if (w == 0)
			w = 0.00000001;

		// hardcore math model for movement
		yMoved = noised(v / w
				* (Math.cos(robotAngle) - Math.cos(robotAngle + w)));
		xMoved = noised(v / w
				* (Math.sin(robotAngle + w) - Math.sin(robotAngle)));

		// calculate the required translation and translation
		angleMoved = noised(w);

	
		// actually move the robot
		position = new Circle2D(new Point2D(center.x()
				+ xMoved, center.y() + yMoved), radius);
		robotAngle+=angleMoved;

		// update odometry
		yMoved = noised(yMoved);
		xMoved = noised(xMoved);
		angleMoved = noised(angleMoved);

	
		// check if the robots collected any red balls
		for (int i = 0; i < redBalls.size(); i++) {
			Circle2D redBall = redBalls.get(i);
			boolean touched = redBall
					.intersections(position).iterator().hasNext();
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
					.intersections(position).iterator().hasNext();
			if (touched) {
				greenBallsCollected++;
				ballsCollected++;
				greenBalls.remove(i);
			}
		}

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
	 * Simulates the release of balls in the top of the reactor
	 * 
	 * @param howMany
	 */
	public void dumpGreenBallsTop(int howMany) {
		greenBallsCollected -= howMany;
		updatePosition();
		// check reactors
		boolean touched = false;
		for (LineSegment2D reactor : reactorBalls.keySet()) {
			touched = reactor.intersections(direction).iterator().hasNext();
			if (touched) {
				int top = reactorBalls.get(reactor).getKey();
				int bottom = reactorBalls.get(reactor).getValue();
				top++;
				reactorBalls.put(reactor, new SimpleEntry(top, bottom));
			}
		}
		if (!touched) {

		}
		updateScores();
	}

	/**
	 * Simulates the release of balls in the bottom of the reactor
	 * 
	 * @param howMany
	 */
	public void dumpGreenBallsBottom(int howMany) {
		greenBallsCollected -= howMany;
		updatePosition();
		boolean touched = false;
		for (LineSegment2D reactor : reactorBalls.keySet()) {
			touched = reactor.intersections(direction).iterator().hasNext();
			if (touched) {
				int top = reactorBalls.get(reactor).getKey();
				int bottom = reactorBalls.get(reactor).getValue();
				bottom++;
				reactorBalls.put(reactor, new SimpleEntry<Integer, Integer>(
						top, bottom));
			}
		}
		if (!touched) {

		}
		updateScores();
	}

	public void updateScores() {
		for (LineSegment2D reactor : reactorBalls.keySet()) {
			int top = reactorBalls.get(reactor).getKey();
			int bottom = reactorBalls.get(reactor).getValue();
			int bonus1 = 0, bonus2 = 0;
			if (top > 0)
				bonus1 = 10;
			if (bottom > 0)
				bonus2 = 10;
			//score += top * 7 + bottom * 3 + bonus1 + bonus2;
			// mygui.setScore(score);
		}
	}

	/**
	 * Simulates the vision processing
	 * 
	 * @param robot
	 *            our robot
	 */
	public void updateCamera(VisionDetector detector) {

		detector.reset();
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
						detector.sawBall(Color.red, distanceToBall, cameraAngle
								- robotAngle);
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
								cameraAngle - robotAngle);
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
					if (distanceToReactor <= distanceToMazeWall
							&& distanceToReactor < 90) {
						detector.sawRectangle(Color.green, distanceToReactor,
								cameraAngle - robotAngle);
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
					if (distanceToYellowWall <= distanceToMazeWall
							&& distanceToYellowWall < 90) {
						detector.sawRectangle(Color.yellow,
								distanceToYellowWall, cameraAngle - robotAngle);
					}
				}
			}
			// check silo

			boolean touched = silo.intersections(cameraRay).iterator()
					.hasNext();
			if (touched) {

				Point2D siloIntersection = silo.intersections(cameraRay)
						.iterator().next();
				double distanceToSilo = siloIntersection.distance(head);
				if (distanceToSilo <= distanceToMazeWall && distanceToSilo < 90) {
					detector.sawRectangle(Color.red, distanceToSilo,
							cameraAngle - robotAngle);
				}
			}

		}

	}

	/**
	 * update Range sensor readings readings
	 */

	public void updateReadings(RangeSensors irSensors) {
		updatePosition();

		// calculate and update readings
		for (int i = 0; i < Constants.numberOfIRs; i++) {
			Ray2D beam=new Ray2D(position.center(), Constants.irDirections.get(i)+robotAngle);
			Point2D source=position.intersections(beam).iterator().next();
			double distanceToMazeWall = this.mazeIntersect(source,
					beam);
			if (distanceToMazeWall > Constants.maxIRreading) distanceToMazeWall=Constants.maxIRreading+1;
			else if (distanceToMazeWall < Constants.minIRreading) distanceToMazeWall =1;
			else distanceToMazeWall=noised(distanceToMazeWall);

			irSensors.set(i, distanceToMazeWall);
		}

	}

	/**
	 * Simulates odometry readings
	 * 
	 * @return odometry readings
	 */
	public Odometry odometry() {
		return new Odometry(xMoved, yMoved, angleMoved);
	}

	public void updateOdometry(Odometry odometry) {
		odometry.set(xMoved, yMoved, angleMoved);
	}

	public int ballsCollected() {
		ballsCollected = 0;
		return ballsCollected;
	}

	public void collectSilo() {
		ballsCollected++;
		redBallsCollected++;
	}

	public boolean seesWall() {
		return false;
	}

	/**
	 * Tells how many red balls where collected
	 * 
	 * @return number of balls
	 */
	public int redBallsInside() {
		return redBallsCollected;
	}

	/**
	 * Tells how many red balls where collected
	 * 
	 * @return number of balls
	 */
	public int greenBallsInside() {
		return greenBallsCollected;
	}

	public void setState(State state) {
		// mygui.setState(state);
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
		for (LineSegment2D wall : maze) {
			wall.draw(g);
		}

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
		g.setStroke(new BasicStroke(5));
		Circle2D face=new Circle2D(head,5);
		face.draw(g);
	

	}

}
