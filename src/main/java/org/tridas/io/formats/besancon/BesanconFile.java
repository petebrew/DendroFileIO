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
package org.tridas.io.formats.besancon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.besancon.BesanconToTridasDefaults.DefaultFields;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataFormat;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class BesanconFile implements IDendroFile {

	private ArrayList<BesanconSeriesDefaultsPair> dataPairList = new ArrayList<BesanconSeriesDefaultsPair>();
	
	public BesanconFile() {
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		return "txt";
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		
		ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
		
		for (BesanconSeriesDefaultsPair datapair : dataPairList)
		{
			seriesList.add(datapair.series);
		}
		
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
	
	public void addSeries(ITridasSeries series, TridasToBesanconDefaults defaults){
		
		BesanconSeriesDefaultsPair dataPair = new BesanconSeriesDefaultsPair(series, defaults);
		dataPairList.add(dataPair);
		
	}
	

	@Override
	public String[] saveToString() {
		ArrayList<String> file = new ArrayList<String>();
		
		for (BesanconSeriesDefaultsPair dataPair : dataPairList)
		{
			// Take advantage of the free text feature first
			if (dataPair.series instanceof TridasMeasurementSeries)
			{
				// Just show title of measurement series
				file.add("## TridasMeasurementSeries: "+dataPair.defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).getValue());
			}
			else
			{
				// Try and show a list of series this derivedSeries is created from
				file.add("## TridasDerivedSeries: "+dataPair.defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).getValue());
				TridasDerivedSeries ds = (TridasDerivedSeries) dataPair.series;
				if(ds.isSetLinkSeries())
				{
					file.add("## "+I18n.getText("besancon.parentSeries")+":");
					List<SeriesLink> serieslist = ds.getLinkSeries().getSeries();
					for (SeriesLink link : serieslist)
					{
						if(link.isSetXLink())
						{
							file.add("##   - "+link.getXLink().getHref());
						}
						else if (link.isSetIdentifier())
						{
							file.add("##   - "+link.getIdentifier().getDomain()+":"+link.getIdentifier().getValue());
						}
						else if (link.isSetIdRef())
						{
							try{
							ITridasSeries linkedSeries = ((ITridasSeries)link.getIdRef().getRef());
							file.add("##   - "+linkedSeries.getIdentifier().getDomain()+":"+linkedSeries.getIdentifier().getValue());
							} catch (Exception e)
							{
								file.add("##   - "+I18n.getText("unnamed.series"));
							}
						}
					}
				}
			}
						
			// Series title 
			file.add(". "+dataPair.defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).getValue().replace(" ", ""));
			
			// Species
			file.add("   ESP "+String.valueOf(dataPair.defaults.getStringDefaultValue(DefaultFields.SPECIES).getValue()));
			
			// Length of series
			file.add("   LON "+String.valueOf(dataPair.defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()));
			
			// Position of series in the mean
			file.add("   POS "+String.valueOf(dataPair.defaults.getIntegerDefaultValue(DefaultFields.POSITION_IN_MEAN).getValue()));
			
			// First year
			if(dataPair.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue()!=null)
			{
				file.add("   ORI "+dataPair.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue().toString());
			}
			
			// Last year
			if(dataPair.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue()!=null)
			{
				file.add("   TER "+dataPair.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue().toString());
			}	
			
			// Pith
			if(dataPair.defaults.getBooleanDefaultValue(DefaultFields.PITH).getValue())
			{
				file.add("   MOE ");
			}
			
			// Sapwood - this is a little tricky as Besancon format requires the index of the first sapwood ring 
			if(dataPair.defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()!=null &&
			   dataPair.defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).getValue()!=null &&
			   dataPair.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue()!=null)
			{
				// First calculate the year of the first sapwood ring
				SafeIntYear firstSapYear = dataPair.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue()
						.add(dataPair.defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue())
						.add(0-dataPair.defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).getValue());
				
				// Calculate index
				int index = firstSapYear.diff(dataPair.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue());
				
				// Only add to file if index is not 0 or the same as the number of rings
				if(index>0 && index<dataPair.defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue())
				{
					file.add("   AUB "+String.valueOf(index));
				}
			}
			
			
			// Start data block
			file.add("VALeur");
			
			// Loop through values
			String line = "";
			int i = 0;
			for (TridasValue value : dataPair.series.getValues().get(0).getValues())
			{
					i++;
					// First line has leader spaces
					if (i==1)
					{
						line +="   ";
					}
					
					// Add value to line
					if(value.getValue().equals("0"))
					{
						line += StringUtils.leftPad(",", 6);
					}
					else
					{
						line += StringUtils.leftPad(value.getValue(), 6);
					}
					
					// Last value in decade block
					if (i==10)
					{
						file.add(line);
						line = "";
						i=0;
					}
				
			}
			
			// Add end marker (and any remaining values in line buffer)
			if(line.equals(""))
			{
				file.add(StringUtils.leftPad(";", 9));
			}
			else
			{
				line += StringUtils.leftPad(";", 6);
				file.add(line);
			}
		}
		
		return file.toArray(new String[0]);
	}
	
	
	protected static class BesanconSeriesDefaultsPair {
		public ITridasSeries series;
		public TridasToBesanconDefaults defaults;
		
		protected BesanconSeriesDefaultsPair(ITridasSeries ser, TridasToBesanconDefaults def)
		{
			series = ser;
			defaults = def;
		}
	}

}
