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
package org.tridas.io;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;
import org.tridas.io.formats.belfastapple.BelfastAppleReader;
import org.tridas.io.formats.belfastapple.BelfastAppleWriter;
import org.tridas.io.formats.belfastarchive.BelfastArchiveReader;
import org.tridas.io.formats.besancon.BesanconReader;
import org.tridas.io.formats.besancon.BesanconWriter;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.csv.CSVWriter;
import org.tridas.io.formats.excelmatrix.ExcelMatrixWriter;
import org.tridas.io.formats.heidelberg.HeidelbergReader;
import org.tridas.io.formats.heidelberg.HeidelbergWriter;
import org.tridas.io.formats.sheffield.SheffieldReader;
import org.tridas.io.formats.sheffield.SheffieldWriter;
import org.tridas.io.formats.windendro.WinDendroReader;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.trims.TrimsReader;
import org.tridas.io.formats.trims.TrimsWriter;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonWriter;
import org.tridas.io.formats.tucsoncompact.TucsonCompactReader;
import org.tridas.io.formats.tucsoncompact.TucsonCompactWriter;
import org.tridas.io.formats.vformat.VFormatReader;
import org.tridas.io.formats.vformat.VFormatWriter;
import org.tridas.io.formats.topham.TophamReader;
import org.tridas.io.formats.topham.TophamWriter;
import org.tridas.io.formats.nottingham.NottinghamReader;
import org.tridas.io.formats.nottingham.NottinghamWriter;
import org.tridas.io.formats.past4.Past4Reader;
import org.tridas.io.formats.past4.Past4Writer;
import org.tridas.io.formats.corina.CorinaWriter;
import org.tridas.io.formats.corina.CorinaReader;



/**
 * Used to get readers/writers from name or extension. In order to include your
 * writer/reader in the list,
 * register it with {@link #registerFileReader(Class)} and
 * {@link #registerFileWriter(Class)}. Also, this class
 * stores global properties for the library, such as charset detection when loading files.
 * 
 * @author daniel
 */
public class TridasIO {
	
	private static final SimpleLogger log = new SimpleLogger(TridasIO.class);
	private static final HashMap<String, TridasIOEntry> converterMap = new HashMap<String, TridasIOEntry>();
	private static final HashMap<String, String> extensionMap = new HashMap<String, String>();
	
	private static String readingCharset = Charset.defaultCharset().displayName();
	private static String writingCharset = Charset.defaultCharset().displayName();
	private static boolean charsetDetection = false;
	
	static {
		// register file readers/writers
		registerFileReader(BelfastAppleReader.class);
		registerFileReader(BelfastArchiveReader.class);
		registerFileReader(BesanconReader.class);
		registerFileReader(CatrasReader.class);
		registerFileReader(CorinaReader.class);
		registerFileReader(HeidelbergReader.class);
		registerFileReader(NottinghamReader.class);
		//registerFileReader(Past4Reader.class);
		registerFileReader(SheffieldReader.class);
		registerFileReader(TophamReader.class);
		registerFileReader(TridasReader.class);
		registerFileReader(TrimsReader.class);
		registerFileReader(TucsonReader.class);
		registerFileReader(TucsonCompactReader.class);
		registerFileReader(VFormatReader.class);
		registerFileReader(WinDendroReader.class);
		
		registerFileWriter(BelfastAppleWriter.class);
		registerFileWriter(BesanconWriter.class);
		registerFileWriter(CorinaWriter.class);
		registerFileWriter(CSVWriter.class);
		registerFileWriter(ExcelMatrixWriter.class);
		registerFileWriter(HeidelbergWriter.class);
		registerFileWriter(NottinghamWriter.class);
		registerFileWriter(SheffieldWriter.class);
		registerFileWriter(TophamWriter.class);
		registerFileWriter(TridasWriter.class);
		registerFileWriter(TrimsWriter.class);
		registerFileWriter(TucsonWriter.class);
		registerFileWriter(TucsonCompactWriter.class);
		registerFileWriter(VFormatWriter.class);
		registerFileWriter(Past4Writer.class);

	}
	
