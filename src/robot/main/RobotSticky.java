package robot.main;

import game.StateMachine.State;

import fakerobot.MapGUI;
import fakerobot.RobotSimulator;
import fakerobot.SampleMaps;
import global.Constants;
import jssc.SerialPort;
import robot.map.Localization;
import robot.map.MapForSensors;
import robot.map.MazeMap;
import robot.map.SampleMapsLocalization;
import robot.moves.Driver;
import robot.sensors.RangeSensors;
import robot.sensors.Odometry;
import robot.sensors.RobotEnviroment;
import robot.sensors.RobotHardware;
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
	
	private Localization localization;
	
	private MapForSensors mapForSensors;
	
	private MazeMap mazeMap;
	
	private Odometry odometry;

	/**
	 * flag to know whether it's a simulation or no
	 */
	private boolean real;

	/**
	 * 
	 */
	private State state;
	
	private MapGUI gui;
	
	private RangeSensors irs = new RangeSensors(Constants.numberOfIRs);

	

	public RobotSticky(boolean real, double x, double y, double angle) {
		this.real = real;
		
		// open port if not simulation
		if (real) {
			hardware=new RobotHardware();

		} else {
			hardware = SampleMaps.createMap2();
			
		}
		this.odometry=new Odometry(0, 0, 0);
		this.mapForSensors=SampleMapsLocalization.mapForSensors2(irs);
		this.localization=new Localization(x, y, angle, mapForSensors);
	
		localization.setOdometry(odometry);
		this.mazeMap=SampleMapsLocalization.mazeMap2();
		
		gui = new MapGUI(hardware, localization,mazeMap);
		
		// initialize sensors, driver and camera
		this.driver = new Driver(0.01, 0.0, 0.005, 0.15, 0.0, 0.003);
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
	
	public RangeSensors irs(){
		return irs;
	}
	
	public Odometry odometry(){
		return odometry;
	}
	
	public Localization localization(){
		return localization;
	}
	
	public MazeMap map(){
		return mazeMap;
	}
	
	/**
	 * updates sensor readings
	 */
	public void update() {
		hardware.updateReadings(irs);
		hardware.updateCamera(camera);
		hardware.updateOdometry(odometry);
		gui.update();
	
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
