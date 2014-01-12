package robot.moves;

import jssc.SerialPort;

public class Driver {
	
	PID speedController, angularSpeedController;
	double speed,angularSpeed;
	SerialPort port;
	
	public Driver(SerialPort port){
		this.speedController=new PID(1,0,0.1);
		this.angularSpeedController=new PID(1,0,0.1);
	}
	
	public void move(double distance, double angle){
		speed=speedController.next(distance);
		angularSpeed=angularSpeedController.next(angle);
		
		//TODO actually move the robot
	}
}
