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
package org.tridas.io.formats.trims;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.trims.TridasToTrimsDefaults.TrimsField;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValue;

public class TrimsFile implements IDendroFile {
	
	private TridasToTrimsDefaults defaults;
	private ArrayList<Integer> data = new ArrayList<Integer>();
	
	public TrimsFile(IMetadataFieldSet argDefaults) {
		defaults = (TridasToTrimsDefaults) argDefaults;
	}
	
	/**
	 * Set the name of the author of this dataset
	 * 
	 * @param name
	 * @throws ConversionWarningException
	 */
	private void setAuthor(String name) throws ConversionWarningException {
		if (name == null) {
			return;
		}
		
		char ch; // One of the characters in str.
		char prevCh; // The character that comes before ch in the string.
		int i; // A position in str, from 0 to str.length()-1.
		prevCh = '.'; // Prime the loop with any non-letter character.
		String initials = "";
		for (i = 0; i < name.length(); i++) {
			ch = name.charAt(i);
			if (Character.isLetter(ch) && !Character.isLetter(prevCh)) {
				initials += Character.toLowerCase(ch);
			}
			prevCh = ch;
		}
		
		defaults.getStringDefaultValue(TrimsField.AUTHOR).setValue(initials);
	}
	
	/**
	 * Set the date this series was measured
	 * 
	 * @param date
	 * @throws ConversionWarningException
	 */
	private void setMeasuringDate(XMLGregorianCalendar date) throws ConversionWarningException {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String dateStr = dateFormat.format(date);
		
		defaults.getStringDefaultValue(TrimsField.MEASURING_DATE).setValue(dateStr);
	}
	
	/**
	 * Set the first year of this sequence
	 * 
	 * @param yr
	 * @throws ConversionWarningException
	 */
	private void setStartYear(SafeIntYear yr) throws ConversionWarningException {
		Integer year = null;
		try {
			year = Integer.valueOf(yr.toString());
		} catch (NumberFormatException e) {
			return;
		}
		
		defaults.getIntegerDefaultValue(TrimsField.START_YEAR).setValue(year);
		
	}
	
	public void setSeries(ITridasSeries series) throws ConversionWarningException {
		
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
		
		// Set start year
		try {
			SafeIntYear yr = new SafeIntYear(series.getInterpretation().getFirstYear());
			setStartYear(yr);
		} catch (NullPointerException e) {}
		
		// Set date
		try {
			XMLGregorianCalendar date;
			if (series instanceof TridasMeasurementSeries) {
				date = ((TridasMeasurementSeries) series).getMeasuringDate().getValue();
			}
			else {
				date = ((TridasDerivedSeries) series).getDerivationDate().getValue();
			}
			setMeasuringDate(date);
		} catch (Exception e) {}
		
		// Set Author
		try {
			String author;
			if (series instanceof TridasMeasurementSeries) {
				author = ((TridasMeasurementSeries) series).getAnalyst();
			}
			else {
				author = ((TridasDerivedSeries) series).getAuthor();
			}
			setAuthor(author);
		} catch (NullPointerException e) {}
		
	}
	
	@Override
	public String getExtension() {
		return "rw";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String[] saveToString() {
		
		StringBuilder string = new StringBuilder();
		
		string.append(StringUtils.getIntialsFromName(defaults.getStringDefaultValue(TrimsField.AUTHOR).getValue(), 2) + "\n");
		string.append(defaults.getDefaultValue(TrimsField.MEASURING_DATE).getValue() + "\n");
		string.append(defaults.getDefaultValue(TrimsField.START_YEAR).getValue() + "\n");
		
		for (Integer value : data) {
			string.append(" " + String.valueOf(value) + "\n");
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
