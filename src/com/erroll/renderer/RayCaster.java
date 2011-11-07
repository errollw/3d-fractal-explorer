package com.erroll.renderer;

import java.awt.Color;

import javax.vecmath.Vector3d;

import com.erroll.camera.Camera;
import com.erroll.camera.CameraInterface;
import com.erroll.gui.FpsCounter;
import com.erroll.gui.RenderPanel;
import com.erroll.octree.OctreeModel;
import com.erroll.octree.OctreeNode;


public class RayCaster extends Thread {

	int[] imagePixelData;
	int W;
	int H;
	RenderPanel renderPanel;
	OctreeModel renderedOctree;
	FpsCounter fpsCounter = FpsCounter.getInstance();

	private CameraInterface camera;

	/**
	 * Initializes a RayCaster object with a reference to the raw pixel data of the image and height and width of the
	 * panel
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
	public void initialise(int[] imagePixelDataParam, int WParam, int HParam, RenderPanel renderPanelParam) {
		
		//assign fields
		imagePixelData = imagePixelDataParam;
		W = WParam;
		H = HParam;
		renderPanel = renderPanelParam;
		
		//create RenderedOctree
		renderedOctree = new OctreeModel();

		//create camera object
		camera = new Camera();
	}

	@Override
	public void run() {
		// loop forever
		while (true) {

			// redraw the image
			renderPanel.repaint();
			// tick over the fpsCounter
			fpsCounter.tick();

			// for every pixel on the screen, cast a ray through it at the renderedOctree
			for (int row = 0; row < H; row++)
				for (int col = 0; col < W; col++)
					imagePixelData[row * W + col] = rayCast(
							new Ray(camera.getPosition(), camera.getVectorToPixel(col, row, H, W)), renderedOctree)
							.getRGB();
		}
	}

	/**
	 * Returns a Color for a ray if the ray hits the RenderedOctree, black otherwise
	 * 
	 * @param ray
	 *            The Ray that will be marched over
	 * @renderedOctreeParam The RenderedOctree data for its position, size, and root node
	 * @return The color of that Octree that the ray intersects, or black if it misses
	 */
	public Color rayCast(Ray ray, OctreeModel renderedOctreeParam) {

		// initialize variables for the loop
		OctreeNode node = renderedOctreeParam.getRoot();
		double boxDim = renderedOctreeParam.getDim();
		Vector3d boxMid = new Vector3d();
		Vector3d boxMin = new Vector3d(renderedOctreeParam.getPosition());

		// if ray misses bounding box, return black. Comment these lines out for strange behavior.
		if (!RayCastUtils.boxClip(ray, boxMin, boxDim))
			return Color.BLACK;

		double tmin = RayCastUtils.boxClip_tmin(ray, boxMin, boxDim);

		// will loop until a color is returned
		while (true) {
			// reset node and bounding box for kd-restart algorithm
			node = renderedOctreeParam.getRoot();
			boxDim = renderedOctreeParam.getDim();
			boxMid = new Vector3d();
			boxMin = new Vector3d(renderedOctreeParam.getPosition());

			// calculate point where ray hits using current tmin value
			Vector3d P = new Vector3d();
			P.scaleAdd(tmin, ray.getDir(), ray.getStart());
			P.scaleAdd(0.0001d, ray.getDir(), P);

			// while node is leave descend hierarchy
			while (!node.isLeaf()) {
				boxDim /= 2d;
				boxMid.add(boxMin, new Vector3d(boxDim, boxDim, boxDim));
				Vector3d s = RayCastUtils.step(P, boxMid);

				boxMin.scaleAdd(boxDim, s, boxMin);
				node = node.getChildren()[(int) s.x][(int) s.y][(int) s.z];
			}

			// if node is a non-empty leaf, return its color
			if (node.isLeaf() && !node.isEmpty()) {
				return node.getColor();
			}

			// otherwise node is empty and set ray tmin to tmax of current node
			double tmax = RayCastUtils.boxClip_tmax(ray, boxMin, boxDim);
			tmin = tmax;

			// find direction of neighbor node
			Vector3d neighbour = RayCastUtils.findNeighbour(ray, boxMin, boxDim);

			// find new boxMin for potential neighbor of same dimension using
			boxMin.setX(boxMin.x + (neighbour.x * boxDim));
			boxMin.setY(boxMin.y + (neighbour.y * boxDim));
			boxMin.setZ(boxMin.z + (neighbour.z * boxDim));

			// depending on which neighbor is being checked, potential the ray has now exited the bounding box
			if ((neighbour.x == 1 && Math.abs(boxMin.x) >= 1) || (neighbour.x == -1 && Math.abs(boxMin.x) > 1))
				return Color.BLACK;
			if ((neighbour.y == 1 && Math.abs(boxMin.y) >= 1) || (neighbour.y == -1 && Math.abs(boxMin.y) > 1))
				return Color.BLACK;
			if ((neighbour.z == 1 && Math.abs(boxMin.z) >= 1) || (neighbour.z == -1 && Math.abs(boxMin.z) > 1))
				return Color.BLACK;
		}
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	public CameraInterface getCamera() {
		return camera;
	}

	public void setCamera(CameraInterface camera) {
		this.camera = camera;
	}
}
