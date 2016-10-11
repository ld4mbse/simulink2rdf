/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *  
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *
 *     Michael Fiedler     - initial API and implementation for Bugzilla adapter
 *     
 *     Axel Reichwein	   - implementation for Simulink adapter and Simulink to RDF conversion tool (axel.reichwein@koneksys.com)
 *     
 *******************************************************************************/
package edu.gatech.mbsec.adapter.simulink.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.xerces.util.URI;
import edu.gatech.mbsec.adapter.simulink.resources.Constants;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkBlock;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkElementsToCreate;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkInputPort;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkLine;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkModel;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkOutputPort;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkParameter;

import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;

import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
import org.eclipse.lyo.oslc4j.provider.json4j.Json4JProvidersRegistry;

import com.opencsv.CSVReader;

import cli.Simulink2RDF;
import edu.gatech.mbsec.adapter.simulink.application.SimulinkManager;

/**
 * OSLC4JSimulinkApplication registers all entity providers for converting POJOs
 * into RDF/XML, JSON and other formats. OSLC4JSimulinkApplication registers
 * also registers each servlet class containing the implementation of OSLC
 * RESTful web services.
 * 
 * OSLC4JSimulinkApplication also reads the user-defined configuration file with
 * loadPropertiesFile(). This is done at the initialization of the web
 * application, for example when the first resource or service of the OSLC
 * Simulink adapter is requested.
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
public class OSLC4JSimulinkApplication {

	public static String simulinkModelPath = null;
	public static String portNumber = null;
	public static boolean syncWithSvnRepo = false;
	public static String svnurl = null;
	public static int delayInSecondsBetweenDataRefresh = 100000;
	public static boolean useIndividualSubversionFiles = false;

	public static String svnUserName;
	public static String svnPassword;

	// public static String configFilePath =
	// "oslc4jsimulink configuration/config.properties";
	// public static String configFilePath = "configuration/config.properties";
	// public static String configFilePath =
	// "C:/Users/Axel/Desktop/apache-tomcat-7.0.59/configuration/config.properties";
	public static String warConfigFilePath = "../oslc4jsimulink configuration/config.properties";
	public static String localConfigFilePath = "oslc4jsimulink configuration/config.properties";
	public static String configFilePath = null;
	public static String warSVNURLsFilePath = "../oslc4jsimulink configuration/subversionfiles.csv";
	public static String localSVNURLsFilePath = "oslc4jsimulink configuration/subversionfiles.csv";
	public static String svnURLsFilePath = null;
	public static ArrayList<String> subversionFileURLs;

	public static void main(String[] args) {

		loadPropertiesFile();

		String simulinkFilePath = OSLC4JSimulinkApplication.simulinkModelPath;
		File file = new File(simulinkFilePath);
		String simulinkFileName = file.getName();
		simulinkFileName = simulinkFileName.replace(".slx", ".rdf");
		Simulink2RDF.rdfFileLocation = simulinkFileName;

		readDataFirstTime();

		Simulink2RDF.outputMode = "rdfxml";
		SimulinkManager.writeRDFtoXML();

	}

	public static void run() {

		loadPropertiesFile2();

		readDataFirstTime();

		SimulinkManager.writeRDFtoXML();

	}

	private static void loadPropertiesFile() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			// loading properties file
			// input = new FileInputStream("./configuration/config.properties");
			input = new FileInputStream(warConfigFilePath); // for war file
			configFilePath = warConfigFilePath;
		} catch (FileNotFoundException e) {
			try {
				input = new FileInputStream(localConfigFilePath);
				configFilePath = localConfigFilePath;
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} // for war file
		}

		// load property file content and convert backslashes into forward
		// slashes
		String str;
		if (input != null) {
			try {
				str = readFile(configFilePath, Charset.defaultCharset());
				prop.load(new StringReader(str.replace("\\", "/")));

				// get the property value

				simulinkModelPath = prop.getProperty("simulinkModel");
				portNumber = prop.getProperty("portNumber");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

	private static void loadPropertiesFile2() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			// loading properties file
			// input = new FileInputStream("./configuration/config.properties");
			input = new FileInputStream(warConfigFilePath); // for war file
			configFilePath = warConfigFilePath;
		} catch (FileNotFoundException e) {
			try {
				input = new FileInputStream(localConfigFilePath);
				configFilePath = localConfigFilePath;
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} // for war file
		}

		// load property file content and convert backslashes into forward
		// slashes
		String str;
		if (input != null) {
			try {
				str = readFile(configFilePath, Charset.defaultCharset());
				prop.load(new StringReader(str.replace("\\", "/")));

				// get the property value

				// simulinkModelPath = prop.getProperty("simulinkModel");
				simulinkModelPath = Simulink2RDF.simulinkFileLocation;

				portNumber = prop.getProperty("portNumber");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

	public static ArrayList<String> readSVNFileURLs(List<String[]> allElements) {
		List<String> svnurls = new ArrayList<String>();

		for (String[] element : allElements) {
			if ((element.length == 1)) {
				svnurls.add(element[0]);
			}
		}

		ArrayList<String> subversionFileURLs = new ArrayList<String>();
		// for (String subversionFileURL : SubversionFileURLsFromUserArray) {
		for (String subversionFileURL : svnurls) {
			// make sure to delete possible space character
			if (subversionFileURL.startsWith(" ")) {
				subversionFileURL = subversionFileURL.substring(1, subversionFileURL.length());
			}
			if (subversionFileURL.endsWith(" ")) {
				subversionFileURL = subversionFileURL.substring(0, subversionFileURL.length() - 1);
			}

			try {
				// make sure that URL is valid
				new URL(subversionFileURL);

				// make sure that url is not a duplicate
				if (!subversionFileURLs.contains(subversionFileURL)) {
					subversionFileURLs.add(subversionFileURL);
				}

			} catch (Exception e) {

			}
		}
		return subversionFileURLs;
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	public static void readDataFirstTime() {
		Thread thread = new Thread() {
			public void start() {

				reloadSimulinkModels();
			}
		};
		thread.start();
		try {
			thread.join();
			System.out.println("Simulink to RDF conversion finished.");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static void reloadSimulinkModels() {

		SimulinkManager.simulinkWorkingDirectory = null; // to reload
															// models
		SimulinkManager.loadSimulinkWorkingDirectory();

	}
}
