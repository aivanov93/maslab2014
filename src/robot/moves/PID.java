package robot.moves;

import global.Constants;

/**
 * Fancy PID
 *
 */
public class PID {
	double P,I,D;
	double previousError;
	double Kp,Ki,Kd;
	
	public PID(double Kp, double Ki, double Kd){
		this.I=0;
		this.D=0;
		this.previousError=0;
		this.Kd=Kd;
		this.Kp=Kp;
		this.Ki=Ki;
	}
	
	public double next(double error){
		P=error;
		D=(error-previousError)/Constants.clock;
		I+=error*Constants.clock;
		
		previousError=error;
		return Kp*P+Ki*I+Kd*D;
	}
	
	public void reset(){
		P=0; D=0; I=0;
	}
	
}
