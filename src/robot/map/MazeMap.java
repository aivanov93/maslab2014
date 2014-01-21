package robot.map;

import game.StateMachine.Goal;
import global.Constants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;

import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.PolygonBufferer;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacleImpl;
import straightedge.geom.path.PathData;
import straightedge.geom.path.PathFinder;

/**
 * Map representation for the maze. Used to calculate the shortest paths. Is
 * different from MapForSensors because it is based on StraighEdge library which
 * uses different geometry objects
 */
public class MazeMap implements Runnable {
	JFrame frame;
	ArrayList<KPolygon> walls = new ArrayList<KPolygon>();
	ArrayList<PathBlockingObstacleImpl> obstacles = new ArrayList<PathBlockingObstacleImpl>();
	ArrayList<KPoint> reactors = new ArrayList<KPoint>();
	ArrayList<KPoint> safeReactors = new ArrayList<KPoint>();
	ArrayList<KPoint> yellowWalls = new ArrayList<KPoint>();
	ArrayList<KPoint> safeYellowWalls = new ArrayList<KPoint>();
	KPoint silo;
	KPoint safeSilo;
	Set<KPoint> visitedReactorSet = new HashSet<KPoint>();
	PathFinder pathFinder;
	NodeConnector<PathBlockingObstacleImpl> nodeConnector;
	PathData pathData = new PathData();
	PolygonBufferer bufferer = new PolygonBufferer();

	KPoint currentPosition;
	KPoint currentObject;
	Goal goal;

	/**
	 * Initializers
	 * 
	 * @param walls
	 *            a list of all walls of the maze including silo and reactors
	 * @param reactors
	 *            list of reactor segments
	 * @param yellowWalls
	 *            list of yellow walls
	 * @param silo
	 *            the silo
	 */
	public MazeMap(List<LineSegment2D> walls, List<LineSegment2D> reactors,
			List<LineSegment2D> yellowWalls, LineSegment2D silo) {
		for (LineSegment2D wall : walls) {
			// get the wall coordinates
			double x0 = wall.firstPoint().x();
			double y0 = wall.firstPoint().y();
			double x1 = wall.lastPoint().x();
			double y1 = wall.lastPoint().y();

			// make a rectangle out of them
			double l = 0.5;
			double angle = Math.atan2(y1 - y0, x1 - x0);
			double yn1, yn2, xn1, xn2;

			xn1 = x0 - l * Math.sin(angle);
			xn2 = x1 - l * Math.sin(angle);

			yn1 = y0 + l * Math.cos(angle);
			yn2 = y1 + l * Math.cos(angle);

			ArrayList<KPoint> points = new ArrayList<KPoint>();
			points.add(new KPoint(x0, y0));
			points.add(new KPoint(xn1, yn1));
			points.add(new KPoint(xn2, yn2));
			points.add(new KPoint(x1, y1));

			// make an obstacle out of the rectangle
			PathBlockingObstacleImpl.BUFFER_AMOUNT = (float) Constants.safeDistanceToWall;
			PathBlockingObstacleImpl.NUM_POINTS_IN_A_QUADRANT = 0;
			KPolygon buffered = bufferer.buffer(new KPolygon(points),
					(float) Constants.safeDistanceToWall, 0);
			PathBlockingObstacleImpl.BUFFER_AMOUNT = 3;// (float) 50;
			obstacles.add(PathBlockingObstacleImpl
					.createObstacleFromInnerPolygon(buffered));

		}

		for (LineSegment2D reactor : reactors) {
			Point2D center = Point2D.midPoint(reactor.firstPoint(),
					reactor.lastPoint());
			this.reactors.add(new KPoint(center.x(), center.y()));
			this.safeReactors.add(getNearestPointOutsideOfObstacles(new KPoint(
					center.x(), center.y())));
		}

		for (LineSegment2D wall : yellowWalls) {
			Point2D center = Point2D.midPoint(wall.firstPoint(),
					wall.lastPoint());
			this.yellowWalls.add(new KPoint(center.x(), center.y()));
			this.safeYellowWalls
					.add(getNearestPointOutsideOfObstacles(new KPoint(center
							.x(), center.y())));
		}
		Point2D center = Point2D.midPoint(silo.firstPoint(), silo.lastPoint());
		this.silo = new KPoint(center.x(), center.y());
		safeSilo = getNearestPointOutsideOfObstacles(new KPoint(center.x(),
				center.y()));

		nodeConnector = new NodeConnector<PathBlockingObstacleImpl>();
		for (int k = 0; k < obstacles.size(); k++) {
			nodeConnector.addObstacle(obstacles.get(k), obstacles, 1000f);
		}

		// Initialize the PathFinder
		pathFinder = new PathFinder();

		// draw the map
		final JComponent renderComponent = new JComponent() {

			private static final long serialVersionUID = 1L;

			// This method is called every time the repaintThread does
			// renderComponent.repaint().
			public void paint(Graphics graphics) {
				// calculate the time since the last update.

				// render the obstacles and player:
				Graphics2D g = (Graphics2D) graphics;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
				float backGroundGrey = 77f / 255f;
				g.setColor(new Color(backGroundGrey, backGroundGrey,
						backGroundGrey));
				g.fillRect(0, 0, getWidth(), getHeight());

				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				float g4 = 0.1f;

				for (int i = 0; i < obstacles.size(); i++) {
					g.setColor(Color.red);
					g.fill(obstacles.get(i).getOuterPolygon());
				}

				g.setColor(Color.blue);

				ArrayList<KPoint> pathPoints = pathData.points;
				if (pathPoints.size() > 0) {
					KPoint currentPoint = new KPoint(675, 825);
					for (int j = 0; j < pathPoints.size(); j++) {
						KPoint nextPoint = pathPoints.get(j);
						g.draw(new Line2D.Double(currentPoint.getX(),
								currentPoint.getY(), nextPoint.getX(),
								nextPoint.getY()));
						float d = 5f;
						g.fill(new Ellipse2D.Double(nextPoint.getX() - d / 2f,
								nextPoint.getY() - d / 2f, d, d));
						currentPoint = nextPoint;
					}
				}

			}
		};
		frame = new JFrame();
		frame.setPreferredSize(new Dimension(1200, 1080));
		frame.setSize(1200, 1080);
		frame.add(renderComponent);
		renderComponent.repaint();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		List<LineSegment2D> walls = new ArrayList<LineSegment2D>();
		walls.add(new LineSegment2D(200, 500, 100, 400));
		walls.add(new LineSegment2D(100, 400, 200, 300));
		walls.add(new LineSegment2D(200, 300, 0, 100));
		walls.add(new LineSegment2D(0, 100, 600, 0));
		walls.add(new LineSegment2D(600, 0, 600, 500));
		walls.add(new LineSegment2D(600, 500, 400, 200));
		walls.add(new LineSegment2D(400, 200, 200, 500));
		MazeMap n = new MazeMap(walls, new ArrayList<LineSegment2D>(),
				new ArrayList<LineSegment2D>(), new LineSegment2D(0, 0, 0, 0));
		n.findSilo(200, 400);
	}

