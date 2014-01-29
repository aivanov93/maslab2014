package robot.moves;

import jssc.SerialPort;

public class Driver {

	PID speedController, angularSpeedController;
	double speed, angularSpeed;
	SerialPort port;

	public Driver(double Kp1, double Ki1, double Kd1, double Kp2, double Ki2,
			double Kd2) {
		this.speedController = new PID(Kp1, Ki1, Kd1);
		this.angularSpeedController = new PID(Kp2, Ki2, Kd2);
	}

	public double moveForward(double distance) {
		speed=speedController.next(distance);
		return Math.max(Math.min(speed, 0.25),-0.25);

	}

	public double rotate(double angle) {
		angularSpeed=angularSpeedController.next(angle);

		return Math.max(Math.min(angularSpeed, 0.18),-0.18);
	}
	
	public void reset(){
		speedController.reset();
		angularSpeedController.reset();
	}
}
