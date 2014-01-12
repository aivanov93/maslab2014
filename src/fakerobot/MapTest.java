package fakerobot;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Test;

public class MapTest {

	public static Map createMap1() {
		double[] mazex = { 200, 100, 200, 0, 600, 600, 400,200 };
		double[] mazey = { 500, 400, 300, 100, 0, 500, 200,500 };
		int[] reactors = { 0 };
		int[] react = {};
		double[] redBallsX = { 200, 100 };
		double[] redBallsY = { 400, 100 };
		double[] greenBallsX = { 500 };
		double[] greenBallsY = { 300 };
		Map map = new Map(mazex, mazey, redBallsX, redBallsY, greenBallsX,
				greenBallsY, 5, reactors, 1, react);
		map.setLocation(40, 60, 300, 100,Math.PI/2);
		return map;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Map map = createMap1();
				MapGUI gui = new MapGUI(map);
				for (int i=0; i<10; i++) map.move(0, 1);
				MapGUI gui1=new MapGUI(map);
			}
		});
	}
}
