package com.erroll.camera.paths.mengersponge;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

import com.erroll.camera.CameraInterface;
import com.erroll.camera.paths.CircleSegment;
import com.erroll.camera.paths.FlightPath;
import com.erroll.camera.paths.LerpSegment;
import com.erroll.camera.paths.SegmentInterface;
import com.erroll.math.Axis;

public class MengerSpongeTourPath extends FlightPath {

	public MengerSpongeTourPath(CameraInterface c) {
		// set starting position and FOV of camera
		c.moveCamera(new Vector3d(0, 0, 15));
		c.setDistanceToViewplane(2);
		c.rotateAroundOrigin(Axis.X, -Math.PI / 8);

		// make list of path segments
		pathSegs = new ArrayList<SegmentInterface>();
		pathSegs.add(new CircleSegment(200, 1d, 10d, Axis.Y, new Vector3d(0d, 0d, 0d)));
		pathSegs.add(new CircleSegment(30, 0.0625, 0d, Axis.X, new Vector3d(0d, 0d, 0d)));
		pathSegs.add(new LerpSegment(50, new Vector3d(0.67d, 0d, 3d)));
		pathSegs.add(new CircleSegment(70, 0.25d, 1d, Axis.Y, new Vector3d(0.67d, 0d, 0.67d)));
		pathSegs.add(new LerpSegment(100, new Vector3d(1d, 0d, 2 / 3d)));
		pathSegs.add(new LerpSegment(100, new Vector3d(2 / 3d, 0d, 2 / 3d), new Vector3d(0d, -0.3d, -1d)));
		pathSegs.add(new LerpSegment(150, new Vector3d(2 / 3d, 0d, 0d), new Vector3d(0d, 0d, -1d)));
		pathSegs.add(new LerpSegment(150, new Vector3d(2 / 3d, 0d, -4 / 9d), new Vector3d(-1d, -0.3d, 0d)));
		pathSegs.add(new LerpSegment(150, new Vector3d(0d, 0d, -4 / 9d), new Vector3d(-1d, 0d, 0d)));
		pathSegs.add(new LerpSegment(200, new Vector3d(-0.3d, -0.3d, 0.3d), new Vector3d(1d, 1d, -1d)));

		// set starting segment
		currentSeg = pathSegs.get(segIndex);
	}
}
