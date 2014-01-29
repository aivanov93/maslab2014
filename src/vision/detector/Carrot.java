package vision.detector;

import com.seisw.util.geom.Rectangle2D;

import math.geom2d.Point2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;
import math.geom2d.transform.LineProjection2D;


public class Carrot {

	public static Point2D getCarrot(StraightLine2D wall, double dist, double wallDist, boolean onLeft){
		

		double k=wall.horizontalAngle();
		StraightLine2D perpendicular=new StraightLine2D(new Point2D(0,0), k+Math.PI/2);
		Point2D intersection=wall.intersection(perpendicular);
		if (intersection!=null){
			SimplePolygon2D rect=Polygons2D.createOrientedRectangle(intersection, dist*2, wallDist*2,	k);
			if (onLeft) return rect.vertex(1);
			else return rect.vertex(3);
		} else {
			return null;
		}
        	
		
	}
	
public static Point2D getAlligningCarrot(StraightLine2D wall, Point2D center, double dist, double wallDist, boolean onLeft){
		
		double k=wall.horizontalAngle();
		SimplePolygon2D rect=Polygons2D.createOrientedRectangle(center, wallDist*2, dist*2,	k);
//		System.out.println(rect.vertex(0)+" "+rect.vertex(1)+" "+rect.vertex(2)+" "+rect.vertex(3));
		return rect.vertex(0);
	}


public static void main(String[] args){
	StraightLine2D line2d=new StraightLine2D(new Point2D(0, 10), new Point2D(30, 40));
	getAlligningCarrot(line2d, new Point2D(20, 30), 10, 20, true);
	
}
	
	
}
