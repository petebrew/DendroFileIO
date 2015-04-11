/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.NothingToWriteException;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.util.FileHelper;
import org.tridas.io.util.FilePermissionException;
import org.tridas.io.util.IOUtils;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasTridas;

public class CommandLineUI {
	
	static String name = "DendroFileIO";
	static String asciilogo = "______               _          ______ _ _      _____ _____ \n"
			+ "|  _  \\             | |         |  ___(_) |    |_   _|  _  |\n"
			+ "| | | |___ _ __   __| |_ __ ___ | |_   _| | ___  | | | | | |\n"
			+ "| | | / _ \\ '_ \\ / _` | '__/ _ \\|  _| | | |/ _ \\ | | | | | |\n"
			+ "| |/ /  __/ | | | (_| | | | (_) | |   | | |  __/_| |_\\ \\_/ /\n"
			+ "|___/ \\___|_| |_|\\__,_|_|  \\___/\\_|   |_|_|\\___|\\___/ \\___/ \n";
	
	/**
	 * Basic command line interface to the library
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String inputfilename = null;
		String outputFolder = null;
		String outputFormat = null;
		boolean log = false;
		String inputFormat = null;
		boolean verbose = false;
		String convention = "";
		ArrayList<WriterReaderStruct> structs = new ArrayList<WriterReaderStruct>();
		boolean batch = false;
		boolean merge = false;
		String mergelevel = "project";
		
		// Check number of args
		int index = 0;
		if (args.length == 1) {
			if (args[index].equalsIgnoreCase("-version")) {
				showVersion(true);
				return;
			}
			if (args[index].equalsIgnoreCase("-formats")) {
				showFormats();
				return;
			}
		}
		
		if (args.length > 7  || args.length < 2) {
			showHelp(true);
			return;
		}
		
		// Loop through all args setting options
		for (String arg : args) {
			if (arg.equalsIgnoreCase("-log")) {
				log = true;
			}
			else if (arg.equalsIgnoreCase("-verbose")) {
				verbose = true;
			}
			else if (arg.toLowerCase().startsWith("-inputformat=")) {
				inputFormat = arg.substring(13).trim();
			}
			else if (arg.toLowerCase().startsWith("-outputformat=")) {
				outputFormat = arg.substring(14).trim();
			}
			else if (arg.toLowerCase().startsWith("-naming=")) {
				convention = arg.substring(8).trim();
			}
			else if (arg.equalsIgnoreCase("-batch")) {
				batch = true;
			}
			else if (arg.equalsIgnoreCase("-merge-project")) {
				merge = true;
				mergelevel = "project";
				outputFormat = "TRiDaS";
			}
			else if (arg.equalsIgnoreCase("-merge-object")) {
				merge = true;
				mergelevel = "object";
				outputFormat = "TRiDaS";
			}
			else if (arg.startsWith("-")) {
				System.out.println("Unknown arguments: '" + arg + "'");
			}
		}
		
		// Set up filenames
		inputfilename = args[args.length - 2];
		outputFolder = args[args.length - 1];
		
		// Set debug level to requested
		if (log) {
			configureLogFile();
		}
		
		// set up readers
		if (!batch && !merge) {
			// Read in File
			AbstractDendroFileReader reader;
			// Set up reader
			if (inputFormat != null) {
				reader = TridasIO.getFileReader(inputFormat);
			}
			else {
				reader = TridasIO.getFileReaderFromExtension(inputfilename
						.substring(inputfilename.lastIndexOf(".") + 1));
			}
			if (reader == null) {
				showHelp(false, "Reader format invalid");
				return;
			}
			
			try {
				reader.loadFile(inputfilename);
			} catch (IOException e1) {
				System.out.println(e1.toString());
				e1.printStackTrace();
			} catch (InvalidDendroFileException e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			WriterReaderStruct struct = new WriterReaderStruct();
			struct.reader = reader;
			struct.origFilename = inputfilename;
			structs.add(struct);
		}
		else if(batch ){
			// BATCH
			String[] files = getFilesFromFolder(inputfilename);
			for (String file : files) {
				AbstractDendroFileReader reader;
				
				if (inputFormat != null) {
					reader = TridasIO.getFileReader(inputFormat);
				}
				else {
					reader = TridasIO.getFileReaderFromExtension(file.substring(file.lastIndexOf(".") + 1));
				}
				if (reader == null) {
					showHelp(false, "Reader format invalid");
					return;
				}
	
				try {
					reader.loadFile(inputfilename + File.separator + file);
					reader.getTridasContainer();
				} catch (IOException e1) {
					System.out.println(e1.toString());
					e1.printStackTrace();
				} catch (InvalidDendroFileException e) {
					System.out.println(e.toString());
					e.printStackTrace();
				}
				WriterReaderStruct struct = new WriterReaderStruct();
				struct.reader = reader;
				struct.origFilename = file;
				structs.add(struct);
			}
		}
		else
		{
			// MERGE
			String[] files = getFilesFromFolder(inputfilename);
			ArrayList<TridasTridas> containers = new ArrayList<TridasTridas>();
			
			if(files==null || files.length==0) 
			{
				showHelp(false, "No file(s) found in folder "+inputfilename);
				return;
			}
			
			for (String file : files) {
				
				AbstractDendroFileReader reader;
				
				if (inputFormat != null) {
					reader = TridasIO.getFileReader(inputFormat);
				}
				else {
					reader = TridasIO.getFileReaderFromExtension(file.substring(file.lastIndexOf(".") + 1));
				}
				if (reader == null) {
					showHelp(false, "Reader format invalid");
					return;
				}
	
				try {
					reader.loadFile(inputfilename + File.separator + file);
					containers.add(reader.getTridasContainer());
					
					for(ConversionWarning warn : reader.getWarnings())
					{
						System.out.println(warn.getWarningType() + ": "+warn.getMessage());
					}
					
				} catch (IOException e1) {
					System.out.println(e1.toString());
					e1.printStackTrace();
				} catch (InvalidDendroFileException e) {
					System.out.println(e.toString());
					e.printStackTrace();
				}
			}
			
			
			TridasTridas bigcontainer = IOUtils.mergeToSingleProject(containers);
			if(bigcontainer==null) System.out.println("Container is null");
			//TridasUtils.debugTridasStructure(bigcontainer);
			
			TridasWriter writer = new TridasWriter();
			TridasMetadataFieldSet argDefaults = new TridasMetadataFieldSet();
			
			// Write to a temporary TRiDaS file
			File temp = null;
			try {
				writer.parseTridasContainer(bigcontainer, argDefaults);
				temp = File.createTempFile("tmpfolder", "fld");
				temp.delete();
				temp.mkdir();
				try {
					writer.saveAllToDisk(temp.getAbsolutePath());
				} catch (FilePermissionException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				files = getFilesFromFolder(temp.getAbsolutePath());
				
				
				for (String file : files) {
					
					TridasReader reader = new TridasReader();
					
					try {
						reader.loadFile(temp.getAbsolutePath() + File.separator + file);
						reader.getTridasContainer();
					} catch (IOException e1) {
						System.out.println(e1.toString());
						e1.printStackTrace();
					} catch (InvalidDendroFileException e) {
						System.out.println(e.toString());
						e.printStackTrace();
					}
					WriterReaderStruct struct = new WriterReaderStruct();
					struct.reader = reader;
					struct.origFilename = inputfilename;
					structs.add(struct);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (NothingToWriteException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} finally {
				temp.delete();
			}

			

			
			
		}
		
		// set up writers
		for (WriterReaderStruct s : structs) {
			AbstractDendroCollectionWriter writer;
			if (outputFormat != null) {
				writer = TridasIO.getFileWriter(outputFormat);
			}
			else {
				writer = new TridasWriter();
			}
			if (writer == null) {
				showHelp(false, "Writer format invalid: " + outputFormat);
				return;
			}
			
			// Set up naming convention
			INamingConvention namingConvention = null;
			if (convention.equalsIgnoreCase("hierarchy")) {
				namingConvention = new HierarchicalNamingConvention();
			}
			else if (convention.equalsIgnoreCase("uuid")) {
				namingConvention = new UUIDNamingConvention();
			}
			/*else {
				namingConvention = new NumericalNamingConvention(s.origFilename.substring(0, s.origFilename
						.lastIndexOf(".")));
			}*/
			
