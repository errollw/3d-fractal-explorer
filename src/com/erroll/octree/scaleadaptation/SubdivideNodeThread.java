package com.erroll.octree.scaleadaptation;

import javax.vecmath.Vector3d;

import com.erroll.math.fractal.FractalInterface;
import com.erroll.octree.OctreeNode;
import com.erroll.renderer.effects.ColorUtils;

public class SubdivideNodeThread implements Runnable {

	// The interval in depths between different bricks
	private final int BRICK_INTERVAL = 2;

	// The brick manager
	BrickManager brickManager;

	// the fractal to be rendered
	private FractalInterface f;

	// details about the node to be subdivided
	private OctreeNode node;
	private Vector3d boxMin;
	private double boxDim;

	// square root of 3
	private final double sqrt3 = Math.sqrt(3d);

	/**
	 * Create a new thread to subdivide a single node in particular. Pass in all the objects it will need to compute the result.
	 */
	public SubdivideNodeThread(BrickManager brickManager, FractalInterface f, OctreeNode node, Vector3d boxMin, double boxDim) {
		this.brickManager = brickManager;
		this.f = f;
		this.node = node;
		this.boxMin = boxMin;
		this.boxDim = boxDim;
	}

	@Override
	public void run() {

		// modifying this node cannot be done in conjunction with deleting the node
		synchronized (node) {

			// if the node has already been deleted, do not subdivide
			if (node.isDeleted())
				return;

			// constants to be used in checking if the fractal exists at a point
			final double bd2 = boxDim / 2d;
			final double bd4 = boxDim / 4d;
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

			// now we know this node will not be empty right now so we can start allocating child nodes
			OctreeNode[][][] childNodes = node.getChildren();

			// determine if the parent node is now a new brick and set child's brick
			OctreeNode brickOfChild = node.getBrick();
			if (node.getDepth() % BRICK_INTERVAL == 0) {
				brickOfChild = node;
				brickManager.addBrick(node);
			}

			for (int x = 0; x < 2; x++) {
				for (int y = 0; y < 2; y++) {
					for (int z = 0; z < 2; z++) {
						OctreeNode childNode = new OctreeNode();
						childNode.setParent(node);
						childNode.setBrick(brickOfChild);
						childNode.setLeaf(true);
						childNode.setDepth(node.getDepth() + 1);
						childNode.setEmpty(!fractalExists[x][y][z]);
						// childNode.setColor(ColorUtils.getColor(166, 161, 91));
						childNode.setColor(ColorUtils.getPositionalColor(offsetX + (x * bd2), offsetY + (y * bd2), offsetZ + (z * bd2)));
						childNodes[x][y][z] = childNode;
					}
				}
			}

			// set inner neighbors of child nodes and set all outer neighbors (pointing outside parent node) to the parent's neighbor.
			for (int x = 0; x < 2; x++) {
				for (int y = 0; y < 2; y++) {
					for (int z = 0; z < 2; z++) {
						childNodes[x][y][z].setNeighbor(0, x == 1 ? childNodes[0][y][z] : node.getNeighbor(0));
						childNodes[x][y][z].setNeighbor(1, x == 0 ? childNodes[1][y][z] : node.getNeighbor(1));

						childNodes[x][y][z].setNeighbor(2, y == 1 ? childNodes[x][0][z] : node.getNeighbor(2));
						childNodes[x][y][z].setNeighbor(3, y == 0 ? childNodes[x][1][z] : node.getNeighbor(3));

						childNodes[x][y][z].setNeighbor(4, z == 1 ? childNodes[x][y][0] : node.getNeighbor(4));
						childNodes[x][y][z].setNeighbor(5, z == 0 ? childNodes[x][y][1] : node.getNeighbor(5));
					}
				}
			}

			// mark node as finished subdividing
			node.setQueuedSubdiv(false);

			// set the node to be a leaf for future rendering
			node.setLeaf(false);
		}
	}

	/**
	 * Check in case the node's parent is now empty if this is found to actually be an empty node. If it is, set it as empty.
	 * 
	 * @param parentNode
	 *            The node's parent which may now be empty.
	 */
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
}
