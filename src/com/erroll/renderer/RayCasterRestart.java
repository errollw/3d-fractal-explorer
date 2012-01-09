package com.erroll.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Properties;

import javax.vecmath.Vector3d;

import com.erroll.camera.Camera;
import com.erroll.camera.CameraInterface;
import com.erroll.metrics.Metrics;
import com.erroll.octree.OctreeModel;
import com.erroll.octree.OctreeNodeInterface;

public class RayCasterRestart implements RayCasterInterface {

	// properties file to determine how to set up the model to be rendered
	protected Properties props;

	// screen size in pixels
	protected int screenWidth;
	protected int screenHeight;

	// camera from which to cast rays
	protected CameraInterface camera;

	// octree to be rendered
	protected OctreeModel octreeModel;

	// BufferedImage in which to draw pixels during rendering
	protected BufferedImage backbuffer;
	protected int[] imagePixelData;

	// Metrics object in which to record rendering data (not necessarily used)
	protected Metrics metrics;

	// debug flag to trigger debug behavior
	protected Boolean debug = true;

	// Voxel size constants for checking if voxel is small enough to terminate traversal early if projected voxel size is less than 1 pixel on the screen
	protected double voxelSizeConstantA = 0d;
	protected double voxelSizeConstantB = 0d;

	@Override
	public void initialise(Properties propsParam, CameraInterface cameraParam, int WParam, int HParam) {
		// assign fields
		props = propsParam;
		camera = cameraParam;
		screenWidth = WParam;
		screenHeight = HParam;

		// create the RenderedOctree
		octreeModel = new OctreeModel(props);

		// create back buffered image with which to draw pixels
		backbuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
		imagePixelData = ((DataBufferInt) backbuffer.getRaster().getDataBuffer()).getData();

		// set constants for terminating voxel traversal early if projected voxel size is less than 1 pixel on the screen
		voxelSizeConstantA = camera.getDistanceToViewplane() * screenWidth;
		voxelSizeConstantB = camera.getViewplaneTop().length() * 2d;
	}

	@Override
	public void render(Graphics g) {

		// tick over the fpsCounter
		if (metrics != null)
			metrics.registerFrameRender();

		// generate copy of the camera per frame to deal with issues where camera position is moved halfway through rendering
		Camera frameCamera = new Camera(camera);

		// for every pixel on the screen, cast a ray through it at the octreeModel
		for (int row = 0; row < screenHeight; row++)
			for (int col = 0; col < screenWidth; col++)
				imagePixelData[row * screenHeight + col] = rayCast(new Ray(frameCamera.getPosition(), frameCamera.getVectorToPixel(col, row, screenWidth,
						screenHeight)));

		// draw the image onto the graphics
		g.drawImage(backbuffer, 0, 0, null);
		backbuffer.flush();
	}
	
