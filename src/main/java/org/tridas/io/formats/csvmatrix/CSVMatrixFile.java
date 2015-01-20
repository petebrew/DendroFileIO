package org.tridas.io.formats.csvmatrix;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.nottingham.TridasToNottinghamDefaults;
import org.tridas.schema.TridasValues;

public class CSVMatrixFile implements IDendroFile {

	private TridasToNottinghamDefaults defaults;
	
	
	
	@Override
	public String[] saveToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITridasSeries[] getSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExtension() {
		return "txt";
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void addSeries(TridasToMatrixDefaults def, ITridasSeries series, TridasValues values)
	{
		
	}

}
