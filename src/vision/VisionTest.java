package vision;
import static org.junit.Assert.*;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import org.opencv.core.Core;

import vision.detector.VisionDetector;


public class VisionTest {
	
	private static boolean log=true;
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
 
	
	@Test
	public void twoBallsSilo(){
	  Mat im=Highgui.imread("resources/field/image4.png");
	  ImageProcessor proc=new ImageProcessor(log,1);
	  VisionDetector detector=new VisionDetector();
	  proc.process(im, detector);
	  assertTrue(detector.seesRedBall());
	  assertTrue(detector.seesSilo());
	}
	
	@Test
	public void twoBallsOverlapAndSilo(){
	  Mat im=Highgui.imread("resources/field/image2.png");
	  ImageProcessor proc=new ImageProcessor(log,2);
	  VisionDetector detector=new VisionDetector();
	  proc.process(im, detector);
	  assertTrue(detector.seesRedBall());
	  assertTrue(detector.seesSilo());
	}
	
		
	@Test
	public void GreenRedBallSilo(){
	  Mat im=Highgui.imread("resources/field/image3.png");
	  ImageProcessor proc=new ImageProcessor(log,4);
	  VisionDetector detector=new VisionDetector();
	  proc.process(im, detector);
	  assertTrue(detector.seesRedBall());
	  assertTrue(detector.seesSilo());
	  assertTrue(detector.seesGreenBall());
	  assertFalse(detector.seesReactor());
	}
	
	@Test
	public void GreenAndRedCorner(){
	  Mat im=Highgui.imread("resources/5.png");
	  ImageProcessor proc=new ImageProcessor(log,5);
	  VisionDetector detector=new VisionDetector();
	  proc.process(im, detector);
	  assertTrue(detector.seesRedBall());
	  assertFalse(detector.seesSilo());
	  assertTrue(detector.seesRedBall());
	  assertFalse(detector.seesReactor());
	}
	
	@Test
	public void redCloseGreenBehind(){
	  Mat im=Highgui.imread("resources/6.png");
	  ImageProcessor proc=new ImageProcessor(log,6);
	  VisionDetector detector=new VisionDetector();
	  proc.process(im, detector);
	  assertTrue(detector.seesRedBall());
	  assertFalse(detector.seesSilo());
	  assertTrue(detector.seesGreenBall());
	  assertFalse(detector.seesReactor());
	}
	
	@Test
	public void greenNearSilo(){
	  Mat im=Highgui.imread("resources/7.png");
	  ImageProcessor proc=new ImageProcessor(log,7);
	  VisionDetector detector=new VisionDetector();
	  proc.process(im, detector);
	  assertFalse(detector.seesRedBall());
	  assertTrue(detector.seesSilo());
	  assertTrue(detector.seesGreenBall());
	}
}
