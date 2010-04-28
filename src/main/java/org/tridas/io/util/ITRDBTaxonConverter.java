package org.tridas.io.util;

/**
 * Class for converting between ITRDB 4 char taxon code
 * and proper latin name.
 * 
 * @author sbr00pwb
 */
public class ITRDBTaxonConverter {

	public ITRDBTaxonConverter()
	{
		
	}
	
	public static String getCodeFromName(String taxonname)
	{
		return "ABCD";
	}
	
	public static String getNameFromCode(String code)
	{
		return "Abies alba L.";
	}
	
}
