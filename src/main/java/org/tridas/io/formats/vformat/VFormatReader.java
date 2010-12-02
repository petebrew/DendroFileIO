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
package org.tridas.io.formats.vformat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatDataType;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatParameter;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatStatType;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.DateTime;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;

public class VFormatReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(VFormatReader.class);
	private VFormatToTridasDefaults defaults = new VFormatToTridasDefaults();
	private ArrayList<VFormatSeries> seriesList = new ArrayList<VFormatSeries>();
	private Integer currentLineNumber = 0;
	private Integer formatVersion = 12;	
	
	enum VFormatLineType {
		HEADER_1, HEADER_2, HEADER_3, HEADER_4, DATA, INVALID;
	}
	
	public VFormatReader() {
		super(VFormatToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		defaults = (VFormatToTridasDefaults) argDefaultFields;
		
		// Check the file is valid
		checkFile(argFileString);
				
		// Create a new series and set line type to 'DATA'
		VFormatSeries series = new VFormatSeries();
		series.defaults = (VFormatToTridasDefaults) defaults.clone();
		VFormatLineType lastLineType = VFormatLineType.DATA;
		
		// Loop through each line in file
		for (String line : argFileString) 
		{
			currentLineNumber++;
			
			// CHECK IF THIS IS FIRST LINE OF SERIES
			if(checkLineType(line, VFormatLineType.HEADER_1))
			{
				// Last line should have been data otherwise something has gone wrong
				if (!lastLineType.equals(VFormatLineType.DATA)) {
					throw new InvalidDendroFileException(I18n.getText("vformat.invalidLine"), currentLineNumber);
				}
				
				// If series is not null then we should add it to our list
				// as we're just about to start another
				if (series.dataValues.size()>0) {
					this.addSeriesToList(series);
					series = new VFormatSeries();
					series.defaults = (VFormatToTridasDefaults) defaults.clone();
				}
				
				// Check whether this is a supported version format
				try {
					formatVersion = Integer.valueOf(line.substring(68, 70));
					if (formatVersion.compareTo(20) >= 0) {
						addWarning(new ConversionWarning(WarningType.NOT_STRICT, I18n.getText(
								"vformat.unsupportedFormat", String.valueOf(formatVersion))));
					}
				} catch (NumberFormatException e) 
				{
					throw new InvalidDendroFileException(I18n.getText("vformat.invalidFormatVersion"), currentLineNumber);

				}
														
				// Series ID
				series.defaults.getStringDefaultValue(DefaultFields.SERIES_ID).setValue(line.substring(0, 12));
				log.debug(line.substring(0, 11));
				
				// Project id
				series.defaults.getStringDefaultValue(DefaultFields.PROJECT_CODE).setValue(line.substring(0,1));
				log.debug(line.substring(0, 1));
				
				// Object id
				series.defaults.getStringDefaultValue(DefaultFields.OBJECT_CODE).setValue(line.substring(2,4));
				log.debug(line.substring(2, 4));
				
				// Tree id
				series.defaults.getStringDefaultValue(DefaultFields.TREE_CODE).setValue(line.substring(4,6));
				log.debug(line.substring(4, 6));
				
				// Height
				series.defaults.getStringDefaultValue(DefaultFields.HEIGHT_CODE).setValue(line.substring(6,7));
				log.debug(line.substring(6, 7));
				
				// Data type
				GenericDefaultValue<VFormatDataType> dataTypeField = (GenericDefaultValue<VFormatDataType>) series.defaults.getDefaultValue(DefaultFields.DATA_TYPE);
				dataTypeField.setValue(VFormatDataType.fromCode(line.substring(9,10)));
				log.debug(line.substring(9, 10));
				if(dataTypeField.getValue()==null)
				{
					throw new InvalidDendroFileException(I18n.getText("vformat.invalidDataType"), currentLineNumber);
				}
				
				// Stats treatment used
				series.defaults.getStringDefaultValue(DefaultFields.STAT_CODE).setValue(line.substring(10,11));
				log.debug(line.substring(10, 11));
				
				// Parameter
				GenericDefaultValue<VFormatParameter> parameterField = (GenericDefaultValue<VFormatParameter>) series.defaults.getDefaultValue(DefaultFields.PARAMETER_CODE);
				parameterField.setValue(VFormatParameter.fromCode(line.substring(11,12)));
				log.debug(line.substring(11,12));
				if(parameterField.getValue()==null)
				{
					throw new InvalidDendroFileException(I18n.getText("vformat.invalidParameter"), currentLineNumber);
				}
				
				// Units
				series.defaults.getStringDefaultValue(DefaultFields.UNIT).setValue(line.substring(12,15));
				log.debug(line.substring(12, 15));
				
				// Count of values
				try{
				series.defaults.getIntegerDefaultValue(DefaultFields.COUNT).setValue(Integer.parseInt(line.substring(15,20).trim()));
				log.debug(line.substring(15, 20));
				} catch (NumberFormatException e)
				{	
					if(!line.substring(15,20).trim().equals(""))
					{
						addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText(
								"fileio.unableToParse", "count")));
					}
				}

				// Species
				series.defaults.getStringDefaultValue(DefaultFields.SPECIES).setValue(line.substring(20,24));
				log.debug(line.substring(20, 24));
				
				// Last year
				try{
				series.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).setValue(new SafeIntYear(line.substring(24,30)));
				log.debug(line.substring(24, 30));
				} catch (NumberFormatException e)
				{
					if(!line.substring(24,30).trim().isEmpty())
					{
						addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText(
								"fileio.unableToParse", "lastYear")));
					}
				}
				
				// Description
				if((line.substring(30,50)!=null) && (!line.substring(30,50).trim().isEmpty()))
				{
					series.defaults.getStringDefaultValue(DefaultFields.DESCRIPTION).setValue(line.substring(30,50).trim());
					log.debug(line.substring(30, 50));
				}
	
				// Created date
				try{
					DateTime date = DateUtils.parseDateFromDayMonthYearString(line.substring(50,58));
					if(date!=null)
					{
						series.defaults.getDateTimeDefaultValue(DefaultFields.CREATED_DATE).setValue(date);
					}
				}
				catch (Exception e)
				{
					if(!line.substring(50,58).trim().equals(""))
					{
						addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText(
								"fileio.unableToParse", "createdDate")));
					}
				}
				
				// Analyst
				series.defaults.getStringDefaultValue(DefaultFields.ANALYST).setValue(line.substring(58,60));
				log.debug(line.substring(58, 60));

				// Updated date
				try{
					DateTime date = DateUtils.parseDateFromDayMonthYearString(line.substring(60,68));
					if(date!=null)
					{
						series.defaults.getDateTimeDefaultValue(DefaultFields.UPDATED_DATE).setValue(date);
					}
				}
				catch (Exception e)
				{
					if(!line.substring(60,68).trim().equals(""))
					{
						addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText(
								"fileio.unableToParse", "updatedDate")));
					}
				}
								
				// Unmeasured rings at start
				try{
					series.defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).setValue(Integer.parseInt(line.substring(70,73).trim()));
					log.debug(line.substring(70, 73));
				} catch (Exception e)
				{
					if(!line.substring(70,73).trim().equals(""))
					{
						addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText(
								"fileio.unableToParse", "unmeasuredRingsPre")));
					}
				}
				
				// Error for unmeasured rings at start
				series.defaults.getStringDefaultValue(DefaultFields.UNMEAS_PRE_ERR).setValue(line.substring(73,75));						
				log.debug(line.substring(73, 75));

				
				// Unmeasured rings at end
				try{
					series.defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_POST).setValue(Integer.parseInt(line.substring(75,78).trim()));
				} catch (Exception e)
				{
					if(!line.substring(75,78).trim().equals(""))
					{
						addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText(
								"fileio.unableToParse", "unmeasuredRingsPost")));
					}
				}
				
				// Error for unmeasured rings at end
				series.defaults.getStringDefaultValue(DefaultFields.UNMEAS_POST_ERR).setValue(line.substring(78,80));

				// Set this line type as the last linetype before continuing
				lastLineType = VFormatLineType.HEADER_1;
				
			
			}
			
			// If last line was HEADER1 then this must be HEADER2!
			else if(lastLineType.equals(VFormatLineType.HEADER_1))
			{				
				// Free text field
				series.defaults.getStringDefaultValue(DefaultFields.FREE_TEXT_FIELD).setValue(line.trim());
				
				// Set this line type as the last linetype before continuing
				lastLineType = VFormatLineType.HEADER_2;
				
			}
			
			// If last line was Header2 then this could be either HEADER3 or DATA depending on version
			else if ((lastLineType.equals(VFormatLineType.HEADER_2)) && (formatVersion>=10))
			{
				// Expecting HEADER3
				if (!checkLineType(line, VFormatLineType.HEADER_3))
				{
					throw new InvalidDendroFileException(I18n.getText("vformat.invalidLine"), currentLineNumber);
				}
				
				// Latitude and Longitude
				try {		
					series.defaults.getDoubleDefaultValue(DefaultFields.LATITUDE).setValue(Double.parseDouble(line.substring(10, 20)));
					series.defaults.getDoubleDefaultValue(DefaultFields.LONGITUDE).setValue(Double.parseDouble(line.substring(0, 10)));						
				} catch (NumberFormatException e) 
				{
					if(!line.substring(0,20).trim().equals(""))
					{
						addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText(
								"fileio.unableToParse", "coordinates")));
					}
				}
				
				// Set this line type as the last linetype before continuing
				lastLineType = VFormatLineType.HEADER_3;
				
			}
			
			
			// HEADER 3 should be followed by HEADER4 if version is >20
			else if (lastLineType.equals(VFormatLineType.HEADER_3))
			{
				if(formatVersion>=20)
				{
					// Expecting HEADER4
					if (!checkLineType(line, VFormatLineType.HEADER_4))
					{
						throw new InvalidDendroFileException(I18n.getText("vformat.invalidLine"), currentLineNumber);
					}
				}
				
				// Not extracting any HEADER4 info as it is variable between users
				lastLineType = VFormatLineType.HEADER_4;
				
			}
			
			// This should be data now
			else
			{
				if (!checkLineType(line, VFormatLineType.DATA))
				{
					throw new InvalidDendroFileException(I18n.getText("vformat.invalidLine"), currentLineNumber);
				}
								
				// Get this decades values
				ArrayList<TridasValue> thisDecadesValues = new ArrayList<TridasValue>();
				
				// Trim off trailing spaces
				if(line.length()>80)
				{
					line = line.substring(0, 80);
				}
				
				for (int i = 0; i+8 <= line.length(); i = i + 8) 
				{
					/*
					 * String validity = line.substring(i-1,i).trim();
					 * String importance = line.substring(i, i+1).trim();
					 * String remark = line.substring(i+1, i+2).trim();
					 */

					TridasValue theValue = new TridasValue();
					
					// Skip blank values at beginning/end of series
					if(line.substring(i + 3, i + 8).trim().equals("")) continue;
					
					theValue.setValue(line.substring(i + 3, i + 8).trim());
					thisDecadesValues.add(theValue);
				}
				
				// Add them to the series
				series.dataValues.addAll(thisDecadesValues);
				
				// Set this line type as the last linetype before continuing
				lastLineType = VFormatLineType.DATA;
			
			}
		}
	}
	
	/**
	 * Adds a VFormatSeries to the list, whilst setting a number of remaining 
	 * fields. 
	 * 
	 * @param series
	 */
	private void addSeriesToList(VFormatSeries series)
	{
		// Check count is correct, if not fix it
		if(series.defaults.getIntegerDefaultValue(DefaultFields.COUNT).getValue()!=null)
		{
			if (!series.defaults.getIntegerDefaultValue(DefaultFields.COUNT).getValue()
					.equals(series.dataValues.size()))
			{
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText(
						"fileio.valueCountMismatch", 
						series.dataValues.size()+"",
						series.defaults.getIntegerDefaultValue(DefaultFields.COUNT).getValue()+"")));
				series.defaults.getIntegerDefaultValue(DefaultFields.COUNT).setValue(series.dataValues.size());
			}		
		}
		
		// Set First year from last year and count
		if(series.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue()!=null)
		{
			series.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).setValue(
					series.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue()
					.add(1-series.dataValues.size()));
		}
		
		// Add to list
		seriesList.add(series);
	}
	
	private Boolean checkLineType(String line, VFormatLineType type)
	{

		String regex = null;
		Pattern p1;
		Matcher m1;
		
		// If line is empty return false 
		if (line == null) {
			return false;
		}
		
		// Line should only be 80 chars long, but be cool and allow longer
		// lines as long as the trailing characters are white space
		if (line.length() > 80) {
			if(!line.substring(80).matches("^\\s*$")){
				return false;
			}
			line = line.substring(0,80);
		}
			
		switch(type)
		{
		case HEADER_1:
			regex = "^[\\S\\s]{8}.[!%#][FIMOPQRSTWXZ][DFGJKPS][\\S\\s]{12}[\\d\\s]{6}[\\S\\s]{20}[\\d\\s/.]{8}[\\S\\s]{10}[\\d}]{2}[\\d\\s.]{10}$";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				return true;
			}
			else
			{
				return false;
			}
			
		case HEADER_2:
			// Free text line so always matches!
			return true;
			
		case HEADER_3:
		case HEADER_4:
			regex = "^[- \\d.]{0,80}$";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				return true;
			}
			else
			{
				return false;
			}		
			
		case DATA:
			regex = "^([ !\"#$%&\'\\w]{3}[ \\d.]{4}[\\d]{1}){1,10}[\\s]*$";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				return true;
			}
			else
			{
				return false;
			}
		default:
			return false;
		}

	
		
	}
	
	
	/**
	 * Attempt to work out what sort of line this is
	 * 
	 * @param line
	 * @return
	 */
	private VFormatLineType getLineType(String line) {

		if(checkLineType(line, VFormatLineType.HEADER_1))
		{
			return VFormatLineType.HEADER_1;
		}
		else if(checkLineType(line, VFormatLineType.DATA))
		{
			return VFormatLineType.DATA;
		}
		else if(checkLineType(line, VFormatLineType.HEADER_3))
		{
			return VFormatLineType.HEADER_3;
		}		
		else
		{
			return VFormatLineType.HEADER_2;
		}
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	@Override
	public String[] getFileExtensions() {
		
		// File extensions for VFormat files can be any one of the combination
		// of datatype, stattype and paramtype codes.
		
		ArrayList<String> fileExtensions = new ArrayList<String>();
		for (VFormatDataType dataType : VFormatDataType.values())
		{
			for(VFormatStatType statType : VFormatStatType.values())
			{
				for(VFormatParameter paramType : VFormatParameter.values())
				{
					fileExtensions.add(dataType.toString()+statType.toString()+paramType.toString());
				}
			}
		}
		
		return fileExtensions.toArray(new String[0]);
	}
	
	@Override
	public TridasProject getProject() {
		TridasProject project = defaults.getProjectWithDefaults();
		ArrayList<TridasObject> oList = new ArrayList<TridasObject>();
		
		
		for(VFormatSeries series: seriesList)
		{
			TridasObject object = series.defaults.getDefaultTridasObject();
			TridasElement element = series.defaults.getDefaultTridasElement();		
			TridasSample sample = series.defaults.getDefaultTridasSample();
			TridasRadius radius = series.defaults.getDefaultTridasRadius();
			TridasMeasurementSeries ms = series.defaults.getDefaultTridasMeasurementSeries();
			TridasValues tvs = series.defaults.getTridasValuesWithDefaults();
			
			tvs.setValues(series.dataValues);
			
			ArrayList<TridasValues> tvsList = new ArrayList<TridasValues>();
			tvsList.add(tvs);
			ms.setValues(tvsList);
			
			ArrayList<TridasMeasurementSeries> msList = new ArrayList<TridasMeasurementSeries>();
			msList.add(ms);
			radius.setMeasurementSeries(msList);
			
			ArrayList<TridasRadius> rList  = new ArrayList<TridasRadius>();
			rList.add(radius);
			sample.setRadiuses(rList);
			
			ArrayList<TridasSample> sList = new ArrayList<TridasSample>();
			sList.add(sample);
			element.setSamples(sList);
			
			ArrayList<TridasElement> eList = new ArrayList<TridasElement>();
			eList.add(element);
			object.setElements(eList);
						
			oList.add(object);
		}
		
		
		project.setObjects(oList);
		
		return project;
	}
		
	private void checkFile(String[] argFileString) throws InvalidDendroFileException
	{
		if (argFileString[0].length() != 80) {
			throw new InvalidDendroFileException(I18n.getText("vformat.headerWrongSize", String
					.valueOf(argFileString[0].length())), 1);
		}
		
		if (!argFileString[0].substring(8, 9).equals(".")) {
			throw new InvalidDendroFileException(I18n.getText("vformat.missingDot"), 1);
		}
		
		if (!getLineType(argFileString[0]).equals(VFormatLineType.HEADER_1)) {
			throw new InvalidDendroFileException(I18n.getText("vformat.headerLineWrong"), 1);
		}
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("vformat.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("vformat.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("vformat.about.shortName");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		defaults = null;
		seriesList.clear();
	}
	
	/**
	 * Class to store the series data
	 * 
	 * @author peterbrewer
	 */
	private static class VFormatSeries {
		public VFormatToTridasDefaults defaults;
		public final ArrayList<TridasValue> dataValues = new ArrayList<TridasValue>();
	}
}
