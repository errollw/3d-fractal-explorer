package com.erroll.renderer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.vecmath.Vector3d;

import org.junit.Before;
import org.junit.Test;

import com.erroll.camera.Camera;
import com.erroll.metrics.Metrics;
import com.erroll.octree.OctreeNode;
import com.erroll.octree.OctreeNodePoolManager;
import com.erroll.octree.scaleadaptation.Subdivider;

public class RendererTest {

	// Renderer to be tested
	private Renderer renderer;

	@Before
	public void setUp() throws Exception {
		// make testing renderer object and initialise it
		renderer = new Renderer();
		renderer.setScreenSize(256, 256);
		renderer.setCamera(new Camera());
		renderer.setMetrics(new Metrics());
		renderer.setSubdivider(new Subdivider(new OctreeNodePoolManager(99)));
	}

	@Test
	public void testRayCastCube() {
		// make basic octree node voxel cube
		OctreeNode node = new OctreeNode();
		node.setLeaf(true);
		node.setEmpty(false);
		node.setColor(12345);
		renderer.addRootNode(node);

		// check we rays hit it being cast at it, and don't hit it being cast away from it
		assertFalse(renderer.rayCast(new Ray(new Vector3d(0, 0, 5), new Vector3d(0, 0, -1))) == 0);
		assertTrue(renderer.rayCast(new Ray(new Vector3d(0, 0, 5), new Vector3d(0, 1, 0))) == 0);
	}

	@Test
	public void testRayNeighbors() {
		OctreeNode node = new OctreeNode();
		node.setLeaf(false);
		node.setEmpty(false);
		node.setDepth(0);
		node.setColor(1);

		// make and set children
		OctreeNode[][][] childrenNodes = node.getChildren();
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					childrenNodes[x][y][z] = new OctreeNode();
					childrenNodes[x][y][z].setDepth(1);
					childrenNodes[x][y][z].setLeaf(true);
					childrenNodes[x][y][z].setEmpty((z == 1));
					childrenNodes[x][y][z].setColor(12345);
				}
			}
		}

		// set neighbors
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				childrenNodes[x][y][1].setNeighbor(4, childrenNodes[x][y][0]);
			}
		}

		renderer.addRootNode(node);
		assertFalse(renderer.rayCast(new Ray(new Vector3d(0, 0, 5), new Vector3d(0.1, 0.1, -1))) == 0);
		assertTrue(renderer.rayCast(new Ray(new Vector3d(0, 0, 5), new Vector3d(0, 1, 0))) == 0);
	}
}
