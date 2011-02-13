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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.tucsoncompact.TucsonCompactToTridasDefaults.DefaultFields;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class TucsonCompactReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(TucsonCompactReader.class);
	private ArrayList<TucsonCompactSeries> seriesList = new ArrayList<TucsonCompactSeries>();
	private TucsonCompactToTridasDefaults defaults = null;
	private int currentLineNumber = -1;

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
		
		seriesList.clear();

	}

	private TridasProject getProject() {
		
		TridasProject project = defaults.getProjectWithDefaults(false);
		
		// Loop through each series and add to our project
		for(TucsonCompactSeries ser : seriesList)
		{		
			TridasObject obj = ser.defaults.getObjectWithDefaults(true);
			TridasMeasurementSeries ms = obj.getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
			TridasValues valuesGroup = ser.defaults.getDefaultTridasValues();
			valuesGroup.setValues(ser.dataVals);
			ArrayList<TridasValues> valuesList = new ArrayList<TridasValues>();
			valuesList.add(valuesGroup);
			ms.setValues(valuesList);
			project.getObjects().add(obj);
		}
		return project;
		
	}


	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		log.debug("Parsing: " + argFileString);
		defaults = (TucsonCompactToTridasDefaults) argDefaultFields;
		
		// Remove any blank lines 
		ArrayList<String> lines2 = new ArrayList<String>();
		for (String line: argFileString)
		{
			if(!line.equals("")) {lines2.add(line);}
		}
		
		// Check for lines at beginning of file that do not terminate with a ~
		// These will be treated as project comments
		String comments = "";
		ArrayList<String> lines = (ArrayList<String>) lines2.clone();
		for(int linenum = 0; linenum < lines2.size(); linenum++)
		{
			String line = lines2.get(linenum);
			if(line.endsWith("~")) break;
			lines.remove(linenum);
			comments = comments + line + "; "; 	
		}
		// Remove duplicate white space chars from comment
		defaults.getStringDefaultValue(DefaultFields.PROJECT_COMMENT).setValue(comments.replaceAll("\\s+", " "));
		argFileString = lines.toArray(new String[0]);
		
		// Loop through file compiling data in series chunks
		TucsonCompactSeries series = new TucsonCompactSeries((TucsonCompactToTridasDefaults)defaults.clone());
		currentLineNumber = 0;
		for (String line: argFileString)
		{
			currentLineNumber ++;
			if(line.endsWith("~"))
			{
				// Header line so start a new series and add it to the list
				series = new TucsonCompactSeries((TucsonCompactToTridasDefaults)defaults.clone());
				this.seriesList.add(series);
			}
			else
			{
				// Data line so check there are no chars other than spaces and digits
				String regex = "[^\\d ]";
				Pattern p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher m1 = p1.matcher(line);
				if (m1.find()) {
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), currentLineNumber);
				}
			}
			
			// Add line to array 
			series.lines.add(line);
		}
		
		
		// Loop through each series parsing info as we go
		for(TucsonCompactSeries ser : seriesList)
		{
			parseSeries(ser);
		}
	}
	
	/**
	 * Check that the file contains just one double on each line
	 * 
	 * @param argFileString
	 * @throws InvalidDendroFileException
	 */
	private void parseSeries(TucsonCompactSeries series) throws InvalidDendroFileException
	{		
		// Check first line has fortran formatting string
		String[] argFileString = series.lines.toArray(new String[0]);

		String fortranFormat = null;
		Integer cols = null;
		Integer chars = null;
		Integer divFactor = null;
		
		try{
			fortranFormat = argFileString[0].substring(argFileString[0].indexOf("(")-2, argFileString[0].indexOf(")"));
			cols = Integer.parseInt(fortranFormat.substring(3, fortranFormat.indexOf("F")));
			chars = Integer.parseInt(fortranFormat.substring(fortranFormat.indexOf("F")+1, fortranFormat.indexOf(".")));
			divFactor = Integer.parseInt(fortranFormat.substring(0, 2));
			series.defaults.getIntegerDefaultValue(DefaultFields.DIVFACTOR).setValue(divFactor);
		} catch (Exception e)
		{
			throw new InvalidDendroFileException(I18n.getText("tucsoncompact.invalidFortranFormatter"), this.currentLineNumber);
		}
		
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
					
		// Set series Title	
		series.defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(argFileString[0].substring(21,68).trim());
				
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
					throw new InvalidDendroFileException("Failed to convert data value", currentLineNumber);
				}		
				
				// Convert integer from file to double by using the divFactor
				Double dblval = intval*Math.pow(10, divFactor);
				
				// Convert and set the units to appropriate value based on divFactor
				if(divFactor.compareTo(-4)<=0)
				{
					val.setValue(Math.round(UnitUtils.convertDouble(NormalTridasUnit.MILLIMETRES, NormalTridasUnit.MICROMETRES, dblval))+"");
				}
				else 
				{
					val.setValue(Math.round(UnitUtils.convertDouble(NormalTridasUnit.MILLIMETRES, NormalTridasUnit.HUNDREDTH_MM, dblval))+"");
				}
				
				series.dataVals.add(val);
			}
			
		}	
		
		// Set ring count		
		if(series.dataVals!=null)
		{
			series.defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(series.dataVals.size());
		}
		
		// Set start year
		try{
			Integer startYear = Integer.parseInt(argFileString[0].substring(10, 18).trim());
			if(startYear!=null)
			{
				series.defaults.getIntegerDefaultValue(DefaultFields.START_YEAR).setValue(startYear);
			}
		} catch (Exception e){}
				
		// Check that the ring count matches what is in the header
		try{
			Integer ringcount = Integer.parseInt(argFileString[0].substring(0, 8).trim());
			if(ringcount!=series.dataVals.size())
			{
				addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("nottingham.valuesAndRingCountMismatch")));
			}
		} catch (Exception e){}
	}

	/**
	 * Class to store the measurement series data
	 * 
	 * @author peterbrewer
	 */
	private static class TucsonCompactSeries{
		public TucsonCompactToTridasDefaults defaults;
		public final ArrayList<TridasValue> dataVals = new ArrayList<TridasValue>();
		public ArrayList<String> lines = new ArrayList<String>();
		
		private TucsonCompactSeries(TucsonCompactToTridasDefaults df)
		{
			defaults =df;
		}
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"rwm"};
		
		return new DendroFileFilter(exts, getShortName());

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
