package game;

import global.Constants;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import org.junit.runner.notification.StoppedByUserException;

import math.geom2d.Angle2D;
import math.geom2d.Point2D;
import math.geom2d.line.Ray2D;

import robot.main.RobotSticky;
import robot.map.Localization;
import robot.map.MapForSensors;
import robot.map.MazeMap;
import straightedge.geom.KPoint;
import vision.NewImageProcessor;
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
	private Localization localization;

	private MapForSensors mapForSensors;

	private MazeMap mazeMap;
	GUI gui;

	public static enum State {
		Start, LookAround, GoBall, GoForward, GoReactor, GoSilo, LookAway, DriveBlind, StickyState, ReactorDeposit, FollowWall, AlignSilo, Turn90, FindYellowWall, CollectSilo, AllignReactor, GoObject, Stop, DriveStraight, FaceObject, TimedOut, FreeLeft,
	}

	public static enum Goal {
		Silo, Reactor
	}

	public static enum WallSide {
		Left, Right, Undecided
	}

	double time;
	private int i = 0;

	private RobotSticky robot;

	private ArrayList<KPoint> path;
	/**
	 * number of reactors whose position are known
	 */
	private int knownReactors = 0;

	private int[] scoredBottom = new int[3];
	private int[] scoredTop = new int[3];

	private Counters counters = new Counters();
	/**
	 * signals whether balls where collected from silo
	 */
	private boolean siloDone = false;

	/**
	 * tells whether there are still balls in silo
	 */
	private boolean siloHasBalls = true;
	
	private boolean hadWallInFront=false;

	/**
	 * used to count tries of collecting from the silo
	 */
	private int failedToCollectBalls = 0;

	/**
	 * at each step tells whether a path finding algorithm should be run or not
	 */
	private boolean shouldSearch = false;

	/**
	 * used when forcing to turn a specific angle
	 */
	private double angleToTurn = Math.PI;

	/**
	 * wall following
	 */
	private WallSide side = WallSide.Undecided;

	WallGUI wgui = new WallGUI();

	private Goal goal;

	private int t = 0;

	Logger log = new Logger();
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

	List<Double> xsMoved = new ArrayList<Double>();
	List<Double> ysMoved = new ArrayList<Double>();

	private State timedOutState;
	/**
	 * Movement goals
	 */
	private double distance = 0;
	private double dx = 0, dy = 0;
	private double angle = 0;

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public void start() {
		time = System.nanoTime();
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
		counters.missedObject();
		if (counters.lostObject()) {
			angle = 0;
			distance = 15;
			robot.driverReset();
			robot.setState(State.FollowWall);
		}
		distance -= robot.distanceMoved();
		angle -= robot.angleMoved();

	}

	public void correctMovement() {

	}

	public void correctPathFollowing() {

		if (Math.abs(angle) > Math.PI / 6)
			distance = 0;

		// TODO implement wall correction
	}

	public void correctForWalls() {

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

	private void continueMove() {

	}

	private void checkDistanceTimeout() {
		xsMoved.add(robot.odometry().xMoved());
		ysMoved.add(robot.odometry().yMoved());

		if (xsMoved.size() > 90) {
			xsMoved.remove(0);
			ysMoved.remove(0);
			double sumx = 0, sumy = 0;
			for (int i = 0; i < xsMoved.size(); i++) {
				sumx += Math.sqrt(xsMoved.get(i)*xsMoved.get(i)+ysMoved.get(i)*ysMoved.get(i));
				sumy += ysMoved.get(i);
			}
			if (sumx  < 8) {
				timedOutState = robot.state();
				xsMoved.clear();
				ysMoved.clear();
				counters.setStepsCounter(60);
				robot.setState(State.TimedOut);
			}
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
		// System.out.println(robot.state().toString() + " distance:" + distance
		// + " angle: " + angle * 180 / Math.PI);
		robot.update();
		vision.Timer timer = new vision.Timer();
		timer.start();
		localization.localize();
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
		case Stop:
			stop();
			break;
		default:
			System.out.println("noo such state?");
			break;
		}

		this.correctMovement();

	}

	private void calculateDistance() {
		distance = Math.signum(dy) * Math.sqrt(dx * dx + dy * dy);

	}

	public void run() {
		log.step();
		log.log("-------------\n" + robot.state().toString() + " " + side
				+ " distance:" + distance + " angle  "
				+ Constants.formatAngle(angle) * 180 / Math.PI + "  dx  " + dx
				+ "   dy  " + dy);
		t++;

		// update sensors
		robot.update();
		if (newVisionAvailable.compareAndSet(true, false)) {
			cameraWasUpdated = true;
			camera = globalDetector.clone();
			oldVisionConsumed.getAndSet(true);
			// wgui.setLines(camera);

			// gui.setVison(camera.seesGreenBall(), camera.seesRedBall(),
			// camera.biggestBall().distance(), camera.biggestBall().angle());

		}
		if (1 == 0) {
			log.log("wall leftttttttttt dx" + camera.leftWall().x() + " dy "
					+ camera.leftWall().y() + " dist "
					+ camera.leftWall().distance() + " angle "
					+ camera.leftWall().angle() * 180 / Math.PI);
			log.log("wall center" + camera.centerWall().x() + " "
					+ camera.centerWall().y() + " "
					+ camera.centerWall().distance() + " "
					+ camera.centerWall().angle() * 180 / Math.PI);
		}
		// move the robot
		// robot.move(0, 0);
		robot.move(distance, Constants.formatAngle(angle));

		// start the state machine
		switch (robot.state()) {
		case FollowWall:
			followWall();
			break;
		case FreeLeft:
			freeLeft();
			break;
		case DriveStraight:
			driveStraight();
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
		case AllignReactor:
			allignReactor();
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
		case TimedOut:
			timedOut();
			break;
		default:
			System.out.println("noo such state?");
			break;
		}

		this.correctMovement();
		cameraWasUpdated = false;
	}

	public void goObject() {
		while (localization.getPosition().isClose(path.get(0))) {
			path.remove(0);
			if (path.size() == 0)
				break;
		}
		if (path.isEmpty()) {
			robot.setState(State.FaceObject);
			distance = 0;
		} else {
			double dist = localization.getPosition().distance(path.get(0));
			distance = dist;
			angle = localization.getPosition().angle(path.get(0));
		}
		correctPathFollowing();
		correctForWalls();
	}

	public void stop() {
		robot.stop();
		System.exit(0);
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
			if (camera.seesBall()) {
				System.out.println("SAW BALL FROM LOOK AROUND"
						+ camera.biggestBall().x() + "  "
						+ camera.biggestBall().y());
				robot.setState(State.GoBall);
				counters.resetMissedObject();
			} else if (camera.seesWallLeft()) {
				robot.setState(State.FollowWall);
			}

		} else if (Math.abs(angleToTurn) < Math.PI / 60) {
			robot.setState(State.GoForward);
		}

		// set the required speeds
		distance = 0;

	}

	public void timedOut() {
		counters.stepped();
		if (counters.done()) {
			robot.setState(State.FollowWall);
		} else {
			angle = -Math.PI/4;
			distance = -50;
		}
		checkDistanceTimeout();
	}

	public void goForward() {
		angle = 0.0;
		distance = 50;
		if (camera.seesWallLeft()) {
			robot.setState(State.FollowWall);
		}
	}

	public void freeLeft() {
		angle -= robot.odometry().angleMoved();
		distance = 5;
		if (Math.abs(angle) < Math.PI / 30) {
			robot.setState(State.FollowWall);
		}

	}

	/**
	 * approaches the ball
	 */
	public void goBall() {
		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();
		angle = new Ray2D(0, 0, dy, dx).horizontalAngle();
		calculateDistance();
		distance += 20;

		if (cameraWasUpdated) {
			// take care of false positives and false negatives
			if (!camera.seesBall()) {
				updateMissed();
			} else {
				counters.resetMissedObject();
				log.log("SAW BALL");
				// calculate new ball distance
				dx = camera.biggestBall().x();
				dy = camera.biggestBall().y();
				angle = camera.biggestBall().angle();
				wgui.setCarrot(new Point2D(dx, dy));
				System.out.println("angle " + angle + "converterted angle"
						+ Constants.formatAngle(angle));
				angle = Constants.formatAngle(camera.biggestBall().angle());
				calculateDistance();
				// System.out.println("I AM AWAY FOR "+distance);

				distance += 20;
				robot.driverReset();
			}
		}
		if (distance < Constants.minDistanceToBall) {
			// System.out.println("ANGLE??????+" + angle);
			if (Math.abs(angle) < Math.PI / 3) {
				if (camera.seesWallCenter()){
					if (camera.centerWall().distance()<12){
						hadWallInFront=true;
					}
				}
				// System.out.println("  clooooooooooose");
				System.out
						.println("((((((((((((((distance to ball " + distance);
				counters.setStepsCounter(Constants.stepsForCatchingBall);
				robot.setState(State.DriveBlind);
				distance = Constants.distanceForCatchingBall + 20;

				robot.driverReset();
			}
		}
		checkDistanceTimeout();
	}

	public void driveBlind() {
		counters.stepped();
		distance -= robot.odometry().distanceMoved();
		System.out.println("drive blind distance " + distance);
		angle -= robot.odometry().angleMoved();
		// if (counters.done()) {
		if (distance < 30) {
			angle = Math.PI;
			dx = 0;
			dy = 0;
			distance = -30;
			robot.setState(State.TimedOut);
			robot.driverReset();
		}

		checkDistanceTimeout();
	}

	public void driveStraight() {
		log.log("distance moved " + robot.odometry().xMoved() + " "
				+ robot.odometry().yMoved() + " " + " angle moved "
				+ robot.odometry().angleMoved() * 180 / Math.PI);

		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();
		angle -= robot.odometry().angleMoved();
		calculateDistance();

		// if (distance < 0.03) {
		// robot.setState(State.Stop);
		// robot.hardware().move(0, 0);
		// }
	}

	/**
	 * Approaches the silo
	 */
	public void goSilo() {
		// take care of false positives and false negatives
		if (!camera.seesSilo()) {
			updateMissed();
		} else {
			// object seen so reset the miss count
			counters.resetMissedObject();

			// calculate the actual distance
			double distanceToSilo = camera.silo().distance();
			double angleToSilo = camera.silo().angle();

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
		Ray2D objectDirection = new Ray2D(localization.getPosition()
				.toPoint2D(), mazeMap.currentObject());
		Ray2D robotDirection = localization.getPosition().toRay2D();
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
		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();
		angle = new Ray2D(0, 0, dy, dx).horizontalAngle();
		calculateDistance();
		if (cameraWasUpdated) {

		}
	}

	public void allignReactor() {
		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();
		angle = new Ray2D(0, 0, dy, dx).horizontalAngle();
		calculateDistance();
		if (cameraWasUpdated) {
			if (camera.seesReactor()) {
				dx = camera.reactor().x();
				dy = camera.reactor().y();
				angle = camera.reactor().carrotAngle();
				calculateDistance();
				robot.driverReset();
			}
		}
		if (Math.abs(distance) < 5) {
			robot.setState(State.FollowWall);
			counters.startGiveUp();
			robot.driverReset();
		}
	}

	public void collectSilo() {
		// if (stepsLeft == Constants.stepsForSiloArm) {
		// robot.hardware().collectSilo();
		// } els0;//e if (stepsLeft == 0) {

		// check if managed to collect any balls
		if (robot.balls().redBalls() != 0) { // if yes
			// check if there are balls left
			if (camera.seesBigBall()) { // if no
				// turn away from the silo
				angleToTurn = Math.PI / 2;
				robot.setState(State.Turn90);
			} else {
				// reset the time counter for the arm movement
				// stepsLeft = Constants.stepsForSiloArm;
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

	// }

	public void followWall() {
		// angle -= robot.odometry().angleMoved();
		counters.giveUpStep();
		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();
		angle = new Ray2D(0, 0, dy, dx).horizontalAngle();
		calculateDistance();
		switch (side) {
		case Left:

			if (cameraWasUpdated) {
				distance = 40;
				if (!camera.seesWallLeft()) {
					side = WallSide.Undecided;
				} else {

					if ((!camera.leftWall().onLeft() || Math.abs(camera
							.leftWall().angle()) > Math.PI / 2.3)) {
						System.out.println("freeeee left");
						if (Math.abs(distance) < 5) {
							angle = Math.PI / 3;
							distance = 5;
							robot.setState(State.FreeLeft);
						}
					} else {

						camera.leftWall().makeCarrot(30, 25);
						double newangle = camera.leftWall().carrotAngle();
						double newdx = camera.leftWall().x();
						double newdy = camera.leftWall().y();
						calculateDistance();
						log.log("new distance " + distance + " " + angle);

						if (camera.seesWallCenter()) {
							if (camera.centerWall().distance() < 30
									&& Math.abs(camera.centerWall().angle()) > Math.PI / 4) {
								log.loga("correcting for center");
								camera.centerWall().makeCarrot(30, 25);
								newangle = camera.centerWall().carrotAngle();
								newdx = camera.centerWall().x();
								newdy = camera.centerWall().y();
								calculateDistance();
//								System.out.println("on left"
//										+ camera.centerWall().onLeft());

							}
						}

						double ratio = 0.5;
						dx = dx * ratio + newdx * (1 - ratio);
						dy = dy * ratio + newdy * (1 - ratio);
						angle = angle * ratio + (1 - ratio) * newangle;

						wgui.setCarrot(new Point2D(dx, dy));
						log.log("cameraaaaaaaaaaaa" + dx + " " + dy + " "
								+ angle);
						distance *= (Math.cos(angle) * Math.cos(angle));

					}
					// robot.driverReset();
				}
			}

			break;
		case Undecided:
			if (cameraWasUpdated) {
				if (camera.seesWallLeft()) {
					angle = camera.leftWall().carrotAngle();
					dx = camera.leftWall().x();
					dy = camera.leftWall().y();

				} else if (camera.seesWallCenter()) {
					angle = camera.leftWall().carrotAngle();
					dx = camera.leftWall().x();
					dy = camera.leftWall().y();
				} else {
					distance = 15;
				}
				wgui.setCarrot(new Point2D(dx, dy));
			}
			if (Math.abs(angle) < Math.PI / 3) {
				side = WallSide.Left;
			}

		}

		// TODO if sees a ball near reactor
		if (cameraWasUpdated) {
			if (camera.seesBall()) {
				 robot.setState(State.GoBall);
				 side = WallSide.Undecided;
			}

			if (camera.seesReactor() && !counters.shouldGiveUp()) {
				// robot.setState(State.AllignReactor);
			}
		}
		checkDistanceTimeout();
	}

	public StateMachine(RobotSticky robot) {
		this.robot = robot;

		gui = new GUI(mapForSensors, localization, mazeMap);

		// vision initialization
		oldVisionConsumed = new AtomicBoolean(true);
		newVisionAvailable = new AtomicBoolean(false);
		globalDetector = new VisionDetector();
		Thread visionThread = new Thread(new VisionRunnable(globalDetector,
				oldVisionConsumed, newVisionAvailable));
		visionThread.start();
		side = WallSide.Undecided;
		dx = 0;
		dy = 0;
		angle = 0;

	}

	public static void main(String[] args) {
		RobotSticky robot = new RobotSticky(true);

		System.out.println("initialized");
		robot.setState(State.FollowWall);
		new StateMachine(robot).start();
	}

}
