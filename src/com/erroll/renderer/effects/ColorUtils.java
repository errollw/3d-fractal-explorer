package com.erroll.renderer.effects;

public class ColorUtils {

	/**
	 * Either darkens or lightens the int color rgb by the shadowStrength. 255 will make the color black and -255 will make the color white.
	 * 
	 * @param rgb
	 *            The original int color to be adjusted
	 * @param shadowAmount
	 *            The amount to adjust the color by, positive to darken it, negative to lighten it
	 * @return The adjusted int color
	 */
	public static final int adjustLight(int rgb, int shadowStrength) {

		// get color components and return darkened or lightened amounts
		int red = ((rgb >> 16) & 0xFF) - shadowStrength;
		int green = ((rgb >> 8) & 0xFF) - shadowStrength;
		int blue = (rgb & 0xFF) - shadowStrength;

		// clamp values to between 0 and 255
		red = red < 0 ? 0 : red > 255 ? 255 : red;
		green = green < 0 ? 0 : green > 255 ? 255 : green;
		blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;

		// generate new int rgb color and return it
		rgb = red;
		rgb = (rgb << 8) + green;
		rgb = (rgb << 8) + blue;

		return rgb;
	}

	/**
	 * Gets rainbow color for a position in space between (-1,-1,-1) and (1,1,1)
	 * 
	 * @param x
	 *            The x coordinate to get color for
	 * @param y
	 *            The y coordinate to get color for
	 * @param z
	 *            The z coordinate to get color for
	 * @return The int rainbow color of the node at that position
	 */
	public static final int getPositionalColor(double x, double y, double z) {

		// generate and return int color
		int rgb = (int) (((x + 1d) / 2) * 255);
		rgb = (rgb << 8) + (int) (((y + 1d) / 2) * 255);
		rgb = (rgb << 8) + (int) (((z + 1d) / 2) * 255);

		return rgb;
	}

	/**
	 * @return The int color generated with values r, g and b
	 */
	public static final int getColor(int r, int g, int b) {

		int rgb = r;
		rgb = (rgb << 8) + g;
		rgb = (rgb << 8) + b;

		return rgb;
	}
}
