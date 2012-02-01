package com.erroll.renderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import javax.vecmath.Vector3d;

import com.erroll.camera.Camera;
import com.erroll.metrics.Metrics;
import com.erroll.octree.OctreeNode;
import com.erroll.octree.scaleadaptation.Subdivider;

public class Renderer {

	// camera from which to cast rays
	private Camera camera;

	// list of potential root nodes to start traversal from
	private ArrayList<OctreeNode> rootNodes = new ArrayList<OctreeNode>();

	// screen size in pixels
	protected int screenWidth;
	protected int screenHeight;

	// BufferedImage in which to draw pixels during rendering
	protected BufferedImage backbuffer;
	protected int[] imagePixelData;

	// Voxel size constants for checking if voxel is small enough to terminate traversal early if projected voxel size is less than 1 pixel on the screen
	protected double voxelSizeConstantA = 0d;
	protected double voxelSizeConstantB = 0d;

	// Queue nodes here for subdivision
	private Subdivider subdivider;

	// Metrics object to record FPS
	private Metrics metrics;

	public void render(Graphics g) {
		// set constants for terminating voxel traversal early if projected voxel size is less than 1 pixel on the screen
		voxelSizeConstantA = camera.getDistanceToViewplane() * screenWidth;
		voxelSizeConstantB = camera.getViewplaneTop().length() / 2;

		// for every pixel on the screen, cast a ray through it at the octreeModel
		for (int row = 0; row < screenHeight; row++)
			for (int col = 0; col < screenWidth; col++)
				imagePixelData[row * screenHeight + col] = rayCast(new Ray(camera.getPosition(), camera.getVectorToPixel(col, row, screenWidth, screenHeight)));

		// draw the image onto the graphics
		g.drawImage(backbuffer, 0, 0, null);
		backbuffer.flush();

		// register that a frame has been drawn
		metrics.registerFrameRender();
	}

	public int rayCast(Ray ray) {

		// long time = System.nanoTime();

		// initialize variables to be used in the loop to start with the root node of the octree
		Vector3d boxMin = new Vector3d(-1d, -1d, -1d);
		double boxDim = 2d;
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

		// if ray misses bounding box stop and return black.
		if (tmin > tmax)
			return 0;

		// metrics.incrementData("ZONE1", System.nanoTime() - time);

		// ----------------------------------------------------------------------------
		// We now know the ray intersects with the bounding cube of the fractal
		// ----------------------------------------------------------------------------

		// initialize root node bounds before loop
		OctreeNode node = rootNodes.get(0);

		// will loop until a color is returned
		while (true) {

			// time = System.nanoTime();

			// calculate point where ray hits using current tmin value (adjust slightly to take floating point calculations into account)
			Vector3d P = new Vector3d();
			P.scaleAdd(0.0001 * boxDim + tmin, ray.getDir(), ray.getStart());

			// metrics.incrementData("ZONE2", System.nanoTime() - time);
			// time = System.nanoTime();

			// while node is not a leaf, descend hierarchy until leaf reached
			while (!node.isLeaf()) {
				// check if voxel is small enough to terminate hierarcy
				if ((boxDim * voxelSizeConstantA) < (tmin * voxelSizeConstantB))
					return node.getColor();

				// descend hierarchy
				boxDim /= 2d;

				// use step function to adjust boxMin ((boxMin.x + boxDim) is boxMid)
				Vector3d s = new Vector3d((P.x >= (boxMin.x + boxDim)) ? 1 : 0, (P.y >= (boxMin.y + boxDim)) ? 1 : 0, (P.z >= (boxMin.z + boxDim)) ? 1 : 0);
				boxMin.scaleAdd(boxDim, s, boxMin);
				node = node.getChild((int) s.x, (int) s.y, (int) s.z);
			}

			// metrics.incrementData("ZONE3", System.nanoTime() - time);
			// time = System.nanoTime();

			// a leaf node has now been reached; if node is a non-empty leaf, return its color
			if (node.isLeaf() && !node.isEmpty()) {
				if ((boxDim * voxelSizeConstantA) > (tmin * voxelSizeConstantB))
					subdivider.queueNode(node, boxMin, boxDim);

				return node.getColor();
			}

			// metrics.incrementData("ZONE4", System.nanoTime() - time);
			// time = System.nanoTime();

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

			// find tmax
			tmax = tx1 < ty1 ? tx1 < tz1 ? tx1 : tz1 : ty1 < tz1 ? ty1 : tz1;

			// metrics.incrementData("ZONE5", System.nanoTime() - time);
			// time = System.nanoTime();

			// find neighbor node if one exists and adjust boxMin position
			OctreeNode neighbor = null;
			int neighborId = 0;
			synchronized (node) {
				if (tmax == tx1) {
					neighborId = (ray.getDir().x > 0 ? 1 : 0);
					boxMin.x += ray.getDir().x > 0 ? boxDim : -boxDim;
				} else if (tmax == ty1) {
					neighborId = (ray.getDir().y > 0 ? 3 : 2);
					boxMin.y += ray.getDir().y > 0 ? boxDim : -boxDim;
				} else if (tmax == tz1) {
					neighborId = (ray.getDir().z > 0 ? 5 : 4);
					boxMin.z += ray.getDir().z > 0 ? boxDim : -boxDim;
				}
			}
			neighbor = node.getNeighbor(neighborId);

			// metrics.incrementData("ZONE6", System.nanoTime() - time);
			// time = System.nanoTime();

			// if no neighbor node found return black
			if (neighbor == null)
				return 0;

			// if coarser neighbor node found, snap boxMin onto coarser grid
			if (neighbor.getDepth() != node.getDepth()) {
				Vector3d oldBoxMin = new Vector3d(boxMin);

				boxDim = Math.pow(2d, -neighbor.getDepth() + 1d);
				boxMin.x = Math.floor(boxMin.x / boxDim) * boxDim;
				boxMin.y = Math.floor(boxMin.y / boxDim) * boxDim;
				boxMin.z = Math.floor(boxMin.z / boxDim) * boxDim;

				if (!neighbor.isLeaf()) {
					node.setNeighbor(neighborId, neighbor.getChild(oldBoxMin.x >= (boxMin.x + boxDim / 2) ? 1 : 0, oldBoxMin.y >= (boxMin.y + boxDim / 2) ? 1
							: 0, oldBoxMin.z >= (boxMin.z + boxDim / 2) ? 1 : 0));
				}
			}

			// metrics.incrementData("ZONE7", System.nanoTime() - time);

			// finally set node to neighbor found and tmin to new position along ray
			node = neighbor;
			tmin = tmax;
		}
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters which re-initialize fields
	// ----------------------------------------------------------------------------

	public void setCamera(Camera cameraParam) {
		camera = cameraParam;

		// set constants for terminating voxel traversal early if projected voxel size is less than 1 pixel on the screen
		voxelSizeConstantA = camera.getDistanceToViewplane() * screenWidth;
		voxelSizeConstantB = camera.getViewplaneTop().length() / 2;
	}

	public void setScreenSize(int screenWidthParam, int screenHeightParam) {
		screenWidth = screenWidthParam;
		screenHeight = screenHeightParam;

		// create back buffered image with which to draw pixels
		backbuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
		imagePixelData = ((DataBufferInt) backbuffer.getRaster().getDataBuffer()).getData();
	}

	public void addRootNode(OctreeNode node) {
		rootNodes.add(node);
	}

	public void setSubdivider(Subdivider subdivider) {
		this.subdivider = subdivider;
	}

	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	};
}
