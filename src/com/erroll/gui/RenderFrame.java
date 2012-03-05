package com.erroll.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Properties;

import javax.swing.JFrame;

import com.erroll.camera.Camera;
import com.erroll.camera.paths.FlightPath;
import com.erroll.camera.paths.mengersponge.MengerSpongeTourPath;
import com.erroll.camera.paths.mengersponge.MengerSpongeZoomPath;
import com.erroll.metrics.Metrics;
import com.erroll.octree.OctreeNode;
import com.erroll.octree.scaleadaptation.BrickManager;
import com.erroll.octree.scaleadaptation.Subdivider;
import com.erroll.properties.Parameters;
import com.erroll.renderer.Renderer;
import com.erroll.renderer.effects.ColorUtils;

public class RenderFrame extends JFrame {

	// generated serialVersionUID
	private static final long serialVersionUID = -2587976746868524926L;

	// Renderer to draw images
	private static Renderer renderer;

	// Camera to shoot rays from
	private static Camera camera;

	// Subdivider object to subdivide nodes
	private static Subdivider subdivider;

	// BrickManager to tidy up unused OctreeNodes
	private static BrickManager bm;

	// Properties file containing parameters
	private static Properties props;

	// Metrics object to record rendering data
	private static Metrics metrics;

	public RenderFrame() {
		// load properties
		props = Parameters.get();

		// set basic JFrame properties
		int screenSize = Integer.parseInt(props.getProperty("SCREEN_SIZE"));
		setPreferredSize(new Dimension(screenSize, screenSize));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
		pack();

		// create buffer strategy to double buffer renderer
		createBufferStrategy(2);

		// create Metrics object to record rendering data
		metrics = new Metrics();

		// create a default camera
		camera = new Camera();

		// create brick manager
		bm = new BrickManager();

		// create and initialise a subdivider thread to subdivide nodes as they are found
		subdivider = new Subdivider();
		subdivider.setBm(bm);
		Thread subdividerThread = new Thread(subdivider);
		subdividerThread.setDaemon(true);
		subdividerThread.start();

		// create and initialize renderer
		renderer = new Renderer();
		renderer.setScreenSize(screenSize, screenSize);
		renderer.setCamera(camera);
		renderer.setSubdivider(subdivider);
		renderer.setBrickManager(bm);
		renderer.setMetrics(metrics);
		renderer.setRecording(props.getProperty("RECORDING").equals("true"));

		// add a root node to the renderer for starting rendering
		OctreeNode rootNode = new OctreeNode();
		rootNode.setDepth(0);
		rootNode.setBrick(rootNode);
		rootNode.setLeaf(true);
		rootNode.setEmpty(false);
		rootNode.setColor(ColorUtils.getColor(255, 0, 0));
		renderer.setRootNode(rootNode);
	}

	public static void main(String[] args) {

		// frame to be rendered into
		final RenderFrame renderFrame = new RenderFrame();

		// set the flight path (if there is one)
		FlightPath path = null;
		String fractalType = props.getProperty("FRACTAL_TYPE");
		String pathType = props.getProperty("PATH_TYPE");

		// flight path depends on fractal type
		if (fractalType.equals("MengerSponge"))
			path = pathType.equals("Tour") ? new MengerSpongeTourPath(camera) : pathType.equals("Zoom") ? new MengerSpongeZoomPath(camera) : null;

		// keyListener to control the camera and flight path (ENTER to start)
		renderFrame.addKeyListener(new KeyboardControlListener(camera, path));

		// loop to draw graphics to screen using BufferStrategy with 2 buffers
		while (true) {
			Graphics g = null;
			try {
				g = renderFrame.getBufferStrategy().getDrawGraphics();
				renderer.render(g);
			} finally {
				if (g != null)
					g.dispose();
			}
			renderFrame.getBufferStrategy().show();

			// update the camera position using the flight path if one has been loaded
			if (path != null)
				path.updatePosition(camera, renderer, metrics);

			renderFrame.setTitle("FPS: " + metrics.getFps());
		}
	}
}
