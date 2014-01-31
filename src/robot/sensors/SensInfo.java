package robot.sensors;

public class SensInfo {
	private double x, y, angle, t, ul, ur, red, green, lastx = 0, lasty = 0,
			lastangle = Math.PI / 2;
	private boolean didntSeeBall = true;

	public SensInfo() {
		x = 0;
		y = 0;
		angle = Math.PI / 2;
	}

	public synchronized void set(double time1, double time2, double dtotal,
			double dtheta, double red, double green) {
		t = (time2 - time1) / 1000000000.0;

		angle += dtheta;
		y += dtotal * Math.sin(angle);
		x += dtotal * Math.cos(angle);
		// System.out.println("                         "+angle+" "+y+ " "+x);
		this.red = red;
		this.green = green;
	}

	public synchronized void setBeam(boolean nothing) {
		didntSeeBall = didntSeeBall && nothing;
		// if (!didntSeeBall)
		// System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF "+nothing);
	}

	public synchronized boolean sawBallDispensed() {
		boolean ans = !didntSeeBall;
		didntSeeBall = true;
		System.out.println("baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaal dispensed "
				+ !didntSeeBall);
		return ans;
	}

	public synchronized Odometry getAndReset() {
		Odometry odo = new Odometry(-x + lastx, y - lasty, angle - lastangle,
				angle);
		lastx = x;
		lasty = y;
		lastangle = angle;
		return odo;
	}

	public synchronized void resetFrame() {
		lastx = 0;
		lasty = 0;
		lastangle = Math.PI / 2;
		x = 0;
		y = 0;
		angle = Math.PI / 2;
	
	}

	public synchronized double red() {
		return red;
	}

	public synchronized double green() {
		return green;
	}

	public synchronized double ultraL() {
		return ul;
	}

	public synchronized double ultraR() {
		return ur;
	}
}
