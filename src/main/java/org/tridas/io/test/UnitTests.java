/**
 * pboon: need to have some new header
 *
 * TODO: more unit-tests needed when is is going to be a reliable
 * Open source library for reading and writing Tree ring data files
 * formated using a non-Tridas standard
 */

package org.tridas.io.test;

//import edu.cornell.dendro.corina.sample.Sample;
//import edu.cornell.dendro.corina.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.tridas.io.DendroFile;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.heidelberg.HeidelbergFile;
import org.tridas.io.formats.tridas.TridasFile;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults;
import org.tridas.io.formats.tucson.TucsonFile;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.StringUtils;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.util.TridasObjectEx;

public class UnitTests extends TestCase {
	public UnitTests(String name) {
		super(name);
	}

	public void testTridasRoundTrip()
	{
		
		TridasProject project = null;
		TridasReader reader = new TridasReader();
		TridasWriter writer = new TridasWriter();
		
		// Load and extract project from XML file
		try {
			reader.loadFile("TestData/TRiDaS/", "Tridas2.xml", new TridasMetadataFieldSet());
			project = reader.getProject();
		} catch (IOException e) {		
			System.out.println(e.getLocalizedMessage());
			fail();
		} catch (IncorrectDefaultFieldsException e) {
			// TODO Auto-generated catch block
			fail();
		}
			
		
		// Write project classes out to a temp file
		try {
			writer.loadProject(project, new TridasMetadataFieldSet());
			writer.saveAllToDisk();
			for(DendroFile file : writer.getFiles())
			{
				System.out.println("Saved: " + writer.getNamingConvention().getFilename(file));
			}
			
		} catch (IncompleteTridasDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (ConversionWarningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (IncorrectDefaultFieldsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public void testTucson()
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
	    JAXBContext jc;
		try {
			jc = JAXBContext.newInstance( "org.tridas.schema" );
			Unmarshaller u = jc.createUnmarshaller();
		    File xmlFile = new File("TestData/TRiDaS/Tridas1.xml");
		    project = (TridasProject) u.unmarshal(xmlFile);
		   
		} catch (JAXBException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	    
		// Instantiate and override some default field values
		/*TucsonDefaultFields defaultFields = new TucsonDefaultFields();
		defaultFields.setDefaultFieldValue(TucsonField.COMP_DATE, "20091010");
		defaultFields.setDefaultFieldValue(TucsonField.SPECIES_CODE, "BBBB");
		defaultFields.setDefaultFieldValue(TucsonField.SITE_CODE, "AA");
		*/
		
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
		tucsonwriter.saveAllToDisk();
		
		// Parse TridasProject 
		/*try {
			tucsonwriter.parseProject(p);
			tucsonwriter.saveAllToDisk();
			} catch (IncompleteTridasDataException e2) { e2.printStackTrace();	}
*/
		
		if(tucsonwriter.getWarnings()!=null)
			System.out.println("*** Warnings were thrown while converting your data ***".toUpperCase());
		
		for(ConversionWarning cw : tucsonwriter.getWarnings())
			System.out.println(cw.getWarningType().toString()+ ": " + cw.getMessage());

		

		
		// If we want to explicitly override any fields
		// or supply a specific filename then we do...
		/*for (DendroFile f : files)
		{
			// Cast to a specific TucsonFile type
			TucsonFile f2 = (TucsonFile) f;
			try 
			{
				// Now we can tamper with the file for instance
				// this overrides the Site ID code
				f2.setSiteCode("ABCDEF");
			} 
			catch (ConversionWarningException e1) 
			{
				// Show user what went wrong if you want
			}
		}*/
		
		
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

	//
	// testing heidelberg
	//
	/*public void testHeidelberg() {
		// strategy: make sample, save to disk, load from disk, see if
		// it's the same.

		try {
			// get a temporary filename to use
			File file = File.createTempFile("heidelberg", null);
			file.deleteOnExit();
			String filename = file.getPath();

			TreeRingData s1 = makeDummy();
			HeidelbergFile h1 = new HeidelbergFile();
			BufferedWriter w = new BufferedWriter(new FileWriter(filename));
			h1.save(s1, w);
			w.close();

			HeidelbergFile h2 = new HeidelbergFile();
			BufferedReader r = new BufferedReader(new FileReader(filename));
			ITreeRingData s2 = new TreeRingData();
			h2.load(s2, r);
			r.close();

			// WRITEME: give Sample an export(filename, type) method (save()?)
			// -- this way, save(f) = export(f, DEFAULT_TYPE),
			// where DEFAULT_TYPE can be set in the prefs (and defaults to
			// corina)

			// assertEquals(s1, s2);
			// WRITEME: need Sample.equals() (and unit test for *that*?)

			// but, since i don't have a Sample.equals(), and more
			// importantly, because i only want to test some parts of
			// the sample (those which heidelberg can handle), i'll do
			// this myself.
			assertEquals(s1.getData(), s2.getData());
		} catch (IOException ioe) {
			fail();
		}
	}*/

	//
	// testing StringUtils.java
	//
	public void testExtractInts() {
		int x[] = StringUtils.extractInts("1 2 3");
		assertEquals(x.length, 3);
		assertEquals(x[0], 1);
		assertEquals(x[1], 2);
		assertEquals(x[2], 3);
	}
	
	public void testDefaultValues(){
		StringDefaultValue defString = new StringDefaultValue("Hello", -1, -1);
		assertEquals("Hello", defString.getValue());
		defString.setMinLength(10);
		assertEquals("Hello     ", defString.getValue());
		
		defString.setOverridingValue("Goodbye");
		defString.setValue("wait!");
		assertEquals("Goodbye   ", defString.getValue());
		
		IntegerDefaultValue defInt = new IntegerDefaultValue(3, 0, 5, -1, -1);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		assertEquals("3", defInt.getStringValue());
		defInt.setMinLength(5);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		assertEquals("3    ", defInt.getStringValue());
		
		defInt.setValue(10);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		
		defInt.setOverridingValue(4);
		defInt.setValue(3);
		assertEquals(Integer.valueOf(4), defInt.getValue());
		assertEquals("4    ", defInt.getStringValue());
	}
	
	public void testNamingConventions(){
		NumericalNamingConvention naming = new NumericalNamingConvention();
		ArrayList<DendroFile> files = new ArrayList<DendroFile>();
		files.add(new TridasFile());
		files.add(new TucsonFile());
		files.add(new HeidelbergFile());
		for(DendroFile f : files){
			naming.registerFile(f, null, null, null, null, null, null);
		}
		
		for(int i=0; i<files.size(); i++){
			assertEquals((i+1)+"", naming.getFilename(files.get(i)));
		}
		
		naming.setBaseFilename("file");
		naming.clearRegisteredFiles();
		
		for(DendroFile f : files){
			naming.registerFile(f, null, null, null, null, null, null);
		}
		
		for(int i=0; i<files.size(); i++){
			assertEquals("file"+(i+1), naming.getFilename(files.get(i)));
		}
	}
  
	
	   
	public void testPaulsMarshalling() throws Exception    
	{       
		TridasProject projectTridas = new ObjectFactory().createTridasProject();        
		projectTridas.setTitle("my test project");        
		// Add a tridas object entity        
		TridasObject objectTridas = new ObjectFactory().createTridasObject();       
		objectTridas.setTitle("my test object");        
		projectTridas.getObjects().add(objectTridas);       
		JAXBContext jaxbContext = JAXBContext.newInstance("org.tridas.schema");        
		Marshaller marshaller = jaxbContext.createMarshaller();       
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");        
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		//testing        
		java.io.StringWriter sw = new StringWriter();        
		marshaller.marshal(projectTridas, sw);        
		System.out.print(sw.toString());    
		
	}
	
	
}
