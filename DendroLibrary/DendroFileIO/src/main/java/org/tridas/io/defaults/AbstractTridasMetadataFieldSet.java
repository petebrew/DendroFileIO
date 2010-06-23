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
package org.tridas.io.defaults;

import java.util.ArrayList;

import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * Abstract class for creating default Tridas object and fields
 * 
 * @author Daniel
 */
public abstract class AbstractTridasMetadataFieldSet extends AbstractMetadataFieldSet {
	
	/**
	 * Get a TridasProject with mandatory fields set to default
	 * 
	 * @return
	 */
	public TridasProject getProjectWithDefaults(boolean argCascade) {
		TridasProject project = getDefaultTridasProject();
		
		if (argCascade) {
			ArrayList<TridasObject> objects = new ArrayList<TridasObject>();
			objects.add(getObjectWithDefaults(argCascade));
			project.setObjects(objects);
		}
		
		return project;
	}
	
	public TridasProject getProjectWithDefaults() {
		return getProjectWithDefaults(false);
	}
	
	public TridasObject getObjectWithDefaults(boolean argCascade) {
		TridasObject o = getDefaultTridasObject();
		
		if (argCascade) {
			ArrayList<TridasElement> elements = new ArrayList<TridasElement>();
			elements.add(getElementWithDefaults(argCascade));
			o.setElements(elements);
		}
		
		return o;
	}
	
	public TridasObject getObjectWithDefaults() {
		return getObjectWithDefaults(false);
	}
	
	public TridasMeasurementSeries getMeasurementSeriesWithDefaults() {
		return getMeasurementSeriesWithDefaults(false);
	}
	
	public TridasMeasurementSeries getMeasurementSeriesWithDefaults(Boolean argCascade) {
		return getDefaultTridasMeasurementSeries();
		
	}
	
	public TridasDerivedSeries getDerivedSeriesWithDefaults() {
		return getDerivedSeriesWithDefaults(false);
	}
	
	public TridasDerivedSeries getDerivedSeriesWithDefaults(Boolean argCascade) {
		return getDefaultTridasDerivedSeries();
		
	}
	
	public TridasElement getElementWithDefaults(boolean argCascade) {
		TridasElement e = getDefaultTridasElement();
		
		if (argCascade) {
			ArrayList<TridasSample> samples = new ArrayList<TridasSample>();
			TridasSample s = getSampleWithDefaults(argCascade);
			samples.add(s);
			e.setSamples(samples);
		}
		
		return e;
	}
	
	public TridasElement getElementWithDefaults() {
		return getElementWithDefaults(false);
	}
	
	public TridasSample getSampleWithDefaults() {
		return getSampleWithDefaults(false);
	}
	
	public TridasSample getSampleWithDefaults(boolean argCascade) {
		TridasSample s = getDefaultTridasSample();
		
		if (argCascade) {
			ArrayList<TridasRadius> radii = new ArrayList<TridasRadius>();
			radii.add(getRadiusWithDefaults(argCascade));
			s.setRadiuses(radii);
		}
		
		return s;
	}
	
	public TridasRadius getRadiusWithDefaults(Boolean cascade) {
		TridasRadius r = getDefaultTridasRadius();
		
		if (cascade) {
			ArrayList<TridasMeasurementSeries> mslist = new ArrayList<TridasMeasurementSeries>();
			mslist.add(getMeasurementSeriesWithDefaults());
			r.setMeasurementSeries(mslist);
		}
		
		return r;
	}
	
	/**
	 * Returns a non-cascading {@link TridasProject} with default values
	 * 
	 * @return
	 */
	protected abstract TridasProject getDefaultTridasProject();
	
	/**
	 * Returns a non-cascading {@link TridasObject} with default values
	 * 
	 * @return
	 */
	protected abstract TridasObject getDefaultTridasObject();
	
	/**
	 * Returns a non-cascading {@link TridasElement} with default values
	 * 
	 * @return
	 */
	protected abstract TridasElement getDefaultTridasElement();
	
	/**
	 * Returns a non-cascading {@link TridasSample} with default values
	 * 
	 * @return
	 */
	protected abstract TridasSample getDefaultTridasSample();
	
	/**
	 * Returns a non-cascading {@link TridasRadius} with default values
	 * 
	 * @return
	 */
	protected abstract TridasRadius getDefaultTridasRadius();
	
	/**
	 * Returns a non-cascading {@link TridasMeasurementSeries} with default values
	 * 
	 * @return
	 */
	protected abstract TridasMeasurementSeries getDefaultTridasMeasurementSeries();
	
	/**
	 * Returns a non-cascading {@link TridasDerivedSeries} with default values
	 * 
	 * @return
	 */
	protected abstract TridasDerivedSeries getDefaultTridasDerivedSeries();
}