	@Override
	public int rayCast(RayInterface ray) {

		// initialize variables to be used in the loop to start with the root node of the octree
		Vector3d boxMin = new Vector3d(octreeModel.getPosition());
		double boxDim = octreeModel.getDim();
		Vector3d pVec = ray.getDir();
		Vector3d cPos = ray.getStart();

		// calculate t values for each corner
		double tx0 = (boxMin.x - cPos.x) / pVec.x;
		double tx1 = ((boxMin.x + boxDim) - cPos.x) / pVec.x;
		double ty0 = (boxMin.y - cPos.y) / pVec.y;
		double ty1 = ((boxMin.y + boxDim) - cPos.y) / pVec.y;
		double tz0 = (boxMin.z - cPos.z) / pVec.z;
		double tz1 = ((boxMin.z + boxDim) - cPos.z) / pVec.z;

		// ensure t0 and t1 are in the correct order
		if (tx1 < tx0) {
			double temp = tx0;
			tx0 = tx1;
			tx1 = temp;
		}
		if (ty1 < ty0) {
			double temp = ty0;
			ty0 = ty1;
			ty1 = temp;
		}
		if (tz1 < tz0) {
			double temp = tz0;
			tz0 = tz1;
			tz1 = temp;
		}

		// tmin = Math.max(tx0, Math.max(ty0, tz0)); tmax = Math.min(tx1, Math.min(ty1, tz1));
		double tmin = tx0 > ty0 ? tx0 > tz0 ? tx0 : tz0 : ty0 > tz0 ? ty0 : tz0;
		double tmax = tx1 < ty1 ? tx1 < tz1 ? tx1 : tz1 : ty1 < tz1 ? ty1 : tz1;

		// make sure tmin is positive
		tmin = tmin < 0 ? 0 : tmin;

		// if ray misses bounding box stop and return BLACK. Comment these lines out for strange behavior.
		if (tmin > tmax)
			return 0;

		// ----------------------------------------------------------------------------
		// We now know the ray intersects with the octree unit cube
		// ----------------------------------------------------------------------------

		// will loop until a color is returned
		int iters = 0;
		while (true) {

			// reset node and bounding box for kd-restart algorithm
			OctreeNodeInterface node = octreeModel.getRoot();
			boxDim = octreeModel.getDim();
			boxMin = new Vector3d(octreeModel.getPosition());

			// calculate point where ray hits using current tmin value (adjust to take floating point calculations into account
			Vector3d P = new Vector3d();
			P.scaleAdd(tmin, ray.getDir(), ray.getStart());
			P.scaleAdd(0.0001d * boxDim, ray.getDir(), P);

			// ensure point isn't outside bounding volume
			if (Math.abs(P.x) > 1 || Math.abs(P.y) > 1 || Math.abs(P.z) > 1)
				return debug ? getGreyscale(iters) : 0;

			// while node is not a leaf, descend hierarchy until leaf reached
			while (!node.isLeaf()) {
				iters++;

				// check if voxel is small enough to terminate hierarcy
				if ((boxDim * voxelSizeConstantA) < (tmin * voxelSizeConstantB))
					return debug ? getGreyscale(iters) : node.getColor();

				// descend hierarchy
				boxDim /= 2d;

				// use step function to adjust boxMin
				Vector3d s = new Vector3d((P.x >= (boxMin.x + boxDim)) ? 1 : 0, (P.y >= (boxMin.y + boxDim)) ? 1 : 0, (P.z >= (boxMin.z + boxDim)) ? 1 : 0);
				boxMin.scaleAdd(boxDim, s, boxMin);
				node = node.getChild((int) s.x, (int) s.y, (int) s.z);
			}

			// a leaf node has now been reached; if node is a non-empty leaf, return its color
			if (node.isLeaf() && !node.isEmpty()) {
				return debug ? getGreyscale(iters) : node.getColor();
			}

			// otherwise node is empty so we must set ray tmin to tmax of current node, so we need to calculate tmax again
			// calculate t values for each corner
			tx0 = (boxMin.x - cPos.x) / pVec.x;
			tx1 = ((boxMin.x + boxDim) - cPos.x) / pVec.x;
			ty0 = (boxMin.y - cPos.y) / pVec.y;
			ty1 = ((boxMin.y + boxDim) - cPos.y) / pVec.y;
			tz0 = (boxMin.z - cPos.z) / pVec.z;
			tz1 = ((boxMin.z + boxDim) - cPos.z) / pVec.z;

			// ensure t0 and t1 are in the correct order
			if (tx1 < tx0) {
				double temp = tx0;
				tx0 = tx1;
				tx1 = temp;
			}
			if (ty1 < ty0) {
				double temp = ty0;
				ty0 = ty1;
				ty1 = temp;
			}
			if (tz1 < tz0) {
				double temp = tz0;
				tz0 = tz1;
				tz1 = temp;
			}

			// tmax = Math.min(tx1, Math.min(ty1, tz1));
			tmax = tx1 < ty1 ? tx1 < tz1 ? tx1 : tz1 : ty1 < tz1 ? ty1 : tz1;
			tmin = tmax;
		}
	}

	/**
	 * Used by debug mode to return numbers of iterations as greyscale colors
	 * 
	 * @param iters
	 *            the number of iterations of the rendering loop
	 */
	protected int getGreyscale(int iters) {
		int rgbNum = (int) (((iters <= 64 ? iters : 64) / 64.0) * 255.0);
		return new Color(rgbNum, rgbNum, rgbNum).getRGB();
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	@Override
	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	}

	@Override
	public void setDebug(Boolean debug) {
		this.debug = debug;
	}
}
