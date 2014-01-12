package fakerobot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.Thread.State;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MapGUI extends JFrame {

	Panel panel;
	JLabel state;
	private static final long serialVersionUID = 1L;
	private RobotSimulator map;
	BufferedImage image=new BufferedImage(800,800,BufferedImage.TYPE_3BYTE_BGR);
	
	private class Panel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		public void paintComponent(Graphics g){
			Graphics2D g2 = (Graphics2D)g;
						
			g2.scale(1, -1);
			g2.translate(0, -600);
			map.draw(g2);	
		}
		public Panel(){
			super();
			this.setPreferredSize(new Dimension(800, 800));
		}
		
	}
	
	public void update(){
		panel.repaint();
	}
	
	public void setState(State state){
		this.state.setText(state.toString());
	}
	
	public MapGUI(RobotSimulator map){
		super();
		this.map=map;
		panel=new Panel();
		JLabel j=new JLabel("State");
		state=new JLabel("start");
		this.add(j); this.add(state);
		this.add(panel);
		panel.repaint();
		this.setPreferredSize(new Dimension(800, 800));
		this.pack();
		this.setVisible(true);
		
	}

}
