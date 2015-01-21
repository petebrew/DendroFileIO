package org.tridas.io.formats.csvmatrix;

import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.I18n;

public class CSVMatrixFormat extends AbstractDendroFormat{

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("csv.about.description");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("csv.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFormat#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("csv.about.shortName");
		
	}

	/**
	 * @see org.tridas.io.AbstractDendroFormat#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{"csv"};
	}
}
