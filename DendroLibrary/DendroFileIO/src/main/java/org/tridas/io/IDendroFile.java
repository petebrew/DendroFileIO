package org.tridas.io;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.defaults.IMetadataFieldSet;

/**
 * Interface for all Dendro File Formats.
 * 
 * @author daniel
 */
public interface IDendroFile {
	
	/**
	 * Saves the file to strings.
	 * 
	 * @return
	 */
	public String[] saveToString();
	
	/**
	 * Get the series list.
	 * 
	 * @return
	 */
	public ITridasSeries[] getSeries();
	
	/**
	 * Gets the extension to use for saving this file.
	 * 
	 * @return
	 */
	public String getExtension();
	
	/**
	 * Gets the defaults that this file uses to save the file
	 * 
	 * @return
	 */
	public IMetadataFieldSet getDefaults();
}
