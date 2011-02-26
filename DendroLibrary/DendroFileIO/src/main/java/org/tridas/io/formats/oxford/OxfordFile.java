/*******************************************************************************
 * Copyright 2011 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.tridas.io.formats.oxford;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.oxford.OxfordToTridasDefaults.OxDefaultFields;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class OxfordFile implements IDendroFile {

	private TridasToOxfordDefaults defaults;
	private TridasValues valuesGroup;
	
	public OxfordFile(TridasToOxfordDefaults argDefaults, TridasValues argValues)
	{
		defaults = argDefaults;
		valuesGroup = argValues;
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getExtension() {
		return "ddf";
	}

	@Override
	public ITridasSeries[] getSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] saveToString() {
		
		ArrayList<String> file = new ArrayList<String>();
		
		String line = "'" + defaults.getStringDefaultValue(OxDefaultFields.SERIESCODE).getStringValue();
		line += "  <" + defaults.getIntegerDefaultValue(OxDefaultFields.FIRSTYEAR).getStringValue();
		line += "-" + defaults.getIntegerDefaultValue(OxDefaultFields.LASTYEAR).getStringValue() + "> ";
		line += defaults.getStringDefaultValue(OxDefaultFields.DESCRIPTION).getStringValue() + "'";
		
		file.add(line);
		
		line = defaults.getIntegerDefaultValue(OxDefaultFields.SERIESLENGTH).getStringValue() + ",";
		line += defaults.getIntegerDefaultValue(OxDefaultFields.STARTYEAR).getStringValue();
		
		file.add(line);
		
		// Data
		Integer col = 0;
		line = "";
		Boolean hasCounts = false;
		for(TridasValue val : valuesGroup.getValues())
		{
			col++;
			
			if(col > 10)
			{
				file.add(line.substring(0, line.length()-1));
				line = "";
				col = 1;
			}
			
			line+= StringUtils.leftPad(val.getValue(),3) + " ";
			
			if(val.isSetCount()) hasCounts = true;
		}
		
		if(line.length()>0)
		{
			file.add(line.substring(0, line.length()-1));
		}
		
		file.add("");
		
		// Add counts section if applicable
		if(hasCounts)
		{
			col = 0;
			line = "";
			for(TridasValue val : valuesGroup.getValues())
			{
				col++;
				
				if(col > 10)
				{
					file.add(line.substring(0, line.length()-1));
					line = "";
					col = 1;
				}
				
				if(val.isSetCount())
				{
					line+= StringUtils.leftPad(val.getCount().toString(), 3) + " ";
				}
				else
				{
					line+= StringUtils.leftPad("0".toString(), 3) + " ";
					defaults.addConversionWarning(new ConversionWarning(
							WarningType.INVALID, I18n.getText("oxford.countsEmpty")));
				}
			}
			if(line.length()>0)
			{
				file.add(line.substring(0, line.length()-1));
			}
		}
		
		// Comments
		if(defaults.getStringDefaultValue(OxDefaultFields.COMMENTS).getValue()!=null)
		{
			file.add(defaults.getStringDefaultValue(OxDefaultFields.COMMENTS).getStringValue());
		}
		
		// Return file
		return file.toArray(new String[0]);
	}

}
