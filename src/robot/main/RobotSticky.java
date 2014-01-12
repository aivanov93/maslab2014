package robot.main;

import fakerobot.Map;
import fakerobot.SampleMaps;
import jssc.SerialPort;
import robot.moves.Driver;
import robot.sensors.Sensors;
import vision.detector.VisionDetector;

public class RobotSticky implements Robot {

	/**
	 * uses sensors to get readings from IRs
	 */
	private Sensors sensors;
	/**
	 * driver includes PID controllers and computes the required forward and
	 * angular speeds
	 */
	private Driver driver;
	private SerialPort port;
	private VisionDetector camera;

	// simulation
	private Map map;

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

	public RobotSticky(boolean real) {
		// open port if not simulation
		if (real) {
			openPort();
		} else {
			map = SampleMaps.createMap1();
		}

		// initialize sensors, driver and camera
		this.sensors = new Sensors(port);
		this.driver = new Driver(port);
		this.camera = new VisionDetector();

	}

	public void update() {
		sensors.updateReadings();
	}
}
