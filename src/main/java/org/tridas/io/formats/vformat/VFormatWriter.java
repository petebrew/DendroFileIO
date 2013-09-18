/*******************************************************************************
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.tridas.io.formats.vformat;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

public class VFormatWriter extends AbstractDendroCollectionWriter {
	
	private TridasToVFormatDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public VFormatWriter() {
		super(TridasToVFormatDefaults.class, new VFormat());
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
	public void setNamingConvention(INamingConvention argConvention) {
		naming = argConvention;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException {
		defaults = (TridasToVFormatDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		VFormatFile file = new VFormatFile();
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {
			TridasToVFormatDefaults objectDefaults = (TridasToVFormatDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			
			for (TridasElement e : o.getElements()) {
				TridasToVFormatDefaults elementDefaults = (TridasToVFormatDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				elementDefaults.populateFromTridasLocation(o, e);
				
				for (TridasSample s : e.getSamples()) {
					TridasToVFormatDefaults sampleDefaults = (TridasToVFormatDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToVFormatDefaults radiusDefaults = (TridasToVFormatDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) 
						{
							TridasToVFormatDefaults msDefaults = (TridasToVFormatDefaults) radiusDefaults
									.clone();
							
							if(ms.isSetInterpretation())
							{
								if (ms.getInterpretation().isSetDating())
								{
									switch (ms.getInterpretation().getDating().getType())
									{
									case ABSOLUTE:
									case DATED_WITH_UNCERTAINTY:
									case RADIOCARBON:
										break;
									case RELATIVE:
										addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
												I18n.getText("general.outputRelativeDatingUnsupported")));
										break;
									default:
									}
								}
							}
							
							
							msDefaults.populateFromTridasMeasurementSeries(ms);
							msDefaults.populateFromWoodCompleteness(ms, r);
							
							for (int i = 0; i < ms.getValues().size(); i++) 
							{
								boolean skipThisGroup = false;

								TridasValues tvsgroup = ms.getValues().get(i);
								
								// Check we can handle this variable
								if(tvsgroup.isSetVariable())
								{
									if (!tvsgroup.getVariable().isSetNormalTridas())
									{
										this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
									}
									else
									{
										switch(tvsgroup.getVariable().getNormalTridas())
										{
										case RING_WIDTH:
										case EARLYWOOD_WIDTH:
										case LATEWOOD_WIDTH:
											// Handled ok - but needs unit convertion
											try {
												tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);
											} catch (NumberFormatException e1) {
											} catch (ConversionWarningException e1) {
												this.addWarning(e1.getWarning());
											}				
											break;		
										case LATEWOOD_PERCENT:
										case MAXIMUM_DENSITY:
											// All handled ok 
											// TODO handle unit conversion
											break;
										default:
											// All other variables not representable
											this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().value())));
											skipThisGroup = true;
										}
									}
								}
								
								// Dodgy variable so skip
								if(skipThisGroup) continue;
								
								TridasToVFormatDefaults tvDefaults = (TridasToVFormatDefaults) msDefaults.clone();
								tvDefaults.populateFromTridasValues(tvsgroup);
					
								
								// Add series to file
								file.addSeries(ms, tvsgroup, tvDefaults);
								
								// Set naming convention
								naming.registerFile(file, argProject, o, e, s, r, ms);
							}
							
						}
					}
				}
			}
		}
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) 
		{
			TridasToVFormatDefaults dsDefaults = (TridasToVFormatDefaults) defaults.clone();
			dsDefaults.populateFromTridasDerivedSeries(ds);
			
			if(ds.isSetInterpretation())
			{
				if (ds.getInterpretation().isSetDating())
				{
					switch (ds.getInterpretation().getDating().getType())
					{
					case ABSOLUTE:
					case DATED_WITH_UNCERTAINTY:
					case RADIOCARBON:
						break;
					case RELATIVE:
						addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
								I18n.getText("general.outputRelativeDatingUnsupported")));
						break;
					default:
					}
				}
			}
			
			
			for (int i = 0; i < ds.getValues().size(); i++) {
				boolean skipThisGroup = false;

				TridasValues tvsgroup = ds.getValues().get(i);
				
				// Check we can handle this variable
				if(tvsgroup.isSetVariable())
				{
					if (!tvsgroup.getVariable().isSetNormalTridas())
					{
						this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
					}
					else
					{
						switch(tvsgroup.getVariable().getNormalTridas())
						{
						case RING_WIDTH:
						case EARLYWOOD_WIDTH:
						case LATEWOOD_WIDTH:
							// Handled ok - but needs unit convertion
							try {
								tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);
							} catch (NumberFormatException e) {
							} catch (ConversionWarningException e) {
								this.addWarning(e.getWarning());
							}				
							break;		
						case LATEWOOD_PERCENT:
						case MAXIMUM_DENSITY:
							// All handled ok 
							// TODO handle unit conversion
							break;
						default:
							// All other variables not representable
							this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().value())));
							skipThisGroup = true;
						}
					}
				}
				
				// Dodgy variable so skip
				if(skipThisGroup) continue;
				
				TridasToVFormatDefaults tvDefaults = (TridasToVFormatDefaults) dsDefaults.clone();
				tvDefaults.populateFromTridasValues(tvsgroup);
			
				// Try and grab object, element, sample and radius info from linked series
				if(ds.isSetLinkSeries())
				{
					// TODO what happens if there are links to multiple different entities?
					// For now just go with the first link
					if(ds.getLinkSeries().getSeries().size()>1) break;
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
				
				// Add series to file
				file.addSeries(ds, tvsgroup, tvDefaults);
								
				// Set naming convention
				naming.registerFile(file, argProject, ds);
			
			}
			
		}
		
		// Add file to list, but only if it has series in it!
		if(file.getSeries()!=null  && file.getSeries().length>0)
		{
			addToFileList(file);
		}
		
		
	}
}
