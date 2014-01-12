package robot.sensors;

import jssc.SerialPort;

public class Sensors {
	SerialPort port;
	
	public Sensors(SerialPort port){
		this.port=port;
	}
	
	public void updateReadings(){
		
	}

}
