package robot.main;


import game.StateMachine.State;

import fakerobot.RobotGUIMap;
import fakerobot.RobotSimulator;
import fakerobot.SampleMaps;
import global.Constants;
import jssc.SerialPort;
import robot.map.BotClientMap;
import robot.map.Localization;
import robot.map.MapForSensors;
import robot.map.MazeMap;
import robot.map.Position;
import robot.map.SampleMapsLocalization;
import robot.moves.Driver;
import robot.sensors.BallCounter;
import robot.sensors.BallSorter;
import robot.sensors.BallVisionSorter;
import robot.sensors.RangeSensors;
import robot.sensors.Odometry;
import robot.sensors.RobotEnviroment;
import robot.sensors.RobotHardware;
import vision.NewImageProcessor;
import vision.detector.VisionDetector;

public class RobotSticky implements Robot {

	/**
	 * uses sensors to get readings from IRs
	 */
	private RobotHardware hardware;
	/**
	 * driver includes PID controllers and computes the required forward and
	 * angular speeds (unitless 0.0 to 1.0 with respect to max speed)
	 */
	private Driver driver;
	/**
	 * keeps all the readings from the camera
	 */
	
	private BallCounter balls;
	
	private Odometry odometry=new Odometry(0, 0, 0,0);

	/**
	 * flag to know whether it's a simulation or no
	 */
	private boolean real;

	/**
	 * 
	 */
	private State state;
	

	
	private RangeSensors irs = new RangeSensors(Constants.numberOfIRs);

	

	public RobotSticky(boolean real) {
		this.real = real;
		
		// open port if not simulation
		if (real) {
			hardware=new RobotHardware();

		}// else {
			//hardware = SampleMaps.createMap2();
			
	//	}
		Position startPosition=BotClientMap.getDefaultMap().getPosition();
		this.odometry=new Odometry(0, 0, 0,0);
	//	this.mapForSensors=SampleMapsLocalization.mapForSensors2(irs);
	//	this.localization=new Localization(startPosition.x(), startPosition.y(), startPosition.angle(), mapForSensors);
	
		//localization.setOdometry(odometry);
		//this.mazeMap=SampleMapsLocalization.mazeMap2();
	
	
		// initialize sensors, driver and camera
		
	//	this.driver = new Driver(0.0065, 0.0, 0.003, 0.23,0.000 ,0.0025);
		
		this.driver = new Driver(0.005, 0.0, 0.0015, 0.25,0.000 ,0.001);
		//0.0075, 0.0, 0.0015, 0.25,0.000 ,0.001
		
		driver.setP(0.007, 0.14);
		
		//this.driver = new Driver(0.004, 0.0, 0.00, 0.14,0.0 ,0.0);
		
		//this.driver = new Driver(0.003, 0.0, 0.00, 0.15,0.0 ,0.0);
		
		//this.driver = new Driver(0.003, 0.0, 0.00, 0.1,0.0 ,0.0);
		
		//this.driver = new Driver(0.005, 0.0, 0.00, 0.1,0.0 ,0.0);
		
		this.balls=new BallCounter();
		Thread ballsThread=new Thread(new BallSorter(balls, hardware));
		ballsThread.start();
		//this.camera = new VisionDetector();
	}

	/* **********************************************
	 * ****** getters for main robot modules ********
	 * *********************************************/
	
	

	public RobotHardware hardware(){
		return hardware;
	}
	
	public RangeSensors irs(){
		return irs;
	}
	
	public Odometry odometry(){
		return odometry;
	}
	

	public BallCounter balls(){
		return balls;
	}

	public void driverReset(){
		driver.reset();
	}
	
	/**
	 * updates sensor readings
	 */
	public void update() {
		hardware.update();
		hardware.updateReadings(irs);
		hardware.updateOdometry(odometry);
	
	}
	
	public void stop(){
		hardware.move(0, 0);
	}
	

	public void move( double distance, double angle, boolean ponly) {
		if (Math.abs(distance)<0.5) distance=0;
		if (Math.abs(angle)<Math.PI/120) angle=0;
		double forwardSpeed=driver.moveForward(distance, ponly);
		double angularSpeed=driver.rotate(angle,ponly);		
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
