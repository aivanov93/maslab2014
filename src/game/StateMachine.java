package game;

public class StateMachine {
	enum States {
		Start, LookAround, GoBall, GoWall, GoForward, GoReactor, GoSilo, LookAway, DriveBlind, StickyState, ReactorDeposit, 
	}
	
	private int constantSteps;
	private boolean forcedTo;
	
	public StateMachine(){
		this.constantSteps=0;
		this.forcedTo=false;
	}
}
