package com.erroll.math.fractal;

public class Mandelbulb implements FractalInterface {

	@Override
	public boolean isInFractal(double x, double y, double z, double d) {

		double posX = x;
		double posY = y;
		double posZ = z;

		double dr = 1.0;
		double r = 0.0;

		for (int i = 0; i < 10; i++) {
			r = Math.sqrt(x * x + y * y + z * z);
			if (r > 10000)
				break;

			// convert to polar coordinates
			double theta = Math.acos(z / r);
			double phi = Math.atan2(y, x);
			dr = Math.pow(r, 8 - 1.0) * 8 * dr + 1.0;

			// scale and rotate the point
			double zr = Math.pow(r, 8);
			theta = theta * 8;
			phi = phi * 8;

			// convert back to cartesian coordinates
			x = zr * Math.sin(theta) * Math.cos(phi);
			y = zr * Math.sin(phi) * Math.sin(theta);
			z = zr * Math.cos(theta);

			x += posX;
			y += posY;
			z += posZ;
		}
		return ((0.5 * Math.log(r) * r / dr) < d);
	}
}
