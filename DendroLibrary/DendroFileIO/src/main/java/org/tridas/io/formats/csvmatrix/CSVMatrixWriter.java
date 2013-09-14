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
package org.tridas.io.formats.csvmatrix;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasValues;

public class CSVMatrixWriter extends AbstractDendroCollectionWriter {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(CSVMatrixWriter.class);
	
	IMetadataFieldSet defaults;
	INamingConvention naming = new NumericalNamingConvention();
	
	public CSVMatrixWriter() {
		super(TridasToCSVMatrixDefaults.class);
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
		defaults = argDefaults;
		
		ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
		
		Boolean hasgroup = false;
		
		// Grab all derivedSeries from project
		try {
			List<TridasDerivedSeries> lst = argProject.getDerivedSeries();
			for (TridasDerivedSeries ds : lst) {
				
				// add to list
				seriesList.add(ds);
				
				for(TridasValues tvsgroup : ds.getValues())
				{
					if(!tvsgroup.isSetValues())
					{
						this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.noDataValues")));
					}
				}
				
			}
		} catch (NullPointerException e) {}
		
		try {
			List<TridasMeasurementSeries> lst = TridasUtils.getMeasurementSeriesFromTridasProject(argProject);
			for (TridasMeasurementSeries ser : lst) {
							
				// add to list
				seriesList.add(ser);
				
				for(TridasValues tvsgroup : ser.getValues())
				{
					if(tvsgroup.isSetValues())
					{

					}
					else
					{
						this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.noDataValues")));
					}
				}
			}
		} catch (NullPointerException e) {}
		
		// No series found
		if (seriesList.size() == 0) {
			throw new IncompleteTridasDataException(I18n.getText("fileio.noData"));
		}
		
		CSVMatrixFile file = new CSVMatrixFile(argDefaults);
		
		file.setSeriesList(seriesList);
		addToFileList(file);
		naming.registerFile(file, argProject, null);
		
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	@Override
	public String getDescription() {
		return I18n.getText("csv.about.description");
	}
	
	@Override
	public String getFullName() {
		return I18n.getText("csv.about.fullName");
	}
	
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}
	
	@Override
	public String getShortName() {
		return I18n.getText("csv.about.shortName");
	}
	
	@Override
	public void setNamingConvention(INamingConvention argConvention) {
		naming = argConvention;
	}
	
	@Override
	public DendroFileFilter getDendroFileFilter() {
		String[] exts = new String[] {"csv"};
		
		return new DendroFileFilter(exts, getShortName());

	}

}
