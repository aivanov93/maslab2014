package vision;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.MathContext;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Moments;
import org.opencv.core.MatOfPoint;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Scalar;

import vision.detector.VisionDetector;

public class NewImageProcessor {

	private static Scalar redLeft1 = new Scalar(170, 100, 100);
	private static Scalar redRight1 = new Scalar(180, 256, 256);

	private static Scalar redLeft2 = new Scalar(0, 100, 100);
	private static Scalar redRight2 = new Scalar(12, 256, 256);

	private static Scalar greenLeft = new Scalar(43, 100, 50);
	private static Scalar greenRight = new Scalar(90, 256, 230);

	private static Scalar blueLeft = new Scalar(85, 60, 75);
	private static Scalar blueRight = new Scalar(120, 256, 256);

	private static Scalar yellowLeft = new Scalar(25, 80, 120);
	private static Scalar yellowRight = new Scalar(35, 256, 256);

	private static Scalar cyanLeft = new Scalar(85, 60, 150);
	private static Scalar cyanRight = new Scalar(95, 256, 256);

	private static Scalar purpleLeft = new Scalar(119, 70, 80);
	private static Scalar purpleRight = new Scalar(160, 256, 256);

	private static double minimalArea = 50;
	private static Size picSize = new Size(640, 480);
	Mat hierarchy, edges;
	Mat imSlave, imThresholded, imT2;// ,imT;
	List<MatOfPoint> contours;
	Scalar rangeLeft, rangeRight;

	MatOfPoint[] blueContours = new MatOfPoint[100];
	MatOfPoint[] yellowContours = new MatOfPoint[100];
	int numBlue, numYellow;

	double[] data;
	int x, y;

	int[] wallHeight = new int[(int) picSize.width];
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	boolean log, logTime;
	int testnumber;

	/**
	 * Constructor log and testnumber for debugging purposes
	 * 
	 * @param log
	 * @param testnumber
	 */
	public NewImageProcessor(boolean log, boolean logTime, int testnumber) {
		this.log = log;
		this.logTime = logTime;
		this.testnumber = testnumber;
		imSlave = new Mat();
		edges = new Mat();
		imThresholded = new Mat(picSize, CvType.CV_8UC3);
		hierarchy = new Mat();
		contours = new ArrayList<MatOfPoint>();
		// imT = new Mat();
		imT2 = new Mat();
		// imT=new Mat(picSize, CvType.CV_8UC1);
		imT2 = new Mat(picSize, CvType.CV_8UC1);

	}

	/**
	 * resamples the image to a new size
	 * 
	 * @param source
	 *            the source image
	 * @param newSize
	 *            the new size
	 * @return the new image
	 */
	private void resample(Mat source) {
		Imgproc.resize(source, source, picSize, 0, 0, Imgproc.INTER_NEAREST);
		// blur(source);
	}

	private void blur(Mat source) {
		Imgproc.GaussianBlur(source, source, new Size(5, 5), 1);

	}

	/**
	 * Returns an image thresholded in the given color
	 * 
	 * @param imgHSV
	 *            image to threshold
	 * @return
	 */
	private void getColor(Mat imgHSV, Color color) {

		rangeLeft = new Scalar(0, 0, 0);
		rangeRight = new Scalar(0, 0, 0);

		if (color.equals(Color.red)) {
			rangeLeft = redLeft1;
			rangeRight = redRight1;
		} else if (color.equals(Color.green)) {
			rangeLeft = greenLeft;
			rangeRight = greenRight;
		} else if (color.equals(Color.blue)) {
			rangeLeft = blueLeft;
			rangeRight = blueRight;
		} else if (color.equals(Color.yellow)) {
			rangeLeft = yellowLeft;
			rangeRight = yellowRight;
		} else if (color.equals(Color.cyan)) {
			rangeLeft = cyanLeft;
			rangeRight = cyanRight;
		} else if (color.equals(Color.pink)) {
			rangeLeft = purpleLeft;
			rangeRight = purpleRight;
		}
		Mat imT = imThresholded;

		Core.inRange(imgHSV, rangeLeft, rangeRight, imT);

		if (color == Color.red) {
			Core.inRange(imgHSV, redLeft2, redRight2, imT2);
			Core.bitwise_or(imT, imT2, imT);
		}

	}

