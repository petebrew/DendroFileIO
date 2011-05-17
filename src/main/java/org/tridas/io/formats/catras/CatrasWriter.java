/*******************************************************************************
 * Copyright 2011 Daniel Murphy and Peter Brewer
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
package org.tridas.io.formats.catras;

import java.io.IOException;

import jxl.write.WriteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.SeriesCode8CharNamingConvention;
import org.tridas.io.util.FileHelper;
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

public class CatrasWriter extends AbstractDendroCollectionWriter {

	private static final Logger log = LoggerFactory.getLogger(CatrasWriter.class);
	private TridasToCatrasDefaults defaults;
	private INamingConvention naming = new SeriesCode8CharNamingConvention();
	
	public CatrasWriter() {
		super(TridasToCatrasDefaults.class);
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
		return I18n.getText("catras.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("catras.about.fullName");
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getNamingConvention()
	 */
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("catras.about.shortName");
	}

	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException {
		defaults = (TridasToCatrasDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {
			TridasToCatrasDefaults objectDefaults = (TridasToCatrasDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			
			for (TridasElement e : o.getElements()) {
				TridasToCatrasDefaults elementDefaults = (TridasToCatrasDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);

				
				for (TridasSample s : e.getSamples()) {
					TridasToCatrasDefaults sampleDefaults = (TridasToCatrasDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToCatrasDefaults radiusDefaults = (TridasToCatrasDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							TridasToCatrasDefaults msDefaults = (TridasToCatrasDefaults) radiusDefaults
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
								
								TridasToCatrasDefaults tvDefaults = (TridasToCatrasDefaults) msDefaults.clone();
								tvDefaults.populateFromTridasValues(tvsgroup);
								
								CatrasFile file = new CatrasFile(tvDefaults);
								
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
			TridasToCatrasDefaults dsDefaults = (TridasToCatrasDefaults) defaults.clone();
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
				
				TridasToCatrasDefaults tvDefaults = (TridasToCatrasDefaults) dsDefaults.clone();
				tvDefaults.populateFromTridasValues(tvsgroup);
				
				CatrasFile file = new CatrasFile(tvDefaults);
				
				// Add series to file
				file.setSeries(ds);
				
				// Convert units and add data to file
				try {
					file.setDataValues(UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true));
				} catch (NumberFormatException e) {
					throw new IncompleteTridasDataException(I18n.getText("general.ringValuesNotNumbers"));
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

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvention) {	
		if(argConvention instanceof SeriesCode8CharNamingConvention)
		{
			naming = argConvention;
		}
		else
		{
			log.debug("CATRAS must use the SeriesCode naming convention.  Requested naming convention ignored.");		
			naming = new SeriesCode8CharNamingConvention();
		}		
	}
	
	@Override
	public void saveFileToDisk(String argOutputFolder, String argFilename, IDendroFile argFile) {
		
		FileHelper helper;
		
		boolean absolute = argOutputFolder.startsWith("/");
		
		if (absolute) {
			helper = new FileHelper(argOutputFolder);
		}
		else {
			helper = new FileHelper();
			argFilename = argOutputFolder + argFilename;
		}
		
		try {
			((CatrasFile) argFile).saveToDisk(helper.createOutput(argFilename + "." + argFile.getExtension()));
		} catch (WriteException e) {
			log.error("Error saving file to disk", e);
		} catch (IOException e) {
			log.error("Error saving file to disk", e);
		}
	}
	


}
