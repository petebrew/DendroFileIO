package org.tridas.io.formats.tucson;

import org.tridas.io.I18n;

public class TucsonUnstackedWriter extends TucsonWriter {

	public TucsonUnstackedWriter() {
		super();
		super.isstacked = false;
		
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tucson.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tucson.about.fullName")+" (unstacked)";
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tucson.about.shortName")+" (unstacked)";
	}
	
	
}
