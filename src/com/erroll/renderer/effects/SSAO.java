package com.erroll.renderer.effects;

public class SSAO {

	// the size/2 of the number of pixels to sample over
	private static final int KERNEL = 3;

	// the maximum distance away a point can be as a multiple of the difference between depths before being cut off
	private static final double CUT_OFF_DIST = 0.05d;

	// the amount to multiply the ratio of depths by to get the occlusion
	private static final double MULTIPLE = 128d * 1d;

	// the amount to tone down salient screen edge detection
	private static final double SALIENT_SCALE = 0.5d;

	// the maximum amount of shadow allowed by SSAO
	private static final int CUT_OFF_SHADOW = 200;

	/**
	 * Performs simple SSAO using a depth buffer only and stores calculated shadows in the occlusion array. Also stores information about minimum, average and
	 * maximum depths in the image to be returned in the minAvgMaxDepths array
	 * 
	 * @param imageDepth
	 *            The array of depths for each pixel in the image
	 * @param screenHeight
	 *            The height of the screen in pixels
	 * @param screenWidth
	 *            The width of the screen in pixels
	 * @param occlusion
	 *            The array in which to store the occlusion as a number between 0 (full) and 255 (none)
	 * @param minAvgMaxDepths
	 *            The minimum, average and maximum depths in the image
	 */
	public static final void setOcclusion(double[] imageDepth, int screenHeight, int screenWidth, int[] occlusion, double optTmin) {

		// set initial minimum, average, and maximum depths
		optTmin = Double.MAX_VALUE;

		// loop through every pixel in the screen (except edge ones without enough kernel space)
		for (int row = KERNEL; row < screenHeight - KERNEL; row++) {
			for (int col = KERNEL; col < screenWidth - KERNEL; col++) {

				int index = row * screenHeight + col;

				// if depth is maximum return 0
				if (imageDepth[index] == Double.MAX_VALUE) {
					occlusion[index] = 0;
					continue;
				}

				// otherwise calculate the total sum of ratio differences to get occlusion
				double sumOfDifferences = 0;
				for (int i = -KERNEL; i < KERNEL + 1; i++) {
					for (int j = -KERNEL; j < KERNEL + 1; j++) {
						double d = (imageDepth[index] - imageDepth[(row + i) * screenHeight + (col + j)]) / imageDepth[index];
						sumOfDifferences += d * ((Math.abs(d) > CUT_OFF_DIST) ? 0 : MULTIPLE);
					}
				}
				optTmin = optTmin < imageDepth[index] ? optTmin : imageDepth[index];

				// cut off the amount of shadow to be between 0 and CUT_OFF_SHADOW
				if (sumOfDifferences < 0)
					sumOfDifferences *= SALIENT_SCALE;
				if (sumOfDifferences > CUT_OFF_SHADOW)
					sumOfDifferences = CUT_OFF_SHADOW;

				// set the SSAO occlusion of that point
				occlusion[index] = (int) (sumOfDifferences);
			}
		}
	}

}
