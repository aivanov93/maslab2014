package vision;

import java.util.concurrent.atomic.AtomicBoolean;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import vision.detector.VisionDetector;

public class VisionRunnable implements Runnable {

	private VisionDetector detector;
	private AtomicBoolean oldInformationConsumed;
	private AtomicBoolean newInformationAvailable;

	public VisionRunnable(VisionDetector detector,
			AtomicBoolean oldInformationConsumed,
			AtomicBoolean newInformationAvailable) {
		this.detector = detector;
		this.oldInformationConsumed = oldInformationConsumed;
		this.newInformationAvailable = newInformationAvailable;
	}

	@Override
	public void run() {
		// Load the OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Setup the camera
		VideoCapture camera = new VideoCapture();
		camera.open(1);
		NewImageProcessor processor = new NewImageProcessor(true, 2);
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
			// check if the  state machine consumed the new information
			if (oldInformationConsumed.compareAndSet(true, false)) { 
				// Process the image
				processor.process(rawImage, detector);
				newInformationAvailable.getAndSet(true); // announce
			}
		}

	}
}
