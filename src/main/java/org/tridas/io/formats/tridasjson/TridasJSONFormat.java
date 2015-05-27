package org.tridas.io.formats.tridasjson;

import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.I18n;
import org.tridas.io.transform.TridasVersionTransformer.TridasVersion;

public class TridasJSONFormat extends AbstractDendroFormat{
	
	public TridasJSONFormat()
	{
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getDescription()
	 */
	@Override
	public String getDescription() {
		
		return I18n.getText("tridasjson.about.description");
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		
		return I18n.getText("tridasjson.about.fullName");
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {

		return I18n.getText("tridasjson.about.shortName");
		
	}

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{"json"};
	}
	
}
