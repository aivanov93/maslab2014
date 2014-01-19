package game;

import global.Constants;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import robot.main.RobotSticky;
import straightedge.geom.KPoint;
import vision.detector.ColorObject;

public class StateMachine implements Runnable {
	public static enum State {
		Start, LookAround, GoBall, GoWall, GoForward, GoReactor, GoSilo, LookAway, DriveBlind, StickyState, ReactorDeposit, FollowWall, AlignSilo, Turn90, FindYellowWall, CollectSilo, AllignReactor, GoObject,
	}

	public static enum Goal {
		Silo, Reactor
	}

	private int i = 0;

	private RobotSticky robot;

	private ArrayList<KPoint> path;
	/**
	 * number of reactors whose position are known
	 */
	private int knownReactors = 0;

	private int[] scoredBottom = new int[3];
	private int[] scoredTop = new int[3];

	/**
	 * signals whether robot know the position of the yellowWall
	 */
	private boolean knowsYellow = false;

	/**
	 * signals whether balls where collected from silo
	 */
	private boolean siloDone = false;

	/**
	 * tells whether there are still balls in silo
	 */
	private boolean siloHasBalls = true;

	/**
	 * used to count tries of collecting from the silo
	 */
	private int failedToCollectBalls = 0;

	/**
	 * at each step tells whether a path finding algorithm should be run or not
	 */
	private boolean shouldSearch=false;
	/**
	 * steps timer used for different task
	 */
	private int stepsLeft = 0;
	/**
	 * used when forcing to turn a specific angle
	 */
	private double angleToTurn = Math.PI;

	/**
	 * used to account for vision problems
	 */
	private int stepsAllowedToMissObject = 0;

	private Goal goal;

	/**
	 * To keep track of previous movement
	 */
	private double distance = 0;
	private double angle = 0;

	private double reactorCoefficient() {
		return 2 * knownReactors;
	}

	public int reactorsScoredBottom() {
		return 0; // TODO
	}

	public int reactorsScoredTop() {
		return 0; // TODO
	}

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public void start() {
		// schedule clock
		final ScheduledFuture<?> stepHandler = scheduler.scheduleAtFixedRate(
				this, Constants.clock, Constants.clock, TimeUnit.MILLISECONDS);

		// schedule ending
		scheduler.schedule(new Runnable() {
			public void run() {
				stepHandler.cancel(true);
			}
		}, Constants.totalTime, TimeUnit.SECONDS);
	}

	public double timeElapsed() {
		return 0.0;
	}

	public void updateMissed() {
		stepsAllowedToMissObject--;
		if (stepsAllowedToMissObject == 0) {
			robot.setState(State.LookAround);
		}
		distance -= robot.distanceMoved();
		angle -= robot.angleMoved();
		robot.move(distance, angle);
	}

	public void correctMovement(double distance, double angle) {
		if (Math.abs(angle)>Math.PI/8) distance=0;
		robot.move(distance, angle);
		// TODO implement wall correction
	}

	/* *************************************************
	 * *******************S T A T E S ******************
	 * *************************************************
	 * *************************************************
	 */

	public void fakeRun() {
		while (true) {
			run();
		}
	}

	public void run1() {
		System.out.println("running");
		System.out.println(robot.state().toString() + " distance:" + distance
				+ " angle: " + angle * 180 / Math.PI);
		robot.update();
		robot.localization().localize();
		switch (robot.state()) {
		case GoObject:
			goObject();
			break;
		case LookAround:

			lookAround();
			break;
		case GoWall:
			// goWall();
			break;
		case GoBall:
			goBall();
			break;
		case GoSilo:
			goSilo();
			break;
		case CollectSilo:
			collectSilo();
			break;
		case DriveBlind:
			driveBlind();
			break;
		default:
			System.out.println("noo such state?");
			break;
		}

		this.correctMovement(distance, angle);

	}

	public void run() {
		System.out.println("running");
		System.out.println(robot.state().toString() + " distance:" + distance
				+ " angle: " + angle * 180 / Math.PI);
		robot.update();
	
		robot.localization().localize();
		
		switch (robot.state()) {
		
		case GoObject:
			goObject();
			break;
		case LookAround:

			lookAround();
			break;
		case GoWall:
			// goWall();
			break;
		case GoBall:
			goBall();
			break;
		case GoSilo:
			goSilo();
			break;
		case CollectSilo:
			collectSilo();
			break;
		case DriveBlind:
			driveBlind();
			break;
		default:
			System.out.println("noo such state?");
			break;
		}

		this.correctMovement(distance, angle);

	}


	public void goObject() {
		while (robot.localization().getPosition().isClose(path.get(0))) {
			path.remove(0);
		}
		if (path.isEmpty()) {
			if (goal == Goal.Reactor) {
				robot.setState(State.AllignReactor);
				distance = 0;
			}
		} else {
			double dist=robot.localization().getPosition().distance(path.get(0));
			if (dist>40) distance=dist;
			angle= robot.localization().getPosition().angle(path.get(0));
		}
	}


