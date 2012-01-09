package com.erroll.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import com.erroll.camera.Camera;
import com.erroll.camera.CameraInterface;
import com.erroll.metrics.Metrics;
import com.erroll.renderer.RayCasterBeamOptNeighbor;
import com.erroll.renderer.RayCasterBeamOptRestart;
import com.erroll.renderer.RayCasterInterface;
import com.erroll.renderer.RayCasterNeighbor;
import com.erroll.renderer.RayCasterRestart;
import com.erroll.renderer.RayCasterRestartScaleAdaptation;

public class RenderFrame extends JFrame {

	// generated serialVersionUID
	private static final long serialVersionUID = -2587976746868524926L;

	// Properties to be used by each class
	private Properties props;

	// rendering objects to draw the fractal
	private static CameraInterface camera;
	private static RayCasterInterface rayCaster;

	// metrics object to record data about the rendering
	private static Metrics metrics;

	// name to display on top of the frame
	private String displayName = "";

	public RenderFrame() {
		// load properties
		props = loadProperties();
		int size = Integer.parseInt(props.getProperty("SIZE"));

		// create metrics object
		metrics = new Metrics();

		// set basic JFrame properties
		setPreferredSize(new Dimension(size, size));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
		pack();

		// create buffer strategy to double buffer renderer
		createBufferStrategy(2);

		// create camera;
		camera = new Camera();
		//camera.setDistanceToViewplane(10);

		// create and initialize rayCaster
		if (props.getProperty("NEIGHBOR").equals("1")) {
			if (props.getProperty("BEAMOPT").equals("1")) {
				displayName = "Neighbor & BeamOpt";
				rayCaster = new RayCasterBeamOptNeighbor();
			} else {
				displayName = "Neighbor";
				rayCaster = new RayCasterNeighbor();
			}
		} else if (props.getProperty("BEAMOPT").equals("1")) {
			displayName = "Restart & BeamOpt";
			rayCaster = new RayCasterBeamOptRestart();
		} else {
			displayName = "Restart";
			rayCaster = new RayCasterRestart();
		}
		rayCaster = new RayCasterRestartScaleAdaptation();

		rayCaster.initialise(props, camera, size, size);
		rayCaster.setMetrics(metrics);
		rayCaster.setDebug(props.getProperty("DEBUG").equals("1") ? true : false);

		// add listener to handle keyboard controls
		addKeyListener(new KeyboardControlListener(camera));
	}

	// load properties from file "parameters.properties" into props object to be passed on during initializations
	private Properties loadProperties() {
		Properties p = new Properties();
		try {
			p.load(new BufferedReader(new FileReader("parameters.properties")));
		} catch (FileNotFoundException e1) {
			System.out.println("FILE NOT FOUND");
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return p;
	}

	/**
	 * Update title of JFrame with metrics data
	 */
	public void updateTitle() {
		setTitle(displayName + " | Current FPS: " + metrics.getFps());
	}

	public static void main(String[] args) {

		// frame to be rendered into
		final RenderFrame renderFrame = new RenderFrame();

		// set up timer to update title of frame with metrics
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				renderFrame.updateTitle();
			}
		}, 1000, 1000);

		// loop to draw graphics to screen using BufferStrategy with 2 buffers
		while (true) {
			Graphics g = null;
			try {
				g = renderFrame.getBufferStrategy().getDrawGraphics();
				rayCaster.render(g);
			} finally {
				if (g != null)
					g.dispose();
			}
			renderFrame.getBufferStrategy().show();
		}
	}
}
