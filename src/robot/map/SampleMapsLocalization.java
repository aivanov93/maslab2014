package robot.map;

import java.util.ArrayList;
import java.util.List;

import robot.sensors.IRSensors;

import math.geom2d.line.LineSegment2D;

public class SampleMapsLocalization {
	public static MapForSensors mapForSensors1(IRSensors irs){
		List<LineSegment2D> walls=new ArrayList<LineSegment2D>();
		walls.add(new LineSegment2D(200, 500, 100, 400));
		walls.add(new LineSegment2D(100, 400, 200, 300));
		walls.add(new LineSegment2D(200, 300, 0, 100));
		walls.add(new LineSegment2D(0, 100, 600, 0));
		walls.add(new LineSegment2D(600, 0, 600, 500));
		walls.add(new LineSegment2D(600, 500, 400, 200));
		walls.add(new LineSegment2D(400, 200, 200, 500));		
		return new MapForSensors(walls, irs);
	}
	
	public static MapForSensors mapForSensors2(IRSensors irs){
		List<LineSegment2D> walls=BotClientMap.getDefaultMap().getMaze();		
		return new MapForSensors(walls, irs);
	}
	
	public static MazeMap mazeMap2(){
		List<LineSegment2D> walls=BotClientMap.getDefaultMap().getMaze();		
		List<LineSegment2D> reactors=BotClientMap.getDefaultMap().getReactors();		
		List<LineSegment2D> yellow=BotClientMap.getDefaultMap().getYellowWalls();
		return new MazeMap(walls, reactors, yellow, BotClientMap.getDefaultMap().getSilo());
	}
	
	public static MazeMap mazeMap1(){
		List<LineSegment2D> walls=new ArrayList<LineSegment2D>();
		walls.add(new LineSegment2D(200, 500, 100, 400));
		walls.add(new LineSegment2D(100, 400, 200, 300));
		walls.add(new LineSegment2D(200, 300, 0, 100));
		walls.add(new LineSegment2D(0, 100, 600, 0));
		walls.add(new LineSegment2D(600, 0, 600, 500));
		walls.add(new LineSegment2D(600, 500, 400, 200));
		walls.add(new LineSegment2D(400, 200, 200, 500));		
		MazeMap n=new MazeMap(walls,new ArrayList<LineSegment2D>(),new ArrayList<LineSegment2D>(), new LineSegment2D(0,0,0,0));
		return n;
	}
}
