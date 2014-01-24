package game;

import global.Constants;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import math.geom2d.Angle2D;
import math.geom2d.Point2D;
import math.geom2d.line.Ray2D;

import robot.main.RobotSticky;
import straightedge.geom.KPoint;
import vision.VisionRunnable;
import vision.detector.ColorObject;
import vision.detector.VisionDetector;

/**
 * Represents the State Machine of the robot. Uses an instance of Robot and
 * performs the according movements based on current state of the world, e.g.
 * the number of reactors that were score, current time, etc.
 * 
 * The run() imitates one step and the State Machine is scheduled to work at
 * fixed clock rate
 * 
 * 
 */
public class StateMachine implements Runnable {
	public static enum State {
		Start, LookAround, GoBall, GoForward, GoReactor, GoSilo, LookAway, DriveBlind, StickyState, ReactorDeposit, FollowWall, AlignSilo, Turn90, FindYellowWall, CollectSilo, AllignReactor, GoObject, Stop, DriveStraight, FaceObject,
	}

	public static enum Goal {
		Silo, Reactor
	}

	public static enum WallSide {
		Left, Right, Undecided
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
	private boolean shouldSearch = false;
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

	/**
	 * wall following
	 */
	private WallSide side=WallSide.Undecided;
	
	
	private double distanceToBall;
	private double angleToBall;

	private Goal goal;

	/**
	 * To keep track of previous movement
	 */

	/**
	 * Vision related flags
	 */
	private VisionDetector globalDetector; // used only for reference with the
											// vision runnable
	private VisionDetector camera; // used to copy the result from the vision
									// runnable
	private AtomicBoolean oldVisionConsumed;
	private AtomicBoolean newVisionAvailable;
	private boolean cameraWasUpdated = false;

	/**
	 * Movement goals
	 */
	private double distance = 0;
	private double angle = 0;

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

	public void correctMovement() {

	}

	public void correctPathFollowing() {

		if (Math.abs(angle) > Math.PI / 6)
			distance = 0;

		// TODO implement wall correction
	}

	public void correctForWalls() {
		System.out.println("all readings " + robot.irs().getAsList());

		if (robot.irs().get(0) < Constants.minDistanceToWall) { // if the
																// leftmost
																// sensor is too
																// close to the
																// wall
			if (robot.irs().get(1) < robot.irs().get(0) + 1) { // if the robot
																// faces the
																// wall
				// if (angle>-Math.PI/3)
				angle = -Math.PI;
			}
		}

		if (robot.irs().get(4) < Constants.minDistanceToWall) { // if the
																// rightmost
																// sensor is too
																// close to the
																// wall
			if (robot.irs().get(3) < robot.irs().get(4) + 1) { // if the robot
																// faces the
																// wall
				// if (angle<Math.PI/3)
				angle = Math.PI;
			}
		}
		if (robot.irs().get(2) < Constants.minDistanceToWall) { // if a wall is
																// right in
																// front
			distance = 0;
		}
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
		System.out.println(robot.state().toString() + " distance:" + distance
				+ " angle: " + angle * 180 / Math.PI);
		robot.update();
		vision.Timer timer = new vision.Timer();
		timer.start();
		robot.localization().localize();
		timer.print("localization ");

		switch (robot.state()) {
		case GoObject:
			goObject();
			break;
		case LookAround:

			lookAround();
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

		this.correctMovement();

	}

	public void run() {
		System.out.println(robot.state().toString() + " distance:" + distance
				+ " angle: " + angle * 180 / Math.PI);
		// update sensors
		robot.update();
		// move the robot
		robot.move(distance, angle);

		// start the state machine

		switch (robot.state()) {
		case DriveStraight:
			break;
		case GoObject:
			goObject();
			break;
		case FaceObject:
			faceObject();
			break;
		case LookAround:
			lookAround();
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

		this.correctMovement();

	}

	public void goObject() {
		while (robot.localization().getPosition().isClose(path.get(0))) {
			path.remove(0);
			if (path.size() == 0)
				break;
		}
		if (path.isEmpty()) {
			robot.setState(State.FaceObject);
			distance = 0;
		} else {
			double dist = robot.localization().getPosition()
					.distance(path.get(0));
			distance = dist;
			angle = robot.localization().getPosition().angle(path.get(0));
		}
		correctPathFollowing();
		correctForWalls();
	}

	/**
	 * Looks around to see anything new
	 */
	public void lookAround() {
		angle -= robot.odometry().angleMoved();
		if (cameraWasUpdated) {
			/*
			 * if (robot.camera().seesSilo() && !siloDone) {
			 * robot.setState(State.GoSilo); stepsAllowedToMissObject =
			 * Constants.allowedToMiss; } else
			 */
			if (robot.camera().seesBall()) {
				robot.setState(State.GoBall);
				System.out.println(" angle to ball form camera "
						+ robot.camera().biggestBall().angle());
				stepsAllowedToMissObject = Constants.allowedToMiss;
			}
		} else if (Math.abs(angleToTurn) < 0.01) {
			robot.setState(State.GoForward);
		}

		// set the required speeds
		distance = 0;
		

	}

	public void goForward() {
		angle = 0.0;
		distance = 50;
		if (robot.seesWall()) {
			robot.setState(State.FollowWall);
		}
	}

	/**
	 * approaches the ball
	 */
	public void goBall() {
		if (cameraWasUpdated) {
			// take care of false positives and false negatives
			if (!camera.seesBall()) {
				updateMissed();
			} else { // calculate new ball distance
				distanceToBall = camera.biggestBall().distance()
						- Constants.minDistanceToBall;
				System.out.println("distance to ball "
						+ camera.biggestBall().distance());
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
		} else {

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

	public void driveStraight() {
		distance -= robot.odometry().distanceMoved();
		angle -= robot.odometry().angleMoved();
		if (distance < 0.03) {
			robot.setState(State.Stop);
			robot.hardware().move(0, 0);
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

	/**
	 * after we arrive at the object we have to turn towards it
	 */
	public void faceObject() {
		Ray2D objectDirection = new Ray2D(robot.localization().getPosition()
				.toPoint2D(), robot.map().currentObject());
		Ray2D robotDirection = robot.localization().getPosition().toRay2D();
		angle = Angle2D.angle(robotDirection, objectDirection);
		angle = Constants.formatAngle(angle);
		if (Math.abs(angle) < Math.PI / 120) {
			switch (goal) {
			case Silo:
				robot.setState(State.AlignSilo);
				break;
			case Reactor:
				robot.setState(State.AllignReactor);
				break;
			}
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

	public void followWall() {
		angle-=robot.odometry().angleMoved();
		if (cameraWasUpdated ) {
			switch(side){
			case Left:
				if (!camera.seesWallLeft()){
					angle=-Math.PI/2;
					side=WallSide.Undecided;
				} else {
					
				}
			}
			if (camera.seesBall()) robot.setState(State.GoBall);
		}
	}

	public StateMachine(RobotSticky robot) {
		this.robot = robot;

		// vision initialization
		oldVisionConsumed = new AtomicBoolean(true);
		newVisionAvailable = new AtomicBoolean(false);
		globalDetector = new VisionDetector();
		Thread visionThread = new Thread(new VisionRunnable(globalDetector,
				oldVisionConsumed, newVisionAvailable));

	}

	public static void main(String[] args) {
		RobotSticky robot = new RobotSticky(false);
		System.out.println("initialized");
		robot.setState(State.GoObject);
		new StateMachine(robot).start();
	}

}
