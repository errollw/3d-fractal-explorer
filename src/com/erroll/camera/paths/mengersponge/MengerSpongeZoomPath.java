package com.erroll.camera.paths.mengersponge;

import javax.vecmath.Vector3d;

import com.erroll.camera.CameraInterface;
import com.erroll.camera.paths.FlightPath;

public class MengerSpongeZoomPath extends FlightPath {

	public MengerSpongeZoomPath(CameraInterface c) {
		// set starting position and FOV of camera
		c.moveCamera(new Vector3d(-0.3d, -0.3d, 0.3d));
		c.setLookVector(new Vector3d(1, 1, -1));
		c.setDistanceToViewplane(2);

		// set tMaxInfZoom
		tMaxInfZoom = 1500;
	}
}
