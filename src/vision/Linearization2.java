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
public class Linearization2 {
	
	

	private static double  aW = 4.22258 * 1000000, bW1 = -2.68819 * 1000000, cW1 = 14895.6,
			dW1 = 205.745, eW2 = 0.00236352;
	
	private static double bW = 5.55018*0.0000001, cW = 1.48049*0.000000001,ccW=-0.0000168493, dW= 2.47638*100000000.0, 
	 eW = -2.30891*1000000, fW= 13281.5, gW = -44.0231, hW = 0.0755483, 
	 iW = -0.0000515547;

	
	private static double aF = 5.78674*1000000.0, bF1 = -3.68397*1000000, cF1= 13989.5, dF1 = 179.853 ,eF2 =0.00160809;

	private static double bF = -0.188844, cF= -0.000511113, ccF=-0.0000188303, dF= -1011.67, eF= 8.52165, fF = -0.0430726, gF = 0.000124132, hF = -1.86484*0.0000001, 
	 iF = 1.12998*0.0000000001;

	
	public static double linearizeX(int x, int y) {
		return 0+ccW*x*x+(bW - cW*x)*(dW + eW*y + fW*y*y + gW*y*y*y + hW*y*y*y*y + iW*y*y*y*y*y);
	}
	
	public static double linearizeFloorX(int x, int y) {
		return 0.0+ccW*x*x+(bF - cF*x)*(dF + eF*y + fF*y*y + gF*y*y*y + hF*y*y*y*y + iF*y*y*y*y*y);
	}

	public static double linearizeY(int x, int y) {
		return aW + bW1 * Math.atan(cW1 + dW1 * y) + eW2 * x-3.9;
	}
	
	public static double linearizeFloorY(int x, int y) {
		return aF + bF1 * Math.atan(cF1 + dF1 * y) + eF2 * x-7.2;
	}

	public static Point2D linearize(int x, int y) {
		return new Point2D();
	}
	
	public static void print(int x, int y){
		System.out.println(x+","+y+"   "+linearizeFloorX(x, y)+","+ linearizeFloorY(x, y));
	};
	
	public static void main(String[] args){
		print(123, 307);
		print(263, 292);
		print(28, 368);
		print(50, 234);
		print(502, 419);
		print(532, 258);
	}
	
}
