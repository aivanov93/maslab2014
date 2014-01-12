package fakerobot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MapGUI extends JFrame {

	Panel panel;
	private static final long serialVersionUID = 1L;
	private Map map;
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
	
	public MapGUI(Map map){
		super();
		this.map=map;
		panel=new Panel();
		JTextField j=new JTextField(10);
		this.add(j);
		this.add(panel);
		panel.repaint();
		this.setPreferredSize(new Dimension(800, 800));
		this.pack();
		this.setVisible(true);
		
	}

}
