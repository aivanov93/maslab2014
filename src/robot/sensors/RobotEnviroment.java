package robot.sensors;

import vision.detector.VisionDetector;
import game.StateMachine.State;

public interface RobotEnviroment {
	
	public void updateReadings(double[] irs);
	
	public void updateCamera(VisionDetector detector);
	
	public int redBallsInside();
	
	public int greenBallsInside();
	public int ballsCollected();
	
	public void move(double speed, double angularSpeed);
	
	public void setState(State state);
	
	public void collectSilo();
	
	public void dumpRedBalls(int n);
	
	public void dumpGreenBallsTop(int n);
	
	public void dumpGreenBallsBottom(int n);
	
	public double distanceMoved();
	
	public double angleMoved();
	
}
