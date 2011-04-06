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
package org.tridas.io.formats.csv;

import java.util.ArrayList;
import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.TridasValue;

/**
 * Basic Comma Separated Value file format. Files are simple two column spreadsheets
 * with year in column 1 and value in column 2.
 * 
 * @todo add ring remarks column
 * @author peterbrewer
 * @deprecated use CSVMatrix instead
 */
public class CSVFile implements IDendroFile {
	
	private TridasToCSVDefaults defaults;
	private ArrayList<Integer> data = new ArrayList<Integer>();
	private SafeIntYear startYear = new SafeIntYear();
	private Boolean isAstronomical = false;
	private String datingTypeHeader = null;
	
	public CSVFile(IMetadataFieldSet argDefaults) {
		defaults = (TridasToCSVDefaults) argDefaults;
	}
	
	public void setSeries(ITridasSeries series) throws ConversionWarningException {
		
		// Set start year
		if(series.isSetInterpretation())
		{
			if(series.getInterpretation().isSetDating())
			{
				switch(series.getInterpretation().getDating().getType())
				{
				case RELATIVE:
					datingTypeHeader = "Relative Year";
					isAstronomical = true;
					if(series.getInterpretation().isSetFirstYear())
					{
						startYear = new SafeIntYear(series.getInterpretation().getFirstYear().getValue().toString(), true);
					}
					else
					{
						startYear = new SafeIntYear("1", true);
					}
					break;
				case ABSOLUTE:
				case DATED_WITH_UNCERTAINTY:
				case RADIOCARBON:
					datingTypeHeader = "Year";
					if(series.getInterpretation().isSetFirstYear())
					{
						startYear = new SafeIntYear(series.getInterpretation().getFirstYear());
					}
					break;
				default:
					datingTypeHeader = "Year";
				}
				
			}
			else
			{
				datingTypeHeader = "Year";
				if(series.getInterpretation().isSetFirstYear())
				{
					startYear = new SafeIntYear(series.getInterpretation().getFirstYear().getValue().toString(), true);
				}
				else
				{
					startYear = new SafeIntYear("1", true);
				}
			}
		}

		
		// Extract ring widths from series
		List<TridasValue> valueList;
		try {
			valueList = series.getValues().get(0).getValues();
		} catch (NullPointerException e) {
			throw new ConversionWarningException(new ConversionWarning(WarningType.NULL_VALUE, I18n
					.getText("fileio.noData")));
		}
		try {
			for (TridasValue v : valueList) {
				Integer val = Integer.valueOf(v.getValue());
				data.add(val);
			}
		} catch (NumberFormatException e) {
			throw new ConversionWarningException(new ConversionWarning(WarningType.INVALID, I18n
					.getText("fileio.invalidDataValue")));
		}
		
	}
	
	@Override
	public String getExtension() {
		return "csv";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String[] saveToString() {
		
		StringBuilder string = new StringBuilder();
		
		SafeIntYear thisYear = startYear;

		string.append(datingTypeHeader+",Value\n");
		
		for (Integer value : data) {
			String yearStr = "";
			if(isAstronomical)
			{
				yearStr = String.valueOf((thisYear.toAstronomicalInteger()));
			}
			else
			{
				yearStr = thisYear.toString();
			}
			
			string.append(yearStr + ",");
			string.append(String.valueOf(value) + "\n");
			thisYear = thisYear.add(1);
		}
		
		return string.toString().split("\n");
		
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
}
