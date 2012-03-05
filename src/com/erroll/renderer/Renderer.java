package com.erroll.renderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

import com.erroll.camera.Camera;
import com.erroll.metrics.Metrics;
import com.erroll.octree.OctreeNode;
import com.erroll.octree.scaleadaptation.BrickManager;
import com.erroll.octree.scaleadaptation.Subdivider;
import com.erroll.renderer.effects.ColorUtils;
import com.erroll.renderer.effects.SSAO;

public class Renderer {

	// camera from which to cast rays
	private Camera camera;

	// list of potential root nodes to start traversal from
	private OctreeNode rootNode;

	// screen size in pixels
	protected int screenWidth;
	protected int screenHeight;

	// BufferedImage in which to draw pixels during rendering
	protected BufferedImage backbuffer;
	protected int[] imagePixelData;
	protected int[] imageColors;
	protected int[] imageShadows;
	protected double[] imageDepth;

	// Voxel size constants for checking if voxel is small enough to terminate traversal early if projected voxel size is less than 1 pixel on the screen
	protected double voxelSizeConstantA = 0d;
	protected double voxelSizeConstantB = 0d;

	// Queue nodes here for subdivision
	private Subdivider subdivider;
	// private SubdividerThreadless subdivider;

	// bricks unified by the manager
	private BrickManager brickManager;

	// Metrics object to record FPS
	private Metrics metrics;

	// the optimal distance to use as a measurement for distance to fractal from the camera
	private double optTmin = 0d;

	// whether recording is enabled;
	private boolean recording = false;

	// The number of rendering node threads
	private final int NTHREADS = 10;

	// the number of frames that have elapsed since starting the renderer
	private int frameIndex;

