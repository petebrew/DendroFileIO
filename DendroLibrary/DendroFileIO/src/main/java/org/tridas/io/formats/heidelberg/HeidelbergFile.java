package org.tridas.io.formats.heidelberg;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.DendroFile;
import org.tridas.io.warnings.ConversionWarningException;

public class HeidelbergFile extends DendroFile {

	TridasToHeidelbergDefaults defaults = new TridasToHeidelbergDefaults();
	
	
	@Override
	public void addSeries(ITridasSeries series)
			throws ConversionWarningException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] saveToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSeries(ITridasSeries series)
			throws ConversionWarningException {
		// TODO Auto-generated method stub

	}

}
