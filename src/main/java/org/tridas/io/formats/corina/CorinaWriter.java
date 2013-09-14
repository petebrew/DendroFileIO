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
package org.tridas.io.formats.corina;

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
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class CorinaWriter extends AbstractDendroCollectionWriter {

	private TridasToCorinaDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public CorinaWriter() {
		super(TridasToCorinaDefaults.class);
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
		return I18n.getText("corina.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("corina.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("corina.about.shortName");
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getNamingConvention()
	 */
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}
	

	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException {
		
		defaults = (TridasToCorinaDefaults) argDefaults;
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {		
			TridasToCorinaDefaults pdefaults = (TridasToCorinaDefaults) defaults.clone();
			for (TridasElement e : o.getElements()) {			
				TridasToCorinaDefaults edefaults = (TridasToCorinaDefaults) pdefaults.clone();
				edefaults.populateFromTridasElement(e);
				for (TridasSample s : e.getSamples()) {	
					TridasToCorinaDefaults sdefaults = (TridasToCorinaDefaults) edefaults.clone();
					for (TridasRadius r : s.getRadiuses()) {	
						TridasToCorinaDefaults rdefaults = (TridasToCorinaDefaults) sdefaults.clone();
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {	

							
							for (int i = 0; i < ms.getValues().size(); i++) {
								
								TridasToCorinaDefaults msDefaults = (TridasToCorinaDefaults) rdefaults.clone();
								msDefaults.populateFromTridasMeasurementSeries(ms);
								msDefaults.populateFromWoodCompleteness(ms, r);
								boolean skipThisGroup = false;

								TridasValues tvsgroup = ms.getValues().get(i);
								
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
										msDefaults.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
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
								
								// Convert units and add data to file
								try
								{
									tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, 4);
								
								} catch (NumberFormatException ex)
								{
									this.addWarning(new ConversionWarning(WarningType.IGNORED, ex.getLocalizedMessage()));
									skipThisGroup=true;
								} catch (ConversionWarningException ex) {
									this.addWarning(ex.getWarning());
								}
								
								
								// Dodgy variable so skip
								if(skipThisGroup) continue;
								
								msDefaults.populateFromTridasValues(tvsgroup);
								CorinaFile file = new CorinaFile(msDefaults);
								file.setDataValues(tvsgroup);
								file.setSeries(ms);

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
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) 
		{			
			for (int i = 0; i < ds.getValues().size(); i++) 
			{
				
				TridasToCorinaDefaults dsDefaults = (TridasToCorinaDefaults) defaults.clone();
				
				boolean skipThisGroup = false;

				TridasValues tvsgroup = ds.getValues().get(i);
				
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
						dsDefaults.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
						skipThisGroup=true;
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
							 this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().value())));
							skipThisGroup = true;
						}
					}
				}
				
				// Dodgy variable so skip
				if(skipThisGroup) continue;						
				
				// Convert units and add data to file
				try
				{
					tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, 4);
				
				} catch (NumberFormatException ex)
				{
					this.addWarning(new ConversionWarning(WarningType.IGNORED, ex.getLocalizedMessage()));
					skipThisGroup=true;
				} catch (ConversionWarningException ex) {
					this.addWarning(ex.getWarning());
				}
				
				
				// Dodgy variable so skip
				if(skipThisGroup) continue;
				
				dsDefaults.populateFromTridasValues(tvsgroup);
				dsDefaults.populateFromTridasDerivedSeries(ds);
				
				
				// Try and grab element and sample info from linked series
				if(ds.isSetLinkSeries())
				{
					// TODO what happens if there are links to multiple different entities?
					// For now just go with the first link
					if(ds.getLinkSeries().getSeries().size()==1)
					{
						TridasIdentifier id = ds.getLinkSeries().getSeries().get(0).getIdentifier();
						TridasElement parentElement = (TridasElement) TridasUtils.getEntityByIdentifier(argProject, id, TridasElement.class);
						if(parentElement!=null)
						{
							dsDefaults.populateFromTridasElement(parentElement);
						}
						TridasSample parentSample = (TridasSample) TridasUtils.getEntityByIdentifier(argProject, id, TridasSample.class);
						if(parentSample!=null)
						{
							dsDefaults.populateFromTridasSample(parentSample);
						}
					}
				}
				
				
				CorinaFile file = new CorinaFile(dsDefaults);
				file.setDataValues(tvsgroup);
				file.setSeries(ds);
				
				TridasValues wjvalues = this.getWJValues(ds);
				if(wjvalues!=null)
				{
					file.setWJValues(wjvalues);
				}

				// Set naming convention
				naming.registerFile(file, argProject, ds);
				
				// Add file to list
				addToFileList(file);
			}

			
			
		}

	}
	
	private TridasValues getWJValues(ITridasSeries series)
	{
		for(TridasValues values : series.getValues())
		{
			if(!values.isSetVariable()) continue;
			
			if(!values.getVariable().isSetNormalStd()) continue;
			
			if(values.getVariable().getNormalStd().equals("Tellervo"))
			{
				if(values.getVariable().getNormal().equals("Weiserjahre"))
				{
					return values;
				}
			}
		}
		
		return null;
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		naming = argConvension;
	}
	
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"*.*"};
		
		return new DendroFileFilter(exts, getShortName());

	}
}
