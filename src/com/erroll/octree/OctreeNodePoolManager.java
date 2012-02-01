package com.erroll.octree;

public class OctreeNodePoolManager {

	// pool of octree nodes and flags for if they are in use
	private OctreeNode[] nodePool;
	private boolean[] inUse;

	// index of the next free OctreeNode
	private int nextFree;

	/**
	 * Constructs a pool of OctreeNodes of size poolSize from which OctreeNodes can be borrowed and returned (poolsize-1 nodes will be available as there must
	 * always be one free one)
	 * 
	 * @param poolSize
	 *            The number of OctreeNodes that the pool starts with
	 */
	public OctreeNodePoolManager(int poolSize) {
		// arrays of nodes and flags for whether they are in use
		nodePool = new OctreeNode[poolSize];
		inUse = new boolean[poolSize];

		// generate poolSize number of OctreeNodes and store them in array, set inUse flag to false for each one
		for (int i = 0; i < poolSize; i++) {
			inUse[i] = false;
		}

		// next free OctreeNode is the first one
		nextFree = 0;
		nodePool[nextFree] = new OctreeNode();
	}

	/**
	 * The object that calls this will receive an OctreeNode that has already been created it can then use. It will have to "return" it later.
	 * 
	 * @return An OctreeNode to be used, null if there are no nodes left to be returned.
	 */
	public OctreeNode acquireNode() {

		// get position of OctreeNode to be borrowed out
		int nodeToBeAllocated = nextFree;
		inUse[nodeToBeAllocated] = true;

		// loop forward from nextFree position to get new nextFree if it exists before end of node array
		for (int i = nodeToBeAllocated + 1; i < nodePool.length; i++) {
			if (inUse[i] == false) {
				nextFree = i;
				break;
			}
		}
		// if nextFree has not been updated yet loop through from the start of the array
		if (nodeToBeAllocated == nextFree) {
			for (int i = 0; i < nodeToBeAllocated; i++) {
				if (inUse[i] == false) {
					nextFree = i;
					break;
				}
			}
			// if no free space can be found, return null
			if (nodeToBeAllocated == nextFree) {
				return null;
			}
		}

		// if an OctreeNode object has not yet been created here, make one
		if (nodePool[nextFree] == null)
			nodePool[nextFree] = new OctreeNode();

		// return OctreeNode that was at nextFree position
		return nodePool[nodeToBeAllocated];
	}

	/**
	 * The object that calls this releases the OctreeNode in the pool back into future use.
	 * 
	 * @param poolIndex
	 *            The index of the node being released back into the pool
	 */
	public void releaseNode(int poolIndex) {
		inUse[poolIndex] = false;
	}
}