	/**
	 * Sets if charset detection is used when loading files. Usually this doesn't
	 * matter. Default of true.
	 * 
	 * @param argCharsetDetection
	 */
	public static void setCharsetDetection(boolean argCharsetDetection) {
		charsetDetection = argCharsetDetection;
		if(charsetDetection == true){
			readingCharset = null;
		}
	}
	
	/**
	 * If the library detects the charset when loading files.
	 * 
	 * @return
	 */
	public static boolean isCharsetDetection() {
		return charsetDetection;
	}
	
	/**
	 * Register a reader.
	 * 
	 * @param argReader
	 */
	public synchronized static void registerFileReader(Class<? extends AbstractDendroFileReader> argReader) {
		// test to see if we can make an instance
		AbstractDendroFileReader reader;
		try {
			reader = argReader.newInstance();
		} catch (InstantiationException e) {
			log.error(I18n.getText("fileio.missingEmptyConstructor", argReader.getName()));
			log.dbe(DebugLevel.L2_ERROR, e);
			return;
		} catch (IllegalAccessException e) {
			log.error(I18n.getText("fileio.creationError", argReader.getName()));
			log.dbe(DebugLevel.L2_ERROR, e);
			return;
		}
		
		// it worked, so get filetypes
		String[] filetypes = reader.getFileExtensions();
		String name = reader.getShortName();
		
		if (filetypes == null) {
			log.error(I18n.getText("fileio.fileExtensionNull", argReader.getName()));
			return;
		}
		
		TridasIOEntry entry = converterMap.get(name.toLowerCase());
		
		if (entry == null) {
			entry = new TridasIOEntry();
			entry.fileReader = argReader;
			entry.formatName = name;
			converterMap.put(name.toLowerCase(), entry);
		}
		else {
			if (entry.fileReader == null) {
				entry.fileReader = argReader;
			}
			else {
				log.warn(I18n.getText("fileio.replaceReader", name));
				// throw new RuntimeException("Cannot register another reader for "
				// don't throw an exception, just use new reader
				entry.fileReader = argReader;
			}
			if (!name.equals(entry.formatName)) {
				log.warn("Name in entry '" + entry.formatName + "' does not match format name for reader '" + name
						+ "'.  Replacing.");
				entry.formatName = name;
			}
		}
		
		for (String filetype : filetypes) {
			String old = extensionMap.put(filetype.toLowerCase(), name);
			if (old != null && !name.equals(old)) {
				log.warn("Extension " + filetype + " already mapped to " + old + ".  Replacing with " + name);
			}
		}
	}
	
	/**
	 * Register a writer
	 * 
	 * @param argWriter
	 */
	public synchronized static void registerFileWriter(Class<? extends AbstractDendroCollectionWriter> argWriter) {
		// test to see if we can make an instance
		AbstractDendroCollectionWriter writer;
		try {
			writer = argWriter.newInstance();
		} catch (InstantiationException e) {
			log.error(I18n.getText("fileio.missingEmptyConstructor", argWriter.getName()));
			log.dbe(DebugLevel.L2_ERROR, e);
			return;
		} catch (IllegalAccessException e) {
			log.error(I18n.getText("fileio.creationError", argWriter.getName()));
			log.dbe(DebugLevel.L2_ERROR, e);
			return;
		}
		
		String name = writer.getShortName();
		
		TridasIOEntry entry = converterMap.get(name.toLowerCase());
		if (entry == null) {
			entry = new TridasIOEntry();
			entry.fileWriter = argWriter;
			entry.formatName = name;
			converterMap.put(name.toLowerCase(), entry);
		}
		else {
			if (entry.fileWriter == null) {
				entry.fileWriter = argWriter;
			}
			else {
				log.warn(I18n.getText("fileio.replaceWriter", name));
				// throw new RuntimeException("Cannot register another reader for "
				// don't throw an exception, just use new reader
				entry.fileWriter = argWriter;
			}
			if (!name.equals(entry.formatName)) {
				log.warn("Name in entry '" + entry.formatName + "' does not match format name for writer '" + name
						+ "'.  Replacing.");
				entry.formatName = name;
			}
		}
	}
	
