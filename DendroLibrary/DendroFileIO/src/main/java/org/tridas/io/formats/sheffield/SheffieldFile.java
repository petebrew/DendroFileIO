package org.tridas.io.formats.sheffield;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;

public class SheffieldFile implements IDendroFile {

	public SheffieldFile() {
	}
	
	/**
	 * Does this line contain any special chars?
	 * 
	 * @param str
	 * @return
	 */
	public static Boolean containsSpecialChars(String str)
	{
		if(str.contains(",")) return true;
		if(str.contains("\"")) return true;
		if(str.contains("(")) return true;
		if(str.contains(")")) return true;
		return false;
	}


	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @see org.tridas.io.IDendroFile#saveToString()
	 */
	@Override
	public String[] saveToString() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		// TODO Auto-generated method stub
		return null;
	}

}
