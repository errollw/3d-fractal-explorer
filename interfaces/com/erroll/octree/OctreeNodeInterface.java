package com.erroll.octree;

import java.awt.Color;



public interface OctreeNodeInterface {

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
	public void setChildren(OctreeNode[][][] children);

	/**
	 * @return The color of the OctreeNode
	 */
	public Color getColor();

	/**
	 * Sets the color of the OctreeNode to the Color c
	 * 
	 * @param c
	 */
	public void setColor(Color c);

	/**
	 * Sets color of the OctreeNode to a default color
	 */
	public void setColor();
}
