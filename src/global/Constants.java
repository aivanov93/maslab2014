package global;

import java.util.Arrays;
import java.util.List;

public class Constants {
	public static int clock=250; // ms
	public static int totalTime=5*60; //s
	
	public static double robotWidth=40;
	public static double robotLength=40;

	
	/**
	 * sensor related constants
	 */
	public static int numberOfIRs=6;
	public static List<Double> irDirections=Arrays.asList(Math.PI/4, 0.0, -Math.PI/4, -Math.PI/2, Math.PI, Math.PI/2);
	public static double minIRreading=10;
	public static double maxIRreading=120;
	public static double sideIRspacing=40;
	
	public static double angleBetweenTopIrs=Math.atan(robotLength/robotWidth);
	
	/**
	 * walls related constants
	 */
	
	public static double safeDistanceToWall=40;
	
	/**
	 * for localization
	 */
	public static int numberOfParticles;
	public static double distanceUncertainty=40*2;
	public static double angleUncertainty=Math.PI/4*2;
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
	

	public static void formatAngle(double angle){
		if (angle>Math.PI) angle-=Math.PI*2;
		if (angle<-Math.PI) angle+=Math.PI*2;
	}
	
	
}
