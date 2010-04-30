package org.tridas.io.test;

import java.io.IOException;

import org.tridas.io.DendroFile;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;



public class TestConvertTucsonToTridas {

	
	public static void main(String args[]) 
	{
		// Create a new converter
		TucsonReader reader = new TucsonReader();

		// Parse the legacy data file
		try {
			//TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
			reader.loadFile("TestData/Tucson", "Tucson4.crn", new TucsonToTridasDefaults());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getLocalizedMessage());
			return;
		} catch (IncorrectDefaultFieldsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidDendroFileException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getLocalizedMessage());
			return;
		}
		
		if (reader.getRawMetadata().size()>0) 
		System.out.println("Following metadata lines were interpreted");
		for (String md : reader.getRawMetadata())
		{
			System.out.println(md);
		}
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConversionWarningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncorrectDefaultFieldsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.saveAllToDisk("testSaveTridas");
		
		for(DendroFile file : writer.getFiles())
		{
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());
			System.out.println("Saved: " + writer.getNamingConvention().getFilename(file)+"."+file.getExtension());

		}
		
		
		
	}
	
	public TestConvertTucsonToTridas() throws IncompleteTridasDataException
	{	

		
	}
	
}
