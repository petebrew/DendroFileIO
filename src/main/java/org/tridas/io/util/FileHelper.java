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
package org.tridas.io.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.util.UnicodeBOMInputStream;
import org.tridas.io.util.FilePermissionException.PermissionType;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Helps working with most files.
 * 
 * @author daniel
 */
public class FileHelper {
	
	private final static Logger log = LoggerFactory.getLogger(FileHelper.class);
	
	final static int[] illegalChars = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 34, 42, 47, 58, 60, 62, 63, 92, 124};
	
	public final String envPath;
	
	/**
	 * Replace special characters in a filename.  String filename should be the actual filename and not the full path
	 * 
	 * @param filename
	 * @return
	 */
	public static String sanitiseFilename(String badFileName)
	{
		if(badFileName==null) return null;
		if(badFileName.length()==0) return "";
		
		StringBuilder cleanName = new StringBuilder();
	    for (int i = 0; i < badFileName.length(); i++) {
	        int c = (int)badFileName.charAt(i);
	        if (Arrays.binarySearch(illegalChars, c) < 0) {
	            cleanName.append((char)c);
	        }
	    }
	    return cleanName.toString();
	}
	
	public FileHelper() {
		try {
			envPath = System.getProperty("user.dir");
		} catch (Exception e) {
			throw new RuntimeException("Error getting property 'user.dir'");
		}
	}
	
	public FileHelper(String argRootDataPath) {
		envPath = argRootDataPath;
	}
	
	public File outputFile() {
		return IOUtils.outputFile("Save as...", null);
	}
	
	public File inputFile() {
		return IOUtils.inputFile("Select a file...", null);
	}
	
	/**
	 * I want to print lines to a file.
	 * @throws FilePermissionException 
	 */
	public PrintWriter createWriter(String filename) throws FilePermissionException, Exception {
		return IOUtils.createWriter(saveFile(filename));
	}
	
	/**
	 * I want to read lines from a file.
	 */
	public BufferedReader createReader(String filename) {
		try {
			InputStream is = createInput(filename);
			if (is == null) {
				log.error(filename + " does not exist or could not be read");
				return null;
			}
			return IOUtils.createReader(is);
			
		} catch (Exception e) {
			if (filename == null) {
				log.error("Filename passed to reader() was null.  Why?");
			}
			else {
				log.error("Couldn't create a reader for " + filename);
			}
		}
		return null;
	}
	
	public String[] loadStringsFromDetectedCharset(String argFilename) {
		InputStream is = createInput(argFilename);
		if (is == null) {
			log.error("The file '" + argFilename + "' " + "is missing or inaccessible, make sure "
					+ "the URL is valid or that the file is in the same" + "directory as the jar and is readable.");
		}
		byte[] bytes = IOUtils.loadBytes(is);
		CharsetDetector detector = new CharsetDetector();
		CharsetMatch match;
		try {
			detector.setText(bytes);
			match = detector.detect();
			
			log.debug("Best charset match for file '" + argFilename + "' is " + match.getName() + " ("
					+ match.getLanguage() + ") with a confidence of " + match.getConfidence() + "%");
			String all = match.getString();
			return all.split("\n");
		} catch (IOException e) {
			log.error("Error while detecting or decoding charset on file: " + argFilename, e);
		}
		return null;
	}
	
	/**
	 * Load data from a file and shove it into a String array.
	 * <P>
	 * Exceptions are handled internally, when an error, occurs, an exception is printed
	 * to the console and 'null' is returned, but the program continues running. This is a
	 * tradeoff between 1) showing the user that there was a problem but 2) not requiring
	 * that all i/o code is contained in try/catch blocks, for the sake of new users (or
	 * people who are just trying to get things done in a "scripting" fashion). If you
	 * want to handle exceptions, use Java methods for I/O.
	 */
	public String[] loadStrings(String filename) {
		InputStream is = createInput(filename);
		if (is != null) {
			return IOUtils.loadStrings(is);
		}
		
		log.error("The file '" + filename + "' " + "is missing or inaccessible, make sure "
				+ "the URL is valid or that the file is in the same" + "directory as the jar and is readable.");
		return null;
	}
	
	public String[] loadStrings(String filename, String argEncoding) throws UnsupportedEncodingException {
		
        UnicodeBOMInputStream is = null;
		try {
			is = new UnicodeBOMInputStream(createInput(filename));
			is.skipBOM();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

		if (is != null) {
			return IOUtils.loadStrings(is, argEncoding);
		}
		
		log.error("The file '" + filename + "' " + "is missing or inaccessible, make sure "
				+ "the URL is valid or that the file is in the same" + "directory as the jar and is readable.");
		return null;
	}
	
	public byte[] loadBytes(String filename) {
		InputStream is = createInput(filename);
		if (is != null) {
			return IOUtils.loadBytes(is);
		}
		
		log.error("The file '" + filename + "' " + "is missing or inaccessible, make sure "
				+ "the URL is valid or that the file is in the same" + "directory as the jar and is readable.");
		return null;
	}
	
	/**
	 * Call createInput() without automatic gzip decompression.
	 */
	public InputStream createInputRaw(String filename) {
		InputStream stream = null;
		
		if (filename == null) {
			return null;
		}
		
		if (filename.length() == 0) {
			return null;
		}
		
		// safe to check for this as a url first
		try {
			URL url = new URL(filename);
			stream = url.openStream();
			return stream;
			
		} catch (MalformedURLException mfue) {
			// not a url, that's fine
			
		} catch (FileNotFoundException fnfe) {
			// Java 1.5 likes to throw this when URL not available.
		} catch (IOException e) {
			// shouldn't be throwing exception
			log.error("Error getting input", e);
			return null;
		}
		
		// using getClassLoader() prevents java from converting dots
		// to slashes or requiring a slash at the beginning.
		// (a slash as a prefix means that it'll load from the root of
		// the jar, rather than trying to dig into the package location)
		ClassLoader cl = getClass().getClassLoader();
		
		// when used with an online script, also need to check without the
		// data folder, in case it's not in a subfolder called 'data'
		stream = cl.getResourceAsStream(filename);
		if (stream != null) {
			String cn = stream.getClass().getName();
			if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
				return stream;
			}
		}
		
		// handle case sensitivity check
		try {
			// first see if it's in a data folder
			File file = new File(fullPath(filename));
			if (!file.exists()) {
				// next see if it's just in this folder
				file = new File(envPath, filename);
			}
			if (file.exists()) {
				try {
					String filePath = file.getCanonicalPath();
					String filenameActual = new File(filePath).getName();
					// make sure there isn't a subfolder prepended to the name
					String filenameShort = new File(filename).getName();
					// if the actual filename is the same, but capitalized
					// differently, warn the user.
					if (!filenameActual.equals(filenameShort)) {
						log.error("This file is named '" + filenameActual + "' not '" + filename
								+ "'. Re-name it or change your code.");
						return null;
					}
				} catch (IOException e) {}
			}
			
			// if this file is ok, may as well just load it
			stream = new FileInputStream(file);
			if (stream != null) {
				return stream;
			}
			
			// have to break these out because a general Exception might
			// catch the RuntimeException being thrown above
		} catch (IOException ioe) {} catch (SecurityException se) {}
		
		try {
			// attempt to load from a local file, used when running as
			// an application, or as a signed applet
			try { // first try to catch any security exceptions
				try {
					stream = new FileInputStream(fullPath(filename));
					if (stream != null) {
						return stream;
					}
				} catch (IOException e2) {}
				
				try {
					stream = new FileInputStream(filename);
					if (stream != null) {
						return stream;
					}
				} catch (IOException e1) {}
				
			} catch (SecurityException se) {} // online?
			
		} catch (Exception e) {
			log.error("Error getting input stream", e);
		}
		return null;
	}
	
	/**
	 * Simplified method to open a Java InputStream.
	 * <P>
	 * This method is useful if you want to use the facilities provided by PApplet to
	 * easily open things from the data folder or from a URL, but want an InputStream
	 * object so that you can use other Java methods to take more control of how the
	 * stream is read.
	 * <P>
	 * If the requested item doesn't exist, null is returned
	 * <P>
	 * If not online, this will also check to see if the user is asking for a file whose
	 * name isn't properly capitalized. This helps prevent issues when a sketch is
	 * exported to the web, where case sensitivity matters, as opposed to Windows and the
	 * Mac OS default where case sensitivity is preserved but ignored.
	 * <P>
	 * It is strongly recommended that libraries use this method to open data files, so
	 * that the loading sequence is handled in the same way as functions like loadBytes(),
	 * loadImage(), etc.
	 * <P>
	 * The filename passed in can be:
	 * <UL>
	 * <LI>A URL, for instance createInput("http://processing.org/");
	 * <LI>A file in the same folder as the jar
	 * <LI>A file in the jar
	 * <LI>Another file to be opened locally (when running as an application)
	 * </UL>
	 */
	public InputStream createInput(String filename) {
		InputStream input = createInputRaw(filename);
		if ((input != null) && filename.toLowerCase().endsWith(".gz")) {
			try {
				return new GZIPInputStream(input);
			} catch (IOException e) {
				log.error("Error creating input stream", e);
				return null;
			}
		}
		return input;
	}
	
	public void saveStrings(String filename, String strings[]) throws FilePermissionException, Exception {
		IOUtils.saveStrings(saveFile(filename), strings);
	}
	
	public void saveStrings(String filename, String strings[], String argEncoding) throws UnsupportedEncodingException, FilePermissionException, Exception {
		IOUtils.saveStrings(saveFile(filename), strings, argEncoding);
	}
	
	/**
	 * Saves bytes to a file to the Environment path. Usually where the jar is located,
	 * but
	 * can be customized with the {@link #FileHelper(String)} constructor. The filename
	 * can be a
	 * relative path, i.e. "hey/more/directories/wowbytes.txt" would save the "hey"
	 * directory in the Environment path.
	 * @throws FilePermissionException 
	 */
	public void saveBytes(String filename, byte buffer[]) throws FilePermissionException, Exception {
		IOUtils.saveBytes(saveFile(filename), buffer);
	}
	
	/**
	 * Identical to the other saveStream(), but writes to a File object, for
	 * greater control over the file location. Note that unlike other api
	 * methods, this will not automatically uncompress gzip files.
	 */
	public void saveStream(File targetFile, String sourceLocation) {
		IOUtils.saveStream(targetFile, createInputRaw(sourceLocation));
	}
	
	/**
	 * Save the contents of a stream to a file usually where the jar is located,
	 * but can be customized with the {@link #FileHelper(String)} constructor. This is
	 * basically saveBytes(blah, loadBytes()), but done in a less confusing
	 * manner.
	 * @throws FilePermissionException 
	 */
	public void saveStream(String targetFilename, String sourceLocation) throws FilePermissionException, Exception {
		saveStream(saveFile(targetFilename), sourceLocation);
	}
	
	/**
	 * Similar to createInput(), this creates a Java
	 * OutputStream for a given filename or path. The file will be created where
	 * the jar is located folder, or customized with {@link #FileHelper(String)}.
	 * <p/>
	 * If the path does not exist, intermediate folders will be created. If an exception
	 * occurs, it will be printed to the console, and null will be returned.
	 * @throws FilePermissionException 
	 */
	public OutputStream createOutput(String filename) throws FilePermissionException, Exception {
		return IOUtils.createOutput(saveFile(filename));
	}
	
	/**
	 * Prepend the Environment path to the filename (or path) that is passed
	 * in. Note that when running as an applet inside a web browser,
	 * the Environment path will be set to null, because security restrictions prevent
	 * applets from accessing that information.
	 */
	public String fullPath(String where) {
		if (envPath == null) {
			throw new RuntimeException("The applet was not initiated properly, "
					+ "or security restrictions prevented " + "it from determining its path.");
		}
		// isAbsolute() could throw an access exception, but so will writing
		// to the local disk using the jar path, so this is safe here.
		try {
			if (new File(where).isAbsolute()) {
				return where;
			}
		} catch (Exception e) {}
		
		return envPath + File.separator + where;
	}
	
	public File createFile(String where) {
		return new File(fullPath(where));
	}
	
	/**
	 * Returns a path inside the applet folder to save to. Like jarPath(), but
	 * creates any in-between folders so that things save properly.
	 */
	public String savePath(String where) {
		String filename = fullPath(where);
		IOUtils.createPath(filename);
		return filename;
	}
	
	/**
	 * Identical to savePath(), but returns a File object.
	 * @throws Exception 
	 */
	public File saveFile(String where) throws FilePermissionException, Exception {
		File file = new File(savePath(where));
		
		if(file.exists())
		{	
			if(file.canWrite())
			{
				return file;
			}
			else
			{
				throw new FilePermissionException(file, PermissionType.WRITE);
			}
		}
		else
		{

			try {
				if(file.createNewFile())
				{
					return file;
				}
				else
				{
					throw new Exception("Cannot create "+file.getAbsolutePath()+".  File already exists.");
				}
			} catch (Exception e) {
				throw new FilePermissionException(file, PermissionType.WRITE);
			}

		}
	}
	
	
	/**
	 * Determine whether a file contains non-printable characters,
	 * suggesting that its a binary file.
	 * 
	 * @param argLines
	 * @return
	 */
	public static Boolean isBinary(String[] argLines)
	{
		
		return false;
	}
	
}
