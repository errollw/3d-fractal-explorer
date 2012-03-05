package com.erroll.camera.paths;

import com.erroll.camera.CameraInterface;

public interface SegmentInterface {

	/**
	 * Update the CameraInterface c for time t (between 0 and maximum time for that segment)
	 * 
	 * @param c
	 *            The CameraInterface to be updated
	 * @param t
	 *            The time passed
	 */
	void update(CameraInterface c, int t);

	/**
	 * @return The end time for this segment
	 */
	int getTMax();

}
