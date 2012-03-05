package com.erroll.camera.paths;

import javax.vecmath.Vector3d;

import com.erroll.camera.CameraInterface;
import com.erroll.math.Axis;

public class CircleSegment implements SegmentInterface {

	// the change in angle and radius per unit time
	private double dAngle;
	private double dRadius;

	// the axis to rotate around
	private Axis axis;

	// the origin of the rotation
	private Vector3d origin;

	// start and end time values
	private int tMax;

	public CircleSegment(int tMax, double circlePercentage, double changeInRadius, Axis axis, Vector3d origin) {
		// set change in angle per unit time
		dAngle = Math.PI * 2d * circlePercentage / tMax;
		dRadius = changeInRadius / tMax;

		// set segment specific properties
		this.axis = axis;
		this.origin = origin;
		this.tMax = tMax;
	}

	@Override
	public void update(CameraInterface c, int t) {
		// rotate
		c.rotateAround(origin, axis, dAngle);

		// change radius
		Vector3d offset = new Vector3d(c.getLookVector());
		offset.scale(dRadius);
		c.moveCameraBy(offset);
	}

	@Override
	public int getTMax() {
		return tMax;
	}

}
