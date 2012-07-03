package org.tridas.io.formats.corina;

import org.tridas.io.I18n;

public class CorinaRecursiveReader extends CorinaReader {

	public CorinaRecursiveReader()
	{
		super();
		loadRecursively = true;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("corina.about.fullName")+" (include linked files)";
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("corina.about.shortName") +" (include linked files)";
	}
	
}
