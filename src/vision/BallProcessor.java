package vision;

import java.awt.Color;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class BallProcessor {
	
	private Mat cropped, in1,in2;
	private static Rect rect=new Rect(300,200,50,50);
	Scalar color;
	private static Scalar redLeft1 = new Scalar(170, 110, 100);
	private static Scalar redRight1 = new Scalar(180, 256, 230);

	private static Scalar redLeft2 = new Scalar(0, 110, 100);
	private static Scalar redRight2 = new Scalar(10, 256, 230);

	private static Scalar greenLeft = new Scalar(43, 100, 50);
	private static Scalar greenRight = new Scalar(90, 256, 230);

	private boolean logIm, logText;
	

	public BallProcessor(boolean logIm, boolean logText){
		cropped=new Mat(50,50,CvType.CV_8UC3);
		color=new Scalar(0, 0, 0);
		in1=new Mat(50,50,CvType.CV_8UC1);
		in2=new Mat(50,50,CvType.CV_8UC1);
	}
	
	private void blur(Mat source) {
		Imgproc.GaussianBlur(source, source, new Size(9, 9), 3);

	}
	
	public Color process(Mat image){
		cropped=new Mat(image,rect);
		blur(cropped);
		Imgproc.cvtColor(cropped, cropped, Imgproc.COLOR_BGR2HSV);
		Core.inRange(cropped, redLeft1, redRight1, in1);
		Core.inRange(cropped, redLeft2, redRight2, in2);
		Core.bitwise_or(in1, in2, in1);
		color=Core.mean(in1);
		if (color.val[0]>0.7){
			return Color.red;
		}
		
		Core.inRange(cropped, greenLeft, greenRight, in1);
		
		color=Core.mean(in1);
		if (color.val[0]>0.7){
			return Color.green;
		}
		return Color.black;
	}
}
