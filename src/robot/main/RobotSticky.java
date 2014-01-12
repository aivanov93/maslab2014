package robot.main;

import java.lang.Thread.State;

import fakerobot.RobotSimulator;
import fakerobot.SampleMaps;
import global.Constants;
import jssc.SerialPort;
import robot.moves.Driver;
import robot.sensors.RobotEnviroment;
import robot.sensors.Sensors;
import vision.detector.VisionDetector;

public class RobotSticky implements Robot {

	/**
	 * uses sensors to get readings from IRs
	 */
	private RobotEnviroment hardware;
	/**
	 * driver includes PID controllers and computes the required forward and
	 * angular speeds (unitless 0.0 to 1.0 with respect to max speed)
	 */
	private Driver driver;
	/**
	 * keeps all the readings from the camera
	 */
	private VisionDetector camera;

	/**
	 * flag to know whether it's a simulation or no
	 */
	private boolean real;

	/**
	 * 
	 */
	private State state;
	
	private double[] irs = new double[Constants.numberOfIRs];

	

	public RobotSticky(boolean real) {
		this.real = real;

		// open port if not simulation
		if (real) {
			

		} else {
			hardware = SampleMaps.createMap1();
		}

		// initialize sensors, driver and camera
		this.driver = new Driver(1.0, 0.0, 0.1, 1, 0.0, 0.1);
		this.camera = new VisionDetector();
	}

	public VisionDetector camera() {
		return camera;
	}

	public void update() {
		hardware.updateReadings(irs);
		hardware.updateCamera(camera);

	}

	public int redBallsInside() {
		return hardware.redBallsCollected();
	}

	public int greenBallsInside() {
		return hardware.greenBallsCollected();
	}

	public void move( double distance, double angle) {

	}

	public double angleMoved() {
		return 0.0;
	}

	public double distanceMoved() {
		return 0.0;
	}

	public boolean seesWall() {
		return false;
	}
	
	public void setState(State state){
		this.state=state;
	}
	
	public State state(){
		return state;
	}

}
