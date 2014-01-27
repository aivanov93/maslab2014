package vision.detector;

import com.seisw.util.geom.Rectangle2D;

import math.geom2d.Point2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;


public class Carrot {

	public static Point2D getCarrot(StraightLine2D wall, double dist, double wallDist){
		
		StraightLine2D perpendicular=StraightLine2D.createPerpendicular(wall, new Point2D(0, 0));
		Point2D intersection=wall.intersection(perpendicular);
		double k=wall.horizontalAngle();
		SimplePolygon2D rect=Polygons2D.createOrientedRectangle(intersection, dist*2, wallDist*2,	k);
		return rect.vertex(1);
	}
	
	
}
