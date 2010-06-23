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
package org.tridas.io.formats.tucsoncompact;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.tucsoncompact.TucsonCompactToTridasDefaults.DefaultFields;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class TucsonCompactFile implements IDendroFile {

	private TridasToTucsonCompactDefaults defaults;
	private TridasValues dataValues;
	private Integer cols = null;
	private Integer charsPerVal = 0;
	private Integer impliedDecimalPlaces = 0;
	private Integer expFactor = -2;
	
	public TucsonCompactFile(TridasToTucsonCompactDefaults argDefaults) {
		defaults = argDefaults;
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getExtension() {
		return "rwm";
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
		
		// Work out exp. factor
		if (vals.isSetUnit())
		{
			if(vals.getUnit().isSetNormalTridas())
			{
				switch(vals.getUnit().getNormalTridas())
				{
				case METRES:
					expFactor = +3;
				case CENTIMETRES:
					expFactor = +1;
				case MILLIMETRES:
					expFactor = 0;
					break;
				case TENTH_MM:
					expFactor = -1;
					break;
				case HUNDREDTH_MM:
					expFactor = -2;
					break;
				case MICROMETRES:
					expFactor = -3;
					break;
				}
			}
		}
		
		
		// Find the maximum number of characters required for values
		for(TridasValue val : dataValues.getValues())
		{
			Integer s = val.getValue().toString().length();
			if(val.toString().length()>charsPerVal) charsPerVal=val.getValue().toString().length();
		}
		
		// Find the number of cols that will fit in 80 characters
		cols = 80 / charsPerVal;
	}
	
	
	@Override
	public ITridasSeries[] getSeries() {
		//return new ITridasSeries[]{series};
		return null;
	}

	/**
	 * Get the Fortran 'F format' formatting string which will best 
	 * fit this dataset 
	 * 
	 * @return
	 */
	private String getFortranFormatString()
	{
		return StringUtils.leftPad(expFactor.toString()+"("+cols.toString()+"F"+charsPerVal.toString()+"."+impliedDecimalPlaces.toString()+")", 11);
	}
	
	@Override
	public String[] saveToString() {
			
		ArrayList<String> file = new ArrayList<String>();
	
		// Write header line
		file.add(defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getStringValue() + "=N"  +
				 defaults.getIntegerDefaultValue(DefaultFields.START_YEAR).getStringValue() + "=I " +
				 defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).getValue()      +
				 getFortranFormatString() + "~");
		
		
		// Loop through data
		String line = "";
		int i=0;
		for (TridasValue val : dataValues.getValues())
		{
			// Add value to our line
			line += StringUtils.leftPad(val.getValue(), charsPerVal);
			i++;
			
			// If this is the last value for the line, add line to file
			if(i==cols)
			{
				file.add(line);
				line = "";
				i=0;				
			}
		}
		
		// Add any remaining data
		if (line.length()>0)
		{
			file.add(line);
		}
		
		// Return array of lines
		return file.toArray(new String[0]);
		
	}

}
