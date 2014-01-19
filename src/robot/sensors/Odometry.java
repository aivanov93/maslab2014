package robot.sensors;

public class Odometry {

	private double x;
	private double y;
	private double angle;
	private double distance;
	
	public Odometry(double xMoved, double yMoved, double angleMoved){
		this.x=xMoved;
		this.y=yMoved;
		this.angle=angleMoved;
		this.distance=Math.sqrt(x*x+y*y);
	}
	
	public double xMoved(){
		return x;
	}
	
	public double yMoved(){
		return y;
	}
	
	public double angleMoved(){
		return angle;
	}
	
	public double distanceMoved(){
		return distance;
	}
	
	public void set(double xMoved, double yMoved, double angleMoved){
		System.out.println("*****************ODOM "+xMoved+" "+yMoved+" "+angleMoved);
		this.x=xMoved;
		this.y=yMoved;
		this.angle=angleMoved;
		this.distance=Math.sqrt(x*x+y*y);
	}
}
