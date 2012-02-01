package com.erroll.metrics;

import java.util.HashMap;

public class Metrics {

	// fields for tracking frames per second
	private long timeSince;
	private int fpsAccumulator;
	private int fps;

	// this HashMap can be used for other data being tracked
	private HashMap<String, Double> data = new HashMap<String, Double>();

	/**
	 * Construct the FpsCounter, initializing all fields
	 */
	public Metrics() {
		fpsAccumulator = 0;
		fps = 0;
		timeSince = 0;
	}

	/**
	 * Called by the panel once it draws a frame. This increases the frames-per-second counter during the second for which it is counted
	 */
	public void registerFrameRender() {
		if (System.currentTimeMillis() - timeSince >= 1000) {
			fps = fpsAccumulator;
			fpsAccumulator = 0;
			timeSince = System.currentTimeMillis();
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
	 * @param id
	 *            The identifier of the particular value being requested
	 * @return the value of the arbitrary data being requested or Integer.MIN_VALUE if no data exists
	 */
	public Double getData(String id) {
		if (data.containsKey(id))
			return data.get(id);
		else
			return Double.MIN_VALUE;
	}

	/**
	 * Adds d to or creates a field in the data of value d for the key specified
	 * 
	 * @param id
	 *            The identifier of the value
	 */
	public void incrementData(String id, double d) {
		if (data.containsKey(id))
			data.put(id, data.get(id) + d);
		else
			data.put(id, d);
	}
}
