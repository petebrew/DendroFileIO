/*******************************************************************************
 * Copyright 2010 Peter Brewer and Daniel Murphy
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
package org.tridas.io.formats.vformat;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class VFormatFile implements IDendroFile {

	private ArrayList<VFormatContainer> dataPairList = new ArrayList<VFormatContainer>();
	private ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
	
	public VFormatFile() {
	
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		
		// VFormat uses different extensions depending on the content of the file.  However,
		// I have no idea what is supposed to happen if there are different types of data
		// in the same file.  This code therefore arbitrarily uses the first data block
		// to determine the extension
		
		String val = "!oj";
		
		if(dataPairList.isEmpty()) return val;
		
		if(dataPairList.get(0).defaults!=null)
		{
			val = "";
			val+= dataPairList.get(0).defaults.getDefaultValue(DefaultFields.DATA_TYPE).getStringValue();
			val+= dataPairList.get(0).defaults.getDefaultValue(DefaultFields.STAT_CODE).getStringValue();
			val+= dataPairList.get(0).defaults.getDefaultValue(DefaultFields.PARAMETER_CODE).getStringValue();
			return val;
		}
		else
		{
			return "!oj";
		}
	}
	
	
	public void addSeries(ITridasSeries ser, TridasValues tv, TridasToVFormatDefaults defaults){
		
		VFormatContainer dataPair = new VFormatContainer(tv, defaults);
		dataPairList.add(dataPair);
		seriesList.add(ser);
		
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		
		return seriesList.toArray(new ITridasSeries[0]);

	}
		
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		if(dataPairList.isEmpty())
		{
			return null;
		}
		else
		{
			// Bit of a fudge.  Just return the first defaults
			return dataPairList.get(0).defaults;
		}
	}
	

	@Override
	public String[] saveToString() {
		ArrayList<String> file = new ArrayList<String>();
		
		for (VFormatContainer dataPair : dataPairList)
		{
			// Header line 1
			String line = "";
			line+= dataPair.defaults.getDefaultValue(DefaultFields.PROJECT_CODE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.REGION_CODE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.OBJECT_CODE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.TREE_CODE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.HEIGHT_CODE).getStringValue();
			line+= "1"; // Hard coded running number
			line+= "."; // Hard coded separator (equivalent to the filename.ext separator)
			line+= dataPair.defaults.getDefaultValue(DefaultFields.DATA_TYPE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.STAT_CODE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.PARAMETER_CODE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.UNIT).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.COUNT).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.SPECIES).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.LAST_YEAR).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.DESCRIPTION).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.CREATED_DATE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.ANALYST).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.UPDATED_DATE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.FORMAT_VERSION).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.UNMEAS_PRE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.UNMEAS_PRE_ERR).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.UNMEAS_POST).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.UNMEAS_POST_ERR).getStringValue();
			file.add(line);
			
			// Header line 2
			file.add(dataPair.defaults.getDefaultValue(DefaultFields.FREE_TEXT_FIELD).getStringValue());
			
			// Header line 3
			line = "";
			line+= dataPair.defaults.getDefaultValue(DefaultFields.LONGITUDE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.LATITUDE).getStringValue();
			line+= dataPair.defaults.getDefaultValue(DefaultFields.ELEVATION).getStringValue();
			file.add(line);
			
			// Data lines
			int i = 0;
			line = "";
			for (TridasValue value : dataPair.dataValues.getValues())
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
		}
		
		// Return file as array
		return file.toArray(new String[0]);
	}
	
	protected static class VFormatContainer {
		public TridasValues dataValues;
		public TridasToVFormatDefaults defaults;
		
		protected VFormatContainer(TridasValues dv, TridasToVFormatDefaults def)
		{
			dataValues = dv;
			defaults = def;
		}
	}

}
