package robot.sensors;

import global.Constants;

import java.util.concurrent.atomic.AtomicReference;

import maple.comm.MapleComm;
import maple.devices.sensors.Encoder;
import maple.devices.sensors.Gyroscope;
import maple.devices.sensors.Ultrasonic;

public class SensorsRunnable implements Runnable {

	SensInfo data;
	MapleComm comm;
	Gyroscope gyro;
	Encoder encLeft;
	Encoder encRight;
Ultrasonic ultraL, ultraR;
	public SensorsRunnable(SensInfo data, MapleComm comm, Gyroscope gyro, Encoder encLeft, Encoder encRight, Ultrasonic ultraL, Ultrasonic ultraR) {
		this.data = data;
		this.comm=comm;
		this.gyro=gyro;
		this.encLeft=encLeft;
		this.encRight=encRight;
		this.ultraL=ultraL;
		this.ultraR=ultraR;
	}

	public void run() {
			double lastTime=System.nanoTime();
			double newTime;
			while(true){
				try {
				
				comm.updateSensorData();
				newTime=System.nanoTime();
				double dl = -encLeft.getDeltaAngularDistance() * Constants.wheelRadius;
				double dr = encRight.getDeltaAngularDistance() * Constants.wheelRadius;
				double dtotal = (dl + dr) / 2;
				double theta = (dr - dl) / Constants.wheelBase;
				data.set(lastTime, newTime, dtotal * Math.sin(theta), dtotal * Math.cos(theta),
						-gyro.getOmega(), ultraL.getDistance(), ultraR.getDistance() );
				lastTime=newTime;
				
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	}
}
