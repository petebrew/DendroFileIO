package org.tridas.io.formats.sheffield;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;

public class SheffieldFile implements IDendroFile {

	public SheffieldFile() {
	}

	
	/**
	 * Sheffield format has a number of special chars that can't be included in 
	 * certain lines.  This function replaces these characters with some similar
	 * ones.
	 * 
	 * @param str
	 * @return
	 */
	public static String stripSpecialChars(String str)
	{
		str.replace(",", ";");
		str.replace("\"", "'");
		str.replace("(", "[");
		str.replace(")", "]");
		return str;
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
	 * @see org.tridas.io.IDendroFile#getWriter()
	 */
	@Override
	public IDendroCollectionWriter getWriter() {
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

}
