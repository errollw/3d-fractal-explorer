package com.erroll.math.fractal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.erroll.math.fractal.SierpinskiGasket;

public class SierpinskiGasketTest {

	SierpinskiGasket sierpinskiGasket;

	@Before
	public void setUp() throws Exception {
		sierpinskiGasket = new SierpinskiGasket();
	}

	@Test
	public void testWholeVolume() {
		// check that fractal detail exists for within the space from (-1,-1,-1) to (1,1,1)
		assertTrue(sierpinskiGasket.isInFractal(-1d, -1d, -1d, 2d));
	}

	@Test
	public void testCorners() {
		// check the corners return correct values for relatively small distances
		assertFalse(sierpinskiGasket.isInFractal(-1d, -1d, -1d, 0.1d));
		assertFalse(sierpinskiGasket.isInFractal(-1d, +1d, +1d, 0.1d));
		assertFalse(sierpinskiGasket.isInFractal(+1d, -1d, +1d, 0.1d));
		assertFalse(sierpinskiGasket.isInFractal(+1d, +1d, -1d, 0.1d));

		assertTrue(sierpinskiGasket.isInFractal(-1d, -1d, +1d, 0.1d));
		assertTrue(sierpinskiGasket.isInFractal(-1d, +1d, -1d, 0.1d));
		assertTrue(sierpinskiGasket.isInFractal(+1d, -1d, -1d, 0.1d));
		assertTrue(sierpinskiGasket.isInFractal(+1d, +1d, +1d, 0.1d));
	}
}
