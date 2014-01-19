package robot.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Map;
import java.util.Random;

import robot.sensors.Odometry;

import global.Constants;

public class Localization implements Runnable {
	private Random sampler=new Random();
	private Position[] particlesOld = new Position[Constants.numberOfParticles];
	private Position[] particlesNew = new Position[Constants.numberOfParticles];

	private Position[] swap;

	private Odometry odometry;

	private Position position;
	private MapForSensors map;

	public double noiseDist() {
		return (sampler.nextDouble() - 0.5) * Constants.distanceUncertainty;
	}

	public double noiseAngle() {
		return (sampler.nextDouble() - 0.5) * Constants.angleUncertainty;
	}

	public Localization(double x, double y, double angle, MapForSensors map) {
		this.map = map;
		position = new Position(x, y, angle, 1.0 / Constants.numberOfParticles);
		particlesNew[0] = position;
		// uniformly distribute the particles around the starting position
		for (int i = 1; i < Constants.numberOfParticles; i++) {
			particlesNew[i] = new Position(x + noiseDist(), y + noiseDist(),
					angle + noiseAngle(), 1.0 / Constants.numberOfParticles);
		}
	}
	
	public void setOdometry(Odometry odometry){
		this.odometry=odometry;
	}

	/**
	 * main thing for localization
	 */
	public void localize() {

		swap = particlesOld;
		particlesOld = particlesNew;
		particlesNew = swap;
		double totalWeight=0;
		
		//motion and sensor update
		for (int i = 0; i < particlesOld.length; i++) {
			//motion update
			particlesOld[i].motionUpdate(odometry);
			//calculate the probability of this particle being the right one
			double weight=map.getProbability(particlesOld[i].x(),
					particlesOld[i].y(), particlesOld[i].angle());
			//assign a new weight
			particlesOld[i].setWeight(weight);
			//increase the total weight
			totalWeight+=weight;			
		}
		position=particlesOld[0];
		//calculate the current best position and resample particles
		for (int i=0; i<particlesOld.length;i++){
			//check if this particle has bigger weight
			if (particlesOld[i].weight()>position.weight()){
				position=particlesOld[i];
			}			
			//draw a new particle
			double random=sampler.nextDouble()*totalWeight;			
			particlesNew[i]=drawParticle(random);
		}
		
	}

	/**
	 * Draws a random particle from the given set according to their weights
	 * 
	 * @param random
	 *            the value that we search for in the array of weights
	 * @return the found particle
	 */
	private Position drawParticle(double random) {
		int lowIndex = 0;
		int highIndex = Constants.numberOfParticles-1;
		while (lowIndex <= highIndex) {

			int midNum = (lowIndex + highIndex) / 2;
			
			double rangeLower = midNum - 1 < 0 ? 0 : particlesOld[midNum - 1]
					.weight();
			double rangeUpper = particlesOld[midNum].weight();

			if (random < rangeLower) {
				highIndex = midNum - 1;
			} else if (random >= rangeUpper) {
				lowIndex = midNum + 1;
			} else if (random >= rangeLower && random < rangeUpper) {
				return particlesOld[midNum].clone();
			}
		}
		return particlesOld[0].clone();
	}

	public Position getPosition(){
		return position;
	}
	
	/**
	 * used for thread runnable
	 */
	public void run() {
		localize();
	}
	
	
	
	public void draw(Graphics2D g) {
		double r=3;
		for (int i = 0; i < particlesOld.length; i++) {			
			g.setColor(Color.blue);
			if (particlesNew[i]!=null){
			g.fill(new Ellipse2D.Double(particlesNew[i].x() -r, particlesNew[i].y()-r, 2*r, 2*r));	
			}
		}
		g.setColor(Color.orange);
		r=6;
		g.fill(new Ellipse2D.Double(position.x() -r, position.y()-r, 2*r, 2*r));
	}

}
