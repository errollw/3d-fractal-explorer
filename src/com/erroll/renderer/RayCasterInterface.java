package com.erroll.renderer;

import java.awt.Graphics;
import java.util.Properties;

import com.erroll.camera.CameraInterface;
import com.erroll.metrics.Metrics;

public interface RayCasterInterface {

	/**
	 * Initializes a RayCaster object with a reference to the raw pixel data of the image and height and width of the panel
	 * 
	 * @param imagePixelDataParam
	 *            The BufferedImage's DataBuffer's Data that can be modified
	 * @param WParam
	 *            the width of the screen in pixels
	 * @param HParam
	 *            the height of the screen in pixels
	 * @param renderPanelParam
	 *            the Panel that can be repainted to redraw the image
	 */
	public void initialise(Properties propsParam, CameraInterface cameraParam, int WParam, int HParam);

	/**
	 * Renders the scene into the graphics object g to be drawn onto the screen
	 * 
	 * @param g
	 *            The Graphics object to be rendered into
	 */
	public void render(Graphics g);

	/**
	 * Finds the color to be drawn on the screen for a particular ray intersecting the scene
	 * 
	 * @param ray
	 *            The ray being shot through the scene that will be marched over
	 * @return The int color of that Octree that the ray intersects, or black if it misses
	 */
	public int rayCast(RayInterface ray);

	/**
	 * Sets the Metrics object in which data about the rendering will be recorded (such as FPS, number of iterations etc)
	 * 
	 * @param metrics
	 *            The Metrics object in which to record rendering data
	 */
	public void setMetrics(Metrics metrics);
	
	/**
	 * Sets whether to display colors in a debug depth mode or not
	 * 
	 * @param debug True if debug colours are to be used, false otherwise
	 */
	public void setDebug(Boolean debug);
}
