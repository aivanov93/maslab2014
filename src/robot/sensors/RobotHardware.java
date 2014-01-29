package robot.sensors;

import java.awt.Graphics2D;

import java.util.*;

import maple.comm.MapleComm;
import maple.comm.MapleIO;
import maple.devices.actuators.Cytron;
import maple.devices.actuators.Servo3001HB;
import maple.devices.actuators.Servo6001HB;
import maple.devices.sensors.ColorSensor;
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
	public Cytron corkScrew;
	public Servo3001HB sortingServo, redServo, lowGreenServo, stickyServo;
	public Servo6001HB unstickyServo;
	public ColorSensor colorSensor;
	public SensInfo data = new SensInfo();
	Logger logger = new Logger();

	/**
	 * Initializes all the required robot's part
	 */
	public RobotHardware() {
		comm = new MapleComm(MapleIO.SerialPortType.LINUX);
		motor1 = new Cytron(4, 3);
		motor2 = new Cytron(36, 6);
		corkScrew = new Cytron(10, 14);

		colorSensor = new ColorSensor();
		ultraL = new Ultrasonic(16, 24);
		comm.registerDevice(ultraL);

		ultraR = new Ultrasonic(17, 25);
		comm.registerDevice(ultraR);

		sortingServo = new Servo3001HB(7);
		lowGreenServo = new Servo3001HB(2);
		redServo = new Servo3001HB(1);
		stickyServo = new Servo3001HB(8);
		unstickyServo = new Servo6001HB(0);

		gyro = new Gyroscope(1, 9);
		encLeft = new Encoder(18, 27);
		encRight = new Encoder(19, 20);

		comm.registerDevice(motor1);
		comm.registerDevice(motor2);
		comm.registerDevice(corkScrew);
		comm.registerDevice(gyro);
		comm.registerDevice(lowGreenServo);
		comm.registerDevice(redServo);
		comm.registerDevice(stickyServo);
		comm.registerDevice(unstickyServo);
		comm.registerDevice(encLeft);
		comm.registerDevice(encRight);
		comm.registerDevice(sortingServo);
		comm.registerDevice(colorSensor);

		comm.initialize();

		stickyServo.setAngle(180);
		unstickyServo.setAngle(180);
		lowGreenServo.setAngle(72);
		sortingServo.setAngle(90);
		redServo.setAngle(105);

		corkScrew.setSpeed(0.25);
		comm.transmit();
		Thread odoThread = new Thread(new SensorsRunnable(this));
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

	public void sortRed() {

	}

	public void sortGreen() {

	}

	public void move(double speed, double angularSpeed) {
		logger.step();
		double speed1 = speed - angularSpeed;
		double speed2 = speed + angularSpeed;
		logger.log("speeds " + speed1 + " " + speed2 + " " + speed + " "
				+ angularSpeed);

		// if (Math.abs(speed1)>0.01) speed1+=(Math.signum(speed1)*0.0);
		// else speed1=0;
		// if (Math.abs(speed2)>0.01) speed2+=(Math.signum(speed2)*0.0);
		// else speed2=0;
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

	public void dumpGreenBallsTop(int n) {
		lowGreenServo.setAngle(72);
	}

	public void dumpGreenBallsBottom(int n) {
		lowGreenServo.setAngle(157);
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
		// hardware.corkScrew.setSpeed(0.25);
		// hardware.stickyServo.setAngle(30);
		// hardware.unstickyServo.setAngle(150);
		// hardware.lowGreenServo.setAngle(90);
		// hardware.sortingServo.setAngle(90);
		// hardware.redServo.setAngle(120);

		// hardware.comm.transmit();

		for (int i = 0; i < 150; i++) {
			System.out.println("red " + hardware.colorSensor.getRedValue()+" green " + hardware.colorSensor.getGreenValue());
			Thread.sleep(300);
		}

		// hardware.lowGreenServo.setAngle(9);

		// hardware.redServo.setAngle(9);
		hardware.comm.transmit();

		hardware.corkScrew.setSpeed(0.0);
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
