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
package org.tridas.io.formats.tucsoncompact;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.tucsoncompact.TucsonCompactToTridasDefaults.DefaultFields;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class TucsonCompactReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(TucsonCompactReader.class);
	private TucsonCompactToTridasDefaults defaults = null;
	private int currentLineNumber = -1;
	
	Integer cols = null;
	Integer chars = null;
	Integer divFactor = null;
	
	private ArrayList<TridasValue> dataVals = new ArrayList<TridasValue>();

	public TucsonCompactReader() {
		super(TucsonCompactToTridasDefaults.class);
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
		return I18n.getText("tucsoncompact.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tucsoncompact.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tucsoncompact.about.shortName");
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"txt"};
	}
	
	@Override
	protected void resetReader() {
		
		dataVals.clear();

	}


	@Override
	public TridasProject getProject() {
		
		TridasProject project = defaults.getProjectWithDefaults(true);
		
		TridasMeasurementSeries ms = project.getObjects().get(0).getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
		TridasValues valuesGroup = defaults.getDefaultTridasValues();
		valuesGroup.setValues(dataVals);
		ArrayList<TridasValues> valuesList = new ArrayList<TridasValues>();
		valuesList.add(valuesGroup);
		ms.setValues(valuesList);

		return project;
		
	}


	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		log.debug("Parsing: " + argFileString);
		defaults = (TucsonCompactToTridasDefaults) argDefaultFields;
		
		// Remove any blank lines 
		ArrayList<String> lines = new ArrayList<String>();
		for (String line: argFileString)
		{
			if(!line.equals("")) {lines.add(line);}
		}
		argFileString = lines.toArray(new String[0]);
		
		checkFileIsValid(argFileString);
		
		// Set ring count		
		if(dataVals!=null)
		{
			defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(dataVals.size());
		}
		
		// Set start year
		try{
			Integer startYear = Integer.parseInt(argFileString[0].substring(10, 18).trim());
			if(startYear!=null)
			{
				defaults.getIntegerDefaultValue(DefaultFields.START_YEAR).setValue(startYear);
			}
		} catch (Exception e){}
		
		// Set series Title	
		defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(argFileString[0].substring(21,68).trim());
				
		// Copy each value into the data array
		for (int linenum=1; linenum<argFileString.length; linenum++)
		{
			String line = argFileString[linenum];
			
			for (int charpos=0; charpos+chars<=line.length(); charpos = charpos+chars)
			{
				TridasValue val = new TridasValue();
				String strval = line.substring(charpos, charpos+chars).trim();
				Integer intval;
				try 
				{
					intval = Integer.parseInt(strval);
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException("Failed to convert data value");
				}		
				
				// Convert integer from file to double by using the divFactor
				Double dblval = intval*Math.pow(10, this.divFactor);
				
				// Convert and set the units to appropriate value based on divFactor
				if(divFactor.compareTo(-4)<=0)
				{
					val.setValue(Math.round(UnitUtils.convertDouble(NormalTridasUnit.MILLIMETRES, NormalTridasUnit.MICROMETRES, dblval))+"");
				}
				else 
				{
					val.setValue(Math.round(UnitUtils.convertDouble(NormalTridasUnit.MILLIMETRES, NormalTridasUnit.HUNDREDTH_MM, dblval))+"");
				}
				
				dataVals.add(val);
			}
			
		}	
		
		// Check that the ring count matches what is in the header
		try{
			Integer ringcount = Integer.parseInt(argFileString[0].substring(0, 8).trim());
			if(ringcount!=dataVals.size())
			{
				addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("nottingham.valuesAndRingCountMismatch")));
			}
		} catch (Exception e){}


	}

	private Double convertDataVal(Integer val)
	{
		return Math.pow(val, this.divFactor);
	}
	
	/**
	 * Check that the file contains just one double on each line
	 * 
	 * @param argFileString
	 * @throws InvalidDendroFileException
	 */
	private void checkFileIsValid(String[] argFileString) throws InvalidDendroFileException
	{		
		// Check first line has fortran formatting string
		String fortranFormat = null;
		try{
			fortranFormat = argFileString[0].substring(argFileString[0].indexOf("(")-2, argFileString[0].indexOf(")"));
		cols = Integer.parseInt(fortranFormat.substring(3, fortranFormat.indexOf("F")));
		chars = Integer.parseInt(fortranFormat.substring(fortranFormat.indexOf("F")+1, fortranFormat.indexOf(".")));
		divFactor = Integer.parseInt(fortranFormat.substring(0, 2));
		defaults.getIntegerDefaultValue(DefaultFields.DIVFACTOR).setValue(divFactor);
		} catch (Exception e)
		{
			throw new InvalidDendroFileException(I18n.getText("tucsoncompact.invalidFortranFormatter"));
		}
		
		// Check that the divFactor is 0
		/*if (divFactor!=0)
		{
			throw new InvalidDendroFileException(I18n.getText("tucsoncompact.invalidFortranFormatter"));
		}*/
		
		// Check first line is terminated with a ~
		if(!argFileString[0].substring(argFileString[0].length()-1).equals("~"))
		{
			throw new InvalidDendroFileException(I18n.getText("tucsoncompact.missingTilde"));
		}
				
		// Check there are two '=' signs at the expected positions
		if((argFileString[0].indexOf("=")!=8) || (argFileString[0].indexOf("=", 9)!=18))
		{
			throw new InvalidDendroFileException(I18n.getText("tucsoncompact.missingEqualsSigns"));
		}
		
		for (int linenum=1; linenum<argFileString.length; linenum++)
		{
			String line = argFileString[linenum];
			
			// Check there are no chars other than spaces and digits
			String regex = "[^\\d ]";
			Pattern p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m1 = p1.matcher(line);
			if (m1.find()) {
				if(!line.endsWith("~"))
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"));
				}
			}
		}		
	}


}
