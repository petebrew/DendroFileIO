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
package org.tridas.io.formats.oxford;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.oxford.OxfordToTridasDefaults.OxDefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class OxfordReader extends AbstractDendroFileReader {

	private OxfordToTridasDefaults defaults;
	private int currentLineNumber = 0;
	private ArrayList<Integer> dataVals = new ArrayList<Integer>();
	private ArrayList<Integer> countVals = new ArrayList<Integer>();

	
	public OxfordReader()
	{
		super(OxfordToTridasDefaults.class);
	}
	
	public OxfordReader(OxfordToTridasDefaults argDefaultFieldsClass) {
		super(OxfordToTridasDefaults.class);
		defaults = argDefaultFieldsClass;
	}

	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public DendroFileFilter getDendroFileFilter() {
		String[] exts = new String[] {"*.*"};	
		return new DendroFileFilter(exts, getShortName());
	}

	@Override
	public String getDescription() {
		return I18n.getText("oxford.about.description");
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { "ddf" };
	}

	@Override
	public String getFullName() {
		return I18n.getText("oxford.about.fullName");
	}

	@Override
	public String getShortName() {
		return I18n.getText("oxford.about.shortName");
	}


	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		defaults = (OxfordToTridasDefaults) argDefaultFields;
		
		checkFile(argFileString);
		
		// Series code
		defaults.getStringDefaultValue(OxDefaultFields.SERIESCODE).setValue(argFileString[0].substring(1, 10).trim());
		
		// Trim trailing apostrophe from description if present
		String desc = argFileString[0].substring(23, argFileString[0].length()-1).trim();
		if (desc.endsWith("'")) desc = desc.substring(0, desc.length()-1);
		defaults.getStringDefaultValue(OxDefaultFields.DESCRIPTION).setValue(desc);
		
		
		String firstYearVal = argFileString[0].substring(12,16).trim();	
		if(!firstYearVal.equals(""))
		{
			try{
				defaults.getSafeIntYearDefaultValue(OxDefaultFields.FIRSTYEAR).setValue(new SafeIntYear(firstYearVal));
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException("First year dodgy", 1);
			}
		}
		
		String lastYearVal = argFileString[0].substring(17,21).trim();
		if(!lastYearVal.equals(""))
		{
			try{
				defaults.getSafeIntYearDefaultValue(OxDefaultFields.LASTYEAR).setValue(new SafeIntYear(lastYearVal));
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException("Last year dodgy", 1);
			}
		}

		Integer ringCount;
		try{
			ringCount = Integer.valueOf(argFileString[1].substring(0,argFileString[1].indexOf(",")).trim());
			defaults.getIntegerDefaultValue(OxDefaultFields.SERIESLENGTH).setValue(ringCount);
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException("Ring count dodgy", 2);
		}
		
		Integer startYear;
		try{
			startYear = Integer.valueOf(argFileString[1].substring(argFileString[1].indexOf(",")+1).trim());
			defaults.getSafeIntYearDefaultValue(OxDefaultFields.STARTYEAR).setValue(new SafeIntYear(startYear));
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException("Start year dodgy", 2);
		}
		
		// Now loop through the data lines
		Boolean inCounts = false;
		String regex = "[^\\d\\s]";
		Pattern p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		for (int i= 2; i< argFileString.length; i++)
		{
			// Grab line and increment line number
			String line = argFileString[i];
			currentLineNumber = i+1;

			// Remove any ASCII control characters
			line = line.replaceAll("\\p{Cntrl}", "");
			
			// Ignore empty lines
			if(line.equals("")) { 
				inCounts = true;
				continue;
			}
			
			// Check to see if there are any characters other than numbers and spaces
			Matcher m1 = p1.matcher(line);
			if (m1.find()) 
			{
				if(dataVals.size() == ringCount)
				{
					// Treat remainder of file as comments and break
					String comments = "";
					for (int j=i; j<argFileString.length; j++)
					{
						comments += argFileString[j].replaceAll("\\p{Cntrl}", "") + " ";
					}
					comments.trim();
					defaults.getStringDefaultValue(OxDefaultFields.COMMENTS).setValue(comments);
					break;
				}
			}
			
			String[] vals = line.split("[\\s]+");
			for(String val : vals)
			{
				if(val.trim().equals("")) continue;

				try{
					Integer v = Integer.parseInt(val);
					if(inCounts)
					{
						countVals.add(v);
					}
					else {
						dataVals.add(v);
					}
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException("Invalid data value found - '"+val+"' from line: "+vals , currentLineNumber);
				}
			}
			
			
		}
		
		// Check length of counts is valid
		if (countVals.size()!=0)
		{
			if(countVals.size()!=dataVals.size())
			{
				countVals = new ArrayList<Integer>();
				addWarning(new ConversionWarning(WarningType.INVALID, "Number of count values inconsistent.  Ignoring them."));
			}
		}
		
		
	}
	
	/**
	 * Check the file is a valid Oxford format file 
	 * 
	 * @param argFileString
	 * @throws InvalidDendroFileException
	 */
	private void checkFile(String[] argFileString) throws InvalidDendroFileException
	{
		if(!argFileString[0].startsWith("'"))
		{
			throw new InvalidDendroFileException(I18n.getText("oxford.missingApostrophe"), 1);
		}
		if((!argFileString[0].substring(11,12).equals("<")) ||
		   (!argFileString[0].substring(21,22).equals(">")) ||
		   (!argFileString[0].substring(16,17).equals("-")) )
		{
			throw new InvalidDendroFileException(I18n.getText("oxford.missingYearRangeBrackets"), 1);
		}
	}

	@Override
	protected void resetReader() {
		defaults = null;
		currentLineNumber = 0;
		dataVals = new ArrayList<Integer>();
		countVals = new ArrayList<Integer>();
	}
	
	private TridasProject getProject()
	{
		TridasProject p = defaults.getProjectWithDefaults();

		if(countVals.size()>0)
		{
			TridasDerivedSeries dser = defaults.getDerivedSeriesWithDefaults();
			TridasValues valgroup = defaults.getDefaultTridasValues();
			
			for (int i=0; i< dataVals.size(); i++)
			{
				Integer ringWidth = dataVals.get(i);
	
				TridasValue value = new TridasValue();
				value.setValue(ringWidth.toString());
				
				try{ 
					Integer count = countVals.get(i);
					value.setCount(count);
				} catch (Exception ex)
				{ }
			
				valgroup.getValues().add(value);
			}
			
			dser.getValues().add(valgroup);
			p.getDerivedSeries().add(dser);
		}
		else
		{
			TridasObject o = defaults.getObjectWithDefaults();
			TridasElement e = defaults.getElementWithDefaults();
			TridasSample s = defaults.getSampleWithDefaults();
			TridasRadius r = defaults.getRadiusWithDefaults(false);
			
			TridasMeasurementSeries ser = defaults.getMeasurementSeriesWithDefaults();
			
			TridasValues valgroup = defaults.getDefaultTridasValues();
			
			for (int i=0; i< dataVals.size(); i++)
			{
				Integer ringWidth = dataVals.get(i);
	
				TridasValue value = new TridasValue();
				value.setValue(ringWidth.toString());			
				valgroup.getValues().add(value);
			}
			
			ser.getValues().add(valgroup);
			
			
			r.getMeasurementSeries().add(ser);
			s.getRadiuses().add(r);
			e.getSamples().add(s);
			o.getElements().add(e);		
			p.getObjects().add(o);
		}
		
				
		return p;
	}
	
	
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