	/**
	 * Find two largest contours in the image
	 * 
	 * @param imRed
	 *            source image
	 * @param toEdit
	 * @param testnumber
	 * @return a pair of contours
	 */
	private void findBalls(VisionDetector detector, Mat image, Color color) {
		getColor(image, color);
		// find the contours of all white spots
		contours.clear();
		Imgproc.findContours(imThresholded, contours, hierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

		// find all color objects below the blue line and consider them balls
		for (int i = 0; i < contours.size(); i++) {
			// get area
			double area = Imgproc.contourArea(contours.get(i));

			if (area > 35) { // if the objects has a reasonable size
				Moments m1 = Imgproc.moments(contours.get(i), false);
				int cx = (int) (m1.get_m10() / m1.get_m00());
				int cy = (int) (m1.get_m01() / m1.get_m00());
				if (cy > wallHeight[cx]) {

					detector.sawBall(color, cx, cy);
					// debug data
					if (log) {
						Imgproc.drawContours(imSlave, contours, i, new Scalar(
								0, 130, 130), 3);
					}
				}
			}
		}

		if (log) {
			Highgui.imwrite("resources/field/test/" + testnumber + ".jpg",
					imSlave);
		}

	}

	/**
	 * Find two largest contours in the image
	 * 
	 * @param imRed
	 *            source image
	 * @param toEdit
	 * @param testnumber
	 * @return a pair of contours
	 */
	private void findGoals(VisionDetector detector, Mat image, Color color) {
		getColor(image, color);
		// find the contours of all white spots
		contours.clear();
		Imgproc.findContours(imThresholded, contours, hierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

		// find all color objects below the blue line and consider them balls
		for (int i = 0; i < contours.size(); i++) {
			// get area
			double area = Imgproc.contourArea(contours.get(i));
			
			if (area > 90) { // if the objects has a reasonable size
				Moments m1 = Imgproc.moments(contours.get(i), false);
				if (color.equals(Color.pink)){
					for (int j = 0; j < contours.get(i).size().height; j++) {
						double[] data = contours.get(i).get(j, 0);
						x = (int) data[0];
						y = (int) data[1];
						if (wallHeight[x] > y || wallHeight[x]==0) {
							wallHeight[x] = y;
						}
					}
				}
				int cx = (int) (m1.get_m10() / m1.get_m00());
				int cy = (int) (m1.get_m01() / m1.get_m00());
				if ((color.equals(Color.cyan) && cy < wallHeight[cx])
						|| color.equals(Color.pink)) {
					detector.sawRectangle(color, cx, cy);
					// debug data
					if (log) {
						Imgproc.drawContours(imSlave, contours, i, new Scalar(
								0, 255,100), 5);
					}
				}
			}
		}

		if (log) {
			Highgui.imwrite("resources/field/test/" + testnumber + ".jpg",
					imSlave);
		}

	}

	private void findWallContours(Mat imgHSV, Color color) {
		// Imgproc.Canny(imgHSV, edges, 100, 300);
		getColor(imgHSV, color);
		// find the contours of all white spots
		contours.clear();
		Imgproc.findContours(imThresholded, contours, hierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

		if (color.equals(Color.blue) || color.equals(Color.white)) {
			numBlue = 0;
		} else {
			numYellow = 0;
		}

		// find the two largest contours
		for (int i = 0; i < contours.size(); i++) {
			double area = Imgproc.contourArea(contours.get(i));
			if (area > 55) {
				if (color.equals(Color.blue) || color.equals(Color.white)) {
					blueContours[numBlue] = contours.get(i);
					numBlue++;
				} else {
					yellowContours[numYellow] = contours.get(i);
					numYellow++;
				}
			}
		}

		if (log) {
			Imgproc.drawContours(imSlave, contours, -1,
					new Scalar(0, 130, 130), 1);
			Highgui.imwrite("resources/field/test/bluewalls" + testnumber
					+ ".jpg", imSlave);
		}

	}

	private void analyzeWalls() {
		for (int i = 0; i < numBlue; i++) {
			for (int j = 0; j < blueContours[i].size().height; j++) {
				double[] data = blueContours[i].get(j, 0);
				x = (int) data[0];
				y = (int) data[1];
				if (wallHeight[x] < y) {
					wallHeight[x] = y;
				}
			}
		}

		for (int i = 0; i < numYellow; i++) {
			for (int j = 0; j < yellowContours[i].size().height; j++) {
				double[] data = yellowContours[i].get(j, 0);
				x = (int) data[0];
				y = (int) data[1];
				if (wallHeight[x] < y)
					wallHeight[x] = y;
			}
		}
		if (log) {
			byte[] whit = { (byte) 0, (byte) 0, (byte) 0 };

			byte[] white = { (byte) 0, (byte) 0, (byte) 255 };
			for (int i = 0; i < wallHeight.length; i++) {
				imSlave.put(wallHeight[i], i, white);
			}
			Highgui.imwrite("resources/field/walls" + testnumber + ".jpg",
					imSlave);
		}
	}

	public void findWalls(Mat rawImage) {
		getColor(rawImage, Color.blue);
	}

	public void process(Mat rawImage, VisionDetector detector) {
		for (int i = 0; i < picSize.width; i++) {
			wallHeight[i] = 0;
		}
		Timer timer = new Timer();

		if (logTime) {
			timer.start();
			timer.start();

		}

		resample(rawImage);

		if (log) {
			rawImage.copyTo(imSlave);
		}

		blur(rawImage);

		Imgproc.cvtColor(rawImage, rawImage, Imgproc.COLOR_BGR2HSV);

		if (logTime) {
			timer.print("HSV conversion ");
			timer.start();
		}
		// ***********************
		// *****wall contours*****
		// ************************
		findGoals(detector, rawImage, Color.pink);
		
		findWallContours(rawImage, Color.blue);
		findWallContours(rawImage, Color.yellow);
		analyzeWalls();
		detector.foundWalls(wallHeight);
		findGoals(detector, rawImage, Color.cyan);
		if (logTime) {
			timer.print("walls ");
			timer.start();
		}

		// ********************************
		// *******balls********************
		// *******************************

		findBalls(detector, rawImage, Color.red);
		findBalls(detector, rawImage, Color.green);

		if (logTime) {
			timer.print("balls");
			timer.print("everything");
		}

	}

	public static void main(String[] args) {
		for (int i = 1; i < 7; i++) {
			Mat im = Highgui.imread("resources/field/test" + i + ".png");
			System.out.println(im.size().height);
			NewImageProcessor proc = new NewImageProcessor(true, true, i);
			VisionDetector detector = new VisionDetector();
			proc.process(im, detector);
			detector.draw(i);
		}
	}
}
