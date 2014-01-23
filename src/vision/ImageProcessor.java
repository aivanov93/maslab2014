package vision;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
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

import Core.FilterOp;

import vision.detector.VisionDetector;

public class ImageProcessor {

	private static Scalar redLeft1 = new Scalar(170, 160, 100);
	private static Scalar redRight1 = new Scalar(180, 256, 230);

	private static Scalar redLeft2 = new Scalar(0, 160, 100);
	private static Scalar redRight2 = new Scalar(8, 256, 230);

	private static Scalar greenLeft = new Scalar(43, 100, 50);
	private static Scalar greenRight = new Scalar(90, 256, 230);

	private static Scalar blueLeft = new Scalar(105, 150, 100);
	private static Scalar blueRight = new Scalar(125, 256, 236);

	private static Scalar yellowLeft = new Scalar(25, 110, 120);
	private static Scalar yellowRight = new Scalar(35, 256, 256);
	
	private static double minimalArea = 50;
	private static Size picSize = new Size(320, 240);
	Mat hierarchy, edges;
	Mat imSlave, imRed, imGreen, imT2;// ,imT;
	List<MatOfPoint> contours;
	Scalar rangeLeft, rangeRight;
	FilterOp edge;
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	boolean log;
	int testnumber;

	/**
	 * Constructor log and testnumber for debugging purposes
	 * 
	 * @param log
	 * @param testnumber
	 */
	public ImageProcessor(boolean log, int testnumber) {
		this.log = log;
		this.testnumber = testnumber;
		imSlave = new Mat();
		edges = new Mat();
		imRed = new Mat(picSize, CvType.CV_8UC3);
		imGreen = new Mat();
		hierarchy = new Mat();
		contours = new ArrayList<MatOfPoint>();
		// imT = new Mat();
		imT2 = new Mat();
		// imT=new Mat(picSize, CvType.CV_8UC1);
		imT2 = new Mat(picSize, CvType.CV_8UC1);
		edge = new FilterOp("hedge");
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
			rangeLeft = greenLeft;
			rangeRight = greenRight;
		} else if (color.equals(Color.yellow)){
			rangeLeft = greenLeft;
			rangeRight = greenRight;
		}
		Mat imT = imRed;
		if (color.equals(Color.green)) {
			imT = imGreen;
		}

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
	private Contour[] findTwoLargestContours(Mat image, Mat toEdit) {

		// find the contours of all white spots
		contours.clear();
		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_NONE);

		// checking if any contours found
		if (contours.size() == 0) {
			Contour[] contourPair = new Contour[0];
			return contourPair;
		}

		// find the two largest contours
		double largestArea = 0, secondLargestArea = 0;
		int largestIndex = 0, secondLargestIndex = 0;
		for (int i = 0; i < contours.size(); i++) {
			double area = Imgproc.contourArea(contours.get(i));
			if (area > largestArea) {
				secondLargestArea = largestArea;
				secondLargestIndex = largestIndex;
				largestArea = area;
				largestIndex = i;
			} else if (area > secondLargestArea) {
				secondLargestArea = area;
				secondLargestIndex = i;
			}
		}

		if (log) {
			Imgproc.drawContours(toEdit, contours, -1, new Scalar(0, 130, 130),
					1);
			Imgproc.drawContours(toEdit, contours, largestIndex, new Scalar(0,
					255, 0), 1);
			Imgproc.drawContours(toEdit, contours, secondLargestIndex,
					new Scalar(0, 255, 0), 1);
		}

		// Contour is a self defined class.. see Contour.java
		Contour[] contourPair = new Contour[2];
		if (largestIndex == secondLargestIndex) {
			contourPair = new Contour[1];
			contourPair[0] = new Contour(contours.get(largestIndex));
		} else {
			contourPair[0] = new Contour(contours.get(largestIndex));
			contourPair[1] = new Contour(contours.get(secondLargestIndex));
		}
		return contourPair;
	}

	private void analyzeContours(Contour[] contours, Color color,
			VisionDetector detector, Mat toEdit) {

		boolean sawLabel = false;
		boolean sawBall = false;

		for (int i = 0; i < contours.length; i++) {
			contours[i].printPr();

			if (contours[i].isRect() && !sawLabel) {
				detector.sawRectangle(color, contours[i].center().x,
						contours[i].center().y);
				System.out.println("sawLabel " + color.toString());

				sawLabel = true;
			} else if (contours[i].isEll() && !sawBall) {
				detector.sawBall(color, contours[i].center().x,
						contours[i].center().y);
				System.out.println("sawBall " + color.toString());
				sawBall = true;
			} else if (contours[i].isSomethingBig() && !sawBall) {
				detector.sawBall(color, contours[i].center().x,
						contours[i].center().y);
				System.out.println("sawBall " + color.toString());
				sawBall = true;
			}

			if (log) {
				contours[i].drawR(toEdit);
				byte[] data = { (byte) 255, (byte) 0, (byte) 0 };
				for (int k = -2; k < 3; k++) {
					for (int j = -2; j < 3; j++) {
						toEdit.put((int) contours[i].center().x + j,
								(int) contours[i].center().y + k, data);
					}
				}
			}

		}

		if (log) {
			Highgui.imwrite("resources/centroids" + testnumber + ".png", toEdit);
		}

	}
	
	public void findWalls(Mat rawImage){
		getColor(rawImage,Color.blue);
	}

	public void prrftocess(Mat rawImage, VisionDetector detector) {
		Timer timer = new Timer();
		timer.start();
		timer.start();

		if (log) {
			rawImage.copyTo(imSlave);
		}
		resample(rawImage);
		blur(rawImage);
		Imgproc.cvtColor(rawImage, rawImage, Imgproc.COLOR_BGR2HSV);
		
		if (log) {
			Highgui.imwrite("resources/edges" + testnumber + ".png", edges);
		}
		timer.print("HSV conversion ");
		timer.start();

		// extract thresholded colors

		/*
		 * getColor(rawImage, Color.red); getColor(rawImage, Color.green);
		 */

		BufferedImage image = Mat2Image.getImage(rawImage);
		timer.print("thresholding ");
		timer.start();

		if (log) {
			Highgui.imwrite("resources/filtered" + testnumber + ".png", imRed);
			// Highgui.imwrite("resources/greenfiltered" + testnumber + ".png",
			// imGreen);
		}

		// find and analyze contours for red
		Contour[] contours = findTwoLargestContours(imRed, imSlave);
		analyzeContours(contours, Color.red, detector, imSlave);

		// find and analyze contours for green
		contours = findTwoLargestContours(imGreen, imSlave);
		analyzeContours(contours, Color.green, detector, imSlave);

		timer.print("contours");
		timer.print("everything");

	}

	public void process(Mat rawImage, VisionDetector detector) {
		Timer timer = new Timer();
		timer.start();
		

		if (log) {
			rawImage.copyTo(imSlave);
		}
		resample(rawImage);
		blur(rawImage);
		BufferedImage image = Mat2Image.getImage(rawImage);

		timer.print("OpenCV  ");
		
		
		edge.apply(image);	
		
		timer.start();
		edge.apply();
		timer.print("shader ");
		BufferedImage edges = FilterOp.getImage();
		//BufferedImage colors = FilterOp.getImage();
		
	}

	public static void main(String[] args){
		  Mat im=Highgui.imread("resources/field/image4.png");
		  ImageProcessor proc=new ImageProcessor(true,1);
		  VisionDetector detector=new VisionDetector();
		  proc.process(im, detector);
		  
	}
}
