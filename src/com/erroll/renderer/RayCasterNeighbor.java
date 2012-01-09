package com.erroll.renderer;

import javax.vecmath.Vector3d;

import com.erroll.octree.OctreeNodeInterface;

public class RayCasterNeighbor extends RayCasterRestart {

	// ------------------------------------------------------------------------------------
	// Override the original restart rayCast behavior with neighbor pointer implementation
	// ------------------------------------------------------------------------------------
	@Override
	public int rayCast(RayInterface ray) {

		// initialize variables to be used in the loop to start with the root node of the octree
		Vector3d boxMin = new Vector3d(octreeModel.getPosition());
		double rootBoxDim = octreeModel.getDim();
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

		// initialize root node bounds before loop
		OctreeNodeInterface node = octreeModel.getRoot();
		boxDim = octreeModel.getDim();
		boxMin = new Vector3d(octreeModel.getPosition());

		// will loop until a color is returned
		int iters = 0;
		while (true) {

			// calculate point where ray hits using current tmin value (adjust to take floating point calculations into account
			Vector3d P = new Vector3d();
			P.scaleAdd(tmin, ray.getDir(), ray.getStart());
			P.scaleAdd(0.0001d * boxDim, ray.getDir(), P);

			// while node is not a leaf, descend hierarchy until leaf reached
			while (!node.isLeaf()) {
				iters++;

				// check if voxel is small enough to terminate hierarcy
				if ((boxDim * voxelSizeConstantA) < (tmin * voxelSizeConstantB))
					return debug ? getGreyscale(iters) : node.getColor();

				// descend hierarchy
				boxDim /= 2d;

				// use step function to adjust boxMin ((boxMin.x + boxDim) is boxMid)
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

			// find tmax
			tmax = tx1 < ty1 ? tx1 < tz1 ? tx1 : tz1 : ty1 < tz1 ? ty1 : tz1;

			// find neighbor node if one exists and adjust boxMin position
			OctreeNodeInterface neighbor = null;
			if (tmax == tx1) {
				neighbor = node.getNeighbor(ray.getDir().x > 0 ? 1 : 0);
				boxMin.x += ray.getDir().x > 0 ? boxDim : -boxDim;
			} else if (tmax == ty1) {
				neighbor = node.getNeighbor(ray.getDir().y > 0 ? 3 : 2);
				boxMin.y += ray.getDir().y > 0 ? boxDim : -boxDim;
			} else if (tmax == tz1) {
				neighbor = node.getNeighbor(ray.getDir().z > 0 ? 5 : 4);
				boxMin.z += ray.getDir().z > 0 ? boxDim : -boxDim;
			}

			// if no neighbor node found return black
			if (neighbor == null)
				return debug ? getGreyscale(iters) : 0;

			// if coarser neighbor node found adjust boxMin
			if (neighbor.getDepth() != node.getDepth()) {
				boxDim = rootBoxDim * Math.pow(2, -neighbor.getDepth());
				boxMin.x = Math.floor(boxMin.x / boxDim) * boxDim;
				boxMin.y = Math.floor(boxMin.y / boxDim) * boxDim;
				boxMin.z = Math.floor(boxMin.z / boxDim) * boxDim;
			}

			// finally set node to neighbor found and tmin to new position along ray
			node = neighbor;
			tmin = tmax;
		}
	}
}
