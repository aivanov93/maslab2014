package game;

import global.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import robot.main.RobotSticky;

public class StateMachine implements Runnable{
	enum States {
		Start, LookAround, GoBall, GoWall, GoForward, GoReactor, GoSilo, LookAway, DriveBlind, StickyState, ReactorDeposit,
	}
	
	private int i=0;
	
	private RobotSticky robot;

	@Override
	public void run() {
		i++; System.out.println(i);
		
	}
	
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);
	

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
