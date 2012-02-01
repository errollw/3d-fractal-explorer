package com.erroll.renderer;

import static org.junit.Assert.assertTrue;

import javax.vecmath.Vector3d;

import org.junit.Before;
import org.junit.Test;

import com.erroll.TestUtils;

public class RayTest {

	// the object to be tested on
	private Ray ray;

	// prepare starting parameters to test with
	private final Vector3d startStart = new Vector3d(1d, 1d, 1d);
	private final Vector3d startDir = new Vector3d(2d, 2d, 2d);

	@Before
	public void setUp() throws Exception {
		// make testing camera object
		ray = new Ray(startStart, startDir);
	}

	@Test
	public void testGetDir() {
		// test getter for default values
		Vector3d startDirNorm = new Vector3d(startDir);
		startDirNorm.normalize();
		assertTrue(ray.getDir().epsilonEquals(startDirNorm, TestUtils.EPSILON));

		// assert direction is normalized
		assertTrue(TestUtils.equals(ray.getDir().length(), 1d));
	}

	@Test
	public void testGetStart() {
		// test getter for default values
		assertTrue(ray.getStart().epsilonEquals(startStart, TestUtils.EPSILON));
	}
}
