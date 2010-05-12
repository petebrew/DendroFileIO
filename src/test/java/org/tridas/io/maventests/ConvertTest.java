package org.tridas.io.maventests;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.formats.belfastapple.BelfastAppleReader;
import org.tridas.io.formats.belfastarchive.BelfastArchiveReader;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.heidelberg.HeidelbergReader;
import org.tridas.io.formats.sheffield.SheffieldReader;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.trims.TrimsReader;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.formats.vformat.VFormatReader;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

import junit.framework.TestCase;

public class ConvertTest extends TestCase {

	public void test() 
	{
		String folder = "TestData/Tucson";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			if (!filename.equals("Tucson7.rwl")) continue;
			
			System.out.println("Test conversion of: "+filename);
			
			TridasProject project = null;
		    
			TucsonReader reader = new TucsonReader();
			try {
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				System.out.println(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				e.printStackTrace();
				fail();
			}
			
			// Extract the TridasProject
			project = reader.getProject();
	
			// Create a new converter based on a TridasProject
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			try {
				writer.loadProject(project);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
				e.printStackTrace();
			}
	
	
			// Actually save file(s) to disk
			writer.saveAllToDisk("TestData/Output");
		}

	}
	
	
	public void testTridasToTucson() 
	{
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			System.out.println("Test conversion of: "+filename);
			
			TridasProject project = null;
		    
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				System.out.println(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				e.printStackTrace();
				fail();
			}
			
			// Extract the TridasProject
			project = reader.getProject();
	
			// Create a new converter based on a TridasProject
			TucsonWriter tucsonwriter = new TucsonWriter();
			tucsonwriter.setNamingConvention(new UUIDNamingConvention());
			try {
				tucsonwriter.loadProject(project);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
				e.printStackTrace();
			}
	
	
			// Actually save file(s) to disk
			tucsonwriter.saveAllToDisk("TestData/Output");
		}

	}
	
	private String[] getFilesFromFolder(String folder)
	{
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
			System.out.println("Test conversion of: "+filename);
			
			// Create a new converter
			TucsonReader reader = new TucsonReader();

			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename, new TucsonToTridasDefaults());
			} catch (IOException e) {
				// Standard IO Exception
				System.out.println(e.getLocalizedMessage());
				fail();
			} catch (IncorrectDefaultFieldsException e) {
				// The default fields you gave were wrong
				e.printStackTrace();
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				System.out.println(e.getLocalizedMessage());
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
			writer.saveAllToDisk("TestData/Output");
		

		}
		
		
	}
	
	
	public void testCatrasToTridas() 
	{
		String folder = "TestData/CATRAS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			System.out.println("Test conversion of: "+filename);
			
			// Create a new converter
			CatrasReader reader = new CatrasReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				System.out.println(e.getLocalizedMessage());
				fail();
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				System.out.println(e.getLocalizedMessage());
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
			writer.saveAllToDisk("TestData/Output");

		}
	}
	
	public void testHeidelbergToTridas(){
		
		String folder = "TestData/Heidelberg";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			System.out.println("Test conversion of: "+filename);
		
		
			HeidelbergReader reader = new HeidelbergReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
				//reader.loadFile("TestData/Heidelberg", "UAKK0530.fh");
			} catch (IOException e) {
				// Standard IO Exception
				System.out.println(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				System.out.println(e.getLocalizedMessage());
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
			writer.saveAllToDisk("TestData/Output/");

		}
		
	}
	
	public void testTrimsToTridas() 
	{
		String folder = "TestData/TRIMS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			System.out.println("Test conversion of: "+filename);
			
			// Create a new converter
			TrimsReader reader = new TrimsReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				System.out.println(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				System.out.println(e.getLocalizedMessage());
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
			writer.saveAllToDisk("TestData/Output");
			
		}
		
	}
	
	public void testSheffieldToTridas() 
	{
		String folder = "TestData/Sheffield";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			System.out.println("Test conversion of: "+filename);
		
			// Create a new converter
			SheffieldReader reader = new SheffieldReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				System.out.println(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				System.out.println(e.getLocalizedMessage());
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
			writer.saveAllToDisk("TestData/Output");
			
		}
		
	}
	
	public void testBelfastAppleToTridas() 
	{
		String folder = "TestData/BelfastApple";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			System.out.println("Test conversion of: "+filename);		
		
			// Create a new converter
			BelfastAppleReader reader = new BelfastAppleReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				System.out.println(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				System.out.println(e.getLocalizedMessage());
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
			writer.saveAllToDisk("TestData/Output");
			
		}
		
	}
	
	public void testBelfastArchiveToTridas() 
	{
		String folder = "TestData/BelfastArchive";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			System.out.println("Test conversion of: "+filename);		
		
			// Create a new converter
			BelfastArchiveReader reader = new BelfastArchiveReader();
			
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				System.out.println(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				System.out.println(e.getLocalizedMessage());
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
			writer.saveAllToDisk("TestData/Output");
			
		}
		
	}
	
	public void testVFormatToTridas() 
	{
		String folder = "TestData/VFormat";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length==0) fail();
		
		for (String filename : files)
		{	
			System.out.println("Test conversion of: "+filename);	
		
			// Create a new converter
			VFormatReader reader = new VFormatReader();
	
			// Parse the legacy data file
			try {
				//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
				} catch (IOException e) {
					// Standard IO Exception
					System.out.println(e.getLocalizedMessage());
					return;
				} catch (InvalidDendroFileException e) {
					// Fatal error interpreting file
					System.out.println(e.getLocalizedMessage());
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
			writer.saveAllToDisk("TestData/Output");
		
			
		}
		
		
		
	}
	
	
}
