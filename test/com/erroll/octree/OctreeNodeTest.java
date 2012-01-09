package com.erroll.octree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

public class OctreeNodeTest {

	// the object to be tested on
	private OctreeNode octreeNode;

	@Before
	public void setUp() throws Exception {
		// initialize octreeNode with default field values
		octreeNode = new OctreeNode(false, true, null, null, Color.BLUE.getRGB(), 0);
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
	public void testGetChildren() {
		// test default octree values (no children)
		assertTrue(octreeNode.getChildren() == null);
	}

	@Test
	public void testSetChildren() {
		// prepare children to test with
		OctreeNode[][][] children = new OctreeNode[2][2][2];

		// set children and test getter
		octreeNode.setChildren(children);
		assertTrue(octreeNode.getChildren().equals(children));
	}

	@Test
	public void testGetColor() {
		// test default octree values (Color.BLUE)
		assertTrue(octreeNode.getColor() == Color.BLUE.getRGB());
	}

	@Test
	public void testSetColorColor() {
		// set color to Color.RED and test for change
		octreeNode.setColor(Color.RED.getRGB());
		assertTrue(octreeNode.getColor() == Color.RED.getRGB());
	}
}
