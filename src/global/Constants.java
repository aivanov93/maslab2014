package global;

public class Constants {
	public static int clock=250; // ms
	public static int totalTime=5*60; //s
	
	public static double robotWidth=40;
	public static double robotLenght=60;
	
	/**
	 * sensor related constants
	 */
	public static int numberOfIRs=8;
	public static double minIRreading=10;
	public static double maxIRreading=120;
	
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
	

	
	
}
