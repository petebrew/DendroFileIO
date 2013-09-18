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
package org.tridas.io.formats.tucson;

import java.util.List;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.heidelberg.HeidelbergFile;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NamingConventionGrouper;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.TridasUtils;
import org.tridas.io.util.UnitUtils;
import org.tridas.io.util.YearRange;
import org.tridas.schema.NormalTridasDatingType;
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

/**
 * Writer for the Tucson file format.
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TucsonWriter extends AbstractDendroCollectionWriter {
	
	TridasToTucsonDefaults defaults;
	INamingConvention naming = new NumericalNamingConvention();
	protected boolean isstacked = true;
	
	
	/**
	 * Standard constructor
	 */
	public TucsonWriter() {
		super(TridasToTucsonDefaults.class, new TucsonFormat());
	}
	

	@Override
	public void parseTridasProject(TridasProject p, IMetadataFieldSet argDefaults) 
	throws IncompleteTridasDataException {
	
		// Base defaults for all the output files
		defaults = (TridasToTucsonDefaults) argDefaults;
		
		// Set project level fields
		defaults.populateFromTridasProject(p);
				
		// Extract any TridasDerivedSeries from project
		List<TridasDerivedSeries> dsList = null;
		try { dsList = p.getDerivedSeries();
		} catch (NullPointerException e) {}
				
		
		TucsonFile file = new TucsonFile(defaults);
		NamingConventionGrouper ncgroup = new NamingConventionGrouper();
		ncgroup.add(p);
		
		
		if (dsList!=null && dsList.size()>0) 
		{
			/**
			 * CHRONOLOGY FILE
			 *
			 * There is a derived series in this project so we will be creating a .crn file.
			 * 
			 */
		
			for (TridasDerivedSeries ds : dsList) 
			{
				
				for(TridasValues  tvs: ds.getValues())
				{
					TridasToTucsonDefaults dft = (TridasToTucsonDefaults) defaults.clone();
					try {
						ds.getValues().set(0, UnitUtils.convertTridasValues(getOutputUnits(tvs), ds.getValues().get(0), true));
					} catch (NumberFormatException e) {
						this.addWarning(new ConversionWarning(WarningType.INVALID, e.getMessage()));
						continue;
					} catch (ConversionWarningException e) {
						this.addWarning(e.getWarning());
					}
					
					// Check that the range does not go outside that which Tucson format is capable of storing
					YearRange thisSeriesRange = new YearRange(ds);
					if (SafeIntYear.min(thisSeriesRange.getStart(), new SafeIntYear(-1001)) == thisSeriesRange.getStart()) 
					{
						// Series with data before 1000BC cannot be saved
						addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
								I18n.getText("tucson.before1000BC", ds.getTitle())));
						continue;
					}
					else
					{
						// Range ok so create file and add series
						
						// Try and grab object, element, sample and radius info from linked series
						if(ds.isSetLinkSeries())
						{
							// TODO what happens if there are links to multiple different entities?
							// For now just go with the first link
							//if(ds.getLinkSeries().getSeries().size()>1) break;
							TridasIdentifier id = ds.getLinkSeries().getSeries().get(0).getIdentifier();
							
							TridasObject parentObject = (TridasObject) TridasUtils.getEntityByIdentifier(p, id, TridasObject.class);
							if(parentObject!=null)
							{
								dft.populateFromTridasObject(parentObject);
							}
	
							TridasElement parentElement = (TridasElement) TridasUtils.getEntityByIdentifier(p, id, TridasElement.class);
							if(parentElement!=null)
							{
								dft.populateFromTridasElement(parentElement);
							}
	
						}
						
						
						if(!isstacked) 
						{		
							dft.populateFromTridasDerivedSeries(ds);
							file = new TucsonFile(defaults);
							file.addSeries(ds, tvs, dft);
							
							naming.registerFile(file, p, ds);
							addToFileList(file);
						}
						else
						{
							file.addSeries(ds, tvs, dft);
							naming.registerFile(file, ncgroup);
						}
						
						

					}
				}
			}
			
		}
		
		if (TridasUtils.getMeasurementSeriesFromTridasProject(p).size()>0)
		{
			/**
			 * RWL FILE(S)
			 * 
			 * The project contains one or more measurement series so we will save these
			 * to one or more RWL files.  RWL files should contain only one batch of metadata, 
			 * so we create a new file for each object.
			 */
			
			for (TridasObject o : p.getObjects()) 
			{
				
				// Clone defaults and set fields specific to this object
				TridasToTucsonDefaults objectDefaults = (TridasToTucsonDefaults) defaults.clone();
				objectDefaults.populateFromTridasObject(o);
				
				file = null;
				
				for (TridasElement e : TridasUtils.getElementList(o)) {
					TridasToTucsonDefaults elementDefaults = (TridasToTucsonDefaults) objectDefaults.clone();
					elementDefaults.populateFromTridasElement(e);
					
					for (TridasSample s : e.getSamples()) {
						
						for (TridasRadius r : s.getRadiuses()) {
							
							for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
								TridasToTucsonDefaults msDefaults = (TridasToTucsonDefaults) elementDefaults
										.clone();
								msDefaults.populateFromTridasMeasurementSeries(ms);
								
								if(ms.isSetInterpretation())
								{
									if(ms.getInterpretation().isSetDating())
									{
										if(ms.getInterpretation().getDating().getType().equals(NormalTridasDatingType.RELATIVE))
										{
											this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS,
													I18n.getText("tucson.relativeDates")));
										}
									}
								}
								
								for (int i = 0; i < ms.getValues().size(); i++) {
									TridasValues tvs = ms.getValues().get(i);
	
									try {
										ms.getValues().set(i, UnitUtils.convertTridasValues(getOutputUnits(tvs), ms.getValues().get(i), true));
									} catch (NumberFormatException e1) {
									} catch (ConversionWarningException e1) {
										this.addWarning(e1.getWarning());
									}
																		
									//TridasToTucsonDefaults tvDefaults = new TridasToTucsonDefaults();
									TridasToTucsonDefaults tvDefaults = (TridasToTucsonDefaults) msDefaults.clone();

									// Check that the range does not go outside that which Tucson format is capable of storing
									YearRange thisSeriesRange = new YearRange(ms);
									if (SafeIntYear.min(thisSeriesRange.getStart(), new SafeIntYear(-1001)) == thisSeriesRange.getStart()) 
									{
										// Series with data before 1000BC cannot be saved
										addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
												I18n.getText("tucson.before1000BC", ms.getTitle())));
										continue;
									}
									else
									{
										tvDefaults.populateFromTridasMeasurementSeries(ms);
										
										// Range ok so create file and add series
										if(file==null)
										{
											file = new TucsonFile(tvDefaults);
										}
										
										if(!isstacked) 
										{
											file = new TucsonFile(tvDefaults);
											file.addSeries(ms, tvs, tvDefaults);
											naming.registerFile(file, p, o, e, s, r, ms, tvs);
											addToFileList(file);
											file = new TucsonFile(defaults);
										}
										else
										{
											file.addSeries(ms, tvs, tvDefaults);
											naming.registerFile(file, ncgroup);
										}																			
									}
								}
							}
						}
					}
				}
				naming.registerFile(file, p);

				if(file.getSeries().length>0)
				{
					addToFileList(file);
				}
			}
		}
	}
	

	/**
	 * Get the NormalTridasUnits for this TridasValues block.  Tucson can represent 1/100th mm
	 * or micrometres.  If its any other sort of variable being stored, just return without
	 * handling units.
	 * 
	 * 
	 * @param tvs
	 * @return
	 */
	private NormalTridasUnit getOutputUnits(TridasValues tvs)
	{
		if(tvs.getVariable().getNormalTridas()!=null)
		{
			switch (tvs.getVariable().getNormalTridas())
			{
			case RING_WIDTH:
			case LATEWOOD_WIDTH:
			case EARLYWOOD_WIDTH:
				if(tvs.getUnit() != null && tvs.getUnit().getNormalTridas()!=null)
				{
					if (tvs.getUnit().getNormalTridas().equals(NormalTridasUnit.HUNDREDTH_MM))
					{
						return NormalTridasUnit.HUNDREDTH_MM;
					}
					else
					{
						return NormalTridasUnit.MICROMETRES;
					}
				}
				else
				{
					return null;
				}
			default: 
				break;
				
			}
		}
		
		addWarning(new ConversionWarning(WarningType.NOT_STRICT, I18n
						.getText("tucson.unknownVariableUnitsUnhandled")));
		return null;
		
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
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
}
