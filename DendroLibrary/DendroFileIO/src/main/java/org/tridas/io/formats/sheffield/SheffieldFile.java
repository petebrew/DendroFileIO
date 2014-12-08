/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io.formats.sheffield;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.DefaultFields;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldPeriodCode;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class SheffieldFile implements IDendroFile {
	
	private final TridasToSheffieldDefaults defaults;
	private final ITridasSeries series;
	private final TridasValues dataValues;
	
	public SheffieldFile(TridasToSheffieldDefaults argDefaults, ITridasSeries ser, TridasValues vals) throws ImpossibleConversionException {
		defaults = argDefaults;
		series = ser;
		dataValues = vals;
	
		if(vals==null || !vals.isSetValues())
		{
			throw new ImpossibleConversionException("Series contains no data values");
			
		}
		
		if(ser==null)
		{
			throw new ImpossibleConversionException("Series is empty");
			
		}
		
		
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
		
		file.add(defaults.getSheffieldStringDefaultValue(DefaultFields.SITE_NAME_SAMPLE_NUMBER).getValue());
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
		file.add(defaults.getDefaultValue(DefaultFields.VARIABLE_TYPE).getValue().toString());

	
		for (TridasValue value : dataValues.getValues())
		{
			file.add(value.getValue());
		}
		
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
