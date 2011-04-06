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

//import edu.cornell.dendro.corina.sample.Sample;
//import edu.cornell.dendro.corina.util.StringUtils;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.schema.TridasTridas;

public class UnitTest extends TestCase {
	public UnitTest(String name) {
		super(name);
	}
	
	public void testTridasRoundTrip() {
		
		TridasTridas container = null;
		TridasReader reader = new TridasReader();
		TridasWriter writer = new TridasWriter();
		
		// Load and extract project from XML file
		try {
			reader.loadFile("TestData/TRiDaS/", "Tridas2.xml");
			container = reader.getTridasContainer();
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			fail();
		} catch (InvalidDendroFileException e) {
			fail();
		}
		
		// Write project classes out to a temp file
		try {
			writer.load(container, new TridasMetadataFieldSet());
			writer.saveAllToDisk("target/TestOutput");
			
		} catch (IncompleteTridasDataException e) {
			e.printStackTrace();
			fail();
		} catch (ConversionWarningException e) {
			e.printStackTrace();
			fail();
		} catch (IncorrectDefaultFieldsException e) {
			e.printStackTrace();
		} 
		
	}
	
	public void testTucson() {
		// Create a dummy project to export. This would need to
		// be complete with TridasObject, TridasElement etc for
		// the code to work properly
		/*
		 * TridasProject p = null;
		 * TridasObject o = new TridasObject();
		 * TridasElement e = new TridasElement();
		 * TridasSample s = new TridasSample();
		 * TridasRadius r = new TridasRadius();
		 * TridasMeasurementSeries ser = new TridasMeasurementSeries();
		 */
		TridasTridas container = null;
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("org.tridas.schema");
			Unmarshaller u = jc.createUnmarshaller();
			File xmlFile = new File("TestData/TRiDaS/Utrecht.xml");
			container = (TridasTridas) u.unmarshal(xmlFile);
			
		} catch (JAXBException e2) {
			e2.printStackTrace();
		}
		
		// Instantiate and override some default field values
		/*
		 * TucsonDefaultFields defaultFields = new TucsonDefaultFields();
		 * defaultFields.setDefaultFieldValue(TucsonField.COMP_DATE, "20091010");
		 * defaultFields.setDefaultFieldValue(TucsonField.SPECIES_CODE, "BBBB");
		 * defaultFields.setDefaultFieldValue(TucsonField.SITE_CODE, "AA");
		 */

		// Create a new converter based on a TridasProject
		TucsonWriter tucsonwriter = new TucsonWriter();
		try {
			tucsonwriter.load(container, new TridasToTucsonDefaults());
		} catch (IncompleteTridasDataException e) {
			e.printStackTrace();
		} catch (ConversionWarningException e) {
			e.printStackTrace();
		} catch (IncorrectDefaultFieldsException e) {
			e.printStackTrace();
		} 
		
		// Actually save file(s) to disk
		tucsonwriter.saveAllToDisk("target/TestOutput");
		
	}
	
	//
	// testing heidelberg
	//
	/*
	 * public void testHeidelberg() {
	 * // strategy: make sample, save to disk, load from disk, see if
	 * // it's the same.
	 * try {
	 * // get a temporary filename to use
	 * File file = File.createTempFile("heidelberg", null);
	 * file.deleteOnExit();
	 * String filename = file.getPath();
	 * TreeRingData s1 = makeDummy();
	 * HeidelbergFile h1 = new HeidelbergFile();
	 * BufferedWriter w = new BufferedWriter(new FileWriter(filename));
	 * h1.save(s1, w);
	 * w.close();
	 * HeidelbergFile h2 = new HeidelbergFile();
	 * BufferedReader r = new BufferedReader(new FileReader(filename));
	 * ITreeRingData s2 = new TreeRingData();
	 * h2.load(s2, r);
	 * r.close();
	 * // WRITEME: give Sample an export(filename, type) method (save()?)
	 * // -- this way, save(f) = export(f, DEFAULT_TYPE),
	 * // where DEFAULT_TYPE can be set in the prefs (and defaults to
	 * // corina)
	 * // assertEquals(s1, s2);
	 * // WRITEME: need Sample.equals() (and unit test for *that*?)
	 * // but, since i don't have a Sample.equals(), and more
	 * // importantly, because i only want to test some parts of
	 * // the sample (those which heidelberg can handle), i'll do
	 * // this myself.
	 * assertEquals(s1.getData(), s2.getData());
	 * } catch (IOException ioe) {
	 * fail();
	 * }
	 * }
	 */
}
