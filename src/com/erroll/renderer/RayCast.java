package com.erroll.renderer;

import javax.vecmath.Vector3d;

import com.erroll.octree.OctreeNode;
import com.erroll.octree.scaleadaptation.Subdivider;

public class RayCast implements Runnable {

	private OctreeNode startNode;
	private Vector3d startBoxMin;
	private double startBoxDim;
	private Ray ray;
	private int index;
	private int[] imageColors;
	private double[] imageDepth;
	private Subdivider subdivider;
	private double voxelSizeConstantA;
	private double voxelSizeConstantB;

	/**
	 * Creates a RayCast thread which will determine the color and depth of the octree for a certain pixel
	 */
	public RayCast(OctreeNode startNode, Vector3d startBoxMin, double startBoxDim, Ray ray, int index, int[] imageColors, double[] imageDepth,
			Subdivider subdivider, double voxelSizeConstantA, double voxelSizeConstantB) {
		this.startNode = startNode;
		this.startBoxMin = startBoxMin;
		this.startBoxDim = startBoxDim;
		this.ray = ray;
		this.index = index;
		this.imageColors = imageColors;
		this.imageDepth = imageDepth;
		this.subdivider = subdivider;
		this.voxelSizeConstantA = voxelSizeConstantA;
		this.voxelSizeConstantB = voxelSizeConstantB;
	}

	@Override
	public void run() {

		// initialize variables to be used in the loop to start with the root node of the octree
		OctreeNode node = startNode;
		Vector3d boxMin = new Vector3d(startBoxMin);
		double boxDim = startBoxDim;
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
		if (tmin > tmax) {
			imageColors[index] = 0;
			imageDepth[index] = Double.MAX_VALUE;
			return;
		}

		// ----------------------------------------------------------------------------
		// We now know the ray intersects with the bounding cube of the fractal
		// ----------------------------------------------------------------------------

		// create vector for point in space where ray intersects the octree
		Vector3d P = new Vector3d();

		// will loop until a color is returned
		while (true) {

			// mark node and its bricks as having been visited by a ray
			node.visit();

			// calculate point where ray hits using current tmin value (adjust slightly to take floating point calculations into account)
			P.scaleAdd(0.0001 * boxDim + tmin, ray.getDir(), ray.getStart());

			// while node is not a leaf, descend hierarchy until leaf reached
			while (!node.isLeaf()) {
				// check if voxel is small enough to terminate hierarcy
				if ((boxDim * voxelSizeConstantA) < (tmin * voxelSizeConstantB)) {
					imageColors[index] = node.getColor();
					imageDepth[index] = tmin;
					return;
				}

				// descend hierarchy
				boxDim /= 2d;

				// use step function to adjust boxMin ((boxMin.x + boxDim) is boxMid)
				boolean[] s = { (P.x >= (boxMin.x + boxDim)) ? true : false, (P.y >= (boxMin.y + boxDim)) ? true : false,
						(P.z >= (boxMin.z + boxDim)) ? true : false };
				boxMin.x += s[0] ? boxDim : 0;
				boxMin.y += s[1] ? boxDim : 0;
				boxMin.z += s[2] ? boxDim : 0;
				node = node.getChild(s[0] ? 1 : 0, s[1] ? 1 : 0, s[2] ? 1 : 0);

				// mark node and its bricks as having been visited by a ray
				node.visit();
			}

			// a leaf node has now been reached; if node is a non-empty leaf, return its color
			if (node.isLeaf() && !node.isEmpty()) {
				if ((boxDim * voxelSizeConstantA) > (tmin * voxelSizeConstantB))
					subdivider.queueNode(node, boxMin, boxDim);

				imageColors[index] = node.getColor();
				imageDepth[index] = tmin;
				return;
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

			// find tmax
			tmax = tx1 < ty1 ? tx1 < tz1 ? tx1 : tz1 : ty1 < tz1 ? ty1 : tz1;

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

			// if no neighbor node found return black
			if (neighbor == null) {
				imageColors[index] = 0;
				imageDepth[index] = Double.MAX_VALUE;
				return;
			}

			// if coarser neighbor node found, snap boxMin onto coarser grid
			if (neighbor.getDepth() != node.getDepth()) {
				// save old position of box for setting neighbor once found
				double oldBoxMinX = boxMin.x;
				double oldBoxMinY = boxMin.y;
				double oldBoxMinZ = boxMin.z;

				boxDim = Math.pow(2d, -neighbor.getDepth() + 1d);
				boxMin.x = Math.floor(boxMin.x / boxDim) * boxDim;
				boxMin.y = Math.floor(boxMin.y / boxDim) * boxDim;
				boxMin.z = Math.floor(boxMin.z / boxDim) * boxDim;

				if (!neighbor.isLeaf()) {
					node.setNeighbor(neighborId, neighbor.getChild(oldBoxMinX >= (boxMin.x + boxDim / 2) ? 1 : 0,
							oldBoxMinY >= (boxMin.y + boxDim / 2) ? 1 : 0, oldBoxMinZ >= (boxMin.z + boxDim / 2) ? 1 : 0));
				}
			}

			// finally set node to neighbor found and tmin to new position along ray
			node = neighbor;
			tmin = tmax;
		}
	}

}
