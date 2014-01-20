package fakerobot;

import java.util.List;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Test;

import vision.detector.VisionDetector;

/**
 * Initial simulator testings. Currently outdated.
 * 
 */
public class MapTest {
	@Test
	public void Test0() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				RobotSimulator map = SampleMaps.createMap1();
				//MapGUI gui = new MapGUI(map);
				for (int i = 0; i < 10; i++)
					map.move(0.5, 0);
				//MapGUI gui1 = new MapGUI(map);
			}
		});
	}

	@Test
	public void TestIR() {
		double irs[] = new double[8];
		RobotSimulator map = SampleMaps.createMap1();
		// map.updateReadings(irs);
		System.out.println(irs);
		VisionDetector det = new VisionDetector();
		map.updateCamera(det);
		System.out.println(det.seesSomething());
		System.out.println(det.seesRedBall());
	}

}
