package org.tridas.io.formats.topham;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.heidelberg.TridasToHeidelbergDefaults;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class TophamFile implements IDendroFile {

	private TridasToTophamDefaults defaults;
	private TridasValues dataValues;
	
	public TophamFile(TridasToTophamDefaults argDefaults) {
		defaults = argDefaults;
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getExtension() {
		return "txt";
	}
	
	public void setSeries(ITridasSeries argSeries) {
		
		
	}
	
	/**
	 * 
	 * @param vals
	 */
	public void setDataValues(TridasValues vals)
	{	
		dataValues = vals;
	}
	
	
	@Override
	public ITridasSeries[] getSeries() {
		//return new ITridasSeries[]{series};
		return null;
	}

	@Override
	public String[] saveToString() {
		
		ArrayList<String> file = new ArrayList<String>();
		
		for (TridasValue val : dataValues.getValues())
		{
			file.add(val.getValue().toString());
		}
				
		return file.toArray(new String[0]);
		
	}

}
