/*******************************************************************************
 * Copyright 2011 Daniel Murphy and Peter Brewer
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
package org.tridas.io.formats.dendrodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.formats.dendrodb.DendroDBToTridasDefaults.DDBDefaultFields;
import org.tridas.io.formats.dendrodb.DendroDBToTridasDefaults.DendroDBParameter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class DendroDBReader extends AbstractDendroFileReader {

	Integer currentLineNumber=0;
	DendroDBToTridasDefaults defaults;
	ArrayList<DendroDBSeries> seriesList = new ArrayList<DendroDBSeries>();
	
	public DendroDBReader()
	{
		super(DendroDBToTridasDefaults.class);
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
		String[] exts = new String[] {"dat"};	
		return new DendroFileFilter(exts, getShortName());
	}

	@Override
	public String getDescription() {
		return I18n.getText("dendrodb.about.description");
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { "dat" };
	}

	@Override
	public String getFullName() {
		return I18n.getText("dendrodb.about.fullName");
	}

	@Override
	public String getShortName() {
		return I18n.getText("dendrodb.about.shortName");
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

	@SuppressWarnings("unchecked")
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		defaults = (DendroDBToTridasDefaults) argDefaultFields;
		checkFile(argFileString);
		
		defaults.getStringDefaultValue(DDBDefaultFields.SITE).setValue(argFileString[1].substring(argFileString[1].indexOf(":")+2));
		defaults.getStringDefaultValue(DDBDefaultFields.CONTACT).setValue(argFileString[2].substring(argFileString[2].indexOf(":")+2));
		defaults.getStringDefaultValue(DDBDefaultFields.SPECIES).setValue(argFileString[3].substring(argFileString[3].indexOf(":")+2));
		try{
			defaults.getDoubleDefaultValue(DDBDefaultFields.LATITUDE).setValue(
				Double.parseDouble(argFileString[5].substring(argFileString[5].indexOf(":")+2)));
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException(I18n.getText("fileio.unableToParse", "Latitude"), 6);
		}
		try{
			defaults.getDoubleDefaultValue(DDBDefaultFields.LONGITUDE).setValue(Double.parseDouble(argFileString[6].substring(argFileString[6].indexOf(":")+2)));
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException(I18n.getText("fileio.unableToParse", "Longitude"), 7);
		}
		try{
			defaults.getDoubleDefaultValue(DDBDefaultFields.ELEVATION).setValue(Double.parseDouble(argFileString[7].substring(argFileString[7].indexOf(":")+2)));
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException(I18n.getText("fileio.unableToParse", "Elevation"), 8);
		}

		
		GenericDefaultValue<DendroDBParameter> dateTypeField = (GenericDefaultValue<DendroDBParameter>) defaults
									.getDefaultValue(DDBDefaultFields.PARAMETER);
		dateTypeField.setValue(DendroDBParameter.fromCode(argFileString[4].substring(argFileString[4].indexOf(":")+2)));
		
		String lastTree = "";
		String lastCore = "";
		DendroDBSeries thisSeries =  new DendroDBSeries();
		DendroDBToTridasDefaults thisDefaults ;
		for (int i=9; i<argFileString.length; i++)
		{	
			String[] parts = argFileString[i].split(" ");
			
			if(parts.length!=4)
			{
				throw new InvalidDendroFileException(I18n.getText("dendrodb.unexpectedNumberOfParts"), i+1);
			}
			
			String thisTree = parts[0];
			String thisCore = parts[1];
			String thisYear = parts[2];
			String thisValue = parts[3];
			
			if(lastTree.equals(""))
			{
				lastTree = thisTree;
				lastCore = thisCore;
				
				SafeIntYear startYear = null;
				try{
					startYear = new SafeIntYear(thisYear);
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.unableToParse", "StartYear"), i+1);
				}
				
				thisDefaults = (DendroDBToTridasDefaults) defaults.clone();
				thisDefaults.getSafeIntYearDefaultValue(DDBDefaultFields.STARTYEAR).setValue(startYear);
				thisSeries.defaults = thisDefaults;
			}
			
			if((!lastTree.equals(thisTree) || !lastCore.equals(thisCore)))
			{

				
				seriesList.add(thisSeries);
				
				thisDefaults = (DendroDBToTridasDefaults) defaults.clone();
				thisDefaults.getStringDefaultValue(DDBDefaultFields.TREE).setValue(thisTree);
				thisDefaults.getStringDefaultValue(DDBDefaultFields.CORE).setValue(thisCore);
				
				SafeIntYear startYear = null;
				try{
					startYear = new SafeIntYear(thisYear);
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.unableToParse", "StartYear"), i+1);
				}
				
				thisDefaults.getSafeIntYearDefaultValue(DDBDefaultFields.STARTYEAR).setValue(startYear);
				
				thisSeries = new DendroDBSeries();
				thisSeries.defaults = thisDefaults;
			}
			
			try{
				thisSeries.dataVals.add(Integer.parseInt(thisValue));
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException(I18n.getText("dendrodb.invalidDataValue"), i+1);
			}
			
			lastTree = thisTree;
			lastCore = thisCore;
			
		}
		
		if(thisSeries!=null)
		{
			seriesList.add(thisSeries);
		}

	}
	
	private void checkFile(String[] argFileString) throws InvalidDendroFileException
	{
		if(!argFileString[1].startsWith("Site:"))
		{
			throw new InvalidDendroFileException(I18n.getText("dendrodb.expectingDifferentLineStart", "Site"), 2);
		}
		if(!argFileString[2].startsWith("Contact:"))
		{
			throw new InvalidDendroFileException(I18n.getText("dendrodb.expectingDifferentLineStart", "Contact"), 3);
		}
		if(!argFileString[3].startsWith("Species:"))
		{
			throw new InvalidDendroFileException(I18n.getText("dendrodb.expectingDifferentLineStart", "Species"), 4);
		}
		if(!argFileString[4].startsWith("Parameter:"))
		{
			throw new InvalidDendroFileException(I18n.getText("dendrodb.expectingDifferentLineStart", "Parameter"), 5);
		}
		if(!argFileString[5].startsWith("Latitude:"))
		{
			throw new InvalidDendroFileException(I18n.getText("dendrodb.expectingDifferentLineStart", "Latitude"), 6);
		}
		if(!argFileString[6].startsWith("Longitude:"))
		{
			throw new InvalidDendroFileException(I18n.getText("dendrodb.expectingDifferentLineStart", "Longitude"), 7);
		}
		if(!argFileString[7].startsWith("Elevation:"))
		{
			throw new InvalidDendroFileException(I18n.getText("dendrodb.expectingDifferentLineStart", "Elevation"), 8);
		}
	}
	
	public TridasProject getProject()
	{
		TridasProject p = defaults.getDefaultTridasProject();
		TridasObject o = defaults.getDefaultTridasObject();

		for(DendroDBSeries series : seriesList)
		{
			TridasElement e = series.defaults.getDefaultTridasElement();
			TridasSample s = series.defaults.getDefaultTridasSample();
			TridasRadius r = series.defaults.getDefaultTridasRadius();
			TridasMeasurementSeries ms = series.defaults.getDefaultTridasMeasurementSeries();
			
			TridasValues valuesGroup = series.defaults.getDefaultTridasValues();
			
			for (Integer val : series.dataVals)
			{
				TridasValue value = new TridasValue();
				value.setValue(val.toString());
				valuesGroup.getValues().add(value);
			}
			
			ms.getValues().add(valuesGroup);			
			r.getMeasurementSeries().add(ms);
			s.getRadiuses().add(r);
			e.getSamples().add(s);
			o.getElements().add(e);
		}
				
		p.getObjects().add(o);
		
		
		return p;
	}

	@Override
	protected void resetReader() {
		defaults =null;
		currentLineNumber = 0;

	}

	private static class DendroDBSeries {
		public DendroDBToTridasDefaults defaults;
		public final ArrayList<Integer> dataVals = new ArrayList<Integer>();
	}
	
}
