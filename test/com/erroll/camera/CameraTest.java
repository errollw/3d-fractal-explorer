package com.erroll.camera;

import static org.junit.Assert.assertTrue;

import javax.vecmath.Vector3d;

import org.junit.Before;
import org.junit.Test;

import com.erroll.TestUtils;
import com.erroll.math.Axis;

public class CameraTest {

	//the object to be tested on
	private Camera camera;

	// prepare starting parameters to test with
	private final Vector3d startPosition = new Vector3d(0d, 0d, 5d);
	private final Vector3d startLookPoint = new Vector3d(0d, 0d, 0d);
	private final double startDistanceToViewplane = 3d;
	private final double startViewplaneWidth = 3d;
	private final double startViewplaneHeight = 3d;

	// list of vector offsets to use in some test cases
	private final Vector3d[] offsets = { new Vector3d(0d, 0d, 0d), new Vector3d(1d, 0d, 0d), new Vector3d(0d, 1d, 0d), new Vector3d(0d, 0d, 1d),
			new Vector3d(-1d, -1d, -1d), new Vector3d(100d, -200d, 300d) };

	@Before
	public void setUp() {
		// make testing camera object
		camera = new Camera();
		camera.initialise(startPosition, startLookPoint, startDistanceToViewplane, startViewplaneWidth, startViewplaneHeight);
	}

	@Test
	public void testInitialise() {
		// prepare parameters to test with
		Vector3d position = new Vector3d(0d, 0d, 1d);
		Vector3d lookPoint = new Vector3d(-1d, 0d, 0d);
		double distanceToViewplane = 1d;
		double viewplaneWidth = 1d;
		double viewplaneHeight = 1d;

		// re-initialize camera object with different parameters
		camera.initialise(position, lookPoint, distanceToViewplane, viewplaneWidth, viewplaneHeight);

		// assert position and lookPoint are correct
		assertTrue("position not correctly set", camera.getPosition().epsilonEquals(position, TestUtils.EPSILON));
		assertTrue("lookPoint not correctly set", camera.getLookPoint().epsilonEquals(lookPoint, TestUtils.EPSILON));

		// assert vectors point in correct direction (create local lookVector for testing)
		Vector3d lookVector = new Vector3d();
		lookVector.sub(lookPoint, position);
		lookVector.normalize();
		assertTrue("lookVector not pointing to lookPoint from position", camera.getLookVector().epsilonEquals(lookVector, TestUtils.EPSILON));
		assertTrue("upVector not pointing upwards in y direction", camera.getUpVector().epsilonEquals(new Vector3d(0d, 1d, 0d), TestUtils.EPSILON));

		// assert distances are correct
		assertTrue("lookVector not normalized", TestUtils.equals(camera.getLookVector().length(), 1d));
		assertTrue("upVector not normalized", TestUtils.equals(camera.getUpVector().length(), 1d));
		assertTrue("distanceToViewplane not correctly set", TestUtils.equals(camera.getDistanceToViewplane(), distanceToViewplane));
		assertTrue("viewplaneWidth not correctly set", TestUtils.equals(camera.getViewplaneTop().length(), viewplaneWidth));
		assertTrue("viewplaneHeight not correctly set", TestUtils.equals(camera.getViewplaneLeft().length(), viewplaneHeight));

		// assert correct vectors are perpendicular
		assertTrue("viewplaneTop and viewplaneLeft not perpendicular", TestUtils.equals(camera.getViewplaneTop().dot(camera.getViewplaneLeft()), 0d));
		assertTrue("viewplaneTop and lookVector not perpendicular", TestUtils.equals(camera.getViewplaneTop().dot(camera.getLookVector()), 0d));
		assertTrue("upVector and lookVector not perpendicular", TestUtils.equals(camera.getUpVector().dot(camera.getLookVector()), 0d));
	}

