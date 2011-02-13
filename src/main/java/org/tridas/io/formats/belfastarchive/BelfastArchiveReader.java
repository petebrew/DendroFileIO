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
package org.tridas.io.formats.belfastarchive;

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
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.Year;

public class BelfastArchiveReader extends AbstractDendroFileReader {
	
	private static final Logger log = LoggerFactory.getLogger(BelfastArchiveReader.class);
	// defaults given by user
	private BelfastArchiveToTridasDefaults defaults = null;
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	String objectname;
	String samplename;
	SafeIntYear startYear;
	
	public BelfastArchiveReader() {
		super(BelfastArchiveToTridasDefaults.class);
	}
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		defaults = (BelfastArchiveToTridasDefaults) argDefaultFields;
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		
		// Extract 'metadata' ;-)
		objectname = argFileString[0].trim();
		samplename = argFileString[1].trim();
		
		// Extract data
		ArrayList<TridasValue> ringWidthValues = new ArrayList<TridasValue>();
		int footerStartInd = 0;
		for (int i = 2; i < argFileString.length; i++) {
			TridasValue v = new TridasValue();
			int val;
			
			if (argFileString[i].contains("[[ARCHIVE")) {
				// Reached footer block
				footerStartInd = i;
				break;
			}
			
			try {
				val = Integer.valueOf(argFileString[i].trim());
			} catch (NumberFormatException e) {
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
			}
			
			v.setValue(argFileString[i].trim());
			ringWidthValues.add(v);
			log.debug("value = " + String.valueOf(argFileString[i]));
		}
		
		// Extract metadata from footer
		
		// TODO - implement
		/*
		 * "[[ARCHIVE]]"
		 * 1277 <- Start year
		 * 9177 <- ??
		 * .01 <- Resolution = hundredsmm
		 * 1.035795 <- ??
		 * 0.212144 <- ??
		 * IAN 21/01/96 <- User id and date
		 * TWYNING CHURCH #01 <- Title
		 * Pith F Sap 32 <- ??
		 * "" <- ??
		 * "[[ END OF TEXT ]]"
		 */

		// Line 1 - Start year
		try {
			startYear = new SafeIntYear(Integer.valueOf(argFileString[footerStartInd + 1]));
			TridasInterpretation interp = new TridasInterpretation();
			TridasDating dating = new TridasDating();
			dating.setType(NormalTridasDatingType.ABSOLUTE);
			interp.setDating(dating);
			Year firstYear = startYear.toTridasYear(DatingSuffix.AD);
			interp.setFirstYear(firstYear);
			series.setInterpretation(interp);
			
		} catch (NumberFormatException e) {}
		
		// Line 2 - ?
		
		// Line 3 - Resolution
		TridasUnit units = new TridasUnit();
		if (argFileString[footerStartInd + 3].equals("0.01")) {
			// Set units to 1/100th mm.
			units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		}
		else if (argFileString[footerStartInd + 3].equals("0.001")) {
			// Set units to microns
			units.setNormalTridas(NormalTridasUnit.MICROMETRES);
		}
		else if (argFileString[footerStartInd + 3].equals("0.1")) {
			// Set units to microns
			units.setNormalTridas(NormalTridasUnit.TENTH_MM);
		}
		else if (argFileString[footerStartInd + 3].equals("0.2")) {
			// Set units to twentieths
			units.setNormalTridas(NormalTridasUnit.TWENTIETH_MM);
		}
		else if (argFileString[footerStartInd + 3].equals("0.5")) {
			// Set units to fiftieths
			units.setNormalTridas(NormalTridasUnit.FIFTIETH_MM);
		}
		else {
			units = null;
		}
		
		// Lines 4,5,6 - ?
		
		// Line 7 - Series title
		series.setTitle(argFileString[footerStartInd + 7]);
		
		// Lines 8,9 - ?
		
		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(UUID.randomUUID().toString());
		seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		
		// Add values to nested value(s) tags
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setValues(ringWidthValues);
		if (units != null) {
			valuesGroup.setUnit(units);
		}
		else {
			valuesGroup.setUnitless(new TridasUnitless());
		}
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) defaults
				.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
		valuesGroupList.add(valuesGroup);
		
		// Add all the data to the series
		series.setValues(valuesGroupList);
		series.setIdentifier(seriesId);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime());
		
		// Add series to our list
		mseriesList.add(series);
		
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"arx"};
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
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getCurrentLineNumber()
	 */
	@Override
	public int getCurrentLineNumber() {
		// TODO track this
		return 0;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("belfastarchive.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("belfastarchive.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("belfastarchive.about.shortName");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		defaults = null;
		mseriesList.clear();
		objectname = null;
		samplename = null;
		startYear = null;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"txt", "arx", "dat"};
		
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
