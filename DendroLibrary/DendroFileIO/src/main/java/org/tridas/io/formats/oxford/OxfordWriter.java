/*******************************************************************************
 * Copyright 2011 Peter Brewer and Daniel Murphy
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
package org.tridas.io.formats.oxford;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.oxford.OxfordToTridasDefaults.OxDefaultFields;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

public class OxfordWriter extends AbstractDendroCollectionWriter {

	private TridasToOxfordDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public OxfordWriter() {
		super(TridasToOxfordDefaults.class);
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
		return I18n.getText("oxford.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("oxford.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("oxford.about.shortName");
	}

	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException 
	{
	
		defaults = (TridasToOxfordDefaults) argDefaults;

		for (TridasObject o : argProject.getObjects()) 
		{
			TridasToOxfordDefaults objectDefaults = (TridasToOxfordDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			
			for (TridasElement e : TridasUtils.getElementList(o)) {
				TridasToOxfordDefaults elementDefaults = (TridasToOxfordDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				
				for (TridasSample s : e.getSamples()) {
					TridasToOxfordDefaults sampleDefaults = (TridasToOxfordDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToOxfordDefaults radiusDefaults = (TridasToOxfordDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							TridasToOxfordDefaults msDefaults = (TridasToOxfordDefaults) radiusDefaults
									.clone();
														
							try{
								msDefaults.populateFromTridasSeries(ms);		
							} catch (ConversionWarningException ex)
							{
								addWarning(ex.getWarning());
								continue;
							}
							
							try{
								OxfordFile file = createFileForValuesGroup(ms, msDefaults);
								addToFileList(file);
								naming.registerFile(file, argProject, o, e, s, r, ms);
							} catch (ConversionWarningException ex)
							{
								addWarning(ex.getWarning());
							}
						}
					}
				}
			}
		}
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) {
			TridasToOxfordDefaults dsDefaults = (TridasToOxfordDefaults) argDefaults
					.clone();
										
			try{
				dsDefaults.populateFromTridasSeries(ds);		
			} catch (ConversionWarningException ex)
			{
				addWarning(ex.getWarning());
				continue;
			}
			
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
					dsDefaults.populateFromTridasObject(parentObject);
				}

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
				
				TridasRadius parentRadius = (TridasRadius) TridasUtils.getEntityByIdentifier(argProject, id, TridasRadius.class);
				if(parentRadius!=null)
				{
					dsDefaults.populateFromTridasRadius(parentRadius);
				}
			}
			
			
			
			try{
				OxfordFile file = createFileForValuesGroup(ds, dsDefaults);
				addToFileList(file);
				naming.registerFile(file, argProject, ds);
			} catch (ConversionWarningException ex)
			{
				addWarning(ex.getWarning());
			}

		}
	}
	
	/**
	 * Create an OxfordFile based for a series and defaults
	 * @param ser
	 * @param def
	 * @return
	 * @throws ConversionWarningException
	 */
	private OxfordFile createFileForValuesGroup(ITridasSeries ser, TridasToOxfordDefaults def) 
						throws ConversionWarningException
	{
		for(TridasValues valuesGroup : ser.getValues())
		{	
			
			TridasToOxfordDefaults vDefaults = (TridasToOxfordDefaults) def.clone();
			
			// Check if variable is ok
			if(valuesGroup.isSetVariable())
			{
				if (valuesGroup.getVariable().isSetNormalTridas())
				{
					if(!valuesGroup.getVariable().getNormalTridas().equals(NormalTridasVariable.RING_WIDTH))
					{
						addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
								I18n.getText("fileio.unsupportedVariable", 
										valuesGroup.getVariable().getNormalTridas().value())));
						continue;
						
					}
				}
				else
				{
					addWarning(new ConversionWarning(WarningType.AMBIGUOUS, 
							I18n.getText("oxford.assumingRingWidths")));
				}
			}
			else
			{
				addWarning(new ConversionWarning(WarningType.AMBIGUOUS, 
						I18n.getText("oxford.assumingRingWidths")));
			}
			
			
			// Convert units to 1/100ths mm
			try {
				valuesGroup = UnitUtils.convertTridasValues(
						NormalTridasUnit.HUNDREDTH_MM, 
						valuesGroup, true);
			} catch (NumberFormatException ex) {
				throw new ConversionWarningException(
						new ConversionWarning(WarningType.UNREPRESENTABLE, 
								ex.getLocalizedMessage()));
			} catch (ConversionWarningException ex) {
				this.addWarning(ex.getWarning());
			}
			
			// Check values fit in three digits
			if(!UnitUtils.checkValuesFitInFields(valuesGroup, 3))
			{
				throw new ConversionWarningException(new ConversionWarning(WarningType.UNREPRESENTABLE,
						I18n.getText("oxford.valueTooLarge")));
			}
						
			vDefaults.getIntegerDefaultValue(OxDefaultFields.SERIESLENGTH)
					.setValue(valuesGroup.getValues().size());
			
			OxfordFile file = new OxfordFile(vDefaults, valuesGroup);
			return file;
		}
		
		throw new ConversionWarningException(new ConversionWarning(WarningType.UNREPRESENTABLE, 
				"No file created"));
	}
	
	@Override
	public DendroFileFilter getDendroFileFilter() {
		String[] exts = new String[] {"ddf"};
		
		return new DendroFileFilter(exts, getShortName());

	}

}
