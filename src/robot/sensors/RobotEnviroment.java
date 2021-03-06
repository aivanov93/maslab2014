package robot.sensors;

import java.awt.Graphics2D;

import robot.map.Position;



import vision.detector.VisionDetector;
import game.StateMachine.State;

public interface RobotEnviroment {
	
	/**
	 * update the IR readings
	 * @param irs
	 */
	public void updateReadings(RangeSensors irs);
	
	/**
	 * updates the camera state.. should probably use image processor from vision
	 * @param detector
	 */
	public void updateCamera(VisionDetector detector);
	

	public void sortRed();
	
	public void sortGreen();
	
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
	
	public void dumpGreenBall();
	
	public void prepareBottomDump();
	
	public void resetGreenDump();
	
	public boolean checkIfDumped();
	
	public void update();
	/**
	 * odometry
	 * @return
	 */
	public void updateOdometry(Odometry odometry);
	
	public void draw(Graphics2D g);
	
}
