package org.tridas.io.formats.heidelberg;

import org.tridas.io.I18n;

public class HeidelbergUnstackedFormat extends HeidelbergFormat{

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("heidelberg.about.fullName")+" (unstacked)";
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("heidelberg.about.shortName")+" (unstacked)";
		
	}

}
