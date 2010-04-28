/**
 * Created on Apr 8, 2010, 8:09:39 PM
 */
package org.tridas.io;

import java.util.ArrayList;
import java.util.HashMap;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;

/**
 * For getting a file converter based on extension, later can do things with storing default
 * fields
 * @author daniel
 *
 */
public class TridasIO {
	
	private static final SimpleLogger log = new SimpleLogger(TridasIO.class);
	
	private static HashMap<String, TridasIOEntry> converterMap = new HashMap<String, TridasIOEntry>();
	
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
		
		if(filetypes == null || filetypes.length == 0 || argReader == null){
			return;
		}
		
		for(String filetype: filetypes){
			TridasIOEntry entry = converterMap.get(filetype);
			if(entry == null){
				entry = new TridasIOEntry();
				entry.fileReader = argReader;
				converterMap.put(filetype, entry);
			}else{
				if(entry.fileReader == null){
					entry.fileReader = argReader;
				}else{
					// TODO locale
					log.error(I18n.getText("fileio.replaceReader",filetype));
					//throw new RuntimeException("Cannot register another reader for "
					// don't throw an exception, just use new reader
					entry.fileReader = argReader;
				}
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
		
		String[] filetypes = writer.getFileExtensions();
		
		if(filetypes == null || filetypes.length == 0 || argWriter == null){
			return;
		}
		
		for(String filetype: filetypes){
			TridasIOEntry entry = converterMap.get(filetype);
			if(entry == null){
				entry = new TridasIOEntry();
				entry.fileWriter = argWriter;
				converterMap.put(filetype, entry);
			}else{
				if(entry.fileWriter == null){
					entry.fileWriter = argWriter;
				}else{
					log.error(I18n.getText("fileio.replaceWriter",filetype));
					//throw new RuntimeException("Cannot register another reader for "
					// don't throw an exception, just use new writer
					entry.fileWriter = argWriter;
				}
			}
		}
	}
	
	public synchronized static IDendroCollectionWriter getFileWriter(String argFileExtension){
		Class<? extends IDendroCollectionWriter> c = converterMap.get(argFileExtension).fileWriter;
		if(c == null){
			return null;
		}
		try {
			return c.newInstance();
		} catch (Exception e){
			log.error(I18n.getText("fileio.creationError",c.getName()));

			log.dbe(DebugLevel.L2_ERROR, e);
			return null;
		}
	}
	
	public synchronized static IDendroFileReader getFileReader(String argFileExtension){
		Class<? extends IDendroFileReader> c = converterMap.get(argFileExtension).fileReader;
		if(c == null){
			return null;
		}
		try {
			return c.newInstance();
		} catch (Exception e){
			log.error("Could not create new instance, this should not happen because we already" +
					  " created a new instance when registering the class: "+c.getName());
			log.dbe(DebugLevel.L2_ERROR, e);
			return null;
		}
	}
	
	public synchronized static String[] getSupportedReadingExtensions(){
		ArrayList<String> list = new ArrayList<String>();
		for(String extension : converterMap.keySet()){
			TridasIOEntry entry = converterMap.get(extension);
			if(entry.fileReader != null){
				list.add(extension);
			}
		}
		return list.toArray(new String[0]);
	}
	
	public synchronized static String[] getSupportedWritingExtensions(){
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
	}
}
