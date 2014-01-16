package robot.map;

public class Position {

	private double weight;
	private double x;
	private double y;
	private double angle;
	
	public Position(double x, double y, double angle, double weight){
		this.x=x;
		this.y=y;
		this.angle=angle;
		this.weight=weight;
	}
	
	public double x(){
		return x;
	}
	
	public double y(){
		return y;
	}
	
	public double angle(){
		return angle;
	}
	
	public double weight() {
		return weight;
	}
	
	public void motionUpdate(double distance, double angle){
		
	}
	
	public void setWeight(double weight){
		this.weight=weight;
	}
}
