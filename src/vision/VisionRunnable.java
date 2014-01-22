package vision;



import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import vision.detector.VisionDetector;

public class VisionRunnable implements Runnable {

	private VisionDetector detector;

	public VisionRunnable(VisionDetector detector) {
		this.detector=detector;
	}

	@Override
	public void run() {
		// Load the OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Setup the camera
		VideoCapture camera = new VideoCapture();
		camera.open(1);
		ImageProcessor processor = new ImageProcessor(true, 2);

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

			// Process the image however you like

			synchronized(detector){
				processor.process(rawImage,detector);
			}
			try {
				Thread.sleep(60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
