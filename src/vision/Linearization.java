package vision;

import java.awt.Point;
import java.awt.print.Printable;
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

	private static double  a = 3.26683 * 1000000, b = -2.07974 * 1000000, c = 4206.17,
			d = 188.304, e = -0.00129213;
	
	private static double bb = 1.22186*0.000000001, cc = 3.08475*0.000000000001, dd= 1.09886*100000000000.0, 
	 ee = -5.08818*100000000, f= 235386., g = 5049.51, h = -14.5258, 
	 i = 0.0121934;

	private static double linearizeX(int x, int y) {
		return -0.5+(bb - cc*x)*(dd + ee*y + f*y*y + g*y*y*y + h*y*y*y*y + i*y*y*y*y*y);
	}

	private static double linearizeY(int x, int y) {
		return a + b * Math.atan(c + d * y) + e * x-1.2;
	}

	public static Point2D linearize(int x, int y) {
		return new Point2D();
	}
	
	public static void print(int x, int y){
		System.out.println(x+","+y+"   "+linearizeX(x, y)+","+ linearizeY(x, y));
	};
	
	public static void main(String[] args){
		print(43, 398);
		print(459, 442);
		print(510, 240);
		print(569, 216);
		print(71, 233);
	}
	
}
