package com.erroll.octree;

import java.awt.Color;

public class OctreeNode implements OctreeNodeInterface {

	private boolean empty;
	private boolean leaf;
	private OctreeNodeInterface[][][] children;
	private OctreeNodeInterface[] neighbors;
	private int color = Color.BLUE.getRGB();
	private int depth;

	/**
	 * Creates an empty OctreeNode with no children or color. Space for neighbor nodes is allocated as all nodes should have neighbors.
	 */
	public OctreeNode() {
		empty = true;
		leaf = true;
		children = null;
		neighbors = new OctreeNodeInterface[6];
		color = 0;
		depth = 0;
	}

	/**
	 * Create an OctreeNode with the following parameters
	 * 
	 * @param emptyParam
	 *            Whether the node is empty (has no voxel data in it) or not
	 * @param leafParam
	 *            Whether the node is a leaf (is either empty or 1 unit of voxel data) or not
	 * @param childrenParam
	 *            A 3d array of OctreeNode children if it exists, null otherwise
	 * @param neighborsParam
	 *            An array of OctreeNode neighbors
	 * @param colorParam
	 *            The color of the OctreeNode
	 * @param depthParam
	 *            The depth of the node in the Octree hierarchy
	 */
	public OctreeNode(boolean emptyParam, boolean leafParam, OctreeNode[][][] childrenParam, OctreeNode[] neighborsParam, int colorParam, int depthParam) {
		empty = emptyParam;
		leaf = leafParam;
		children = childrenParam;
		neighbors = neighborsParam;
		color = colorParam;
		depth = depthParam;
	}

	@Override
	public boolean isEmpty() {
		return empty;
	}

	@Override
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	@Override
	public boolean isLeaf() {
		return leaf;
	}

	@Override
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	@Override
	public OctreeNodeInterface[][][] getChildren() {
		return children;
	}

	@Override
	public void setChildren(OctreeNodeInterface[][][] children) {
		this.children = children;
	}

	@Override
	public OctreeNodeInterface getChild(int x, int y, int z) {
		return leaf ? null : children[x][y][z];
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public void setColor(int c) {
		color = c;
	}

	@Override
	public void setColor() {
		color = 0;
	}

	@Override
	public OctreeNodeInterface[] getNeighbors() {
		return neighbors;
	}

	@Override
	public void setNeighbors(OctreeNodeInterface[] neighbors) {
		this.neighbors = neighbors;
	}

	@Override
	public OctreeNodeInterface getNeighbor(int neighborId) {
		return neighbors[neighborId];
	}

	@Override
	public void setNeighbor(int neighborId, OctreeNodeInterface neighbor) {
		neighbors[neighborId] = neighbor;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public void setDepth(int depth) {
		this.depth = depth;
	}
}
