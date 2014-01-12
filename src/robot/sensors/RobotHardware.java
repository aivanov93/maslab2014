package robot.sensors;

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
	
	public void updateReadings(double[] irs){
		
	}
	
	public void updateCamera(VisionDetector detector){};

}
