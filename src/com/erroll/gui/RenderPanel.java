package com.erroll.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JPanel;

import com.erroll.renderer.RayCaster;


public class RenderPanel extends JPanel {
	private static final long serialVersionUID = -1298205187260747210L;

	// width and height of the screen in pixels
	private static final int W = 512;
	private static final int H = 512;

	// object that casts the rays themselves
	private RayCaster rayCaster;

	// image and associated pixel data drawn into screen
	private static BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
	private static int[] imagePixelData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

	public RenderPanel() {
		this.setPreferredSize(new Dimension(W, H));

		rayCaster = new RayCaster();
		rayCaster.initialise(imagePixelData, W, H, this);
		rayCaster.start();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------
	
	public RayCaster getRaycaster() {
		return rayCaster;
	}

	public void setRaycaster(RayCaster r) {
		this.rayCaster = r;
	}
}