	@Test
	public void testMoveCamera() {

		// loop through each possible offset
		for (Vector3d offset : offsets) {

			// get old values to check new ones against later after move
			Vector3d oldLookVector = new Vector3d(camera.getLookVector());
			Vector3d oldUpVector = new Vector3d(camera.getUpVector());
			Vector3d oldViewPlaneLeft = new Vector3d(camera.getViewplaneLeft());
			Vector3d oldViewPlaneTop = new Vector3d(camera.getViewplaneTop());
			double oldDistanceToViewplane = camera.getDistanceToViewplane();

			// calculate new positions and lookPoints
			Vector3d newPosition = new Vector3d();
			Vector3d newLookPoint = new Vector3d();
			newPosition.add(camera.getPosition(), offset);
			newLookPoint.add(camera.getLookPoint(), offset);

			// move camera to new position and check camera and lookPoint have been moved correctly
			camera.moveCamera(newPosition);
			assertTrue("camera not moved to the newPosition", camera.getPosition().epsilonEquals(newPosition, TestUtils.EPSILON));
			assertTrue("lookPoint not moved by the correct offset", camera.getLookPoint().epsilonEquals(newLookPoint, TestUtils.EPSILON));

			// assert the camera's vectors and distances have not changed after the move
			assertTrue("lookVector changed during move", camera.getLookVector().epsilonEquals(oldLookVector, TestUtils.EPSILON));
			assertTrue("upVector changed during move", camera.getUpVector().epsilonEquals(oldUpVector, TestUtils.EPSILON));
			assertTrue("viewPlaneLeft changed during move", camera.getViewplaneLeft().epsilonEquals(oldViewPlaneLeft, TestUtils.EPSILON));
			assertTrue("viewPlaneTop changed during move", camera.getViewplaneTop().epsilonEquals(oldViewPlaneTop, TestUtils.EPSILON));
			assertTrue("distanceToViewplane changed during move", TestUtils.equals(camera.getDistanceToViewplane(), oldDistanceToViewplane));
		}
	}

	@Test
	public void testMoveCameraBy() {

		// loop through each possible offset
		for (Vector3d offset : offsets) {
			// re-set up the camera between each offset (is this OK practice?)
			setUp();

			// get old values to check new ones against later after move
			Vector3d oldLookVector = new Vector3d(camera.getLookVector());
			Vector3d oldUpVector = new Vector3d(camera.getUpVector());
			Vector3d oldViewPlaneLeft = new Vector3d(camera.getViewplaneLeft());
			Vector3d oldViewPlaneTop = new Vector3d(camera.getViewplaneTop());
			double oldDistanceToViewplane = camera.getDistanceToViewplane();

			// calculate new positions and lookPoints
			Vector3d newPosition = new Vector3d();
			Vector3d newLookPoint = new Vector3d();
			newPosition.add(camera.getPosition(), offset);
			newLookPoint.add(camera.getLookPoint(), offset);

			// move camera to new position and check camera and lookPoint have been moved correctly
			camera.moveCameraBy(offset);
			assertTrue("camera not moved by the correct offset", camera.getPosition().epsilonEquals(newPosition, TestUtils.EPSILON));
			assertTrue("lookPoint not moved by the correct offset", camera.getLookPoint().epsilonEquals(newLookPoint, TestUtils.EPSILON));

			// assert the camera's vectors and distances have not changed after the move
			assertTrue("lookVector changed during move", camera.getLookVector().epsilonEquals(oldLookVector, TestUtils.EPSILON));
			assertTrue("upVector changed during move", camera.getUpVector().epsilonEquals(oldUpVector, TestUtils.EPSILON));
			assertTrue("viewPlaneLeft changed during move", camera.getViewplaneLeft().epsilonEquals(oldViewPlaneLeft, TestUtils.EPSILON));
			assertTrue("viewPlaneTop changed during move", camera.getViewplaneTop().epsilonEquals(oldViewPlaneTop, TestUtils.EPSILON));
			assertTrue("distanceToViewplane changed during move", TestUtils.equals(camera.getDistanceToViewplane(), oldDistanceToViewplane));
		}
	}

