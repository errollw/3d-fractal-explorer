package com.erroll.math.fractal;

public interface FractalInterface {

	/**
	 * Checks if a point in space has fractal detail within distance d from it
	 * 
	 * @param x
	 *            The x coordinate to be checked (between -1 and 1)
	 * @param y
	 *            The y coordinate to be checked (between -1 and 1)
	 * @param z
	 *            The z coordinate to be checked (between -1 and 1)
	 * @param d
	 *            The maximum distance allowed from this point to contain a fractal
	 * @return True if the coordinate has fractal detail, all of which is between -1,-1,-1 and 1,1,1, at least distance d away from it, or false otherwise
	 */
	public boolean isInFractal(double x, double y, double z, double d);

}
