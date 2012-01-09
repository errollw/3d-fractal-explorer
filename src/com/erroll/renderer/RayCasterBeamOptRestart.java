package com.erroll.renderer;

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

public class RayCasterBeamOptRestart extends RayCasterRestart implements RayCasterBeamOptInterface {

	// constants
	protected final double DOUBLE_MAX = Double.MAX_VALUE;
	protected int PACKET_SIZE = 8;

	// size of coarse image to be produced
	protected int screenWidthCoarse;
	protected int screenHeightCoarse;

	// tmin values stored during beam optimization
	protected double[][] tminPackets;

	// Voxel size constants for checking if voxel is small enough to terminate traversal early if projected voxel size during the coarse traversal is less than
	// 1 pixel on the screen
	protected double voxelSizeConstantCoarseA = 0d;
	protected double voxelSizeConstantCoarseB = 0d;

	@Override
	public void initialise(Properties propsParam, CameraInterface cameraParam, int WParam, int HParam) {
		// assign fields
		props = propsParam;
		camera = cameraParam;
		screenWidth = WParam;
		screenHeight = HParam;
		PACKET_SIZE = Integer.parseInt(props.getProperty("PACKET_SIZE"));
		screenWidthCoarse = screenWidth / PACKET_SIZE;
		screenHeightCoarse = screenHeight / PACKET_SIZE;

		// create the RenderedOctree
		octreeModel = new OctreeModel(props);

		// create back buffered image with which to draw pixels
		backbuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
		imagePixelData = ((DataBufferInt) backbuffer.getRaster().getDataBuffer()).getData();

		// create int[] to store tmin packet data
		tminPackets = new double[(screenHeight / PACKET_SIZE) + 1][(screenWidth / PACKET_SIZE) + 1];

		// set constants for terminating voxel traversal early if projected voxel size is less than 1 pixel on the screen (or earlier for coarse image)
		voxelSizeConstantA = camera.getDistanceToViewplane() * screenWidth;
		voxelSizeConstantB = camera.getViewplaneTop().length() * 2d;
		voxelSizeConstantCoarseA = camera.getDistanceToViewplane() * screenWidthCoarse;
		voxelSizeConstantCoarseB = camera.getViewplaneTop().length() * 2d;
	}

	@Override
	public void render(Graphics g) {

		// tick over the fpsCounter
		if (metrics != null)
			metrics.registerFrameRender();

		// generate copy of the camera per frame to deal with issues where camera position is moved halfway through rendering
		Camera frameCamera = new Camera(camera);

		// generate rough image to get tmin values for each packet
		for (int row = 0; row < screenHeightCoarse; row++)
			for (int col = 0; col < screenWidthCoarse; col++)
				tminPackets[row][col] = coarseRayCast(new Ray(frameCamera.getPosition(), frameCamera.getVectorToPixel(col, row, screenWidthCoarse,
						screenHeightCoarse)));

		// now for every pixel on the screen, cast a ray through it at the octreeModel using coarse tmin values
		for (int row = 0; row < screenHeight; row++) {
			for (int col = 0; col < screenWidth; col++) {
				int pRow = row / PACKET_SIZE;
				int pCol = col / PACKET_SIZE;
				double tminRay = Math.min(tminPackets[pRow][pCol],
						Math.min(tminPackets[pRow + 1][pCol], Math.min(tminPackets[pRow][pCol + 1], tminPackets[pRow + 1][pCol + 1])));
				imagePixelData[row * screenHeight + col] = rayCast(
						new Ray(frameCamera.getPosition(), frameCamera.getVectorToPixel(col, row, screenWidth, screenHeight)), tminRay);
			}
		}

		// draw the image onto the graphics
		g.drawImage(backbuffer, 0, 0, null);
		backbuffer.flush();
	}

