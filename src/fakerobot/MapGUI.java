package fakerobot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import game.StateMachine.State;

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
public class MapGUI extends JFrame {

	Panel panel;
	JLabel state, redBalls, greenBalls, error, score;
	JPanel topPanel;
	
	private static final long serialVersionUID = 1L;
	private RobotEnviroment map;
	private MazeMap mazeMap;
	private Localization localization;
	
	private class Panel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		public void paintComponent(Graphics g){
			Graphics2D g2 = (Graphics2D)g;
			g2.setColor(Color.white);
			g2.fillRect(0, 0, 1100, 1080);
			g2.scale(1, -1);
			g2.translate(0, -900);
			map.draw(g2);	
			localization.draw(g2);
			mazeMap.draw(g2);
		}
		public Panel(){
			super();
			this.setPreferredSize(new Dimension(1100, 1080));
		}
		
	}
	
	/**
	 * updates the view of the map
	 */
	public void update(){
		panel.repaint();
		redBalls.setText(map.redBallsInside()+"");
		greenBalls.setText(map.greenBallsInside()+"");
	}
	
	public void setScore(int score){
		this.score.setText(score+"");
	}
	
	public void setState(State state){
		this.state.setText(state.toString());
	}
	
	public MapGUI(RobotEnviroment map, Localization localization, MazeMap mazeMap){
		super();
		this.localization=localization;
		this.mazeMap=mazeMap;
		this.map=map;
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		panel=new Panel();
		topPanel=new JPanel();
		
		this.add(topPanel);
		this.add(Box.createRigidArea(new Dimension(0,100)));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		
		topPanel.add(new JLabel("State  "));
		state=new JLabel("start");
		topPanel.add(state);
		
		topPanel.add(Box.createRigidArea(new Dimension(15,0)));
		
		topPanel.add(new JLabel("Red Balls   "));
		redBalls=new JLabel("0");
     	topPanel.add(redBalls);
     	
    	topPanel.add(Box.createRigidArea(new Dimension(15,0)));
		
		topPanel.add(new JLabel("Green Balls   "));
		greenBalls=new JLabel("0");
		topPanel.add(greenBalls);
		
		topPanel.add(Box.createRigidArea(new Dimension(15,0)));
		
		topPanel.add(new JLabel("Error   "));
		error=new JLabel("none");
		topPanel.add(error);
		
		topPanel.add(Box.createRigidArea(new Dimension(15,0)));
		
		topPanel.add(new JLabel("Score   "));
		score=new JLabel("0");
		topPanel.add(score);
		
		topPanel.add(Box.createRigidArea(new Dimension(15,0)));
		
		this.add(panel);		
		panel.repaint();
		
		this.setPreferredSize(new Dimension(1100, 1080));
		this.pack();
		this.setVisible(true);
		
	}

}
