/**
 * Copyright 2013 Peter Brewer
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
package org.tridas.io.formats.heikkenenchrono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class HeikkenenChronoFile implements IDendroFile {

	private TridasToHeikkenenChronoDefaults defaults;
	private TridasValues dataValues;
	
	public HeikkenenChronoFile(TridasToHeikkenenChronoDefaults argDefaults) {
		defaults = argDefaults;
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getExtension() {
		return "rng";
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
		
		if(dataValues!=null && dataValues.isSetValues())
		{		
			List<TridasValue> vals = dataValues.getValues();
			
			Collections.reverse(vals);
			
			for (TridasValue val : vals)
			{				
				file.add(StringUtils.leftPad(val.getValue().toString(), 4));
			}
		}
				
		return file.toArray(new String[0]);
		
	}

}
