package org.tridas.io.formats.excelmatrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jxl.Cell;
import jxl.CellType;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

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
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class ExcelMatrixReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(ExcelMatrixReader.class);
	private ExcelMatrixToTridasDefaults defaults;
	private Sheet sheet;
	private Cell[] yearCol;
	private ArrayList<ExcelDendroSeries> series = new ArrayList<ExcelDendroSeries>();
	
	public ExcelMatrixReader()
	{
		super(ExcelMatrixToTridasDefaults.class);
	}
	
	@Override
	public DendroFileFilter getDendroFileFilter() {
		String[] exts = new String[] {"xls"};
		
		return new DendroFileFilter(exts, getShortName());

	}

	@Override
	public String getDescription() {
		return I18n.getText("excelmatrix.about.description");
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"xls"};
	}

	@Override
	public String getFullName() {
		return I18n.getText("excelmatrix.about.fullName");
	}

	@Override
	public String getShortName() {
		return I18n.getText("excelmatrix.about.shortName");
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
		defaults = (ExcelMatrixToTridasDefaults) argDefaultFields;
		File file = new File(argFilename);
		
		try {
			Workbook wb = Workbook.getWorkbook(file);
			parseFile(wb);
		} catch (BiffException e) {
			throw new InvalidDendroFileException(e.getMessage());
		}

	}
	
	@Override
	public void loadFile(String argPath, String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		
		log.debug("loading file from: " + argPath + File.separatorChar + argFilename);
		defaults = (ExcelMatrixToTridasDefaults) argDefaultFields;
		File file = new File(argPath + File.separatorChar + argFilename);
		
		try {
			Workbook wb = Workbook.getWorkbook(file);
			parseFile(wb);
		} catch (BiffException e) {
			throw new InvalidDendroFileException(e.getMessage());
		}

	}
		
	/**
	 * Check this is a valid Excel file
	 * 
	 * @param argFileBytes
	 * @throws InvalidDendroFileException
	 */
	protected void parseFile(Workbook wb) throws InvalidDendroFileException{
	
		if(wb==null) throw new InvalidDendroFileException(I18n.getText("excelmatrix.workbookError"));
		
		if(wb.getSheets().length>1)
		{
			this.addWarning(new ConversionWarning(WarningType.IGNORED, 
					I18n.getText("excelmatrix.ignoringWorksheetsExcept",
							wb.getSheet(0).getName())));
		}
		
		sheet = wb.getSheet(0);

		// Check year column is valid
		Cell[] yearCol = sheet.getColumn(0);
		Integer lastval = null;
		Integer thisval = null;
		for (int i=1; i < yearCol.length; i++)
		{
			// Check cell is an integer
			try{
				thisval = Integer.parseInt(sheet.getCell(0, i).getContents());
				
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
		for(int i=1; i < sheet.getColumns(); i++)
		{
			Cell[] datacol = sheet.getColumn(i);
			ExcelDendroSeries edc = new ExcelDendroSeries();
			
			// Throw a wobbly if header is empty
			if(datacol[0].getContents().equals("")) 
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.emptyHeader"), 
						getColRef(i)+"1", PointerType.CELL);
			}
			
			// Warn if there is more data than years
			if(datacol.length>yearCol.length)
			{
				this.addWarning(new ConversionWarning(WarningType.IGNORED, 
						I18n.getText("excelmatrix.moreDataThanYears",
						getColRef(i))));
			}
					
			// Compile a list of the data values
			ArrayList<Double> dataVals = new ArrayList<Double>();
			Boolean atStartOfData =false;
			for(int j=1; j<datacol.length; j++)
			{
				if(atStartOfData== false && datacol[j].getContents().equals(""))
				{
					continue;
				}
				else if (atStartOfData == false)
				{
					atStartOfData = true;
					edc.startYear = this.getYearForRow(j);
				}
				else if(atStartOfData== true && datacol[j].getContents().equals(""))
				{
					break;
				}
				
				
				try{ 
					if(datacol[j].getType() != CellType.NUMBER)
					{
						throw new InvalidDendroFileException(
								I18n.getText("excelmatrix.invalidDataValue"), 
								getColRef(i)+String.valueOf(j+1), 
								PointerType.CELL);
					}
					
					NumberCell nc = (NumberCell) datacol[j];
					dataVals.add(nc.getValue());
					
					if(nc.getValue()>10d)
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
			
			edc.label = datacol[0].getContents();
			edc.defaults = defaults;
			edc.dataVals = dataVals;
			series.add(edc);
			
			
		}
		


	}
	
	private SafeIntYear getYearForRow(int row)
	{
		try{
			Cell cell = sheet.getCell(0, row);
			return new SafeIntYear(cell.getContents());
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
	
	@Override
	public TridasProject getProject() {
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
		public ExcelMatrixToTridasDefaults defaults;
		public SafeIntYear startYear;
		public String label;
		public ArrayList<Double> dataVals = new ArrayList<Double>();
		
	}

}

