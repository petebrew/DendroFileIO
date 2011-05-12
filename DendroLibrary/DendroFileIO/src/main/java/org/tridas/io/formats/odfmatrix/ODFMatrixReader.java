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
package org.tridas.io.formats.odfmatrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
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

public class ODFMatrixReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(ODFMatrixReader.class);
	private ODFMatrixToTridasDefaults defaults;
	private OdfTable sheet;
	private ArrayList<ODFDendroSeries> series = new ArrayList<ODFDendroSeries>();
	
	public ODFMatrixReader()
	{
		super(ODFMatrixToTridasDefaults.class);
	}
	
	@Override
	public DendroFileFilter getDendroFileFilter() {
		String[] exts = new String[] {"xls"};
		
		return new DendroFileFilter(exts, getShortName());

	}

	@Override
	public String getDescription() {
		return I18n.getText("odfmatrix.about.description");
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"ods"};
	}

	@Override
	public String getFullName() {
		return I18n.getText("odfmatrix.about.fullName");
	}

	@Override
	public String getShortName() {
		return I18n.getText("odfmatrix.about.shortName");
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
		defaults = (ODFMatrixToTridasDefaults) argDefaultFields;
		File file = new File(argFilename);
		
		try {
			OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(file);
			parseFile(doc);
		} catch (Exception e) {
			throw new InvalidDendroFileException(e.getLocalizedMessage());
		}

	}
	
	@Override
	public void loadFile(String argPath, String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		
		log.debug("loading file from: " + argPath + File.separatorChar + argFilename);
		defaults = (ODFMatrixToTridasDefaults) argDefaultFields;
		File file = new File(argPath + File.separatorChar + argFilename);
		
		try {
			OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(file);
			parseFile(doc);
		} catch (Exception e) {
			throw new InvalidDendroFileException(e.getLocalizedMessage());
		}

	}
		
	/**
	 * Check this is a valid ODF file
	 * 
	 * @param argFileBytes
	 * @throws InvalidDendroFileException
	 */
	protected void parseFile(OdfSpreadsheetDocument doc) throws InvalidDendroFileException{
	
		if(doc==null) throw new InvalidDendroFileException(I18n.getText("excelmatrix.workbookError"));
		
		if(doc.getTableList().size()>1)
		{
			this.addWarning(new ConversionWarning(WarningType.IGNORED, 
					I18n.getText("excelmatrix.ignoringWorksheetsExcept",
							doc.getTableList().get(0).getTableName())));
		}
		
		sheet = doc.getTableList().get(0);

		// Check year column is valid
		OdfTableColumn yearCol = sheet.getColumnByIndex(0);
		Integer lastval = null;
		Integer thisval = null;
		System.out.println("Cell count : "+yearCol.getCellCount());
		for (int i=1; i < (yearCol.getCellCount()); i++)
		{
			if(sheet.getCellByPosition(0, i).getStringValue().equals(""))
			{
				break;
			}
			
			// Check cell is an integer
			try{
				thisval = Integer.parseInt(sheet.getCellByPosition(0, i).getStringValue());
				//System.out.println("Row "+ (i+1) +" = "+thisval);
				
				if(thisval.equals(0))
				{
					throw new InvalidDendroFileException(
							I18n.getText("excelmatrix.yearsNotGregorian"), 
							"A"+String.valueOf(i), PointerType.CELL);
				}
				
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.yearNumberExpected"), 
						"A"+String.valueOf(i), PointerType.CELL);
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
						"A"+String.valueOf(i+1), PointerType.CELL);
			}
		}
		
		// Loop through data columns
		for(int i=1; i < sheet.getColumnCount(); i++)
		{
			OdfTableColumn datacol = sheet.getColumnByIndex(i);
			ODFDendroSeries edc = new ODFDendroSeries();
			
			// Throw a wobbly if header is empty
			if(datacol.getCellByIndex(0).getStringValue().equals("")) 
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.emptyHeader"), 
						getColRef(i)+"1", PointerType.CELL);
			}
			
			// Warn if there is more data than years
			if(datacol.getCellCount()>yearCol.getCellCount())
			{
				this.addWarning(new ConversionWarning(WarningType.IGNORED, 
						I18n.getText("excelmatrix.moreDataThanYears",
						getColRef(i))));
			}
					
			// Compile a list of the data values
			ArrayList<Double> dataVals = new ArrayList<Double>();
			Boolean atStartOfData =false;
			for(int j=1; j<datacol.getCellCount(); j++)
			{
				if(atStartOfData== false && datacol.getCellByIndex(j).getStringValue().equals(""))
				{
					continue;
				}
				else if (atStartOfData == false)
				{
					atStartOfData = true;
					edc.startYear = this.getYearForRow(j);
				}
				else if(atStartOfData== true && datacol.getCellByIndex(j).getStringValue().equals(""))
				{
					break;
				}
				
				
				try{ 
					if(!datacol.getCellByIndex(j).getValueType().equals("float"))
					{
						throw new InvalidDendroFileException(
								I18n.getText("excelmatrix.invalidDataValue"), 
								getColRef(i)+String.valueOf(j+1), 
								PointerType.CELL);
					}
						
					dataVals.add(datacol.getCellByIndex(j).getDoubleValue());
					
					if(datacol.getCellByIndex(j).getDoubleValue()>10d)
					{
						this.addWarning(new ConversionWarning(WarningType.ASSUMPTION, 
								I18n.getText("excelmatrix.largeDataValue")));
					}
					
					
					
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(
							I18n.getText("excelmatrix.invalidDataValue"), 
							getColRef(i)+String.valueOf(j+1), 
							PointerType.CELL);
				}
			}
			
			edc.label = datacol.getCellByIndex(0).getStringValue();
			edc.defaults = defaults;
			edc.dataVals = dataVals;
			series.add(edc);
			
			
		}
		


	}
	
	private SafeIntYear getYearForRow(int row)
	{
		try{
			return new SafeIntYear(sheet.getCellByPosition(0, row).getStringValue());
		} catch (NumberFormatException e)
		{
			return null;
		}
	}
	
	/**
	 * Get the column reference for a column number 
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
		
		for (ODFDendroSeries eds : series)
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

	private static class ODFDendroSeries {
		public ODFMatrixToTridasDefaults defaults;
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

