package com.erroll;

public class TestUtils {
	public static final double EPSILON = 000000000.1d;

	/**
	 * Tests whether double a equals double b to within default precision
	 * 
	 * @param a
	 *            First double to be tested for equality
	 * @param b
	 *            Second double to be tested for equality
	 * @return true if (a-b) < epsilonParam
	 */
	public static boolean equals(double a, double b) {
		return Math.abs(a - b) < EPSILON;
	}
}
