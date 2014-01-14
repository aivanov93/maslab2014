package robot.sensors;

import global.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IRSensors {

	public static enum Side {
		Left, Right
	};

	private List<Double> readings = new ArrayList<Double>();

	public IRSensors(int number) {
		for (int i = 0; i < number; i++) {
			readings.add(0.0);
		}
	}

	public void set(int index, double value) {
		readings.set(index, value);
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
		double ir1, ir2;
		if (side == Side.Left) {
			ir2 = readings.get(7);
			ir1 = readings.get(6);
		} else {
			ir1 = readings.get(3);
			ir2 = readings.get(4);
		}
		if (ir1 == ir2)
			ir1 += 0.00000001;

		// return the angle between the wall and the robot
		return Math.atan((ir1 - ir2) / Constants.robotLenght);
	}
	
	/**
	 * get the ir that has the smallest reading
	 * @return 
	 */
	public int smallestReading(){
		int smallest=0;
		for (int i=1; i<readings.size();i++){
			if (readings.get(i)<readings.get(smallest)){
				smallest=i;
			}
		}
		return smallest;
	}
	
	public double getCorrectedAngle(){
		int smallest=smallestReading();
		
		return 0.0;
	}
}