	@Override
	public double coarseRayCast(RayInterface ray) {

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

		// if ray misses bounding box stop and return MISSED RAY. Comment these lines out for strange behavior.
		if (tmin > tmax)
			return DOUBLE_MAX;

		// ----------------------------------------------------------------------------
		// We now know the ray intersects with the octree unit cube
		// ----------------------------------------------------------------------------

		// will loop until a color is returned
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
				return DOUBLE_MAX;

			// while node is not a leaf, descend hierarchy until leaf reached
			while (!node.isLeaf()) {

				// check if voxel is small enough to terminate hierarcy
				if ((boxDim * voxelSizeConstantCoarseA) < (tmin * voxelSizeConstantCoarseB))
					return tmin;

				// descend hierarchy
				boxDim /= 2d;

				// use step function to adjust boxMin
				Vector3d s = new Vector3d((P.x >= (boxMin.x + boxDim)) ? 1 : 0, (P.y >= (boxMin.y + boxDim)) ? 1 : 0, (P.z >= (boxMin.z + boxDim)) ? 1 : 0);
				boxMin.scaleAdd(boxDim, s, boxMin);
				node = node.getChild((int) s.x, (int) s.y, (int) s.z);
			}

			// a leaf node has now been reached; if node is a non-empty leaf, return its color
			if (node.isLeaf() && !node.isEmpty()) {
				return tmin;
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

			// tmin = Math.max(tx0, Math.max(ty0, tz0)); tmax = Math.min(tx1, Math.min(ty1, tz1));
			tmax = tx1 < ty1 ? tx1 < tz1 ? tx1 : tz1 : ty1 < tz1 ? ty1 : tz1;
			tmin = tmax;
		}
	}

	@Override
	public int rayCast(RayInterface ray, double tmin) {

		// if we know ray missed return black
		if (tmin == DOUBLE_MAX)
			return 0;

		// initialize variables to be used in the loop to start with the root node of the octree
		Vector3d boxMin = new Vector3d(octreeModel.getPosition());
		double boxDim = octreeModel.getDim();
		Vector3d pVec = ray.getDir();
		Vector3d cPos = ray.getStart();

		Vector3d P = new Vector3d();
		P.scaleAdd(tmin, ray.getDir(), ray.getStart());
		P.scaleAdd(0.0001d * boxDim, ray.getDir(), P);

		// ensure point isn't outside bounding volume
		if (Math.abs(P.x) > 1 || Math.abs(P.y) > 1 || Math.abs(P.z) > 1)
			return rayCast(ray);

		// ----------------------------------------------------------------------------
		// We now know the ray intersects with the octree unit cube
		// ----------------------------------------------------------------------------

		// will loop until a color is returned
		int itersBeamOpt = 0;
		while (true) {

			// reset node and bounding box for kd-restart algorithm
			OctreeNodeInterface node = octreeModel.getRoot();
			boxDim = octreeModel.getDim();
			boxMin = new Vector3d(octreeModel.getPosition());

			// calculate point where ray hits using current tmin value (adjust to take floating point calculations into account
			P = new Vector3d();
			P.scaleAdd(tmin, ray.getDir(), ray.getStart());
			P.scaleAdd(0.0001d * boxDim, ray.getDir(), P);

			// ensure point isn't outside bounding volume
			if (Math.abs(P.x) > 1 || Math.abs(P.y) > 1 || Math.abs(P.z) > 1)
				return debug ? getGreyscale(itersBeamOpt) : 0;

			// while node is not a leaf, descend hierarchy until leaf reached
			while (!node.isLeaf()) {
				itersBeamOpt++;

				// check if voxel is small enough to terminate hierarcy
				if ((boxDim * voxelSizeConstantA) < (tmin * voxelSizeConstantB))
					return debug ? getGreyscale(itersBeamOpt) : node.getColor();

				// descend hierarchy
				boxDim /= 2d;

				// use step function to adjust boxMin ((boxMin.x + boxDim) is boxMid)
				Vector3d s = new Vector3d((P.x >= (boxMin.x + boxDim)) ? 1 : 0, (P.y >= (boxMin.y + boxDim)) ? 1 : 0, (P.z >= (boxMin.z + boxDim)) ? 1 : 0);
				boxMin.scaleAdd(boxDim, s, boxMin);
				node = node.getChild((int) s.x, (int) s.y, (int) s.z);
			}

			// a leaf node has now been reached; if node is a non-empty leaf, return its color
			if (node.isLeaf() && !node.isEmpty()) {
				return debug ? getGreyscale(itersBeamOpt) : node.getColor();
			}

			// otherwise node is empty so we must set ray tmin to tmax of current node, so we need to calculate tmax again
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
			tmin = tx1 < ty1 ? tx1 < tz1 ? tx1 : tz1 : ty1 < tz1 ? ty1 : tz1;
		}

	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	@Override
	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	}

	@Override
	public int getPacketSize() {
		return PACKET_SIZE;
	}

	@Override
	public void setPacketSize(int packetSize) {
		PACKET_SIZE = packetSize;
	}
}
