package com.erroll.camera.paths;

import java.util.List;

import javax.vecmath.Vector3d;

import com.erroll.camera.CameraInterface;
import com.erroll.metrics.Metrics;
import com.erroll.renderer.Renderer;

public abstract class FlightPath {

	// whether the flight path has been started or not
	private boolean started = false;

	// the number of frames that have passed for each flight path segment
	protected int t = 0;

	// the maximum number of frames for the infinite zoom part of flight path (default 500)
	protected int tMaxInfZoom = 500;

	// list of segments in tour flight path
	protected List<SegmentInterface> pathSegs;

	// records which segment is being currently carried out and index into the list of segments
	protected SegmentInterface currentSeg;
	protected int segIndex = 0;

	/**
	 * Updates the camera position per frame, possibly using distance information from the renderer. Will print out Metrics data once the flight paths is
	 * complete.
	 * 
	 * @param camera
	 *            The Camera object to be moved during the frame
	 * @param renderer
	 *            The renderer with which to use distance information during infinite zoom sequences
	 * @param metrics
	 *            The Metrics object which will be used when printing out end of flight path data
	 */
	public void updatePosition(CameraInterface camera, Renderer renderer, Metrics metrics) {

		if (started) {
			if (currentSeg != null && t < currentSeg.getTMax()) {

				// follow tour flight path stage
				currentSeg.update(camera, t);
				t++;
				// if there is another segment in the list, load it and reset frame counter
				if (t == (currentSeg.getTMax() - 1) && segIndex < (pathSegs.size() - 1)) {
					t = 0;
					segIndex++;
					currentSeg = pathSegs.get(segIndex);
				}

			} else if (t < tMaxInfZoom) {

				// infinite zoom stage
				Vector3d offset = new Vector3d(camera.getLookVector());
				offset.scale(renderer.getOptTmin() * 0.01);
				camera.moveCameraBy(offset);
				t++;

			} else if (t == tMaxInfZoom) {

				// finally print out average frames per second
				System.out.println("Average FPS: " + metrics.getAvgFps());
				t++;

			}
		}
	}

	/**
	 * Starts the flight path
	 */
	public void start() {
		started = true;
	}
}
