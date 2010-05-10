package org.tridas.io.formats.sheffield;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.DendroFile;
import org.tridas.io.warnings.ConversionWarningException;

public class SheffieldFile extends DendroFile {

	public SheffieldFile() {
		super("sheffield");
	}

	@Override
	public void addSeries(ITridasSeries series)
			throws ConversionWarningException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] saveToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSeries(ITridasSeries series)
			throws ConversionWarningException {
		// TODO Auto-generated method stub

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

}
