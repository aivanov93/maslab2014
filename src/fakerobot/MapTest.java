package fakerobot;

import java.util.List;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Test;

import vision.detector.VisionDetector;

public class MapTest {


	@Test
	public void Test0(){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Map map = SampleMaps.createMap1();
				MapGUI gui = new MapGUI(map);
				for (int i=0; i<10; i++) map.move(0.5, 0);
				MapGUI gui1=new MapGUI(map);
			}
		});
	}
	
	@Test
	public void TestIR(){
		List<Double> irs=new ArrayList<Double>();
		for (int i=0; i<8; i++) irs.add(0.0);
		Map map = SampleMaps.createMap1();
		map.updateIRs(irs);
		System.out.println(irs);
		VisionDetector det=new VisionDetector();
		map.checkColors(det);
		System.out.println(det.seesSomething());
		System.out.println(det.seesRedBall());
	}
	
	
}
