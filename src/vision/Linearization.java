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
	
	private static double bb = -8.78232*0.0000000001, cc = -2.21729*0.000000000001, dd= -1.64492*100000000000.0, 
	 ee = 9.66086*100000000, f= -2.47376*1000000, g = 1342.14, h = 4.80783, 
	 i = -0.0061815;

	
	private static double fa = 2.08798*1000000, fb =-1.32926*1000000, fc= 2688.37, fd = 120.354,fe =-0.00129213;

	private static double gb = 288.706, gc= 0.761061, gd= 1.23361, ge= -0.0132503, gf = 0.0000719371, gg = -2.06996*0.0000001, gh = 3.0209*0.0000000001, 
	 gi = -1.7597*0.0000000000001;

	
	public static double linearizeX(int x, int y) {
		return 0+(bb - cc*x)*(dd + ee*y + f*y*y + g*y*y*y + h*y*y*y*y + i*y*y*y*y*y);
	}
	
	public static double linearizeFloorX(int x, int y) {
		return 0.0+(gb - gc*x)*(gd + ge*y + gf*y*y + gg*y*y*y + gh*y*y*y*y + gi*y*y*y*y*y);
	}

	public static double linearizeY(int x, int y) {
		return a + b * Math.atan(c + d * y) + e * x-1.2;
	}
	
	public static double linearizeFloorY(int x, int y) {
		return fa + fb * Math.atan(fc + fd * y) + fe * x+3;
	}

	public static Point2D linearize(int x, int y) {
		return new Point2D();
	}
	
	public static void print(int x, int y){
		System.out.println(x+","+y+"   "+linearizeX(x, y)+","+ linearizeY(x, y));
	};
	
	public static void main(String[] args){
		print(259, 362);
		print(429, 425);
		print(87, 356);
		print(569, 216);
		print(71, 233);
	}
	
}
