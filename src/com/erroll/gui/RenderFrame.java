package com.erroll.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Properties;

import javax.swing.JFrame;
import javax.vecmath.Vector3d;

import com.erroll.camera.Camera;
import com.erroll.math.Axis;
import com.erroll.metrics.Metrics;
import com.erroll.octree.OctreeNode;
import com.erroll.octree.OctreeNodePoolManager;
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

	// OctreeNodePool to manage memory usage
	private static OctreeNodePoolManager onpm;

	// Subdivider object to subdivide nodes
	// private static SubdividerThreadless subdivider;
	private static Subdivider subdivider;

	private static BrickManager bm;

	// Metrics object to record rendering data
	private static Metrics metrics;

	public RenderFrame() {
		// load properties
		Properties props = Parameters.get();

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

		// create pool of octree nodes to be used
		onpm = new OctreeNodePoolManager(5000000);

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
		OctreeNode rootNode = onpm.acquireNode();
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
		renderFrame.addKeyListener(new KeyboardControlListener(camera, bm));

		// camera.moveCameraBy(new Vector3d(-0.9d, -0.9d, 0));
		camera.moveCameraBy(new Vector3d(0d, 0d, 10d));
		camera.rotateAroundOrigin(Axis.Y, -0.5);
		camera.rotateAroundOrigin(Axis.X, -0.5);
		camera.rotateAroundOrigin(Axis.X, 1.4);
		// camera.setDistanceToViewplane(camera.getDistanceToViewplane() * 6);

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

			// rotate camera
			// camera.rotateAroundOrigin(Axis.Y, 0.05);
			// camera.setDistanceToViewplane(camera.getDistanceToViewplane() * 1.01);

			Vector3d offset = new Vector3d(camera.getLookVector());
			offset.scale(renderer.getOptTmin() * 0.01);
			camera.moveCameraBy(offset);

			renderFrame.setTitle("FPS: " + metrics.getFps());
		}
	}
}
