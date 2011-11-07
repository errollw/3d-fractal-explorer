package com.erroll;

public class TestUtils {
	public static final double EPSILON = 0.0000001d;

	/**
	 * Tests whether double a equals double b to within a certain precision passed to the method
	 * 
	 * @param a
	 *            First double to be tested for equality
	 * @param b
	 *            Second double to be tested for equality
	 * @param epsilonParam
	 *            The allowed difference between a and b for them still to be equal
	 * @return true if (a-b) < epsilonParam
	 */
	public static boolean equals(double a, double b, double epsilonParam) {
		return Math.abs(a - b) < epsilonParam;
	}

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
