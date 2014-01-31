package game;

import global.Constants;

public class Counters {
	/**
	 * steps timer used for different task
	 */
	private int stepsLeft = 0;
	private int stateSteps = 0;
	private int steps = 0;
	private int giveUp = -1;
	/**
	 * used to account for vision problems
	 */
	private int stepsAllowedToMissObject = 0;

	public Counters() {

	}

	public void startStateSteps() {
		stateSteps = 0;
	}

	public void stateStep() {
		stateSteps++;
	}

	public int stateSteps() {
		return stateSteps;
	}

	public void startGiveUp() {
		giveUp = 0;
	}

	public void missedObject() {
		stepsAllowedToMissObject--;
	}

	public void resetMissedObject() {
		stepsAllowedToMissObject = 3;
	}

	public boolean lostObject() {
		return stepsAllowedToMissObject == 0;
	}

	public void setStepsCounter(int n) {
		stepsLeft = n;
	}

	public void stepped() {

		if (stepsLeft > 0)
			stepsLeft--;
	}

	public void giveUpStep() {
		if (giveUp > -1)
			giveUp++;
	}

	public boolean shouldGiveUp() {
		if (giveUp == 400) {
			giveUp = -1;
			return false;
		} else if (giveUp==-1) return false;
		return true;

	}

	public void startStepCounter() {
		steps = 0;
	}

	public void countStep() {
		steps++;
	}

	public int steps() {
		return steps;
	}

	public boolean done() {
		return stepsLeft == 0;
	}
}
