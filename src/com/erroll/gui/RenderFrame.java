package com.erroll.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.vecmath.Vector3d;

import com.erroll.camera.CameraInterface;

public class RenderFrame extends JFrame {

	private static final long serialVersionUID = 1554826591877804614L;

	// singleton stuff (thread-safe)
	private static class SingletonHolder {
		public static final RenderFrame instance = new RenderFrame();
	}

	public static RenderFrame getInstance() {
		return SingletonHolder.instance;
	}

	private RenderFrame() {
	};

	public static void main(String[] args) {

		final RenderFrame frame = RenderFrame.getInstance();
		final RenderPanel panel = new RenderPanel();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		frame.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				CameraInterface c = panel.getRaycaster().getCamera();

				// ARROW KEYS [rotate camera around origin]
				if (e.getExtendedKeyCode() == 37)
					c.rotateAroundOrigin(c.getViewplaneLeft(), -0.05d);
				else if (e.getExtendedKeyCode() == 38)
					c.rotateAroundOrigin(c.getViewplaneTop(), +0.05d);
				else if (e.getExtendedKeyCode() == 39)
					c.rotateAroundOrigin(c.getViewplaneLeft(), +0.05d);
				else if (e.getExtendedKeyCode() == 40)
					c.rotateAroundOrigin(c.getViewplaneTop(), -0.05d);

				// WASD [move camera in 3D space]
				if (e.getExtendedKeyCode() == 87) {
					Vector3d offset = new Vector3d(c.getLookVector());
					offset.scale(0.1d);
					c.moveCameraBy(offset);
				} else if (e.getExtendedKeyCode() == 65) {
					Vector3d offset = new Vector3d(c.getViewplaneTop());
					offset.scale(-0.02d);
					c.moveCameraBy(offset);
				} else if (e.getExtendedKeyCode() == 83) {
					Vector3d offset = new Vector3d(c.getLookVector());
					offset.scale(-0.1d);
					c.moveCameraBy(offset);
				} else if (e.getExtendedKeyCode() == 68) {
					Vector3d offset = new Vector3d(c.getViewplaneTop());
					offset.scale(0.02d);
					c.moveCameraBy(offset);
				}

				// IJKL [rotate lookvector]
				if (e.getExtendedKeyCode() == 73)
					c.rotate(c.getViewplaneTop(), +0.01d);
				else if (e.getExtendedKeyCode() == 74)
					c.rotate(c.getViewplaneLeft(), -0.01d);
				else if (e.getExtendedKeyCode() == 75)
					c.rotate(c.getViewplaneTop(), -0.01d);
				else if (e.getExtendedKeyCode() == 76)
					c.rotate(c.getViewplaneLeft(), +0.01d);

			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
	}
}
