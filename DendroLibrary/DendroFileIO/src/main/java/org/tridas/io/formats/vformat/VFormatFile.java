package org.tridas.io.formats.vformat;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class VFormatFile implements IDendroFile {

	private TridasToVFormatDefaults defaults;
	private ITridasSeries series;
	private TridasValues dataValues;
	
	public VFormatFile(TridasToVFormatDefaults argDefaults) {
		defaults = argDefaults;
	}
	
	
	/**
	 * Set the series that this VFormat file represents
	 * @param ser
	 */
	public void setSeries(ITridasSeries ser)
	{
		series = ser;
	}

	/**
	 * 
	 * @param vals
	 */
	public void setDataValues(TridasValues vals)
	{	
		dataValues = vals;
	}
	
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		String val = "";
		if(defaults!=null)
		{
			val+= defaults.getDefaultValue(DefaultFields.DATA_TYPE).getStringValue();
			val+= defaults.getDefaultValue(DefaultFields.STAT_CODE).getStringValue();
			val+= defaults.getDefaultValue(DefaultFields.PARAMETER_CODE).getStringValue();
			return val;
		}
		else
		{
			return "!oj";
		}
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		return new ITridasSeries[]{series};
	}
		
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String[] saveToString() {
		ArrayList<String> file = new ArrayList<String>();
		
		// Header line 1
		String line = "";
		line+= defaults.getDefaultValue(DefaultFields.PROJECT_CODE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.REGION_CODE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.OBJECT_CODE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.TREE_CODE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.HEIGHT_CODE).getStringValue();
		line+= "1"; // Hard coded running number
		line+= "."; // Hard coded separator (equivalent to the filename.ext separator)
		line+= defaults.getDefaultValue(DefaultFields.DATA_TYPE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.STAT_CODE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.PARAMETER_CODE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.UNIT).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.COUNT).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.SPECIES).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.LAST_YEAR).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.DESCRIPTION).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.CREATED_DATE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.ANALYST).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.UPDATED_DATE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.FORMAT_VERSION).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.UNMEAS_PRE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.UNMEAS_PRE_ERR).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.UNMEAS_POST).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.UNMEAS_POST_ERR).getStringValue();
		file.add(line);
		
		// Header line 2
		file.add(defaults.getDefaultValue(DefaultFields.FREE_TEXT_FIELD).getStringValue());
		
		// Header line 3
		line = "";
		line+= defaults.getDefaultValue(DefaultFields.LONGITUDE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.LATITUDE).getStringValue();
		line+= defaults.getDefaultValue(DefaultFields.ELEVATION).getStringValue();
		file.add(line);
		
		// Data lines
		int i = 0;
		line = "";
		for (TridasValue value : dataValues.getValues())
		{
			i++;
			
			// First 3 characters aren't used so leave blank, next five contain ring value
			line+= StringUtils.leftPad("", 3)+StringUtils.leftPad(value.getValue(), 5);
			
			// On the tenth value, add line to file
			if(i==10)
			{
				file.add(line);
				i=0;
				line="";
			}	
		}
		
		// Add any remaining values in the buffer and pad to the full 80 chars
		if(line!="")
		{
			file.add(StringUtils.rightPad(line, 80));
		}
		
		// Return file as array
		return file.toArray(new String[0]);
	}

}
