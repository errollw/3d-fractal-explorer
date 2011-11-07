package com.erroll.gui;

public class FpsCounter {
	public int currentFps;
	public int fps;
	public long start;

	// singleton stuff (Bill Pugh thread-safe)
	private static class SingletonHolder {
		public static final FpsCounter instance = new FpsCounter();
	}

	public static FpsCounter getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Construct the FpsCounter, initialize all fields to 0
	 */
	private FpsCounter() {
		currentFps = 0;
		fps = 0;
		start = 0;
	}

	/**
	 * Called by the panel once it draws a frame. This increases the frames-per-second counter during the second for
	 * which it is counted
	 */
	public void tick() {
		currentFps++;
		if (System.currentTimeMillis() - start >= 1000) {
			fps = currentFps;
			currentFps = 0;
			start = System.currentTimeMillis();

			RenderFrame.getInstance().setTitle("Octree kd-restart test: " + fps + " fps");
		}
	}

	/**
	 * @return The current frames rendered per second
	 */
	public int getFps() {
		return fps;
	}

}
