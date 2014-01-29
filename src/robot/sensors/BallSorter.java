package robot.sensors;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import vision.BallProcessor;
import vision.NewImageProcessor;
import vision.detector.VisionDetector;

public class BallSorter implements Runnable {

	private BallCounter balls;
	private RobotHardware hardware;
 private Color color;
 
	public BallSorter(BallCounter balls, RobotEnviroment hardware) {
		this.balls = balls;
		this.hardware = (RobotHardware) hardware;
	}

	@Override
	public void run() {
		// Load the OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Setup the camera
		while (true) {
			// Wait until the camera has a new frame
			double red=hardware.data.red();
			double green=hardware.data.green();
			//System.out.println(red+" "+green);
			
			if (red>500 && green>500){
				if (red-green>100){
					System.out.println(red+" "+green);
					
					hardware.sortRed();
					balls.gotBall(Color.red);
				} else if (green-red>100){
					System.out.println(red+" "+green);
					
					hardware.sortGreen();
					balls.gotBall(Color.green);
				}
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				hardware.resetSort();
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
