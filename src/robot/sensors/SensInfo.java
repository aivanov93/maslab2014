package robot.sensors;

public class SensInfo {
	private double x, y, angle, t, ul, ur, red,green;

	public SensInfo() {
		x = 0;
		y = 0;
		angle = 0;
	}

	public synchronized void set(double time1, double time2, double dx,
			double dy, double dangle, double ul, double ur, double red, double green) {
		t = (time2 - time1)/1000000000.0;
		x += dx;
		y += dy;
		angle += dangle * t;
		this.ul = ul;
		this.ur = ur;
		this.red=red;
		this.green=green;
	}

	public synchronized Odometry getAndReset() {
		Odometry odo = new Odometry(x, y, angle);
		x = 0;
		y = 0;
		angle = 0;
		return odo;
	}
	
	public synchronized  double red(){
		return red;
	}
	
	public synchronized  double green(){
		return green;
	}

	public synchronized double ultraL() {
		return ul;
	}

	public synchronized double ultraR() {
		return ur;
	}
}
