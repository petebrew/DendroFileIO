package org.tridas.io.formats.vformat;

import java.util.ArrayList;

import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatDataType;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatParameter;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatStatType;

public class VFormat extends AbstractDendroFormat{

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("vformat.about.description");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("vformat.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("vformat.about.shortName");
		
	}

	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"!oj", "!*"};
		
		return new DendroFileFilter(exts, getShortName());

	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		// File extensions for VFormat files can be any one of the combination
		// of datatype, stattype and paramtype codes.
		
		ArrayList<String> fileExtensions = new ArrayList<String>();
		for (VFormatDataType dataType : VFormatDataType.values())
		{
			for(VFormatStatType statType : VFormatStatType.values())
			{
				for(VFormatParameter paramType : VFormatParameter.values())
				{
					fileExtensions.add(dataType.toString()+statType.toString()+paramType.toString());
				}
			}
		}
		
		return fileExtensions.toArray(new String[0]);
	}
}
