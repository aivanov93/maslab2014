package vision.detector;

import java.util.Iterator;

import vision.detector.ColorObject.Type;

import com.seisw.util.geom.Rectangle2D;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;
import math.geom2d.transform.LineProjection2D;

public class Carrot {

	public static Point2D getCarrot(StraightLine2D wall, double dist,
			double wallDist, boolean onLeft) {

		double k = wall.horizontalAngle();
		Point2D intersection = wall.projectedPoint(0, 0);
		if (intersection != null) {
			SimplePolygon2D rect = Polygons2D.createOrientedRectangle(
					intersection, dist * 2, wallDist * 2, k);
			if (onLeft)
				return rect.vertex(1);
			else
				return rect.vertex(3);
		} else {
			return null;
		}

	}

	public static Point2D getAlligningCarrot(Type type, StraightLine2D wall,
			Point2D center, double dist, double wallDist, boolean onLeft) {

		double k = wall.horizontalAngle();
		SimplePolygon2D rect = Polygons2D.createOrientedRectangle(center,
				dist * 2, wallDist * 2, k);
//		System.out.println(rect.vertex(0) + " " + rect.vertex(1) + " "
//				+ rect.vertex(2) + " " + rect.vertex(3));

		if (type == Type.Reactor) {
			if (onLeft)
				return rect.vertex(0);
			else
				return rect.vertex(2);
		} else {
			if (onLeft)
				return rect.vertex(1);
			else
				return rect.vertex(3);
		}
	}

	public static Point2D getCarrotOnGoal(Type type, StraightLine2D wall,
			Point2D center, double dist, double wallDist, boolean onLeft) {

		double k = wall.horizontalAngle();
		SimplePolygon2D rect = Polygons2D.createOrientedRectangle(center,
				dist * 2, wallDist * 2, k);

		Point2D p1 = null, p2 = null;
		// System.out.println(rect.vertex(0)+" "+rect.vertex(1)+" "+rect.vertex(2)+" "+rect.vertex(3));
		for (int i = 0; i < 4; i++) {
			LineSegment2D line = new LineSegment2D(rect.vertex(i),
					rect.vertex((i + 1) % 4));
			Point2D p = line.intersection(wall);
			if (p != null) {
				if (p1 == null) {
					p1 = p;
				} else
					p2 = p;
			}
		}
		Point2D lower, higher;

		if (p1.y() > p2.y()) {
			higher = p1;
			lower = p2;
		} else {
			higher = p2;
			lower = p1;
		}
		if (type == Type.Reactor) {
			return higher;
		} else {
			return lower;
		}

	}

	public static void main(String[] args) {
		StraightLine2D line2d = new StraightLine2D(new Point2D(0, 10),
				new Point2D(30, 40));

	}

}
