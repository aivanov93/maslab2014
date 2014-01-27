package robot.sensors;

import java.awt.Graphics2D;

import java.util.*;

import maple.comm.MapleComm;
import maple.comm.MapleIO;
import maple.devices.actuators.Cytron;
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
	public SensInfo data = new SensInfo();
	Logger logger=new Logger();
	/**
	 * Initializes all the required robot's part
	 */
	public RobotHardware() {
		comm = new MapleComm(MapleIO.SerialPortType.LINUX);
		motor1 = new Cytron(4, 3);
		motor2 = new Cytron(5, 6);

		ultraL = new Ultrasonic(16, 24);
		comm.registerDevice(ultraL);

		ultraR = new Ultrasonic(17, 25);
		comm.registerDevice(ultraR);

		gyro = new Gyroscope(1, 9);
		encLeft = new Encoder(18, 27);
		encRight = new Encoder(19, 29);

		comm.registerDevice(motor1);
		comm.registerDevice(motor2);
		comm.registerDevice(gyro);
		comm.registerDevice(encLeft);
		comm.registerDevice(encRight);

		comm.initialize();

		Thread odoThread = new Thread(new SensorsRunnable(data, comm, gyro,
				encLeft, encRight, ultraL, ultraR));
		odoThread.start();

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

	public int redBallsInside() {
		return 0;
	}

	public int greenBallsInside() {
		return 0;
	}

	public int ballsCollected() {
		return 0;
	}

	public void move(double speed, double angularSpeed) {
		logger.step();
		double speed1=speed-angularSpeed;
		double speed2=speed+angularSpeed;
		if (Math.abs(speed1)>0.0001)	speed1+=(Math.signum(speed1)*0.105);
		else speed1=0;
		if (Math.abs(speed2)>0.0001) speed2+=(Math.signum(speed2)*0.105);
		else speed2=0;
		motor1.setSpeed(speed1);
		motor2.setSpeed(speed2);
		logger.log("speeds "+(speed - angularSpeed)+" "+(speed + angularSpeed));
		comm.transmit();
	}

	public void setState(State state) {
	}

	public void collectSilo() {
	}

	public void dumpRedBalls(int n) {
	}

	public void dumpGreenBallsTop(int n) {
	}

	public void dumpGreenBallsBottom(int n) {
	}

	@Override
	public void updateOdometry(Odometry odometry) {
		Odometry odom = data.getAndReset();
		odometry.set(odom.xMoved(), odom.yMoved(), odom.angleMoved());
	}

	@Override
	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws InterruptedException {
		RobotHardware hardware = new RobotHardware();
		// hardware.update();
		System.out.println("wtf");
		hardware.motor1.setSpeed(0.11);
		hardware.motor2.setSpeed(0.11);
		hardware.comm.transmit();

		Thread.sleep(3000);
		
		hardware.motor1.setSpeed(0);
		hardware.motor2.setSpeed(0);
		hardware.comm.transmit();
		
		System.out.println(hardware.data.getAndReset().angleMoved()*180/Math.PI);
		
		for (int i = 0; i < 50; i++) {
			System.out.println(hardware.data.ultraL()+" "+hardware.data.ultraR());
			Thread.sleep(100);
		}

		hardware.motor1.setSpeed(0);
		hardware.motor2.setSpeed(0);
		hardware.comm.transmit();

	}

}
