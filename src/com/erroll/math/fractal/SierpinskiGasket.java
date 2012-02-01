package com.erroll.math.fractal;

public class SierpinskiGasket implements FractalInterface {

	@Override
	public boolean isInFractal(double x, double y, double z, double d) {

		final double scale = 2;
		final double MI = 100;

		// number of iterations and point being tested
		int i;
		double r = x * x + y * y + z * z;

		for (i = 0; i < MI && r < 7; i++) {

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

			x = scale * x - 1 * (scale - 1);
			y = scale * y - 1 * (scale - 1);
			z = scale * z - 1 * (scale - 1);
			r = x * x + y * y + z * z;
		}

		// checks if the distance to fractal is less than d
		return ((Math.sqrt(r) - 2) * Math.pow(scale, (-i)) < d);
	}
}
