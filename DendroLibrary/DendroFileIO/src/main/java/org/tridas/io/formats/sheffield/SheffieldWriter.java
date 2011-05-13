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
package org.tridas.io.formats.sheffield;

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
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class SheffieldWriter extends AbstractDendroCollectionWriter {

	private TridasToSheffieldDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public SheffieldWriter() {
		super(TridasToSheffieldDefaults.class);
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
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("sheffield.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("sheffield.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("sheffield.about.shortName");
	}

	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException {
		defaults = (TridasToSheffieldDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {
			TridasToSheffieldDefaults objectDefaults = (TridasToSheffieldDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			
			for (TridasElement e : o.getElements()) {
				TridasToSheffieldDefaults elementDefaults = (TridasToSheffieldDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				elementDefaults.populateFromTridasLocation(o, e);
				
				for (TridasSample s : e.getSamples()) {
					TridasToSheffieldDefaults sampleDefaults = (TridasToSheffieldDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToSheffieldDefaults radiusDefaults = (TridasToSheffieldDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							TridasToSheffieldDefaults msDefaults = (TridasToSheffieldDefaults) radiusDefaults
									.clone();
							msDefaults.populateFromTridasMeasurementSeries(ms);
							msDefaults.populateFromWoodCompleteness(ms, r);
							
							for (int i = 0; i < ms.getValues().size(); i++) {
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
										case MAXIMUM_DENSITY:
											// All handled ok
											break;
										default:
											// All other variables not representable
											this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().toString().toLowerCase().replace("_", " "))));
											skipThisGroup = true;
										}
									}
								}
								
								// Dodgy variable so skip
								if(skipThisGroup) continue;
								
								TridasToSheffieldDefaults tvDefaults = (TridasToSheffieldDefaults) msDefaults.clone();
								tvDefaults.populateFromTridasValues(tvsgroup);
								
								SheffieldFile file = new SheffieldFile(tvDefaults);
								
								// Add series to file
								file.setSeries(ms);
								
								// Convert units and add data to file
								TridasValues theValues = null;
								try {
									theValues = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);
								} catch (NumberFormatException e1) {
									throw new IncompleteTridasDataException(I18n.getText("general.ringValuesNotNumbers"));
								} catch (ConversionWarningException e1) {
									// Convertion failed so warn and stick with original values
									this.addWarning(e1.getWarning());
									theValues = tvsgroup;
								}
						
								// Intercept missing rings and replace with 1's as Sheffield can't cope otherwise
								for(TridasValue val : theValues.getValues())
								{
									try
									{
										Double dblval = Double.parseDouble(val.getValue());
										if(dblval.equals(0.0))
										{
											val.setValue("1");
											this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
													I18n.getText("sheffield.missingRingHandling")));
										}
										else if (dblval.compareTo(0.0)<0)
										{
											val.setValue("1");
											this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
													I18n.getText("sheffield.negativeRingHandling")));
										}
									}
									catch (Exception e2)
									{
										throw new IncompleteTridasDataException(I18n.getText("general.ringValuesNotNumbers"));
									}
								}
								file.setDataValues(theValues);
								
								// Set naming convention
								naming.registerFile(file, argProject, o, e, s, r, ms);
								
								// Add file to list
								addToFileList(file);
							}
							
						}
					}
				}
			}
		}
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) {
			TridasToSheffieldDefaults dsDefaults = (TridasToSheffieldDefaults) defaults.clone();
			dsDefaults.populateFromTridasDerivedSeries(ds);
			
			
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
						case MAXIMUM_DENSITY:
							// All handled ok
							break;
						default:
							// All other variables not representable
							this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().toString().toLowerCase().replace("_", " "))));
							skipThisGroup = true;
						}
					}
				}
				
				// Dodgy variable so skip
				if(skipThisGroup) continue;
				
				TridasToSheffieldDefaults tvDefaults = (TridasToSheffieldDefaults) dsDefaults.clone();
				tvDefaults.populateFromTridasValues(tvsgroup);
				
				SheffieldFile file = new SheffieldFile(tvDefaults);
				
				// Add series to file
				file.setSeries(ds);
				
				// Convert units and add data to file
				try {
					file.setDataValues(UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true));
				} catch (NumberFormatException e) {
				} catch (ConversionWarningException e) {
					this.addWarning(e.getWarning());
				}
				
				// Set naming convention
				naming.registerFile(file, argProject, ds);
				
				// Add file to list
				addToFileList(file);
			}
			
		}
		
	}
}
