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
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.NothingToWriteException;
import org.tridas.io.formats.belfastapple.BelfastAppleReader;
import org.tridas.io.formats.belfastarchive.BelfastArchiveReader;
import org.tridas.io.formats.besancon.BesanconReader;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.corina.CorinaReader;
import org.tridas.io.formats.cracow.CracowReader;
import org.tridas.io.formats.dendrodb.DendroDBReader;
import org.tridas.io.formats.excelmatrix.ExcelMatrixReader;
import org.tridas.io.formats.fhx2.FHX2Reader;
import org.tridas.io.formats.fhx2.FHX2ToTridasDefaults;
import org.tridas.io.formats.heidelberg.HeidelbergReader;
import org.tridas.io.formats.nottingham.NottinghamReader;
import org.tridas.io.formats.odfmatrix.ODFMatrixReader;
import org.tridas.io.formats.ooxml.OOXMLReader;
import org.tridas.io.formats.oxford.OxfordReader;
import org.tridas.io.formats.past4.Past4Reader;
import org.tridas.io.formats.past4.Past4ToTridasDefaults;
import org.tridas.io.formats.sheffield.SheffieldReader;
import org.tridas.io.formats.topham.TophamReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.trims.TrimsReader;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults;
import org.tridas.io.formats.tucsoncompact.TucsonCompactReader;
import org.tridas.io.formats.vformat.VFormatReader;
import org.tridas.io.formats.windendro.WinDendroReader;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.FilePermissionException;
import org.tridas.schema.TridasTridas;

public class TestToTridas extends TestCase {
	
	private static final Logger log = LoggerFactory.getLogger(TestToTridas.class);
	private static final String outputLocation = "TestData/TRiCYCLE-Output-From-Legacy";
	
