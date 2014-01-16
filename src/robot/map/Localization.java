package robot.map;

import java.util.Map;
import java.util.Random;

import global.Constants;

public class Localization implements Runnable {
	private Random sampler;
	private Position[] particlesOld = new Position[Constants.numberOfParticles];
	private Position[] particlesNew = new Position[Constants.numberOfParticles];

	private Position[] swap;

	private double distanceMoved = 0, angleMoved = 0;

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

	/**
	 * main thing for localization
	 */
	public void localize() {

		swap = particlesOld;
		particlesOld = particlesNew;
		particlesNew = swap;

		for (int i = 0; i < particlesOld.length; i++) {
			particlesOld[i].motionUpdate(distanceMoved, angleMoved);
			double weight=map.getProbability(particlesOld[i].x(),
					particlesOld[i].y(), particlesOld[i].angle());
			
		}
		
		for (int i=0; i<particlesOld.length;i++){
			
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
		int highIndex = Constants.numberOfParticles;

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
				return particlesOld[midNum];
			}
		}
		return particlesOld[0];
	}

	public void run() {

	}

}
