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
package org.tridas.io.formats.corina;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.corina.CorinaToTridasDefaults.DefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class CorinaReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(CorinaReader.class);
	private CorinaToTridasDefaults defaults = null;
	private int currentLineNumber = -1;
	
	private ArrayList<Integer> dataVals = new ArrayList<Integer>();
	private ArrayList<Integer> countVals = new ArrayList<Integer>();
	private ArrayList<String> parentSeries = new ArrayList<String>();
	
	
	public CorinaReader() {
		super(CorinaToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}


	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("corina.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("corina.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("corina.about.shortName");
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"raw", "rec", "cln", "sum"};
	}
	
	@Override
	protected void resetReader() {
		
		dataVals.clear();

	}


	@Override
	public TridasProject getProject() {
		
		TridasProject project = defaults.getProjectWithDefaults(true);
		
		TridasMeasurementSeries ms = project.getObjects().get(0).getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
		TridasUnit units = new TridasUnit();
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		TridasVariable variable = new TridasVariable();
		variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		
		ArrayList<TridasValues> valuesList = new ArrayList<TridasValues>();
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setUnit(units);
		valuesGroup.setVariable(variable);
	

		
		valuesGroup.setValues(getTridasValueList());
		valuesList.add(valuesGroup);
		
		ms.setValues(valuesList);

		return project;
		
	}

	
	private ArrayList<TridasValue> getTridasValueList()
	{
		ArrayList<TridasValue> tvs = new ArrayList<TridasValue>();
		
		Boolean includeCounts = false;
		
		if(tvs.size()==0) return null;
		
		for (int i=0; i<countVals.size(); i++)
		{
			if(countVals.get(i).compareTo(1)>0) includeCounts=true;
		}
		
		for (int i=0; i<dataVals.size(); i++)
		{
			TridasValue value = new TridasValue();
			value.setValue(dataVals.get(i).toString());
			if(includeCounts)
			{
				value.setCount(countVals.get(i));
			}
			tvs.add(value);
		}
		
		return tvs;
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		log.debug("Parsing: " + argFileString);
		defaults = (CorinaToTridasDefaults) argDefaultFields;
				
		checkFileIsValid(argFileString);
		
		// Loop through lines
		for (int linenum=1; linenum<argFileString.length; linenum++)
		{
			String line = argFileString[linenum];
			currentLineNumber =linenum+1;
			
			// if line is blank, skip it
			if(line.equals("")) continue;
			
			// Last line is user name which is denoted by a ~
			if(line.startsWith("~"))
			{
				defaults.getStringDefaultValue(DefaultFields.USERNAME).setValue(line.substring(2).trim());
				return;
			}
			
			// Split the line into key-value pairs based on ';' delimiter
			if(!line.contains(";")) continue;
			
			for(String tagAndValue : line.split(";"))
			{	
				String key = null;
				String value = null;
				
				try{
				key = tagAndValue.substring(0, tagAndValue.indexOf(" "));
				value = tagAndValue.substring(tagAndValue.indexOf(" "));
				} catch (Exception e)
				{
					continue;
				}
				
				if(key.equalsIgnoreCase("DATA"))
				{
					readData(argFileString, linenum+1);
					linenum = this.getNextLineToRead(argFileString, linenum);
					
				}
				else if (key.equalsIgnoreCase("ELEMENTS"))
				{
					readElements(argFileString, linenum+1);
					linenum = this.getNextLineToRead(argFileString, linenum);
				}
				else if (key.equalsIgnoreCase("WEISERJAHRE"))
				{
					linenum = this.getNextLineToRead(argFileString, linenum);
				}
				else if (key.equalsIgnoreCase("ID"))
				{
					defaults.getStringDefaultValue(DefaultFields.ID).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("NAME"))
				{
					defaults.getStringDefaultValue(DefaultFields.NAME).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("DATING"))
				{
					defaults.getStringDefaultValue(DefaultFields.DATING).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("UNMEAS_PRE"))
				{
					try{
					defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).setValue(Integer.parseInt(value.trim()));
					} catch (NumberFormatException e)
					{
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("corina.invalidUnmeasPre")));
					}
				}
				else if (key.equalsIgnoreCase("UNMEAS_POST"))
				{
					try{
					defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_POST).setValue(Integer.parseInt(value.trim()));
					} catch (NumberFormatException e)
					{
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("corina.invalidUnmeasPost")));
					}
				}
				else if (key.equalsIgnoreCase("FILENAME"))
				{
					defaults.getStringDefaultValue(DefaultFields.FILENAME).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("TYPE"))
				{
					defaults.getStringDefaultValue(DefaultFields.TYPE).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("SPECIES"))
				{
					defaults.getStringDefaultValue(DefaultFields.SPECIES).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("FORMAT"))
				{
					defaults.getStringDefaultValue(DefaultFields.FORMAT).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("PITH"))
				{
					defaults.getStringDefaultValue(DefaultFields.PITH).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("TERMINAL"))
				{
					defaults.getStringDefaultValue(DefaultFields.TERMINAL).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("CONTINUOUS"))
				{
					defaults.getStringDefaultValue(DefaultFields.CONTINUOUS).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("QUALITY"))
				{
					defaults.getStringDefaultValue(DefaultFields.QUALITY).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("RECONCILED"))
				{
					defaults.getStringDefaultValue(DefaultFields.RECONCILED).setValue(value.trim());
				}
			}	
		}	
	}

	private Integer getNextLineToRead(String[] argFileString, Integer currentIndex)
	{
		for (int i=currentIndex; i<argFileString.length; i++)
		{
			if(argFileString[i].startsWith(";") || argFileString[i].startsWith("~"))
			{
				return i;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void readData(String[] argFileString, Integer dataStartIndex) throws InvalidDendroFileException
	{
		defaults.getSafeIntYearDefaultValue(DefaultFields.START_YEAR).setValue(
				new SafeIntYear(argFileString[dataStartIndex].substring(0, 5).trim()));

		
		for (int i=dataStartIndex; i+1<argFileString.length; i=i+2)
		{
			// Skip blank lines
			if(argFileString[i].equals("")) continue;
			
			// Grab data line (minus the year field)
			String dataLine = argFileString[i].substring(5);
			
			// Grab count line (minus starting spaces)
			String countLine = argFileString[i+1].substring(5);
			
			// Loop through values in dataLine
			for (int charpos=0; charpos+6<=dataLine.length(); charpos=charpos+6)
			{
				String strval = dataLine.substring(charpos, charpos+6).trim();
				
				// Skip blank values
				if (strval.equals("")) continue;
				
				// Parse into integer
				try{
					Integer intval = Integer.parseInt(strval);
					
					// Check for stop marker
					if(intval.equals(9990)) return;
					
					// Add to array
					dataVals.add(intval);
				} catch(NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
				}
			}
			
			String[] countArr = countLine.split("\\[");
			for(String strcount : countArr)
			{
				if(strcount.trim().length()==0) continue;
				
				strcount = strcount.substring(0, strcount.indexOf("]"));
				
				// Parse into integer
				try{
					Integer intval = Integer.parseInt(strcount);
					countVals.add(intval);
				} catch(NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
				}
			}
			
			
		}
		
	}
	
	
	private void readElements(String[] argFileString, Integer dataStartIndex)
	{
		for (int i=dataStartIndex; i<argFileString.length; i++)
		{
			// Skip blank lines
			if(argFileString[i].equals("")) continue;
				
			String line = argFileString[i];
			
			// If line has a ; in it then return
			if (line.contains(";")) return;
			
			// Add to list
			parentSeries.add(line);
			
		}
		
	}
	
	private void readWJ(String[] argFileString, Integer dataStartIndex) throws InvalidDendroFileException
	{
		for (int i=dataStartIndex; i<argFileString.length; i++)
		{
			// Skip blank lines
			if(argFileString[i].equals("")) continue;
		
			// Grab data line (minus the year field)
			String wjLine = argFileString[i].substring(5);
		
			// Loop through values in wjLine
			for (int charpos=0; charpos+6<=wjLine.length(); charpos=charpos+6)
			{
				String strval = wjLine.substring(charpos, charpos+6).trim();
				
				// Skip blank values
				if (strval.equals("")) continue;
				
				// Parse into integer
				try{
					Integer intval = Integer.parseInt(strval);
					
					// Check for stop marker
					if(intval.equals(9990)) return;
					
					// Add to array
					dataVals.add(intval);
				} catch(NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
				}
			}
			
		}
		
		
	}
	
	
	/** 
	 * @param argFileString
	 * @throws InvalidDendroFileException
	 */
	private void checkFileIsValid(String[] argFileString) throws InvalidDendroFileException
	{
	
	}


}
