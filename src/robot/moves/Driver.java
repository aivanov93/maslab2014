package robot.moves;

import jssc.SerialPort;

public class Driver {
	
	PID speedController, angularSpeedController;
	double speed,angularSpeed;
	SerialPort port;
	
	public Driver(double Kp1, double Ki1, double Kd1,double Kp2, double Ki2, double Kd2){
		this.speedController=new PID(Kp1,Ki1,Kd1);
		this.angularSpeedController=new PID(Kp2,Ki2,Kd2);
	}
	
	public double moveForward(double distance){
		return speedController.next(distance);
		
	}
	
	public double rotate(double angle){
		return angularSpeedController.next(angle);
	}
}
