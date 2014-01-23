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
	public Cytron motor1, motor2;
	public List<Ultrasonic> ultrasonics=new ArrayList<Ultrasonic>();
	public Encoder encLeft, encRight;
	public Gyroscope gyro;

	/**
	 * Initializes all the required robot's part
	 */
	public RobotHardware() {
		comm = new MapleComm(MapleIO.SerialPortType.LINUX);
		motor1 = new Cytron(4, 3);
		motor2 = new Cytron(5, 6);
		Ultrasonic ultra;
		for (int i = 0; i < 5; i++) {
			switch (i) {
			case 0:
				ultra = new Ultrasonic(16,24);
				System.out.println(ultra);
				ultrasonics.add(ultra);
				comm.registerDevice(ultra);
				break;
				
			case 1:
				ultra = new Ultrasonic(17,25);
				ultrasonics.add(ultra);
				comm.registerDevice(ultra);
				break;
			case 2:
				ultra = new Ultrasonic(15, 14);
				ultrasonics.add(ultra);
				comm.registerDevice(ultra);
				break;
			/*case 3:
				ultra = new Ultrasonic(18,26);
				ultrasonics.add(ultra);
				comm.registerDevice(ultra);
				break;
			case 4:
				ultra = new Ultrasonic(19, 29);
				ultrasonics.add(ultra);
				comm.registerDevice(ultra);
				break;*/
				
			}

		}
		gyro = new Gyroscope(1, 9);
		encLeft = new Encoder(18, 27);
		encRight = new Encoder(19, 29);

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
		motor1.setSpeed(speed + angularSpeed);
		motor2.setSpeed(speed - angularSpeed);
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
		double dl = encLeft.getDeltaAngularDistance() * Constants.wheelRadius;
		double dr = encRight.getDeltaAngularDistance();
		double dtotal = (dl + dr) / 2;
		double theta = (dr - dl) / Constants.wheelBase;
		odometry.set(dtotal * Math.sin(theta), dtotal * Math.cos(theta),
				gyro.getAngleChangeSinceLastUpdate());
	}

	@Override
	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args){
		RobotHardware hardware=new RobotHardware();
		hardware.update();
		
		for (int i=0; i<40; i++){
			hardware.update();
			hardware.motor1.setSpeed(i*0.025);
			hardware.motor2.setSpeed(i*0.025);
			hardware.comm.transmit();
			
			//System.out.println("sonars "+hardware.ultrasonics.get(0).getDistance()+ " "+hardware.ultrasonics.get(1).getDistance()+ " "+hardware.ultrasonics.get(2).getDistance());//+ " "+hardware.ultrasonics.get(3).getDistance()+ " "+hardware.ultrasonics.get(4).getDistance() );
			//System.out.println("gyro "+hardware.gyro.getAngleChangeSinceLastUpdate());
			System.out.println("left enc "+hardware.encLeft.getDeltaAngularDistance()+" right enc "+hardware.encRight.getDeltaAngularDistance());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	
		hardware.motor1.setSpeed(0);
		hardware.motor2.setSpeed(0);
		hardware.comm.transmit();
		
	}

}
