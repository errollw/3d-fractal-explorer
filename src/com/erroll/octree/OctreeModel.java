package com.erroll.octree;

import java.awt.Color;
import java.util.Properties;

import javax.vecmath.Vector3d;

import com.erroll.fractal.FractalUtils;

public class OctreeModel {
	// number of voxels in each octree axis
	private int VOXEL_NUMBER = 32;

	// position (bottom left) and dimensions of the octree
	private Vector3d position;
	private double dim;

	// root node of the octree
	private OctreeNodeInterface root;
	private int structure[][][] = new int[VOXEL_NUMBER][VOXEL_NUMBER][VOXEL_NUMBER];

	/**
	 * Make a RenderedOctree centered on origin 2 units wide.
	 */
	public OctreeModel(Properties props) {
		position = new Vector3d(-1d, -1d, -1d);
		dim = 2d;

		VOXEL_NUMBER = Integer.parseInt(props.getProperty("VOXEL_NUMBER"));
		structure = new int[VOXEL_NUMBER][VOXEL_NUMBER][VOXEL_NUMBER];

		int colorMultiple = 256 / VOXEL_NUMBER;

		for (int i = 0; i < VOXEL_NUMBER; i++)
			for (int j = 0; j < VOXEL_NUMBER; j++)
				for (int k = 0; k < VOXEL_NUMBER; k++) {
					Color c = new Color(i * colorMultiple, j * colorMultiple, k * colorMultiple);
					// Color c = ((i+j+k) % 2 == 0) ? Color.blue : Color.WHITE;

					Boolean b = false;

					if (props.getProperty("GRID").equals("1"))
						b = FractalUtils.isInGrid(i, j, k, 2);
					else if (props.getProperty("MENGER1").equals("1"))
						b = FractalUtils.isInMengerSponge1(i, j, k, VOXEL_NUMBER);
					else if (props.getProperty("MENGER2").equals("1"))
						b = FractalUtils.isInMengerSponge2((0d + i) / VOXEL_NUMBER, (0d + j) / VOXEL_NUMBER, (0d + k) / VOXEL_NUMBER);
					else if (props.getProperty("MENGER3").equals("1"))
						b = FractalUtils
								.isInMengerSponge3(-1 + 2 * (0d + i) / VOXEL_NUMBER, -1 + 2 * (0d + j) / VOXEL_NUMBER, -1 + 2 * (0d + k) / VOXEL_NUMBER);
					else if (props.getProperty("MANDELBULB").equals("1"))
						b = FractalUtils.isInMandelBulb((2d * i) / (VOXEL_NUMBER) - 1d, (2d * j) / (VOXEL_NUMBER) - 1d, (2d * k) / (VOXEL_NUMBER) - 1d);
					else if (props.getProperty("SIERPINSKY").equals("1"))
						b = FractalUtils.sierpinski3((2d * i) / (VOXEL_NUMBER) - 1d, (2d * j) / (VOXEL_NUMBER) - 1d, (2d * k) / (VOXEL_NUMBER) - 1d);

					structure[i][j][k] = b ? c.getRGB() : Color.BLACK.getRGB();

				}

		// turn 3d array into octree
		root = buildOctree(structure, 0);

		// set neighbor pointers for the octree nodes
		setNeighborPointers(root);

		// the 3d array is no longer needed
		structure = null;
	}

