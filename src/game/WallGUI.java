package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import vision.NewImageProcessor;
import vision.detector.VisionDetector;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.StraightLine2D;

public class WallGUI extends JFrame{

	Point2D carot=new Point2D(0,0); double angle;
	Panel panel;
	private class Panel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void paint(Graphics g){
			super.paint(g);
			Graphics2D g2 = (Graphics2D)g;
			g2.setColor(Color.white);
			g2.fillRect(0, 0, 500, 500);
			g2.setStroke(new BasicStroke(3));
			g.setColor(Color.black);
			g.fillOval(230, 230, 40, 40);			
			g.drawLine(250, 250, 250, 200);
			g.setColor(Color.orange);
			g.fillOval(250-(int)carot.x()*2, 250-(int)carot.y()*2, 5, 5);
			
		}
		
		public Panel(){
			super();
			this.setPreferredSize(new Dimension(500, 500));
			this.setSize(new Dimension(500,500));
			this.setMinimumSize(new Dimension(500,500));
			this.repaint();
		}
		
		
	}
	
	
	public void setCarrot( Point2D p){
		carot=p;
		panel.repaint();
	}
	
	public WallGUI(){
		super();
		this.panel=new Panel();
		this.add(panel);
		panel.repaint();
		this.setSize(new Dimension(500, 500));
		this.setPreferredSize(new Dimension(500, 500));
		this.pack();
		this.setVisible(true);
	}
}
