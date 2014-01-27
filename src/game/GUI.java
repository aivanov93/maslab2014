package game;

import game.StateMachine.State;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import robot.map.Localization;
import robot.map.MapForSensors;
import robot.map.MazeMap;
import robot.sensors.RobotEnviroment;

/**
 * Used to show the current state of Enviroment of the robot
 * 
 * Shows the walls, the maze objects, the balls, the current position of the robot and its predicted position
 */
public class GUI extends JFrame {

	Panel panel;
	JLabel state, distance, angle, green,red, odometryd, odometrya, balld, balla;
	JPanel topPanel;
	
	private static final long serialVersionUID = 1L;
	private MapForSensors mapForSensors;
	private MazeMap mazeMap;
	private Localization localization;
	
	private class Panel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		public void paintComponent(Graphics g){
			Graphics2D g2 = (Graphics2D)g;
			g2.setColor(Color.white);
			g2.fillRect(0, 0, 500, 500);
			g2.scale(1, -1);
		//	g2.scale(2,2);
			g2.translate(0, -350);
			mapForSensors.draw(g2);	
			localization.draw(g2);
			mazeMap.draw(g2);
		}
		public Panel(){
			super();
			this.setPreferredSize(new Dimension(500, 500));
		}
		
	}
	
	/**
	 * updates the view of the map
	 */
	public void update(){
		panel.repaint();
	
	}
	
	public void setVison(Boolean green, Boolean red, double d, double a){
		this.red.setText(red.toString());
		this.green.setText(green.toString());
		balla.setText(a+""); balld.setText(d+"");
	}
	
	public void setGoal(double distance, double angle){
		this.distance.setText(distance+"");
		this.angle.setText(angle+"");
	}
	
	public void setState(State state) {
		this.state.setText(state.toString());
	}
	
	public void setOdometry(double dist, double angle){
		balla.setText(angle+"");
		balld.setText(dist+"");
	}
	
	public GUI(MapForSensors mapForSensors, Localization localization, MazeMap mazeMap){
		super();
		this.localization=localization;
		this.mazeMap=mazeMap;
		this.mapForSensors=mapForSensors;
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.LINE_AXIS));
		//panel=new Panel();
		topPanel=new JPanel();
		
		this.add(topPanel);
		this.add(Box.createRigidArea(new Dimension(0,100)));
		GridLayout experimentLayout = new GridLayout(0,4);
		
		topPanel.setLayout(experimentLayout);
		
		topPanel.add(new JLabel("State  "));
		state=new JLabel("start");
		state.setForeground(Color.blue);
		topPanel.add(state);
		
		topPanel.add(Box.createRigidArea(new Dimension(5,0)));
		topPanel.add(Box.createRigidArea(new Dimension(5,0)));

		///
		topPanel.add(new JLabel("Distance to goal   "));		
		distance=new JLabel("0");
     	topPanel.add(distance);
     	
   
		
		topPanel.add(new JLabel("Angle to goal   "));
		angle=new JLabel("0");
		topPanel.add(angle);
		
	////
		
		topPanel.add(new JLabel("Sees green ball"));
		green=new JLabel("false");
		topPanel.add(green);

		green.setForeground(Color.green);
		
		
		topPanel.add(new JLabel("Left wall distance"));
		red=new JLabel("false");
		red.setForeground(Color.red);
		topPanel.add(red);
		
		////
		
		topPanel.add(new JLabel("Left wall angle"));
		balld=new JLabel("none");
		topPanel.add(balld);
		
		

		topPanel.add(new JLabel("Angle ball"));
		balla=new JLabel("none");
		topPanel.add(balla);
		
		///
		
		topPanel.add(new JLabel("Odometry distance"));
		odometryd=new JLabel("0");
		topPanel.add(odometryd);
		
		

		topPanel.add(new JLabel("Odometry angle"));
		odometrya=new JLabel("0");
		topPanel.add(odometrya);
		
		//this.add(panel);		
		//panel.repaint();
		
		this.setPreferredSize(new Dimension(500, 500));
		this.pack();
	//	this.setVisible(true);
		
	}
	
	public static void main(String[] args){
		GUI gui=new GUI(null, null, null);
	}

}
