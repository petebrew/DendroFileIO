package org.tridas.io.formats.tucson;

import org.tridas.io.I18n;

public class TucsonUnstackedFormat extends TucsonFormat{

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tucson.about.description");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tucson.about.fullName")+" (unstacked)";
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tucson.about.shortName")+" (unstacked)";
		
	}
}
