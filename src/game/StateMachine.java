package game;

import global.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import robot.main.RobotSticky;

public class StateMachine implements Runnable{
	enum State {
		Start, LookAround, GoBall, GoWall, GoForward, GoReactor, GoSilo, LookAway, DriveBlind, StickyState, ReactorDeposit, FollowWall,  AlignSilo,
	}
	
	private State state;
	
	private int i=0;
	
	private RobotSticky robot;
	
	/**
	 * number of reactors whose position are known
	 */
	private int knownReactors=0;
	
	private int[] scoredBottom=new int[3];
	private int[] scoredTop=new int[3];
	
	/**
	 * signals whether robot know the position of the yellowWall
	 */
	private boolean knowsYellow=false;
	
	/**
	 * signals whether balls where collected from silo
	 */
	private boolean siloDone=false;
	
	private double angleToTurn=0;
	
	/**
	 * 
	 */
	private int stepsAllowedToMissObject=0;
	
	/**
	 * To keep track of previous movement
	 */
	private double distance=0;
	private double angle=0;
	
	private double reactorCoefficient(){
		return 6-reactorsScoredBottom()-reactorsScoredTop();
	}
	
	public int reactorsScoredBottom(){
		return 0; //TODO
	}
	
	public int reactorsScoredTop(){
		return 0; //TODO
	}
	
	
	
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);
		
	@Override
	public void run() {
		i++; System.out.println(i);
		
	}	
	public void start() {			
		//schedule clock
		final ScheduledFuture<?> stepHandler = scheduler.scheduleAtFixedRate(
				this, Constants.clock, Constants.clock, TimeUnit.MILLISECONDS);
		
		//schedule ending
		scheduler.schedule(new Runnable() {
			public void run() {
				stepHandler.cancel(true);
			}
		}, Constants.totalTime, TimeUnit.SECONDS);
	}
	
	public double timeElapsed(){
		return 0.0;
	}
	
	public void updateMissed(){
		stepsAllowedToMissObject--;
		if (stepsAllowedToMissObject==0){
			state=State.LookAround;
		}
		distance-=robot.distanceMoved();
		angle-=robot.angleMoved();
		robot.move(distance, angle);
	}
	
	/* *************************************************
	 * *******************S T A T E S ******************
	 * *************************************************
	 ***************************************************/
	
	public void lookAround(){
		angleToTurn-=robot.angleMoved();
		
		if (robot.camera().seesSilo() && !siloDone){
			state=State.GoSilo;
			stepsAllowedToMissObject=Constants.allowedToMiss;
		} else if (robot.camera().seesBall()) {
			state=State.GoBall;	
		} else if (robot.seesWall()){
			state=State.FollowWall;
		} else if (Math.abs(angleToTurn)<0.01){
			state=State.GoForward;
		} 
		//turns the amount left		
		robot.move(0,angleToTurn);
	}
	
	public void goSilo(){
		
		//take care of false positives and false negatives
		if (!robot.camera().seesSilo()){
			updateMissed();
		} else {
			//object seen so reset the miss count
			stepsAllowedToMissObject=Constants.allowedToMiss;
			
			//calculate the actual distance
			double distanceToSilo=robot.camera().silo().distance();
			double angleToSilo=robot.camera().silo().angle();
			
			//check if it's time to align
			if (distanceToSilo<Constants.minDistanceToSilo){
				state=State.AlignSilo;
			}
			
			//move the robot
			robot.move(distanceToSilo, angleToSilo);			
		}
		
	}
	
	public void allignSilo(){
		
	}
	
	
	

	private int constantSteps;
	private boolean forcedTo;

	public StateMachine(RobotSticky robot) {
		this.constantSteps = 0;
		this.forcedTo = false;
	}
	
	public static void main(String[] args){
	//	new StateMachine().start();
	}

	
}
