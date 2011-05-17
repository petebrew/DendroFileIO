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
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.formats.belfastapple.BelfastAppleWriter;
import org.tridas.io.formats.besancon.BesanconWriter;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.catras.CatrasWriter;
import org.tridas.io.formats.corina.CorinaWriter;
import org.tridas.io.formats.csvmatrix.CSVMatrixWriter;
import org.tridas.io.formats.excelmatrix.ExcelMatrixWriter;
import org.tridas.io.formats.heidelberg.HeidelbergWriter;
import org.tridas.io.formats.nottingham.NottinghamWriter;
import org.tridas.io.formats.odfmatrix.ODFMatrixWriter;
import org.tridas.io.formats.oxford.OxfordWriter;
import org.tridas.io.formats.past4.Past4Writer;
import org.tridas.io.formats.sheffield.SheffieldWriter;
import org.tridas.io.formats.topham.TophamWriter;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.trims.TrimsWriter;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.formats.tucsoncompact.TucsonCompactWriter;
import org.tridas.io.formats.vformat.VFormatWriter;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.naming.SeriesCode8CharNamingConvention;
import org.tridas.schema.TridasTridas;

public class TestFromTridas extends TestCase {
	
	private static final Logger log = LoggerFactory.getLogger(TestFromTridas.class);
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
	
	public void testTridasToCSV() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if (!filename.equals("Tridas2.xml")) {
				continue;
			}
			
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				log.info("Failed reading - file not found/readable");
				// fail();
				continue;
			} catch (InvalidDendroFileException e) {
				log.info("Failed reading - " + e.getLocalizedMessage());
				// fail();
				continue;
			}
			
			// Extract container
			container = reader.getTridasContainer();
			
			// Create a new converter based on a TridasProject
			CSVMatrixWriter writer = new CSVMatrixWriter();
			
			writer.setNamingConvention(new NumericalNamingConvention("CSVMatrix"));
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				log.info("Failed Writing - " + e.getLocalizedMessage());
				// fail();
				continue;
			} catch (ConversionWarningException e) {

			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToTucson() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {		
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			TucsonWriter tucsonwriter = new TucsonWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("Tucson-"+filename);
			tucsonwriter.setNamingConvention(nc);
			try {
				tucsonwriter.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			tucsonwriter.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToTopham() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			TophamWriter writer = new TophamWriter();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk("target/TestOutput");
		}
	}
	
	
	public void testTridasToHeidelberg() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			
			//if (!filename.equals("Tridas4.xml")) {continue;	}
			
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			HeidelbergWriter writer = new HeidelbergWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Heidelberg-"+filename));
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToTrims() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(filename.equals("StringRingValues.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas project = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				e.printStackTrace();
				fail();
			}
			
			// Extract the TridasProject
			project = reader.getTridasContainer();
			
			// Create a new converter based on a TridasProject
			TrimsWriter writer = new TrimsWriter();
			writer.setNamingConvention(new HierarchicalNamingConvention());
			try {
				writer.load(project);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToBelfastApple() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			//if(!filename.equals("DerivedSeriesLinkedToDSeries.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			BelfastAppleWriter writer = new BelfastAppleWriter();
			writer.setNamingConvention(new NumericalNamingConvention("BelfastTestOut-"+filename));
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToExcelMatrix() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			ExcelMatrixWriter writer = new ExcelMatrixWriter();
			// TucsonWriter writer = new TucsonWriter();
			
			try {
				writer.setNamingConvention(new NumericalNamingConvention(filename.substring(0, filename
						.lastIndexOf("."))));
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToSheffield() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			if(!filename.equals("TridasMultiVars.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			SheffieldWriter writer = new SheffieldWriter();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToNottingham() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			//if(!filename.equals("TridasMultiVars.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			NottinghamWriter writer = new NottinghamWriter();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToODFMatrix() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			if(!filename.equals("Tridas1.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			ODFMatrixWriter writer = new ODFMatrixWriter();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
				fail();
			} catch (ConversionWarningException e) {
				e.printStackTrace();
				fail();
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToOxford() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			if(!filename.equals("Utrecht.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			OxfordWriter writer = new OxfordWriter();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
				fail();
			} catch (ConversionWarningException e) {
				e.printStackTrace();
				fail();
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToTucsonCompact() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			//if(!filename.equals("TridasMultiVars.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			TucsonCompactWriter writer = new TucsonCompactWriter();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToCorina() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			//if(!filename.equals("TridasMultiVars.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			CorinaWriter writer = new CorinaWriter();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToCatras() {
			String folder = "TestData/TRiDaS";
			String[] files = getFilesFromFolder(folder);
			
			if (files.length == 0) {
				fail();
			}
			
			for (String filename : files) {
				if(!filename.equals("Tridas1.xml")) continue;
				
				log.info("Test conversion of: " + filename);
				
				TridasTridas container = null;
				
				TridasReader reader = new TridasReader();
				try {
					reader.loadFile(folder, filename);
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
				CatrasWriter writer = new CatrasWriter();
				
				try {
					writer.setNamingConvention(new SeriesCode8CharNamingConvention());
					writer.load(container);
				} catch (IncompleteTridasDataException e) {
					e.printStackTrace();
				} catch (ConversionWarningException e) {
				} 
				
				
				
				// Actually save file(s) to disk
				writer.saveAllToDisk(outputLocation);
			}
		}
	
	public void testTridasToCatrasToTridas() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(!filename.equals("Tridas1.xml")) continue;
			
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			CatrasWriter writer = new CatrasWriter();
			
			try {
				writer.setNamingConvention(new NumericalNamingConvention("CatrasFirst"));
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
			
		}
		
		TridasTridas container = null;
		
		CatrasReader reader = new CatrasReader();
		try {
			reader.loadFile(outputLocation, "CatrasFirst(1).CAT");
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
		TridasWriter writer = new TridasWriter();
		
		try {
			writer.setNamingConvention(new NumericalNamingConvention("CatrasSecond"));
			writer.load(container);
		} catch (IncompleteTridasDataException e) {
			e.printStackTrace();
		} catch (ConversionWarningException e) {
		} 
		
		
		// Actually save file(s) to disk
		writer.saveAllToDisk(outputLocation);
		
	}

	
	
	public void testTridasToBesancon() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			if(!filename.equals("DerivedSeriesLinkedToDSeries.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			BesanconWriter writer = new BesanconWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Bescancon-"+filename));
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToVFormat() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			if(!filename.equals("othervar.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			VFormatWriter writer = new VFormatWriter();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
				e.printStackTrace();
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
	public void testTridasToPast4() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
				
			if(!filename.equals("AMO Roman cedar.xml")) continue;
			log.info("Test conversion of: " + filename);
			
			TridasTridas container = null;
			
			TridasReader reader = new TridasReader();
			try {
				reader.loadFile(folder, filename);
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
			Past4Writer writer = new Past4Writer();
			writer.setNamingConvention(new NumericalNamingConvention());
			try {
				writer.load(container);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
}
