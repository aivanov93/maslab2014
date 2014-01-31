package robot.moves;

import jssc.SerialPort;

public class Driver {

	PID speedController, angularSpeedController;
	double speed, angularSpeed;
	double Pd, Pa;
	SerialPort port;

	public Driver(double Kp1, double Ki1, double Kd1, double Kp2, double Ki2,
			double Kd2) {
		this.speedController = new PID(Kp1, Ki1, Kd1);
		this.angularSpeedController = new PID(Kp2, Ki2, Kd2);
	}
	
	public void setP(double Pd, double Pa){
		this.Pd=Pd;
		this.Pa=Pa;
	}

	public double moveForward(double distance, boolean ponly) {
		speed=speedController.next(distance);
		if (!ponly)
		return Math.max(Math.min(speed, 0.2),-0.2);
		 else {
				return Math.max(Math.min(distance*Pd, 0.2),-0.2);
			}
	}
	
	

	public double rotate(double angle, boolean ponly) {
		angularSpeed=angularSpeedController.next(angle);
		if (!ponly){
		return Math.max(Math.min(angularSpeed, 0.15),-0.15);
		} else {
			return Math.max(Math.min(angle*Pa, 0.2),-0.2);
		}
	}
	
	public void reset(){
		speedController.reset();
		angularSpeedController.reset();
	}
}
