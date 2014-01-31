package robot.sensors;

import java.awt.Graphics2D;

import java.util.*;

import maple.comm.MapleComm;
import maple.comm.MapleIO;
import maple.devices.actuators.Cytron;
import maple.devices.actuators.Servo3001HB;
import maple.devices.actuators.Servo6001HB;
import maple.devices.sensors.ColorSensor;
import maple.devices.sensors.DigitalInput;
import maple.devices.sensors.Encoder;
import maple.devices.sensors.Gyroscope;
import maple.devices.sensors.Infrared;
import maple.devices.sensors.Ultrasonic;

import game.Logger;
import game.StateMachine.State;
import global.Constants;
import vision.detector.VisionDetector;
import jssc.SerialPort;

public class RobotHardware implements RobotEnviroment {

	MapleComm comm;
	public Cytron motor1, motor2;
	Ultrasonic ultraL, ultraR;
	public Encoder encLeft, encRight;
	public Gyroscope gyro;
	public Cytron corkScrew, dispenser;
	public Servo3001HB  redServo, lowGreenServo, stickyServo;
	public Servo6001HB sortingServo, unstickyServo;
	public ColorSensor colorSensor;
	public DigitalInput wallLeftBump,dispenserBeam, silo1,silo2,leftBump, rightBump, leftBackBump, rightBackBump;
	public SensInfo data = new SensInfo();
	Logger logger = new Logger();

	private double sortingangle;
	private boolean setGreen=false;
	private boolean dispence=false;


	BallDispencer balls;
	private boolean stopDispence;
	/**
	 * Initializes all the required robot's part
	 */
	public RobotHardware() {
		comm = new MapleComm(MapleIO.SerialPortType.LINUX);
		motor1 = new Cytron(4, 3);
		motor2 = new Cytron(36, 6);
		dispenser=new Cytron(31, 5);
		corkScrew = new Cytron(10, 14);
		leftBump=new DigitalInput(35);
		rightBump=new DigitalInput(37);
		leftBackBump=new DigitalInput(26);
		rightBackBump=new DigitalInput(28);
		silo1=new DigitalInput(32);
		silo2=new DigitalInput(34);
		dispenserBeam=new DigitalInput(33);
		wallLeftBump=new DigitalInput(23);
		colorSensor = new ColorSensor();
		ultraL = new Ultrasonic(16, 24);
		//comm.registerDevice(ultraL);

		ultraR = new Ultrasonic(17, 25);
//		comm.registerDevice(ultraR);

		sortingServo = new Servo6001HB(7);
		lowGreenServo = new Servo3001HB(2);
		redServo = new Servo3001HB(1);
		stickyServo = new Servo3001HB(8);
		unstickyServo = new Servo6001HB(0);

		gyro = new Gyroscope(1, 9);
		encLeft = new Encoder(18, 27);
		encRight = new Encoder(19, 20);

		comm.registerDevice(motor1);
		comm.registerDevice(dispenserBeam);
		comm.registerDevice(silo1);
		comm.registerDevice(silo2);
		comm.registerDevice(leftBackBump);
		comm.registerDevice(rightBackBump);
		comm.registerDevice(motor2);
		comm.registerDevice(leftBump);
		comm.registerDevice(rightBump);
		comm.registerDevice(corkScrew);
		comm.registerDevice(gyro);
		comm.registerDevice(lowGreenServo);
		comm.registerDevice(redServo);
		comm.registerDevice(stickyServo);
		comm.registerDevice(unstickyServo);
		comm.registerDevice(encLeft);
		comm.registerDevice(encRight);
		comm.registerDevice(sortingServo);
		comm.registerDevice(dispenser);
		comm.registerDevice(colorSensor);
		comm.registerDevice(wallLeftBump);

		sortingangle=110;
		comm.initialize();

		stickyServo.setAngle(180);
		unstickyServo.setAngle(180);
		lowGreenServo.setAngle(74);
		sortingServo.setAngle(110);
		redServo.setAngle(105);

		corkScrew.setSpeed(0.25);
		comm.transmit();
		Thread odoThread = new Thread(new SensorsRunnable(this));
		odoThread.start();

		//Thread b=new Thread(new Dispencer())
	}

	public void update() {
		// comm.updateSensorData();
	}

	public void updateReadings(RangeSensors range) {
		range.set(0, data.ultraL());
		range.set(1, data.ultraR());
	}

	public void updateCamera(VisionDetector detector) {

	}
	
