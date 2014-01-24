package vision;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.AbstractMap.SimpleEntry;

import math.geom2d.Point2D;

@SuppressWarnings("unchecked")
public class Linearization {
	private static HashMap<Point, SimpleEntry<Double, Double>> mapping;
	static{
		File file = new File("resources/field/camerafloor");
		
		FileInputStream ff = null;
		try {
			ff = new FileInputStream(file);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		ObjectInputStream ss=null;
		try {
			ss = new ObjectInputStream(ff);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		try {
			mapping = (HashMap<Point, SimpleEntry<Double, Double>>) ss
					.readObject();
		} catch (ClassNotFoundException | IOException e) {			
			e.printStackTrace();
		}
		try {
			ss.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		;
	}
	
	public static Point2D linearize(int x, int y){
		return new Point2D();
	}
}
