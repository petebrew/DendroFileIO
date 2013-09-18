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
package org.tridas.io.formats.belfastapple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.util.DateUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class BelfastAppleReader extends AbstractDendroFileReader {
	
	private static final Logger log = LoggerFactory.getLogger(BelfastAppleReader.class);
	// defaults given by user
	private BelfastAppleToTridasDefaults defaults = null;
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	String objectname = null;
	String samplename = null;
	Integer ringcount = 0;
	public BelfastAppleReader() {
		super(BelfastAppleToTridasDefaults.class, new BelfastAppleFormat());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		defaults = (BelfastAppleToTridasDefaults) argDefaultFields;
		// Extract 'metadata' ;-)
		
		// Check for any white space lines
		for(int i=0; i<argFileString.length; i++)
		{
			if(argFileString[i].trim().length()==0)
			{
				throw new InvalidDendroFileException(I18n.getText("belfastapple.blankLine"), i+1);
			}
		}
		
		objectname = argFileString[0].trim();
		samplename = argFileString[1].trim();
		
		// Read the ring count from line 3
		try{
			ringcount = Integer.parseInt(argFileString[2].trim());
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException(I18n.getText("belfastapple.ringcountinvalid"), 3);
		}
		
		// Extract data
		ArrayList<TridasValue> ringWidthValues = new ArrayList<TridasValue>();
		for (int i = 3; i < argFileString.length - 1; i++) {
			TridasValue v = new TridasValue();

			try {
				Integer.valueOf(argFileString[i].trim());
			} catch (NumberFormatException e) {
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i+1);
			}
			
			v.setValue(argFileString[i].trim());
			ringWidthValues.add(v);
			log.debug("value = " + String.valueOf(argFileString[i]));
		}
		
		// Get last line which contains comments
		String comments = argFileString[argFileString.length-1].trim();
		if(comments.startsWith("COMMENT - ")) comments = comments.substring(9).trim();
		
		// Now build up our measurementSeries
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		TridasUnit units = new TridasUnit();
		
		// Set units to 1/100th mm. Is this always the case?
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		
		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(UUID.randomUUID().toString());
		seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		
		// Add values to nested value(s) tags
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setValues(ringWidthValues);
		valuesGroup.setUnit(units);
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) defaults
				.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
		valuesGroupList.add(valuesGroup);
		
		// Add all the data to the series
		series.setValues(valuesGroupList);
		series.setIdentifier(seriesId);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime());
		series.setComments(comments);
		
		// Add series to our list
		mseriesList.add(series);
		
	}
	

	
	private TridasProject getProject() {
		TridasProject project = null;
		
		try {
			project = defaults.getProjectWithDefaults(true);
			TridasObject o = project.getObjects().get(0);
			
			// Override object name if found in file
			if (objectname != null) {
				project.getObjects().get(0).setTitle(objectname);
			}
			
			TridasElement e = o.getElements().get(0);
			TridasSample s = e.getSamples().get(0);
			
			// Override element name if found in file
			if (samplename != null) {
				project.getObjects().get(0).getElements().get(0).getSamples().get(0).setTitle(samplename);
			}
			
			if (mseriesList.size() > 0) {
				TridasRadius r = s.getRadiuses().get(0);
				r.setMeasurementSeries(mseriesList);
			}
			
		} catch (NullPointerException e) {

		} catch (IndexOutOfBoundsException e2) {

		}
		
		return project;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	@Override
	public int getCurrentLineNumber() {
		// TODO keep track of this
		return 0;
	}

	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		mseriesList.clear();
		defaults = null;
		objectname = null;
		samplename = null;
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
