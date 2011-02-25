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
package org.tridas.io.formats.csvmatrix;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.YearRange;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasValue;

public class CSVMatrixFile implements IDendroFile {
	
	private ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
	private YearRange yrRange;
	private DatingSuffix calendar = DatingSuffix.AD;
	private IMetadataFieldSet defaults;
	
	public CSVMatrixFile(IMetadataFieldSet argDefaults) {
		defaults = argDefaults;
	}
	
	public void setSeriesList(ArrayList<ITridasSeries> lst) {
		
		
		// Switch the BP dating if any series are in BP
		for (ITridasSeries ser : lst) {
			
			if (calendar == DatingSuffix.BP) {
				break;
			}
			try {
				if (ser.getInterpretation().getFirstYear().getSuffix() == DatingSuffix.BP) {
					calendar = DatingSuffix.BP;
				}
			} catch (NullPointerException e) {}
		}
		

		
		
		// Calculate the range for these series
		for (ITridasSeries ser : lst) {
			Integer ringcount = ser.getValues().get(0).getValues().size();
			SafeIntYear startYear = null;
			try {
				
				// Make sure we're using years with the right calendar
				//Year yearsWithCalendar = new SafeIntYear(ser.getInterpretation().getFirstYear()).toTridasYear(calendar);
				startYear = new SafeIntYear(ser.getInterpretation().getFirstYear());
				
			} catch (NullPointerException e) {
				startYear = new SafeIntYear();
			}
			
			YearRange thisrange = new YearRange(startYear, ringcount);
			
			if (yrRange == null) {
				yrRange = thisrange;
			}
			
			yrRange = yrRange.union(thisrange);
		}
		
		// Set the list
		seriesList = lst;
	}
	
	@Override
	public String getExtension() {
		return "csv";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		return seriesList.toArray(new ITridasSeries[0]);

	}
	
	@Override
	public String[] saveToString() {	
		OdfSpreadsheetDocument outputDocument;
		ArrayList<String> lines = new ArrayList<String>();
		
		try {
			// Compile spreadsheet using ODFToolkit
			outputDocument = OdfSpreadsheetDocument.newSpreadsheetDocument();
			OdfTable table; 
			table = outputDocument.getTableList().get(0);
			writeYearHeaderCol(table);
			
			int col = 1;
			for (ITridasSeries series : seriesList) {
				writeRingWidthColumn(table, col, series);
				col++;
			}	
			
			// Convert ODF Spreadsheet into CSV doc
			for(int row=0; row < table.getRowCount(); row++)
			{
				String line = "";
				for(int column=0; column < table.getColumnCount(); column++)
				{
					line+= table.getCellByPosition(column, row).getStringValue() + ",";
				}
				line = line.substring(0, line.length()-2) ;
				lines.add(line);
			}
			
			return lines.toArray(new String[0]);
			
		} catch (Exception e) {

			System.out.println("Failed to write to file");
			e.printStackTrace();
			return null;
		}

	}

	
	/**
	 * Write the range of years in the first column of the worksheet
	 * 
	 * @param table
	 */
	private void writeYearHeaderCol(OdfTable table) {
		if (yrRange == null) {
			return;
		}
		
		// Create year label
		String yearlabel = I18n.getText("general.years");
		if (calendar == DatingSuffix.BP) {
			yearlabel += " (" + I18n.getText("general.years.bp") + ")";
		}
		else if ((yrRange.getStart().compareTo(new SafeIntYear("-1")) < 0)
				&& (yrRange.getEnd().compareTo(new SafeIntYear("1")) > 0)) {
			yearlabel += " (" + I18n.getText("general.years.bc") + "/" + I18n.getText("general.years.ad") + ")";
		}
		else if ((yrRange.getStart().compareTo(new SafeIntYear("-1")) < 0)
				&& (yrRange.getEnd().compareTo(new SafeIntYear("1")) <= 0)) {
			yearlabel += " (" + I18n.getText("general.years.bc") + ")";
		}
		else if ((yrRange.getStart().compareTo(new SafeIntYear("-1")) >= 0)
				&& (yrRange.getEnd().compareTo(new SafeIntYear("1")) > 0)) {
			yearlabel += " (" + I18n.getText("general.years.ad") + ")";
		}
	
		table.getCellByPosition(0, 0).setStringValue(yearlabel);
		
		SafeIntYear yr = yrRange.getStart();
		Integer rowNumber = 1;
		Double yearval;
		while (yr.compareTo(yrRange.getEnd()) <= 0) {
			yearval = Double.parseDouble(yr.toTridasYear(calendar).getValue().toString());
			
			table.getCellByPosition(0, rowNumber).setDoubleValue(yearval);
			table.getCellByPosition(0, rowNumber).setFormatString("0");
			// Increment to next year and row number
			yr = yr.add(1);
			rowNumber++;
		}
		
	}
	

	/**
	 * Write the ring widths for the provided series in the specified column
	 * 
	 * @param table
	 * @param col
	 * @param series
	 */
	private void writeRingWidthColumn(OdfTable table, Integer col, ITridasSeries series)  {
		List<TridasValue> values = series.getValues().get(0).getValues();
		
		// Creates year label
		String l;
		String keycode = null;
		if(series.isSetGenericFields())
		{
			for(TridasGenericField gf : series.getGenericFields())
			{
				if(gf.getName().toLowerCase().equals("keycode"))
				{
					keycode = gf.getValue();
				}
			}
		}
		if(keycode!=null)
		{
			l = keycode;
		}
		else
		{
		    l = series.getTitle();
		}
		
		table.getCellByPosition(col, 0).setStringValue(l);
		
		// Calculate which row to start on
		SafeIntYear thisStartYear = new SafeIntYear();
		try {
			thisStartYear = new SafeIntYear(series.getInterpretation().getFirstYear());
		} catch (NullPointerException e) {}
		
		Integer row = 1;
		
		for (SafeIntYear currYear = yrRange.getStart(); currYear.compareTo(thisStartYear) < 0; currYear = currYear
				.add(1)) {
			table.getCellByPosition(col, row).setStringValue("");
			row++;
		}
		
		// Loop through values and write to spreadsheet
		Double yearval;
		for (TridasValue value : values) {
			if(value.getValue()==null);
			
			yearval = Double.parseDouble(value.getValue());
			table.getCellByPosition(col, row).setDoubleValue(yearval);
			row++;
		}
		
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
}
