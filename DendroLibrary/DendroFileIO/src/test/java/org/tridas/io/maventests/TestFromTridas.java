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
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.NothingToWriteException;
import org.tridas.io.formats.belfastapple.BelfastAppleWriter;
import org.tridas.io.formats.besancon.BesanconWriter;
import org.tridas.io.formats.catras.CatrasWriter;
import org.tridas.io.formats.corina.CorinaWriter;
import org.tridas.io.formats.excelmatrix.ExcelMatrixWriter;
import org.tridas.io.formats.heidelberg.HeidelbergWriter;
import org.tridas.io.formats.nottingham.NottinghamWriter;
import org.tridas.io.formats.oxford.OxfordWriter;
import org.tridas.io.formats.past4.Past4Writer;
import org.tridas.io.formats.sheffield.SheffieldWriter;
import org.tridas.io.formats.topham.TophamWriter;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.trims.TrimsWriter;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.formats.tucsoncompact.TucsonCompactWriter;
import org.tridas.io.formats.vformat.VFormatWriter;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.schema.TridasTridas;

public class TestFromTridas extends TestCase {

	private static final Logger log = LoggerFactory.getLogger(TestFromTridas.class);
	private static final String outputFolder = System.getProperty("java.io.tmpdir")+"/DendroFileIOTests/";
	private static final String inputFolder = "TestData/TRiDaS";

	private String[] getFilesFromFolder(String folder) {
		File dir = new File(folder);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}
		};
		return dir.list(filter);

	}

	/*public void testTridasToFHX2() {

		try {
			genericTest(TridasReader.class, FHX2Writer.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}*/

	/*public void testTridasToCSV() {

		try {
			genericTest(TridasReader.class, CSVMatrixWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}*/

	public void testTridasToTucson() {

		try {
			genericTest(TridasReader.class, TucsonWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToTopham() {

		try {
			genericTest(TridasReader.class, TophamWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToHeidelberg() {

		try {
			genericTest(TridasReader.class, HeidelbergWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	/*public void testTridasToTrims() {

		try {
			genericTest(TridasReader.class, TrimsWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}*/

	public void testTridasToBelfastApple() {

		try {
			genericTest(TridasReader.class, BelfastAppleWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToExcelMatrix() {

		try {
			genericTest(TridasReader.class, ExcelMatrixWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToSheffield() {

		try {
			genericTest(TridasReader.class, SheffieldWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToNottingham() {

		try {
			genericTest(TridasReader.class, NottinghamWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	/*public void testTridasToODFMatrix() {

		try {
			genericTest(TridasReader.class, ODFMatrixWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}*/

	public void testTridasToOxford() {

		try {
			genericTest(TridasReader.class, OxfordWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToTucsonCompact() {

		try {
			genericTest(TridasReader.class, TucsonCompactWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToCorina() {

		try {
			genericTest(TridasReader.class, CorinaWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToCatras() {
	
		try {
			genericTest(TridasReader.class, CatrasWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToBesancon() {

		try {
			genericTest(TridasReader.class, BesanconWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToVFormat() {

		try {
			genericTest(TridasReader.class, VFormatWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}

	public void testTridasToPast4() {

		try {
			genericTest(TridasReader.class, Past4Writer.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}
	
	
	/*public void testTridasToOOXML() {
	
		try {
			genericTest(TridasReader.class, OOXMLWriter.class);
		} catch (InstantiationException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
	}*/
	
	
	private void genericTest(Class<? extends AbstractDendroFileReader> readerClass, Class<? extends AbstractDendroCollectionWriter> writerClass) throws InstantiationException, IllegalAccessException
	{
		
		log.debug("********************************************************************************************");
		log.debug("*** TESTING CONVERSION --- "+readerClass.getSimpleName()+" to "+writerClass.getSimpleName()+" ***");
		log.debug("********************************************************************************************");
		
		String[] files = getFilesFromFolder(inputFolder);
		
		//String[] files = new String[1];
		//files[0] = "TestData/TRiDaS/StringRingValues.xml";
		

		if (files.length == 0) {
			fail();
		}

		for (String filename : files) {

			log.info(" ");
			log.info("Converting file: " + filename);

			TridasTridas container = null;

			AbstractDendroFileReader reader = readerClass.newInstance();
			
			try {
				reader.loadFile(inputFolder, filename);
			} catch (IOException e) {
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				e.printStackTrace();
				fail();
			}

			// Extract the TridasProject
			container = reader.getTridasContainer();

			// Create a new converter based on a TridasProject
			AbstractDendroCollectionWriter writer = writerClass.newInstance();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				log.warn("ImpossibleConversionException: " + filename);
				log.warn(e.getLocalizedMessage());
				continue;
			} catch (ConversionWarningException e) {
				log.warn("Conversion Warning: " + e.getLocalizedMessage());
			}

			// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputFolder);
			} catch (NothingToWriteException e) {
				fail();
			}
		}
		
	}
}
