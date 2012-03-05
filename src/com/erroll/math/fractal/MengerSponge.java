package com.erroll.math.fractal;

public class MengerSponge implements FractalInterface {

	@Override
	public boolean isInFractal(double x, double y, double z, double d) {

		double r = x * x + y * y + z * z;
		double scale = 3d;
		double MI = 10000;
		int i = 0;

		for (i = 0; i < MI && r < 9d; i++) {

			x = Math.abs(x);
			y = Math.abs(y);
			z = Math.abs(z);

			if (x - y < 0d) {
				double x1 = y;
				y = x;
				x = x1;
			}
			if (x - z < 0d) {
				double x1 = z;
				z = x;
				x = x1;
			}
			if (y - z < 0d) {
				double y1 = z;
				z = y;
				y = y1;
			}

			x = scale * x - 1d * (scale - 1d);
			y = scale * y - 1d * (scale - 1d);
			z = scale * z;

			if (z > 0.5d * 1d * (scale - 1d))
				z -= 1d * (scale - 1d);

			r = x * x + y * y + z * z;
		}
		return ((Math.sqrt(r)) * Math.pow(scale, (-i)) < d);
	}
}
