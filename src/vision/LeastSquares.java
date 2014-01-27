package vision;

import java.util.List;

import math.geom2d.Point2D;
import math.geom2d.line.Line2D;
import math.geom2d.line.StraightLine2D;


public class LeastSquares { 

    public static StraightLine2D fitLine(List<Point2D> points) {   
        

        // first pass: read in data, compute xbar and ybar
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (Point2D point:points) {           
            sumx  += point.x();
            sumx2 += point.x() * point.x();
            sumy  += point.y();            
        }
        double xbar = sumx / points.size();
        double ybar = sumy / points.size();

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (Point2D point:points) {
            xxbar += (point.x() - xbar) * (point.x() - xbar);
            yybar += (point.y() - ybar) * (point.y() - ybar);
            xybar += (point.x() - xbar) * (point.y() - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;
        Point2D one=new Point2D(1, beta1+beta0);
        Point2D two=new Point2D(2, 2*beta1+beta0);
        return new StraightLine2D(one, two);
        
    }
}