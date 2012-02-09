package org.tridas.io.formats.heidelberg;

import org.tridas.io.I18n;

public class HeidelbergUnstackedWriter extends HeidelbergWriter {

	
	public HeidelbergUnstackedWriter() {
		super();
		super.isstacked = false;
		
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("heidelberg.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("heidelberg.about.fullName")+" (unstacked)";
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("heidelberg.about.shortName")+" (unstacked)";
	}
	
}
