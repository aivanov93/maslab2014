package game;

import global.Constants;

public class Counters {
	/**
	 * steps timer used for different task
	 */
	private int stepsLeft = 0;
	
	private int giveUp=0;
	/**
	 * used to account for vision problems
	 */
	private int stepsAllowedToMissObject = 0;

	public Counters(){
		
	}
	
	public void startGiveUp(){
		giveUp=90;
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
		
		if (stepsLeft>0)stepsLeft--;
	}
	
	public void giveUpStep(){
		if (giveUp>0) giveUp--;
	}
	
	public boolean shouldGiveUp(){
		return giveUp!=0;
	}
	
	public boolean done(){
		return stepsLeft==0;
	}
}
