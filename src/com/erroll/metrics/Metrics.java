package com.erroll.metrics;

import java.util.HashMap;

public class Metrics {

	// fields for tracking frames per second
	public int fpsAccumulator;
	public int fps;
	public long start;

	// this HashMap can be used for other data being tracked
	private HashMap<String, Integer> data = new HashMap<String, Integer>();

	/**
	 * Construct the FpsCounter, initializing all fields
	 */
	public Metrics() {
		initialise();
	}

	/**
	 * Initialize all fields being recorded
	 */
	private void initialise() {
		fpsAccumulator = 0;
		fps = 0;
		start = 0;
	}

	/**
	 * Called by the panel once it draws a frame. This increases the frames-per-second counter during the second for which it is counted
	 */
	public void registerFrameRender() {
		if (System.currentTimeMillis() - start >= 1000) {
			fps = fpsAccumulator;
			fpsAccumulator = 0;
			start = System.currentTimeMillis();
		} else {
			fpsAccumulator++;
		}
	}

	/**
	 * @return The current frames rendered per second
	 */
	public int getFps() {
		return fps;
	}

	/**
	 * @return The stored arbitrary data in the form of a HashMap
	 */
	public HashMap<String, Integer> getData() {
		return data;
	}

	/**
	 * @param id
	 *            The identifier of the particular value being requested
	 * @return the value of the arbitrary data being requested or Integer.MIN_VALUE if no data exists
	 */
	public Integer getData(String id) {
		if (data.containsKey(id))
			return data.get(id);
		else
			return Integer.MIN_VALUE;
	}

	/**
	 * Adds 1 to or creates a field in the data of value 1 for the key specified
	 * 
	 * @param id
	 *            The identifier of the value
	 */
	public void incrementData(String id) {
		if (data.containsKey(id))
			data.put(id, data.get(id) + 1);
		else
			data.put(id, 1);
	}
}
