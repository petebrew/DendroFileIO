package org.tridas.io.maventests;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.TestCase;

import org.grlea.log.SimpleLogger;
import org.tridas.io.formats.trims.TrimsWriter;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

public class TestBetweenFormats extends TestCase {
	
	private static final SimpleLogger log = new SimpleLogger(TestBetweenFormats.class);
	private static final String outputLocation = "target/TestOutput";
	
	private String[] getFilesFromFolder(String folder) {
		File dir = new File(folder);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}
		};
		return dir.list(filter);
	}
	
	public void testTucsonToTrims() {
		String folder = "TestData/Tucson";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			TucsonReader reader = new TucsonReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename, new TucsonToTridasDefaults());
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (IncorrectDefaultFieldsException e) {
				// The default fields you gave were wrong
				e.printStackTrace();
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			TrimsWriter writer = new TrimsWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.loadProject(myproject);
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {}
			writer.saveAllToDisk(outputLocation);
			
		}
	}
}
