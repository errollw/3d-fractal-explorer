package com.erroll.math.fractal;

public class Mandelbox implements FractalInterface {

	@Override
	public boolean isInFractal(double x, double y, double z, double d) {

		int s = 7;
		x *= s;
		y *= s;
		z *= s;
		d *= s;

		double posX = x;
		double posY = y;
		double posZ = z;

		Double dr = 1.0;
		double r = 0.0;

		double scale = 2d;

		double minRadius2 = 0.25;
		double fixedRadius2 = 1;

		for (int n = 0; n < 50; n++) {
			// Reflect
			if (x > 1.0)
				x = 2.0 - x;
			else if (x < -1.0)
				x = -2.0 - x;
			if (y > 1.0)
				y = 2.0 - y;
			else if (y < -1.0)
				y = -2.0 - y;
			if (z > 1.0)
				z = 2.0 - z;
			else if (z < -1.0)
				z = -2.0 - z;

			// Sphere Inversion
			double r2 = x * x + y * y + z * z;

			if (r2 < minRadius2) {
				x = x * fixedRadius2 / minRadius2;
				y = y * fixedRadius2 / minRadius2;
				z = z * fixedRadius2 / minRadius2;
				dr = dr * fixedRadius2 / minRadius2;
			} else if (r2 < fixedRadius2) {
				x = x * fixedRadius2 / r2;
				y = y * fixedRadius2 / r2;
				z = z * fixedRadius2 / r2;
				fixedRadius2 *= fixedRadius2 / r2;
			}

			x = x * scale + posX;
			y = y * scale + posY;
			z = z * scale + posZ;
			dr *= scale;
		}
		r = Math.sqrt(x * x + y * y + z * z);
		return (r / Math.abs(dr)) < d;
	}
}
