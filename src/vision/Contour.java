package vision;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;


public class Contour {
	private MatOfPoint original;
	private MatOfPoint2f conv2f=new MatOfPoint2f();
	private double area;
	private RotatedRect boundingRect;
	private float[] circleRadius=new float[5];
	private float circle;
	private double rectArea;
	private double ellArea;
	private boolean isNull=true;
	private boolean isRect=false;
	private boolean isEll=false;
	private double prRect,prEll;
	private Point center;
	
	public Contour(MatOfPoint c){
		if (c==null){
			isNull=true;
		} else {
			original=c;
			original.convertTo(conv2f, CvType.CV_32FC2);
			area=Imgproc.contourArea(original);
			boundingRect = Imgproc.minAreaRect(conv2f);
			rectArea=boundingRect.size.area();
			
			Imgproc.minEnclosingCircle(conv2f, new Point(), circleRadius);
			circle=circleRadius[0];
			
			ellArea=circle*circle*Math.PI;
			
			prRect=area/rectArea;
			prEll=area/ellArea;
			
			
			Moments m1 = Imgproc.moments(original, false);			
			double cx,cy;
			cx =  (m1.get_m10() / m1.get_m00());
			cy =  (m1.get_m01() / m1.get_m00());
			center=new Point(cy,cx);
		}
	}
	
	public double area(){
		return area;
	}
	
	public double rectArea(){
		return rectArea;
	}
	
	public double ellArea(){
		return ellArea;
	}
	
	public boolean isRect(){
		return prRect>0.7;
	}
	
	public boolean isEll(){
		return prRect<=0.84;
	}
	
	public Point center(){
		return center;
	}
	
	public boolean isSomethingBig(){
		return area>50;
	}
	
	public void printPr(){
		System.out.println("prob ellipse " + prEll+ ", prob rect "+prRect);
	}
	
	public void drawR(Mat im){
		 Point[] rect_points=new Point[4]; boundingRect.points( rect_points );
		 for( int j = 0; j < 4; j++ ){			 
	          Core.line( im, rect_points[j], rect_points[(j+1)%4], new Scalar(255,255,0), 1);
	     }
	}
	

}
