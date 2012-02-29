package com.erroll.properties;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Parameters {

	/**
	 * load properties from file "parameters.properties" into props object to be passed on during initializations
	 * 
	 * @return The loaded properties file
	 */
	public static final Properties get() {
		Properties p = new Properties();
		try {
			p.load(new BufferedReader(new FileReader("parameters.properties")));
		} catch (FileNotFoundException e1) {
			System.out.println("FILE NOT FOUND");
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return p;
	}
}