	private String[] getFilesFromFolder(String folder) {
		File dir = new File(folder);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}
		};
		return dir.list(filter);
	}
	
	public void testTucsonToTridas() {
		String folder = "TestData/Tucson";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(!filename.equals("Maris.rwl")) { continue; }
			
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
				fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Tucson-"+filename));
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testFHX2ToTridas() {
		String folder = "TestData/FHX2";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
						
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			FHX2Reader reader = new FHX2Reader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename, new FHX2ToTridasDefaults());
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
				fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("FHX2-"+filename));
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testPast4ToTridas() {
		String folder = "TestData/PAST4";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(!filename.equals("Voorburg 1988.p4p")) { continue; }
			//if(!filename.equals("title1.p4p")) { continue; }
			
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			Past4Reader reader = new Past4Reader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename, new Past4ToTridasDefaults());
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
				fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Past-"+filename));
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testCracowToTridas() {
		String folder = "TestData/Cracow";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			//if(!filename.equals("WR1916.AVR")) { continue; }

			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			CracowReader reader = new CracowReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Cracow-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
	}
	
	public void testCatrasToTridas() {
		String folder = "TestData/CATRAS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			//if(!filename.equals("AKK00010.CAT")) continue;
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			CatrasReader reader = new CatrasReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Catras-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
	}
	
	public void testDendroDBToTridas() {
		String folder = "TestData/DendroDB";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(!filename.equals("0567_25278.dat")) continue;
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			DendroDBReader reader = new DendroDBReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("DendroDB-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
	}
	
	public void testHeidelbergToTridas() {
		
		String folder = "TestData/Heidelberg";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(!filename.equals("earlylatewood.fh")) continue;
			
			log.info("Test conversion of: " + filename);
			
			HeidelbergReader reader = new HeidelbergReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
				// reader.loadFile("TestData/Heidelberg", "UAKK0530.fh");
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Heidelberg-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testTrimsToTridas() {
		String folder = "TestData/TRIMS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			TrimsReader reader = new TrimsReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Trims-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testSheffieldToTridas() {
		String folder = "TestData/Sheffield";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			// if(!filename.equals("yhg50683.d")) continue;
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			SheffieldReader reader = new SheffieldReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			try {
				writer.setNamingConvention(new NumericalNamingConvention("Sheffield-"+filename));
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testBelfastAppleToTridas() {
		String folder = "TestData/BelfastApple";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			BelfastAppleReader reader = new BelfastAppleReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("BelfastApple-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testBelfastArchiveToTridas() {
		String folder = "TestData/BelfastArchive";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			BelfastArchiveReader reader = new BelfastArchiveReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("BelfastArchive-"+filename));
			
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testVFormatToTridas() {
		String folder = "TestData/VFormat";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(!filename.equals("NL_V.!oj")) continue;
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			VFormatReader reader = new VFormatReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("VFormat-"+filename));
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	

	
	public void testBesanconToTridas() {
		String folder = "TestData/Besancon";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(!filename.equals("BesanconNew2.txt")) continue;
			
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			BesanconReader reader = new BesanconReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("Besancon-"+filename);
			writer.setNamingConvention(nc);
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			}
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testWinDendroToTridas() {
		String folder = "TestData/WinDendro";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			WinDendroReader reader = new WinDendroReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("WinDendro-"+filename);
			writer.setNamingConvention(nc);
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	
	public void testTophamToTridas() {
		String folder = "TestData/Topham";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			TophamReader reader = new TophamReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("Topham-"+filename);
			writer.setNamingConvention(nc);
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testNottinghamToTridas() {
		String folder = "TestData/Nottingham";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			NottinghamReader reader = new NottinghamReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("Nottingham-"+filename);
			writer.setNamingConvention(nc);
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testODFMatrixToTridas() {
		String folder = "TestData/ODFMatrix";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			//if (!filename.equals("denh2-1.dan")) continue;
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			ODFMatrixReader reader = new ODFMatrixReader();
			
			// Parse the legacy data file
			try {
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("OODFMatrix-"+filename);
			writer.setNamingConvention(nc);
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testOOXMLToTridas() {
		String folder = "TestData/OOXML";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			//if (!filename.equals("denh2-1.dan")) continue;
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			OOXMLReader reader = new OOXMLReader();
			
			// Parse the legacy data file
			try {
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("OOXML-"+filename);
			writer.setNamingConvention(nc);
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testOxfordToTridas() {
		String folder = "TestData/Oxford";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			//if (!filename.equals("denh2-1.dan")) continue;
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			OxfordReader reader = new OxfordReader();
			
			// Parse the legacy data file
			try {
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("Oxford-"+filename);
			writer.setNamingConvention(nc);
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	public void testTucsonCompactToTridas() {
		String folder = "TestData/TucsonCompact";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			TucsonCompactReader reader = new TucsonCompactReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("TucsonCompact-"+filename);
			writer.setNamingConvention(nc);
			
			try {
				writer.load(container, new TridasMetadataFieldSet());
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
		
	}
	
	/*public void testCorinaToTridas() {
		String folder = "TestData/Corina";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			CorinaReader reader = new CorinaReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				return;
			}
			
			// Extract the TridasProject
			TridasProject myproject = reader.getProject();
			
			TridasWriter writer = new TridasWriter();
			NumericalNamingConvention nc = new NumericalNamingConvention("test");
			writer.setNamingConvention(nc);
			
			try {
				writer.loadProject(myproject, new TridasMetadataFieldSet());
			} catch (IncompleteTridasDataException e) {
				fail();
			} catch (ConversionWarningException e) {} catch (IncorrectDefaultFieldsException e) {
				fail();
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			};
			
		}
		
	}*/
	
	public void testCorinaToTridas() {
		String folder = "TestData/Corina";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			if(!filename.equals("TRB1AB.SUM")) continue;
			
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			CorinaReader reader = new CorinaReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("CorinaLegacy-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
	}
	
	/*public void testCSVMatrixToTridas() {
		String folder = "TestData/CSVMatrix";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			//if(!filename.equals("TRB1AB.SUM")) continue;
			
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			CSVMatrixReader reader = new CSVMatrixReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
				return;
			}
			
			// Extract the TridasProject
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("CSVMatrix-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
						// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			};
			
		}
	}*/
	
	public void testExcelMatrixToTridas() {
		String folder = "TestData/ExcelMatrix";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			//if(!filename.equals("AKK00010.CAT")) continue;
			log.info("Test conversion of: " + filename);
			
			// Create a new converter
			ExcelMatrixReader reader = new ExcelMatrixReader();
			
			// Parse the legacy data file
			try {
				// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
				reader.loadFile(folder, filename);
			} catch (IOException e) {
				// Standard IO Exception
				log.info(e.getLocalizedMessage());
				fail();
				return;
			} catch (InvalidDendroFileException e) {
				// Fatal error interpreting file
				log.info(e.getLocalizedMessage());
				fail();
				return;
			}
			
			// Extract 
			TridasTridas container = reader.getTridasContainer();
			
			TridasWriter writer = new TridasWriter();
			writer.setNamingConvention(new NumericalNamingConvention("Excel-"+filename));
			
			try {
				writer.load(container);
			} catch (ImpossibleConversionException e) {
				fail();
			} catch (ConversionWarningException e) {
			} 
			
			// Actually save file(s) to disk
			try {
				writer.saveAllToDisk(outputLocation);
			} catch (NothingToWriteException e) {
				fail();
			} catch (FilePermissionException e) {
				log.error(e.getLocalizedMessage());
				fail();
			};
			
		}
	}
	
}
