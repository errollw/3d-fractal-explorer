package com.erroll.fractal;

public class FractalUtils {

	/**
	 * Decides if a point at a specific location in a 3D grid with gaps between is filled or not.
	 * 
	 * @param x
	 *            The x coordinate of the point being checked
	 * @param y
	 *            The y coordinate of the point being checked
	 * @param z
	 *            The z coordinate of the point being checked
	 * @param gap
	 *            The size of the gaps between voxels
	 * @return true if it is to be filled or false if it is not
	 */
	public static boolean isInGrid(int x, int y, int z, int gap) {
		return (((x % (gap + 1)) + (y % (gap + 1)) + (z % (gap + 1))) == 0);
	}

	/**
	 * Decides if a point at a specific location in the MengerSponge is filled or not [Option 1]
	 * 
	 * @param x
	 *            The x coordinate of the point being checked
	 * @param y
	 *            The y coordinate of the point being checked
	 * @param z
	 *            The z coordinate of the point being checked
	 * @param size
	 *            The size of the Sierpinski Carpet being checked
	 * @return true if it is to be filled or false if it is not
	 */
	public static boolean isInMengerSponge1(int x, int y, int z, int size) {
		// base case 1 of 2, top row or left column or out of bounds should be full
		if ((x <= 0) || (y <= 0) || (z <= 0) || (x >= size) || (y >= size) || (z >= size))
			return true;

		// If the grid was split in 9 parts, what part(x2,y2) would x,y fit into?
		int x2 = x * 3 / size;
		int y2 = y * 3 / size;
		int z2 = z * 3 / size;

		// base case 2 of 2, if in the center square, it should be empty
		if (((x2 & y2) == 1) || ((x2 & z2) == 1) || ((y2 & z2) == 1))
			return false;

		/*
		 * general case:
		 * 
		 * offset x and y so it becomes bounded by 0..width/3 and 0..height/3 and prepares for recursive call some offset is added to make sure the parts have
		 * all the correct size when width and height isn't divisible by 3
		 */
		x -= (x2 * size + 2) / 3;
		y -= (y2 * size + 2) / 3;
		z -= (z2 * size + 2) / 3;
		size = (size + 2 - x2) / 3;

		return isInMengerSponge1(x, y, z, size);
	}

	/**
	 * Decides if a point at a specific location in the MengerSponge is filled or not. x, y and z must be between 0 and 1 and the number of iterations taken is
	 * hard-coded [Option 2]
	 * 
	 * @param x
	 *            The x coordinate of the point being checked
	 * @param y
	 *            The y coordinate of the point being checked
	 * @param z
	 *            The z coordinate of the point being checked
	 */
	public static boolean isInMengerSponge2(double x, double y, double z) {

		int iterations = 4;

		if (x < 0.0 || x > 1 || y < 0 || y > 1 || z < 0 || z > 1) {
			return false; // Not part of Menger Sponge
		}

		double p = 3.0;
		for (int m = 1; m < iterations; m++) {
			double xa = (x * p % 3);
			double ya = (y * p % 3);
			double za = (z * p % 3);
			if ((xa > 1.0 && xa < 2.0 && ya > 1.0 && ya < 2.0) || (ya > 1.0 && ya < 2.0 && za > 1.0 && za < 2.0)
					|| (xa > 1.0 && xa < 2.0 && za > 1.0 && za < 2.0))
				return false; // Not part of Menger Sponge

			p *= 3;
		}
		return true; // Is part of Menger Sponge
	}
}
