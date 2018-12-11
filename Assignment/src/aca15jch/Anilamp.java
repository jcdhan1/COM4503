package aca15jch;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main class to run. Code reused from M04 from Chapter 7 scene graph.
 */
public class Anilamp extends JFrame implements ActionListener {

	private static final int WIDTH = 1024;
	private static final int HEIGHT = 768;
	private static final Dimension dimension = new Dimension(WIDTH, HEIGHT);
	private GLCanvas canvas;
	private Anilamp_GLEventListener glEventListener;
	private final FPSAnimator animator;
	private Camera camera;

	public static void main(String[] args) {
		Anilamp b1 = new Anilamp("Anilamp");
		b1.getContentPane().setPreferredSize(dimension);
		b1.pack();
		b1.setVisible(true);
	}

	public Anilamp(String textForTitleBar) {
		super(textForTitleBar);
		GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));
		canvas = new GLCanvas(glcapabilities);
		camera = new Camera(Camera.DEFAULT_POSITION, Camera.DEFAULT_TARGET, Camera.DEFAULT_UP);
		glEventListener = new Anilamp_GLEventListener(camera);
		canvas.addGLEventListener(glEventListener);
		canvas.addMouseMotionListener(new MyMouseInput(camera));
		canvas.addKeyListener(new MyKeyboardInput(camera));
		getContentPane().add(canvas, BorderLayout.CENTER);

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(this);
		fileMenu.add(quitItem);
		menuBar.add(fileMenu);

		JPanel p = new JPanel();
		JButton b = new JButton("camera X");
		b.addActionListener(this);
		p.add(b);
		b = new JButton("camera Z");
		b.addActionListener(this);
		p.add(b);
		b = new JButton("Random Pose");
		b.addActionListener(this);
		p.add(b);
		b = new JButton("Jump");
		b.addActionListener(this);
		p.add(b);
		b = new JButton("Reset");
		b.addActionListener(this);
		p.add(b);
		this.add(p, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				animator.stop();
				remove(canvas);
				dispose();
				System.exit(0);
			}
		});
		animator = new FPSAnimator(canvas, 60);
		animator.start();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("camera X")) {
			camera.setCamera(Camera.CameraType.X);
			canvas.requestFocusInWindow();
		} else if (e.getActionCommand().equalsIgnoreCase("camera Z")) {
			camera.setCamera(Camera.CameraType.Z);
			canvas.requestFocusInWindow();
		} else if (e.getActionCommand().equalsIgnoreCase("Random Pose")) {
			glEventListener.startAnimation(3 * Math.random());
		} else if (e.getActionCommand().equalsIgnoreCase("Jump")) {
			glEventListener.startAnimation(false);
		} else if (e.getActionCommand().equalsIgnoreCase("Reset")) {
			glEventListener.startAnimation(true);
		} else if (e.getActionCommand().equalsIgnoreCase("quit"))
			System.exit(0);
	}
}
