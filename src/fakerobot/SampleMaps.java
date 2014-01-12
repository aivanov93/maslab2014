package fakerobot;

public class SampleMaps {
	public static Map createMap1(){
		double[] mazex = { 200, 100, 200, 0, 600, 600, 400,200 };
		double[] mazey = { 500, 400, 300, 100, 0, 500, 200,500 };
		int[] reactors = { 0 };
		int[] react = {};
		double[] redBallsX = { 200, 200 };
		double[] redBallsY = { 400, 100 };
		double[] greenBallsX = { 500 };
		double[] greenBallsY = { 300 };
		Map map = new Map(mazex, mazey, redBallsX, redBallsY, greenBallsX,
				greenBallsY, 5, reactors, 1, react);
		map.setLocation(40, 60, 300, 100,Math.PI);
		return map;
	}
}