	public void render(Graphics g) {
		// make a copy of the camera to prevent artefacts from moving the camera during rendering
		Camera cameraFrame = new Camera(camera);

		// increment the number of frames rendered
		frameIndex++;

		// set constants for terminating voxel traversal early if projected voxel size is less than 1 pixel on the screen. This small calculation is done once a
		// frame in case the camera's FOV has changed.
		voxelSizeConstantA = cameraFrame.getDistanceToViewplane() * screenWidth;
		voxelSizeConstantB = cameraFrame.getViewplaneTop().length() * 0.5d;

		// set the skipNode for this frame
		setSkipNode(cameraFrame.getPosition());

		// make a thread executor to execute node rendering threads - one for each pixel
		ExecutorService rendererExecutor = Executors.newFixedThreadPool(NTHREADS);

		// for every pixel on the screen, cast a ray through it at the octreeModel
		for (int row = 0; row < screenHeight; row++) {
			for (int col = 0; col < screenWidth; col++) {
				Ray cameraRay = new Ray(cameraFrame.getPosition(), cameraFrame.getVectorToPixel(col, row, screenWidth, screenHeight));

				if (skipNode != null) {
					RayCast rc = new RayCast(skipNode, skipNodeBoxMin, skipNodeBoxDim, cameraRay, row * screenHeight + col, imageColors, imageDepth,
							subdivider, voxelSizeConstantA, voxelSizeConstantB);
					rendererExecutor.execute(rc);
				} else {
					RayCast rc = new RayCast(rootNode, new Vector3d(-1, -1, -1), 2, cameraRay, row * screenHeight + col, imageColors, imageDepth, subdivider,
							voxelSizeConstantA, voxelSizeConstantB);
					rendererExecutor.execute(rc);
				}
			}
		}

		// shutdown the executor and wait for all threads to terminate
		rendererExecutor.shutdown();
		try {
			rendererExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// do SSAO calculations and put results in imageShadows and get minimum tMin
		optTmin = SSAO.setOcclusion(imageDepth, screenHeight, screenWidth, imageShadows);

		// set the optimal tmin value to be used by camera movement
		optTmin = imageDepth[(screenWidth / 2) * screenHeight + (screenWidth / 2)] == Double.MAX_VALUE ? optTmin : imageDepth[(screenWidth / 2) * screenHeight
				+ (screenWidth / 2)];

		// adjust the colors in imageColors by the lighting in imageShadows to output the final colors in imagePixelData
		for (int row = 0; row < screenHeight; row++)
			for (int col = 0; col < screenWidth; col++)
				imagePixelData[row * screenHeight + col] = ColorUtils
						.adjustLight(imageColors[row * screenHeight + col], imageShadows[row * screenHeight + col]);

		// save image if recording is enabled
		if (isRecording()) {
			try {
				File outputfile = new File("images/screenshot" + frameIndex + ".png");
				ImageIO.write(backbuffer, "png", outputfile);
			} catch (IOException e) {
				System.err.println("file write IO exception");
			}
		}

		// draw the image onto the graphics
		g.drawImage(backbuffer, 0, 0, null);
		backbuffer.flush();

		// register that a frame has been drawn
		metrics.registerFrameRender();

		// unify the bricks every 10 frames
		if (frameIndex % 10 == 0)
			brickManager.unifyBricks();
	}

	// the node we can skip to during rendering
	private OctreeNode skipNode;
	private Vector3d skipNodeBoxMin;
	private double skipNodeBoxDim;

	/**
	 * Sets the skipNode for each frame. The skipNode should be the smallest node in which the camera's rays start from (its position)
	 * 
	 * @param cameraPos
	 *            The position of the camera in space.
	 */
	private void setSkipNode(Vector3d cameraPos) {

		// if the skipNode has not been set yet, check if the camera is inside the bounding box
		if (skipNode == null) {
			if (cameraPos.x < 1 && cameraPos.x > -1 && cameraPos.y < 1 && cameraPos.y > -1 && cameraPos.z < 1 && cameraPos.z > -1) {

				// if it is, descend hierarchy from root node until leaf reached
				OctreeNode node = rootNode;
				double boxDim = 2d;
				Vector3d boxMin = new Vector3d(-1, -1, -1);

				while (!node.isLeaf()) {
					boxDim /= 2d;
					boolean[] s = { (cameraPos.x >= (boxMin.x + boxDim)) ? true : false, (cameraPos.y >= (boxMin.y + boxDim)) ? true : false,
							(cameraPos.z >= (boxMin.z + boxDim)) ? true : false };
					boxMin.x += s[0] ? boxDim : 0;
					boxMin.y += s[1] ? boxDim : 0;
					boxMin.z += s[2] ? boxDim : 0;
					node = node.getChild(s[0] ? 1 : 0, s[1] ? 1 : 0, s[2] ? 1 : 0);
				}

				// once skipNode has been found for next frame, set it and record it's position and dimensions
				skipNode = node;
				skipNodeBoxMin = boxMin;
				skipNodeBoxDim = boxDim;
			}
		} else {

			// otherwise check the camera's position is still within the skipNode, if it is, possibly descend further down the hierarchy
			if (cameraPos.x > skipNodeBoxMin.x && cameraPos.x < (skipNodeBoxMin.x + skipNodeBoxDim) && cameraPos.y > skipNodeBoxMin.y
					&& cameraPos.y < (skipNodeBoxMin.y + skipNodeBoxDim) && cameraPos.z > skipNodeBoxMin.z && cameraPos.z < (skipNodeBoxMin.z + skipNodeBoxDim)) {

				if (!skipNode.isLeaf()) {
					// take current skipNode values and descend hierarchy
					OctreeNode node = skipNode;
					double boxDim = skipNodeBoxDim;
					Vector3d boxMin = new Vector3d(skipNodeBoxMin);

					while (!node.isLeaf()) {
						boxDim /= 2d;
						boolean[] s = { (cameraPos.x >= (boxMin.x + boxDim)) ? true : false, (cameraPos.y >= (boxMin.y + boxDim)) ? true : false,
								(cameraPos.z >= (boxMin.z + boxDim)) ? true : false };
						boxMin.x += s[0] ? boxDim : 0;
						boxMin.y += s[1] ? boxDim : 0;
						boxMin.z += s[2] ? boxDim : 0;
						node = node.getChild(s[0] ? 1 : 0, s[1] ? 1 : 0, s[2] ? 1 : 0);
					}

					// once new skipNode has been found inside old one, set it and record it's position and dimensions
					skipNode = node;
					skipNodeBoxMin = boxMin;
					skipNodeBoxDim = boxDim;
				}

			} else {

				// otherwise the camera has left the current skipNode and a new one must be found
				// first find the possibly correct neighbor
				OctreeNode oldSkipNode = skipNode;
				if (cameraPos.x < skipNodeBoxMin.x) {
					skipNode = skipNode.getNeighbor(0);
					skipNodeBoxMin.x -= skipNodeBoxDim;
				} else if (cameraPos.x > (skipNodeBoxMin.x + skipNodeBoxDim)) {
					skipNode = skipNode.getNeighbor(1);
					skipNodeBoxMin.x += skipNodeBoxDim;
				} else if (cameraPos.y < skipNodeBoxMin.y) {
					skipNode = skipNode.getNeighbor(2);
					skipNodeBoxMin.y -= skipNodeBoxDim;
				} else if (cameraPos.y > (skipNodeBoxMin.y + skipNodeBoxDim)) {
					skipNode = skipNode.getNeighbor(3);
					skipNodeBoxMin.y += skipNodeBoxDim;
				} else if (cameraPos.z < skipNodeBoxMin.z) {
					skipNode = skipNode.getNeighbor(4);
					skipNodeBoxMin.z -= skipNodeBoxDim;
				} else if (cameraPos.z > (skipNodeBoxMin.z + skipNodeBoxDim)) {
					skipNode = skipNode.getNeighbor(5);
					skipNodeBoxMin.z += skipNodeBoxDim;
				}

				// if no neighbor node found, just retun null skipNode for worst case scenario
				if (skipNode != null) {

					// if coarser neighbor node found, snap skipNodeBoxMin onto coarser grid
					if (skipNode.getDepth() != oldSkipNode.getDepth()) {
						skipNodeBoxDim = Math.pow(2d, -skipNode.getDepth() + 1d);
						skipNodeBoxMin.x = Math.floor(skipNodeBoxMin.x / skipNodeBoxDim) * skipNodeBoxDim;
						skipNodeBoxMin.y = Math.floor(skipNodeBoxMin.y / skipNodeBoxDim) * skipNodeBoxDim;
						skipNodeBoxMin.z = Math.floor(skipNodeBoxMin.z / skipNodeBoxDim) * skipNodeBoxDim;
					}

					// now do one last check that the camera is inside the new skipNode. If not, set it to null
					if (!(cameraPos.x > skipNodeBoxMin.x && cameraPos.x < (skipNodeBoxMin.x + skipNodeBoxDim) && cameraPos.y > skipNodeBoxMin.y
							&& cameraPos.y < (skipNodeBoxMin.y + skipNodeBoxDim) && cameraPos.z > skipNodeBoxMin.z && cameraPos.z < (skipNodeBoxMin.z + skipNodeBoxDim)))
						skipNode = null;
				}
			}

		}
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters which re-initialize fields
	// ----------------------------------------------------------------------------

	public void setCamera(Camera cameraParam) {
		camera = cameraParam;

		// set constants for terminating voxel traversal early if projected voxel size is less than 1 pixel on the screen
		voxelSizeConstantA = camera.getDistanceToViewplane() * screenWidth;
		voxelSizeConstantB = camera.getViewplaneTop().length() / 2;
	}

	public void setScreenSize(int screenWidthParam, int screenHeightParam) {
		screenWidth = screenWidthParam;
		screenHeight = screenHeightParam;

		// create back buffered image with which to draw pixels
		backbuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
		imagePixelData = ((DataBufferInt) backbuffer.getRaster().getDataBuffer()).getData();

		// create color and SSAO arrays
		imageColors = new int[screenHeight * screenWidth];
		imageDepth = new double[screenHeight * screenWidth];
		imageShadows = new int[screenHeight * screenWidth];
	}

	public void setRootNode(OctreeNode node) {
		rootNode = node;
	}

	public void setSubdivider(Subdivider subdivider) {
		this.subdivider = subdivider;
	}

	public void setBrickManager(BrickManager brickManager) {
		this.brickManager = brickManager;
	}

	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	};

	public double getOptTmin() {
		return optTmin;
	}

	public boolean isRecording() {
		return recording;
	}

	public void setRecording(boolean recording) {
		this.recording = recording;
	}
}
