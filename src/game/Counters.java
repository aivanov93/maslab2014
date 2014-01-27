package game;

import global.Constants;

public class Counters {
	/**
	 * steps timer used for different task
	 */
	private int stepsLeft = 0;
	
	/**
	 * used to account for vision problems
	 */
	private int stepsAllowedToMissObject = 0;

	public Counters(){
		
	}
	
	public void missedObject(){
		stepsAllowedToMissObject--;
	}
	
	public void resetMissedObject(){
		stepsAllowedToMissObject=3;
	}
	
	public boolean lostObject(){
		return stepsAllowedToMissObject==0;
	}
	
	public void setStepsCounter(int n){
		stepsLeft=n;
	}
	
	public void stepped(){
		stepsLeft--;
	}
	
	public boolean done(){
		return stepsLeft==0;
	}
}
