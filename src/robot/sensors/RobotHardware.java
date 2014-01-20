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

import game.StateMachine.State;
import global.Constants;
import vision.detector.VisionDetector;
import jssc.SerialPort;

public class RobotHardware implements RobotEnviroment {

	MapleComm comm;
	Cytron motor1, motor2;
	List<Ultrasonic> ultrasonics;
	Encoder encLeft, encRight;
	Gyroscope gyro;

	/**
	 * Initializes all the required robot's part
	 */
	public RobotHardware() {
		comm = new MapleComm(MapleIO.SerialPortType.LINUX);
		motor1 = new Cytron(4, 5);
		motor2 = new Cytron(6, 7);
		for (int i = 0; i < 5; i++) {
			Ultrasonic ultra = new Ultrasonic(13, 12);
			ultrasonics.add(ultra);
			comm.registerDevice(ultra);
		}
		gyro = new Gyroscope(1, 9);
		encLeft = new Encoder(2, 3);
		encRight = new Encoder(2, 3);

		comm.registerDevice(motor1);
		comm.registerDevice(motor2);
		comm.registerDevice(gyro);
		comm.registerDevice(encLeft);
		comm.registerDevice(encRight);
		comm.initialize();
	}

	public void update() {
		comm.updateSensorData();
	}

	public void updateReadings(RangeSensors range) {
		for (int i = 0; i < Constants.numberOfIRs; i++) {
			range.set(i, ultrasonics.get(i).getDistance());
		}
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
		motor1.setSpeed(speed+angularSpeed);
		motor2.setSpeed(speed-angularSpeed);
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
		double dl=encLeft.getDeltaAngularDistance()*Constants.wheelRadius;
		double dr=encRight.getDeltaAngularDistance();
		double dtotal=(dl+dr)/2;
		double theta=(dr-dl)/Constants.wheelBase;
		odometry.set(dtotal*Math.sin(theta), dtotal*Math.cos(theta), gyro.getAngleChangeSinceLastUpdate());
	}

	@Override
	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub

	}

}
