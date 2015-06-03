package org.tridas.io.formats.lipd;

import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.I18n;

public class LiPDFormat extends AbstractDendroFormat{
	
	public LiPDFormat()
	{
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getDescription()
	 */	
	@Override
	public String getDescription() {
		
		return I18n.getText("lipd.about.description");
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		
		return I18n.getText("lipd.about.fullName");
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {

		return I18n.getText("lipd.about.shortName");
		
	}

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{"zip"};
	}
	
}
