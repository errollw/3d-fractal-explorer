package com.erroll.octree;

import java.awt.Color;

public class OctreeNode implements OctreeNodeInterface {
	private boolean empty;
	private boolean leaf;
	private OctreeNode[][][] children = new OctreeNode[2][2][2];
	private Color color = Color.BLUE;

	/**
	 * Creates an empty OctreeNode with no children or color
	 */
	public OctreeNode() {
		empty = true;
		leaf = true;
		children = null;
		color = null;
	}

	/**
	 * Create an OctreeNode with the following parameters passed immediately
	 * 
	 * @param emptyParam
	 *            Whether the node is empty (has no voxel data in it) or not
	 * @param leafParam
	 *            Whether the node is a leaf (is either empty or 1 unit of voxel data) or not
	 * @param childrenParam
	 *            A 3d array of OctreeChildren if it exists, null otherwise
	 * @param colorParam
	 *            The color of the OctreeNode if it is a leaf
	 */
	public OctreeNode(boolean emptyParam, boolean leafParam, OctreeNode[][][] childrenParam, Color colorParam) {
		empty = emptyParam;
		leaf = leafParam;
		children = childrenParam;
		color = colorParam;
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
	public OctreeNode[][][] getChildren() {
		return children;
	}

	@Override
	public void setChildren(OctreeNode[][][] children) {
		this.children = children;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color c) {
		color = c;
	}

	@Override
	public void setColor() {
		color = Color.RED;
	}
}
