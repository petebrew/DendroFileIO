package org.tridas.io.util;

import java.awt.FileDialog;
import java.awt.Frame;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.grlea.log.SimpleLogger;

/**
 * Static IO utility methods
 * @author daniel
 */
public class IOUtils {

	private final static SimpleLogger log = new SimpleLogger(IOUtils.class);

	
	private IOUtils(){}
	
	/*
	 * #########################################
	 * ######### FILE/FOLDER SELECTION #########
	 * #########################################
	 */


	public static File inputFile(Frame argParent) {
		return inputFile("Select a file...", argParent);
	}

	/**
	 * The parentFrame is the Frame that will guide the placement of the prompt
	 * window. If no Frame is available, just pass in null.
	 */

	public static File inputFile(String argPrompt, Frame argParentFrame) {
		if (argParentFrame == null) {
			argParentFrame = new Frame();
		}
		FileDialog fd = new FileDialog(argParentFrame, argPrompt, FileDialog.LOAD);
		fd.setVisible(true);

		String directory = fd.getDirectory();
		String filename = fd.getFile();
		if (filename == null) {
			return null;
		}
		return new File(directory, filename);
	}

	public static File outputFile(Frame parentFrame) {
		return outputFile("Save as...", parentFrame);
	}

	/**
	 * The parentFrame is the Frame that will guide the placement of the prompt
	 * window. If no Frame is available, just pass in null.
	 */
	public static File outputFile(String prompt, Frame parentFrame) {
		if (parentFrame == null) {
			parentFrame = new Frame();
		}
		FileDialog fd = new FileDialog(parentFrame, prompt, FileDialog.SAVE);
		fd.setVisible(true);

		String directory = fd.getDirectory();
		String filename = fd.getFile();
		if (filename == null) {
			return null;
		}
		return new File(directory, filename);
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
			InputStream is = new FileInputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			return createReader(is);

		} catch (Exception e) {
			if (file == null) {
				throw new RuntimeException(
						"File passed to createReader() was null. "
								+ "Why are you doing this to me?");
			} else {
				e.printStackTrace();
				throw new RuntimeException("Couldn't create a reader for "
						+ file.getAbsolutePath());
			}
		}
		// return null;
	}

	/**
	 * I want to read lines from a stream.
	 */
	public static BufferedReader createReader(InputStream input) {
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		} // not gonna happen
		return new BufferedReader(isr);
	}

	/**
	 * I want to print lines to a file. 
	 */
	public static PrintWriter createWriter(File file) {
		try {
			OutputStream output = new FileOutputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				output = new GZIPOutputStream(output);
			}
			return createWriter(output);

		} catch (Exception e) {
			if (file == null) {
				throw new RuntimeException(
						"File passed to createWriter() was null");
			} else {
				e.printStackTrace();
				throw new RuntimeException("Couldn't create a writer for "
						+ file.getAbsolutePath());
			}
		}
	}

	/**
	 * I want to print lines to a file.
	 */
	public static PrintWriter createWriter(OutputStream output) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(output, "UTF-8");
			return new PrintWriter(osw);
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/*
	 * #########################################
	 * ############## FILE INPUT ###############
	 * #########################################
	 */
	
	public static InputStream createInput(File file) {
		try {
			InputStream input = new FileInputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				return new GZIPInputStream(input);
			}
			return input;

		} catch (IOException e) {
			if (file == null) {
				throw new RuntimeException(
						"File passed to createInput() was null");

			} else {
				e.printStackTrace();
				throw new RuntimeException("Couldn't createInput() for "
						+ file.getAbsolutePath());
			}
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
			e.printStackTrace();
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

	public static String[] loadStrings(InputStream input) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input, "UTF-8"));

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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		return null;
	}

	public static void saveStream(File targetFile, InputStream sourceStream) {
		File tempFile = null;

		try {
			File parentDir = targetFile.getParentFile();
			tempFile = File.createTempFile(targetFile.getName(), null,
					parentDir);

			BufferedInputStream bis = new BufferedInputStream(sourceStream,
					16384);
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
				log.error("Could not rename temporary file "
						+ tempFile.getAbsolutePath());
			}
		} catch (IOException e) {
			if (tempFile != null) {
				tempFile.delete();
			}
			e.printStackTrace();
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
			log.error("error saving bytes to " + file);
			e.printStackTrace();
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
			String location = file.getAbsolutePath();
			createPath(location);
			OutputStream output = new FileOutputStream(location);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				output = new GZIPOutputStream(output);
			}
			saveStrings(output, strings);
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveStrings(OutputStream output, String strings[]) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(output, "UTF-8");
			PrintWriter writer = new PrintWriter(osw);
			for (int i = 0; i < strings.length; i++) {
				writer.println(strings[i]);
			}
			writer.flush();
		} catch (UnsupportedEncodingException e) {
		}
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
}
