package robot.sensors;

public class BallDispencer {

	boolean sawBall;
	public BallDispencer(){
		sawBall=false;
	}
	
	public synchronized void sawBall(boolean sawBall){
		this.sawBall=!sawBall || this.sawBall;
	}
	
	public synchronized void reset(){
		sawBall=false;
		
	}
	
	public synchronized boolean didSeeBall(){
		return sawBall;
				}
}
