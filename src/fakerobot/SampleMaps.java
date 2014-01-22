package fakerobot;

import global.Constants;

import java.util.ArrayList;
import java.util.List;

import robot.map.BotClientMap;
import robot.map.Position;

import math.geom2d.line.LineSegment2D;

/**
 * Sample maps used for the testing
 *
 */
public class SampleMaps {
	public static RobotSimulator createMap1(){
		List<LineSegment2D> walls=new ArrayList<LineSegment2D>();

		walls.add(new LineSegment2D(200, 500, 100, 400));
		walls.add(new LineSegment2D(100, 400, 200, 300));
		walls.add(new LineSegment2D(200, 300, 0, 100));
		walls.add(new LineSegment2D(0, 100, 600, 0));
		walls.add(new LineSegment2D(600, 0, 600, 500));
		walls.add(new LineSegment2D(600, 500, 400, 200));
		walls.add(new LineSegment2D(400, 200, 200, 500));	
		List<LineSegment2D> reactors=new ArrayList<LineSegment2D>();
		reactors.add(new LineSegment2D(200, 500, 100, 400));
	
		List<LineSegment2D> yellow=new ArrayList<LineSegment2D>();
		double[] redBallsX = { 200, 200 };
		double[] redBallsY = { 400, 100 };
		double[] greenBallsX = { 500 };
		double[] greenBallsY = { 300 };
		RobotSimulator map = new RobotSimulator(walls, new LineSegment2D(100, 400, 200, 300), reactors, yellow, redBallsX, redBallsY, greenBallsX,
				greenBallsY, 5);
		map.setLocation(40, 500, 200,Math.PI/2);
		return map;
	}
	
	public static RobotSimulator createMap2(){
		BotClientMap map=BotClientMap.getDefaultMap();
		List<LineSegment2D> walls=map.getMaze();
		
		List<LineSegment2D> reactors=map.getReactors();		
		List<LineSegment2D> yellow=map.getYellowWalls();
		double[] redBallsX = { 200, 200 };
		double[] redBallsY = { 400, 100 };
		double[] greenBallsX = { 500 };
		double[] greenBallsY = { 300 };
		RobotSimulator map1 = new RobotSimulator(walls, map.getSilo(), reactors, yellow, redBallsX, redBallsY, greenBallsX,
				greenBallsY, 5);
		Position position=map.getPosition();
		map1.setLocation(Constants.robotRadius, position.x(), position.y(),position.angle());
		return map1;
	}
}
