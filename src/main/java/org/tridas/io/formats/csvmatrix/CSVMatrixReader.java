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
package org.tridas.io.formats.csvmatrix;

import java.io.File;
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
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.InvalidDendroFileException.PointerType;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.DatingSuffix;
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

public class CSVMatrixReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(CSVMatrixReader.class);
	private CSVMatrixToTridasDefaults defaults;
	private OdfTable sheet;
	private ArrayList<ODFDendroSeries> series = new ArrayList<ODFDendroSeries>();
	
	public CSVMatrixReader()
	{
		super(CSVMatrixToTridasDefaults.class);
	}
	
	@Override
	public DendroFileFilter getDendroFileFilter() {
		String[] exts = new String[] {"csv"};
		
		return new DendroFileFilter(exts, getShortName());

	}

	@Override
	public String getDescription() {
		return I18n.getText("csv.about.description");
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"csv"};
	}

	@Override
	public String getFullName() {
		return I18n.getText("csv.about.fullName");
	}

	@Override
	public String getShortName() {
		return I18n.getText("csv.about.shortName");
	}

	/**
	 * Check this is a valid CSV file
	 * 
	 * @throws InvalidDendroFileException
	 */
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) 
				throws InvalidDendroFileException {
		log.debug("loading file");
		defaults = (CSVMatrixToTridasDefaults) argDefaultFields;
		
		OdfSpreadsheetDocument doc;

		try {
			doc = OdfSpreadsheetDocument.newSpreadsheetDocument();
			
			OdfTable table; 
			
			table = doc.getTableList().get(0);

			int row = -1;
			int col = -1;
			int colcount = 0;
			for(String line : argFileString)
			{	
				row++;
				col=-1;
				String[] vals = line.split(",");
				if(colcount==0) colcount = vals.length;
				
				/*if(colcount!=vals.length)
				{
					throw new InvalidDendroFileException("Inconsistent number of columns", row+1);
				}*/
				
				for(String val : vals)
				{
					col++;
					if(row==0)
					{
						table.getCellByPosition(col, row).setStringValue(val);
					}
					else 
					{
						table.getCellByPosition(col, row).setDoubleValue(Double.parseDouble(val));
					}
					
					if(col==0 && row!=0)
					{
						table.getCellByPosition(col, row).setFormatString("0");
					}
					
				}
				
			}
			
			doc.save(new File("/tmp/out.ods"));
			
			parseFile(doc);
			
		} catch (Exception e) {

			throw new InvalidDendroFileException(e.getLocalizedMessage());
		}
		
	}
	
	
		

	protected void parseFile(OdfSpreadsheetDocument doc) throws InvalidDendroFileException{
	
		if(doc==null) throw new InvalidDendroFileException(I18n.getText("excelmatrix.workbookError"));
				
		sheet = doc.getTableList().get(0);

		// Check year column is valid
		OdfTableColumn yearCol = sheet.getColumnByIndex(0);
		Integer lastval = null;
		Integer thisval = null;
		log.debug("Cell count : "+yearCol.getCellCount());
		for (int i=1; i < (yearCol.getCellCount()); i++)
		{
			if(sheet.getCellByPosition(0, i).getStringValue().equals(""))
			{
				break;
			}
			
			// Check cell is an integer
			try{
				thisval = Integer.parseInt(sheet.getCellByPosition(0, i).getStringValue());
				//log.debug("Row "+ (i+1) +" = "+thisval);
				
				if(thisval.equals(0))
				{
					throw new InvalidDendroFileException(
							I18n.getText("excelmatrix.yearsNotGregorian"), 
							String.valueOf(i), PointerType.LINE);
				}
				
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.yearNumberExpected"), 
						String.valueOf(i), PointerType.LINE);
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
						String.valueOf(i+1), PointerType.LINE);
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
						1, PointerType.LINE);
			}
			
			// Warn if there is more data than years
			if(datacol.getCellCount()>yearCol.getCellCount())
			{
				this.addWarning(new ConversionWarning(WarningType.IGNORED, 
						I18n.getText("excelmatrix.moreDataThanYears",
						String.valueOf(i))));
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
								String.valueOf(j+1), 
								PointerType.LINE);
					}
						
					dataVals.add(datacol.getCellByIndex(j).getDoubleValue());
						
					
					
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(
							I18n.getText("excelmatrix.invalidDataValue"), 
							String.valueOf(j+1), 
							PointerType.LINE);
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
				if(StringUtils.isStringWholeInteger(dbl.toString()))
				{
					Integer intval = dbl.intValue();
					val.setValue(intval.toString());
				}
				else
				{
					val.setValue(dbl.toString());
				}
				
				valuesList.add(val);
			}
			
			TridasValues valuesGroup = new TridasValues();
			TridasVariable variable = new TridasVariable();
			variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
			TridasUnit units = new TridasUnit();
			units.setValue(I18n.getText("Unknown"));
			
			valuesGroup.setVariable(variable);
			valuesGroup.setUnit(units);
			valuesGroup.setValues(valuesList);

			ms.getValues().add(valuesGroup);
			
			project.getObjects().add(o);
		}
		
		
		
		return project;
	}

	private static class ODFDendroSeries {
		public CSVMatrixToTridasDefaults defaults;
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

