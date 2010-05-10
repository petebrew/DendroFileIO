package org.tridas.io;

import java.io.IOException;

import org.grlea.log.SimpleLog;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

public class ConvertFile {
	
	static String asciilogo = " ___   ____  _      ___   ___   ___       _   ___  \n"+
							  "| | \\ | |_  | |\\ | | | \\ | |_) / / \\     | | / / \\ \n"+
							  "|_|_/ |_|__ |_| \\| |_|_/ |_| \\ \\_\\_/     |_| \\_\\_/ \n";
	
	
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
		
		IDendroCollectionWriter writer;
		IDendroFileReader reader;
		TridasProject project = null;
		
		// Check we have the right number of args
		if(args.length > 5 || args.length < 3){
			showHelp(true);
			return;
		}
		
		int index = 0;
		if(args[index].equalsIgnoreCase("-debug")){
			debug = true;
			index++;
		}
		if(args[index].startsWith("-")){
			inputFormat = args[index].substring(1);
			index++;
		}
		
		// Set up filenames
		inputfilename = args[index];
		outputFormat = args[index+1];
		outputFolder = args[index+2];
		
		if(debug){
			// TODO ! figure this out, how do I change log stuff
		}
		
		if(inputFormat != null){
			reader = TridasIO.getFileReader(inputFormat);
		}else{
			reader = TridasIO.getFileReaderFromExtension(inputfilename.substring(inputfilename.lastIndexOf(".")+1));
		}
		if(reader == null){
			System.out.println("Could not find reader");
			showHelp(false);
			return;
		}
		
		writer = TridasIO.getFileWriter(outputFormat);
		if(writer == null){
			System.out.println("Could not find writer");
			showHelp(false);
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
		
		if(reader.getDefaults() != null){
			for(ConversionWarning cw : reader.getDefaults().getConversionWarnings()){
				System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
			}
		}
		
		
	    // Write out project
		try {
			writer.setNamingConvention(new UUIDNamingConvention());
			writer.loadProject(project);
			writer.saveAllToDisk(outputFolder);
			
		} catch (IncompleteTridasDataException e) {
			e.printStackTrace();
		} catch (ConversionWarningException e) {
			e.printStackTrace();
		} catch (IncorrectDefaultFieldsException e) {
			e.printStackTrace();
		}
		
		if(writer.getDefaults() != null){
			for(ConversionWarning cw : reader.getDefaults().getConversionWarnings()){
				System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
			}
		}
		
		
		
		// Show warnings if necessary
		if(writer.getWarnings().size() > 0)
			System.out.println("*** Warnings were thrown while converting your data ***".toUpperCase());
		for(ConversionWarning cw : writer.getWarnings())
			System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
		
		// Grab a list of dendro files that will be written
		DendroFile[] files = writer.getFiles();
		
		// Show list of output files
		for(DendroFile f : files)
		{
			System.out.println("File saved to: " + writer.getNamingConvention().getFilename(f));
		}
				
		
	}
	
	private static void showHelp(boolean argLogo){
		if(argLogo){
			System.out.print(ConvertFile.asciilogo);
		}
		System.out.println("");
		System.out.println("Dendro File Converter:");
		System.out.println("Usage: [-debug] [-inputFormatName] inputFilename outputFormatName outputFolder");
		System.out.println("Supported reading formats: ");
		for( String format : TridasIO.getSupportedReadingFormats()){
			System.out.println("  "+format);
		}
		System.out.println("Supported writing formats: ");
		for( String format : TridasIO.getSupportedWritingFormats()){
			System.out.println("  "+format);
		}
	}

}
