package org.tridas.io.maventests;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.formats.belfastapple.BelfastAppleReader;
import org.tridas.io.formats.belfastarchive.BelfastArchiveReader;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.csv.CSVWriter;
import org.tridas.io.formats.heidelberg.HeidelbergReader;
import org.tridas.io.formats.sheffield.SheffieldReader;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.trims.TrimsReader;
import org.tridas.io.formats.trims.TrimsWriter;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.formats.vformat.VFormatReader;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.naming.SeriesNamingConvention;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

import junit.framework.TestCase;

public class TestToTridas extends TestCase {
	
	private static final SimpleLogger log = new SimpleLogger(TestToTridas.class);
	private static final String outputLocation = "target/TestOutput";
	
	private String[] getFilesFromFolder(String folder){
		File dir = new File(folder);
		FilenameFilter filter = new FilenameFilter() 
		{ 
			public boolean accept(File dir, String name) 
			{ 
				return !name.startsWith("."); 
			} 
		}; 
		return dir.list(filter);	
	}
	
	public void testTucsonToTridas() 
	{
		String folder = "TestData/Tucson";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			log.info("Test conversion of: "+filename);
			
			// Create a new converter
			TucsonReader reader = new TucsonReader();

			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
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
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.loadProject(myproject, new TridasMetadataFieldSet());
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {
			} catch (IncorrectDefaultFieldsException e) {
				fail();
			}
			writer.saveAllToDisk(outputLocation);
		

		}
		
		
	}
	
	
	public void testCatrasToTridas() 
	{
		String folder = "TestData/CATRAS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			log.info("Test conversion of: "+filename);
			
			// Create a new converter
			CatrasReader reader = new CatrasReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
				return;
			}
			
			
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			
				
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.loadProject(myproject);
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {
			}
			writer.saveAllToDisk(outputLocation);

		}
	}
	
	public void testHeidelbergToTridas(){
		
		String folder = "TestData/Heidelberg";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			log.info("Test conversion of: "+filename);
		
		
			HeidelbergReader reader = new HeidelbergReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
				//reader.loadFile("TestData/Heidelberg", "UAKK0530.fh");
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.loadProject(myproject);
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {
			}
			writer.saveAllToDisk("target/TestOutput/");

		}
		
	}
	
	public void testTrimsToTridas() 
	{
		String folder = "TestData/TRIMS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			log.info("Test conversion of: "+filename);
			
			// Create a new converter
			TrimsReader reader = new TrimsReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			
				
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.loadProject(myproject);
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {
			}
			writer.saveAllToDisk(outputLocation);
			
		}
		
	}
	
	public void testSheffieldToTridas() 
	{
		String folder = "TestData/Sheffield";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			//if(!filename.equals("yhg50683.d")) continue;
			log.info("Test conversion of: "+filename);
		
			// Create a new converter
			SheffieldReader reader = new SheffieldReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			
				
			TridasWriter writer = new TridasWriter();			
			try {
				writer.setNamingConvention(new NumericalNamingConvention(filename.substring(0, filename.lastIndexOf("."))));
				writer.loadProject(myproject);
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {
			}
			writer.saveAllToDisk(outputLocation);
			
		}
		
	}
	
	public void testBelfastAppleToTridas() 
	{
		String folder = "TestData/BelfastApple";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			log.info("Test conversion of: "+filename);		
		
			// Create a new converter
			BelfastAppleReader reader = new BelfastAppleReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			
				
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.loadProject(myproject);
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {
			}
			writer.saveAllToDisk(outputLocation);
			
		}
		
	}
	
	public void testBelfastArchiveToTridas() 
	{
		String folder = "TestData/BelfastArchive";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			log.info("Test conversion of: "+filename);		
		
			// Create a new converter
			BelfastArchiveReader reader = new BelfastArchiveReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			
				
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.loadProject(myproject);
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {
			}
			writer.saveAllToDisk(outputLocation);
			
		}
		
	}
	
	public void testVFormatToTridas() 
	{
		String folder = "TestData/VFormat";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			log.info("Test conversion of: "+filename);	
		
			// Create a new converter
			VFormatReader reader = new VFormatReader();
	
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
				} catch (IOException e) {
					// Standard IO Exception
					log.info(e.getLocalizedMessage());
					return;
				} catch (InvalidDendroFileException e) {
					// Fatal error interpreting file
					log.info(e.getLocalizedMessage());
					return;
				}
						
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			
				
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.loadProject(myproject, new TridasMetadataFieldSet());
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {
			} catch (IncorrectDefaultFieldsException e) {
				fail();
			}
			writer.saveAllToDisk(outputLocation);
		
			
		}
		
		
		
	}
}