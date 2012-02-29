package com.erroll.renderer.effects;

import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Test;

public class ColorUtilsTest {

	@Test
	public void testAdjustLight() {
		// test darkening a color
		int darkRed = ColorUtils.adjustLight(Color.RED.getRGB(), 200);
		assertTrue(new Color(darkRed).getRed() == 55);
		assertTrue(new Color(darkRed).getGreen() == 0);
		assertTrue(new Color(darkRed).getBlue() == 0);

		// test lightening a color
		int lightRed = ColorUtils.adjustLight(Color.RED.getRGB(), -200);
		assertTrue(new Color(lightRed).getRed() == 255);
		assertTrue(new Color(lightRed).getGreen() == 200);
		assertTrue(new Color(lightRed).getBlue() == 200);
	}

	@Test
	public void testGetPositionalColor() {

		// check maximum corner is white
		assertTrue(new Color(ColorUtils.getPositionalColor(1d, 1d, 1d)).getRed() == 255);
		assertTrue(new Color(ColorUtils.getPositionalColor(1d, 1d, 1d)).getGreen() == 255);
		assertTrue(new Color(ColorUtils.getPositionalColor(1d, 1d, 1d)).getBlue() == 255);

		// check minimum corner is black
		assertTrue(new Color(ColorUtils.getPositionalColor(-1d, -1d, -1d)).getRed() == 0);
		assertTrue(new Color(ColorUtils.getPositionalColor(-1d, -1d, -1d)).getGreen() == 0);
		assertTrue(new Color(ColorUtils.getPositionalColor(-1d, -1d, -1d)).getBlue() == 0);

		// check other corners
		assertTrue(new Color(ColorUtils.getPositionalColor(1d, 1d, -1d)).getRed() == 255);
		assertTrue(new Color(ColorUtils.getPositionalColor(1d, 1d, -1d)).getGreen() == 255);
		assertTrue(new Color(ColorUtils.getPositionalColor(1d, 1d, -1d)).getBlue() == 0);

		assertTrue(new Color(ColorUtils.getPositionalColor(-1d, 1d, -1d)).getRed() == 0);
		assertTrue(new Color(ColorUtils.getPositionalColor(-1d, 1d, -1d)).getGreen() == 255);
		assertTrue(new Color(ColorUtils.getPositionalColor(-1d, 1d, -1d)).getBlue() == 0);
	}

	@Test
	public void testGetColor() {

		// test getting a basic color
		int red = ColorUtils.getColor(255, 0, 0);
		assertTrue(new Color(red).getRed() == 255);
		assertTrue(new Color(red).getGreen() == 0);
		assertTrue(new Color(red).getBlue() == 0);

		// test getting another color
		int c = ColorUtils.getColor(12, 34, 56);
		assertTrue(new Color(c).getRed() == 12);
		assertTrue(new Color(c).getGreen() == 34);
		assertTrue(new Color(c).getBlue() == 56);
	}
}
