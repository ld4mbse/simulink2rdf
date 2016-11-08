/*********************************************************************************************
 * Copyright (c) 2016 Model-Based Systems Engineering Center, Georgia Institute of Technology.
 *                         http://www.mbse.gatech.edu/
 *                  http://www.mbsec.gatech.edu/research/oslc
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Eclipse Distribution License is available at
 *  http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *  Contributors:
 *
 *	   Axel Reichwein, Koneksys (axel.reichwein@koneksys.com)		
 *******************************************************************************************/
package edu.gatech.mbsec.adapter.simulink.matlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import edu.gatech.mbsec.adapter.simulink.services.OSLC4JSimulinkApplication;

/**
 * Simulink2XMIThread2 is responsible for converting the information contained
 * in several Simulink models into an XMI file which can then be easily parsed
 * and read by the OSLC Simulink adapter.
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
public class Simulink2XMIThread2 extends Thread {

	public static boolean loadedFromJar = false; // = java or jar

	public void run() {

		// necessary to add that folder to the Matlab path
		// File file = new File(simulinkModelPath);
		// String folderPath = file.getParentFile().getAbsolutePath();
		
		// finding out if program has been launched from jar
		URL classURL = Simulink2XMIThread2.class.getResource("Simulink2XMIThread2.class");
		if(classURL.toString().startsWith("jar")){
			loadedFromJar = true;
		}

		long startTime = System.currentTimeMillis();
		// Execute Matlab from the command prompt
		try {
			// Process process = Runtime
			// .getRuntime()
			// .exec("matlab start /wait "
			// + "-nodisplay -nosplash -nodesktop -r " +
			// "addpath('" + folderPath + "');addpath
			// ('matlab');simulinkModel2xmi('" + simulinkModelPath +
			// "');exit;");

			String[] simulinkModelsPathArray = OSLC4JSimulinkApplication.simulinkModelPaths.split(",");

			String argumentString = "";
			Set<String> pathsAddedToMatlabWorkspacePath = new HashSet<String>();
			for (String simulinkModelPath : simulinkModelsPathArray) {
				File file = new File(simulinkModelPath);
				String folderPath = file.getParentFile().getAbsolutePath();
				if (!pathsAddedToMatlabWorkspacePath.contains(folderPath)) {
					argumentString = argumentString + "addpath('" + folderPath + "');";
					pathsAddedToMatlabWorkspacePath.add(folderPath);
				}

			}
			
			// add path to folder containing the matlab script (possibly
			// extracted from the jar)
			// if(runMode.equals("jar")){

			// in jar mode, matlab script is currently expected to be in
			// target/matlab folder
			// in java mode, matlab script is found under {current location =
			// root folder of Eclipse project}/matlab

			// currently, both java and jar mode work but there are exceptions
			// exception in java mode: a path is added to matlab workspace which
			// is not valid? (not a path set manually in matlab! not set from
			// within Eclipse)
			// exception in jar mode: it is trying to find the matlab script
			// under target/matlab?

			// this addPath command will trigger an exception in jar mode (as it
			// does not exist in jar model - but it will not break the
			// conversion)
			// only necessary in java mode
			if(!loadedFromJar){
				String currentDir = System.getProperty("user.dir");
				argumentString = argumentString + "addpath('" + currentDir + "\\matlab');";
				// or is it necessary to add the parent path?
				System.out.println("System.getProperty(\"user.dir\"): " + System.getProperty("user.dir"));
				if (!pathsAddedToMatlabWorkspacePath.contains(currentDir)) {
					argumentString = argumentString + "addpath('" + currentDir + "');";
					pathsAddedToMatlabWorkspacePath.add(currentDir);
				}
			}
			

			// }

			// when running jar, the matlab script needs to exist outside the
			// jar, else it cannot be found by the cmd bash script.
			// if(runMode.equals("jar")){
			// get the current absolute path
			String folderContainingJarPath = Paths.get(".").toAbsolutePath().normalize().toString();
			// add it to the matlab workspace

			// only necessary in jar mode
			if(loadedFromJar){
				if (!pathsAddedToMatlabWorkspacePath.contains(folderContainingJarPath)) {
					argumentString = argumentString + "addpath('" + folderContainingJarPath + "');";
					pathsAddedToMatlabWorkspacePath.add(folderContainingJarPath);
				}
			}
			

			// only necessary in jar mode
			// only have simulinkModels2xmi script as external resource
			// copy it from the jar and place it outside the jar
			if(loadedFromJar){
				InputStream inputStream = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("matlab/simulinkModels2xmi.m");
				// write the inputStream to a FileOutputStream
				OutputStream outputStream = new FileOutputStream(
						new File(folderContainingJarPath + "/simulinkModels2xmi.m"));
				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				outputStream.close();
			}
			
			// }
			// else{
			//// argumentString = argumentString + "addpath('matlab');";
			//
			// // do a test in regular java mode to see if it works
			// // this is also useful in regular java/Eclipse mode as the
			//
			// // get the current absolute path
			// String folderContainingJarPath =
			// Paths.get(".").toAbsolutePath().normalize().toString();
			// // add it to the matlab workspace
			// argumentString = argumentString + "addpath('" +
			// folderContainingJarPath + "');";
			// // only have simulinkModels2xmi script as external resource
			// // copy it from the jar and place it outside the jar
			// InputStream inputStream =
			// Thread.currentThread().getContextClassLoader().getResourceAsStream("matlab\\simulinkModels2xmi.m");
			// // write the inputStream to a FileOutputStream
			// OutputStream outputStream =
			// new FileOutputStream(new File(folderContainingJarPath
			// +"\\simulinkModels2xmi.m"));
			// int read = 0;
			// byte[] bytes = new byte[1024];
			//
			// while ((read = inputStream.read(bytes)) != -1) {
			// outputStream.write(bytes, 0, read);
			// }
			//
			// }

			argumentString = argumentString + "simulinkModels2xmi({";

			int i = 0;
			for (String simulinkModelPath : simulinkModelsPathArray) {
				if (i > 0) {
					argumentString = argumentString + ",";
				}
				argumentString = argumentString + "'" + simulinkModelPath + "'";
				i++;
			}			
			argumentString = argumentString + "});";

			// simulinkModels2xmi({'C:\Users\rb16964\git\simulink2rdf\simulink2rdf\simulinkmodels\sldemo_househeat.slx'
			// 'C:\Users\rb16964\git\simulink2rdf\simulink2rdf\simulinkmodels\TwoDOFRobotDynCon.slx'})

			// "addpath('" + folderPath +
			// "');addpath('matlab');simulinkModel2xmi('" + simulinkModelPath +
			// "');

			// Process process = Runtime
			// .getRuntime()
			// .exec("matlab start /wait "
			// + "-nodisplay -nosplash -nodesktop -r " +
			// "addpath('" + folderPath +
			// "');addpath('matlab');simulinkModel2xmi('" + simulinkModelPath +
			// "');exit;");

			Process process = Runtime.getRuntime()
					.exec("matlab start /wait " + "-nodisplay -nosplash -nodesktop -r " + argumentString + "exit;");

			process.waitFor();
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			System.out.println("Simulink -> RDF Conversion in " + (duration / 1000) + " seconds");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