			// Write out project
			try {
				if(namingConvention!=null)
				{
					writer.setNamingConvention(namingConvention);
				}
				writer.load(s.reader.getTridasContainer());
								
				writer.saveAllToDisk(outputFolder);
			} catch (ImpossibleConversionException e) {
				System.out.println(e.toString());
				e.printStackTrace();
			} catch (ConversionWarningException e) {
				System.out.println(e.toString());
			} catch (NothingToWriteException e) {
				System.out.println(e.toString());
				e.printStackTrace();
			} catch (FilePermissionException e) {
				System.out.println(e.toString());
				e.printStackTrace();
			} 
			s.writer = writer;
		}
		
		// Show warnings if necessary
		if (verbose) {
			for (WriterReaderStruct struct : structs) {
				AbstractDendroFileReader reader = struct.reader;
				AbstractDendroCollectionWriter writer = struct.writer;
				
				System.out.println();
				System.out.println("--- File '" + struct.origFilename + "'" + " ---");
				System.out.println("Reader warnings thrown:");
				
				if (reader.getDefaults() != null) {
					if (reader.getDefaults().getWarnings() != null) {
						for (ConversionWarning cw : reader.getDefaults().getWarnings()) {
							System.out.println("  - [" + cw.getWarningType().toString() + "]: " + cw.getMessage());
						}
					}
				}
				
				for (ConversionWarning cw : reader.getWarnings()) {
					System.out.println("  - [" + cw.getWarningType().toString() + "]: " + cw.getMessage());
				}
				System.out.println("Writer warnings thrown:");
				if (writer.getDefaults() != null) {
					if (writer.getDefaults().getWarnings() != null) {
						for (ConversionWarning cw : writer.getDefaults().getWarnings()) {
							System.out.println("  - [" + cw.getWarningType().toString() + "]: " + cw.getMessage());
						}
					}
				}
				
				for (ConversionWarning cw : writer.getWarnings()) {
					System.out.println("  - [" + cw.getWarningType().toString() + "]: " + cw.getMessage());
				}
				System.out.println("--------------------------");
			}
		}
		
		System.out.println("Files saved:");
		for (WriterReaderStruct s : structs) {
			AbstractDendroCollectionWriter writer = s.writer;
			IDendroFile[] files = writer.getFiles();
			
			// Show list of output files
			for (IDendroFile f : files) {
				System.out.println(s.origFilename + " --> '" + outputFolder+File.separator+writer.getNamingConvention().getFilename(f) + "."
						+ f.getExtension() + "'");
			}
		}
	}
	
	private static void showTitle() {
		System.out.print(CommandLineUI.asciilogo);
		System.out.println(name
				+ StringUtils.leftPad("ver. " + CommandLineUI.class.getPackage().getImplementationVersion(), 49));
		System.out.println();
	}
	
	private static void showHelp(boolean argTitle, String error) {
		if (argTitle) {
			showTitle();
		}
		
		if (error != null) {
			System.out.println("Error: " + error);
			System.out.println("");
		}
		
		System.out.println("Usage: [options] inputFilename outputFolder");
		System.out.println("       [options] -batch inputFolder outputFolder");
		System.out.println("       -merge-project -inputFormat=sheffield inputFolder outputFolder");
		System.out.println("  -log               - log all errors to file");
		System.out.println("  -formats           - show the list of supported formats and quit");
		System.out.println("  -help              - show this help information");
		System.out.println("  -verbose           - include verbose warnings");
		System.out.println("  -version           - show version information and quit");
		System.out.println("  -naming=convention - either basic, uuid or hierarchy (default is basic)");
		System.out.println("  -inputFormat=name  - specify input format name");
		System.out.println("  -outputFormat=name - specify output format name (default is Tridas)");
		System.out.println("  -batch             - loads all files in a folder");
		System.out.println("");
		System.out.println("The following options are experimental:");
		System.out.println("  -merge-project     - all input files treated as if from a single project");
		//System.out.println("  -merge-object      - all input files treated as if from a single object");
		System.out.println("");
	}
	
	private static void showHelp(boolean argTitle) {
		showHelp(argTitle, null);
	}
	
	private static void showVersion(boolean argLogo) {
		System.out.println(name + " version: " + CommandLineUI.class.getPackage().getImplementationVersion());
	}
	
	private static void showFormats() {
		System.out.println("Supported reading formats: ");
		for (String format : TridasIO.getSupportedReadingFormats()) {
			System.out.println("  -" + format);
		}
		System.out.println("Supported writing formats: ");
		for (String format : TridasIO.getSupportedWritingFormats()) {
			System.out.println("  -" + format);
		}
	}
	
	private static void configureLogFile() {
		String simplelog = "simplelog.properties";
		FileHelper fh = new FileHelper();
		
		File propFile = fh.createFile(simplelog);
		Properties logProperties = new Properties();
		
		if (propFile.exists()) {
			try {
				logProperties.load(IOUtils.createInput(propFile));
			} catch (IOException e) {
				System.out.println("Error loading log file, creating new one.");
			}
			propFile.delete();
		}
		
		try {
			propFile.createNewFile();
		} catch (IOException e1) {
			System.out.println("Error creating properties log properties file.");
		}
		
		if (!logProperties.contains("simplelog.logFile")) {
			logProperties.put("simplelog.logFile", "DendroIOLog.txt");
			System.out.println("Saving error log to DendroIOLog.txt");
		}
		else {
			System.out.println("Saving error log to " + logProperties.getProperty("simplelog.logFile"));
		}
		
		if (!logProperties.contains("simplelog.defaultLevel")) {
			logProperties.put("simplelog.defaultLevel", "Error");
		}
		
		try {
			logProperties.store(IOUtils.createOutput(propFile),
					"For custom log properties, see https://simple-log.dev.java.net/");
		} catch (IOException e) {
			System.out.println("Error storing log properties to file");
		}
	}
	
	private static String[] getFilesFromFolder(String folder) {
		File dir = new File(folder);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}
		};
		return dir.list(filter);
	}
	
	private static class WriterReaderStruct {
		AbstractDendroFileReader reader;
		AbstractDendroCollectionWriter writer;
		String origFilename;
	}
}
