/**
 * Copyright 2013 Peter Brewer
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
package org.tridas.io.formats.heikkenenchrono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.InvalidDendroFileException.PointerType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class HeikkenenChronoReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(HeikkenenChronoReader.class);
	private HeikkenenChronoToTridasDefaults defaults = null;
	private int currentLineNumber = -1;
	
	private ArrayList<TridasValue> dataVals = new ArrayList<TridasValue>();

	public HeikkenenChronoReader() {
		super(HeikkenenChronoToTridasDefaults.class);
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
		return I18n.getText("heikkenenchrono.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("heikkenenchrono.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("heikkenenchrono.about.shortName");
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"rng"};
	}
	
	@Override
	protected void resetReader() {
		
		dataVals.clear();

	}

	private TridasProject getProject() {
		
		TridasProject project = defaults.getProjectWithDefaults(true);
		
		TridasMeasurementSeries ms = project.getObjects().get(0).getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
		TridasUnit units = new TridasUnit();
		units.setNormalTridas(NormalTridasUnit.MICROMETRES);
		TridasVariable variable = new TridasVariable();
		variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		
		ArrayList<TridasValues> valuesList = new ArrayList<TridasValues>();
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setUnit(units);
		valuesGroup.setVariable(variable);
		valuesGroup.setValues(dataVals);
		valuesList.add(valuesGroup);
		
		ms.setValues(valuesList);

		return project;
		
	}


	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		log.debug("Parsing: " + argFileString);
		defaults = (HeikkenenChronoToTridasDefaults) argDefaultFields;
		
		checkFileIsValid(argFileString);
		
		// Copy each value into the data array
		for(String line : argFileString)
		{
			if (line.trim().equals("")) continue;
			TridasValue tval = new TridasValue();
			tval.setValue(line.trim());
			
			dataVals.add(tval);
			
		}
		
		// Reverse the dataVals as they run bark to pith
		Collections.reverse(dataVals);
	}

	
	/**
	 * Check that the file contains only 4 character padded integers, with equal
	 * numbers of each line.
	 * 
	 * @param argFileString
	 * @throws InvalidDendroFileException
	 */
	private void checkFileIsValid(String[] argFileString) throws InvalidDendroFileException
	{
		if(argFileString[0].trim().length()==0) throw new InvalidDendroFileException(I18n.getText("heikkenenchrono.lineEmpty"), 1, PointerType.LINE);
		
		
		
		for(String line : argFileString)
		{
			if (line.trim().equals("")) continue;
			
			try{
				Integer.parseInt(line.trim());
			} catch (NumberFormatException e){
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"));
			}
		}
		
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"rng"};
		
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
