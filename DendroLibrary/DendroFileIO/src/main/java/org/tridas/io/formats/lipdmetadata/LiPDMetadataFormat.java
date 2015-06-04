package org.tridas.io.formats.lipdmetadata;

import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.I18n;

public class LiPDMetadataFormat extends AbstractDendroFormat{
	
	public LiPDMetadataFormat()
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
		
		return I18n.getText("lipd.about.fullName")+ " (metadata only)";
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {

		return I18n.getText("lipd.about.shortName")+ " (metadata only)";
		
	}

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{"jsonld"};
	}
	
}
