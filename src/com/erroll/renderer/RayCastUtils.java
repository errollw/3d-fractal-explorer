package com.erroll.renderer;

import javax.vecmath.Vector3d;

public class RayCastUtils {

	/**
	 * Helper function to return a vector of either 1s or 0s depending on whether each component of a is greater than or equal to b's relative component
	 * 
	 * @param a
	 *            The potentially larger vector
	 * @param b
	 *            The potentially smaller vector
	 * @return (1 if a.x > b.x or 0 otherwise, similar, similar)
	 */
	static Vector3d step(Vector3d a, Vector3d b) {
		Vector3d result = new Vector3d((a.x >= b.x) ? 1 : 0, (a.y >= b.y) ? 1 : 0, (a.z >= b.z) ? 1 : 0);
		return result;
	}

	/**
	 * This function clips a ray against an axis aligned bounding box to check if they intersect. It returns true if they do and false otherwise.
	 * 
	 * @param ray
	 *            The ray to be clipped against the bounding box
	 * @param a
	 *            The start position in space of the bounding box
	 * @param dim
	 *            The dimensions of the box extending from this start position to (a.x + dim, a.y + dim, a.z + dim)
	 * @return true if the ray passes through the box starting at position a with sides of length dim, false otherwise
	 */
	static boolean boxClip(RayInterface ray, Vector3d a, double dim) {
		Vector3d pVec = ray.getDir();
		Vector3d cPos = ray.getStart();

		double tx0 = (a.x - cPos.x) / pVec.x;
		double tx1 = ((a.x + dim) - cPos.x) / pVec.x;
		double ty0 = (a.y - cPos.y) / pVec.y;
		double ty1 = ((a.y + dim) - cPos.y) / pVec.y;
		double tz0 = (a.z - cPos.z) / pVec.z;
		double tz1 = ((a.z + dim) - cPos.z) / pVec.z;

		if (tx1 < tx0) {
			double temp = tx0;
			tx0 = tx1;
			tx1 = temp;
		}
		if (ty1 < ty0) {
			double temp = ty0;
			ty0 = ty1;
			ty1 = temp;
		}
		if (tz1 < tz0) {
			double temp = tz0;
			tz0 = tz1;
			tz1 = temp;
		}

		double tmin = Math.max(tx0, Math.max(ty0, tz0));
		double tmax = Math.min(tx1, Math.min(ty1, tz1));

		if (tmin < tmax)
			return true;
		else
			return false;
	}

	/**
	 * Returns the minimum t value the ray can have to clip with the bounding box for ray P(t) = ray.start + t*ray.direction (assuming the ray clips the box)
	 * 
	 * @param ray
	 *            The ray to be clipped against the bounding box
	 * @param a
	 *            The start position in space of the bounding box
	 * @param dim
	 *            The dimensions of the box extending from this start position to (a.x + dim, a.y + dim, a.z + dim)
	 * @return the minimum t parameter
	 */
	static double boxClip_tmin(RayInterface ray, Vector3d a, double dim) {
		Vector3d pVec = ray.getDir();
		Vector3d cPos = ray.getStart();

		double tx0 = (a.x - cPos.x) / pVec.x;
		double tx1 = ((a.x + dim) - cPos.x) / pVec.x;
		double ty0 = (a.y - cPos.y) / pVec.y;
		double ty1 = ((a.y + dim) - cPos.y) / pVec.y;
		double tz0 = (a.z - cPos.z) / pVec.z;
		double tz1 = ((a.z + dim) - cPos.z) / pVec.z;

		double tmin = Math.max(Math.min(tx0, tx1), Math.max(Math.min(ty0, ty1), Math.min(tz0, tz1)));
		return tmin;
	}

	/**
	 * Returns the maximum t value the ray can have to clip with the bounding box for ray P(t) = ray.start + t*ray.direction (assuming the ray clips the box)
	 * 
	 * @param ray
	 *            The ray to be clipped against the bounding box
	 * @param a
	 *            The start position in space of the bounding box
	 * @param dim
	 *            The dimensions of the box extending from this start position to (a.x + dim, a.y + dim, a.z + dim)
	 * @return the maximum t parameter
	 */
	static double boxClip_tmax(RayInterface ray, Vector3d a, double dim) {
		Vector3d pVec = ray.getDir();
		Vector3d cPos = ray.getStart();

		double tx0 = (a.x - cPos.x) / pVec.x;
		double tx1 = ((a.x + dim) - cPos.x) / pVec.x;
		double ty0 = (a.y - cPos.y) / pVec.y;
		double ty1 = ((a.y + dim) - cPos.y) / pVec.y;
		double tz0 = (a.z - cPos.z) / pVec.z;
		double tz1 = ((a.z + dim) - cPos.z) / pVec.z;

		if (tx1 < tx0) {
			double temp = tx0;
			tx0 = tx1;
			tx1 = temp;
		}
		if (ty1 < ty0) {
			double temp = ty0;
			ty0 = ty1;
			ty1 = temp;
		}
		if (tz1 < tz0) {
			double temp = tz0;
			tz0 = tz1;
			tz1 = temp;
		}

		double tmax = Math.min(tx1, Math.min(ty1, tz1));
		return tmax;
	}

	/**
	 * Finds the direction of the the next neighboring bounding box the ray would pass through once it exits the current bounding box
	 * 
	 * @param ray
	 *            The ray that passes through the bounding box
	 * @param a
	 *            The start position in space of the bounding box
	 * @param dim
	 *            The dimensions of the box extending from this start position to (a.x + dim, a.y + dim, a.z + dim)
	 * @return A vector (x,y,z) with y = 1 if the neighbor is above, -1 if it is below and 0 if it will have the same y coordinate. Similar for other axes.
	 */
	static Vector3d findNeighbour(RayInterface ray, Vector3d a, double dim) {
		Vector3d pVec = ray.getDir();
		Vector3d cPos = ray.getStart();

		double tx0 = (a.x - cPos.x) / pVec.x;
		double tx1 = ((a.x + dim) - cPos.x) / pVec.x;
		double ty0 = (a.y - cPos.y) / pVec.y;
		double ty1 = ((a.y + dim) - cPos.y) / pVec.y;
		double tz0 = (a.z - cPos.z) / pVec.z;
		double tz1 = ((a.z + dim) - cPos.z) / pVec.z;

		if (tx1 < tx0) {
			double temp = tx0;
			tx0 = tx1;
			tx1 = temp;
		}
		if (ty1 < ty0) {
			double temp = ty0;
			ty0 = ty1;
			ty1 = temp;
		}
		if (tz1 < tz0) {
			double temp = tz0;
			tz0 = tz1;
			tz1 = temp;
		}

		double tmax = Math.min(tx1, Math.min(ty1, tz1));

		if (tmax == tx1)
			return new Vector3d(ray.getDir().x / Math.abs(ray.getDir().x), 0, 0);
		if (tmax == ty1)
			return new Vector3d(0, ray.getDir().y / Math.abs(ray.getDir().y), 0);
		if (tmax == tz1)
			return new Vector3d(0, 0, ray.getDir().z / Math.abs(ray.getDir().z));
		else
			return new Vector3d();
	}
}
