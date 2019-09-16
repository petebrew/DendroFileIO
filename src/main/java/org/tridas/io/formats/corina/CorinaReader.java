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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.formats.corina.CorinaToTridasDefaults.DefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.SeriesLink.XLink;
import org.tridas.schema.SeriesLinks;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class CorinaReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(CorinaReader.class);
	private CorinaToTridasDefaults defaults = null;
	private int currentLineNumber = -1;
	
	private ArrayList<Integer> dataVals = new ArrayList<Integer>();
	private ArrayList<Integer> countVals = new ArrayList<Integer>();
	private ArrayList<String> parentSeriesLinks = new ArrayList<String>();
	private ArrayList<TridasIdentifier> linkIds = new ArrayList<TridasIdentifier>();
	private TridasProject parentProject = new TridasProject();
	private Boolean isDerivedSeries = false;
	protected Boolean loadRecursively = false;
	
	
	public CorinaReader() {
		super(CorinaToTridasDefaults.class, new CorinaFormat());
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
	protected void resetReader() {
		
		dataVals.clear();

	}


	/*@Override
	public TridasProject getProject() {
		
		TridasProject project = defaults.getProjectWithDefaults(true);
		
		TridasMeasurementSeries ms = project.getObjects().get(0).getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
		TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
		valuesGroup.setValues(getTridasValueList());
		
		ArrayList<TridasValues> valuesList = new ArrayList<TridasValues>();
		
		valuesList.add(valuesGroup);
		ms.setValues(valuesList);

		return project;
		
	}*/
	
	private TridasProject getProject() {

		// Create entities
		TridasProject p = defaults.getProjectWithDefaults();
		TridasObject o = defaults.getObjectWithDefaults();
		TridasElement e = defaults.getElementWithDefaults();
		TridasSample s = defaults.getSampleWithDefaults();
		
		// Compile TridasValues array
		TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
		valuesGroup.setValues(getTridasValueList());
		ArrayList<TridasValues> vlist = new ArrayList<TridasValues>();
		vlist.add(valuesGroup);
		
		if(isDerivedSeries)
		{
			TridasDerivedSeries dseries = defaults.getDefaultTridasDerivedSeries();
			dseries.setValues(vlist);
			
			// Set Link series
			SeriesLinks slinks = new SeriesLinks();
			ArrayList<SeriesLink> seriesList = new ArrayList<SeriesLink>();
			for(String parent : parentSeriesLinks)
			{
				SeriesLink series = new SeriesLink();
				XLink linkvalue = new XLink();
				linkvalue.setHref(parent);
				series.setXLink(linkvalue);
				seriesList.add(series);
			}
			
			for(TridasIdentifier parent : this.linkIds)
			{
				SeriesLink series = new SeriesLink();
				series.setIdentifier(parent);
				seriesList.add(series);
			}
			
			slinks.setSeries(seriesList);
			dseries.setLinkSeries(slinks);
			
			ArrayList<TridasDerivedSeries> dslist = new ArrayList<TridasDerivedSeries>();
			dslist.add(dseries);
			p.setDerivedSeries(dslist);
		}
		else
		{
			TridasRadius r = defaults.getRadiusWithDefaults(false);
					
			// Now build up our measurementSeries
			TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();

			// Compile project
			series.setValues(vlist);
			
			ArrayList<TridasMeasurementSeries> seriesList = new ArrayList<TridasMeasurementSeries>();
			seriesList.add(series);
			r.setMeasurementSeries(seriesList);
		
			ArrayList<TridasRadius> rList = new ArrayList<TridasRadius>();
			rList.add(r);
			s.setRadiuses(rList);
			
			ArrayList<TridasSample> sList = new ArrayList<TridasSample>();
			sList.add(s);
			e.setSamples(sList);
			
			ArrayList<TridasElement> eList = new ArrayList<TridasElement>();
			eList.add(e);
			o.setElements(eList);
			
			ArrayList<TridasObject> oList = new ArrayList<TridasObject>();
			oList.add(o);		
			p.setObjects(oList);
		}
				
		return p;
		
	}

	
	private ArrayList<TridasValue> getTridasValueList()
	{
		ArrayList<TridasValue> tvs = new ArrayList<TridasValue>();
		
		for (int i=0; i<countVals.size(); i++)
		{
			if(countVals.get(i).compareTo(1)>0) isDerivedSeries=true;
		}
		
		if(countVals.size()!=dataVals.size())
		{
			log.warn("Count and data vals are not the same size: Count="+ countVals.size()+ " Data="+ dataVals.size());
			return null;
		}
		
		for (int i=0; i<dataVals.size(); i++)
		{
			TridasValue value = new TridasValue();
			value.setValue(dataVals.get(i).toString());
			if(isDerivedSeries)
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
		
		log.debug("Parsing: " + argFileString[3]);
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
				if(line.length()>2)
				{
					defaults.getStringDefaultValue(DefaultFields.USERNAME).setValue(line.substring(2).trim());
				}
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
	
	private void readData(String[] argFileString, Integer dataStartIndex) throws InvalidDendroFileException
	{
		defaults.getSafeIntYearDefaultValue(DefaultFields.START_YEAR).setValue(
				new SafeIntYear(argFileString[dataStartIndex].substring(0, 5).trim()));

		Boolean eolMarker = false;
		
		for (int i=dataStartIndex; i+1<=argFileString.length; i=i+2)
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
					if(intval.equals(9990)) {
						eolMarker = true;
						break;
					}
					
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
					log.warn("invalid count number");
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
				}
			}
			
			// Reached end of data
			if(eolMarker) {
				// Remove the last count as it is just refers to the end marker
				countVals.remove(countVals.size()-1);
				return;
			}
			
		}
		
	}
	
	
	private void readElements(String[] argFileString, Integer dataStartIndex)
	{
		ArrayList<String> elementsHandled = new ArrayList<String>();
	
		for (int i=dataStartIndex; i<argFileString.length; i++)
		{
			// Skip blank lines
			if(argFileString[i].equals("")) continue;
			
			log.debug("Reading element from line "+i);
				
			String line = argFileString[i];
			
			if(elementsHandled.contains(line))
			{
				this.addWarning(new ConversionWarning(WarningType.INFORMATION, "The file '"+line+"' has been included more than once in this chronology.  Duplicate entries will be ignored."));
				continue;
			}
			else
			{
				elementsHandled.add(line);
			}
			
			// If line has a ; in it then return
			if (line.contains(";")) return;
			
			// If we're not loading recursively then just add link to parent
			// element and move on
			if(!loadRecursively)
			{
				parentSeriesLinks.add(line);
				continue;
			}

			// Try and load any files that are referred to
			File f = new File(line);
			if(!f.exists())
			{
				// File does not exist so just warn and link to it
				this.addWarning(new ConversionWarning(WarningType.INFORMATION, "The file '"+line+"' cannot be found and has therefore been excluded."));
				parentSeriesLinks.add(line);
			}
			else
			{
				// File exists so go ahead and load
				
				// Create a new converter
				CorinaReader reader = new CorinaReader();
				
				// Don't recurse any further though as life gets too complicated
				reader.loadRecursively = false;
				
				// Parse the legacy data file
				try {
					// TridasEntitiesFromDefaults def = new TridasEntitiesFromDefaults();
					reader.loadFile(line);
				} catch (IOException e) {
					// Standard IO Exception
					log.info(e.getLocalizedMessage());
					this.addWarning(new ConversionWarning(WarningType.INFORMATION, "The file '"+line+"' cannot be found and has therefore been excluded."));
					parentSeriesLinks.add(line);
					continue;
				} catch (InvalidDendroFileException e) {
					// Fatal error interpreting file
					log.info(e.getLocalizedMessage());
					this.addWarning(new ConversionWarning(WarningType.INFORMATION, "The file '"+line+"' is not a valid Corina file so has been excluded."));
					parentSeriesLinks.add(line);
					continue;
				}
				
				TridasProject currentProject =reader.getProject();
				if(currentProject.isSetDerivedSeries()) {
					log.debug("  - Derived series count: "+currentProject.getDerivedSeries().size());
				}
				else
				{
					log.debug("  - Derived series count: 0");
				}
				
				if(currentProject.isSetDerivedSeries())
				{
					
					if(currentProject.getDerivedSeries().size()>1)
					{
						this.addWarning(new ConversionWarning(WarningType.INFORMATION, "The file '"+line+"' contains more than one chronology which shouldn't be possible.  Excluding."));
						parentSeriesLinks.add(line);
					}
					else
					{
						this.linkIds.add(currentProject.getDerivedSeries().get(0).getIdentifier());
						
						if(parentProject==null)
						{
							parentProject = currentProject;
						}
						else
						{
							parentProject.getObjects().addAll(currentProject.getObjects());
							parentProject.getDerivedSeries().addAll(currentProject.getDerivedSeries());
						}
					}
				}
				

				ArrayList<TridasMeasurementSeries> series = TridasUtils.getMeasurementSeriesFromTridasProject(currentProject);

				log.debug("  - Measurement series count: "+series.size());

				if((series==null || series.size()==0))
				{
					if(!currentProject.isSetDerivedSeries())
					{
						this.addWarning(new ConversionWarning(WarningType.INFORMATION, "The file '"+line+"' does not include any series so it is being excluded."));
						parentSeriesLinks.add(line);
						continue;
					}
				}
				else if (series.size()>1)
				{
					this.addWarning(new ConversionWarning(WarningType.INFORMATION, "The file '"+line+"' contains more than one series which shouldn't be possible.  Excluding."));
					parentSeriesLinks.add(line);
					continue;
				}
				else
				{
					this.linkIds.add(series.get(0).getIdentifier());
					
					if(parentProject==null)
					{
						parentProject = currentProject;
					}
					else
					{
						parentProject.getObjects().addAll(currentProject.getObjects());
						parentProject.getDerivedSeries().addAll(currentProject.getDerivedSeries());
					}
											
					continue;
				}
			}
		}
		
	}
	
	@SuppressWarnings("unused")
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
		if(argFileString.length<3){
			throw new InvalidDendroFileException("File too short to be a valid Corina file", argFileString.length);
		}
		/*if(!argFileString[2].startsWith(";"))
		{
			throw new InvalidDendroFileException("Line 3 should begin with a ;", 3);
		}*/
		if(!argFileString[argFileString.length-1].startsWith("~"))
		{
			throw new InvalidDendroFileException("Last line is missing author tag", argFileString.length);
		}
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"*.*"};
		
		return new DendroFileFilter(exts, getShortName());

	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		
		ArrayList<TridasProject> list = new ArrayList<TridasProject>();
		list.add(this.getProject());	
		
		if(parentProject!=null)
		{
			list.add(this.parentProject);
		}
		
		ArrayList<TridasProject> list2 = TridasUtils.consolidateProjects(list, false);
		
		return list2.toArray(new TridasProject[0]);
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
