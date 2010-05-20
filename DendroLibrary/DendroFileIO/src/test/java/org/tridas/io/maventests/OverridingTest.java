package org.tridas.io.maventests;

import java.io.IOException;

import org.grlea.log.SimpleLogger;
import org.tridas.io.formats.heidelberg.HeidelbergWriter;
import org.tridas.io.formats.heidelberg.TridasToHeidelbergDefaults;
import org.tridas.io.formats.heidelberg.TridasToHeidelbergDefaults.HeidelbergField;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults.TucsonField;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

import junit.framework.TestCase;

public class OverridingTest extends TestCase {
	private static final SimpleLogger log = new SimpleLogger(OverridingTest.class);
	
	public void testHeidelbergOverriding() throws IOException, InvalidDendroFileException, IncorrectDefaultFieldsException{
		// load tridas file
		TridasReader reader = new TridasReader();
		reader.loadFile("TestData/TRiDaS/Tridas4.xml");
		TridasProject project = reader.getProject();
		
		// ok we got our project, lets set defaults stuff
		TridasToHeidelbergDefaults defaults = new TridasToHeidelbergDefaults();
		HeidelbergWriter writer = new HeidelbergWriter();
		
		defaults.getStringDefaultValue(HeidelbergField.KEY_CODE).setOverridingValue("TestOverriding");
		defaults.getIntegerDefaultValue(HeidelbergField.DATEBEGIN).setOverridingValue(0);
		
		writer.loadProject(project, defaults);
		
		boolean keyCodeFound = false;
		boolean dateBeginFound = false;
		
		String[] file = writer.getFiles()[0].saveToString();
		assertNotNull(file);
		
		for(String s : file){
			if(s.equals("KeyCode=TestOverriding")){
				keyCodeFound = true;
			}
			if(s.equals("DateBegin=0")){
				dateBeginFound = true;
			}
		}
		assertTrue(keyCodeFound);
		assertTrue(dateBeginFound);
		
		defaults.getIntegerDefaultValue(HeidelbergField.DATEBEGIN).setOverriding(false);
		writer.loadProject(project, defaults);
		
		keyCodeFound = false;
		boolean dateChanged = false;
		
		file = writer.getFiles()[1].saveToString();
		assertNotNull(file);
		
		for(String s : file){
			log.info(s);
			if(s.equals("KeyCode=TestOverriding")){
				keyCodeFound = true;
			}
			if(s.equals("DateBegin=0")){
				dateChanged = true;
			}
		}
		assertTrue(keyCodeFound);
		//assertFalse(dateChanged);
	}
	
	public void testTucsanOverriding() throws IOException, InvalidDendroFileException, IncorrectDefaultFieldsException{
		// load tridas file
		TridasReader reader = new TridasReader();
		reader.loadFile("TestData/TRiDaS/Tridas4.xml");
		TridasProject project = reader.getProject();
		
		// ok we got our project, lets set defaults stuff
		TridasToTucsonDefaults defaults = new TridasToTucsonDefaults();
		
		defaults.getStringDefaultValue(TucsonField.SITE_NAME).setOverridingValue("TestOverriding");
		defaults.getStringDefaultValue(TucsonField.INVESTIGATOR).setOverridingValue("MyInvestigator");
		
		TucsonWriter writer = new TucsonWriter();
		writer.loadProject(project, defaults);
		
		boolean siteNameFound = false;
		boolean investigatorFound = false;
		
		String[] file = writer.getFiles()[0].saveToString();
		assertNotNull(file);
		
		for(String s : file){
			if(s.contains("TestOverriding")){
				siteNameFound = true;
			}
			if(s.contains("MyInvestigator")){
				investigatorFound = true;
			}
		}
		assertTrue(siteNameFound);
		assertTrue(investigatorFound);
		
		defaults.getStringDefaultValue(TucsonField.INVESTIGATOR).setOverriding(false);
		writer.loadProject(project, defaults);
		
		siteNameFound = false;
		boolean investegatorChanged = false;
		
		file = writer.getFiles()[1].saveToString();
		assertNotNull(file);
		
		
		for(String s : file){
			log.info(s);
			if(s.contains("TestOverriding")){
				siteNameFound = true;
			}
			if(s.contains("MyInvestigator")){
				investegatorChanged = true;
			}
		}
		
		assertTrue(siteNameFound);
		assertFalse(investegatorChanged);
	}
}
