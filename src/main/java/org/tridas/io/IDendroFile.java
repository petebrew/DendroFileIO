package org.tridas.io;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.warnings.ConversionWarningException;

/**
 * Interface for all Dendro File Formats.
 * 
 * @author daniel
 *
 */
public interface IDendroFile {
	
	/**
	 * Get the writer of this file.
	 * @return
	 */
	public IDendroCollectionWriter getWriter();
	
	/**
	 * Saves the file to strings.
	 * @return
	 */
	public String[] saveToString();
		
	/**
	 * Get the series list.
	 * @return
	 */
	public ITridasSeries[] getSeries();
	
	/**
	 * Gets the extension to use for saving this file.
	 * @return
	 */
	public String getExtension();
}
