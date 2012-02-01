package com.erroll.renderer;

import javax.vecmath.Vector3d;

public class Ray {
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

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	/**
	 * @return The normalized direction vector of the Ray
	 */
	public Vector3d getDir() {
		return dir;
	}

	/**
	 * Sets the start position in space of the Ray
	 * 
	 * @param start
	 *            The new start position
	 */
	public Vector3d getStart() {
		return start;
	}
}
