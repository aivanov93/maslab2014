package robot.sensors;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
		PrintWriter writer=null;
		try {
			writer=new PrintWriter("resources/balllog.txt", "UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// Setup the camera
		while (true) {
			// Wait until the camera has a new frame
			double red=hardware.data.red();
			double green=hardware.data.green();
			
			if (red>400 && green>400){
				if (red-green>300){
					writer.write("red "+ red+" "+green+"\n");
					
					hardware.sortRed();
					balls.gotBall(Color.red);
				} else if (green-red>300){
					writer.write("green "+ red+" "+green+"\n");
					hardware.sortGreen();
					balls.gotBall(Color.green);
				}
				try {
					Thread.sleep(900);
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