	@Test
	public void testRotateAxisDouble() {

		// starting at (0,0,5) and pointing at (0,0,0)
		assertTrue("camera not in correct starting position", camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));
		assertTrue("camera not pointing in correct start direction", camera.getLookPoint().epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));

		// rotate camera lookPoint 90 degrees CCW around X axis, now pointing at (0,5,5)
		camera.rotate(Axis.X, Math.PI / 2);
		assertTrue("camera moved position during rotation around itself", camera.getPosition().epsilonEquals(startPosition, TestUtils.EPSILON));
		assertTrue("expected lookPoint: (0,5,5), got: " + camera.getLookPoint(),
				camera.getLookPoint().epsilonEquals(new Vector3d(0d, 5d, 5d), TestUtils.EPSILON));

		// rotate camera lookPoint 180 degrees CW around X axis, now pointing at (0,-5,5)
		camera.rotate(Axis.X, Math.PI);
		assertTrue("expected lookPoint: (0,-5,5), got: " + camera.getLookPoint(),
				camera.getLookPoint().epsilonEquals(new Vector3d(0d, -5d, 5d), TestUtils.EPSILON));

		// rotate camera lookPoint 90 degrees CCW around Z axis, now pointing at (5,0,5)
		camera.rotate(Axis.Z, Math.PI / 2);
		assertTrue("expected lookPoint: (5,0,5), got: " + camera.getLookPoint(),
				camera.getLookPoint().epsilonEquals(new Vector3d(5d, 0d, 5d), TestUtils.EPSILON));

		// rotate camera lookPoint 90 degrees CCW around Y axis, now pointing at (0,0,0) again
		camera.rotate(Axis.Y, Math.PI / 2);
		assertTrue("expected lookPoint: (0,0,0), got: " + camera.getLookPoint(),
				camera.getLookPoint().epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));
	}

	@Test
	public void testRotateVector3dDouble() {

		// starting at (0,0,5) and pointing at (0,0,0)
		assertTrue("camera not in correct starting position", camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));
		assertTrue("camera not pointing in correct start direction", camera.getLookPoint().epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));

		// rotate camera lookPoint 90 degrees CCW around vector (1,1,0), now pointing at (-5/2*sqrt(2),5/2*sqrt(2),5)
		camera.rotate(new Vector3d(1d, 1d, 0d), Math.PI / 2);
		assertTrue("camera moved position during rotation around itself", camera.getPosition().epsilonEquals(startPosition, TestUtils.EPSILON));
		assertTrue("expected: (-5/2*sqrt(2),5/2*sqrt(2),5), got: " + camera.getLookPoint(),
				camera.getLookPoint().epsilonEquals(new Vector3d(-5d * Math.sqrt(2d) / 2d, 5d * Math.sqrt(2d) / 2d, 5d), TestUtils.EPSILON));

		// rotate camera lookPoint 180 degrees CW around vector (1,1,0), now pointing at (5/2*sqrt(2),-5/2*sqrt(2),5)
		camera.rotate(new Vector3d(1d, 1d, 0d), Math.PI);
		assertTrue("expected: (5/2*sqrt(2),-5/2*sqrt(2),5), got: " + camera.getLookPoint(),
				camera.getLookPoint().epsilonEquals(new Vector3d(5d * Math.sqrt(2d) / 2d, -5d * Math.sqrt(2d) / 2d, 5d), TestUtils.EPSILON));

		// rotate camera lookPoint -90 degrees CCW around vector (-1,-1,0), now pointing at (0,0,0) again
		camera.rotate(new Vector3d(-1d, -1d, 0d), -Math.PI / 2);
		assertTrue("expected: (0,0,0), got: " + camera.getLookPoint(), camera.getLookPoint().epsilonEquals(new Vector3d(0, 0, 0), TestUtils.EPSILON));
	}

	@Test
	public void testRotateAroundVector3dAxisDouble() {

		// starting at (0,0,5) and pointing at (0,0,0)
		assertTrue("camera not in correct starting position", camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));
		assertTrue("camera not pointing in correct start direction", camera.getLookPoint().epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));

		// rotate the camera -90 degrees CCW round the z axis, should have no effect on positions but rotates the upVector to point in x direction
		camera.rotateAround(new Vector3d(0d, 0d, 2.5d), Axis.Z, -Math.PI / 2);
		assertTrue("camera moved position during rotation around starting Z axis", camera.getPosition().epsilonEquals(startPosition, TestUtils.EPSILON));
		assertTrue("lookPoint moved during rotation around starting Z axis", camera.getLookPoint().epsilonEquals(startLookPoint, TestUtils.EPSILON));
		assertTrue("upVector not been changed to (1,0,0)", camera.getUpVector().epsilonEquals(new Vector3d(1d, 0d, 0d), TestUtils.EPSILON));
		// check viewplane is also in the correct orientation
		assertTrue("viewplaneTop not correctly aligned with Y axis", camera.getViewplaneTop().epsilonEquals(new Vector3d(0d, -3d, 0d), TestUtils.EPSILON));
		assertTrue("viewplaneLeft not correctly aligned with X axis", camera.getViewplaneLeft().epsilonEquals(new Vector3d(-3d, 0d, 0d), TestUtils.EPSILON));

		// rotate the camera 180 degrees round the X axis onto the origin, expected camera position: (0,0,0)
		camera.rotateAround(new Vector3d(0d, 0d, 2.5d), Axis.X, Math.PI);
		assertTrue("camera not in correct new position at origin", camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));
		assertTrue("lookpoint not in correct new position", camera.getLookPoint().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));
		// viewplaneTop should now be negated but viewplaneLeft should experience no change
		assertTrue("viewplaneTop not correctly aligned with Y axis", camera.getViewplaneTop().epsilonEquals(new Vector3d(0d, 3d, 0d), TestUtils.EPSILON));
		assertTrue("viewplaneLeft changed during rotation about X axis", camera.getViewplaneLeft().epsilonEquals(new Vector3d(-3d, 0d, 0d), TestUtils.EPSILON));
	}

	@Test
	public void testRotateAroundVector3dVector3dDouble() {

		// starting at (0,0,5) and pointing at (0,0,0)
		assertTrue("camera not in correct starting position", camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));
		assertTrue("camera not pointing in correct start direction", camera.getLookPoint().epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));

		// rotate 180 degrees around point (1,0,0) with axis (-1,-1,-1)
		camera.rotateAround(new Vector3d(1d, 0d, 0d), new Vector3d(-1d, -1d, -1d), Math.PI);
		assertTrue(camera.getPosition().epsilonEquals(new Vector3d(14d / 3d, 8d / 3d, -7d / 3d), TestUtils.EPSILON));

		// rotate 180 degrees around point (1,0,0) with axis (1,1,1) back to (0,0,5)
		camera.rotateAround(new Vector3d(1d, 0d, 0d), new Vector3d(1d, 1d, 1d), Math.PI);
		assertTrue(camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));

		// rotate 90 degrees CCW around point (0,0,2.5) with axis (0,1,0): the y-axis
		camera.rotateAround(new Vector3d(0d, 0d, 2.5d), new Vector3d(0d, 1d, 0d), Math.PI / 2);
		assertTrue(camera.getPosition().epsilonEquals(new Vector3d(2.5d, 0d, 2.5d), TestUtils.EPSILON));

	}

	@Test
	public void testRotateAroundOriginAxisDouble() {

		// starting at (0,0,5) and pointing at (0,0,0)
		assertTrue("camera not in correct starting position", camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));
		assertTrue("camera not pointing in correct start direction", camera.getLookPoint().epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));

		// rotate 90 degrees CCW around x-axis to (0,-5,0)
		camera.rotateAroundOrigin(Axis.X, Math.PI / 2);
		assertTrue("incorrect camera position after X axis rotation", camera.getPosition().epsilonEquals(new Vector3d(0d, -5d, 0d), TestUtils.EPSILON));

		// rotate 90 degrees CCW around z-axis to (0,0,-5)
		camera.rotateAroundOrigin(Axis.X, Math.PI / 2);
		assertTrue("incorrect camera position after Z axis rotation", camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, -5d), TestUtils.EPSILON));

		// rotate -90 degrees CCW around y-axis to (5,0,0)
		camera.rotateAroundOrigin(Axis.Y, -Math.PI / 2);
		assertTrue("incorrect camera position after Y axis rotation", camera.getPosition().epsilonEquals(new Vector3d(5d, 0d, 0d), TestUtils.EPSILON));
	}

	@Test
	public void testRotateAroundOriginVector3dDouble() {

		// starting at (0,0,5) and pointing at (0,0,0)
		assertTrue("camera not in correct starting position", camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));
		assertTrue("camera not pointing in correct start direction", camera.getLookPoint().epsilonEquals(new Vector3d(0d, 0d, 0d), TestUtils.EPSILON));

		// rotate 180 degrees around axis (1,1,1)
		camera.rotateAroundOrigin(new Vector3d(1, 1, 1), Math.PI);
		assertTrue(camera.getPosition().epsilonEquals(new Vector3d(10d / 3d, 10d / 3d, -5d / 3d), TestUtils.EPSILON));

		// rotate 180 degrees around axis (-1,-1,-1) back to (0,0,5)
		camera.rotateAroundOrigin(new Vector3d(-1, -1, -1), Math.PI);
		assertTrue(camera.getPosition().epsilonEquals(new Vector3d(0d, 0d, 5d), TestUtils.EPSILON));

		// rotate 90 degrees CCS around axis (0,1,0): y-axis
		camera.rotateAroundOrigin(new Vector3d(0d, 1d, 0d), Math.PI / 2);
		assertTrue(camera.getPosition().epsilonEquals(new Vector3d(5d, 0d, 0d), TestUtils.EPSILON));

	}

	@Test
	public void testGetVectorToPixel() {

		// check vector to center of screen is along negative z-axis for default values
		assertTrue("incorrect vector to center of screen for default values",
				camera.getVectorToPixel(1d, 1d, 2d, 2d).epsilonEquals(new Vector3d(0d, 0d, -1d), TestUtils.EPSILON));

		// re-initialize camera so viewplane is 1 unit away from it and has size 2 * 2 units (for easier math)
		camera.initialise(camera.getPosition(), camera.getLookPoint(), 1d, 2d, 2d);

		// vector to center of screen should still be along negative z-axis
		assertTrue("incorrect vector to center of screen after re-initialization",
				camera.getVectorToPixel(2d, 3d, 4d, 6d).epsilonEquals(new Vector3d(0d, 0d, -1d), TestUtils.EPSILON));

		// check vectors to all 4 corners clockwise from top left
		assertTrue(camera.getVectorToPixel(0d, 0d, 100d, 100d).epsilonEquals(new Vector3d(-1d / Math.sqrt(3), 1d / Math.sqrt(3), -1d / Math.sqrt(3)),
				TestUtils.EPSILON));
		assertTrue(camera.getVectorToPixel(100d, 0d, 100d, 100d).epsilonEquals(new Vector3d(1d / Math.sqrt(3), 1d / Math.sqrt(3), -1d / Math.sqrt(3)),
				TestUtils.EPSILON));
		assertTrue(camera.getVectorToPixel(0d, 100d, 100d, 100d).epsilonEquals(new Vector3d(-1d / Math.sqrt(3), -1d / Math.sqrt(3), -1d / Math.sqrt(3)),
				TestUtils.EPSILON));
		assertTrue(camera.getVectorToPixel(100d, 100d, 100d, 100d).epsilonEquals(new Vector3d(1d / Math.sqrt(3), -1d / Math.sqrt(3), -1d / Math.sqrt(3)),
				TestUtils.EPSILON));

		// rotate camera 180 degrees around the y-axis of origin then make sure vector to center of screen has been negated in x and z-axis
		camera.rotate(Axis.Y, Math.PI);
		assertTrue(camera.getVectorToPixel(1d, 1d, 2d, 2d).epsilonEquals(new Vector3d(0d, 0d, 1d), TestUtils.EPSILON));
		assertTrue(camera.getVectorToPixel(0d, 0d, 100d, 100d).epsilonEquals(new Vector3d(1d / Math.sqrt(3), 1d / Math.sqrt(3), 1d / Math.sqrt(3)),
				TestUtils.EPSILON));
	}

	@Test
	public void testGetPositionOfPixel() {

		// check position of pixel in top left of screen for default values (resolution not important)
		assertTrue("expected pixel position for top left (0): (-1.5,1.5,2)",
				camera.getPositionOfPixel(0d, 0d, 999d, 111d).epsilonEquals(new Vector3d(-1.5d, 1.5d, 2d), TestUtils.EPSILON));
		assertTrue("expected pixel position for top left (1): (-1.5,1.5,2)",
				camera.getPositionOfPixel(0d, 0d, 111d, 999d).epsilonEquals(new Vector3d(-1.5d, 1.5d, 2d), TestUtils.EPSILON));

		// check position of pixel in center of screen for default values
		assertTrue("expected pixel position for center: (0,0,2)",
				camera.getPositionOfPixel(1, 1, 2, 2).epsilonEquals(new Vector3d(0d, 0d, 2d), TestUtils.EPSILON));

		// check position of pixel in bottom right screen for default values (set pixel number to resolution max)
		assertTrue("expected pixel position for bottom right: (1.5,-1.5,2)",
				camera.getPositionOfPixel(123d, 456d, 123d, 456d).epsilonEquals(new Vector3d(1.5d, -1.5d, 2d), TestUtils.EPSILON));

		// move camera by offset (1,1,1) and check position in center of screen has changed accordingly
		camera.moveCameraBy(new Vector3d(1d, 1d, 1d));
		assertTrue("expected pixel position for center after move: (1,1,3)",
				camera.getPositionOfPixel(1, 2, 2, 4).epsilonEquals(new Vector3d(1d, 1d, 3d), TestUtils.EPSILON));
	}

	@Test
	public void testGetPosition() {
		// assert getting the default camera position
		assertTrue("incorrect position for default values: ", camera.getPosition().epsilonEquals(startPosition, TestUtils.EPSILON));
	}

	@Test
	public void testGetLookPoint() {
		// assert getting the default lookPoint of the camera
		assertTrue("incorrect lookPoint for default values: ", camera.getLookPoint().epsilonEquals(startLookPoint, TestUtils.EPSILON));
	}

	@Test
	public void testGetLookVector() {
		// assert getting the look vector works for default camera: (0,0,-1)
		assertTrue("incorrect lookVector for default values: " + camera.getLookVector(),
				camera.getLookVector().epsilonEquals(new Vector3d(0d, 0d, -1d), TestUtils.EPSILON));
	}

	@Test
	public void testGetUpVector() {
		// assert getting the up vector works for default camera: (0,1,0)
		assertTrue("incorrect upVector for default values: " + camera.getUpVector(),
				camera.getUpVector().epsilonEquals(new Vector3d(0d, 1d, 0d), TestUtils.EPSILON));
	}

	@Test
	public void testGetDistanceToViewplane() {
		// assert getting the distance to the viewplane works for default distance
		assertTrue("incorrect distanceToViewplane for default values: " + camera.getDistanceToViewplane(),
				TestUtils.equals(camera.getDistanceToViewplane(), startDistanceToViewplane));
	}

	@Test
	public void testGetViewplaneTop() {
		// assert getting the default viewplaneTop
		assertTrue("incorrect viewplaneTop for default values: " + camera.getViewplaneTop(),
				camera.getViewplaneTop().epsilonEquals(new Vector3d(3d, 0d, 0d), TestUtils.EPSILON));
	}

	@Test
	public void testGetViewplaneLeft() {
		// assert getting the default viewplaneLeft
		assertTrue("incorrect viewplaneLeft for default values: " + camera.getViewplaneLeft(),
				camera.getViewplaneLeft().epsilonEquals(new Vector3d(0d, -3d, 0d), TestUtils.EPSILON));
	}

}
