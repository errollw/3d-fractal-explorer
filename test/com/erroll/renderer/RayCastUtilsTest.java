package com.erroll.renderer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.vecmath.Vector3d;

import org.junit.Before;
import org.junit.Test;

import com.erroll.TestUtils;

public class RayCastUtilsTest {

	// the object to be tested on
		private RayCastUtils rcu;

		@Before
		public void setUp() throws Exception {
			// initialize octreeNode with default field values
			rcu = new RayCastUtils();
		}
	
	@Test
	public void testStep() {
		// assert (0,0,0) >= (1,1,1) is (F,F,F)
		assertTrue(rcu.step(new Vector3d(0d, 0d, 0d), new Vector3d(1d, 1d, 1d)).epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));

		// assert (1,0,0) >= (1,1,1) is (T,F,F)
		assertTrue(rcu.step(new Vector3d(1d, 0d, 0d), new Vector3d(1d, 1d, 1d)).epsilonEquals(new Vector3d(1d, 0d, 0d), TestUtils.EPSILON));

		// assert (1,2,0) >= (1,1,1) is (T,T,F)
		assertTrue(rcu.step(new Vector3d(1d, 2d, 0d), new Vector3d(1d, 1d, 1d)).epsilonEquals(new Vector3d(1d, 1d, 0d), TestUtils.EPSILON));

		// assert (-1,2,3) >= (-2,1,1) is (T,T,T)
		assertTrue(rcu.step(new Vector3d(-1d, 2d, 3d), new Vector3d(-2d, 1d, 1d)).epsilonEquals(new Vector3d(1d, 1d, 1d), TestUtils.EPSILON));
	}

	@Test
	public void testBoxClip() {
		// create parameters to test with
		Ray r = new Ray(new Vector3d(0d, 0d, 5d), new Vector3d(0d, 0d, -1d));
		Vector3d a = new Vector3d(-1, -1, -1);
		double dim = 2d;

		// assert the ray clips the box with default parameters
		assertTrue(rcu.boxClip(r, a, dim));

		// assert the ray clips the box if it's moved forwards or backwards
		assertTrue(rcu.boxClip(r, new Vector3d(-1, -1, 0), dim));
		assertTrue(rcu.boxClip(r, new Vector3d(-1, -1, -2), dim));

		// assert the ray misses the box if the box becomes tiny
		assertFalse(rcu.boxClip(r, a, 0.1d));

		// assert the ray misses the box if the box moves too far in perpendicular direction to the ray
		assertFalse(rcu.boxClip(r, new Vector3d(-1, 2, -1), dim));
		assertFalse(rcu.boxClip(r, new Vector3d(2, -1, -1), dim));
	}

	@Test
	public void testBoxClip_tmin() {
		// create parameters to test with
		Vector3d a = new Vector3d(-1, -1, -1);

		// check tmin adjusts correctly depending on distance to bounding box
		assertTrue(TestUtils.equals(rcu.boxClip_tmin(new Ray(new Vector3d(0d, 0d, 5d), new Vector3d(0d, 0d, -1d)), a, 2d), 4d));
		assertTrue(TestUtils.equals(rcu.boxClip_tmin(new Ray(new Vector3d(0d, 0d, 4d), new Vector3d(0d, 0d, -1d)), a, 2d), 3d));
		assertTrue(TestUtils.equals(rcu.boxClip_tmin(new Ray(new Vector3d(5d, 0d, 0d), new Vector3d(-1d, 0d, 0d)), a, 2d), 4d));

		// check tmin adjusts correctly depending on size of bounding box
		assertTrue(TestUtils.equals(rcu.boxClip_tmin(new Ray(new Vector3d(0d, 0d, 5d), new Vector3d(0d, 0d, -1d)), a, 2d), 4d));
		assertTrue(TestUtils.equals(rcu.boxClip_tmin(new Ray(new Vector3d(0d, 0d, 5d), new Vector3d(0d, 0d, -1d)), a, 3d), 3d));
	}

	@Test
	public void testBoxClip_tmax() {
		// create parameters to test with
		Vector3d a = new Vector3d(-1, -1, -1);

		// check tmax adjusts correctly depending on distance to bounding box
		assertTrue(TestUtils.equals(rcu.boxClip_tmax(new Ray(new Vector3d(0d, 0d, 5d), new Vector3d(0d, 0d, -1d)), a, 2d), 6d));
		assertTrue(TestUtils.equals(rcu.boxClip_tmax(new Ray(new Vector3d(0d, 0d, 4d), new Vector3d(0d, 0d, -1d)), a, 2d), 5d));
		assertTrue(TestUtils.equals(rcu.boxClip_tmax(new Ray(new Vector3d(5d, 0d, 0d), new Vector3d(-1d, 0d, 0d)), a, 2d), 6d));

		// check tmax adjusts correctly depending on size of bounding box
		assertTrue(TestUtils.equals(rcu.boxClip_tmax(new Ray(new Vector3d(-5d, 0d, 0d), new Vector3d(1d, 0d, 0d)), a, 2d), 6d));
		assertTrue(TestUtils.equals(rcu.boxClip_tmax(new Ray(new Vector3d(-5d, 0d, 0d), new Vector3d(1d, 0d, 0d)), a, 3d), 7d));
	}

	@Test
	public void testFindNeighbour() {

		// create parameters to test with
		Vector3d a = new Vector3d(-1, -1, -1);
		double dim = 2d;

		// check for correct neighbor identified with ray approaching bounding box either x-axis direction
		assertTrue(rcu.findNeighbour(new Ray(new Vector3d(-1d, 0d, 0d), new Vector3d(1d, 0d, 0d)), a, dim).epsilonEquals(new Vector3d(1d, 0d, 0d),
				TestUtils.EPSILON));
		assertTrue(rcu.findNeighbour(new Ray(new Vector3d(1d, 0d, 0d), new Vector3d(-1d, 0d, 0d)), a, dim).epsilonEquals(new Vector3d(-1d, 0d, 0d),
				TestUtils.EPSILON));

		// check for correct neighbor identified with ray approaching bounding box either y-axis direction
		assertTrue(rcu.findNeighbour(new Ray(new Vector3d(0d, -1d, 0d), new Vector3d(0d, 1d, 0d)), a, dim).epsilonEquals(new Vector3d(0d, 1d, 0d),
				TestUtils.EPSILON));
		assertTrue(rcu.findNeighbour(new Ray(new Vector3d(0d, 1d, 0d), new Vector3d(0d, -1d, 0d)), a, dim).epsilonEquals(new Vector3d(0d, -1d, 0d),
				TestUtils.EPSILON));

		// check for correct neighbor identified with ray approaching bounding box either z-axis direction
		assertTrue(rcu.findNeighbour(new Ray(new Vector3d(0d, 0d, -1d), new Vector3d(0d, 0d, 1d)), a, dim).epsilonEquals(new Vector3d(0d, 0d, 1d),
				TestUtils.EPSILON));
		assertTrue(rcu.findNeighbour(new Ray(new Vector3d(0d, 0d, 1d), new Vector3d(0d, 0d, -1d)), a, dim).epsilonEquals(new Vector3d(0d, 0d, -1d),
				TestUtils.EPSILON));
	}

}
