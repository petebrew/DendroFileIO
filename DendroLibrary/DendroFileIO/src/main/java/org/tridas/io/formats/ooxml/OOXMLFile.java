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
package org.tridas.io.formats.ooxml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.YearRange;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasValue;

public class OOXMLFile implements IDendroFile {
	
	private ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
	private YearRange yrRange;
	private DatingSuffix calendar = DatingSuffix.AD;
	private IMetadataFieldSet defaults;
	private CreationHelper createHelper;

	public OOXMLFile(IMetadataFieldSet argDefaults) {
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
		return "xlsx";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		return seriesList.toArray(new ITridasSeries[0]);

	}
	
	@Override
	public String[] saveToString() {
		
		throw new UnsupportedOperationException(I18n.getText("fileio.binaryAsStringUnsupported"));
		
	}
	
	/**
	 * An alternative to the normal saveToString() as this is a binary format
	 * 
	 * @param os
	 * @throws IOException
	 * @throws WriteException
	 */
	public void saveToDisk(OutputStream os) throws IOException {
		
		Workbook workbook = new XSSFWorkbook(); 
		createHelper = workbook.getCreationHelper();
		
		Sheet dataSheet = workbook.createSheet(I18n.getText("general.data"));
		createRows(dataSheet);
		
		
		// WritableSheet metadataSheet =
		// workbook.createSheet(I18n.getText("general.metadata"), 1);
		// writeMetadataHeaderCol(metadataSheet);
		
		int col = 1;
		for (ITridasSeries series : seriesList) {
			writeRingWidthColumn(dataSheet, col, series);
			col++;
		}
		
		workbook.write(os);
		os.close();
		
	}
	
	private void createRows(Sheet s)
	{
		if (yrRange == null) {
			return;
		}
		
		Row labelRow = s.createRow(0);
		
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
		
		labelRow.createCell(0).setCellValue(createHelper.createRichTextString(yearlabel));
		
		SafeIntYear yr = yrRange.getStart();
		Integer rowNumber = 1;
		Integer yearval;
		while (yr.compareTo(yrRange.getEnd()) <= 0) {
			yearval = Integer.parseInt(yr.toTridasYear(calendar).getValue().toString());
			s.createRow(rowNumber).createCell(0).setCellValue(yearval);
						
			// Increment to next year and row number
			yr = yr.add(1);
			rowNumber++;
		}
		
	}
		
	
	/**
	 * Write the ring widths for the provided series in the specified column
	 * 
	 * @param s
	 * @param col
	 * @param series
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	private void writeRingWidthColumn(Sheet s, Integer col, ITridasSeries series) 
	{
		List<TridasValue> values = series.getValues().get(0).getValues();
		
		// Creates label
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
			s.getRow(0).createCell(col).setCellValue(createHelper.createRichTextString(keycode));
		}
		else
		{
			s.getRow(0).createCell(col).setCellValue(createHelper.createRichTextString(series.getTitle()));
		}
		
		// Calculate which row to start on
		SafeIntYear thisStartYear = new SafeIntYear();
		try {
			thisStartYear = new SafeIntYear(series.getInterpretation().getFirstYear());
		} catch (NullPointerException e) {}
		
		Integer row = 1;
		
		for (SafeIntYear currYear = yrRange.getStart(); currYear.compareTo(thisStartYear) < 0; currYear = currYear
				.add(1)) {
			row++;
		}
		
		// Loop through values and write to spreadsheet
		for (TridasValue value : values) {
			s.getRow(row).createCell(col).setCellValue(createHelper.createRichTextString(value.getValue()));
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
