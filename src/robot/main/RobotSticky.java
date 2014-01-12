package robot.main;

import jssc.SerialPort;
import robot.moves.Driver;
import robot.sensors.Sensors;
import vision.detector.VisionDetector;

public class RobotSticky implements Robot{

	private Sensors sensors;
	private Driver driver;
	private SerialPort port;
	private VisionDetector camera;
	public RobotSticky(){
		
		//open the port for maple communication
		try {
			port = new SerialPort("/dev/ttyACM0");
            port.openPort();
            port.setParams(115200, 8, 1, 0);
        }
        catch (Exception ex){
            System.out.println(ex);
        }
		
		//initialize sensors, driver and camera
		this.sensors=new Sensors(port);
		this.driver=new Driver(port);
		this.camera=new VisionDetector();
	}
	
	public void update(){
		sensors.updateReadings();
	}
}
