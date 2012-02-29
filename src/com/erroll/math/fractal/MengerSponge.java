package com.erroll.math.fractal;

public class MengerSponge implements FractalInterface {

	@Override
	public boolean isInFractal(double x, double y, double z, double d) {

		double r = x * x + y * y + z * z;
		double scale = 3;
		double MI = 100;
		int i = 0;

		for (i = 0; i < MI && r < 9; i++) {

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

			x = scale * x - 1 * (scale - 1);
			y = scale * y - 1 * (scale - 1);
			z = scale * z;

			if (z > 0.5 * 1 * (scale - 1))
				z -= 1 * (scale - 1);

			r = x * x + y * y + z * z;
		}
		return ((Math.sqrt(r)) * Math.pow(scale, (-i)) < d);
	}
}