	/**
	 * finds the shortest path from (x,y) to (finx,finy) through the maze;
	 * points can be inside obstacles
	 * 
	 * @param x
	 * @param y
	 * @param finx
	 * @param finy
	 */
	public void findSilo(double x, double y) {
		KPoint pos = getNearestPointOutsideOfObstacles(new KPoint(x, y));
		currentObject = silo;
		pathData = pathFinder.calc(pos, safeSilo, 1000, nodeConnector,
				obstacles);

	}

	/**
	 * finds the closest reactor to this point
	 * 
	 * @param x
	 * @param y
	 */
	public void findClosestReactor(double x, double y) {
		KPoint pos = getNearestPointOutsideOfObstacles(new KPoint(x, y));
		pathData = pathFinder.calc(pos, safeReactors.get(0), 1000,
				nodeConnector, obstacles);
		double minLength = length(pathData.points);
		currentObject = reactors.get(0);
		for (int i = 1; i < 3; i++) {
			PathData temp = pathFinder.calc(pos, safeReactors.get(i), 1000,
					nodeConnector, obstacles);
			double length = length(temp.points);
			if (length < minLength) {
				pathData = temp;
				minLength = length;
				currentObject = reactors.get(i);
			}

		}
		System.out.println("path in map" + pathData.points);
	}

	/**
	 * finds the path to the given reactor from this point
	 * 
	 * @param x
	 * @param y
	 */
	public void findReactor(double x, double y, int i) {
		KPoint pos = getNearestPointOutsideOfObstacles(new KPoint(x, y));
		currentObject = reactors.get(i);
		pathData = pathFinder.calc(pos, safeReactors.get(i), 500,
				nodeConnector, obstacles);
		System.out.println("path in map" + pathData.points);
	}

	/**
	 * sets the next goal to look the path for
	 * 
	 * @param goal
	 */
	public void setNextGoal(Goal goal) {
		this.goal = goal;
	}

	public double length(ArrayList<KPoint> path) {
		double distance = 0;
		KPoint currentPoint = path.get(0);
		for (int i = 1; i < path.size(); i++) {
			KPoint nextPoint = path.get(i);
			distance += currentPoint.distance(nextPoint);
			nextPoint = currentPoint;
		}
		return distance;
	}

	public ArrayList<KPoint> getPath() {
		return pathData.points;
	}

	/**
	 * returns the real position of the object we are currently aligning to
	 * 
	 * @return
	 */
	public Point2D currentObject() {
		return new Point2D(currentObject.x, currentObject.y);
	}

	public KPoint getNearestPointOutsideOfObstacles(KPoint point) {
		KPoint movedPoint = point.copy();
		boolean targetIsInsideObstacle = false;
		int count = 0;
		while (true) {
			for (PathBlockingObstacleImpl obst : obstacles) {
				if (obst.getOuterPolygon().contains(movedPoint)) {
					targetIsInsideObstacle = true;
					KPolygon poly = obst.getOuterPolygon();
					KPoint p = poly.getBoundaryPointClosestTo(movedPoint);
					if (p != null) {
						movedPoint.x = p.x;
						movedPoint.y = p.y;
					}
					assert point != null;
				}
			}
			count++;
			if (targetIsInsideObstacle == false || count >= 3) {
				break;
			}
		}
		return movedPoint;
	}

	public void draw(Graphics2D g2) {
		ArrayList<KPoint> pathPoints = pathData.points;
		g2.setColor(Color.gray);
		g2.setStroke(new BasicStroke(2));
		if (pathPoints.size() > 0) {
			KPoint currentPoint =  pathPoints.get(0);
			for (int j = 1; j < pathPoints.size(); j++) {
				KPoint nextPoint = pathPoints.get(j);
				g2.draw(new Line2D.Double(currentPoint.getX(), currentPoint
						.getY(), nextPoint.getX(), nextPoint.getY()));
				float d = 5f;
				g2.fill(new Ellipse2D.Double(nextPoint.getX() - d / 2f,
						nextPoint.getY() - d / 2f, d, d));
				currentPoint = nextPoint;
			}
		}
	}

	@Override
	public void run() {

	}

}
