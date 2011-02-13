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
package org.tridas.io.formats.heidelberg;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldVariableCode;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**
 * Writer for heidelberg files. Can only handle basic tree and chronology data, doesn't
 * handle other measurements like earlywood and latewood.
 * 
 * @author daniel
 */
public class HeidelbergWriter extends AbstractDendroCollectionWriter {
	
	private TridasToHeidelbergDefaults defaults;
	private INamingConvention naming = new HierarchicalNamingConvention();
	
	public HeidelbergWriter() {
		super(TridasToHeidelbergDefaults.class);
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException {
		defaults = (TridasToHeidelbergDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		HeidelbergFile file = new HeidelbergFile(defaults);
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {
			TridasToHeidelbergDefaults objectDefaults = (TridasToHeidelbergDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			
			for (TridasElement e : o.getElements()) {
				TridasToHeidelbergDefaults elementDefaults = (TridasToHeidelbergDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				elementDefaults.populateFromTridasLocation(o, e);
				
				for (TridasSample s : e.getSamples()) {
					TridasToHeidelbergDefaults sampleDefaults = (TridasToHeidelbergDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToHeidelbergDefaults radiusDefaults = (TridasToHeidelbergDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							TridasToHeidelbergDefaults msDefaults = (TridasToHeidelbergDefaults) radiusDefaults
									.clone();
							msDefaults.populateFromMS(ms);
							msDefaults.populateFromWoodCompleteness(ms, r);
							
							for (int i = 0; i < ms.getValues().size(); i++) {
								boolean skipThisGroup = false;

								TridasValues tvsgroup = ms.getValues().get(i);
								TridasToHeidelbergDefaults tvDefaults = (TridasToHeidelbergDefaults) msDefaults.clone();
								
								// Check we can handle this variable
								if(tvsgroup.isSetVariable())
								{
									if (!tvsgroup.getVariable().isSetNormalTridas())
									{
										tvDefaults.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
									}
									else
									{
										switch(tvsgroup.getVariable().getNormalTridas())
										{
										case RING_WIDTH:
										case EARLYWOOD_WIDTH:
										case MAXIMUM_DENSITY:
										case LATEWOOD_WIDTH:
											// All handled ok
											break;
										default:
											// All other variables not representable
											tvDefaults.addConversionWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().toString().toLowerCase().replace("_", " "))));
											skipThisGroup = true;
										}
									}
								}
								
								// Dodgy variable so skip
								if(skipThisGroup) continue;
								
								// Check there are no non-number values
								for (TridasValue v : tvsgroup.getValues()) {
									try {
										Integer.parseInt(v.getValue());
									} catch (NumberFormatException e2) {
										throw new IncompleteTridasDataException(
												"One or more data values are not numbers!  This is technically acceptable in TRiDaS but not supported in this library.");
									}
								}
								
								
								tvDefaults.populateFromTridasValues(tvsgroup);

								file.addSeries(ms, tvsgroup, tvDefaults);
								naming.registerFile(file, argProject, o, e, s, r, ms);
							}
							
						}
					}
					
					/*
					 * if( s.isSetRadiusPlaceholder()){
					 * // we have to search through all derived series to find the one
					 * matching our placeholder id
					 * for(TridasDerivedSeries ds : argProject.getDerivedSeries()){
					 * if(ds.getId() == null){
					 * throw new
					 * IncompleteTridasDataException("Id in derived series was null");
					 * }
					 * if(ds.getId().equals(s.getRadiusPlaceholder().
					 * getMeasurementSeriesPlaceholder().getId())){
					 * TridasToHeidelbergDefaults dsDefaults =
					 * (TridasToHeidelbergDefaults) elementDefaults.clone();
					 * dsDefaults.populateFromDerivedSeries(ds);
					 * for(int i=0; i< ds.getValues().size(); i++){
					 * TridasValues tvs = ds.getValues().get(i);
					 * TridasToHeidelbergDefaults tvDefaults =
					 * (TridasToHeidelbergDefaults) dsDefaults.clone();
					 * tvDefaults.populateFromTridasValues(tvs);
					 * HeidelbergFile file = new HeidelbergFile(this, tvDefaults);
					 * file.setSeries(ds, i);
					 * naming.registerFile(file, argProject, ds);
					 * addToFileList(file);
					 * }
					 * }
					 * }
					 * }
					 */
				}
			}
		}
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) {
			TridasToHeidelbergDefaults dsDefaults = (TridasToHeidelbergDefaults) defaults.clone();
			dsDefaults.populateFromDerivedSeries(ds);
			
			for (int i = 0; i < ds.getValues().size(); i++) {
				
				TridasValues tvsgroup = ds.getValues().get(i);
				
				boolean skipThisGroup = false;
				TridasToHeidelbergDefaults tvDefaults = (TridasToHeidelbergDefaults) dsDefaults.clone();
				
				// Check we can handle this variable
				if(tvsgroup.isSetVariable())
				{
					if (!tvsgroup.getVariable().isSetNormalTridas())
					{
						tvDefaults.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
					}
					else
					{
						switch(tvsgroup.getVariable().getNormalTridas())
						{
						case RING_WIDTH:
						case EARLYWOOD_WIDTH:
						case MAXIMUM_DENSITY:
						case LATEWOOD_WIDTH:
							// All handled ok
							break;
						default:
							// All other variables not representable
							this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().value())));
							skipThisGroup = true;
						}
					}
				}
				
				// Dodgy variable so skip
				if(!skipThisGroup)
				{
				
					// Check there are no non-number values
					for (TridasValue v : tvsgroup.getValues()) {
						try {
							Integer.parseInt(v.getValue());
						} catch (NumberFormatException e2) {
							throw new IncompleteTridasDataException(
									I18n.getText("heidelberg.integerValuesOnly"));
						}
					}
					
					tvDefaults.populateFromTridasValues(tvsgroup);
					
					file.addSeries(ds, tvsgroup, tvDefaults);

					naming.registerFile(file, argProject, ds);
					
				}
			}
		}
		
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
		return defaults;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("heidelberg.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("heidelberg.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("heidelberg.about.shortName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#parseTridasContainer()
	 */
	@Override
	protected void parseTridasContainer(TridasTridas argContainer,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
	
		for(TridasProject project : argContainer.getProjects())
		{
			parseTridasProject(project, argDefaults);
		}	
	}
}
