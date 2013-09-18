package org.tridas.io.formats.belfastapple;

import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.I18n;

public class BelfastAppleFormat extends AbstractDendroFormat{

	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("belfastapple.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("belfastapple.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("belfastapple.about.shortName");
		
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"txt"};
	}

}
