package org.tridas.io.formats.past4;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;

public class Past4File implements IDendroFile {

	private ITridasSeries series = null;
	private TridasToPast4Defaults defaults;
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getExtension() {
		return "P4P";
	}

	@Override
	public ITridasSeries[] getSeries() {
		return new ITridasSeries[]{series};
	}

	@Override
	public String[] saveToString() {
		// TODO Auto-generated method stub
		return null;
	}

}