	/**
	 * Make a OctreeModel centered on origin 2 units wide using the shape of structure provided
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
					structure[i][j][k] = structureParam[i][j][k] ? c.getRGB() : Color.BLACK.getRGB();
				}

		// turn 3d array into octree
		root = buildOctree(structure, 0);

		// set neighbor pointers in the octree starting from the root
		setNeighborPointers(root);

		// the 3d array is no longer needed
		structure = null;
	}

	/**
	 * Recursively builds an octree from a 3D array of voxels until an empty node or a leaf is found.
	 * 
	 * @param structureSubset
	 *            the subset of voxels to turn into an OctreeNode
	 * @param currentDepth
	 *            the depth currently being constructed at (0 is root)
	 * @return the OctreeNode containing all voxels in the structureSubset
	 */
	public OctreeNode buildOctree(int[][][] structureSubset, int currentDepth) {

		// if down to 1 voxel, set it's color if not empty
		if (structureSubset.length == 1) {
			OctreeNode leaf = new OctreeNode();
			leaf.setDepth(currentDepth);
			leaf.setEmpty(structureSubset[0][0][0] == Color.BLACK.getRGB() ? true : false);
			leaf.setLeaf(true);
			leaf.setColor(structureSubset[0][0][0]);

			return leaf;
		} else

		// if octree is empty return empty leaf
		if (isEmpty(structureSubset)) {
			OctreeNode emptyLeaf = new OctreeNode();
			emptyLeaf.setDepth(currentDepth);
			emptyLeaf.setEmpty(true);
			emptyLeaf.setLeaf(true);
			emptyLeaf.setColor(0);

			return emptyLeaf;
		} else

		// make children octrees
		{
			OctreeNode fullNode = new OctreeNode();
			fullNode.setDepth(currentDepth);
			fullNode.setEmpty(false);
			fullNode.setLeaf(false);

			OctreeNode[][][] children = new OctreeNode[2][2][2];

			int colorAverage = 0;
			for (int x = 0; x < 2; x++)
				for (int y = 0; y < 2; y++)
					for (int z = 0; z < 2; z++) {
						children[x][y][z] = buildOctree(getChildrenAtIndex(x, y, z, structureSubset), currentDepth + 1);

						// build average color for parent node with children
						OctreeNode c = children[x][y][z];
						colorAverage = (int) (c.isEmpty() ? colorAverage : colorAverage == 0 ? c.getColor()
								: (((((colorAverage) ^ (c.getColor())) & 0xfffefefeL) >> 1) + ((colorAverage) & (c.getColor()))));
					}

			fullNode.setChildren(children);
			fullNode.setColor(colorAverage);
			return fullNode;
		}
	}

	/**
	 * Sets neighbor pointers for all children of a node which already has neighbor pointers
	 */
	public void setNeighborPointers(OctreeNodeInterface node) {
		OctreeNodeInterface[][][] children = node.getChildren();

		// Loop to correctly assign neighbors, NULL if no neighbor exists (out of bounding cube)
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					children[x][y][z].setNeighbor(0,
							x == 1 ? children[0][y][z] : node.getNeighbor(0) == null ? null : node.getNeighbor(0).isLeaf() ? node.getNeighbor(0) : node
									.getNeighbor(0).getChild(1, y, z));
					children[x][y][z].setNeighbor(1,
							x == 0 ? children[1][y][z] : node.getNeighbor(1) == null ? null : node.getNeighbor(1).isLeaf() ? node.getNeighbor(1) : node
									.getNeighbor(1).getChild(0, y, z));
					children[x][y][z].setNeighbor(2,
							y == 1 ? children[x][0][z] : node.getNeighbor(2) == null ? null : node.getNeighbor(2).isLeaf() ? node.getNeighbor(2) : node
									.getNeighbor(2).getChild(x, 1, z));
					children[x][y][z].setNeighbor(3,
							y == 0 ? children[x][1][z] : node.getNeighbor(3) == null ? null : node.getNeighbor(3).isLeaf() ? node.getNeighbor(3) : node
									.getNeighbor(3).getChild(x, 0, z));
					children[x][y][z].setNeighbor(4,
							z == 1 ? children[x][y][0] : node.getNeighbor(4) == null ? null : node.getNeighbor(4).isLeaf() ? node.getNeighbor(4) : node
									.getNeighbor(4).getChild(x, y, 1));
					children[x][y][z].setNeighbor(5,
							z == 0 ? children[x][y][1] : node.getNeighbor(5) == null ? null : node.getNeighbor(5).isLeaf() ? node.getNeighbor(5) : node
									.getNeighbor(5).getChild(x, y, 0));
				}
			}
		}

		//set neighbor pointers for all children of non-leaf nodes
		for (int x = 0; x < 2; x++)
			for (int y = 0; y < 2; y++)
				for (int z = 0; z < 2; z++)
					if (!children[x][y][z].isLeaf())
						setNeighborPointers(children[x][y][z]);
	}

	/**
	 * @param structureSubset
	 *            The 3d array of voxels to be tested for being empty
	 * @return true if the 3d array is empty, false otherwise
	 */
	public boolean isEmpty(int[][][] structureSubset) {
		int size = structureSubset.length;

		// loop through all voxels and return false if a non-empty (non-black) one is found
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				for (int k = 0; k < size; k++)
					if (structureSubset[i][j][k] != Color.BLACK.getRGB())
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
	public int[][][] getChildrenAtIndex(int x, int y, int z, int[][][] structureSubset) {

		// determine the size of the array of voxels passed to it and the size of the child array
		int size = structureSubset.length;
		int s2 = size / 2;
		int offsetX = 0, offsetY = 0, offsetZ = 0;

		// make a new smaller array for the child voxels
		int[][][] childrenArray = new int[s2][s2][s2];

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

	public OctreeNodeInterface getRoot() {
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