	public synchronized void resetSort(){
		sortingServo.setAngle(110);
	}

	public synchronized void sortRed() {
		sortingServo.setAngle(20);
	}

	public synchronized void sortGreen() {
		sortingServo.setAngle(200);
	}
	
	public synchronized void move(double speed, double angularSpeed) {
		logger.step();
		double speed1 = speed - angularSpeed;
		double speed2 = speed + angularSpeed;
		logger.log("speeds" + speed1 + " " + speed2 + " " + speed + " "
				+ angularSpeed);

		 speed1+=(Math.signum(speed1)*0.13);
		// if (Math.abs(speed2)>0.01)
		 speed2+=(Math.signum(speed2)*0.13);
		
		 /*
		  *  speed1+=(Math.signum(speed1)*0.15);
		 if (Math.abs(speed2)>0.01)
		 speed2+=(Math.signum(speed2)*0.15);
		
		  */
		 // else speed2=0;
		 if (setGreen) {
			 setGreen=false;
			 lowGreenServo.setAngle(130);
		 }
		 if (dispence) {
			 dispence=false;
			 dispenser.setSpeed(1.0);
		 }
		 if (stopDispence){
			 stopDispence=false;
			 dispenser.setSpeed(0.0);
		 }
		motor1.setSpeed(speed1);
		motor2.setSpeed(speed2);
		comm.transmit();
	}

	public void setState(State state) {
	}

	public void collectSilo() {
	}

	public void dumpRedBalls(int n) {
		redServo.setAngle(170);
	}

	
	public void resetGreenDump(){
		lowGreenServo.setAngle(72);
	}

	public synchronized  void dumpGreenBall() {
		System.out.println("preparing dispence");
		dispence=true;
	}
	
	public boolean checkIfDumped(){
		boolean dumped=data.sawBallDispensed();
		if (dumped) {
			dispenser.setSpeed(0);
		}
		return dumped;
	}
	
	public synchronized void stopDispencer(){
		stopDispence=true;
	}
	
	public void prepareBottomDump(){
		setGreen=true;
	}

	@Override
	public void updateOdometry(Odometry odometry) {
		Odometry odom = data.getAndReset();
		odometry.set(odom.xMoved(), odom.yMoved(), odom.angleMoved(), odom.theta());
	}
	
	public void resetFrame(){
		data.resetFrame();
	}

	@Override
	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws InterruptedException {
		RobotHardware hardware = new RobotHardware();
		// hardware.update();
		// hardware.corkScrew.setSpeed(0.25);
		// hardware.stickyServo.setAngle(30);
		// hardware.unstickyServo.setAngle(150);
		// hardware.lowGreenServo.setAngle(90);
		// hardware.sortingServo.setAngle(90);
		// hardware.redServo.setAngle(120);

		// hardware.comm.transmit();

		//hardware.motor1.setSpeed(0.1);
		//hardware.motor2.setSpeed(0.8);
		hardware.dispenser.setSpeed(1);

		hardware.comm.transmit();

	//	hardware.comm.transmit();
		for (int i = 0; i < 1000; i++) {
		//	System.out.println("red " + hardware.colorSensor.getRedValue()+" green " + hardware.colorSensor.getGreenValue());
//			System.out.println("silo1 "+hardware.silo1.getValue()+"  silo2 "+hardware.silo2.getValue()+" dispenser beam "+ hardware.dispenserBeam.getValue()+ " left front "+hardware.leftBump.getValue()+" right front "+hardware.rightBump.getValue()+ " left back "+hardware.leftBackBump.getValue()+" right back "+ hardware.rightBackBump.getValue());
		System.out.println("left "+ hardware.encLeft.getTotalAngularDistance()+"   "+"right "+ hardware.encRight.getTotalAngularDistance());
			System.out.println(hardware.dispenserBeam.getValue());
			Thread.sleep(30);
		}

		// hardware.lowGreenServo.setAngle(9);

		 hardware.redServo.setAngle(9);
		hardware.comm.transmit();


		System.out.println(hardware.data.getAndReset().angleMoved() * 180
				/ Math.PI);

		for (int i = 0; i < 50; i++) {
			// System.out.println(hardware.data.ultraL()+" "+hardware.data.ultraR());
			// Thread.sleep(100);
		}

		hardware.motor1.setSpeed(0);
		hardware.motor2.setSpeed(0);
		hardware.comm.transmit();

	}



}
