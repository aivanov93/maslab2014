package global;

import java.util.Arrays;
import java.util.List;

/**
 * Various constants
 *
 */
public class Constants {
	/**
	 * Time constants
	 */
	public static int clock=30; // ms
	public static int totalTime=180; //s
	
	/**
	 * Robot size related constants
	 */
	public static double robotRadius=20.32;

	public static double wheelBase=34.29;
	public static double wheelRadius=4.92125;
	
	/**
	 * sensor related constants
	 */
	public static int numberOfIRs=2;
	public static List<Double> irDirections=Arrays.asList(Math.PI/2, Math.PI/3, 0.0, -Math.PI/3, -Math.PI/2);//, -Math.PI*3/4, -Math.PI, Math.PI*3/4);
	public static double minIRreading=3;
	public static double maxIRreading=100;
	public static double sideIRspacing=40;
	public static double readingSTD=2;
	

	
	/**
	 * walls related constants
	 */
	
	public static double safeDistanceToWall=25;
	
	/**
	 * for localization
	 */
	public static int numberOfParticles=500;
	public static double distanceUncertainty=25*2;
	public static double angleUncertainty=Math.PI/4*2;
	
	public static double motionSTD=10;
	public static double rotationSTD=Math.PI/60;
	
	public static double closeEnough=12;
	/**
	 * some step constants
	 */
	public static int allowedToMiss=5;
	public static int stepsForCatchingBall=80;
	public static int stepsForSiloArm=300;	
	public static double minDistanceToWall=5;
	
	public static double minDistanceToSilo=80;
	public static double minDistanceToBall=5;	
	public static double distanceForCatchingBall=30;
		
	public static double siloBallDistance=25;
	

	public static double formatAngle(double angle){
//		System.out.println("converting "+ angle);
		angle%=Math.PI*2;
		if (angle>Math.PI) angle-=Math.PI*2;
		if (angle<-Math.PI) angle+=Math.PI*2;
//		System.out.println("returning "+angle);
		return angle;
	}
	
	/**
	 * vision related constants
	 */
	
	public static int picWidth=640;
	
}
