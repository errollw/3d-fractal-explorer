package com.erroll.octree;

public interface OctreeNodeInterface {

	// ----------------------------------------------------------------------------
	// EMPTY/LEAF
	// ----------------------------------------------------------------------------

	/**
	 * @return true if the node is empty, false otherwise
	 */
	public boolean isEmpty();

	/**
	 * Sets whether the node is an empty one or not; used in generation
	 * 
	 * @param empty
	 *            true if the node is empty, false otherwise
	 */
	public void setEmpty(boolean empty);

	/**
	 * @return true if the node is a leaf (can be empty or full), false if it still has children
	 */
	public boolean isLeaf();

	/**
	 * Sets whether the node is a leaf (can be empty or full) or not
	 * 
	 * @param leaf
	 *            true if the node is a leaf, false otherwise
	 */
	public void setLeaf(boolean leaf);

	// ----------------------------------------------------------------------------
	// CHILDREN
	// ----------------------------------------------------------------------------

	/**
	 * Gets a 3d array of the children of this OctreeNode
	 * 
	 * @return 3d array [2][2][2] containing the children OctreeNodes of this OctreeNode
	 */
	public OctreeNodeInterface[][][] getChildren();

	/**
	 * Sets the children OctreeNode array
	 * 
	 * @param children
	 *            The 3d array of the children of this OctreeNode
	 */
	public void setChildren(OctreeNodeInterface[][][] children);

	/**
	 * Gets a specific child of the node
	 * 
	 * @param x
	 *            The x co-ordinate of the child (0 or 1)
	 * @param y
	 *            The y co-ordinate of the child (0 or 1)
	 * @param z
	 *            The z co-ordinate of the child (0 or 1)
	 * @return The child node requested or null if this node is a leaf
	 */
	public OctreeNodeInterface getChild(int x, int y, int z);

	// ----------------------------------------------------------------------------
	// NEIGHBOR
	// ----------------------------------------------------------------------------

	/**
	 * Gets the array of all neighboring nodes: 0,1 being neighboring x nodes in front and behind; 2,3 are y nodes and 4,5 are z nodes.
	 * 
	 * The neighbor of each node is null if it would be out of the bounding cube or a leaf or non-leaf node at depth >= that node's depth. It will not be
	 * possible for 2 nodes of the same depth to be neighboring each-other but not be eachother's neighbor pointers.
	 * 
	 * @return The array of neighboring nodes
	 */
	public OctreeNodeInterface[] getNeighbors();

	/**
	 * Sets the whole array of neighboring nodes at once.
	 * 
	 * The neighbor of each node is null if it would be out of the bounding cube or a leaf or non-leaf node at depth >= that node's depth. It will not be
	 * possible for 2 nodes of the same depth to be neighboring each-other but not be eachother's neighbor pointers.
	 * 
	 * @param neighbors
	 *            the new array of neighboring nodes
	 */
	public void setNeighbors(OctreeNodeInterface[] neighbors);

	/**
	 * Get's a specific neighboring node from the supplied position requested.
	 * 
	 * The neighbor of each node is null if it would be out of the bounding cube or a leaf or non-leaf node at depth >= that node's depth. It will not be
	 * possible for 2 nodes of the same depth to be neighboring each-other but not be eachother's neighbor pointers.
	 * 
	 * @param neighborId
	 *            The number to identify the position of the node requested: 0,1 being neighboring x nodes in front and behind; 2,3 are y nodes and 4,5 are z
	 *            nodes
	 * @return the neighboring octree node
	 */
	public OctreeNodeInterface getNeighbor(int neighborId);

	/**
	 * Sets a specific neighboring node at the supplied position.
	 * 
	 * The neighbor of each node is null if it would be out of the bounding cube or a leaf or non-leaf node at depth >= that node's depth. It will not be
	 * possible for 2 nodes of the same depth to be neighboring each-other but not be eachother's neighbor pointers.
	 * 
	 * @param neighborId
	 *            The identifying position of the new neighbor node
	 * @param neighbor
	 *            The new OctreeNodeInterface neighbor
	 */
	public void setNeighbor(int neighborId, OctreeNodeInterface neighbor);

	// ----------------------------------------------------------------------------
	// DEPTH (used by neighbor traversal if coarse neighbor found)
	// ----------------------------------------------------------------------------

	/**
	 * Get the depth of the node's octree level. The root node is 0, it's children are 1, etc.
	 * 
	 * @return The octree level the node is on in the heirarchy - root is 0
	 */
	public int getDepth();

	/**
	 * Set the depth of the node's octree level. The root node is 0, it's children are 1, etc.
	 */
	public void setDepth(int depth);

	// ----------------------------------------------------------------------------
	// COLOR
	// ----------------------------------------------------------------------------

	/**
	 * @return The color of the OctreeNode
	 */
	public int getColor();

	/**
	 * Sets the color of the OctreeNode to the Color c
	 * 
	 * @param c
	 */
	public void setColor(int c);

	/**
	 * Sets color of the OctreeNode to a default color
	 */
	public void setColor();
}
