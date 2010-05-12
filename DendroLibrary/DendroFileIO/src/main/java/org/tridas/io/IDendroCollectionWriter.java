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
	
	
	public void loadProject(TridasProject argProject) throws IncompleteTridasDataException, ConversionWarningException;
	
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
	public IDendroFile[] getFiles();
	
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
	 * Save all associated files to disk
	 */
	public void saveAllToDisk(String argFolderName);
	
	/**
	 * Get the short name of the format
	 * @return
	 */
	public String getShortName();
	
	/**
	 * Get the full name of the format
	 * @return
	 */
	public String getFullName();
	
	/**
	 * Get the description of the format
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Get the default values for this writer that were given in
	 * {@link #loadProject(TridasProject, IMetadataFieldSet)}.  If a project was never
	 * loaded then this returns null.
	 * @return
	 */
	public IMetadataFieldSet getDefaults();
	
	/**
	 * Get the conversion warnings generated from writing this file.
	 * @return
	 */
	public List<ConversionWarning> getWarnings();
}
