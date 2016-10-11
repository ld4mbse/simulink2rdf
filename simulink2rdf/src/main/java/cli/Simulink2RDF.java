package cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.gatech.mbsec.adapter.simulink.services.OSLC4JSimulinkApplication;

public class Simulink2RDF {

	public static String outputMode;
	public static String simulinkFileLocation;
	public static String rdfFileLocation;
	public static String tdbdir;
	
	
	public static void main(String[] args) {
		// create Options object
		Options options = new Options();

		
		
//		2.1 Generating RDF from an ODX file and save to a file
//
//		XML2RDFClient.exe -xsd input/odx.xsd -xml input/HCU.odx-d -t rdfxml -f output/HCU.odx-d.rdf 
//		
//				
//		2.4 Generating RDF from an ODX file and add RDF into existing TDB
//
//		XML2RDFClient.exe -xsd input/odx.xsd -xml input/HCU.odx-d -t jenatdb -tdbdir <Existing TDB Folder>
		
		
		
//		Options used
//		-slx input/HCU.odx-d		(reference to Simulink file)
//		-t output format (rdfxml or TDB)
//		-f location of output generated rdfxml
//		-tdbdir location of output generated jenatdb
		
		// add t option
		options.addOption("slx", true, "Simulink file location");
		options.addOption("t", true, "output mode (rdfxml or TDB)");
		
		// add c option
		options.addOption("f", true, "generated rdfxml location");
		options.addOption("tdbdir", true, "generated jenatdb location");
		
		options.addOption("help", false, "help");
		
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse( options, args);
			HelpFormatter formatter = new HelpFormatter();
			if(cmd.hasOption("help")) {				
				formatter.printHelp( "simulink2rdf", options );
				return;
			}
			
			if(!cmd.hasOption("slx")) {
				System.err.println("Missing definition of input Simulink file");
				formatter.printHelp( "simulink2rdf", options );
				return;
			}
			if(!cmd.hasOption("t")) {
				System.err.println("Missing definition of output mode (rdfxml or jenatdb)");
				formatter.printHelp( "simulink2rdf", options );
				return;
			}
			
			if(cmd.hasOption("slx")) {
				simulinkFileLocation = cmd.getOptionValue("slx");
				File file = new File(simulinkFileLocation);
				
				if(!file.exists()){
					System.err.println("Invalid location of Simulink model (file does not exist)");
					formatter.printHelp( "simulink2rdf", options );
					return;
				}	
				
				System.out.println("simulinkFileLocation: " + simulinkFileLocation);
			}
			
			
			if(cmd.hasOption("t")) {
				outputMode = cmd.getOptionValue("t");
				if(!(outputMode.equals("rdfxml") | outputMode.equals("jenatdb"))){
					System.err.println("Wrong value of output mode (must be rdfxml or jenatdb)");
					formatter.printHelp( "simulink2rdf", options );
					return;
				}	
				
				System.out.println("outputMode: " + outputMode);
			}
			
			
			
			
			if(outputMode.equals("rdfxml")) {
				if(!cmd.hasOption("f")){
					System.err.println("Missing definition of generated RDF file location");
					formatter.printHelp( "simulink2rdf", options );
					return;
				}
				
				rdfFileLocation = cmd.getOptionValue("f");
				System.out.println("rdfFileLocation: " + rdfFileLocation);
				
			}
			else if(outputMode.equals("jenatdb")) {
				if(!cmd.hasOption("tdbdir")){
					System.err.println("Missing definition of generated TDB folder location");
					formatter.printHelp( "simulink2rdf", options );
					return;
				}
				
				tdbdir = cmd.getOptionValue("tdbdir");
				System.out.println("tdbdir: " + tdbdir);
			}
			
			OSLC4JSimulinkApplication.run();
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
