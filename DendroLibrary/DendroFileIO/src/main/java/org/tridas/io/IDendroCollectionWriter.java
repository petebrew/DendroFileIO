package org.tridas.io;

import java.util.List;

import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.schema.TridasProject;

/**
 * Interface for classes that convert TridasProjects to one or more
 * dendro files.  A TridasProject is wider in scope than most dendro
 * file formats so often a single TridasProject will result in the
 * output of multiple dendro files.  
 * 
 * @author peterbrewer
 *
 */
public interface IDendroCollectionWriter {
	
	
	public void loadProject(TridasProject argProject) throws IncompleteTridasDataException, ConversionWarningException, IncorrectDefaultFieldsException;
	
	/**
	 * Parse a TridasProject into a legacy format
	 * 
	 * @param argProject
	 * @throws IncompleteTridasDataException
	 * @throws ConversionWarningException
	 * @throws IncorrectDefaultFieldsException 
	 */
	public void loadProject(TridasProject argProject, IMetadataFieldSet argDefaults) throws IncompleteTridasDataException, ConversionWarningException, IncorrectDefaultFieldsException;

	/**
	 * Get all the dendro files that were parsed
	 * @return
	 */
	public DendroFile[] getFiles();
	
	/**
	 * Set the naming convention
	 * @param argConvension
	 */
	public void setNamingConvention(INamingConvention argConvension);
	
	/**
	 * Get the naming convention
	 * @return
	 */
	public INamingConvention getNamingConvention();
	
	/**
	 * 
	 * @return
	 */
	public int getFileCount();
	
	/**
	 * Get all the file extensions associated with this writer
	 * @return
	 */
	public String[] getFileExtensions();
	
	/**
	 * Save all associated files to disk
	 */
	public void saveAllToDisk(String argFolderName);
	
	public List<ConversionWarning> getWarnings();
}
