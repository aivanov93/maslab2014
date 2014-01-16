package robot.map;

import global.Constants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import math.geom2d.line.LineSegment2D;

import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacleImpl;
import straightedge.geom.path.PathData;
import straightedge.geom.path.PathFinder;

/**
 * Map representation for the maze
 * 
 */
public class MazeMap {
	JFrame frame;
	ArrayList<KPolygon> walls = new ArrayList<KPolygon>();
	ArrayList<PathBlockingObstacleImpl> obstacles = new ArrayList<PathBlockingObstacleImpl>();
	PathFinder pathFinder;
	NodeConnector nodeConnector;
	PathData pathData;

	public MazeMap(List<LineSegment2D> walls) {
		for (LineSegment2D wall: walls) {
			// get the wall coordinates
			double x0 = wall.firstPoint().x();
			double y0 = wall.firstPoint().y();
			double x1 = wall.lastPoint().x();
			double y1 = wall.lastPoint().y();

			// make a rectangle out of them
			double l = 0.5;
			double angle = Math.atan2(y1 - y0, x1 - x0);
			System.out.println(angle);
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
			PathBlockingObstacleImpl.NUM_POINTS_IN_A_QUADRANT = 3;
			obstacles.add(PathBlockingObstacleImpl
					.createObstacleFromInnerPolygon(new KPolygon(points)));

		}

		nodeConnector = new NodeConnector();
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
					g.fill(obstacles.get(i).getInnerPolygon());
					System.out.println(obstacles.get(i).getInnerPolygon());
				}
				
				g.setColor(Color.blue);
				
				ArrayList<KPoint> pathPoints = pathData.points;
				if (pathPoints.size() > 0) {
					KPoint currentPoint = new KPoint(200,400);
					for (int j = 0; j < pathPoints.size(); j++) {
						KPoint nextPoint = pathPoints.get(j);
						g.draw(new Line2D.Double(currentPoint.getX(), currentPoint.getY(), nextPoint.getX(), nextPoint.getY()));
						float d = 5f;
						g.fill(new Ellipse2D.Double(nextPoint.getX() - d / 2f, nextPoint.getY() - d / 2f, d, d));
						currentPoint = nextPoint;
					}
				}

			}
		};
		frame = new JFrame();
		frame.setPreferredSize(new Dimension(800, 800));
		frame.setSize(800, 800);
		frame.add(renderComponent);
		renderComponent.repaint();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		List<LineSegment2D> walls=new ArrayList<LineSegment2D>();
		walls.add(new LineSegment2D(200, 500, 100, 400));
		walls.add(new LineSegment2D(100, 400, 200, 300));
		walls.add(new LineSegment2D(200, 300, 0, 100));
		walls.add(new LineSegment2D(0, 100, 600, 0));
		walls.add(new LineSegment2D(600, 0, 600, 500));
		walls.add(new LineSegment2D(600, 500, 400, 200));
		walls.add(new LineSegment2D(400, 200, 200, 500));		
		MazeMap n=new MazeMap(walls);
		n.findPath(200, 400, 500, 300);
	}

	/**
	 * finds the shortest path from (x,y) to (finx,finy) through the maze;
	 * points can be inside obstacles
	 * 
	 * @param x
	 * @param y
	 * @param finx
	 * @param finy
	 * @return
	 */
	public ArrayList<KPoint> findPath(double x, double y, double finx,
			double finy) {
		KPoint pos = getNearestPointOutsideOfObstacles(new KPoint(x, y));
		KPoint target = getNearestPointOutsideOfObstacles(new KPoint(finx, finy));

		pathData = pathFinder.calc(pos, target, 1000, nodeConnector,
				obstacles);
		ArrayList<KPoint> pathPoints = pathData.points;
		return pathPoints;
	}
	
	//public double findProbability

	public KPoint getNearestPointOutsideOfObstacles(KPoint point) {
		// check that the target point isn't inside any obstacles.
		// if so, move it.
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

}
