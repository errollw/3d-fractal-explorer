package com.erroll.octree.scaleadaptation;

import java.awt.Color;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.vecmath.Vector3d;

import com.erroll.math.fractal.FractalInterface;
import com.erroll.math.fractal.SierpinskiGasket;
import com.erroll.octree.OctreeNode;
import com.erroll.octree.OctreeNodePoolManager;

public class Subdivider implements Runnable {

	// queues of nodes to be subdivided,
	private BlockingQueue<OctreeNode> subdivNodeQueue = new LinkedBlockingQueue<OctreeNode>();
	private BlockingQueue<Vector3d> subdivBoxMinQueue = new LinkedBlockingQueue<Vector3d>();
	private BlockingQueue<Double> subdivBoxDimQueue = new LinkedBlockingQueue<Double>();

	// the fractal to be rendered
	private FractalInterface f = new SierpinskiGasket();

	// The Obejct Pool of octree nodes
	private OctreeNodePoolManager onpm;

	public Subdivider(OctreeNodePoolManager onpm) {
		this.onpm = onpm;
	}

	/**
	 * Queue a node for subdivision where it will be split into 8 children if any of them contain fractal detail; or made empty otherwise.
	 * 
	 * @param node
	 *            OctreeNode to be subdivided
	 * @param boxMin
	 *            The minimum position in space of the node
	 * @param boxDim
	 *            The width of the node
	 */
	public synchronized void queueNode(OctreeNode node, Vector3d boxMin, double boxDim) {
		try {
			subdivNodeQueue.put(node);
			subdivBoxMinQueue.put(boxMin);
			subdivBoxDimQueue.put(boxDim);
		} catch (InterruptedException e) {
			System.err.println("InterruptedException in queueing node");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// loop to consume and subdivide nodes if available
		while (true) {
			try {
				subdivideNode(subdivNodeQueue.take(), subdivBoxMinQueue.take(), subdivBoxDimQueue.take());
			} catch (InterruptedException e) {
				System.err.println("InterruptedException in consuming node");
				e.printStackTrace();
			}
		}
	}

	// sqrt(3) used in calculating cube diagonals
	final double sqrt3 = Math.sqrt(3);

	/**
	 * This will first check that if the node is subdivided, it will not be empty. If it will be empty then the node is set to empty. Otherwise the node is
	 * turned into a full inner node and given 8 children.
	 * 
	 * @param node
	 *            The node to be subdivided
	 * @param boxMin
	 *            The minimum position of the node in 3D space
	 * @param boxDim
	 *            The width of the node to be subdivided
	 */
	public void subdivideNode(OctreeNode node, Vector3d boxMin, double boxDim) {

		// TODO: add queued flag in node perhaps?
		if (!node.isLeaf())
			return;

		// constants to be used in checking if the fractal exists at a point
		final double bd2 = boxDim / 2;
		final double bd4 = boxDim / 4;
		final double offsetX = boxMin.x + bd4;
		final double offsetY = boxMin.y + bd4;
		final double offsetZ = boxMin.z + bd4;
		final double distance = sqrt3 * bd4;

		// array of booleans for whether a fractal exists in that child node
		boolean[][][] fractalExists = new boolean[2][2][2];

		// whether the node will be empty or not
		boolean empty = true;

		// loop through all positions in the node and check if fractal detail exists there
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					if (f.isInFractal(offsetX + (x * bd2), offsetY + (y * bd2), offsetZ + (z * bd2), distance)) {
						fractalExists[x][y][z] = true;
						empty = false;
					} else {
						fractalExists[x][y][z] = false;
					}
				}
			}
		}

		// if the node is determined to now be empty, set it to empty and return
		if (empty) {
			node.setEmpty(true);
			checkParentNowEmpty(node.getParent());
			return;
		}

		// now we know this node will not be empty so we can start allocating child nodes
		OctreeNode[][][] childNodes = node.getChildren();

		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					OctreeNode childNode = onpm.acquireNode();
					childNode.setParent(node);
					childNode.setLeaf(true);
					childNode.setDepth(node.getDepth() + 1);
					childNode.setEmpty(!fractalExists[x][y][z]);
					childNode.setColor(getColor(offsetX + (x * bd2), offsetY + (y * bd2), offsetZ + (z * bd2)));
					childNodes[x][y][z] = childNode;
				}
			}
		}

		// set inner neighbors of child nodes and set all outer neighbors (pointing outside parent node) to the parent's neighbor.
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					if (x == 1) {
						childNodes[x][y][z].setNeighbor(0, childNodes[0][y][z]);
					} else {
						childNodes[x][y][z].setNeighbor(0, node.getNeighbor(0));
					}
					if (x == 0) {
						childNodes[x][y][z].setNeighbor(1, childNodes[1][y][z]);
					} else {
						childNodes[x][y][z].setNeighbor(1, node.getNeighbor(1));
					}

					if (y == 1) {
						childNodes[x][y][z].setNeighbor(2, childNodes[x][0][z]);
					} else {
						childNodes[x][y][z].setNeighbor(2, node.getNeighbor(2));
					}
					if (y == 0) {
						childNodes[x][y][z].setNeighbor(3, childNodes[x][1][z]);
					} else {
						childNodes[x][y][z].setNeighbor(3, node.getNeighbor(3));
					}

					if (z == 1) {
						childNodes[x][y][z].setNeighbor(4, childNodes[x][y][0]);
					} else {
						childNodes[x][y][z].setNeighbor(4, node.getNeighbor(4));
					}
					if (z == 0) {
						childNodes[x][y][z].setNeighbor(5, childNodes[x][y][1]);
					} else {
						childNodes[x][y][z].setNeighbor(5, node.getNeighbor(5));
					}
				}
			}
		}

		// finally set the node to be a leaf for future rendering
		node.setLeaf(false);
	}

	private void checkParentNowEmpty(OctreeNode parentNode) {

		// check if all children of a parent is empty
		boolean parentEmpty = true;
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					if (!parentNode.getChild(x, y, z).isEmpty())
						parentEmpty = false;
				}
			}
		}

		// if this parent is now empty, then set the parent to a leaf and check if its parent is now also empty
		if (parentEmpty) {
			parentNode.setEmpty(true);
			parentNode.setLeaf(true);
			checkParentNowEmpty(parentNode.getParent());
		}
		return;
	}

	/**
	 * Gets rainbow color for a position in space between (-1,-1,-1) and (1,1,1)
	 * 
	 * @param x
	 *            The x coordinate to get color for
	 * @param y
	 *            The y coordinate to get color for
	 * @param z
	 *            The z coordinate to get color for
	 * @return The int rainbow color of the node at that position
	 */
	private final int getColor(double x, double y, double z) {
		Color c = new Color((int) (((x + 1d) / 2) * 255), (int) (((y + 1d) / 2) * 255), (int) (((z + 1d) / 2) * 255));
		return c.getRGB();
	}
}
