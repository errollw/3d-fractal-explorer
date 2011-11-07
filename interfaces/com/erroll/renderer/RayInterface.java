package com.erroll.renderer;

import javax.vecmath.Vector3d;

public interface RayInterface {
	
	/**
	 * @return The normalized direction vector of the Ray
	 */
	public Vector3d getDir();
	
	/**
	 * Sets the normalized direction vector of the Ray
	 * 
	 * @param dir The new direction vector
	 */
	public void setDir(Vector3d dir);
	
	/**
	 * @return The start position in space of the Ray
	 */
	public Vector3d getStart();
	
	/**
	 * Sets the start position in space of the Ray
	 * 
	 * @param start The new start position
	 */
	public void setStart(Vector3d start);
	
	/**
	 * Prints formatted information about the Ray implementation to the console
	 */
	public void printInfo();
}
