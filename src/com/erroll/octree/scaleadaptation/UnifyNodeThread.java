package com.erroll.octree.scaleadaptation;

import com.erroll.octree.OctreeNode;

public class UnifyNodeThread implements Runnable {

	private OctreeNode node;

	public UnifyNodeThread(OctreeNode node) {
		this.node = node;
	}

	@Override
	public void run() {
		synchronized (node) {
			// if node has already been deleted, do nothing
			if (node.isDeleted())
				return;

			// set node as a leaf
			node.setLeaf(true);

			// get all children of node and delete them
			for (int x = 0; x < 2; x++)
				for (int y = 0; y < 2; y++)
					for (int z = 0; z < 2; z++)
						node.getChild(x, y, z).delete(node);
		}
	}
}
