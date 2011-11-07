package com.erroll.octree;

import java.awt.Color;

import javax.vecmath.Vector3d;

import com.erroll.fractal.FractalUtils;

public class OctreeModel {
	// number of voxels in each octree axis
	private int VOXEL_NUMBER = 32;

	// position (bottom left) and dimensions of the octree
	private Vector3d position;
	private double dim;

	// root node of the octree
	private OctreeNode root;
	private Color structure[][][] = new Color[VOXEL_NUMBER][VOXEL_NUMBER][VOXEL_NUMBER];

	/**
	 * Make a RenderedOctree centered on origin 2 units wide.
	 */
	public OctreeModel() {
		position = new Vector3d(-1d, -1d, -1d);
		dim = 2d;

		int colorMultiple = 256 / VOXEL_NUMBER;

		for (int i = 0; i < VOXEL_NUMBER; i++)
			for (int j = 0; j < VOXEL_NUMBER; j++)
				for (int k = 0; k < VOXEL_NUMBER; k++) {
					Color c = new Color(i * colorMultiple, j * colorMultiple, k * colorMultiple + 1);

					// structure[i][j][k] = Math.random() > 0.7 ? c : Color.BLACK;
					// structure[i][j][k] = FractalUtils.isInGrid(i, j, k, 3) ? c : Color.BLACK;
					// structure[i][j][k] = FractalUtils.isInMengerSponge1(i, j, k, VOXEL_NUMBER) ? c : Color.BLACK;
					structure[i][j][k] = FractalUtils.isInMengerSponge2((0.0d + i) / VOXEL_NUMBER, (0.0d + j) / VOXEL_NUMBER, (0.0d + k) / VOXEL_NUMBER) ? c
							: Color.BLACK;

				}

		// turn 3d array into octree
		root = buildOctree(structure);
		// the 3d array is no longer needed
		structure = null;
	}

	/**
	 * Make a RenderedOctree centered on origin 2 units wide using the shape of structure provided
	 */
	public OctreeModel(boolean structureParam[][][]) {
		VOXEL_NUMBER = structureParam.length;
		position = new Vector3d(-1d, -1d, -1d);
		dim = 2d;

		int colorMultiple = 256 / VOXEL_NUMBER;

		for (int i = 0; i < VOXEL_NUMBER; i++)
			for (int j = 0; j < VOXEL_NUMBER; j++)
				for (int k = 0; k < VOXEL_NUMBER; k++) {
					Color c = new Color(i * colorMultiple, j * colorMultiple, k * colorMultiple + 1);
					structure[i][j][k] = structureParam[i][j][k] ? c : Color.BLACK;
				}

		// turn 3d array into octree
		root = buildOctree(structure);
		// the 3d array is no longer needed
		structure = null;
	}

	/**
	 * Recursively builds an octree from a 3D array of voxels until an empty node or a leaf is found.
	 * 
	 * @param structureSubset
	 *            the subset of voxels to turn into an OctreeNode
	 * @return the OctreeNode containing all voxels in the structureSubset
	 */
	public OctreeNode buildOctree(Color[][][] structureSubset) {

		// if down to 1 voxel, set it's color if not empty
		if (structureSubset.length == 1) {
			OctreeNode leaf = new OctreeNode();
			leaf.setEmpty(structureSubset[0][0][0] == Color.BLACK ? true : false);
			leaf.setLeaf(true);
			leaf.setColor(structureSubset[0][0][0]);

			return leaf;
		} else

		// if octree is empty return empty leaf
		if (isEmpty(structureSubset)) {
			OctreeNode emptyLeaf = new OctreeNode();
			emptyLeaf.setEmpty(true);
			emptyLeaf.setLeaf(true);
			emptyLeaf.setColor();

			return emptyLeaf;
		} else

		// make children octrees
		{
			OctreeNode fullNode = new OctreeNode();
			fullNode.setEmpty(false);
			fullNode.setLeaf(false);
			fullNode.setColor();

			OctreeNode[][][] children = new OctreeNode[2][2][2];

			for (int x = 0; x < 2; x++)
				for (int y = 0; y < 2; y++)
					for (int z = 0; z < 2; z++)
						children[x][y][z] = buildOctree(getChildrenAtIndex(x, y, z, structureSubset));

			fullNode.setChildren(children);

			return fullNode;
		}
	}

	/**
	 * @param structureSubset
	 *            The 3d array of voxels to be tested for being empty
	 * @return true if the 3d array is empty, false otherwise
	 */
	public boolean isEmpty(Color[][][] structureSubset) {
		int size = structureSubset.length;

		// loop through all voxels and return false if a non-empty (non-black) one is found
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				for (int k = 0; k < size; k++)
					if (structureSubset[i][j][k] != Color.BLACK)
						return false;

		return true;
	}

	/**
	 * Returns a requested subset of the voxels from the set given to it
	 * 
	 * @param x
	 *            the x index of the children
	 * @param y
	 *            the y index of the children
	 * @param z
	 *            the z index of the children
	 * @param structureSubset
	 *            the set of voxels to take the children from
	 * @return a subset of one eighth of the voxels passed to it
	 */
	public Color[][][] getChildrenAtIndex(int x, int y, int z, Color[][][] structureSubset) {

		// determine the size of the array of voxels passed to it and the size of the child array
		int size = structureSubset.length;
		int s2 = size / 2;
		int offsetX = 0, offsetY = 0, offsetZ = 0;

		// make a new smaller array for the child voxels
		Color[][][] childrenArray = new Color[s2][s2][s2];

		// determine the offset depending on the x, y, and z indexes passed to it
		switch (x) {
		case 0:
			offsetX = 0;
			break;
		case 1:
			offsetX = s2;
			break;
		}

		switch (y) {
		case 0:
			offsetY = 0;
			break;
		case 1:
			offsetY = s2;
			break;
		}

		switch (z) {
		case 0:
			offsetZ = 0;
			break;
		case 1:
			offsetZ = s2;
			break;
		}

		// loop through all child voxels in the array of all voxels and put them into a new smaller array
		for (int i = 0; i < s2; i++)
			for (int j = 0; j < s2; j++)
				for (int k = 0; k < s2; k++)
					childrenArray[i][j][k] = structureSubset[i + offsetX][j + offsetY][k + offsetZ];

		return childrenArray;
	}

	// ----------------------------------------------------------------------------
	// Getters & Setters
	// ----------------------------------------------------------------------------

	public OctreeNode getRoot() {
		return root;
	}

	public Vector3d getPosition() {
		return position;
	}

	public void setPosition(Vector3d position) {
		this.position = position;
	}

	public double getDim() {
		return dim;
	}

	public void setDim(double dim) {
		this.dim = dim;
	}
}
