package vision.detector;

import game.StateMachine.WallSide;
import global.Constants;

import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import math.geom2d.Point2D;
import math.geom2d.line.Line2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;

import vision.LeastSquares;
import vision.Linearization;
import vision.Linearization2;
import vision.NewImageProcessor;
import vision.detector.ColorObject.Type;

public class VisionDetector {

	HashMap<Type, ColorObject> objects;
	int[] wallCoordinates = new int[Constants.picWidth];
	int[] dWall = new int[Constants.picWidth];
	StraightLine2D wall = null;
	Point2D[] wallPoints = new Point2D[Constants.picWidth];
	double minDist = 0;
	boolean left;

	public VisionDetector() {
		objects = new HashMap<Type, ColorObject>();
	}

	public VisionDetector(HashMap<Type, ColorObject> objects) {
		this.objects = (HashMap<Type, ColorObject>) objects.clone();

	}

	public VisionDetector clone() {
		return new VisionDetector(this.objects);
	}

	public void reset() {
		objects.clear();
	}

	/**
	 * Saves the given detected object if needed Checks if there was a similar
	 * object seen and if yes looks if this one is closer
	 * 
	 * @param object
	 */
	public void putObject(ColorObject object) {
		Type type = object.type();
		double distance = object.distance();
		if (objects.get(type) != null) {
			if (objects.get(type).distance() > distance) {
				objects.put(type, object);
			}
		} else {
			objects.put(type, object);
		}
	}

	/**
	 * Analyzes a new ball seen
	 * 
	 * @param color
	 * @param dist
	 * @param angle
	 */
	public void sawBall(Color color, double x, double y) {
		Type type;
		if (color == Color.red) {
			type = Type.RedBall;
		} else {
			type = Type.GreenBall;
		}
		double xx = Linearization2.linearizeFloorY((int) x, (int) y);
		double yy = Linearization2.linearizeFloorX((int) x, (int) y);
		Point2D ball = new Point2D(xx, yy);
		System.out.println("baaaaaaaaaaaaaaaaaaaaaaaal " + yy + "  " + xx
				+ "  dist  " + ball.distance(0, 0));
		Ray2D ray2d = new Ray2D(0, 0, xx, yy);
		putObject(new ColorObject(type, ball.distance(0, 0),
				ray2d.horizontalAngle(), yy, xx));
	}

	public void sawRectangle(Color color, double x, double y) {
		Type type;
		double dist, wallDist = 20;
		if (color == Color.cyan) {
			type = Type.Reactor;
			dist = 1;
		} else {
			dist = 10;
			type = Type.Silo;
		}
		double xx = Linearization2.linearizeFloorY((int) x, (int) y);
		double yy = Linearization2.linearizeFloorX((int) x, (int) y);

		int xpixel = (int) x;
		Point2D center = wallPoints[xpixel];
		StraightLine2D wall = findGoodPixels(xpixel - 40, xpixel + 40);
		Point2D alligningPoint = Carrot.getAlligningCarrot(wall, center, dist,
				wallDist, left);
		putObject(new ColorObject(type, new Point2D(xx, yy).distance(0, 0),
				wall.horizontalAngle(), alligningPoint.y(), alligningPoint.y()));
	}

	public int clamp(int i) {
		return Math.max(Math.min(i, 319), 0);
	}

	public void foundWalls(int[] wallHeight) {
		wallCoordinates = wallHeight;
		for (int i = 0; i < wallHeight.length; i++) {
			wallPoints[i] = new Point2D(Linearization2.linearizeY(i,
					wallHeight[i]), Linearization2.linearizeX(i, wallHeight[i]));
		}
		List<Integer> corners = new ArrayList<Integer>();
		for (int i = 3; i < wallHeight.length; i++) {
			if (Math.abs(wallHeight[i] - wallHeight[i - 2]) < Math
					.abs(wallHeight[i - 1] - wallHeight[i])) {
				wallHeight[i - 1] = (wallHeight[i] + wallHeight[i - 2]) / 2;
			}
		}
		for (int i = 1; i < wallHeight.length; i++) {
			dWall[i] = wallHeight[i] - wallHeight[i - 1];
		}

		wall = findGoodPixels(10, 150);
		makeWall(wall, Type.LeftWall);
		wall = findGoodPixels(600, 640);
		makeWall(wall, Type.RightWall);
		wall = findGoodPixels(200, 300);
		makeWall(wall, Type.CenterWall);

	}

