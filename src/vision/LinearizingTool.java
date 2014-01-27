package vision;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class LinearizingTool extends JFrame implements MouseListener, ItemListener, ActionListener {

	JPanel frame;
	BufferedImage image;
	public HashMap<Point, Double> xp = new HashMap<Point, Double>();
	public HashMap<Point, Double> yp = new HashMap<Point, Double>();
	boolean usingOldX = false;
	JCheckBox useOld;
	double oldX, oldY;

	public LinearizingTool() {
		super();
		try {
			image = ImageIO.read(new File("resources/field/floor.png"));
		} catch (IOException ex) {

		}
		JLabel label = new JLabel(new ImageIcon(image));
		frame = new JPanel();
		frame.setPreferredSize(new Dimension(640, 480));
		JButton doneButton=new JButton("done");
		doneButton.addActionListener(this);
		useOld = new JCheckBox("Use previously selected x");
		useOld.setSelected(false);
		useOld.addItemListener(this);
		this.setSize(new Dimension(650, 650));
		frame.add(useOld);
		frame.add(label);
		frame.add(doneButton);
		this.add(frame);
		this.setVisible(true);
		label.addMouseListener(this);

	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();

		if (source == useOld) {
			if (e.getStateChange() == ItemEvent.DESELECTED)
				usingOldX = false;
			else
				usingOldX = true;
		}
	}
	
	public void actionPerformed(ActionEvent e){
		try {
			save();
		} catch (ClassNotFoundException | IOException e1) {
		
			e1.printStackTrace();
		}
	}
	
	public void save() throws IOException, ClassNotFoundException{
		File file = new File("resources/field/camerabot");
		FileOutputStream f = new FileOutputStream(file);
		ObjectOutputStream s = new ObjectOutputStream(f);
		s.writeObject(xp);
		s.writeObject(yp);
		s.flush();

		FileInputStream ff = new FileInputStream(file);
		ObjectInputStream ss = new ObjectInputStream(ff);
		HashMap<Point, Double> xpn = (HashMap<Point, Double>) ss
				.readObject();
		
		HashMap<Point, Double> ypn = (HashMap<Point, Double>) ss
				.readObject();
		ss.close();
		System.out.println(xpn);
		for (Point point:xpn.keySet()){
			System.out.print("{"+point.x+", "+point.y+", "+xpn.get(point)+"}, ");
		}
		System.out.println();
		for (Point point:ypn.keySet()){
			System.out.print("{"+point.x+", "+point.y+", "+ypn.get(point)+"}, ");
		}
		System.out.println();
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		HashMap<Integer, Double> points = new HashMap<Integer, Double>();
		points.put(5, 10.0);
		LinearizingTool linearizing = new LinearizingTool();		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	
	
	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println(e.getX()+" "+ e.getY());
		if (!usingOldX) {
			String s = (String) JOptionPane.showInputDialog(frame,
					"Real Distance:\n" + "\"X and Y\"",
					"Customized Dialog", JOptionPane.PLAIN_MESSAGE, null, null,
					"ham");			
			// If a string was returned, say so.
			if ((s != null) && (s.length() > 0)) {
				String[] xandy=s.split(" ");
				if (xandy.length!=2){
					System.out.println("cherez probel shmara!");
				} else {
					oldX=Double.parseDouble(xandy[0]);
					oldY=Double.parseDouble(xandy[1]);
					System.out.println(oldX+ " "+oldY);
					xp.put(new Point(e.getX(), e.getY()), oldX);
					yp.put(new Point(e.getX(), e.getY()), oldY);					
				}
			}
		} else {
			//default icon, custom title
			int n = JOptionPane.showConfirmDialog(
			    frame,
			    "Sure?",
			    "Ne tupi",
			    JOptionPane.YES_NO_OPTION);
			if (n==0){
				oldY+=4;
				System.out.println(oldX+ " "+oldY);
				xp.put(new Point(e.getX(), e.getY()), oldX);
				yp.put(new Point(e.getX(), e.getY()), oldY);
				
			} 
		}
	}
}
