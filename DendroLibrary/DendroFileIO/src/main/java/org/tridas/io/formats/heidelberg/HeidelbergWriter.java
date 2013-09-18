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
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NamingConventionGrouper;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.StringUtils;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
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
	private INamingConvention naming = new NumericalNamingConvention();
	protected boolean isstacked = true;
	
	public HeidelbergWriter() {
		super(TridasToHeidelbergDefaults.class, new HeidelbergFormat());
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException {
		defaults = (TridasToHeidelbergDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		
		//TODO ESTHER SPECIAL
		HeidelbergFile file = new HeidelbergFile(defaults);
		NamingConventionGrouper ncgroup = new NamingConventionGrouper();
		ncgroup.add(argProject);
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {
			TridasToHeidelbergDefaults objectDefaults = (TridasToHeidelbergDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			ncgroup.add(o);
			
			for (TridasElement e : o.getElements()) {
				TridasToHeidelbergDefaults elementDefaults = (TridasToHeidelbergDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				elementDefaults.populateFromTridasLocation(o, e);
				ncgroup.add(e);
				
				for (TridasSample s : e.getSamples()) {
					TridasToHeidelbergDefaults sampleDefaults = (TridasToHeidelbergDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					ncgroup.add(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToHeidelbergDefaults radiusDefaults = (TridasToHeidelbergDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
						ncgroup.add(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							TridasToHeidelbergDefaults msDefaults = (TridasToHeidelbergDefaults) radiusDefaults
									.clone();
							msDefaults.populateFromMS(ms);
							msDefaults.populateFromWoodCompleteness(ms, r);
							ncgroup.add(ms);
							
							for (int i = 0; i < ms.getValues().size(); i++) {
								boolean skipThisGroup = false;

								TridasValues tvsgroup = ms.getValues().get(i);
								TridasToHeidelbergDefaults tvDefaults = (TridasToHeidelbergDefaults) msDefaults.clone();
								
								// Check we can handle this variable
								if(tvsgroup.isSetVariable())
								{
									if(!tvsgroup.isSetValues())
									{
										this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.noDataValues")));
										skipThisGroup = true;
									}
									else if (!tvsgroup.getVariable().isSetNormalTridas())
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
								for (TridasValue v : tvsgroup.getValues()) 
								{
									if(!StringUtils.isStringWholeInteger(v.getValue()))
									{
										throw new IncompleteTridasDataException(
												I18n.getText("general.ringValuesNotWholeNumbers"));
									}
								}
								
								
								tvDefaults.populateFromTridasValues(tvsgroup);
								
								//TODO ESTHER SPECIAL
								
								if(!isstacked) 
								{
									file = new HeidelbergFile(defaults);
									file.addSeries(ms, tvsgroup, tvDefaults);
									naming.registerFile(file, argProject, o, e, s, r, ms);
									addToFileList(file);
									file = new HeidelbergFile(defaults);
								}
								else
								{
									file.addSeries(ms, tvsgroup, tvDefaults);
									naming.registerFile(file, ncgroup);
								}
							}
						}
					}
				}
			}
		}
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) {
			TridasToHeidelbergDefaults dsDefaults = (TridasToHeidelbergDefaults) defaults.clone();
			dsDefaults.populateFromDerivedSeries(ds);
			
			ncgroup.add(ds);
			
			for (int i = 0; i < ds.getValues().size(); i++) {
				
				TridasValues tvsgroup = ds.getValues().get(i);
				
				boolean skipThisGroup = false;
				TridasToHeidelbergDefaults tvDefaults = (TridasToHeidelbergDefaults) dsDefaults.clone();
				
				// Check we can handle this variable
				if(tvsgroup.isSetVariable())
				{
					if(!tvsgroup.isSetValues())
					{
						this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.noDataValues")));
						skipThisGroup = true;
					}
					else if (!tvsgroup.getVariable().isSetNormalTridas())
					{
						if(tvsgroup.getVariable().isSetNormal())
						{
							if(tvsgroup.getVariable().getNormal().equals("Weiserjahre"))
							{
								this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, I18n.getText("tellervo.skippingWeiserjahre")));
								skipThisGroup = true;
							}
							else
							{
								tvDefaults.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
							}
						}
						else
						{
							tvDefaults.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
						}
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
					
					// Try and grab object, element, sample and radius info from linked series
					if(ds.isSetLinkSeries())
					{
						// TODO what happens if there are links to multiple different entities?
						// For now just go with the first link
						//if(ds.getLinkSeries().getSeries().size()>1) break;
						TridasIdentifier id = ds.getLinkSeries().getSeries().get(0).getIdentifier();
						
						TridasObject parentObject = (TridasObject) TridasUtils.getEntityByIdentifier(argProject, id, TridasObject.class);
						if(parentObject!=null)
						{
							tvDefaults.populateFromTridasObject(parentObject);
						}

						TridasElement parentElement = (TridasElement) TridasUtils.getEntityByIdentifier(argProject, id, TridasElement.class);
						if(parentElement!=null)
						{
							tvDefaults.populateFromTridasElement(parentElement);
						}
						
						TridasSample parentSample = (TridasSample) TridasUtils.getEntityByIdentifier(argProject, id, TridasSample.class);
						if(parentSample!=null)
						{
							tvDefaults.populateFromTridasSample(parentSample);
						}
						
						TridasRadius parentRadius = (TridasRadius) TridasUtils.getEntityByIdentifier(argProject, id, TridasRadius.class);
						if(parentRadius!=null)
						{
							tvDefaults.populateFromTridasRadius(parentRadius);
						}
					}
					
					
					if(!isstacked) 
					{
						file = new HeidelbergFile(defaults);
						file.addSeries(ds, tvsgroup, tvDefaults);
						naming.registerFile(file, argProject, ds);
						addToFileList(file);
						file = new HeidelbergFile(defaults);
					}
					else
					{
						file.addSeries(ds, tvsgroup, tvDefaults);
						naming.registerFile(file, ncgroup);
					}
				}
			}
		}
		
		//TODO ESTHER SPECIAL	
		if(file.getSeries().length>0)
		{
			addToFileList(file);
		}
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
	
}
