package org.tridas.io.test;

import java.io.IOException;

import org.tridas.io.DendroFile;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults;
import org.tridas.io.formats.tucson.TucsonFile;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.schema.TridasProject;



public class TestConvertTridasToTucson {

	
	public static void main(String args[]) 
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
			reader.loadFile("TestData/TRiDaS", "TridasInvalid.xml", new TridasMetadataFieldSet());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getLocalizedMessage());
			return;
		} catch (IncorrectDefaultFieldsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Extract the TridasProject
		project = reader.getProject();

		// Create a new converter based on a TridasProject
		TucsonWriter tucsonwriter = new TucsonWriter();
		try {
			tucsonwriter.loadProject(project, new TridasToTucsonDefaults());
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


		// Actually save file(s) to disk
		tucsonwriter.saveAllToDisk("testTusconOutput");

		
		// Display any conversion warnings
		if(tucsonwriter.getWarnings().size() > 0)
		{
			System.out.println("*** Warnings were thrown while converting your data ***".toUpperCase());
		
			for(ConversionWarning cw : tucsonwriter.getWarnings())
				System.out.println("  - [" + cw.getWarningType().toString()+ "]: " + cw.getMessage());
		}
		


		
		// Any warnings can be shown to user if applicable
		// e.g. if a field was truncated 
		// tc.getWarnings();
		
		
		// Grab a list of dendro files that will be written
		DendroFile[] files = tucsonwriter.getFiles();
		
		for(DendroFile f : files)
		{
			TucsonFile f2 = (TucsonFile) f;
			System.out.println("File output: " + tucsonwriter.getNamingConvention().getFilename(f2));
		}
		
		// Alternatively we could print a string representation of each file
		/*for (DendroFile f : files)
		{
			TucsonFile thisFile = (TucsonFile) f;
			System.out.println(thisFile.saveToString());
		}*/
		
		
		
	}
	public TestConvertTridasToTucson() 
	{

	}
	
}
