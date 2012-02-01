package com.erroll.octree;

public class OctreeNode {

	private boolean empty;
	private boolean leaf;
	private OctreeNode[][][] children;
	private OctreeNode[] neighbors;
	private OctreeNode parent;
	private int color;
	private int depth;

	/**
	 * Creates a new OctreeNode. This should only ever be used by the pool manager. The default values is for it to be an empty leaf with Color 0 (black)
	 */
	public OctreeNode() {
		empty = true;
		leaf = true;
		children = new OctreeNode[2][2][2];
		neighbors = new OctreeNode[6];
		color = 0;
		depth = 0;
	}

	/**
	 * @return True if the node is empty, false if it has detail at its position or if it has children which are not empty.
	 */
	public boolean isEmpty() {
		return empty;
	}

	/**
	 * @param empty
	 *            The new value for whether this node is an empty node.
	 */
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	/**
	 * @return True if it has no children, false if it has children
	 */
	public boolean isLeaf() {
		return leaf;
	}

	/**
	 * @param leaf
	 *            The new value for whether this node is a leaf node
	 */
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	/**
	 * @return The actual array of children OctreeNodes
	 */
	public OctreeNode[][][] getChildren() {
		return children;
	}

	/**
	 * Get the OctreeNode child at position (x*size/2, y*size/2, z*size/2) from its minimum position in space.
	 * 
	 * @param x
	 *            The x position of the child relative to its parent
	 * @param y
	 *            The y position of the child relative to its parent
	 * @param z
	 *            The z position of the child relative to its parent
	 * @return The OctreeNode child of this node at the position requested
	 */
	public OctreeNode getChild(int x, int y, int z) {
		return children[x][y][z];
	}

	/**
	 * Set the OctreeNode child at position (x*size/2, y*size/2, z*size/2) from its minimum position in space.
	 * 
	 * @param x
	 *            The x position of the new child relative to its parent
	 * @param y
	 *            The y position of the new child relative to its parent
	 * @param z
	 *            The z position of the new child relative to its parent
	 */
	public void setChild(int x, int y, int z, OctreeNode child) {
		children[x][y][z] = child;
	}

	/**
	 * @return The color of the node as an RGB int
	 */
	public int getColor() {
		return color;
	}

	/**
	 * @param color
	 *            The new color to set the node as an RGB int
	 */
	public void setColor(int color) {
		this.color = color;
	}

	/**
	 * Gets the face neighbor OctreeNode at position described by index number. 0 & 1 are neighbors in the x axis (positive and negative respectively), 2 & 3 in
	 * y axis, 4 & 5 in z axis.
	 * 
	 * @param neighborIndex
	 *            Describes which neighbor is being requested
	 * @return The neighbor at the requested position
	 */
	public OctreeNode getNeighbor(int neighborIndex) {
		return neighbors[neighborIndex];
	}

	/**
	 * Sets the face neighbor OctreeNode at position described by index number. 0 & 1 are neighbors in the x axis (positive and negative respectively), 2 & 3 in
	 * y axis, 4 & 5 in z axis.
	 * 
	 * @param neighborIndex
	 *            Describes which neighbor is being set
	 * @param neighbor
	 *            The new neighbor OctreeNode to set at this position
	 */
	public void setNeighbor(int neighborIndex, OctreeNode neighbor) {
		neighbors[neighborIndex] = neighbor;
	}

	/**
	 * @return The depth of this node from the maximum bounding box root node (0)
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @param depth
	 *            The depth of this node from the maximum bounding box root node (0)
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	public OctreeNode getParent() {
		return parent;
	}

	public void setParent(OctreeNode parent) {
		this.parent = parent;
	}
}
