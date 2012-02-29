package com.erroll.properties;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

public class ParametersTest {

	@Test
	public void testGet() {
		Properties props = Parameters.get();

		// assert getting basic property of size works
		assertTrue(Integer.parseInt(props.getProperty("SCREEN_SIZE")) > 0);
	}

}