	/**
	 * Get a file writer from the format name.
	 * 
	 * @param argFormatName
	 * @see #getSupportedWritingFormats()
	 * @return
	 */
	public synchronized static AbstractDendroCollectionWriter getFileWriter(String argFormatName) {
		TridasIOEntry e = converterMap.get(argFormatName.toLowerCase());
		if (e == null || e.fileWriter == null) {
			return null;
		}
		try {
			return e.fileWriter.newInstance();
		} catch (Exception e1) {
			log.error(I18n.getText("fileio.creationError", e.fileWriter.getName()));
			log.dbe(DebugLevel.L2_ERROR, e1);
			return null;
		}
	}
	
	/**
	 * Get a file reader from the format name.
	 * 
	 * @param argFormatName
	 * @see #getSupportedReadingFormats()
	 * @return
	 */
	public synchronized static AbstractDendroFileReader getFileReader(String argFormatName) {
		TridasIOEntry e = converterMap.get(argFormatName.toLowerCase());
		if (e == null || e.fileReader == null) {
			return null;
		}
		try {
			return e.fileReader.newInstance();
		} catch (Exception e1) {
			log.error(I18n.getText("fileio.creationError", e.fileReader.getName()));
			log.dbe(DebugLevel.L2_ERROR, e1);
			return null;
		}
	}
	
	/**
	 * Gets the reader from the given extension.
	 * 
	 * @param argExtension
	 * @return
	 */
	public synchronized static AbstractDendroFileReader getFileReaderFromExtension(String argExtension) {
		if (!extensionMap.containsKey(argExtension.toLowerCase())) {
			return null;
		}
		return getFileReader(extensionMap.get(argExtension.toLowerCase()).toLowerCase());
	}
	
	/**
	 * Gets the writer from the given extension.
	 * 
	 * @param argExtension
	 * @return
	 */
	public synchronized static AbstractDendroCollectionWriter getFileWriterFromExtension(String argExtension) {
		if (!extensionMap.containsKey(argExtension.toLowerCase())) {
			return null;
		}
		return getFileWriter(extensionMap.get(argExtension.toLowerCase()).toLowerCase());
	}
	
	/**
	 * Get all supported reading formats.
	 * 
	 * @return
	 */
	public synchronized static String[] getSupportedReadingFormats() {
		ArrayList<String> list = new ArrayList<String>();
		for (String extension : converterMap.keySet()) {
			TridasIOEntry entry = converterMap.get(extension);
			if (entry.fileReader != null) {
				list.add(entry.formatName);
			}
		}
		Collections.sort(list);
		return list.toArray(new String[0]);
	}
	
	/**
	 * Get all supported writing formats
	 * 
	 * @return
	 */
	public synchronized static String[] getSupportedWritingFormats() {
		ArrayList<String> list = new ArrayList<String>();
		for (String extension : converterMap.keySet()) {
			TridasIOEntry entry = converterMap.get(extension);
			if (entry.fileWriter != null) {
				list.add(entry.formatName);
			}
		}
		Collections.sort(list);
		return list.toArray(new String[0]);
	}
	
	/**
	 * sets the reading 
	 * @param argCharset
	 *            the charset to read with
	 * @throws IllegalCharsetNameException
	 *             - If the given charset name is illegal
	 * @throws UnsupportedCharsetException
	 *             - If no support for the named charset is available in this instance of
	 *             the Java virtual machine
	 */
	public static void setReadingCharset(String argCharset) {
		if (argCharset != null) {
			// check to see if we can get the charset
			Charset.forName(argCharset);
		}
		TridasIO.readingCharset = argCharset;
		if(readingCharset != null){
			charsetDetection = false;
		}
	}
	
	/**
	 * @return the charset
	 */
	public static String getReadingCharset() {
		return readingCharset;
	}
	
	/**
	 * @param argCharset
	 *            the writingCharset to set
	 */
	public static void setWritingCharset(String argCharset) {
		if (argCharset != null) {
			// check to see if we can get the charset
			Charset.forName(argCharset);
		}
		TridasIO.writingCharset = argCharset;
	}
	
	/**
	 * @return the writingCharset
	 */
	public static String getWritingCharset() {
		return writingCharset;
	}
	
	private static class TridasIOEntry {
		Class<? extends AbstractDendroFileReader> fileReader = null;
		Class<? extends AbstractDendroCollectionWriter> fileWriter = null;
		String formatName = null;
	}
}
