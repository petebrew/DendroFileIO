package org.tridas.io;

public abstract class AbstractDendroFormat {

	
	/**
	 * Returns a list of the file extensions for this file
	 * 
	 * @return
	 */
	public abstract String[] getFileExtensions();
	
	/**
	 * Get the short name of the format
	 * 
	 * @return
	 */
	public abstract String getShortName();
	
	/**
	 * Get the full name of the format
	 * 
	 * @return
	 */
	public abstract String getFullName();
	
	/**
	 * Get the description of the format
	 * 
	 * @return
	 */
	public abstract String getDescription();
	
	
	public DendroFileFilter getDendroFileFilter() {

		return new DendroFileFilter(getFileExtensions(), getShortName());

	}
}
