package robot.sensors;

import game.StateMachine.State;
import vision.detector.VisionDetector;
import jssc.SerialPort;

public class RobotHardware implements RobotEnviroment{
	SerialPort port;
	
	public void openPort() {
		// open the port for maple communication
		try {
			port = new SerialPort("/dev/ttyACM0");
			port.openPort();
			port.setParams(115200, 8, 1, 0);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	public RobotHardware(SerialPort port){
		this.port=port;
	}
	
public void updateReadings(IRSensors irs){}
	
	public void updateCamera(VisionDetector detector){}
	
	public int redBallsInside(){ return 0;}
	
	public int greenBallsInside(){ return 0;}
	public int ballsCollected(){return 0;}
	
	public void move(double speed, double angularSpeed){}
	
	public void setState(State state){}
	
	public void collectSilo(){}
	
	public void dumpRedBalls(int n){}
	
	public void dumpGreenBallsTop(int n){}
	
	public void dumpGreenBallsBottom(int n){}
	
	
	public double distanceMoved(){return 1;}
	
	public double angleMoved(){return 1;}
     
}
