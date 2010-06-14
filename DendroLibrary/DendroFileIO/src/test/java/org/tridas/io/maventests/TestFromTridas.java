package org.tridas.io.maventests;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.TestCase;

import org.grlea.log.SimpleLogger;
import org.tridas.io.formats.belfastapple.BelfastAppleWriter;
import org.tridas.io.formats.csv.CSVWriter;
import org.tridas.io.formats.excelmatrix.ExcelMatrixWriter;
import org.tridas.io.formats.heidelberg.HeidelbergWriter;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.trims.TrimsWriter;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.warningsandexceptions.ConversionWarningException;
import org.tridas.io.warningsandexceptions.IncompleteTridasDataException;
import org.tridas.io.warningsandexceptions.InvalidDendroFileException;
import org.tridas.io.warningsandexceptions.UnrepresentableTridasDataException;
import org.tridas.schema.TridasProject;

public class TestFromTridas extends TestCase {
	
	private static final SimpleLogger log = new SimpleLogger(TestFromTridas.class);
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
			
			TridasProject project = null;
			
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
			
			// Extract the TridasProject
			project = reader.getProject();
			
			// Create a new converter based on a TridasProject
			CSVWriter writer = new CSVWriter();
			
			writer.setNamingConvention(new HierarchicalNamingConvention());
			try {
				writer.loadProject(project);
			} catch (IncompleteTridasDataException e) {
				log.info("Failed Writing - " + e.getLocalizedMessage());
				// fail();
				continue;
			} catch (ConversionWarningException e) {

			} catch (UnrepresentableTridasDataException e) {

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
			
			TridasProject project = null;
			
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
			project = reader.getProject();
			
			// Create a new converter based on a TridasProject
			TucsonWriter tucsonwriter = new TucsonWriter();
			tucsonwriter.setNamingConvention(new HierarchicalNamingConvention());
			try {
				tucsonwriter.loadProject(project);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} catch (UnrepresentableTridasDataException e) {
			}
			
			// Actually save file(s) to disk
			tucsonwriter.saveAllToDisk("target/TestOutput");
		}
	}
	
	public void testTridasToHeidelberg() {
		String folder = "TestData/TRiDaS";
		String[] files = getFilesFromFolder(folder);
		
		if (files.length == 0) {
			fail();
		}
		
		for (String filename : files) {
			
			if (!filename.equals("Tridas4.xml")) {
				continue;
			}
			
			log.info("Test conversion of: " + filename);
			
			TridasProject project = null;
			
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
			project = reader.getProject();
			
			// Create a new converter based on a TridasProject
			HeidelbergWriter writer = new HeidelbergWriter();
			writer.setNamingConvention(new HierarchicalNamingConvention());
			try {
				writer.loadProject(project);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} catch (UnrepresentableTridasDataException e) {
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
			log.info("Test conversion of: " + filename);
			
			TridasProject project = null;
			
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
			project = reader.getProject();
			
			// Create a new converter based on a TridasProject
			TrimsWriter writer = new TrimsWriter();
			writer.setNamingConvention(new HierarchicalNamingConvention());
			try {
				writer.loadProject(project);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} catch (UnrepresentableTridasDataException e) {
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
			log.info("Test conversion of: " + filename);
			
			TridasProject project = null;
			
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
			project = reader.getProject();
			
			// Create a new converter based on a TridasProject
			BelfastAppleWriter writer = new BelfastAppleWriter();
			writer.setNamingConvention(new HierarchicalNamingConvention());
			try {
				writer.loadProject(project);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} catch (UnrepresentableTridasDataException e) {
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
			
			TridasProject project = null;
			
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
			project = reader.getProject();
			
			// Create a new converter based on a TridasProject
			ExcelMatrixWriter writer = new ExcelMatrixWriter();
			// TucsonWriter writer = new TucsonWriter();
			
			try {
				writer.setNamingConvention(new NumericalNamingConvention(filename.substring(0, filename
						.lastIndexOf("."))));
				writer.loadProject(project);
			} catch (IncompleteTridasDataException e) {
				e.printStackTrace();
			} catch (ConversionWarningException e) {
			} catch (UnrepresentableTridasDataException e) {
			}
			
			// Actually save file(s) to disk
			writer.saveAllToDisk(outputLocation);
		}
	}
	
}
