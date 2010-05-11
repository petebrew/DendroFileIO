package org.tridas.io;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.warnings.ConversionWarningException;

/**
 * Interface for all Dendro File Formats.
 * 
 * @author peterbrewer
 *
 */
public abstract class DendroFile {
	
	private String extension;
	private ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
	private DendroFormatInfo formatInformation;
	
	/**
	 * Constructor for a DendroFile.  This takes a string containing the I18n tag
	 * name of the format this file is in.  
	 * 
	 * @param baseTag
	 */
	public DendroFile(String baseTag){
		formatInformation = new DendroFormatInfo(baseTag);
	}
	
	public abstract String[] saveToString();
		
	/**
	 * Add a series to this file.  For file formats that only support
	 * a single file this should return an error.
	 * 
	 * @param series
	 * @throws ConversionWarningException 
	 */
	public abstract void addSeries(ITridasSeries series) throws ConversionWarningException;
	
	/**
	 * Set the series for this file.
	 * 
	 * @param series
	 */
	public abstract void setSeries(ITridasSeries series) throws ConversionWarningException;
	
	/**
	 * Get the series list.
	 * @return
	 */
	protected ArrayList<ITridasSeries> getSeriesList(){
		return seriesList;
	}
	
	/**
	 * @see org.tridas.io.DendroFormatInfo#getShortName()
	 */
	public String getShortName()
	{
		return formatInformation.getShortName();
	}
	
	/**
	 * @see org.tridas.io.DendroFormatInfo#getFullName()
	 */
	public String getFullName()
	{
		return formatInformation.getFullName();
	}
	
	/**
	 * @see org.tridas.io.DendroFormatInfo#getDescription()
	 */
	public String getDescription()
	{
		return formatInformation.getDescription();
	}
	
	
	/**
	 * Remove all series from file
	 */
	public void clearSeries(){
		seriesList.clear();
	}
	
	/**
	 * Set the extension for this file
	 * 
	 * @param ext
	 */
	public void setExtension(String ext){
		// Strip leading dot if present
		if(ext.substring(0,1).equals("."))
			extension = ext.substring(1, ext.length());
		else
			extension = ext;
	}
	
	/**
	 * Get the extension for this file
	 * 
	 * @return
	 */
	public String getExtension(){
		return extension;
	}
	
}
