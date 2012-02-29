package com.erroll.octree;

public class OctreeNode {

	private boolean empty;
	private boolean leaf;
	private OctreeNode[][][] children;
	private OctreeNode[] neighbors;
	private OctreeNode parent;
	private int color;
	private int depth;

	// the brick this node belongs to
	private OctreeNode brick;

	// whether the node has been queued for subdivision
	private boolean queuedSubdiv;

	// whether the brick has been recently visited or not
	private boolean visited;

	// whether the node has been deleted by the brick manager
	private boolean deleted;

	/**
	 * Creates a new OctreeNode. This should only ever be used by the pool manager. The default values is for it to be an empty leaf with Color 0 (black)
	 */
	public OctreeNode() {
		empty = true;
		leaf = true;
		children = null;
		neighbors = new OctreeNode[6];
		color = 0;
		depth = 0;
		brick = null;
		queuedSubdiv = false;
		visited = true;
		deleted = false;
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
		if (children == null)
			children = new OctreeNode[2][2][2];
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

		if (neighbors[neighborIndex] != null && neighbors[neighborIndex].deleted) {
			OctreeNode newNeighbor = neighbors[neighborIndex].getBrick();
			while (newNeighbor.deleted)
				newNeighbor = newNeighbor.getBrick();

			setNeighbor(neighborIndex, newNeighbor);
			return newNeighbor;
		}

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

	/**
	 * @return The parent OctreeNode of this node
	 */
	public OctreeNode getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            The new parent OctreeNode of this node
	 */
	public void setParent(OctreeNode parent) {
		this.parent = parent;
	}

	/**
	 * @return True if this node has already been queued for subdivision, false otherwise
	 */
	public boolean isQueuedSubdiv() {
		return queuedSubdiv;
	}

	/**
	 * @param queuedSubdiv
	 *            True if this node is going to be subdivided, false once subdivision is over for potential future re-subdivision
	 */
	public void setQueuedSubdiv(boolean queuedSubdiv) {
		this.queuedSubdiv = queuedSubdiv;
	}

	/**
	 * @return The brick node this node is a member of
	 */
	public OctreeNode getBrick() {
		return brick;
	}

	/**
	 * Sets which brick node this node is a member of
	 * 
	 * @param brick
	 *            The brick node this node is a member of
	 */
	public void setBrick(OctreeNode brick) {
		this.brick = brick;
	}

	/**
	 * Sets this node's brick and all its parent bricks to a visited state
	 */
	public void visit() {
		visited = true;

		// only set brick as visited if not already marked
		if (!brick.getVisited())
			this.brick.visit();
	}

	/**
	 * Clears the visited value for the next pass.
	 */
	public void clearVisited() {
		visited = false;
	}

	/**
	 * @return Whether this node has been visited or not
	 */
	public boolean getVisited() {
		return visited;
	}

	/**
	 * Method to mark this node and all its children as having been deleted. Any references to them from now on should be discarded as they are invalid.
	 * References to children and neighbors are discarded to aid the garbage collector. A new brick node is set to the next brick that is still valid. This
	 * brick node pointer is followed to instead of this node and the invalid neighbor pointer will be corrected.
	 * 
	 * @param newBrick
	 *            The new node to recognize as being this node's brick.
	 */
	public void delete(OctreeNode newBrick) {
		synchronized (this) {
			deleted = true;

			if (!leaf && children != null)
				for (int x = 0; x < 2; x++)
					for (int y = 0; y < 2; y++)
						for (int z = 0; z < 2; z++)
							children[x][y][z].delete(newBrick);

			parent = null;
			children = null;
			neighbors = null;
			brick = newBrick;
		}
	}

	/**
	 * @return Whether this node has been deleted or not
	 */
	public boolean isDeleted() {
		return deleted;
	}
}
