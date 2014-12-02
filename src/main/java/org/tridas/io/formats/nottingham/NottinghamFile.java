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
package org.tridas.io.formats.nottingham;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.nottingham.NottinghamToTridasDefaults.DefaultFields;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class NottinghamFile implements IDendroFile {

	private TridasToNottinghamDefaults defaults;
	private TridasValues dataValues;
	
	public NottinghamFile(TridasToNottinghamDefaults argDefaults) {
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
			
		StringBuilder sb = new StringBuilder();
		
	
	
		sb.append(defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).getValue() + " " +
				 defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()+System.lineSeparator());
		
		
		int i=1;
		for (TridasValue val : dataValues.getValues())
		{
			// Add value to our line
			sb.append(StringUtils.leftPad(val.getValue(), 4));
			i++;
			
			// If this is the 20th value add line to file
			if(i==20) sb.append(System.lineSeparator());
		}
				
		// Return array of lines
		return sb.toString().split(System.lineSeparator());
		
	}

}
