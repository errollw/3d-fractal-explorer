package com.erroll.renderer;

public interface RayCasterBeamOptInterface extends RayCasterInterface {

	/**
	 * Finds the color to be drawn on the screen for a particular ray intersecting the scene starting with minimum tmin distance value we know that ray will
	 * travel before it can hit anything
	 * 
	 * @param ray
	 *            The ray being shot through the scene that will be marched over
	 * @param tmin
	 *            The starting tmin value to determine the starting point of intersection
	 * @return The int color of that Octree that the ray intersects, or black if it misses
	 */
	public int rayCast(RayInterface ray, double tmin);

	/**
	 * Shoots a ray to make a coarse image so minimum tmin values of rays between the coarse rays can be determined
	 * 
	 * @param ray
	 *            The ray being shot through the scene that will be marched over - it will return early as it will run at a lower resolution
	 * @return the tmin value for the ray being shot through the scene having terminated on a voxel; or DOUBLE_MAX if the ray misses the octree
	 */
	public double coarseRayCast(RayInterface ray);

	/**
	 * @return the square root of the number of rays to be shot in each coarse ray packet
	 */
	public int getPacketSize();

	/**
	 * Sets the square root of the number of rays to be shot in each coarse ray packet
	 */
	public void setPacketSize(int packetSize);
}
