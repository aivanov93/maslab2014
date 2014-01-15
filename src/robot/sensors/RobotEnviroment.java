package robot.sensors;

import vision.detector.VisionDetector;
import game.StateMachine.State;

public interface RobotEnviroment {
	
	/**
	 * update the IR readings
	 * @param irs
	 */
	public void updateReadings(IRSensors irs);
	
	/**
	 * updates the camera state.. should probably use image processor from vision
	 * @param detector
	 */
	public void updateCamera(VisionDetector detector);
	
	/**
	 * no idea how all these will work at the moment
	 * @return
	 */
	public int redBallsInside();
	public int greenBallsInside();
	public int ballsCollected();
	
	/**
	 * speed and agular speed are from 0.0 to 1 so it just represents the coefficient from the maximum speed
	 * @param speed
	 * @param angularSpeed
	 */
	public void move(double speed, double angularSpeed);
	
	/**
	 * only used by map.. shouldn't care for a real robot
	 * @param state
	 */
	public void setState(State state);
	
	public void collectSilo();
	
	public void dumpRedBalls(int n);
	
	public void dumpGreenBallsTop(int n);
	
	public void dumpGreenBallsBottom(int n);
	
	/**
	 * odometry
	 * @return
	 */
	public double distanceMoved();
	
	public double angleMoved();
	
}
