package robot.sensors;

import java.awt.Color;

public class BallCounter {
	private int redBalls=0;
	private int greenBalls=0;
	
	public BallCounter(){
		
	}
	
	public synchronized void gotBall(Color color){
		if (color.equals(Color.red)){
			redBalls++;;
		} else if (color.equals(Color.green)){
			greenBalls++;
		}
	}
	
	public synchronized int greenBalls(){
		return greenBalls;
	}
	
	public synchronized int redBalls(){
		return redBalls;
	}
	
}
