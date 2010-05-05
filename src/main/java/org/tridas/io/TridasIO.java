/**
 * Created on Apr 8, 2010, 8:09:39 PM
 */
package org.tridas.io;

import java.util.ArrayList;
import java.util.HashMap;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;
import org.tridas.io.formats.belfastapple.BelfastAppleReader;
import org.tridas.io.formats.belfastarchive.BelfastArchiveReader;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.heidelberg.HeidelbergReader;
import org.tridas.io.formats.sheffield.SheffieldReader;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.formats.tridas.TridasWriter;
import org.tridas.io.formats.trims.TrimsReader;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonWriter;

/**
 * For getting a file converter based on extension, later can do things with storing default
 * fields
 * @author daniel
 *
 */
public class TridasIO {
	
	private static final SimpleLogger log = new SimpleLogger(TridasIO.class);
	private static final HashMap<String, TridasIOEntry> converterMap = new HashMap<String, TridasIOEntry>();
	private static final HashMap<String, String> extensionMap = new HashMap<String, String>();
	
	private static boolean charsetDetection = false;
	
	static {
		// register file readers/writers
		registerFileReader(BelfastAppleReader.class);
		registerFileReader(BelfastArchiveReader.class);
		registerFileReader(CatrasReader.class);
		registerFileReader(HeidelbergReader.class);
		registerFileReader(SheffieldReader.class);
		registerFileReader(TridasReader.class);
		registerFileReader(TrimsReader.class);
		registerFileReader(TucsonReader.class);
		
		registerFileWriter(TridasWriter.class);
		registerFileWriter(TucsonWriter.class);
	}
	
	public static void setCharsetDetection(boolean argCharsetDetection) {
		charsetDetection = argCharsetDetection;
	}

	public static boolean isCharsetDetection() {
		return charsetDetection;
	}

	/**
	 * Register a reader
	 * @param argReader
	 */
	public synchronized static void registerFileReader(Class<? extends IDendroFileReader> argReader){
		// test to see if we can make an instance
		IDendroFileReader reader;
		try {
			reader = argReader.newInstance();
		} catch (InstantiationException e) {
			// TODO locale
			log.error(I18n.getText("fileio.missingEmptyConstructor",argReader.getName()));
			log.dbe(DebugLevel.L2_ERROR, e);
			return;
		} catch (IllegalAccessException e) {
			log.error(I18n.getText("fileio.creationError",argReader.getName()));
			log.dbe(DebugLevel.L2_ERROR, e);
			return;
		}
		
		// it worked, so get filetypes
		String[] filetypes = reader.getFileExtensions();
		String name = reader.getName();
		
		if(filetypes == null){
			log.error("File extension was null");
			return;
		}
		
		TridasIOEntry entry = converterMap.get(name);
		if(entry == null){
			entry = new TridasIOEntry();
			entry.fileReader = argReader;
			entry.formatName = name;
			converterMap.put(name, entry);
		}else{
			if(entry.fileReader == null){
				entry.fileReader = argReader;
			}else{
				// TODO locale
				log.warn(I18n.getText("fileio.replaceReader",name));
				//throw new RuntimeException("Cannot register another reader for "
				// don't throw an exception, just use new reader
				entry.fileReader = argReader;
			}
			if(!name.equals(entry.formatName)){
				log.warn("Name in entry '"+entry.formatName+"' does not match format name for reader '"+
						name+"'.  Replacing.");
				entry.formatName = name;
			}
		}
		
		
		for(String filetype : filetypes){
			String old = extensionMap.put(filetype,name);
			if(old != null && !name.equals(old)){
				log.warn("Extension "+filetype+" already mapped to "+old+".  Replacing with "+name);
			}
		}
	}
	
	/**
	 * Register a writer
	 * @param argWriter
	 */
	public synchronized static void registerFileWriter(Class<? extends IDendroCollectionWriter> argWriter){
		// test to see if we can make an instance
		IDendroCollectionWriter writer;
		try {
			writer = argWriter.newInstance();
		} catch (InstantiationException e) {
			// TODO locale
			log.error(I18n.getText("fileio.missingEmptyConstructor",argWriter.getName()));
			log.dbe(DebugLevel.L2_ERROR, e);
			return;
		} catch (IllegalAccessException e) {
			log.error(I18n.getText("fileio.creationError",argWriter.getName()));
			log.dbe(DebugLevel.L2_ERROR, e);
			return;
		}
		
		String filetype = writer.getFileExtension();
		String name = writer.getName();
		
		TridasIOEntry entry = converterMap.get(name);
		if(entry == null){
			entry = new TridasIOEntry();
			entry.fileWriter = argWriter;
			entry.formatName = name;
			converterMap.put(name, entry);
		}else{
			if(entry.fileWriter == null){
				entry.fileWriter = argWriter;
			}else{
				// TODO locale
				log.warn(I18n.getText("fileio.replaceWriter",name));
				//throw new RuntimeException("Cannot register another reader for "
				// don't throw an exception, just use new reader
				entry.fileWriter = argWriter;
			}
			if(!name.equals(entry.formatName)){
				log.warn("Name in entry '"+entry.formatName+"' does not match format name for writer '"+
						name+"'.  Replacing.");
				entry.formatName = name;
			}
		}
		
		
		String old = extensionMap.put(filetype,name);
		if(old != null && !name.equals(old)){
			log.warn("Extension "+filetype+" already mapped to "+old+".  Replacing with "+name);
		}
	}
	
	public synchronized static IDendroCollectionWriter getFileWriter(String argFormatName){
		TridasIOEntry e = converterMap.get(argFormatName);
		if(e == null || e.fileWriter == null){
			return null;
		}
		try {
			return e.fileWriter.newInstance();
		} catch (Exception e1){
			log.error(I18n.getText("fileio.creationError",e.fileWriter.getName()));
			log.dbe(DebugLevel.L2_ERROR, e1);
			return null;
		}
	}
	
	public synchronized static IDendroFileReader getFileReader(String argFormatName){
		TridasIOEntry e = converterMap.get(argFormatName);
		if(e == null || e.fileReader == null){
			return null;
		}
		try {
			return e.fileReader.newInstance();
		} catch (Exception e1){
			log.error(I18n.getText("fileio.creationError",e.fileReader.getName()));
			log.dbe(DebugLevel.L2_ERROR, e1);
			return null;
		}
	}
	
	public synchronized static IDendroFileReader getFileReaderFromExtension(String argExtension){
		return getFileReader(extensionMap.get(argExtension));
	}
	
	public synchronized static IDendroCollectionWriter getFileWriterFromExtension(String argExtension){
		return getFileWriter(extensionMap.get(argExtension));
	}
	
	public synchronized static String[] getSupportedReadingFormats(){
		ArrayList<String> list = new ArrayList<String>();
		for(String extension : converterMap.keySet()){
			TridasIOEntry entry = converterMap.get(extension);
			if(entry.fileReader != null){
				list.add(extension);
			}
		}
		return list.toArray(new String[0]);
	}
	
	public synchronized static String[] getSupportedWritingFormats(){
		ArrayList<String> list = new ArrayList<String>();
		for(String extension : converterMap.keySet()){
			TridasIOEntry entry = converterMap.get(extension);
			if(entry.fileWriter != null){
				list.add(extension);
			}
		}
		return list.toArray(new String[0]);
	}
	
	private static class TridasIOEntry{
		Class<? extends IDendroFileReader> fileReader = null;
		Class<? extends IDendroCollectionWriter> fileWriter = null;
		String formatName = null;
	}
}