	/**
	 * Looks around to see anything new
	 */
	public void lookAround() {
		angleToTurn -= robot.odometry().angleMoved();

		if (robot.camera().seesSilo() && !siloDone) {
			robot.setState(State.GoSilo);
			stepsAllowedToMissObject = Constants.allowedToMiss;
		} else if (robot.camera().seesBall()) {
			robot.setState(State.GoBall);
			System.out.println(" angle to ball form camera "
					+ robot.camera().biggestBall().angle());
			stepsAllowedToMissObject = Constants.allowedToMiss;
		} else if (robot.seesWall()) {
			robot.setState(State.FollowWall);
		} else if (Math.abs(angleToTurn) < 0.01) {
			robot.setState(State.GoForward);
		}

		// set the required speeds
		distance = 0;
		angle = angleToTurn;

	}

	/**
	 * approaches the ball
	 */
	public void goBall() {
		// take care of false positives and false negatives
		if (!robot.camera().seesBall()) {
			updateMissed();
		} else {
			double distanceToBall = robot.camera().biggestBall().distance();
			System.out.println("distance to ball " + distanceToBall);
			if (distanceToBall < Constants.minDistanceToBall) {
				this.stepsLeft = Constants.stepsForCatchingBall;
				robot.setState(State.DriveBlind);
				distance = Constants.distanceForCatchingBall;
				angle = 0;
			} else {
				distance = distanceToBall;
				angle = robot.camera().biggestBall().angle();
			}
		}
	}

	public void driveBlind() {
		stepsLeft--;
		distance -= robot.odometry().distanceMoved();
		angle -= robot.odometry().angleMoved();
		if (stepsLeft == 0) {
			angleToTurn = Math.PI;
			robot.setState(State.LookAround);
		}
	}

	/**
	 * Approaches the silo
	 */
	public void goSilo() {
		// take care of false positives and false negatives
		if (!robot.camera().seesSilo()) {
			updateMissed();
		} else {
			// object seen so reset the miss count
			stepsAllowedToMissObject = Constants.allowedToMiss;

			// calculate the actual distance
			double distanceToSilo = robot.camera().silo().distance();
			double angleToSilo = robot.camera().silo().angle();

			// check if it's time to align
			if (distanceToSilo < Constants.minDistanceToSilo) {
				robot.setState(State.AlignSilo);
			}

			// move the robot
			robot.move(distanceToSilo, angleToSilo);
		}

	}

	public void allignSilo() {

	}

	public void collectSilo() {
		if (stepsLeft == Constants.stepsForSiloArm) {
			robot.hardware().collectSilo();
		} else if (stepsLeft == 0) {

			// check if managed to collect any balls
			if (robot.hardware().ballsCollected() != 0) { // if yes
				// check if there are balls left
				if (robot.camera().seesBigBall()) { // if no
					// turn away from the silo
					angleToTurn = Math.PI / 2;
					robot.setState(State.Turn90);
				} else {
					// reset the time counter for the arm movement
					stepsLeft = Constants.stepsForSiloArm;
					failedToCollectBalls = 0;
				}
			} else { // if didnt collect any balls this time
						// increase the failure counter
				failedToCollectBalls++;

				if (failedToCollectBalls == 3) {
					angleToTurn = Math.PI / 2;
					robot.setState(State.Turn90);
				}
			}
		}
	}

	public void turn90() {
		angleToTurn -= robot.odometry().angleMoved();
		if (Math.abs(angleToTurn) < 0.01) {
			robot.setState(State.FindYellowWall);
		}
		// turns the amount left
		distance = 0;
		angle = angleToTurn;
	}

	public void findYellowWall() {
		robot.setState(State.FollowWall);
		/*
		 * if (robot.camera().seesYellowWall() &&
		 * robot.hardware().redBallsInside()>0){
		 * robot.hardware().dumpRedBalls(robot.hardware().redBallsInside()); }
		 * else {
		 * 
		 * }
		 */
	}

	public void followWall() {
		if (robot.camera().seesBall()) {
			ColorObject ball = robot.camera().biggestBall();

		}
	}

	public StateMachine(RobotSticky robot) {
		this.robot = robot;		
		System.out.println(robot.localization().getPosition().x()+ " "+ robot.localization().getPosition().y());
		robot.map().findReactor(robot.localization().getPosition().x(), robot.localization().getPosition().y(),1);
		path=robot.map().getPath();
		path.remove(0);
	}

	public static void main(String[] args) {
		RobotSticky robot = new RobotSticky(false, 675, 825, Math.PI);
		System.out.println("initialized");
		robot.setState(State.GoObject);
		new StateMachine(robot).start();
	}

}
