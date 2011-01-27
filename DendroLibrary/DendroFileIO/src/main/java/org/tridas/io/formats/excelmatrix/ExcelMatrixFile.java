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
package org.tridas.io.formats.excelmatrix;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.YearRange;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasValue;

public class ExcelMatrixFile implements IDendroFile {
	
	private ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
	private YearRange yrRange;
	private DatingSuffix calendar = DatingSuffix.AD;
	private IMetadataFieldSet defaults;
	
	public ExcelMatrixFile(IMetadataFieldSet argDefaults) {
		defaults = argDefaults;
	}
	
	public void setSeriesList(ArrayList<ITridasSeries> lst) {
		
		
		// Switch the BP dating if any series are in BP
		for (ITridasSeries ser : lst) {
			
			if(ser.isSetInterpretation())
			{
				if(ser.getInterpretation().isSetDating())
				{
					if(ser.getInterpretation().getDating().getType().equals(NormalTridasDatingType.RELATIVE))
					{
						this.defaults.addConversionWarning(
								new ConversionWarning(WarningType.UNREPRESENTABLE, ""))
					}
				}
			}
			
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
		return "xls";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		// TODO Auto-generated method stub
		return null;
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
	public void saveToDisk(OutputStream os) throws IOException, WriteException {
		
		WritableWorkbook workbook = Workbook.createWorkbook(os);
		
		WritableSheet dataSheet = workbook.createSheet(I18n.getText("general.data"), 0);
		writeYearHeaderCol(dataSheet);
		// WritableSheet metadataSheet =
		// workbook.createSheet(I18n.getText("general.metadata"), 1);
		// writeMetadataHeaderCol(metadataSheet);
		
		int col = 1;
		for (ITridasSeries series : seriesList) {
			writeRingWidthColumn(dataSheet, col, series);
			// writeMetadataColumn(metadataSheet, col, series);
			col++;
		}
		
		CellView cv = new CellView();
		cv.setAutosize(true);
		dataSheet.setColumnView(0, cv);
		dataSheet.setColumnView(1, cv);
		dataSheet.setColumnView(2, cv);
		/*
		 * metadataSheet.setColumnView(0, cv);
		 * metadataSheet.setColumnView(1, cv);
		 * metadataSheet.setColumnView(2, cv);
		 */

		workbook.write();
		workbook.close();
		
	}
	
	/**
	 * Get the format for the standard header
	 * 
	 * @return
	 */
	private static WritableCellFormat getHeaderFormat() {
		// Create the Header Format
		WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
		WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
		try {
			headerFormat.setWrap(false);
			headerFormat.setBackground(Colour.PALE_BLUE);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return headerFormat;
	}
	
	/**
	 * Get the format for the standard data cell
	 * 
	 * @return
	 */
	private static WritableCellFormat getDataFormat() {
		// Create the Data Format
		WritableFont dataFont = new WritableFont(WritableFont.ARIAL, 12, WritableFont.NO_BOLD);
		WritableCellFormat dataFormat = new WritableCellFormat(dataFont);
		try {
			dataFormat.setWrap(false);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataFormat;
	}
	
	private void writeMetadataHeaderCol(WritableSheet s) throws WriteException {
		
		Label l = new Label(0, 0, "Metadata Field", getHeaderFormat());
		s.addCell(l);
		
		ArrayList<String> metadataKeys = new ArrayList<String>();
		metadataKeys.add("object.title");
		metadataKeys.add("element.title");
		
		int i = 1;
		for (String field : metadataKeys) {
			l = new Label(0, i, field, getDataFormat());
			s.addCell(l);
			i++;
		}
		
	}
	
	/**
	 * Write the range of years in the first column of the worksheet
	 * 
	 * @param s
	 * @throws WriteException
	 */
	private void writeYearHeaderCol(WritableSheet s) throws WriteException {
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
		
		Label l = new Label(0, 0, yearlabel, getHeaderFormat());
		s.addCell(l);
		
		SafeIntYear yr = yrRange.getStart();
		Integer rowNumber = 1;
		Number yearval;
		while (yr.compareTo(yrRange.getEnd()) <= 0) {
			yearval = new Number(0, rowNumber, Double.parseDouble(yr.toTridasYear(calendar).getValue().toString()),
					getDataFormat());
			s.addCell(yearval);
			
			// Increment to next year and row number
			yr = yr.add(1);
			rowNumber++;
		}
		
	}
	
	private void writeMetadataColumn(WritableSheet s, Integer col, ITridasSeries series) throws RowsExceededException,
			WriteException {
		// Creates year label
		Label l;
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
			l = new Label(col, 0, keycode, getHeaderFormat());
		}
		else
		{
			l = new Label(col, 0, series.getTitle(), getHeaderFormat());
		}
		s.addCell(l);
		
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
	private void writeRingWidthColumn(WritableSheet s, Integer col, ITridasSeries series) throws RowsExceededException,
			WriteException {
		List<TridasValue> values = series.getValues().get(0).getValues();
		
		// Creates year label
		Label l;
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
			l = new Label(col, 0, keycode, getHeaderFormat());
		}
		else
		{
		    l = new Label(col, 0, series.getTitle(), getHeaderFormat());
		}
		s.addCell(l);
		
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
		Label yearval;
		for (TridasValue value : values) {
			yearval = new Label(col, row, value.getValue(), getDataFormat());
			s.addCell(yearval);
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
