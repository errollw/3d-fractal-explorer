package com.erroll;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.erroll.camera.CameraTest;
import com.erroll.fractal.FractalUtilsTest;
import com.erroll.octree.OctreeNodeTest;
import com.erroll.renderer.RayCastUtilsTest;
import com.erroll.renderer.RayTest;

@RunWith(Suite.class)
@SuiteClasses({CameraTest.class, FractalUtilsTest.class, OctreeNodeTest.class, RayCastUtilsTest.class, RayTest.class})
public class AllTests {

}
