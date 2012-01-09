package com.erroll.renderer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Properties;

import javax.vecmath.Vector3d;

import com.erroll.camera.CameraInterface;
import com.erroll.octree.OctreeModel;
import com.erroll.octree.OctreeNodeInterface;
import com.erroll.octree.scaleadaptation.ScaleAdapter;

public class RayCasterRestartScaleAdaptation extends RayCasterRestart {

	ScaleAdapter sa = new ScaleAdapter();

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

		// set constants for terminating voxel traversal early if projected voxel size is less than 1 pixel on the screen and generating more voxels if they
		// occupy more than 1 pixel
		voxelSizeConstantA = camera.getDistanceToViewplane() * screenWidth;
		voxelSizeConstantB = camera.getViewplaneTop().length();

		// start scale adapter
		Thread scaleAdapter = new Thread(sa);
		scaleAdapter.setDaemon(true);
		scaleAdapter.start();
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
				synchronized (node) {
					iters++;

					// check if voxel is small enough to terminate hierarcy
					if ((boxDim * voxelSizeConstantA) < (tmin * voxelSizeConstantB)*2)
						return debug ? getGreyscale(iters) : node.getColor();

					// descend hierarchy
					boxDim /= 2d;

					// use step function to adjust boxMin
					Vector3d s = new Vector3d((P.x >= (boxMin.x + boxDim)) ? 1 : 0, (P.y >= (boxMin.y + boxDim)) ? 1 : 0, (P.z >= (boxMin.z + boxDim)) ? 1 : 0);
					boxMin.scaleAdd(boxDim, s, boxMin);
					node = node.getChild((int) s.x, (int) s.y, (int) s.z);
				}
			}

			// a leaf node has now been reached; if node is a non-empty leaf, return its color
			if (node.isLeaf() && !node.isEmpty()) {
				if ((boxDim * voxelSizeConstantA) > (tmin * voxelSizeConstantB))
					sa.queueForSubdivision(node, boxMin, boxDim);

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
}
