package com.erroll.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.vecmath.Vector3d;

import com.erroll.camera.CameraInterface;

public class KeyboardControlListener implements KeyListener {

	private static final double ORIGIN_ROTATE = 0.2d;

	// camera to be rotated
	private CameraInterface c;

	// set up fields
	public KeyboardControlListener(CameraInterface cameraParam) {
		c = cameraParam;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		e.consume();

		// ARROW KEYS [rotate camera around origin]
		if (e.getKeyCode() == 37)
			c.rotateAroundOrigin(c.getViewplaneLeft(), -ORIGIN_ROTATE);
		else if (e.getKeyCode() == 38)
			c.rotateAroundOrigin(c.getViewplaneTop(), +ORIGIN_ROTATE);
		else if (e.getKeyCode() == 39)
			c.rotateAroundOrigin(c.getViewplaneLeft(), +ORIGIN_ROTATE);
		else if (e.getKeyCode() == 40)
			c.rotateAroundOrigin(c.getViewplaneTop(), -ORIGIN_ROTATE);

		// WASD [move camera in 3D space]
		if (e.getKeyCode() == 87) {
			Vector3d offset = new Vector3d(c.getLookVector());
			offset.scale(0.1d);
			c.moveCameraBy(offset);
		} else if (e.getKeyCode() == 65) {
			Vector3d offset = new Vector3d(c.getViewplaneTop());
			offset.scale(-0.02d);
			c.moveCameraBy(offset);
		} else if (e.getKeyCode() == 83) {
			Vector3d offset = new Vector3d(c.getLookVector());
			offset.scale(-0.1d);
			c.moveCameraBy(offset);
		} else if (e.getKeyCode() == 68) {
			Vector3d offset = new Vector3d(c.getViewplaneTop());
			offset.scale(0.02d);
			c.moveCameraBy(offset);
		}

		// IJKL [rotate lookvector]
		if (e.getKeyCode() == 73)
			c.rotate(c.getViewplaneTop(), +0.01d);
		else if (e.getKeyCode() == 74)
			c.rotate(c.getViewplaneLeft(), -0.01d);
		else if (e.getKeyCode() == 75)
			c.rotate(c.getViewplaneTop(), -0.01d);
		else if (e.getKeyCode() == 76)
			c.rotate(c.getViewplaneLeft(), +0.01d);

	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
