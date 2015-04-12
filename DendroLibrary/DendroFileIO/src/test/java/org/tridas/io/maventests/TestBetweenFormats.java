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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.NothingToWriteException;
import org.tridas.io.formats.trims.TrimsWriter;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.util.FilePermissionException;
import org.tridas.schema.TridasTridas;

public class TestBetweenFormats extends TestCase {
	
	private static final Logger log = LoggerFactory.getLogger(TestBetweenFormats.class);
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
				//fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			TrimsWriter writer = new TrimsWriter();
			writer.setNamingConvention(new UUIDNamingConvention());
			
			try {
				writer.load(container);
				writer.saveAllToDisk(outputLocation);
			} catch (ImpossibleConversionException e) {
			} catch (ConversionWarningException e) {
			} catch (NothingToWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FilePermissionException e) {
				log.debug(e.getLocalizedMessage());
				fail();
			} catch (Exception e){
				e.printStackTrace();
				fail();
			}
			
			
		}
	}
}
