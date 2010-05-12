package org.tridas.io;

import java.io.IOException;

import org.grlea.log.SimpleLog;
import org.tridas.io.naming.AbstractNamingConvention;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.util.StringUtils;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

/**
 * This is a default main() class which provides a simple command line interface
 * to the library.  
 * 
 * @author peterbrewer
 *
 */
public class CommandLineUI {
	
	static String asciilogo = "______               _          ______ _ _      _____ _____ \n"+
							  "|  _  \\             | |         |  ___(_) |    |_   _|  _  |\n"+
							  "| | | |___ _ __   __| |_ __ ___ | |_   _| | ___  | | | | | |\n"+
							  "| | | / _ \\ '_ \\ / _` | '__/ _ \\|  _| | | |/ _ \\ | | | | | |\n"+
							  "| |/ /  __/ | | | (_| | | | (_) | |   | | |  __/_| |_\\ \\_/ /\n"+
							  "|___/ \\___|_| |_|\\__,_|_|  \\___/\\_|   |_|_|\\___|\\___/ \\___/ \n"; 

	/**
	 * Basic command line interface to the library
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String inputfilename = null;
		String outputFolder = null;
		String outputFormat = null;
		boolean debug = false;
		String inputFormat = null;
		boolean verbose = false;
		String convention = "uuid";
		IDendroCollectionWriter writer;
		IDendroFileReader reader;
		TridasProject project = null;
		
		// Check number of args
		int index = 0;
		if(args.length == 1){		
			if(args[index].equalsIgnoreCase("-version")){
				showVersion(true);
				return;
			}
			if(args[index].equalsIgnoreCase("-formats")){
				showFormats();
				return;
			}
			else
			{
				showHelp(true);
				return;
			}	
		}
		else if(args.length > 5 || args.length < 3){
			showHelp(true);
			return;
		}
		
		// Loop through all args setting options
		for (String arg : args)
		{
			if (arg.equalsIgnoreCase("-debug"))
			{
				debug = true;
			}
			else if (arg.equalsIgnoreCase("-verbose"))
			{
				verbose = true;
			}
			else if (arg.equalsIgnoreCase("-inputFormat="))
			{
				inputFormat = args[index].substring(13).trim();
			}
			else if (arg.equalsIgnoreCase("-outputFormat="))
			{
				outputFormat = args[index].substring(14).trim();
			}	
			else if (arg.equalsIgnoreCase("-naming="))
			{
				convention = args[index].substring(8).trim();
			}	
		}

		// Set up filenames
		inputfilename = args[args.length-1];
		outputFolder = args[args.length];
		
		// Set debug level to requested
		if(debug){
			// TODO ! figure this out, how do I change log stuff
		}
		
		// Set up reader
		if(inputFormat != null){
			reader = TridasIO.getFileReader(inputFormat);
		}else{
			reader = TridasIO.getFileReaderFromExtension(inputfilename.substring(inputfilename.lastIndexOf(".")+1));
		}
		if(reader == null){
			showHelp(true, "Reader format invalid");
			return;
		}
		
		// Set up writer
		writer = TridasIO.getFileWriter(outputFormat);
		if(writer == null){
			showHelp(true, "Writer format invalid");
			return;
		}

		// Read in File
		try {
			reader.loadFile(inputfilename);
			project = reader.getProject();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidDendroFileException e) {
			e.printStackTrace();
		}

		// Set up naming convention
		INamingConvention namingConvention; 
		if(convention.equalsIgnoreCase("hierarchy"))
		{
			namingConvention = new HierarchicalNamingConvention();
		}
		else
		{
			namingConvention = new UUIDNamingConvention();
		}
		
	    // Write out project
		try {
			writer.setNamingConvention(namingConvention);
			writer.loadProject(project);
			writer.saveAllToDisk(outputFolder);
			
		} catch (IncompleteTridasDataException e) {
			e.printStackTrace();
		} catch (ConversionWarningException e) {
			e.printStackTrace();
		}
		

		// Show warnings if necessary
		if(verbose)
		{
			if(reader.getDefaults() != null){
				if (reader.getDefaults().getConversionWarnings()!=null)
				{
					for(ConversionWarning cw : reader.getDefaults().getConversionWarnings()){
						System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
					}
				}
			}
			
			if(writer.getDefaults() != null){
				if (writer.getDefaults().getConversionWarnings()!=null)
				{
					for(ConversionWarning cw : writer.getDefaults().getConversionWarnings()){
						System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
					}
				}
			}
			
			
			if(writer.getWarnings().size() > 0)
				System.out.println("*** Warnings were thrown while converting your data ***".toUpperCase());
			for(ConversionWarning cw : writer.getWarnings())
				System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
		}
		
		// Grab a list of dendro files that will be written
		IDendroFile[] files = writer.getFiles();
		
		// Show list of output files
		for(IDendroFile f : files)
		{
			System.out.println("File saved to: " + writer.getNamingConvention().getFilename(f));
		}
				
		
	}
		
	private static void showTitle(boolean argLogo){
		if(argLogo){
			System.out.print(CommandLineUI.asciilogo);
		}
		System.out.println(StringUtils.leftPad("ver. "+CommandLineUI.class.getPackage().getImplementationVersion(), 59));
		System.out.println("");
	}
	
	private static void showHelp(boolean argLogo, String error){
		showTitle(argLogo);
		
		if(error!=null)
		{
			System.out.println("Error: "+error);
			System.out.println("");
		}
		
		System.out.println("Usage: [options] inputFilename outputFolder");
		System.out.println("  -debug             - include debug information in output");
		System.out.println("  -formats           - show the list of supported formats and quit");
		System.out.println("  -help              - show this help information");
		System.out.println("  -verbose           - include verbose warnings");
		System.out.println("  -version           - show version information and quit");
		System.out.println("  -naming=convention - either uuid or hierarchy (default is uuid)");
		System.out.println("  -inputFormat=name  - specify input format name (optional)");
		System.out.println("  -outputFormat=name - specify output format name (required)");
		System.out.println("");
	}
	
	private static void showHelp(boolean argLogo){

		showHelp(argLogo, null);
	}
	
	
	private static void showVersion(boolean argLogo){
		System.out.println("Version: "+CommandLineUI.class.getPackage().getImplementationVersion());
	}

	private static void showFormats(){
		showTitle(true);
		System.out.println("Supported reading formats: ");
		for( String format : TridasIO.getSupportedReadingFormats()){
			System.out.println("  -"+format);
		}
		System.out.println("Supported writing formats: ");
		for( String format : TridasIO.getSupportedWritingFormats()){
			System.out.println("  -"+format);
		}
	}
}
