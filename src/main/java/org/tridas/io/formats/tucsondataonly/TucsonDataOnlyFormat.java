package org.tridas.io.formats.tucsondataonly;

import org.tridas.io.I18n;
import org.tridas.io.formats.tucson.TucsonFormat;

public class TucsonDataOnlyFormat extends TucsonFormat{

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tucsondataonly.about.description");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tucsondataonly.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tucsondataonly.about.shortName");
		
	}

}
