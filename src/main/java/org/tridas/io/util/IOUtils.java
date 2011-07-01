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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Static IO utility methods
 * 
 * @author daniel
 */
public class IOUtils {
	
	private final static Logger log = LoggerFactory.getLogger(IOUtils.class);
	
	private IOUtils() {}
	
	/*
	 * #########################################
	 * ######### FILE/FOLDER SELECTION #########
	 * #########################################
	 */

	public static File inputFile(JFrame argParent) {
		return inputFile("Select a file...", argParent);
	}
	
	public static File[] inputFiles(JFrame argParent) {
		return inputFiles("Select a file...", argParent);
	}
	
	public static File outputFolder(JFrame argParent) {
		return outputFolder("Select a folder...", argParent);
	}
	
	/**
	 * The parentFrame is the Frame that will guide the placement of the prompt
	 * window. If no Frame is available, just pass in null.
	 */
	
	public static File inputFile(String argPrompt, JFrame argParentFrame) {
		if (argParentFrame == null) {
			argParentFrame = new JFrame(argPrompt);
		}
		JFileChooser fd = new JFileChooser();
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setMultiSelectionEnabled(false);
		int retValue = fd.showOpenDialog(argParentFrame);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			return fd.getSelectedFile();
		}
		else {
			return null;
		}
	}
	
	public static File[] inputFiles(String argPrompt, JFrame argParentFrame) {
		if (argParentFrame == null) {
			argParentFrame = new JFrame(argPrompt);
		}
		JFileChooser fd = new JFileChooser();
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setMultiSelectionEnabled(true);
		int retValue = fd.showOpenDialog(argParentFrame);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			return fd.getSelectedFiles();
		}
		else {
			return null;
		}
	}
	
	public static File outputFile(JFrame parentFrame) {
		return outputFile("Save as...", parentFrame);
	}
	
	public static File[] outputFiles(JFrame argParentFrame) {
		return outputFiles("Save as...", argParentFrame);
	}
	
	/**
	 * The parentFrame is the Frame that will guide the placement of the prompt
	 * window. If no Frame is available, just pass in null.
	 */
	public static File outputFile(String argPrompt, JFrame argParentFrame) {
		if (argParentFrame == null) {
			argParentFrame = new JFrame(argPrompt);
		}
		JFileChooser fd = new JFileChooser();
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setMultiSelectionEnabled(false);
		int retValue = fd.showSaveDialog(argParentFrame);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			return fd.getSelectedFile();
		}
		else {
			return null;
		}
	}
	
	public static File outputFolder(String argPrompt, JFrame argParentFrame) {
		if (argParentFrame == null) {
			argParentFrame = new JFrame(argPrompt);
		}
		JFileChooser fd = new JFileChooser();
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fd.setMultiSelectionEnabled(false);
		int retValue = fd.showSaveDialog(argParentFrame);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			return fd.getSelectedFile();
		}
		else {
			return null;
		}
	}
	
	public static File[] outputFiles(String argPrompt, JFrame argParentFrame) {
		if (argParentFrame == null) {
			argParentFrame = new JFrame(argPrompt);
		}
		JFileChooser fd = new JFileChooser();
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setMultiSelectionEnabled(true);
		int retValue = fd.showSaveDialog(argParentFrame);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			return fd.getSelectedFiles();
		}
		else {
			return null;
		}
	}
	
	/*
	 * #########################################
	 * ############ READERS/WRITERS ############
	 * #########################################
	 */

	/**
	 * I want to read lines from a file.
	 */
	public static BufferedReader createReader(File file) {
		try {
			return createReader(file, null);
		} catch (UnsupportedEncodingException e) {} // not gonna happen
		return null; // won't happen
	}
	
	/**
	 * I want to read lines from a file. with an encoding!
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static BufferedReader createReader(File file, String argEncoding) throws UnsupportedEncodingException {
		if (file == null) {
			throw new RuntimeException("File passed to createReader() was null. " + "Why are you doing this to me?");
		}
		try {
			InputStream is = new FileInputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			return createReader(is, argEncoding);
			
		} catch (Exception e) {
			if (e instanceof UnsupportedEncodingException) {
				throw (UnsupportedEncodingException) e;
			}
			log.error("Couldn't create a reader for " + file.getAbsolutePath(), e);
			throw new RuntimeException("Couldn't create a reader for " + file.getAbsolutePath());
		}
	}
	
	/**
	 * I want to read lines from a stream.
	 */
	public static BufferedReader createReader(InputStream input) {
		InputStreamReader isr = new InputStreamReader(input);
		return new BufferedReader(isr);
	}
	
	/**
	 * I want to read lines from a stream. with an encoding!
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static BufferedReader createReader(InputStream input, String argEncoding)
			throws UnsupportedEncodingException {
		InputStreamReader isr = new InputStreamReader(input, argEncoding);
		return new BufferedReader(isr);
	}
	
	/**
	 * I want to print lines to a file.
	 */
	public static PrintWriter createWriter(File file) {
		try {
			return createWriter(file, null);
		} catch (UnsupportedEncodingException e) {} // won't happen
		return null; // won't happen
	}
	
	/**
	 * I want to print lines to a file.
	 */
	public static PrintWriter createWriter(File file, String argEncoding) throws UnsupportedEncodingException {
		if (file == null) {
			log.error("File passed to createWriter() was null.  Why are you doing this to me?");
			throw new RuntimeException("File passed to createWriter() was null.  Why are you doing this to me?");
		}
		try {
			OutputStream output = new FileOutputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				output = new GZIPOutputStream(output);
			}
			return createWriter(output, argEncoding);
			
		} catch (Exception e) {
			if (e instanceof UnsupportedEncodingException) {
				throw (UnsupportedEncodingException) e;
			}
			log.error("Couldn't create a writer for " + file.getAbsolutePath(), e);
			throw new RuntimeException("Couldn't create a writer for " + file.getAbsolutePath());
		}
	}
	
	/**
	 * I want to print lines to a file.
	 */
	public static PrintWriter createWriter(OutputStream output) {
		try {
			return createWriter(output, null);
		} catch (UnsupportedEncodingException e) {} // won't happen
		return null;
	}
	
	/**
	 * I want to print lines to a file.
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static PrintWriter createWriter(OutputStream output, String argEncoding) throws UnsupportedEncodingException {
		OutputStreamWriter osw = new OutputStreamWriter(output, argEncoding);
		return new PrintWriter(osw);
	}
	
	/*
	 * #########################################
	 * ############## FILE INPUT ###############
	 * #########################################
	 */

	public static URL getFileInJarURL(String argFile) {
		
		return IOUtils.class.getClassLoader().getResource(argFile);
	}
	
	public static InputStream createInput(File file) {
		if (file == null) {
			throw new RuntimeException("File passed to createInput() was null.  Stop doing this!");
			
		}
		try {
			InputStream input = new FileInputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				return new GZIPInputStream(input);
			}
			return input;
			
		} catch (IOException e) {
			log.error("Couldn't createInput() for " + file.getAbsolutePath(), e);
			throw new RuntimeException("Couldn't createInput() for " + file.getAbsolutePath());
		}
	}
	
	public static byte[] loadBytes(InputStream input) {
		try {
			BufferedInputStream bis = new BufferedInputStream(input);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			int c = bis.read();
			while (c != -1) {
				out.write(c);
				c = bis.read();
			}
			return out.toByteArray();
			
		} catch (IOException e) {
			log.error("Could not load bytes", e);
		}
		return null;
	}
	
	public static String[] loadStrings(File file) {
		InputStream is = createInput(file);
		if (is != null) {
			return loadStrings(is);
		}
		return null;
	}
	
	public static String[] loadStrings(File file, String argEncoding) throws UnsupportedEncodingException {
		InputStream is = createInput(file);
		if (is != null) {
			return loadStrings(is, argEncoding);
		}
		return null;
	}
	
	public static String[] loadStrings(InputStream input) {
		try {
			return loadStrings(input, null);
		} catch (UnsupportedEncodingException e) {} // not gonna happen
		return null;
	}
	
	public static String[] loadStrings(InputStream input, String argEncoding) throws UnsupportedEncodingException {
		try {
			
			BufferedReader reader;
			
			if (argEncoding != null) {
				reader = new BufferedReader(new InputStreamReader(input, argEncoding));
			}
			else {
				reader = new BufferedReader(new InputStreamReader(input));
			}
			
			String lines[] = new String[100];
			int lineCount = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (lineCount == lines.length) {
					String temp[] = new String[lineCount << 1];
					System.arraycopy(lines, 0, temp, 0, lineCount);
					lines = temp;
				}
				lines[lineCount++] = line;
			}
			reader.close();
			
			if (lineCount == lines.length) {
				return lines;
			}
			
			// resize array to appropriate amount for these lines
			String output[] = new String[lineCount];
			System.arraycopy(lines, 0, output, 0, lineCount);
			return output;
			
		} catch (IOException e) {
			if (e instanceof UnsupportedEncodingException) {
				throw (UnsupportedEncodingException) e;
			}
			log.error("Could not load strings", e);
		}
		return null;
	}
	
	/*
	 * #########################################
	 * ############## FILE OUTPUT ##############
	 * #########################################
	 */

	public static OutputStream createOutput(File file) {
		try {
			return new FileOutputStream(file);
			
		} catch (IOException e) {
			log.error("Error creating output", e);
		}
		return null;
	}
	
	public static void saveStream(File targetFile, InputStream sourceStream) {
		File tempFile = null;
		
		try {
			File parentDir = targetFile.getParentFile();
			tempFile = File.createTempFile(targetFile.getName(), null, parentDir);
			
			BufferedInputStream bis = new BufferedInputStream(sourceStream, 16384);
			FileOutputStream fos = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, bytesRead);
			}
			
			bos.flush();
			bos.close();
			bos = null;
			
			if (!tempFile.renameTo(targetFile)) {
				log.error("Could not rename temporary file " + tempFile.getAbsolutePath());
			}
		} catch (IOException e) {
			if (tempFile != null) {
				tempFile.delete();
			}
			log.error("Could not save stream", e);
		}
	}
	
	/**
	 * Saves bytes to a specific File location specified by the user.
	 */
	public static void saveBytes(File file, byte buffer[]) {
		try {
			String filename = file.getAbsolutePath();
			createPath(filename);
			OutputStream output = new FileOutputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				output = new GZIPOutputStream(output);
			}
			saveBytes(output, buffer);
			output.close();
			
		} catch (IOException e) {
			log.error("error saving bytes to " + file, e);
		}
	}
	
	/**
	 * Spews a buffer of bytes to an OutputStream.
	 */
	public static void saveBytes(OutputStream output, byte buffer[]) {
		try {
			output.write(buffer);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveStrings(File file, String strings[]) {
		try {
			saveStrings(file, strings, "UTF-8");
		} catch (UnsupportedEncodingException e) {} // not gonna happen
	}
	
	public static void saveStrings(File file, String strings[], String argEncoding) throws UnsupportedEncodingException {
		try {
			String location = file.getAbsolutePath();
			createPath(location);
			OutputStream output = new FileOutputStream(location);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				output = new GZIPOutputStream(output);
			}
			saveStrings(output, strings, argEncoding);
			output.close();
			
		} catch (IOException e) {
			if (e instanceof UnsupportedEncodingException) {
				throw (UnsupportedEncodingException) e;
			}
			log.error("Error saving strings", e);
		}
	}
	
	public static void saveStrings(OutputStream output, String strings[]) {
		try {
			saveStrings(output, strings, null);
		} catch (UnsupportedEncodingException e) {} // not gonna happen
	}
	
	public static void saveStrings(OutputStream output, String strings[], String argEncoding)
			throws UnsupportedEncodingException {
		if (strings == null) {
			throw new NullPointerException("Strings to save cannot be null");
		}
		OutputStreamWriter osw;
		
		if (argEncoding != null) {
			osw = new OutputStreamWriter(output, argEncoding);
		}
		else {
			osw = new OutputStreamWriter(output);
		}
		
		PrintWriter writer = new PrintWriter(osw);
		for (int i = 0; i < strings.length; i++) {
			writer.println(strings[i]);
		}
		writer.flush();
	}
	
	/**
	 * Takes a path and creates any in-between folders if they don't already
	 * exist. Useful when trying to save to a subfolder that may not actually
	 * exist.
	 */
	public static void createPath(String filename) {
		File file = new File(filename);
		String parent = file.getParent();
		if (parent != null) {
			File unit = new File(parent);
			if (!unit.exists()) {
				unit.mkdirs();
			}
		}
	}
	
	public static Charset detectCharset(byte[] argBytes) {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(argBytes);
		CharsetMatch match = detector.detect();
		log.debug("Best charset match is " + match.getName() + " (" + match.getLanguage() + ") with a confidence of "
				+ match.getConfidence() + "%");
		return Charset.forName(match.getName());
	}
	
	
	/**
	 * Merge all the provided data into a single Tridas container containing
	 * a single TridasProject.  
	 * 
	 * @param containers
	 * @return
	 */
	public static TridasTridas mergeToSingleProject(ArrayList<TridasTridas> containers)
	{
		if(containers==null) {
			log.warn("TRiDaS container is null");
			return null;
		}
		if(containers.size()==0) {
			log.warn("TRiDaS container is null");
			return null;
		}
		if(!containers.get(0).isSetProjects()) {
			log.warn("TRiDaS container has no projects");
			return null;
		}

		//System.out.println(containers.size()+" containers provided to merge");
		
		TridasProject p = containers.get(0).getProjects().get(0);
		p.setObjects(null);
		p.setDerivedSeries(null);
		
		TridasTridas thiscont = new TridasTridas();
		for(TridasTridas c : containers)
		{
			//System.out.println(c.getProjects().size()+" project(s) in this container to merge");
			for (TridasProject pr : c.getProjects())
			{
				
				List<TridasObject> objects = pr.getObjects();
				System.out.println(objects.size()+" object(s) in this project");
				p.getObjects().addAll(objects);
				p.getDerivedSeries().addAll(pr.getDerivedSeries());
			}
		}
		
		
		
		thiscont.getProjects().add(p);
		//System.out.println(thiscont.getProjects().size()+" project(s) in this merged container");
		
		return thiscont;
	}
	
	

}
