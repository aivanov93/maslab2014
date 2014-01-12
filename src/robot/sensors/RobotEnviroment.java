package robot.sensors;

import vision.detector.VisionDetector;

public interface RobotEnviroment {
	
	public void updateReadings(double[] irs);
	
	public void updateCamera(VisionDetector detector);
	
	public int redBallsCollected();
	
	public int greenBallsCollected();
	
	public void move(double speed, double angularSpeed);
	
}
