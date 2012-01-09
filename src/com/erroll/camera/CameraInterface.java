package com.erroll.camera;

import javax.vecmath.Vector3d;

import com.erroll.math.Axis;

public interface CameraInterface {

	/**
	 * Initializes fields of the Camera object with the parameters passed to it
	 * 
	 * @param positionParam
	 *            The position of the camera
	 * @param lookpointParam
	 *            The position of the object the camera is looking at
	 * @param distanceToViewplaneParam
	 *            The distance from the camera to its viewplane
	 * @param viewplaneWidth
	 *            The width of the viewplane
	 * @param viewplaneHeight
	 *            The height of the viewplane
	 */
	void initialise(Vector3d positionParam, Vector3d lookpointParam, double distanceToViewplaneParam, double viewplaneWidth, double viewplaneHeight);

	/**
	 * Returns the vector from the camera to that pixel's position on the viewplane
	 * 
	 * @param x
	 *            The position of the pixel from the left of the screen
	 * @param y
	 *            The position of the pixel from the top of the screen
	 * @param resolutionX
	 *            The resolution of the output horizontally across the screen
	 * @param resolutionY
	 *            The resolution of the output vertical down the screen
	 * @return The vector in 3D space from the camera's position to that pixel's position
	 */
	Vector3d getVectorToPixel(double x, double y, double resolutionX, double resolutionY);

	/**
	 * Returns the position in 3D space of the pixel's coordinates requested
	 * 
	 * @param x
	 *            The position of the pixel from the left of the screen
	 * @param y
	 *            The position of the pixel from the top of the screen
	 * @param resolutionX
	 *            The resolution of the output horizontally across the screen
	 * @param resolutionY
	 *            The resolution of the output vertical down the screen
	 * @return the position in 3D space of that pixel on the viewplane
	 */
	Vector3d getPositionOfPixel(double x, double y, double resolutionX, double resolutionY);

	/**
	 * @param newPosition
	 *            The new position in space to move the camera's position (shifting the lookPoint)
	 */
	void moveCamera(Vector3d newPosition);

	/**
	 * @param offset
	 *            The vector offset by which to move the camera's position and lookPoint
	 */
	void moveCameraBy(Vector3d offset);

	/**
	 * Rotates the camera's position and lookPoint counter-clockwise around a point in space, modifying vectors and viewplanes.
	 * 
	 * @param rotateOrigin
	 *            The point in space about which to rotate the camera
	 * @param axis
	 *            The axis about which to rotate the camera
	 * @param angle
	 *            The angle by which to rotate the camera
	 */
	void rotateAround(Vector3d rotateOrigin, Axis axis, double angle);

	/**
	 * Rotates the camera's position and lookPoint counter-clockwise around a point in space, modifying vectors and viewplanes.
	 * 
	 * @param rotateOrigin
	 *            The point in space about which to rotate the camera
	 * @param axis
	 *            The arbitrary axis about which to rotate the camera
	 * @param angle
	 *            The angle by which to rotate the camera
	 */
	void rotateAround(Vector3d rotateOrigin, Vector3d axis, double angle);

	/**
	 * Rotates the camera's position and lookPoint counter-clockwise around the origin, modifying vectors and viewplanes.
	 * 
	 * @param axis
	 *            The axis about which to rotate it
	 * @param angle
	 *            The angle by which to rotate the camera
	 */
	void rotateAroundOrigin(Axis axis, double angle);

	/**
	 * Rotates the camera's position and lookPoint counter-clockwise around and arbitrary axis, modifying vectors and viewplanes.
	 * 
	 * @param axis
	 *            The arbitrary axis about which to rotate it
	 * @param angle
	 *            The angle by which to rotate the camera
	 */
	void rotateAroundOrigin(Vector3d axis, double angle);

	/**
	 * Rotates the camera's vectors and viewplanes counter-clockwise about it's position
	 * 
	 * @param axis
	 *            The axis about which to rotate it
	 * @param angle
	 *            The angle by which to rotate the camera
	 */
	void rotate(Axis axis, double angle);

	/**
	 * Rotates the camera's vectors and viewplanes counter-clockwise about it's position
	 * 
	 * @param axis
	 *            The arbitrary axis about which to rotate it
	 * @param angle
	 *            The angle by which to rotate the camera
	 */
	void rotate(Vector3d axis, double angle);

	/**
	 * Prints formatted information about the Camera implementation to the console
	 */
	void printInfo();

	/**
	 * @return The position of the camera in space
	 */
	Vector3d getPosition();

	/**
	 * @return The point in space the camera is pointing at
	 */
	Vector3d getLookPoint();

	/**
	 * @return A normalized vector from the camera's position to where it is pointing
	 */
	Vector3d getLookVector();

	/**
	 * @return A normalized vector in the up direction for the camera
	 */
	Vector3d getUpVector();

	/**
	 * @return The distance from the camera's position to its viewplane
	 */
	double getDistanceToViewplane();
	
	/**
	 * This modifies how far the camera is from the viewplane; changes the field-of-view
	 */
	void setDistanceToViewplane(double distanceToViewplaneParam);

	/**
	 * @return The vector describing the top of the viewplane (from top left)
	 */
	Vector3d getViewplaneTop();

	/**
	 * sets the length of the top side of the viewplane
	 */
	void setViewplaneTopLength(double viewplaneHeight);

	/**
	 * @return The vector describing the side of the viewplane (from top left)
	 */
	Vector3d getViewplaneLeft();

	/**
	 * sets the length of the left side of the viewplane
	 */
	void setViewplaneLeftLength(double viewplaneWidth);
}
