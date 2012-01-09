package com.erroll.octree.scaleadaptation;

import javax.vecmath.Vector3d;

import com.erroll.octree.OctreeNodeInterface;

public class SubdivisionQueueEntry {

	private OctreeNodeInterface node;
	private Vector3d boxMin;
	private double boxDim;

	/**
	 * Creates an entry for a node to be queued for later subdivision with it's respective position and dimensions
	 * 
	 * @param nodeParam
	 *            The node to be subdivided
	 * @param boxMinParam
	 *            The position of the node
	 * @param boxDimParam
	 *            The dimension of one side of the node
	 */
	public SubdivisionQueueEntry(OctreeNodeInterface nodeParam, Vector3d boxMinParam, double boxDimParam) {
		this.node = nodeParam;
		this.boxMin = new Vector3d(boxMinParam);
		this.boxDim = boxDimParam;
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	public OctreeNodeInterface getNode() {
		return node;
	}

	public void setNode(OctreeNodeInterface node) {
		this.node = node;
	}

	public Vector3d getBoxMin() {
		return boxMin;
	}

	public void setBoxMin(Vector3d boxMin) {
		this.boxMin = boxMin;
	}

	public double getBoxDim() {
		return boxDim;
	}

	public void setBoxDim(double boxDim) {
		this.boxDim = boxDim;
	}

}
