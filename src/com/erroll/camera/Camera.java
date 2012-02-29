package com.erroll.camera;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.erroll.math.Axis;

public class Camera implements CameraInterface {

	// positions of the camera at the point on viewplane that it's looking at. Only 2 actual 3D coordinates that
	// describe the camera.
	private Vector3d position;
	private Vector3d lookPoint;

	// normalized vectors pointing in the direction the camera is looking in and directly "upwards" from the camera
	private Vector3d lookVector;
	private Vector3d upVector;

	// the distance from the camera to the viewplane
	private double distanceToViewplane;

	// from camera's point of view, the viewplane is defined by these vectors
	private Vector3d viewplaneTop;
	private Vector3d viewplaneLeft;

	/**
	 * Creates a new Camera object that is a copy of the camera object passed to it. This is used when double buffering to keep the camera being used by the
	 * renderer steady even though the scene camera may have been moved during the time taken to render a frame
	 * 
	 * @param cameraToCopy
	 *            The camera object to be copied.
	 */
	public Camera(CameraInterface cameraToCopy) {
		// construct fields from copied CameraInterface object
		position = cameraToCopy.getPosition();
		lookPoint = cameraToCopy.getLookPoint();
		lookVector = cameraToCopy.getLookVector();
		upVector = cameraToCopy.getUpVector();
		distanceToViewplane = cameraToCopy.getDistanceToViewplane();
		viewplaneTop = cameraToCopy.getViewplaneTop();
		viewplaneLeft = cameraToCopy.getViewplaneLeft();
	}

	/**
	 * Creates a new Camera object pointing at the origin, 5 units away from it. Up vector is along y axis.
	 */
	public Camera() {
		// construct fields
		position = new Vector3d();
		lookPoint = new Vector3d();
		lookVector = new Vector3d();
		upVector = new Vector3d();
		distanceToViewplane = 0;
		viewplaneTop = new Vector3d();
		viewplaneLeft = new Vector3d();

		initialise(new Vector3d(0.0d, 0.0d, 5.0d), new Vector3d(0.0d, 0.0d, 0.0d), 3d, 1.5d, 1.5d);
	}

	@Override
	public void initialise(Vector3d positionParam, Vector3d lookpointParam, double distanceToViewplaneParam, double viewplaneWidth, double viewplaneHeight) {

		// sets position of camera in 3D space and points it in the required direction
		position = positionParam;
		lookPoint = lookpointParam;

		// generates lookVector and initializes basic y-axis upVector
		lookVector.sub(lookPoint, position);
		lookVector.normalize();
		upVector = new Vector3d(0.0d, 1.0d, 0.0d);

		// sets the distanceToViewplane to be used later
		distanceToViewplane = distanceToViewplaneParam;

		// viewplaneLeft = -upVector * viewplaneHeight
		viewplaneLeft.scale(viewplaneHeight, upVector);
		viewplaneLeft.negate();

		// viewplaneTop = upVector X lookVector * viewplaneWidth
		viewplaneTop.cross(upVector, lookVector);
		viewplaneTop.negate();
		viewplaneTop.scale(viewplaneWidth);
	}

	@Override
	public void moveCamera(Vector3d newPosition) {
		// find offset and use moveCameraBy
		Vector3d offset = new Vector3d();
		offset.sub(newPosition, position);
		moveCameraBy(offset);
	}

	@Override
	public void moveCameraBy(Vector3d offset) {
		// only need to move the position and lookPoint and everything else is relative
		position.add(offset);
		lookPoint.add(offset);
	}

	@Override
	public void rotate(Axis axis, double angle) {
		// use rotateAround with the camera position
		rotateAround(new Vector3d(position), axis, angle);
	}

	@Override
	public void rotate(Vector3d axis, double angle) {
		// use rotateAround with the camera position (using arbitrary axis)
		rotateAround(new Vector3d(position), axis, angle);
	}

	@Override
	public void rotateAround(Vector3d rotateOrigin, Axis axis, double angle) {

		// move position and lookPoint by offset so can be rotated about origin
		position.sub(rotateOrigin);
		lookPoint.sub(rotateOrigin);

		// now rotate around the origin
		rotateAroundOrigin(axis, angle);

		// return position and lookPoints to their correct offsets
		position.add(rotateOrigin);
		lookPoint.add(rotateOrigin);
	}

