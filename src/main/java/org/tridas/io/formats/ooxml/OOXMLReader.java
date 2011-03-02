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
package org.tridas.io.formats.ooxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.InvalidDendroFileException.PointerType;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class OOXMLReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(OOXMLReader.class);
	private OOXMLToTridasDefaults defaults;
	private Sheet sheet;
	private Cell[] yearCol;
	private ArrayList<ExcelDendroSeries> series = new ArrayList<ExcelDendroSeries>();
	
	public OOXMLReader()
	{
		super(OOXMLToTridasDefaults.class);
	}
	
	@Override
	public DendroFileFilter getDendroFileFilter() {
		String[] exts = new String[] {"xlsx"};
		
		return new DendroFileFilter(exts, getShortName());

	}

	@Override
	public String getDescription() {
		return I18n.getText("ooxml.about.description");
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"xlsx"};
	}

	@Override
	public String getFullName() {
		return I18n.getText("ooxml.about.fullName");
	}

	@Override
	public String getShortName() {
		return I18n.getText("ooxml.about.shortName");
	}

	// *******************************
	// NOT SUPPORTED - BINARY FORMAT
	// *******************************
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) {
		throw new UnsupportedOperationException(I18n.getText("general.binaryNotText"));
	}
	
	@Override
	public void loadFile(String[] argFileStrings) throws InvalidDendroFileException {
		throw new UnsupportedOperationException(I18n.getText("general.binaryNotText"));
	}
		
	
	/**
	 * @throws IncorrectDefaultFieldsException
	 * @throws InvalidDendroFileException
	 * @see org.tridas.io.IDendroCollectionWriter#loadFile(java.lang.String)
	 */
	@Override
	public void loadFile(String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		
		log.debug("loading file from: " + argFilename);
		defaults = (OOXMLToTridasDefaults) argDefaultFields;
		InputStream file = new FileInputStream(argFilename);
		
		try {
			Workbook wb = WorkbookFactory.create(file);
			parseFile(wb);
		} catch (InvalidDendroFileException e) {
			throw e;
		} catch (Exception e)
		{
			throw new InvalidDendroFileException(e.getMessage());
		}

	}
	
	@Override
	public void loadFile(String argPath, String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		
		log.debug("loading file from: " + argPath + File.separatorChar + argFilename);
		defaults = (OOXMLToTridasDefaults) argDefaultFields;
		InputStream file = new FileInputStream(argPath + File.separatorChar + argFilename);
		
		try {
			Workbook wb = WorkbookFactory.create(file);
			parseFile(wb);
		} catch (InvalidDendroFileException e) {
			throw e;
		} catch (Exception e)
		{
			throw new InvalidDendroFileException(e.getMessage());
		}

	}
		
	private String getCellValueAsString(Cell cell)
	{
		if(cell.getCellType() == Cell.CELL_TYPE_BLANK)
		{
			return null;
		}
		else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
		{
			if(cell.getBooleanCellValue()==true)
			{
				return "true";
			}
			else
			{
				return "false";
			}
		}
		else if (cell.getCellType() == Cell.CELL_TYPE_ERROR)
		{
			return null;
		}
		else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA)
		{
			try
			{	
				return cell.getStringCellValue();
			} catch (Exception e)
			{
				return null;
			}
		}
		else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
		{
			return String.valueOf(cell.getNumericCellValue());
		}
		else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
		{
			return cell.getStringCellValue();
		}
		
		
		return null;
		
	}
	
	
	/**
	 * Check this is a valid Excel file
	 * 
	 * @param argFileBytes
	 * @throws InvalidDendroFileException
	 */
	protected void parseFile(Workbook wb) throws InvalidDendroFileException{
	
		if(wb==null) throw new InvalidDendroFileException(I18n.getText("excelmatrix.workbookError"));
		
		try{
			wb.getSheetAt(1);
			this.addWarning(new ConversionWarning(WarningType.IGNORED, 
					I18n.getText("excelmatrix.ignoringWorksheetsExcept",
							wb.getSheetAt(0).getSheetName())));
		} catch (Exception e){ }
		
		
		sheet = wb.getSheetAt(0);

		// Check year column is valid
		Integer lastval = null;
		Integer thisval = null;
		Integer ringCount = 0;
		for (int row=1; row < 1000; row++)
		{
			try{
				if (sheet.getRow(row).getCell(0).getCellType()== Cell.CELL_TYPE_BLANK) break;
			} catch (Exception e){ break; }
			
			ringCount = row;
			
			
			// Check cell is an integer
			try{
				thisval = (int) sheet.getRow(row).getCell(0).getNumericCellValue();
				if(thisval.equals(0))
				{
					throw new InvalidDendroFileException(
							I18n.getText("excelmatrix.yearsNotGregorian"), 
							"A"+String.valueOf(row+1), PointerType.CELL);
				}
				
			} catch (Exception e)
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.yearNumberExpected"), 
						"A"+String.valueOf(row+1), PointerType.CELL);
			}
			
			if (lastval==null) 
			{
				// First year 
				lastval = thisval;
				continue;
			}
			
			SafeIntYear previousYear = new SafeIntYear(lastval);
			SafeIntYear thisYear = new SafeIntYear(thisval);
			
			if(previousYear.add(1).equals(thisYear))
			{
				// Next year in sequence - so ok
				lastval = thisval;
				continue;
			}
			else
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.invalidYearSequence"), 
						"A"+String.valueOf(row+1), PointerType.CELL);
			}
		}
		
		// Loop through data columns
		for(int col=1; col < 100000; col++)
		{
			try{
				if (sheet.getRow(0).getCell(col).getCellType()== Cell.CELL_TYPE_BLANK) break;
			} catch (Exception e){ break; }
			
			ExcelDendroSeries edc = new ExcelDendroSeries();
				
			// Compile a list of the data values
			ArrayList<Double> dataVals = new ArrayList<Double>();
			Boolean atStartOfData =false;
			for(int rowindex=1; rowindex<=ringCount; rowindex++)
			{		
				Row row = sheet.getRow(rowindex);
				
				try{
					if (sheet.getRow(rowindex).getCell(col).getCellType()== Cell.CELL_TYPE_BLANK) continue;
				} catch (Exception e){ continue; }
				
				if (atStartOfData == false)
				{
					atStartOfData = true;
					edc.startYear = this.getYearForRow(rowindex);
				}
				else if(atStartOfData== true && row.getCell(col) == null)
				{
					break;
				}
				
				try{ 
					if(row.getCell(col).getCellType() != Cell.CELL_TYPE_NUMERIC)
					{
						throw new InvalidDendroFileException(
								I18n.getText("excelmatrix.invalidDataValue"), 
								getColRef(col)+String.valueOf(rowindex+1), 
								PointerType.CELL);
					}
					
					Double nc = row.getCell(col).getNumericCellValue();
					dataVals.add(nc);
					
					if(nc>10d)
					{
						this.addWarning(new ConversionWarning(WarningType.ASSUMPTION, 
								I18n.getText("excelmatrix.largeDataValue")));
					}
					
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(
							I18n.getText("excelmatrix.invalidDataValue"), 
							getColRef(col)+String.valueOf(rowindex+1), 
							PointerType.CELL);
				}
			}
			
			edc.label = getCellValueAsString(sheet.getRow(0).getCell(col));
			edc.defaults = defaults;
			edc.dataVals = dataVals;
			series.add(edc);
			
			
		}
		


	}
	
	private SafeIntYear getYearForRow(int row) throws InvalidDendroFileException
	{
		try{
			Cell cell = sheet.getRow(row).getCell(0);
			
			if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
			{
				Double val = cell.getNumericCellValue();
				return new SafeIntYear(val.intValue());
			}
			else
			{
				throw new InvalidDendroFileException("Year value is not a number",
						"A"+String.valueOf(row+1), 
						PointerType.CELL);
			}
		
		} catch (NumberFormatException e)
		{
			return null;
		}
	}
	
	/**
	 * Get the Excel column reference for a column number 
	 * 
	 * @param col <= 676
	 * @return
	 */
	private String getColRef(int col)
	{
		String colcodes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
				
		if(col<0) return null;
		
		if(col > 676)
		{
			System.out.println("Error.  getColRef called with number out of range");
			return "??";
		}
		else if (col < 26)
		{
			return String.valueOf(colcodes.charAt(col));
		}
		else
		{
			int quotient = col / 26;
			int remainder = col % 26;
			return String.valueOf(colcodes.charAt(quotient-1)) + String.valueOf(colcodes.charAt(remainder));
		}		
	}
	
	@Override
	protected void resetReader() {
		sheet = null;
		yearCol = null;
		defaults = null;

	}
	
	@Override
	public int getCurrentLineNumber() {
		return 0;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	private TridasProject getProject() {
		TridasProject project = defaults.getProjectWithDefaults();
		
		for (ExcelDendroSeries eds : series)
		{
			TridasObject o = eds.defaults.getObjectWithDefaults(true);
			TridasMeasurementSeries ms = o.getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
			
			ms.setTitle(eds.label);
			
			TridasInterpretation interp = new TridasInterpretation();
			interp.setFirstYear(eds.startYear.toTridasYear(DatingSuffix.AD));
			ms.setInterpretation(interp);
			
			ArrayList<TridasValue> valuesList = new ArrayList<TridasValue>();
			for(Double dbl : eds.dataVals)
			{
				TridasValue val = new TridasValue();
				val.setValue(dbl.toString());
				valuesList.add(val);
			}
			
			TridasValues valuesGroup = new TridasValues();
			TridasVariable variable = new TridasVariable();
			variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
			TridasUnit units = new TridasUnit();
			units.setNormalTridas(NormalTridasUnit.MILLIMETRES);
			
			valuesGroup.setVariable(variable);
			valuesGroup.setUnit(units);
			valuesGroup.setValues(valuesList);

			ms.getValues().add(valuesGroup);
			
			project.getObjects().add(o);
		}
		
		
		
		return project;
	}

	private static class ExcelDendroSeries {
		public OOXMLToTridasDefaults defaults;
		public SafeIntYear startYear;
		public String label;
		public ArrayList<Double> dataVals = new ArrayList<Double>();
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		TridasProject projects[] = new TridasProject[1];
		projects[0] = this.getProject();
		return projects;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getTridasContainer()
	 */
	public TridasTridas getTridasContainer() {
		TridasTridas container = new TridasTridas();
		List<TridasProject> list = Arrays.asList(getProjects());
		container.setProjects(list);
		return container;
	}

}

