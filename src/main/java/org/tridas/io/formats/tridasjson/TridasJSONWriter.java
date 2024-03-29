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
package org.tridas.io.formats.tridasjson;

import java.util.ArrayList;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.transform.TridasVersionTransformer.TridasVersion;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;

/**
 * Writer for the TRiDaS file format. This is little more than a
 * wrapper around the JaXB marshaller
 * 
 * @see org.tridas.io.formats.tridas
 * @author peterbrewer
 */
public class TridasJSONWriter extends AbstractDendroCollectionWriter {
		
	private INamingConvention naming = new NumericalNamingConvention();
	
	public TridasJSONWriter()
	{
		super(TridasMetadataFieldSet.class, new TridasJSONFormat());
	}
		
	@Override
	public void parseTridasContainer(TridasTridas argContainer,
			IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		
		if (argContainer == null) {
			throw new ImpossibleConversionException("Tridas container is null!");
			
		}
		
		TridasJSONFile file = new TridasJSONFile(argDefaults);

		for(TridasProject p : argContainer.getProjects())
		{
			file.addTridasProject(p);
		}
		
		// Check that we have no internal XML errors in the file
		try{
			file.validate();
		} catch (ImpossibleConversionException e)
		{
			throw e;
		}
		
		TridasProject project = argContainer.getProjects().get(0);
		TridasObject object = null;
		TridasElement element = null;
		TridasSample sample = null;
		TridasRadius radius = null;
		TridasMeasurementSeries series = null;
		
		ArrayList<TridasObject> objects = TridasUtils.getObjectList(project);
		if (objects.size() == 1) {
			object = objects.get(0);
			
			if (object.getElements().size() == 1) {
				element = object.getElements().get(0);
				
				if (element.getSamples().size() == 1) {
					sample = element.getSamples().get(0);
					
					if (sample.getRadiuses().size() == 1) {
						radius = sample.getRadiuses().get(0);
						
						if (radius.getMeasurementSeries().size() == 1) {
							series = radius.getMeasurementSeries().get(0);
						}
					}
				}
			}
		}
		
		naming.registerFile(file, project, object, element, sample, radius, series);
		addToFileList(file);
		
	}
	
	@Override
	public void parseTridasProject(TridasProject p, IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		
		if (p == null) {
			throw new ImpossibleConversionException("Project is null!");
			
		}
		
		TridasJSONFile file = new TridasJSONFile(argDefaults);
		
		file.addTridasProject(p);
		
		// Check that we have no internal XML errors in the file
		try{
			file.validate();
		} catch (ImpossibleConversionException e)
		{
			throw e;
		}
		
		TridasProject project = p;
		TridasObject object = null;
		TridasElement element = null;
		TridasSample sample = null;
		TridasRadius radius = null;
		TridasMeasurementSeries series = null;
		
		ArrayList<TridasObject> objects = TridasUtils.getObjectList(project);
		if (objects.size() == 1) {
			object = objects.get(0);
			
			if (object.getElements().size() == 1) {
				element = object.getElements().get(0);
				
				if (element.getSamples().size() == 1) {
					sample = element.getSamples().get(0);
					
					if (sample.getRadiuses().size() == 1) {
						radius = sample.getRadiuses().get(0);
						
						if (radius.getMeasurementSeries().size() == 1) {
							series = radius.getMeasurementSeries().get(0);
						}
					}
				}
			}
		}
		
		naming.registerFile(file, project, object, element, sample, radius, series);
		addToFileList(file);
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getNamingConvention()
	 */
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		naming = argConvension;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return null;
	}

}
