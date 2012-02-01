package com.erroll.metrics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MetricsTest {

	Metrics tester;

	@Before
	public void setUp() throws Exception {
		// create the testing object
		tester = new Metrics();
	}

	@Test
	public void testRegisterFrameRender() throws InterruptedException {
		// register a frame being rendered [How to test this effectively?]
		tester.registerFrameRender();
		// ensure the FPS recorded is still 0 as 1 second has not yet passed
		assertTrue("FPS recorded at wrong time", tester.getFps() == 0);
	}

	@Test
	public void testGetDataString() {
		// check getting data that hasn't been stored gives Integer.MIN_VALUE
		assertTrue("iterations0 stored when nothing should have been", tester.getData("iterations0") == Double.MIN_VALUE);

		// increment arbitrary fields in the metrics table
		for (int i = 0; i < 2; i++)
			tester.incrementData("iterations1", 2);
		for (int i = 0; i < 5; i++)
			tester.incrementData("iterations2", 1);

		// test they have been stored correctly
		assertTrue("iterations1 incremented wrong number of times", tester.getData("iterations1") == 4);
		assertTrue("iterations2 incremented wrong number of times", tester.getData("iterations2") == 5);
	}
}
