package com.erroll.math.fractal;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class MengerSpongeTest {

	MengerSponge mengerSponge;

	@Before
	public void setUp() throws Exception {
		mengerSponge = new MengerSponge();
	}

	@Test
	public void testWholeVolume() {
		// check that fractal detail exists for within the space from (-1,-1,-1) to (1,1,1)
		assertTrue(mengerSponge.isInFractal(-1d, -1d, -1d, 2d));
	}

	@Test
	public void testCorners() {
		// check the corners return correct values for relatively small distances
		assertTrue(mengerSponge.isInFractal(-1d, -1d, -1d, 0.1d));
		assertTrue(mengerSponge.isInFractal(-1d, +1d, +1d, 0.1d));
		assertTrue(mengerSponge.isInFractal(+1d, -1d, +1d, 0.1d));
		assertTrue(mengerSponge.isInFractal(+1d, +1d, -1d, 0.1d));
		assertTrue(mengerSponge.isInFractal(-1d, -1d, +1d, 0.1d));
		assertTrue(mengerSponge.isInFractal(-1d, +1d, -1d, 0.1d));
		assertTrue(mengerSponge.isInFractal(+1d, -1d, -1d, 0.1d));
		assertTrue(mengerSponge.isInFractal(+1d, +1d, +1d, 0.1d));
	}

	@Test
	public void testCenter() {
		// check the corners return correct values for relatively small distances
		assertTrue(!mengerSponge.isInFractal(0d, 0d, 0d, 0.1d));
	}
}
