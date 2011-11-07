package com.erroll.fractal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FractalUtilsTest {

	@Test
	public void testIsInGrid() {
		// test for a grid with voxels 0 spaces apart (no gaps)
		assertTrue(FractalUtils.isInGrid(0, 0, 0, 0));
		assertTrue(FractalUtils.isInGrid(1, 0, 0, 0));
		assertTrue(FractalUtils.isInGrid(2, 0, 0, 0));
		assertTrue(FractalUtils.isInGrid(3, 0, 0, 0));
		assertTrue(FractalUtils.isInGrid(1, 1, 1, 0));

		// test for a grid with voxels 1 space apart
		assertTrue(FractalUtils.isInGrid(0, 0, 0, 1));
		assertFalse(FractalUtils.isInGrid(1, 0, 0, 1));
		assertTrue(FractalUtils.isInGrid(2, 0, 0, 1));
		assertFalse(FractalUtils.isInGrid(3, 0, 0, 1));
		assertFalse(FractalUtils.isInGrid(1, 1, 1, 1));
		assertTrue(FractalUtils.isInGrid(2, 2, 2, 1));

		// test for a grid with voxels 2 spaces apart
		assertTrue(FractalUtils.isInGrid(0, 0, 0, 2));
		assertFalse(FractalUtils.isInGrid(1, 0, 0, 2));
		assertFalse(FractalUtils.isInGrid(2, 0, 0, 2));
		assertTrue(FractalUtils.isInGrid(3, 0, 0, 2));
		assertFalse(FractalUtils.isInGrid(1, 1, 1, 1));
		assertTrue(FractalUtils.isInGrid(2, 2, 2, 1));
	}

	@Test
	public void testIsInMengerSponge1() {
		// test basic corners of unit Menger Sponge
		assertTrue(FractalUtils.isInMengerSponge1(0, 0, 0, 3));
		assertFalse(FractalUtils.isInMengerSponge1(1, 1, 1, 3));
		assertTrue(FractalUtils.isInMengerSponge1(2, 2, 2, 3));
	}

	@Test
	public void testIsInMengerSponge2() {
		assertTrue(FractalUtils.isInMengerSponge2(0d, 0d, 0d));
		assertFalse(FractalUtils.isInMengerSponge2(0.5d, 0.5d, 0.5d));
		assertTrue(FractalUtils.isInMengerSponge2(1d, 1d, 1d));
	}

}
