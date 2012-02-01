package com.erroll.octree;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class OctreeNodePoolManagerTest {

	// OctreeNodePoolManager to be tested
	OctreeNodePoolManager poolManager;

	@Before
	public void setUp() throws Exception {
		// set up a default test pool with 5 nodes
		poolManager = new OctreeNodePoolManager(6);
	}

	@Test
	public void testAcquireNode() {
		// test getting 5 different nodes, none of them should be null
		OctreeNode[] nodes = new OctreeNode[5];
		for (int i = 0; i < 5; i++) {
			nodes[i] = poolManager.acquireNode();
			assertTrue(nodes[i] != null);
		}
		// getting one more node should yield null
		assertTrue(poolManager.acquireNode() == null);

		// test all 5 nodes are different
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (i != j) {
					assertTrue(nodes[i] != nodes[j]);
				}
			}
		}
	}

	@Test
	public void testReleaseNode() {
		// test getting 5 different nodes, none of them should be null
		OctreeNode[] nodes = new OctreeNode[6];
		for (int i = 0; i < 5; i++) {
			nodes[i] = poolManager.acquireNode();
			assertTrue(nodes[i] != null);
		}
		// getting one more node should yield null
		assertTrue(poolManager.acquireNode() == null);

		// release a node and try to get a node again, should not be null
		poolManager.releaseNode(2);
		nodes[5] = poolManager.acquireNode();
		assertTrue(nodes[5] != null);

		// test all 6 nodes are different
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				if (i != j) {
					assertTrue(nodes[i] != nodes[j]);
				}
			}
		}
	}
}
