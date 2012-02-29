package com.erroll.octree;

public class NeighborUtils {

	/**
	 * @param neighborId
	 *            The neighbor id you want to get the opposite of
	 * @return The opposite neighbor id, e.g. getOpposite(0) = 1
	 */
	public static final int getOpposite(int neighborId) {
		switch (neighborId) {
		case 0:
			return 1;
		case 1:
			return 0;
		case 2:
			return 3;
		case 3:
			return 2;
		case 4:
			return 5;
		case 5:
			return 4;
		default:
			return 0;
		}
	}
}
