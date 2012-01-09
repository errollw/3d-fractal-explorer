package com.erroll.fractal;

public class FractalUtils {

	public static boolean isInMandelBulb(double x, double y, double z) {
		double MAX_ITERATIONS = 8;
		double p = 8.0; // order of the bulb
		double r = Math.sqrt(x * x + y * y + z * z);

		double iter = 0;
		while (iter < MAX_ITERATIONS && r < 2.0) {
			double th = Math.atan2(y, x) * p;
			double ph = Math.asin(z / r) * p;
			double r2p = Math.pow(r, p);
			x = r2p * Math.cos(ph) * Math.cos(th) + x;
			y = r2p * Math.cos(ph) * Math.sin(th) + y;
			z = r2p * Math.sin(ph) + z;
			r = Math.sqrt(x * x + y * y + z * z);
			iter = iter + 1;
		}

		if (iter == MAX_ITERATIONS)
			return true;
		else
			return false;
	}

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

	public static boolean isInMengerSponge3(double x, double y, double z) {
		double r = x * x + y * y + z * z;
		double scale = 3;
		double MI = 15;
		int i = 0;
		for (i = 0; i < MI && r < 1000000000; i++) {
			// rotate1(x,y,z);

			x = Math.abs(x);
			y = Math.abs(y);
			z = Math.abs(z);
			if (x - y < 0) {
				double x1 = y;
				y = x;
				x = x1;
			}
			if (x - z < 0) {
				double x1 = z;
				z = x;
				x = x1;
			}
			if (y - z < 0) {
				double y1 = z;
				z = y;
				y = y1;
			}

			// rotate2(x,y,z);

			x = scale * x - 1 * (scale - 1);
			y = scale * y - 1 * (scale - 1);
			z = scale * z;
			if (z > 0.5 * 1 * (scale - 1))
				z -= 1 * (scale - 1);

			r = x * x + y * y + z * z;
		}
		return (i == MI);
	}

	public static boolean sierpinski3(double x, double y, double z) {
		double r = x * x + y * y + z * z;
		double scale = 2;
		int i = 0;
		double MI = 100;
		for (i = 0; i < MI && r < 7; i++) {

			// Folding... These are some of the symmetry planes of the tetrahedron
			if ((x + y) < 0) {
				double x1 = -y;
				y = -x;
				x = x1;
			}
			if ((x + z) < 0) {
				double x1 = -z;
				z = -x;
				x = x1;
			}
			if (y + z < 0) {
				double y1 = -z;
				z = -y;
				y = y1;
			}
			// if ((x - y) < 0) {
			// double x1 = y;
			// y = x;
			// x = x1;
			// }
			// if ((x - z) < 0) {
			// double x1 = z;
			// z = x;
			// x = x1;
			// }

			// Stretche about the point [1,1,1]*(scale-1)/scale; The "(scale-1)/scale" is here in order to keep the size of the fractal constant wrt scale
			// double a = 1;
			// double b = 0;
			// double c = 0;
			// Vector3d cS = new Vector3d(a - (c / 3.0), a + b + c, a - (c / 3.0));
			x = scale * x - 1 * (scale - 1);
			y = scale * y - 1 * (scale - 1);
			z = scale * z - 1 * (scale - 1);
			r = x * x + y * y + z * z;
		}
		return (i == MI);// the estimated distance
	}

	public static boolean tetraSierpinski(double x, double y, double z) {
		double r = x * x + y * y + z * z;
		double scale = 2;
		int i = 0;
		for (i = 0; i < 10 && r < 5.5; i++) {

			// Folding... These are some of the symmetry planes of the tetrahedron
			if (x + y < 0) {
				double x1 = -y;
				y = -x;
				x = x1;
			}
			if (x + z < 0) {
				double x1 = -z;
				z = -x;
				x = x1;
			}
			if (y + z < 0) {
				double y1 = -z;
				z = -y;
				y = y1;
			}

			// Stretche about the point [1,1,1]*(scale-1)/scale; The "(scale-1)/scale" is here in order to keep the size of the fractal constant wrt scale
			x = scale * x - (scale - 1);// equivalent to: x=scale*(x-cx); where cx=(scale-1)/scale;
			y = scale * y - (scale - 1);
			z = scale * z - (scale - 1);
			r = x * x + y * y + z * z;
		}
		return (i == 10);// the estimated distance
	}
}
