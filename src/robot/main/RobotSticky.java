package robot.main;

import game.StateMachine.State;

import fakerobot.RobotSimulator;
import fakerobot.SampleMaps;
import global.Constants;
import jssc.SerialPort;
import robot.moves.Driver;
import robot.sensors.IRSensors;
import robot.sensors.RobotEnviroment;
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
	
	private IRSensors irs = new IRSensors(Constants.numberOfIRs);

	

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

	/* **********************************************
	 * ****** getters for main robot modules ********
	 * *********************************************/
	
	public VisionDetector camera() {
		return camera;
	}

	public RobotEnviroment hardware(){
		return hardware;
	}
	
	public IRSensors irs(){
		return irs;
	}
	
	
	public void update() {
		hardware.updateReadings(irs);
		hardware.updateCamera(camera);

	}
	
	

	public void move( double distance, double angle) {
		double forwardSpeed=driver.moveForward(distance);
		double angularSpeed=driver.rotate(angle);
		hardware.move(forwardSpeed, angularSpeed);
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
		this.hardware.setState(state);
	}
	
	public State state(){
		return state;
	}

}
