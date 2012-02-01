package com.erroll.octree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

public class OctreeNodeTest {

	// the object to be tested on
	private OctreeNode octreeNode;
	private OctreeNode neighbor;

	@Before
	public void setUp() throws Exception {
		// generate neighbor
		neighbor = new OctreeNode();

		// initialize octreeNode with test field values
		octreeNode = new OctreeNode();
		octreeNode.setEmpty(false);
		octreeNode.setLeaf(true);
		octreeNode.setNeighbor(0, neighbor);
		octreeNode.setDepth(3);
	}

	@Test
	public void testIsEmpty() {
		// test default octree values (not empty)
		assertFalse(octreeNode.isEmpty());
	}

	@Test
	public void testSetEmpty() {
		// set empty to true and check for change
		octreeNode.setEmpty(true);
		assertTrue(octreeNode.isEmpty());
	}

	@Test
	public void testIsLeaf() {
		// test default octree values (leaf node)
		assertTrue(octreeNode.isLeaf());
	}

	@Test
	public void testSetLeaf() {
		// set empty to true and check for change
		octreeNode.setLeaf(false);
		assertFalse(octreeNode.isLeaf());
	}

	@Test
	public void testSetChild() {
		// test setting a child means it no longer returns null
		assertTrue(octreeNode.getChild(0, 0, 1) == null);
		octreeNode.setChild(0, 0, 1, new OctreeNode());
		assertTrue(octreeNode.getChild(0, 0, 1) != null);
	}

	@Test
	public void testGetChildren() {
		assertTrue(octreeNode.getChildren()[0][0][1] == null);
		octreeNode.setChild(0, 0, 1, new OctreeNode());
		assertTrue(octreeNode.getChildren()[0][0][1] != null);
	}

	@Test
	public void testGetChild() {
		// test default octree values (no children)
		for (int x = 0; x < 2; x++)
			for (int y = 0; y < 2; y++)
				for (int z = 0; z < 2; z++)
					assertTrue(octreeNode.getChild(x, y, z) == null);
	}

	@Test
	public void testGetColor() {
		// test default octree values (black)
		assertTrue(octreeNode.getColor() == 0);
	}

	@Test
	public void testSetColorInt() {
		// set color to Color.RED and test for change
		octreeNode.setColor(Color.RED.getRGB());
		assertTrue(octreeNode.getColor() == Color.RED.getRGB());
	}

	@Test
	public void testGetNeighbor() {
		// check default test values work correctly
		assertTrue(octreeNode.getNeighbor(0) == neighbor);
		assertTrue(octreeNode.getNeighbor(1) == null);
	}

	@Test
	public void testSetNeighbor() {
		// set a neighbor node and check
		octreeNode.setNeighbor(5, neighbor);
		assertTrue(octreeNode.getNeighbor(5) == neighbor);
	}

	@Test
	public void testGetDepth() {
		// check test depth level
		assertTrue(octreeNode.getDepth() == 3);
	}

	@Test
	public void testSetDepth() {
		// change depth and check
		octreeNode.setDepth(6);
		assertTrue(octreeNode.getDepth() == 6);
	}

	@Test
	public void testParent() {
		// change depth and check
		OctreeNode parentNode = new OctreeNode();
		octreeNode.setParent(parentNode);
		assertTrue(octreeNode.getParent() == parentNode);
	}
}
