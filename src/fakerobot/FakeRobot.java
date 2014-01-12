package fakerobot;

import java.util.ArrayList;
import java.util.List;

import jssc.SerialPort;
import robot.main.Robot;
import robot.moves.Driver;
import robot.sensors.Sensors;
import vision.detector.VisionDetector;
import math.geom2d.Point2D;
import math.geom2d.polygon.Rectangle2D;


/*
 * The model of the robot
 * 
 *       ir1    ir2   ir3
 *          \    |    / 
 *           \   |   /
 * ir8_ _ _ __\__|__/_ _ _ _ _ ir4
 *            |*****| 
 *            |*****|
 *            |*****|
 *ir7_ _ _ _ _|_____|_ _ _ _ _ ir5
 *               |
 *               |
 *               |
 *               |
 *               |
 *              ir6
 *  
 *  ir1,ir2,ir3,ir6 go through the center
 *  
 */

public class FakeRobot implements Robot {
	private Sensors sensors;
	private Driver driver;
	private VisionDetector camera;
	private List<Double> irs;
	private Rectangle2D position;
	
	private double angleOfView=50 *Math.PI/180;
	
	//represents the actual robot
	private Rectangle2D robot;
	
		
	/**
	 * Initialize the robot
	 * @param width
	 * @param height
	 * @param map the map of the maze
	 * @param startX starting left corner of the robot
	 * @param startY
	 */
	public FakeRobot(double width, double height, Map map, double startX, double startY){
		robot=new Rectangle2D( width, height, startY, startY);
	}
	
	public void update(){
		
	}
	
	public List<Double> getIRreadings(){
		return new ArrayList<Double>();
	}
	
	public Rectangle2D locate(){
		return robot;
	}
	
	public double angleOfView(){
		return angleOfView;
	}
	
	public VisionDetector vision(){
		return camera;
	}
	
	public List<Double> irs(){
		return irs;
	}
	
}