	public void makeWall(StraightLine2D wall, Type type) {
		if (wall != null) {
			double angle = wall.horizontalAngle();
			StraightLine2D perpendicular = new StraightLine2D(
					new Point2D(0, 0), angle + Math.PI / 2);
			Point2D intersection = wall.intersection(perpendicular);
			if (intersection != null) {
				Point2D carrot = Carrot.getCarrot(wall, 40, 30, left);
				if (intersection.distance(0, 0) + 15 > minDist)
					minDist = intersection.distance(0, 0);
				if (carrot != null) {
					ColorObject wallObj = new ColorObject(type, minDist,
							Constants.formatAngle(angle), carrot.y(),
							carrot.x(), left, wall);
					objects.put(type, wallObj);
				}
			}
		}
	}

	/* ********************************
	 * *****Information getters******** *******************************
	 */

	public boolean seesSomething() {
		return !objects.isEmpty();
	}

	public boolean seesBall() {
		return objects.get(Type.RedBall) != null
				|| objects.get(Type.GreenBall) != null;
	}

	public boolean seesBigBall() {
		ColorObject bigBall = biggestBall();
		if (bigBall == null) {
			return false;
		} else {
			return bigBall.distance() < Constants.siloBallDistance;
		}

	}

	public ColorObject biggestBall() {
		ColorObject bigBall = null;
		if (seesRedBall()) {
			bigBall = redBall();
		}

		if (seesGreenBall()) {

			if (bigBall == null)
				bigBall = greenBall();
			else if (bigBall.distance() > greenBall().distance())
				bigBall = greenBall();

		}
		return bigBall;
	}

	/**
	 * Finds pixels that could belong to a line
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public StraightLine2D findGoodPixels(int start, int end) {
		List<Integer> goodWallPixels = new ArrayList<Integer>();
		minDist = Double.MAX_VALUE;
		for (int i = start; i < end; i++) {
			if (wallCoordinates[i] > 0 && dWall[i] - dWall[i - 1] < 3) {
				goodWallPixels.add(i);

			}

		}
		List<Point2D> pointsToFit = new ArrayList<Point2D>();
		if (goodWallPixels.size() > 15) {

			for (int i = 0; i < goodWallPixels.size(); i++) {
				pointsToFit.add(wallPoints[goodWallPixels.get(i)]);
				minDist = Math.min(pointsToFit.get(i).distance(0, 0), minDist);
			}
			left = pointsToFit.get(0).x() < pointsToFit.get(
					pointsToFit.size() - 1).x();

			StraightLine2D wallFound = LeastSquares.fitLine(pointsToFit);
			return wallFound;
		}
		return null;
	}

	public boolean seesWallLeft() {
		return objects.get(Type.LeftWall) != null;
	}

	public boolean seesWallRight() {
		return objects.get(Type.RightWall) != null;
	}

	public boolean seesWallCenter() {
		return objects.get(Type.CenterWall) != null;
	}

	public ColorObject leftWall() {
		return objects.get(Type.LeftWall);
	}

	public ColorObject rightWall() {
		return objects.get(Type.RightWall);
	}

	public ColorObject centerWall() {
		return objects.get(Type.CenterWall);
	}

	public boolean seesRedBall() {
		return objects.get(Type.RedBall) != null;
	}

	public boolean seesGreenBall() {
		return objects.get(Type.GreenBall) != null;
	}

	public boolean seesSilo() {
		return objects.get(Type.Silo) != null;
	}

	public boolean seesReactor() {
		return objects.get(Type.Reactor) != null;
	}

	public boolean seesYellowWall() {
		return objects.get(Type.YellowWall) != null;
	}

	public ColorObject redBall() {
		return objects.get(Type.RedBall);
	}

	public ColorObject greenBall() {
		return objects.get(Type.GreenBall);
	}

	public ColorObject silo() {
		return objects.get(Type.Silo);
	}

	public ColorObject reactor() {
		return objects.get(Type.Reactor);
	}

	public ColorObject yellowWall() {
		return objects.get(Type.YellowWall);
	}

	public void draw(int number) {
		Mat im = new Mat(700, 700, CvType.CV_8UC3);
		byte[] whit = { (byte) 0, (byte) 0, (byte) 0 };
		for (int i = 0; i < 700; i++) {
			for (int j = 0; j < 700; j++) {
				im.put(i, j, whit);
			}
		}
		byte[] white = { (byte) 255, (byte) 255, (byte) 255 };
		for (Point2D point : wallPoints) {
			System.out.println((int) (point.x()) + " "
					+ ((int) (point.y()) + 350));
			im.put((int) (point.x()), (int) (point.y()) + 350, white);
		}
		Highgui.imwrite("resources/field/test/detector" + number + ".jpg", im);
	}
}
