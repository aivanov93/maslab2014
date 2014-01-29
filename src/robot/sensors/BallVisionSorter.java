package robot.sensors;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import vision.BallProcessor;
import vision.NewImageProcessor;
import vision.detector.VisionDetector;

public class BallVisionSorter implements Runnable {

	private BallCounter balls;
	private RobotEnviroment hardware;
 private Color color;
	public BallVisionSorter(BallCounter balls, RobotEnviroment hardware) {
		this.balls = balls;
		this.hardware = hardware;
	}

	@Override
	public void run() {
		// Load the OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Setup the camera
		VideoCapture camera = new VideoCapture();
		camera.open(0);
		BallProcessor processor = new BallProcessor(true, false);
		// detector.
		// Main loop
		Mat rawImage = new Mat();
		while (true) {
			// Wait until the camera has a new frame
			while (!camera.read(rawImage)) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			color=processor.process(rawImage);
			balls.gotBall(color);
			if (color.equals(Color.red)){
				hardware.sortRed();
			} else if (color.equals(Color.green)){
				hardware.sortGreen();
			}
		}
	}

}
