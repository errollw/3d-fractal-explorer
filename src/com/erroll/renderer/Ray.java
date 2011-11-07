package com.erroll.renderer;

import javax.vecmath.Vector3d;

public class Ray implements RayInterface {
	private Vector3d start;
	private Vector3d dir;

	/**
	 * Creates a ray starting at the point start and having the direction dirParam. The ray can be described as P(t) = A + t * D where A is the starting point
	 * and D is the direction vector of the ray
	 * 
	 * @param startParam
	 *            The starting point of the ray (A)
	 * @param dirParam
	 *            The direction of the ray (D)
	 */
	public Ray(Vector3d startParam, Vector3d dirParam) {
		// constructs fields using parameters passed
		start = new Vector3d(startParam);
		dir = new Vector3d(dirParam);

		// normalize direction vector
		dir.normalize();
	}

	@Override
	public void printInfo() {
		System.out.printf("start:\t(%.2f, %.2f, %.2f)" + "\t" + "dir:\t(%.2f, %.2f, %.2f)\n", start.x, start.y, start.z, dir.x, dir.y, dir.z);
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	@Override
	public Vector3d getDir() {
		return dir;
	}

	@Override
	public void setDir(Vector3d dir) {
		this.dir = dir;
		dir.normalize();
	}

	@Override
	public Vector3d getStart() {
		return start;
	}

	@Override
	public void setStart(Vector3d start) {
		this.start = start;
	}
}
