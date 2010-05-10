package org.tridas.io.maventests;

import java.io.IOException;

import org.tridas.io.DendroFile;
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

	public void testTridasToTucson() 
	{
		// Create a dummy project to export.  This would need to
		// be complete with TridasObject, TridasElement etc for
		// the code to work properly
		/*TridasProject p = null;
		TridasObject o = new TridasObject();
		TridasElement e = new TridasElement();
		TridasSample s = new TridasSample();
		TridasRadius r = new TridasRadius();
		TridasMeasurementSeries ser = new TridasMeasurementSeries();
		*/
		TridasProject project = null;
	    
		TridasReader reader = new TridasReader();
		try {
			reader.loadFile("TestData/TRiDaS", "Tridas1.xml");
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			return;
		} catch (InvalidDendroFileException e) {
			e.printStackTrace();
		}
		
		// Extract the TridasProject
		project = reader.getProject();

		// Create a new converter based on a TridasProject
		TucsonWriter tucsonwriter = new TucsonWriter();
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
	
	
	public void testTucsonToTridas() 
	{
		// Create a new converter
		TucsonReader reader = new TucsonReader();

		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/Tucson", "Tucson1.rwl", new TucsonToTridasDefaults());
		} catch (IOException e) {
			// Standard IO Exception
			System.out.println(e.getLocalizedMessage());
			return;
		} catch (IncorrectDefaultFieldsException e) {
			// The default fields you gave were wrong
			e.printStackTrace();
		} catch (InvalidDendroFileException e) {
			// Fatal error interpreting file
			System.out.println(e.getLocalizedMessage());
			return;
		}
		
		// Get the metadata lines that were interpreted
		if (reader.getRawMetadata().size()>0) 
		System.out.println("Following metadata lines were interpreted");
		for (String md : reader.getRawMetadata())
		{
			System.out.println(md);
		}
			
		// Get any warnings thrown during conversion
		if(reader.getWarnings().size()>0)
		System.out.println("Some issues were found whilst converting your file...");
		for (ConversionWarning w : reader.getWarnings())
		{
			System.out.println("  ["+w.getWarningType()+"] -  "+w.getMessage());
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
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
		}
		
		
		
	}
	
	
	public void testCatrasToTridas() 
	{
		
		// Create a new converter
		CatrasReader reader = new CatrasReader();
		
		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/CATRAS", "OBL10002.CAT");
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
			writer.loadProject(myproject);
		} catch (IncompleteTridasDataException e) {
			fail();
		} catch (ConversionWarningException e) {
		}
		writer.saveAllToDisk("TestData/Output");
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
		}
		
	}
	
	public void testHeidelbergToTridas(){
		HeidelbergReader reader = new HeidelbergReader();
		
		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/Heidelberg", "bababa.fh");
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
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
		}
		
	}
	
	public void testTrimsToTridas() 
	{
		
		// Create a new converter
		TrimsReader reader = new TrimsReader();
		
		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/TRIMS", "GRC04B.txt");
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
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
		}
		
	}
	
	public void testSheffieldToTridas() 
	{
		
		// Create a new converter
		SheffieldReader reader = new SheffieldReader();
		
		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/Sheffield", "yhg50683.d");
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
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
		}
		
	}
	
	public void testBelfastAppleToTridas() 
	{
		
		// Create a new converter
		BelfastAppleReader reader = new BelfastAppleReader();
		
		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/BelfastApple", "A1805.txt");
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
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
		}
		
	}
	
	public void testBelfastArchiveToTridas() 
	{
		
		// Create a new converter
		BelfastArchiveReader reader = new BelfastArchiveReader();
		
		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/BelfastArchive", "bellframe1.arx");
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
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
		}
		
	}
	
	public void testVFormatToTridas() 
	{
		// Create a new converter
		VFormatReader reader = new VFormatReader();

		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/VFormat", "H_V.!oj");
			} catch (IOException e) {
				// Standard IO Exception
				System.out.println(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				System.out.println(e.getLocalizedMessage());
				return;
			}
		
		// Get the metadata lines that were interpreted
		if (reader.getRawMetadata().size()>0) 
		System.out.println("Following metadata lines were interpreted");
		for (String md : reader.getRawMetadata())
		{
			System.out.println(md);
		}
			
		// Get any warnings thrown during conversion
		if(reader.getWarnings().size()>0)
		System.out.println("Some issues were found whilst converting your file...");
		for (ConversionWarning w : reader.getWarnings())
		{
			System.out.println("  ["+w.getWarningType()+"] -  "+w.getMessage());
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
	
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
		}
		
		
		
	}
	
	
}
