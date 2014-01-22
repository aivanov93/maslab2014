package vision.detector;

import java.awt.Color;

public class ColorObject {

	public static enum Type {
		RedBall, GreenBall, Silo, Reactor, YellowWall
	};

	private double distance;
	private double angle;
	private Type type;

	public ColorObject(Type type, double dist, double angle) {
		this.distance = dist;
		this.angle = angle;
		this.type = type;
	}

	public double distance() {
		return distance;
	}

	public double angle() {
		return angle;
	}

	public Type type() {
		return type;
	}

}
