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

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.csvmatrix.CSVMatrixFile.MatrixSeries;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NamingConventionGrouper;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

public class CSVMatrixWriter extends AbstractDendroCollectionWriter {

	private TridasToMatrixDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	protected Class<? extends CSVMatrixFile> clazz;
	private boolean seriesAddedFlag = false;
	
	public CSVMatrixWriter() {
		super(TridasToMatrixDefaults.class, new CSVMatrixFormat());
		clazz = CSVMatrixFile.class;
	}
	
	
	public CSVMatrixWriter(Class<? extends IMetadataFieldSet> argDefaultFieldsClass, AbstractDendroFormat format) {
		super(argDefaultFieldsClass, format);
	}
	
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getNamingConvention()
	 */
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}
	

	private void parseSeries(ITridasSeries series, CSVMatrixFile file)
	{
		TridasToMatrixDefaults msDefaults = (TridasToMatrixDefaults) defaults.clone();
		msDefaults.populateFromTridasSeries(series);
		
		for (int i = 0; i < series.getValues().size(); i++) {
			
			
			TridasValues tvsgroup = series.getValues().get(i);
			
			if(tvsgroup.isSetVariable())
			{
				
				TridasToMatrixDefaults valuesDefaults = (TridasToMatrixDefaults) msDefaults.clone();
				
				valuesDefaults.populateFromTridasValues(tvsgroup);
				
				// Warn if non-standard variable
				if (!tvsgroup.getVariable().isSetNormalTridas())
				{
					valuesDefaults.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
				}
				
				// Convert units if possible
				try
				{
					tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, 4);
				
				} catch (NumberFormatException ex)
				{
					this.addWarning(new ConversionWarning(WarningType.IGNORED, ex.getLocalizedMessage()));
				} catch (ConversionWarningException ex) {
					this.addWarning(ex.getWarning());
				}
								
				file.addSeries(valuesDefaults, series, tvsgroup);
				seriesAddedFlag = true;
				
			}
			
		}
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException {
		
		defaults = (TridasToMatrixDefaults) argDefaults;
		
		
		CSVMatrixFile file = null;	
		try {
			file = clazz.newInstance();
		} catch (InstantiationException e1) {
			throw new ImpossibleConversionException("Failed to instantiate CSVMatrixFile class");
		} catch (IllegalAccessException e1) {
			throw new ImpossibleConversionException("Failed to instantiate CSVMatrixFile class");
		}
		
		NamingConventionGrouper ncgroup = new NamingConventionGrouper();
		ncgroup.add(argProject);
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {			
			ncgroup.add(o);
			for (TridasElement e : o.getElements()) {		
				ncgroup.add(e);
				for (TridasSample s : e.getSamples()) {
					ncgroup.add(s);
					for (TridasRadius r : s.getRadiuses()) {
						ncgroup.add(r);
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							ncgroup.add(ms);
							defaults.populateFromTridasMeasurementSeries(ms);
							
							for (int i = 0; i < ms.getValues().size(); i++) {	
								parseSeries(ms, file);
							}
							
							// Set naming convention
							if(TridasUtils.getMeasurementSeriesFromTridasProject(argProject).size()>1)
							{
								naming.registerFile(file, argProject, o, e, s, r, ms);
							}
							else
							{
								naming.registerFile(file, ncgroup);
							}
														
						}
					}
					
				}
			}
		}
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) {		
			TridasToMatrixDefaults dsDefaults = (TridasToMatrixDefaults) defaults.clone();
			
			ncgroup.add(ds);
			defaults.populateFromTridasDerivedSeries(ds);
			for (int i = 0; i < ds.getValues().size(); i++) {
					parseSeries(ds, file);
			}
			
			if(argProject.getDerivedSeries().size()>1)
			{
				naming.registerFile(file, argProject, ds);
			}
			else
			{
				naming.registerFile(file, ncgroup);
			}				
			
		}
		
		if(!seriesAddedFlag)
		{
			this.clearWarnings();
			throw new ImpossibleConversionException("The input file contains no data series that can be represented in this format.");
		}
		else
		{
			this.addToFileList(file);
		}
		
	}

	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		naming = argConvension;
	}
}
