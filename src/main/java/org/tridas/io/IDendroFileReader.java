package org.tridas.io;

import java.io.IOException;

import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

/**
 * Interface for classes that implement the reader of a dendro data files and 
 * conversion into a TridasProject
 * 
 * @author peterbrewer
 *
 */
public interface IDendroFileReader {

	
	
	/**
	 * Loads a file from a given filename.  Filename can be absolute, local, or
	 * a url.
	 * @param argFilename
	 * @throws InvalidDendroFileException 
	 */
	public void loadFile(String argFilename, IMetadataFieldSet argDefaultFields ) throws IOException, IncorrectDefaultFieldsException, InvalidDendroFileException;
	
	/**
	 * Loads a file from a given filename at the given path.
	 * @param argPath
	 * @param argFilename
	 * @param argDefaultFields
	 * @throws InvalidDendroFileException 
	 */
	public void loadFile(String argPath, String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,IncorrectDefaultFieldsException, InvalidDendroFileException;

	/**
	 * Loads the file from the given strings.
	 * @param argFileStrings
	 * @param argDefaultFields
	 * @throws IncorrectDefaultFieldsException
	 * @throws InvalidDendroFileException 
	 */
	public void loadFile(String argFileStrings[], IMetadataFieldSet argDefaultFields) throws IncorrectDefaultFieldsException, InvalidDendroFileException;
	
	/**
	 * Returns a list of the file extensions for this file
	 * @return
	 */
	public String[] getFileExtensions();
	
	/**
	 * Gets the parsed project after it's loaded.
	 */
	public TridasProject getProject();
}
