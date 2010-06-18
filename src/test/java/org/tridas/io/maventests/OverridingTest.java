package org.tridas.io.maventests;

import java.io.IOException;

import junit.framework.TestCase;

import org.grlea.log.SimpleLogger;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.formats.heidelberg.HeidelbergWriter;
import org.tridas.io.formats.heidelberg.TridasToHeidelbergDefaults;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults.TucsonField;
import org.tridas.schema.TridasProject;

public class OverridingTest extends TestCase {
	private static final SimpleLogger log = new SimpleLogger(OverridingTest.class);
	
	public void testHeidelbergOverriding() throws IOException, InvalidDendroFileException,
			IncorrectDefaultFieldsException {
		// load tridas file
		TridasReader reader = new TridasReader();
		reader.loadFile("TestData/TRiDaS/Tridas4.xml");
		TridasProject project = reader.getProject();
		
		// ok we got our project, lets set defaults stuff
		TridasToHeidelbergDefaults defaults = new TridasToHeidelbergDefaults();
		HeidelbergWriter writer = new HeidelbergWriter();
		
		defaults.getStringDefaultValue(DefaultFields.KEYCODE).setOverridingValue("TestOverriding");
		defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setOverridingValue(0);
		
		writer.loadProject(project, defaults);
		
		boolean keyCodeFound = false;
		boolean dateBeginFound = false;
		
		String[] file = writer.getFiles()[0].saveToString();
		assertNotNull(file);
		
		for (String s : file) {
			if (s.equals("KeyCode=TestOverriding")) {
				keyCodeFound = true;
			}
			if (s.equals("DateBegin=0")) {
				dateBeginFound = true;
			}
		}
		assertTrue(keyCodeFound);
		assertTrue(dateBeginFound);
		
		writer.clearFiles();
		
		defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setOverriding(false);
		writer.loadProject(project, defaults);
		
		keyCodeFound = false;
		boolean dateChanged = false;
		
		file = writer.getFiles()[1].saveToString();
		assertNotNull(file);
		
		for (String s : file) {
			if (s.equals("KeyCode=TestOverriding")) {
				keyCodeFound = true;
			}
			if (s.equals("DateBegin=0")) {
				dateChanged = true;
			}
		}
		assertTrue(keyCodeFound);
		assertFalse(dateChanged);
	}
	
	public void testTucsonOverriding() throws IOException, InvalidDendroFileException, IncorrectDefaultFieldsException {
		// load tridas file
		TridasReader reader = new TridasReader();
		reader.loadFile("TestData/TRiDaS/Tridas4.xml");
		TridasProject project = reader.getProject();
		
		// ok we got our project, lets set defaults stuff
		TridasToTucsonDefaults defaults = new TridasToTucsonDefaults();
		
		defaults.getStringDefaultValue(TucsonField.SITE_NAME).setOverridingValue("TstOvrd");
		defaults.getStringDefaultValue(TucsonField.INVESTIGATOR).setOverridingValue("Test");
		
		TucsonWriter writer = new TucsonWriter();
		writer.loadProject(project, defaults);
		
		boolean siteNameFound = false;
		boolean investigatorFound = false;
		
		String[] file = writer.getFiles()[0].saveToString();
		assertNotNull(file);
		
		for (String s : file) {
			if (s.contains("TstOvrd")) {
				siteNameFound = true;
			}
			if (s.contains("Test")) {
				investigatorFound = true;
			}
		}
		assertTrue(siteNameFound);
		assertTrue(investigatorFound);
		
		defaults.getStringDefaultValue(TucsonField.INVESTIGATOR).setOverriding(false);
		defaults.getStringDefaultValue(TucsonField.INVESTIGATOR).setOverridingValue("Test2");

		writer.loadProject(project, defaults);
		
		siteNameFound = false;
		boolean investigatorChanged = false;
		
		file = writer.getFiles()[1].saveToString();
		assertNotNull(file);
		
		for (String s : file) {
			if (s.contains("TstOvrd")) {
				siteNameFound = true;
			}
			if (s.contains("Test2")) {
				investigatorChanged = true;
			}
		}
		
		assertTrue(siteNameFound);
		assertFalse(investigatorChanged);
	}
}
