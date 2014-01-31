package game;

import global.Constants;

import java.util.IllegalFormatCodePointException;
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
import math.geom2d.line.StraightLine2D;

import robot.main.RobotSticky;
import robot.map.Localization;
import robot.map.MapForSensors;
import robot.map.MazeMap;
import straightedge.geom.KPoint;
import vision.NewImageProcessor;
import vision.VisionRunnable;
import vision.detector.ColorObject;
import vision.detector.ColorObject.Type;
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
		Start, LookAround, GoBall, GoForward, GoReactor, GoSilo, LookAway, DriveBlind, StickyState, ReactorDeposit, FollowWall, AlignSilo, Turn90, FindYellowWall, CollectSilo, AllignReactor, GoObject, Stop, DriveStraight, FaceObject, TimedOut, FreeLeft, FaceReactor, BackUp, DumpGreen, DumpGreenTop, DumpGreenBottom,
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

	private boolean hadWallInFront = false;

	private int noball = 0;
	/**
	 * used to count tries of collecting from the silo
	 */
	private int failedToCollectBalls = 0;

	/**
	 * at each step tells whether a path finding algorithm should be run or not
	 */
	private boolean shouldSearch = false;

	boolean dumped = false;

	/**
	 * used when forcing to turn a specific angle
	 */
	private double angleToTurn = Math.PI;

	/**
	 * wall following
	 * 
	 */

	private double xMovedToCarrot = 0, yMovedToCarrot = 0;

	StraightLine2D oldWall;
	private WallSide side = WallSide.Undecided;

	WallGUI wgui = new WallGUI();

	private Goal goal;

	private int t = 0;

	double freeDistance = 0;
	double freeAngle = 0;

	boolean thinksHasGreen = false;
	private boolean dumpedBottom = false;
	Logger log = new Logger();
	/**
	 * To keep track of previous movement
	 */

	/**
	 * Vision related flags
	 */
	private VisionDetector globalDetector; // used only for reference with the
											// vision runnable
	private VisionDetector camera = new VisionDetector(); // used to copy the
															// result from the
															// vision
	// runnable
	private AtomicBoolean oldVisionConsumed;
	private AtomicBoolean newVisionAvailable;
	private boolean cameraWasUpdated = false;
	private double lastAngleSeen;

	List<Double> xsMoved = new ArrayList<Double>();
	List<Double> ysMoved = new ArrayList<Double>();

	private State timedOutState;
	/**
	 * Movement goals
	 */

	private boolean pOnly = false;
	private double distance = 0;
	private double dx = 0, dy = 0;
	private double angle = 0;

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	private double beginTime;

	private boolean continueToCarrot;

	private boolean haventTouched;

	private boolean waitsFalling;

	private boolean notScoring;

	private double targetAngle;

	public void start() {
		beginTime = System.nanoTime();

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
			noball = 10;
			angle = 0;
			distance = 15;
			robot.driverReset();
			counters.stateStep();
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
				sumx += Math.sqrt(xsMoved.get(i) * xsMoved.get(i)
						+ ysMoved.get(i) * ysMoved.get(i));
				sumy += ysMoved.get(i);
			}
			log.log("timeouted distance   " + sumx);
			if (sumx < 6 && (System.nanoTime() - beginTime) / 1000000000 > 10) {
				timedOutState = robot.state();
				xsMoved.clear();
				ysMoved.clear();
				counters.setStepsCounter(40);
				robot.setState(State.TimedOut);
			}
		}

	}

	public void logCamera() {
		if (camera.seesWallLeft()) {
			log.log("left wall: carrot x: " + camera.leftWall().x()
					+ ", carrot y: " + camera.leftWall().y()
					+ ", distance to wall" + camera.leftWall().distance()
					+ " angle to wall " + camera.leftWall().angle() * 180
					/ Math.PI);
			log.flush();
		}

		if (camera.seesWallCenter()) {
			log.log("center wall: carrot x: " + camera.centerWall().x()
					+ ", carrot y: " + +camera.centerWall().y()
					+ ", distance to wall" + camera.centerWall().distance()
					+ " angle to wall " + camera.centerWall().angle() * 180
					/ Math.PI);
			log.flush();
		}

		if (camera.seesWallRight()) {
			log.log("right wall: carrot x: " + camera.rightWall().x()
					+ ", carrot y: " + +camera.rightWall().y()
					+ ", distance to wall" + camera.rightWall().distance()
					+ " angle to wall " + camera.rightWall().angle() * 180
					/ Math.PI);
			log.flush();
		}

		if (camera.seesRedBall()) {
			log.log("red ball: x: " + camera.redBall().x() + ", red ball y: "
					+ +camera.redBall().y());

		}

		if (camera.seesGreenBall()) {
			log.log("    green ball: x: " + camera.greenBall().x()
					+ ", green ball y: " + +camera.greenBall().y());
			log.flush();
		}

		if (camera.seesSilo()) {
			log.log("silo: x: " + camera.silo().x() + ", silo y: "
					+ +camera.silo().y());

		}

		if (camera.seesReactor()) {
			log.log("silo: x: " + camera.reactor().x() + ", silo y: "
					+ +camera.reactor().y());

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

	private void checkTime(double time) {
		if ((time - this.beginTime) / 1000000000 > 180.0) {
			robot.setState(State.Stop);
		}
	}

	private void calculateDistance() {
		distance = Math.signum(dy) * Math.sqrt(dx * dx + dy * dy);

	}

	public void run() {
		try {

			log.step();
			double currentTime = System.nanoTime();

			log.log("-----------------------------   "
					+ (currentTime - beginTime) / 1000000000.0 + "\n"
					+ robot.state().toString() + "\n" + " distance to go: "
					+ distance + " angle to go: "
					+ Constants.formatAngle(angle) * 180 / Math.PI + "  dx  "
					+ dx + "   dy  " + dy);

			checkTime(currentTime);
			// update sensors
			robot.update();
			if (newVisionAvailable.compareAndSet(true, false)) {
				cameraWasUpdated = true;
				camera = globalDetector.clone();
				oldVisionConsumed.getAndSet(true);
			}

			// move the robot
			// robot.move(0, 0);
			robot.move(distance, Constants.formatAngle(angle), pOnly);

			// start the state machine
			switch (robot.state()) {
			case FaceReactor:
				faceReactor();
				break;
			case DumpGreenTop:
				dumpGreenTop();
				break;
			case DumpGreenBottom:
				dumpGreenBottom();
				break;
			case BackUp:
				backUp();
				break;
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
			case Stop:
				stop();
				break;
			default:
				System.out.println("noo such state?");
				break;
			}

			this.correctMovement();
			cameraWasUpdated = false;

		} catch (Exception e) {
			e.printStackTrace();
		}
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
		// angle -= robot.odometry().angleMoved();
		angle = targetAngle-robot.odometry().theta();
		calculateDistance();
		if (camera.seesBall()) {
			if (noball > 0)
				noball--;
			else if (camera.seesReactor()) {
				if (!(camera.biggestBall().distance() + 8 > camera
						.reactor().distance())) {
					robot.driverReset();
					robot.setState(State.GoBall);
					side = WallSide.Undecided;
					log.loga("saw a new ball from Follow Wall BUT TOO CLOSSSEEE \n");
				}
			} else {
				robot.driverReset();
				robot.setState(State.GoBall);
				side = WallSide.Undecided;
				log.loga("saw a new ball from Follow Wall OOOOK \n");
			}
		}
		if (angle<Math.PI/20){
			robot.setState(State.GoForward);
		}				
	}

	public void timedOut() {
		counters.stepped();
		if (counters.done()) {
			robot.setState(State.FollowWall);
		} else {
			angle = -Math.PI / 4;
			distance = -80;
		}
		checkDistanceTimeout();
	}

	public void goForward() {
		angle = 0.0;
		distance = 40;
		if (camera.seesWallLeft()) {
			robot.setState(State.FollowWall);
		}
	}

	public void freeLeft() {
		angle -= robot.odometry().angleMoved();
		distance = 30;
		if (Math.abs(freeAngle) < Math.PI / 30) {
			robot.setState(State.FollowWall);
		}
		checkwallBump();
		checkBumpers();
	}

	/**
	 * approaches the ball
	 */
	public void goBall() {
		counters.stateStep();
		pOnly = true;
		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();

		angle = targetAngle - robot.odometry().theta();
		calculateDistance();

		if (cameraWasUpdated) {
			System.out.println("new camera frame");

			robot.hardware().resetFrame();
			robot.driverReset();
			// take care of false positives and false negatives
			if (!camera.seesBall()) {
				updateMissed();
				log.loga("couldn't find the ball again \n");
			} else {
				counters.resetMissedObject();
				log.log("saw a ball");
				// calculate new ball distance
				double newdx = camera.biggestBall().x();
				double newdy = camera.biggestBall().y();
				double newangle = Constants.formatAngle(Math.atan2(dx, dy));
				wgui.setCarrot(new Point2D(dx, dy));
				calculateDistance();
				// System.out.println("I AM AWAY FOR "+distance);
				if (distance < 9) {
					robot.hardware().resetFrame();
					if (camera.biggestBall().type() == Type.GreenBall) {
						thinksHasGreen = true;
					}
					// System.out.println("ANGLE??????+" + angle);
					if (Math.abs(angle) < Math.PI / 3) {
						if (camera.seesWallCenter()) {
							if (camera.centerWall().distance() < 25) {
								hadWallInFront = true;
							}
						}
						// System.out.println("  clooooooooooose");
						System.out.println("((((((((((((((distance to ball "
								+ distance);
						counters.setStepsCounter(40);
						robot.setState(State.DriveBlind);
						distance = Constants.distanceForCatchingBall;

						robot.driverReset();
					}
				}
				double r = 0.0;
				dx = dx * r + (1 - r) * newdx;
				dy = dy * r + (1 - r) * newdy;
				angle = angle * r + (1 - r) * newangle;
				targetAngle = angle + Math.PI / 2;
				// distance += 10;
				distance *= Math.cos(angle) * Math.cos(angle);
				robot.driverReset();
			}
		}
		checkDistanceTimeout();

		if (camera.seesWallCenter()) {
			if (camera.centerWall().distance() < 30) {
				distance *= camera.centerWall().distance() / 30;
			}
		}
		if (camera.seesReactor()) {
			if (camera.reactor().distance() < 30) {
				if (Math.abs(camera.reactor().angleToCenter()) < Math.PI / 5) {
					distance /= 2;
				} else if (camera.reactor().angle() < 0) {
					if (angle < Math.PI / 3) {
						angle = Math.PI / 3;
					}
				} else if (angle > -Math.PI / 3) {
					angle = -Math.PI / 3;
				}
			}
		}
		checkCloseWalls();
		checkwallBump();
		checkBumpers();
	}

	public void checkCloseWalls() {
		double r = 0.9;

		if (camera.seesWallLeft()) {
			if (camera.leftWall().distance() < 23) {
				angle = r * angle - (1 - r) * Math.PI / 2;
			}
		} else if (!robot.hardware().wallLeftBump.getValue()) {
			angle = r * angle + (1 - r) * Math.PI / 2;
		}

		if (camera.seesWallRight()) {
			if (camera.rightWall().distance() < 23) {
				angle = r * angle + (1 - r) * Math.PI / 2;
			}
		}
	}

	public void driveBlind() {
		
		counters.stepped();
		distance = 25;
		angle -= robot.odometry().angleMoved();
		if (!robot.hardware().leftBump.getValue()
				&& robot.hardware().rightBump.getValue()) {
			distance = 5;
			angle = Math.PI / 6;
		}
		if (robot.hardware().leftBump.getValue()
				&& !robot.hardware().rightBump.getValue()) {
			distance = 5;
			angle = -Math.PI / 6;
		}

		if (!robot.hardware().leftBump.getValue()
				&& !robot.hardware().rightBump.getValue()) {
			distance = 10;
			angle = 0;
		}
		if (counters.done()) {
			if (hadWallInFront) {
				angle = Math.PI;
				distance = -30;
				counters.setStepsCounter(30);
				robot.setState(State.TimedOut);
			} else {
				counters.startStateSteps();
				robot.setState(State.FollowWall);
			}
			dx = 0;
			dy = 0;
			robot.driverReset();
		}

		checkDistanceTimeout();
	}

	public void driveStraight() {

		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();
		angle = Math.atan2(dx, -dy);
		calculateDistance();
		if (distance < 6)
			angle = 0;
		distance = 23;

		double r = 0.8;

		if (!robot.hardware().leftBump.getValue()
				&& !robot.hardware().rightBump.getValue()) {
			if (haventTouched) {
				haventTouched = false;
				distance = 25;
				counters.setStepsCounter(30);
			}
		} else if (!robot.hardware().leftBump.getValue()) {
			angle = angle * r + (1 - r) * Math.PI / 4;
			distance = 0;
		} else if (!robot.hardware().rightBump.getValue()) {
			distance = 0;
			angle = angle * r - (1 - r) * Math.PI / 4;
		}

		if (!haventTouched) {
			distance = 20;
			angle = 0;
			counters.stepped();
			if (counters.done()) {
				distance = 0;
				angle = 0;
				counters.startStepCounter();
				waitsFalling = false;
				robot.setState(State.DumpGreenTop);
			}
		}

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
		}

	}

	/**
	 * after we arrive at the object we have to turn towards it
	 */
	public void faceObject() {
		pOnly = false;
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
		checkwallBump();
	}

	public void allignSilo() {
		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();
		angle = new Ray2D(0, 0, dy, dx).horizontalAngle();
		calculateDistance();
		if (cameraWasUpdated) {

		}
	}

	public void faceReactor() {
		pOnly = false;

		angle =targetAngle -robot.odometry().theta();
		if (cameraWasUpdated) {
			System.out.println("camera was updated");

			if (camera.seesReactor()) {
				robot.hardware().resetFrame();
				dx = camera.reactor().goalX();
				dy = camera.reactor().goalY();
				angle = camera.reactor().angleToCenter();
				targetAngle=angle+Math.PI/2;
				distance = 0;
				robot.driverReset();
			}
		}

		if (Math.abs(angle) < Math.PI / 90) {
			calculateDistance();
			distance += 5;
			haventTouched = true;
			robot.setState(State.DriveStraight);
		}
		checkDistanceTimeout();
	}

	public void allignReactor() {
		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();

		angle = targetAngle - robot.odometry().theta();
		
		calculateDistance();
		
		if (distance < 20)
			distance=20;
		
		if (notScoring && distance < 13) {
			robot.setState(State.FollowWall);
			counters.startGiveUp();

		}

		if (dx < 0 || dy < 0 || distance < 4) {
			angle = 0;
		}
		if (cameraWasUpdated) {
			robot.hardware().resetFrame();
			if (camera.seesReactor()) {
				dx = camera.reactor().x();
				dy = camera.reactor().y();
				angle = Math.atan2(dx, dy);
				lastAngleSeen = camera.reactor().angleToCenter();
				calculateDistance();
				robot.driverReset();
			}
		}
		if (Math.abs(distance) < 10) {
			pOnly=true;
			angle = lastAngleSeen;
			targetAngle=angle+Math.PI/2;;
			robot.setState(State.FaceReactor);
			robot.driverReset();
		}

		checkCloseWalls();
		checkwallBump();
		checkBumpers();
	}

	public void dumpGreenTop() {
		counters.countStep();
		if (counters.steps() == 20 && !waitsFalling) {
			System.out.println("dumping");
			robot.hardware().dumpGreenBall();
		}
		if (counters.steps() > 15) {

			if (!robot.hardware().dispenserBeam.getValue()) {
				robot.hardware().stopDispencer();
				counters.startStepCounter();
				waitsFalling = true;
			}
		}
		if (waitsFalling) {
			if (counters.steps() == 23) {
				robot.hardware().resetGreenDump();
				if (robot.balls().greenBalls() > 0) {
					if (!dumpedBottom) {
						waitsFalling = false;
						counters.startStepCounter();
						robot.setState(State.DumpGreenBottom);
					} else {
						waitsFalling = false;
						counters.startStepCounter();
					}

				} else {
					waitsFalling = false;
					distance = -20;
					angle = 0;
					counters.setStepsCounter(90);
					counters.startGiveUp();
					robot.setState(State.BackUp);
				}

			}
		}
		if (counters.steps() > 150) {
			distance = -20;
			angle = 0;
			counters.setStepsCounter(90);

			counters.startGiveUp();
			robot.setState(State.BackUp);
		}

	}

	public void dumpGreenBottom() {
		counters.countStep();
		System.out.println(counters.steps());
		if (counters.steps() == 15) {
			robot.hardware().prepareBottomDump();
			System.out
					.println("OPPPPPPPPPPPPPPPPPPPENNNNNNNNNNING SERVOOOOOOOOOOOOOOOOO");
		}
		if (counters.steps() == 60) {
			robot.hardware().dumpGreenBall();
			System.out
					.println("BOTTTTTTTTTTTTTTTTTTTTOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
		}
		if (counters.steps() > 60) {
			if (!robot.hardware().dispenserBeam.getValue()) {
				robot.hardware().stopDispencer();
				waitsFalling = true;
			}
		}

		if (waitsFalling) {
			if (counters.steps() == 40) {
				robot.hardware().resetGreenDump();
				if (robot.balls().greenBalls() > 0) {
					waitsFalling = false;
					counters.startStepCounter();
					robot.setState(State.DumpGreenTop);
				} else {
					waitsFalling = false;
					distance = -20;
					angle = 0;
					counters.setStepsCounter(90);
					counters.startGiveUp();
					robot.setState(State.BackUp);
				}
			}
		}

		if (counters.steps() > 120) {
			distance = -20;
			angle = 0;
			counters.setStepsCounter(90);

			counters.startGiveUp();
			robot.setState(State.BackUp);
		}
	}

	public void dumpGreen() {
		counters.countStep();
		System.out.println(counters.steps());
		if (counters.steps() == 1 || counters.steps() == 120) {

			System.out.println("hghfhj" + counters.steps());
			robot.hardware().dumpGreenBall();

		}

		if ((counters.steps() <= 90 && counters.steps() > 20)
				|| (counters.steps() <= 220 && counters.steps() > 120)) {
			dumped = dumped || robot.hardware().checkIfDumped();
		}

		if (counters.steps() == 91 && counters.steps() == 130) {
			if (dumped) {
				robot.balls().dispensedGreen();
				dumped = false;
			}

			if (dumpedBottom) {
				counters.startStepCounter();
			}
			if (robot.balls().greenBalls() == 0) {

				dumpedBottom = false;
				counters.startGiveUp();
				robot.setState(State.BackUp);
			}
		}
		if (counters.steps() == 91) {
			robot.hardware().prepareBottomDump();
			dumpedBottom = true;
		}
		;

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

	/*
	 * public void followWall() { // angle -= robot.odometry().angleMoved();
	 * counters.giveUpStep(); dx -= robot.odometry().xMoved(); dy -=
	 * robot.odometry().yMoved(); angle = new Ray2D(0, 0, dy,
	 * dx).horizontalAngle(); calculateDistance(); switch (side) { case Left:
	 * 
	 * if (cameraWasUpdated) { distance = 40; if (!camera.seesWallLeft()) { side
	 * = WallSide.Undecided; } else {
	 * 
	 * if ((!camera.leftWall().onLeft() || Math.abs(camera .leftWall().angle())
	 * > Math.PI / 2.3)) { System.out.println("freeeee left"); if
	 * (Math.abs(distance) < 5) { angle = Math.PI / 3; distance = 5;
	 * robot.setState(State.FreeLeft); } } else {
	 * 
	 * camera.leftWall().makeCarrot(30, 25); double newangle =
	 * camera.leftWall().carrotAngle(); double newdx = camera.leftWall().x();
	 * double newdy = camera.leftWall().y(); calculateDistance();
	 * log.log("new distance " + distance + " " + angle);
	 * 
	 * if (camera.seesWallCenter()) { if (camera.centerWall().distance() < 30 &&
	 * Math.abs(camera.centerWall().angle()) > Math.PI / 4) {
	 * log.loga("correcting for center"); camera.centerWall().makeCarrot(30,
	 * 25); newangle = camera.centerWall().carrotAngle(); newdx =
	 * camera.centerWall().x(); newdy = camera.centerWall().y();
	 * calculateDistance(); // System.out.println("on left" // +
	 * camera.centerWall().onLeft());
	 * 
	 * } }
	 * 
	 * double ratio = 0.5; dx = dx * ratio + newdx * (1 - ratio); dy = dy *
	 * ratio + newdy * (1 - ratio); angle = angle * ratio + (1 - ratio) *
	 * newangle;
	 * 
	 * wgui.setCarrot(new Point2D(dx, dy)); log.log("cameraaaaaaaaaaaa" + dx +
	 * " " + dy + " " + angle); distance *= (Math.cos(angle) * Math.cos(angle));
	 * 
	 * } // robot.driverReset(); } }
	 * 
	 * break; case Undecided: if (cameraWasUpdated) { if (camera.seesWallLeft())
	 * { angle = camera.leftWall().carrotAngle(); dx = camera.leftWall().x(); dy
	 * = camera.leftWall().y();
	 * 
	 * } else if (camera.seesWallCenter()) { angle =
	 * camera.leftWall().carrotAngle(); dx = camera.leftWall().x(); dy =
	 * camera.leftWall().y(); } else { distance = 15; } wgui.setCarrot(new
	 * Point2D(dx, dy)); } if (Math.abs(angle) < Math.PI / 3) { side =
	 * WallSide.Left; }
	 * 
	 * }
	 * 
	 * // TODO if sees a ball near reactor if (cameraWasUpdated) { if
	 * (camera.seesBall()) { robot.setState(State.GoBall); side =
	 * WallSide.Undecided; }
	 * 
	 * if (camera.seesReactor() && !counters.shouldGiveUp()) { //
	 * robot.setState(State.AllignReactor); } } checkDistanceTimeout(); }
	 */

	public void backUp() {
		counters.stepped();
		if (counters.done()) {
			robot.setState(State.FollowWall);
		}
	}

	public void followWall() {
		counters.stateStep();
		// angle -= robot.odometry().angleMoved();
		pOnly = true;
		counters.giveUpStep();
		dx -= robot.odometry().xMoved();
		dy -= robot.odometry().yMoved();

		angle = targetAngle - robot.odometry().theta();

		xMovedToCarrot += robot.odometry().xMoved();
		yMovedToCarrot += robot.odometry().yMoved();

	/*	if ((Math.abs(dx) < 1 || Math.abs(dy) <1 )
				&& Math.abs(angle) > Math.PI / 6) {
			if (angle <0) angle*=-1;
		}*/
		calculateDistance();
		switch (side) {
		case Left:
			StraightLine2D newWall;
			if (continueToCarrot) {
				if (Math.abs(distance) < 5) {
					System.out.println("reached the carrot");
					continueToCarrot = false;
					robot.setState(State.FreeLeft);
				}
			}
			if (cameraWasUpdated) {
				boolean shouldChange = false;

				System.out.println("new camera frame");
				robot.hardware().resetFrame();
				robot.driverReset();
				if (!camera.seesWallLeft()) {
					side = WallSide.Undecided;
					log.loga("couldn't see left wall");
					log.flush();
				} else {
					double allowed = 33;
					if (camera.seesWallRight()) {
						if (camera.rightWall().distance() > 40) {
							allowed = 35;
						}
					}
					// camera.leftWall().makeCarrot(30, allowed);
					double newdx = camera.leftWall().x();
					double newdy = camera.leftWall().y();
					double newangle = Constants.formatAngle(Math.atan2(newdx,
							newdy));
					double newDist = camera.leftWall().distance();
					// if (newDist<23) shouldChange=true;
					newWall = camera.leftWall().line();
					if (camera.seesWallCenter()) {
						if (camera.centerWall().distance() < 60
								&& Math.abs(camera.centerWall().angle()) > Math.PI / 4) {
							log.log("correcting for center center \n");
							// camera.centerWall().makeCarrot(30, allowed);
							newdx = camera.centerWall().x();
							newdy = camera.centerWall().y();
							newangle = Constants.formatAngle(Math.atan2(newdx,
									newdy));
							newDist = camera.centerWall().distance();
							log.log("completely new carrot: distance "
									+ newDist + ", angle " + newangle + ", dx "
									+ newdx + ", dy " + newdy);
							log.flush();

							newWall = camera.leftWall().line();
							calculateDistance();
						}
					}

					if (angle < -Math.PI / 4)
						distance = -50;

					double ratio = 0.4;

					dx = dx * ratio + newdx * (1 - ratio);
					dy = dy * ratio + newdy * (1 - ratio);
					angle = angle * ratio + (1 - ratio) * newangle;
					targetAngle = angle + Math.PI / 2;
					calculateDistance();
					if (oldWall != null) {

						if (oldWall.projectedPoint(xMovedToCarrot,
								yMovedToCarrot).distance(xMovedToCarrot,
								yMovedToCarrot) + 25 < newDist) {
							System.out.println("FOOOOOOOOO");
							continueToCarrot = true;
						}
					}
					log.log("new carrot: distance " + distance + ", angle "
							+ angle + ", dx " + dx + ", dy " + dy);
					log.flush();
					wgui.setCarrot(new Point2D(dx, dy));
					// distance *= Math.cos(angle);

					log.log("moved to carrot x: " + xMovedToCarrot
							+ " moved to carrot y:" + yMovedToCarrot + "");
					xMovedToCarrot = 0;
					yMovedToCarrot = 0;
					oldWall = newWall;
				}
				robot.driverReset();
			}

			break;
		case Undecided:
			if (cameraWasUpdated) {
				robot.hardware().resetFrame();
				log.log("Still wall undecided \n");
				if (camera.seesWallLeft()) {
					dx = camera.leftWall().x();
					dy = camera.leftWall().y();
					angle = Constants.formatAngle(Math.atan2(dx, dy));
					if (Math.abs(angle) < Math.PI / 3
							&& camera.leftWall().onLeft()) {
						side = WallSide.Left;
					}
				} else if (camera.seesWallCenter()) {
					dx = camera.centerWall().x();
					dy = camera.centerWall().y();
					if (Math.abs(angle) < Math.PI / 3
							&& camera.centerWall().onLeft()) {
						side = WallSide.Left;
					}
					angle = Constants.formatAngle(Math.atan2(dx, dy));
				} else {
					distance = 30;
				}
				wgui.setCarrot(new Point2D(dx, dy));
			}

		}

		if (cameraWasUpdated) {

			if (camera.seesReactor()) {
				
				System.out.println("SAW REACTOR RRRR SHOULD GIVE UP" + counters.shouldGiveUp()+ " "+robot.balls().greenBalls());
				if (robot.balls().greenBalls() > 0 && !counters.shouldGiveUp()) {
					System.out.println("HAVEEEEEEEEEEEE BALLLLLLLLLLLLLLLLLS");
					robot.setState(State.AllignReactor);
					notScoring=false;
				} else {
					notScoring=true;
					Point2D reactor = new Point2D(camera.reactor().goalY(),
							camera.reactor().goalX());
					StraightLine2D carrotLine = new StraightLine2D(new Point2D(
							0, 0), new Point2D(dy, dx));
					if (carrotLine.projectedPoint(reactor).distance(reactor) < 20) {
						System.out
								.println("ALLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLIGNING BUT NOT SCORING");
						robot.setState(State.AllignReactor);
						notScoring = true;
					}
				}

			}

			if (camera.seesBall()) {
				if (noball > 0)
					noball--;
				else if (camera.seesReactor()) {
					if (!(camera.biggestBall().distance() + 8 > camera
							.reactor().distance())) {
						robot.driverReset();
						robot.setState(State.GoBall);
						side = WallSide.Undecided;
						log.loga("saw a new ball from Follow Wall BUT TOO CLOSSSEEE \n");
					}
				} else {
					robot.driverReset();
					robot.setState(State.GoBall);
					side = WallSide.Undecided;
					log.loga("saw a new ball from Follow Wall OOOOK \n");
				}
			}
		}
		checkwallBump();
		checkBumpers();
		checkDistanceTimeout();
	}

	public void checkwallBump() {
		double r = 0.8;
		if (!robot.hardware().wallLeftBump.getValue()) {
			angle = r * angle + (1 - r) * Math.PI / 2;
		}

	}

	public void checkBumpers() {
		if (!robot.hardware().leftBump.getValue()
				&& !robot.hardware().rightBump.getValue()) {
			counters.setStepsCounter(70);
			distance = -40;
			robot.setState(State.BackUp);
		} else if (!robot.hardware().leftBump.getValue()) {
			counters.setStepsCounter(70);
			angle = -Math.PI / 3;
			distance = -20;
			robot.setState(State.BackUp);
		} else if (!robot.hardware().rightBump.getValue()) {
			counters.setStepsCounter(70);
			angle = -Math.PI / 3;
			distance = -20;
			robot.setState(State.BackUp);
		}

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
		//dx = 50;
		//dy = 50;
		// angle = Math.PI / 2;
		counters.startStateSteps();
		angle = 0;
		counters.startStepCounter();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		RobotSticky robot = new RobotSticky(true);

		System.out.println("initialized");

		robot.setState(State.FollowWall);
		// robot.setState(State.LookAround);

		new StateMachine(robot).start();
	}

}
