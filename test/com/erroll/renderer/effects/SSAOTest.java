package com.erroll.renderer.effects;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SSAOTest extends SSAO {

	@Test
	public void test() {

		int screenHeight = 8;
		int screenWidth = 8;
		double[] imageDepth = new double[screenWidth * screenHeight];
		int[] occlusion = new int[screenWidth * screenHeight];
		double optTmin = 0d;

		// initialse all depths to maximum
		for (int row = 0; row < screenHeight; row++)
			for (int col = 0; col < screenWidth; col++)
				imageDepth[row * screenHeight + col] = Double.MAX_VALUE;

		// set manual depths in image depth array
		imageDepth[3 * screenWidth + 4] = 1;
		imageDepth[4 * screenWidth + 4] = 1.01;
		imageDepth[5 * screenWidth + 4] = 1;
		imageDepth[4 * screenWidth + 3] = 1;
		imageDepth[4 * screenWidth + 5] = 1;

		// set the occlusion
		SSAO.setOcclusion(imageDepth, screenHeight, screenWidth, occlusion, optTmin);

		// asset correct shadows have been generated
		assertTrue(occlusion[4 * screenWidth + 4] != 0);
		assertTrue(occlusion[1 * screenWidth + 1] == 0);
	}
}
