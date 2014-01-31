package robot.sensors;

import global.Constants;

import java.util.concurrent.atomic.AtomicReference;

import maple.comm.MapleComm;
import maple.devices.sensors.Encoder;
import maple.devices.sensors.Gyroscope;
import maple.devices.sensors.Ultrasonic;

public class SensorsRunnable implements Runnable {

		RobotHardware hardware;

	public SensorsRunnable(RobotHardware hardware) {
		this.hardware = hardware;
		
	}

	public void run() {
		double lastTime = System.nanoTime();
		double newTime;
		while (true) {
			try {

				hardware.comm.updateSensorData();
				newTime = System.nanoTime();
				double dl = -hardware.encLeft.getDeltaAngularDistance()
						* Constants.wheelRadius;
				double dr = hardware.encRight.getDeltaAngularDistance()
						* Constants.wheelRadius;
				double dtotal = (dl + dr) / 2;
				double theta = (dr - dl) / Constants.wheelBase;
			
				hardware.data.set(lastTime, newTime, dtotal , theta,
						hardware.colorSensor.getRedValue(),
						hardware.colorSensor.getGreenValue());
				hardware.data.setBeam(hardware.dispenserBeam.getValue());
				lastTime = newTime;

				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
