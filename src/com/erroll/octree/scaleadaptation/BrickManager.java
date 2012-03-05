package com.erroll.octree.scaleadaptation;

import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.erroll.octree.OctreeNode;

public class BrickManager {

	// the list of all OctreeNodes which are marked as being bricks
	private LinkedHashSet<OctreeNode> brickList;

	// The number of unifying node threads
	private final int NTHREADS = 100;

	/**
	 * Constructs a BrickManager with a linked list of OctreeNodes to store bricks
	 */
	public BrickManager() {
		brickList = new LinkedHashSet<OctreeNode>();
	}

	/**
	 * Adds new brick to list of brick nodes.
	 * 
	 * @param newBrick
	 *            New brick node to be added
	 */
	public void addBrick(OctreeNode newBrick) {
		synchronized (brickList) {
			brickList.add(newBrick);
		}
	}

	/**
	 * Loop through all stored bricks and unify ones that have not been visited.
	 */
	public synchronized void unifyBricks() {
		synchronized (brickList) {
			// make a new list of bricks
			LinkedHashSet<OctreeNode> newBrickList = new LinkedHashSet<OctreeNode>();

			// make a thread executor to execute node unification threads - one for each node to be unified
			ExecutorService unificationExecutor = Executors.newFixedThreadPool(NTHREADS);

			// copy old bricks into list of new bricks
			for (OctreeNode node : brickList) {
				if (!node.getVisited()) {
					UnifyNodeThread unt = new UnifyNodeThread(node);
					unificationExecutor.execute(unt);
				} else {
					node.clearVisited();
					newBrickList.add(node);
				}
			}

			// shutdown the executor and wait for all threads to terminate
			unificationExecutor.shutdown();
			try {
				unificationExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// sawp the old brickList for the new one with all nodes that were visited during the last pass
			brickList = newBrickList;
		}
	}
}