	@Override
	public void rotateAround(Vector3d rotateOrigin, Vector3d axis, double angle) {

		// move position and lookPoint by offset so can be rotated about origin
		position.sub(rotateOrigin);
		lookPoint.sub(rotateOrigin);

		// now rotate around the origin (using arbitrary axis)
		rotateAroundOrigin(axis, angle);

		// return position and lookPoints to their correct offsets
		position.add(rotateOrigin);
		lookPoint.add(rotateOrigin);
	}

	@Override
	public void rotateAroundOrigin(Axis axis, double angle) {

		// create a rotation matrix for the angle about the correct axis
		Matrix3d rotMatrix = new Matrix3d();
		switch (axis) {
		case X:
			rotMatrix.rotX(angle);
			break;
		case Y:
			rotMatrix.rotY(angle);
			break;
		case Z:
			rotMatrix.rotZ(angle);
			break;
		}

		// apply transformations to fields
		rotMatrix.transform(position);
		rotMatrix.transform(lookPoint);
		rotMatrix.transform(lookVector);
		rotMatrix.transform(upVector);
		rotMatrix.transform(viewplaneLeft);
		rotMatrix.transform(viewplaneTop);
	}

	@Override
	public void rotateAroundOrigin(Vector3d axis, double angle) {

		// create rotation matrix for arbitrary axis
		AxisAngle4d a = new AxisAngle4d(axis, angle);
		Matrix3d m = new Matrix3d();
		m.set(a);

		// apply transformations to fields
		m.transform(position);
		m.transform(lookPoint);
		m.transform(lookVector);
		m.transform(upVector);
		m.transform(viewplaneLeft);
		m.transform(viewplaneTop);
	}

	@Override
	public Vector3d getVectorToPixel(double x, double y, double resolutionX, double resolutionY) {

		// get position of pixel on screen and find normalized vector to it from camera position
		Vector3d pixelPosition = getPositionOfPixel(x, y, resolutionX, resolutionY);
		Vector3d vectorToPixel = new Vector3d();
		vectorToPixel.sub(pixelPosition, position);
		vectorToPixel.normalize();

		return vectorToPixel;
	}

	private Vector3d getPositionOfPixel(double x, double y, double resolutionX, double resolutionY) {

		// set the return value to the top left of the viewplane on origin
		Vector3d pixelPosition = new Vector3d();
		pixelPosition.sub(viewplaneTop);
		pixelPosition.sub(viewplaneLeft);
		pixelPosition.scale(0.5d);

		// move return value onto actual top left of viewplane from camera
		pixelPosition.add(position);
		Vector3d offset = new Vector3d();
		offset.scale(distanceToViewplane, lookVector);
		pixelPosition.add(offset);

		// get the distance from top left of the viewplane to the pixel in x and y and add it
		Vector3d scaledX = new Vector3d();
		Vector3d scaledY = new Vector3d();
		scaledX.scale((x / resolutionX), viewplaneTop);
		scaledY.scale((y / resolutionY), viewplaneLeft);
		pixelPosition.add(scaledX);
		pixelPosition.add(scaledY);

		return pixelPosition;
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	@Override
	public Vector3d getPosition() {
		return new Vector3d(position);
	}

	@Override
	public Vector3d getLookPoint() {
		return new Vector3d(lookPoint);
	}

	@Override
	public Vector3d getLookVector() {
		return new Vector3d(lookVector);
	}

	@Override
	public Vector3d getUpVector() {
		return new Vector3d(upVector);
	}

	@Override
	public double getDistanceToViewplane() {
		return distanceToViewplane;
	}

	@Override
	public void setDistanceToViewplane(double distanceToViewplaneParam) {
		distanceToViewplane = distanceToViewplaneParam;
	}

	@Override
	public Vector3d getViewplaneTop() {
		return new Vector3d(viewplaneTop);
	}

	@Override
	public void setViewplaneTopLength(double viewplaneWidth) {
		// viewplaneTop = upVector X lookVector * viewplaneWidth
		viewplaneTop.cross(upVector, lookVector);
		viewplaneTop.negate();
		viewplaneTop.scale(viewplaneWidth);
	}

	@Override
	public Vector3d getViewplaneLeft() {
		return new Vector3d(viewplaneLeft);
	}

	@Override
	public void setViewplaneLeftLength(double viewplaneHeight) {
		// viewplaneLeft = -upVector * viewplaneHeight
		viewplaneLeft.scale(viewplaneHeight, upVector);
		viewplaneLeft.negate();
	}
}
