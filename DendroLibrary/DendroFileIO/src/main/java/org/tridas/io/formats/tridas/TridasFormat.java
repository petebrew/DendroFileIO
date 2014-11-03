package org.tridas.io.formats.tridas;

import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.I18n;
import org.tridas.io.transform.TridasVersionTransformer.TridasVersion;

public class TridasFormat extends AbstractDendroFormat{

	private TridasVersion version;
	
	public TridasFormat(TridasVersion version)
	{
		this.version = version;
	}
	
	public TridasFormat()
	{
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tridas.about.description");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		if(version!=null)
		{
			return I18n.getText("tridas.about.fullName")+" v."+version.getVersionString();
		}
		else
		{
			return I18n.getText("tridas.about.fullName");
		}
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {
		if(version!=null)
		{
			return I18n.getText("tridas.about.shortName")+" v."+version.getVersionString();
		}
		else
		{
			return I18n.getText("tridas.about.shortName");
		}
	}

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{"xml"};
	}
}
