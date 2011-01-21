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
