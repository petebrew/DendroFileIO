package org.tridas.io.formats.sheffield;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.heidelberg.TridasToHeidelbergDefaults;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.DefaultFields;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldPeriodCode;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class SheffieldFile implements IDendroFile {
	
	private TridasToSheffieldDefaults defaults;
	private ITridasSeries series;
	private TridasValues dataValues;
	
	public SheffieldFile(TridasToSheffieldDefaults argDefaults) {
		defaults = argDefaults;
	}
	
	
	/**
	 * Set the series that this Sheffield file represents
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
	 * Does this line contain any special chars?
	 * 
	 * @param str
	 * @return
	 */
	public static Boolean containsSpecialChars(String str) {
		if (str.contains(",")) {
			return true;
		}
		if (str.contains("\"")) {
			return true;
		}
		if (str.contains("(")) {
			return true;
		}
		if (str.contains(")")) {
			return true;
		}
		return false;
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		return "d";
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
	
	/**
	 * @see org.tridas.io.IDendroFile#saveToString()
	 */
	@Override
	public String[] saveToString() {
		ArrayList<String> file = new ArrayList<String>();
		
		file.add(defaults.getSheffieldStringDefaultValue(DefaultFields.SITE_NAME).getValue());
		file.add(String.valueOf(defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()));
		file.add(defaults.getDefaultValue(DefaultFields.DATE_TYPE).getValue().toString());
		file.add(String.valueOf(defaults.getIntegerDefaultValue(DefaultFields.START_DATE).getValue()));
		file.add(defaults.getDefaultValue(DefaultFields.DATA_TYPE).getValue().toString());
		
		if(series instanceof TridasDerivedSeries)
		{
			file.add(String.valueOf(defaults.getIntegerDefaultValue(DefaultFields.TIMBER_COUNT).getValue()));
			file.add(defaults.getDefaultValue(DefaultFields.CHRONOLOGY_TYPE).getValue().toString());

		}
		else
		{
			file.add(String.valueOf(defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).getValue()));
			file.add(defaults.getDefaultValue(DefaultFields.EDGE_CODE).getValue().toString());
		}
		
		file.add(defaults.getSheffieldStringDefaultValue(DefaultFields.COMMENT).getValue());
		file.add(defaults.getStringDefaultValue(DefaultFields.UK_COORDS).getValue());
		file.add(defaults.getStringDefaultValue(DefaultFields.LAT_LONG).getValue());
		file.add(defaults.getDefaultValue(DefaultFields.PITH_CODE).getValue().toString());
		file.add(defaults.getDefaultValue(DefaultFields.SHAPE_CODE).getValue().toString());
		file.add(String.valueOf(defaults.getIntegerDefaultValue(DefaultFields.MAJOR_DIM).getValue()));
		file.add(String.valueOf(defaults.getIntegerDefaultValue(DefaultFields.MINOR_DIM).getValue()));
		file.add(defaults.getStringDefaultValue(DefaultFields.INNER_RING_CODE).getValue());
		file.add(defaults.getStringDefaultValue(DefaultFields.OUTER_RING_CODE).getValue());
		file.add(defaults.getSheffieldStringDefaultValue(DefaultFields.PHASE).getValue());
		file.add(defaults.getSheffieldStringDefaultValue(DefaultFields.SHORT_TITLE).getValue());
		file.add(((SheffieldPeriodCode)defaults.getDefaultValue(DefaultFields.PERIOD).getValue()).toCode());
		file.add(defaults.getDefaultValue(DefaultFields.TAXON).getValue().toString());
		file.add(defaults.getSheffieldStringDefaultValue(DefaultFields.INTERPRETATION_COMMENT).getValue());
		file.add(defaults.getDefaultValue(DefaultFields.DATA_TYPE).getValue().toString());

	
		for (TridasValue value : dataValues.getValues())
		{
			file.add(value.getValue());
		}
		file.add("H");
		
		if(series instanceof TridasDerivedSeries)
		{
			for (TridasValue value : dataValues.getValues())
			{
				if(value.isSetCount())
				{
					file.add(value.getCount().toString());
				}
				else
				{
					break;
				}
			}
			file.add("R");
		}

		return file.toArray(new String[0]);
		
	}
}
