package global;

import java.util.Arrays;
import java.util.List;

public class Constants {
	public static int clock=300; // ms
	public static int totalTime=30; //s
	
	public static double robotWidth=68;
	public static double robotLength=68;

	public static double wheelBase=30;
	public static double wheelRadius=10;
	
	/**
	 * sensor related constants
	 */
	public static int numberOfIRs=6;
	public static List<Double> irDirections=Arrays.asList(Math.PI/4, 0.0, -Math.PI/4, -Math.PI/2, Math.PI, Math.PI/2);
	public static double minIRreading=10;
	public static double maxIRreading=120;
	public static double sideIRspacing=40;
	public static double readingSTD=2;
	
	public static double angleBetweenTopIrs=Math.atan(robotLength/robotWidth);
	
	/**
	 * walls related constants
	 */
	
	public static double safeDistanceToWall=68;
	
	/**
	 * for localization
	 */
	public static int numberOfParticles=1000;
	public static double distanceUncertainty=40*2;
	public static double angleUncertainty=Math.PI/4*2;
	
	public static double motionSTD=10;
	public static double rotationSTD=Math.PI/60;
	
	public static double closeEnough=20;
	/**
	 * some step constants
	 */
	public static int allowedToMiss=5;
	public static int stepsForCatchingBall=10;
	public static int stepsForSiloArm=300;	
	
	public static double minDistanceToSilo=80;
	public static double minDistanceToBall=10;	
	public static double distanceForCatchingBall=30;
		
	public static double siloBallDistance=25;
	

	public static double formatAngle(double angle){
		if (angle>Math.PI) angle-=Math.PI*2;
		if (angle<-Math.PI) angle+=Math.PI*2;
		return angle;
	}
	
	
}
