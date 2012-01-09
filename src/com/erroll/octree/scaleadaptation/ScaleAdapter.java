package com.erroll.octree.scaleadaptation;

import java.awt.Color;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.vecmath.Vector3d;

import com.erroll.fractal.FractalUtils;
import com.erroll.octree.OctreeNode;
import com.erroll.octree.OctreeNodeInterface;

public class ScaleAdapter implements Runnable {

	// Thread-safe queue for nodes to be subdivided
	private Queue<SubdivisionQueueEntry> subdivisionQueue = new ConcurrentLinkedQueue<SubdivisionQueueEntry>();

	@Override
	public void run() {

		// loop consuming the queue
		while (true) {
			SubdivisionQueueEntry sqe = subdivisionQueue.poll();

			// if queue is not empty, process the entry and subdivide
			if (sqe != null) {
				synchronized (sqe.getNode()) {
					processSubdivisionQueueEntry(sqe);
				}
			}
		}
	}

	/**
	 * Processes an entry popped from the subdivisionQueue by subdividing its node and also checking its normal (face) neighbors AND its edge and vertex
	 * neighbors (not directly supported in node structure to save space)
	 * 
	 * @param sqe
	 *            The queue entry of node, position and dimension
	 */
	private void processSubdivisionQueueEntry(SubdivisionQueueEntry sqe) {

		// extract contents of queue entry
		OctreeNodeInterface node = sqe.getNode();
		Vector3d boxMin = sqe.getBoxMin();
		double boxDim = sqe.getBoxDim();

		// generate all colors for new children of this node (there will be at least 1)
		int[][][] childColors = new int[2][2][2];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					boolean b = FractalUtils.sierpinski3(boxMin.x + i * (boxDim / 2), boxMin.y + j * (boxDim / 2), boxMin.z + k * (boxDim / 2));
					childColors[i][j][k] = b ? getColor(boxMin.x + i * (boxDim / 2), boxMin.y + j * (boxDim / 2), boxMin.z + k * (boxDim / 2)) : Color.BLACK
							.getRGB();
				}
			}
		}

		// subdivide the node
		subdivideNode(node, childColors);

		// loop through all neighbors to check if they should be subdivided, both NORMAL (face) and EXOTIC (edge and vertex)
		for (int dx = -1; dx < 2; dx++) {
			for (int dy = -1; dy < 2; dy++) {
				for (int dz = -1; dz < 2; dz++) {

					// continue on selecting same node as one in SubdivisionQueueEntry
					if (dx == 0 && dy == 0 && dz == 0)
						continue;

					// initialise counts of how many times to move each direction
					int dxLeft = dx;
					int dyLeft = dy;
					int dzLeft = dz;

					// this will be the neighboring node containing the point described by dx, dy and dz
					OctreeNodeInterface nbrNode = node;

					// set nbrNode to correct neighbor by choosing possible X, Y or Z neighboring node with maximum depth (so smallest size)
					int maxDepth = 0;
					while (!(dxLeft == 0 && dyLeft == 0 && dzLeft == 0)) {
						OctreeNodeInterface nbrNodeX = dxLeft == 0 ? null : nbrNode.getNeighbor(dxLeft == -1 ? 0 : 1);
						OctreeNodeInterface nbrNodeY = dyLeft == 0 ? null : nbrNode.getNeighbor(dyLeft == -1 ? 2 : 3);
						OctreeNodeInterface nbrNodeZ = dzLeft == 0 ? null : nbrNode.getNeighbor(dzLeft == -1 ? 4 : 5);
						maxDepth = Math.max(nbrNodeX == null ? -1 : nbrNodeX.getDepth(),
								Math.max(nbrNodeY == null ? -1 : nbrNodeY.getDepth(), nbrNodeZ == null ? -1 : nbrNodeZ.getDepth()));

						// escape loop if neighbors are out of boundingCube
						if (maxDepth == -1)
							break;

						if (nbrNodeX != null && nbrNodeX.getDepth() == maxDepth) {
							// move neighborNode to node neighboring in X Axis
							nbrNode = nbrNodeX;
							dxLeft = 0;
						} else if (nbrNodeY != null && nbrNodeY.getDepth() == maxDepth) {
							// move neighborNode to node neighboring in Y Axis
							nbrNode = nbrNodeY;
							dyLeft = 0;
						} else if (nbrNodeZ != null && nbrNodeZ.getDepth() == maxDepth) {
							// move neighborNode to node neighboring in Z Axis
							nbrNode = nbrNodeZ;
							dzLeft = 0;
						}
					}
					// if neighbors are out of bounding cube, continue
					if (maxDepth == -1)
						continue;

					// check if that node is actually an empty leaf, continue otherwise
					if (!nbrNode.isEmpty() || !nbrNode.isLeaf())
						continue;

					// check that fractal exists there, continue otherwise
					if (!FractalUtils.sierpinski3(boxMin.x + dx * boxDim, boxMin.y + dy * boxDim, boxMin.z + dz * boxDim))
						continue;

					// get position and size of neighbor node, if coarser neighbor node found adjust boxMin
					Vector3d nbrBoxMin = new Vector3d(boxMin.x + dx * boxDim, boxMin.y + dy * boxDim, boxMin.z + dz * boxDim);
					double nbrBoxDim = boxDim;

					if (nbrNode.getDepth() != node.getDepth()) {
						nbrBoxDim = 2 * Math.pow(2, -nbrNode.getDepth());
						nbrBoxMin.x = Math.floor(nbrBoxMin.x / nbrBoxDim) * nbrBoxDim;
						nbrBoxMin.y = Math.floor(nbrBoxMin.y / nbrBoxDim) * nbrBoxDim;
						nbrBoxMin.z = Math.floor(nbrBoxMin.z / nbrBoxDim) * nbrBoxDim;

						// create the node that should be filled by repeatedly subdividing empty nodes
						Vector3d targetBoxMin = new Vector3d(boxMin.x + dx * boxDim, boxMin.y + dy * boxDim, boxMin.z + dz * boxDim);
						while (nbrNode.getDepth() < node.getDepth()) {
							synchronized (nbrNode) {
								// first subdivide empty node to create hierarchy of empty nodes
								subdivideEmptyNode(nbrNode, getColor(targetBoxMin.x, targetBoxMin.y, targetBoxMin.z));

								// descend hierarchy
								nbrBoxDim /= 2d;

								// use step function to adjust boxMin
								Vector3d s = new Vector3d((targetBoxMin.x >= (nbrBoxMin.x + nbrBoxDim)) ? 1 : 0,
										(targetBoxMin.y >= (nbrBoxMin.y + nbrBoxDim)) ? 1 : 0, (targetBoxMin.z >= (nbrBoxMin.z + nbrBoxDim)) ? 1 : 0);
								nbrBoxMin.scaleAdd(nbrBoxDim, s, nbrBoxMin);
								nbrNode = nbrNode.getChild((int) s.x, (int) s.y, (int) s.z);
							}
						}
					}

					synchronized (nbrNode) {
						// finally fill the node
						nbrNode.setEmpty(false);
						nbrNode.setLeaf(true);
						nbrNode.setColor(getColor(nbrBoxMin.x, nbrBoxMin.y, nbrBoxMin.z));
					}
				}
			}
		}
	}

	/**
	 * Add a node to be queued for subdivision to the subdivisionQueue
	 * 
	 * @param node
	 *            OctreeNodeInterface to be subdivided
	 * @param boxMin
	 *            The position of the node to be subdivided
	 * @param boxDim
	 *            The dimension of the node to be subdivided, it's children will have dimension boxDim/2
	 */
	public void queueForSubdivision(OctreeNodeInterface node, Vector3d boxMin, double boxDim) {
		subdivisionQueue.offer(new SubdivisionQueueEntry(node, boxMin, boxDim));
	}

	// TODO: make this right
	private int getColor(double x, double y, double z) {
		Color c = new Color((int) (((x + 1d) / 2) * 255), (int) (((y + 1d) / 2) * 255), (int) (((z + 1d) / 2) * 255));
		return c.getRGB();
	}

	/**
	 * Split this leaf node into 8 new child leaf nodes, at least one of which must be full, as determined by the colors passed into it (Color.BLACK means that
	 * leaf node is empty) Assign all neighbor pointers correctly for these children.
	 * 
	 * @param node
	 *            The parent node being subdivided
	 * @param childColors
	 *            A 3D array[2][2][2] of the colors of children to be created for the node
	 */
	private void subdivideNode(OctreeNodeInterface node, int[][][] childColors) {
		// only subdivide node if it is a leaf (empty or non-empty), return otherwise and keep it as it is
		if (!node.isLeaf())
			return;

		// only subdivide node if it will have at least one non-empty child
		boolean empty = true;
		for (int x = 0; x < 2; x++)
			for (int y = 0; y < 2; y++)
				for (int z = 0; z < 2; z++)
					if (childColors[x][y][z] != Color.BLACK.getRGB())
						empty = false;
		if (empty)
			return;

		// set node as being not a leaf and not empty
		node.setLeaf(false);
		node.setEmpty(false);

		// create an array of children nodes and assign them to the node being subdivided
		OctreeNodeInterface[][][] children = new OctreeNode[2][2][2];
		node.setChildren(children);

		int colorAverage = 0;
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					OctreeNodeInterface leaf = new OctreeNode();
					children[x][y][z] = leaf;
					leaf.setDepth(node.getDepth() + 1);
					leaf.setEmpty(childColors[x][y][z] == Color.BLACK.getRGB() ? true : false);
					leaf.setLeaf(true);
					leaf.setColor(childColors[x][y][z]);

					// Average the children's color for previously empty ndoes. TODO: ignore for non-empty node being subdivided
					colorAverage = (int) (leaf.isEmpty() ? colorAverage : colorAverage == 0 ? leaf.getColor()
							: (((((colorAverage) ^ (leaf.getColor())) & 0xfffefefeL) >> 1) + ((colorAverage) & (leaf.getColor()))));
				}
			}
		}

		// TODO: ignore for non-empty node being subdivided
		node.setColor(colorAverage);

		// set neighbor pointers for the new children nodes
		setChildrenNeighbors(node);
	}

	/**
	 * Split this EMPTY leaf node into 8 new EMPTY child leaf nodes. Assign all neighbor pointers correctly for these children. This function is used when a
	 * larger empty node must be broken down because one of it's eventual children will be non-empty.
	 * 
	 * @param node
	 *            The empty leaf node to be subdivided into more empty leaf nodes
	 */
	private void subdivideEmptyNode(OctreeNodeInterface node, int color) {
		// only subdivide node if it is a leaf (empty or non-empty), return otherwise and keep it as it is
		if (!node.isLeaf() || !node.isEmpty())
			return;

		// set node as being not a leaf and not empty
		node.setLeaf(false);
		node.setEmpty(false);

		// create an array of children nodes and assign them to the node being subdivided
		OctreeNodeInterface[][][] children = new OctreeNode[2][2][2];
		node.setChildren(children);

		// each child node will also be empty
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					OctreeNodeInterface leaf = new OctreeNode();
					children[x][y][z] = leaf;
					leaf.setDepth(node.getDepth() + 1);
					leaf.setEmpty(true);
					leaf.setLeaf(true);
					leaf.setColor(Color.BLACK.getRGB());
				}
			}
		}
		// set node color to color of the eventual child
		node.setColor(color);

		// set neighbor pointers for the new children nodes
		setChildrenNeighbors(node);
	}

	/**
	 * Set neighbor pointers for all new children of a parent node. It will also set neighbor pointers for the neighbors of the parent's node's children if they
	 * now are neighboring a new child.
	 * 
	 * @param node
	 *            The parent node which has children which must have their neighbor pointers set
	 */
	private void setChildrenNeighbors(OctreeNodeInterface node) {
		OctreeNodeInterface[][][] children = node.getChildren();

		// Loop to correctly assign neighbors, NULL if no neighbor exists (out of bounding cube)
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {

					// FIRST: either easy case where child belongs to same parent sibling neighbor OR we have to leave the parent node to find neighbor
					if (x == 1) {
						children[x][y][z].setNeighbor(0, children[0][y][z]);
					} else {
						// neighbor node is NULL if parent's neighbor is NULL otherwise if parent's neighbor is a leaf
						children[x][y][z].setNeighbor(0, node.getNeighbor(0) == null ? null : node.getNeighbor(0).isLeaf() ? node.getNeighbor(0) : node
								.getNeighbor(0).getChild(1, y, z));

						// if neighbor of this new child node is now at the same depth as it, we can also assign it's neighbor's opposite neighbor pointer
						// to the new child node
						if (node.getNeighbor(0) != null && children[x][y][z].getNeighbor(0).getDepth() == children[x][y][z].getDepth())
							children[x][y][z].getNeighbor(0).setNeighbor(1, children[x][y][z]);
					}

					// Repeat for all other similar symmetric cases (6 in total - 1 for each neighbor)
					if (x == 0) {
						children[x][y][z].setNeighbor(1, children[1][y][z]);
					} else {
						children[x][y][z].setNeighbor(1, node.getNeighbor(1) == null ? null : node.getNeighbor(1).isLeaf() ? node.getNeighbor(1) : node
								.getNeighbor(1).getChild(0, y, z));
						if (node.getNeighbor(1) != null && children[x][y][z].getNeighbor(1).getDepth() == children[x][y][z].getDepth())
							children[x][y][z].getNeighbor(1).setNeighbor(0, children[x][y][z]);
					}

					if (y == 1) {
						children[x][y][z].setNeighbor(2, children[x][0][z]);
					} else {
						children[x][y][z].setNeighbor(2, node.getNeighbor(2) == null ? null : node.getNeighbor(2).isLeaf() ? node.getNeighbor(2) : node
								.getNeighbor(2).getChild(x, 1, z));
						if (node.getNeighbor(2) != null && children[x][y][z].getNeighbor(2).getDepth() == children[x][y][z].getDepth())
							children[x][y][z].getNeighbor(2).setNeighbor(3, children[x][y][z]);
					}

					if (y == 0) {
						children[x][y][z].setNeighbor(3, children[x][1][z]);
					} else {
						children[x][y][z].setNeighbor(3, node.getNeighbor(3) == null ? null : node.getNeighbor(3).isLeaf() ? node.getNeighbor(3) : node
								.getNeighbor(3).getChild(x, 0, z));
						if (node.getNeighbor(3) != null && children[x][y][z].getNeighbor(3).getDepth() == children[x][y][z].getDepth())
							children[x][y][z].getNeighbor(3).setNeighbor(2, children[x][y][z]);
					}

					if (z == 1) {
						children[x][y][z].setNeighbor(4, children[x][y][0]);
					} else {
						children[x][y][z].setNeighbor(4, node.getNeighbor(4) == null ? null : node.getNeighbor(4).isLeaf() ? node.getNeighbor(4) : node
								.getNeighbor(4).getChild(x, y, 1));
						if (node.getNeighbor(4) != null && children[x][y][z].getNeighbor(4).getDepth() == children[x][y][z].getDepth())
							children[x][y][z].getNeighbor(4).setNeighbor(5, children[x][y][z]);
					}

					if (z == 0) {
						children[x][y][z].setNeighbor(5, children[x][y][1]);
					} else {
						children[x][y][z].setNeighbor(5, node.getNeighbor(5) == null ? null : node.getNeighbor(5).isLeaf() ? node.getNeighbor(5) : node
								.getNeighbor(5).getChild(x, y, 0));
						if (node.getNeighbor(5) != null && children[x][y][z].getNeighbor(5).getDepth() == children[x][y][z].getDepth())
							children[x][y][z].getNeighbor(5).setNeighbor(4, children[x][y][z]);
					}
				}
			}
		}
	}
}
