package org.tridas.io.formats.oxford;

import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.I18n;

public class OxfordFormat extends AbstractDendroFormat{

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("oxford.about.description");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("oxford.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("oxford.about.shortName");
		
	}

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{"ddf"};
	}
}
