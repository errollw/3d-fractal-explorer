package com.erroll.octree.scaleadaptation;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.vecmath.Vector3d;

import com.erroll.math.fractal.FractalInterface;
import com.erroll.math.fractal.Mandelbox;
import com.erroll.math.fractal.Mandelbulb;
import com.erroll.math.fractal.MengerSponge;
import com.erroll.math.fractal.SierpinskiGasket;
import com.erroll.octree.OctreeNode;
import com.erroll.properties.Parameters;

public class Subdivider implements Runnable {

	// queues of nodes to be subdivided,
	private BlockingQueue<OctreeNode> subdivNodeQueue = new LinkedBlockingQueue<OctreeNode>();
	private BlockingQueue<Vector3d> subdivBoxMinQueue = new LinkedBlockingQueue<Vector3d>();
	private BlockingQueue<Double> subdivBoxDimQueue = new LinkedBlockingQueue<Double>();

	// the fractal to be rendered
	private FractalInterface f;

	// The brick manager
	private BrickManager brickManager;

	// The number of subdivider node threads
	private final int NTHREADS = 100;

	public Subdivider() {
		// load properties and determine fractal type
		Properties props = Parameters.get();
		String fractalName = props.getProperty("FRACTALTYPE");
		if (fractalName.equals("Mandelbox"))
			f = new Mandelbox();
		else if (fractalName.equals("Mandelbulb"))
			f = new Mandelbulb();
		else if (fractalName.equals("MengerSponge"))
			f = new MengerSponge();
		if (fractalName.equals("SierpinskiGasket"))
			f = new SierpinskiGasket();
	}

	/**
	 * Queue a node for subdivision where it will be split into 8 children if any of them contain fractal detail; or made empty otherwise.
	 * 
	 * @param node
	 *            OctreeNode to be subdivided
	 * @param boxMin
	 *            The minimum position in space of the node
	 * @param boxDim
	 *            The width of the node
	 */
	public synchronized void queueNode(OctreeNode node, Vector3d boxMin, double boxDim) {
		try {
			if (!node.isQueuedSubdiv()) {
				node.setQueuedSubdiv(true);
				subdivNodeQueue.put(node);
				subdivBoxMinQueue.put(boxMin);
				subdivBoxDimQueue.put(boxDim);
			}
		} catch (InterruptedException e) {
			System.err.println("InterruptedException in queueing node");
			e.printStackTrace();
		}
	}

	// make a thread executor to execute node subdivision threads - one for each node to be subdivided
	ExecutorService subdivisionExecutor = Executors.newFixedThreadPool(NTHREADS);

	@Override
	public void run() {
		// loop to consume and subdivide nodes if available
		while (true) {
			try {
				SubdivideNodeThread snt = new SubdivideNodeThread(brickManager, f, subdivNodeQueue.take(), subdivBoxMinQueue.take(), subdivBoxDimQueue.take());
				subdivisionExecutor.execute(snt);
			} catch (InterruptedException e) {
				System.err.println("InterruptedException in consuming node");
				e.printStackTrace();
			}
		}
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	public void setBm(BrickManager brickManager) {
		this.brickManager = brickManager;
	}
}
