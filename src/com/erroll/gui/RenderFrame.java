package com.erroll.gui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;

import com.erroll.camera.Camera;
import com.erroll.metrics.Metrics;
import com.erroll.octree.OctreeNode;
import com.erroll.octree.OctreeNodePoolManager;
import com.erroll.octree.scaleadaptation.Subdivider;
import com.erroll.renderer.Renderer;

/**
 * @author Erroll
 * 
 */
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
	private static Subdivider subdivider;

	// Metrics object to record rendering data
	private static Metrics metrics;

	public RenderFrame() {
		// set basic JFrame properties
		setPreferredSize(new Dimension(256, 256));
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
		onpm = new OctreeNodePoolManager(10000000);

		// create a default camera
		camera = new Camera();

		// create and initialise a subdivider thread to subdivide nodes as they are found
		subdivider = new Subdivider(onpm);
		Thread subdividerThread = new Thread(subdivider);
		subdividerThread.setDaemon(true);
		subdividerThread.start();

		// create and initialize renderer
		renderer = new Renderer();
		renderer.setScreenSize(256, 256);
		renderer.setCamera(camera);
		renderer.setSubdivider(subdivider);
		renderer.setMetrics(metrics);

		// add a root node to the renderer for starting rendering
		OctreeNode rootNode = onpm.acquireNode();
		rootNode.setLeaf(true);
		rootNode.setEmpty(false);
		rootNode.setColor(99999999);
		renderer.addRootNode(rootNode);
	}

	public static void main(String[] args) {

		// frame to be rendered into
		final RenderFrame renderFrame = new RenderFrame();
		renderFrame.addKeyListener(new KeyboardControlListener(camera));

		// camera.moveCameraBy(new Vector3d(-0.9d, -0.9d, 0));
		// camera.rotateAroundOrigin(Axis.Y, -0.5);
		// camera.rotateAroundOrigin(Axis.X, -0.5);
		// camera.rotateAroundOrigin(Axis.X, 1.4);
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

			renderFrame.setTitle("FPS: " + metrics.getFps());
		}
	}
}
