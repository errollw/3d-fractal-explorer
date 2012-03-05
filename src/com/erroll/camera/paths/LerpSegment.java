package com.erroll.camera.paths;

import javax.vecmath.Vector3d;

import com.erroll.camera.CameraInterface;

public class LerpSegment implements SegmentInterface {

	// the change in camera position per frame
	private Vector3d dPos;

	// the end position of the camera
	private Vector3d endPos;

	// the start and end look directions of the camera
	private Vector3d startLookVector;
	private Vector3d endLookVector;

	// total time to be taken for transform
	private int tMax;

	/**
	 * Create a linear interpolation segment without a specified start point, start look vector, or end look vector. Starting positions are retrieved on first
	 * update. Look vector does not change during this interpolation.
	 * 
	 * @param tMax
	 *            The time in frames allowed for this sequence
	 * @param endPos
	 *            The position for the camera to be in at the end
	 */
	public LerpSegment(int tMax, Vector3d endPos) {
		// set the change in position
		this.endPos = endPos;

		// set total time for transform
		this.tMax = tMax;
	}

	/**
	 * Create a linear interpolation segment without a specified start point or start look vector. Starting positions are retrieved on first update and the look
	 * vector will change per frame also.
	 * 
	 * @param tMax
	 *            The time in frames allowed for this sequence
	 * @param endPos
	 *            The position for the camera to be in at the end
	 * @param endLookVector
	 *            The direction the camera should be looking at the end
	 */
	public LerpSegment(int tMax, Vector3d endPos, Vector3d endLookVector) {
		// set the change in position
		this.endPos = endPos;

		// set total time for transform
		this.tMax = tMax;

		// set final lookVector
		this.endLookVector = endLookVector;
		endLookVector.normalize();
	}

	@Override
	public void update(CameraInterface c, int t) {

		// initialize on first run with camera position and direction information
		if (t == 0) {
			// set the change in position
			dPos = new Vector3d();
			dPos.sub(endPos, c.getPosition());
			dPos.scale(1d / tMax);

			// set look vectors if needed
			if (endLookVector != null)
				startLookVector = new Vector3d(c.getLookVector());
		}

		// move camera
		c.moveCameraBy(dPos);

		// adjust look vectors if needed
		if (endLookVector != null) {
			double d = ((1d + t) / tMax);
			Vector3d frameLookVector = new Vector3d();
			frameLookVector.scaleAdd(1d - d, startLookVector, new Vector3d());
			frameLookVector.scaleAdd(d, endLookVector, frameLookVector);
			c.setLookVector(frameLookVector);
		}
	}

	@Override
	public int getTMax() {
		return tMax;
	}

}
