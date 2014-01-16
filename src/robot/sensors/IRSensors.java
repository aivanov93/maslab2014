package robot.sensors;

import global.Constants;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IRSensors {

	public static enum Side {
		Left, Right, Undecided
	};

	private List<Double> readings = new ArrayList<Double>();

	public IRSensors(int number) {
		for (int i = 0; i < number; i++) {
			readings.add(0.0);
		}
	}
	
	public double get(int number){
		return readings.get(number);
	}

	public void set(int index, double value) {
		readings.set(index, value);
	}
	
	private double cosineLaw(double side1, double side2, double angle){
		return Math.sqrt(side1*side1+side2*side2-2*side1*side2*Math.cos(angle));
	}
	
	private double angleCosineLaw(double side1, double side2, double side3){
		double angle=Math.acos((side3*side3-side2*side2-side1*side1)/(2*side1*side2));
		if (angle>Math.PI) angle=2*Math.PI-angle;
		return angle;
	}
	
	/**
	 * Tells whether the robot feels a  wall with its irs
	 * @return
	 */
	public boolean seesWall() {
		boolean sees = false;
		for (int i = 0; i < readings.size(); i++) {
			if (!sees) {
				sees = readings.get(i) < Constants.maxIRreading
						&& readings.get(i) > Constants.minIRreading;
			}
		}
		return sees;
	}

	/**
	 * Calculates the angle needed to turn to follow a wall;
	 * 
	 * @param side
	 *            which side is the wall on
	 */
	public double followWallAngle(Side side) {
		double irSideTop, irSideBottom, irHeadAngled, coeff=1;
		
		// check on which side are we following the wall
		if (side == Side.Left) {
			irSideTop = readings.get(7);
			irSideBottom = readings.get(6);
			irHeadAngled= readings.get(0);
			//this is to reverse the formula below
			coeff=-1;
		} else {
			irSideTop = readings.get(3);
			irSideBottom = readings.get(4);
			irHeadAngled=readings.get(2);
		}
		
		double angle=0;
		// if both irs have enough range return the angle between the wall and the robot
		if (irSideTop>0 && irSideBottom>0){
			angle=Math.atan(coeff*(irSideTop - irSideBottom) / Constants.sideIRspacing);
		} else { // if the bottom side one is out of range
			double height= irHeadAngled/Math.sin(Constants.angleBetweenTopIrs);	
			double wallLength = this.cosineLaw(irSideTop, irHeadAngled, Constants.angleBetweenTopIrs);
			angle=Math.acos(height/wallLength);
			if (angleCosineLaw(wallLength, irSideTop, irHeadAngled)<0) angle*=-1;
		} 
		return angle;
	}
	
	/**
	 * get the ir that has the smallest reading
	 * @return 
	 */
	public int smallestReading(){
		int smallest=0;
		double minDist=Double.MAX_VALUE;
		for (int i=1; i<readings.size();i++){
			if (readings.get(i)<minDist && readings.get(i)>0){
				smallest=i;
				minDist=readings.get(i);
			}
		}
		return smallest;
	}
	
	public double getCorrectedAngle(){
		int smallest=smallestReading();
		
		return 0.0;
	}
}
