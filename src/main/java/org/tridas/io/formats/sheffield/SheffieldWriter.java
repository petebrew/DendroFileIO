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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.DefaultFields;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldEdgeCode;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldPithCode;
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

public class SheffieldWriter extends AbstractDendroCollectionWriter {

	private static final Logger log = LoggerFactory.getLogger(SheffieldWriter.class);

	private TridasToSheffieldDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public SheffieldWriter() {
		super(TridasToSheffieldDefaults.class, new SheffieldFormat());
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
			throws ImpossibleConversionException {
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
							
							ArrayList<SheffieldFile> files = generateSheffieldFile(msDefaults, ms);
							
							if(files!=null && files.size()>0)
							{
								for(SheffieldFile file : files)
								{
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
		}
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) {
			TridasToSheffieldDefaults dsDefaults = (TridasToSheffieldDefaults) defaults.clone();
			dsDefaults.populateFromTridasDerivedSeries(ds);
			
			ArrayList<SheffieldFile> files = generateSheffieldFile(dsDefaults, ds);
			
			if(files!=null && files.size()>0)
			{
				for(SheffieldFile file : files)
				{
					// Set naming convention
					naming.registerFile(file, argProject, ds);
					
					// Add file to list
					addToFileList(file);
				}
			}
		}

		if(this.getFiles().length==0)
		{
			this.clearWarnings();
			throw new ImpossibleConversionException("File conversion failed.  This output format is unable to represent the data stored in the input file.");
		}
	}
	
	/**
	 * Generate an array of SheffieldFiles, one for each TridasValues group in a series
	 * 
	 * @param msDefaults
	 * @param series
	 * @return
	 * @throws ImpossibleConversionException
	 */
	private ArrayList<SheffieldFile> generateSheffieldFile(TridasToSheffieldDefaults msDefaults, ITridasSeries series) throws ImpossibleConversionException
	{
		ArrayList<SheffieldFile> files = new ArrayList<SheffieldFile>();
		
		if(!series.isSetValues()) return null;
		if(series.getValues().isEmpty()) return null;

		
		if(series.isSetInterpretation())
		{
			if(!series.getInterpretation().isSetDating())
			{
				this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, "No information on dating type given.  Assuming absolutely dated"));
			}
		}
		else
		{
			this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, "No information on dating type given.  Assuming absolutely dated"));
		}
					
		
		for (int i = 0; i < series.getValues().size(); i++) {
			boolean skipThisGroup = false;
			
			TridasValues tvsgroup = series.getValues().get(i);
			
			if(!tvsgroup.isSetValues()) continue;

			
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
			
			
			

			
			// Convert units and add data to file
			TridasValues theValues = null;
			try {
				theValues = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);
			} catch (NumberFormatException e1) {
				throw new ImpossibleConversionException(I18n.getText("general.ringValuesNotNumbers"));
			} catch (ConversionWarningException e1) {
				// Convertion failed so warn and stick with original values
				this.addWarning(e1.getWarning());
				theValues = tvsgroup;
			}
			
			// Remove unmeasured start rings
			Integer startRingsRemoved = 0;
			try
			{
				Iterator<TridasValue> it = theValues.getValues().iterator();
			    while (it.hasNext()) {
			    	TridasValue val = it.next();
			    	Double dblval = Double.parseDouble(val.getValue());
			        if (dblval.compareTo(0.0)<=0) {
			            it.remove();
			            startRingsRemoved++;
			            log.debug("Removing unmeasured rings at start of sequence");
			        }
			        else
			        {
			        	break;
			        }
			    }
			} catch (NumberFormatException ex) {} 

			if(startRingsRemoved>0)
			{
				GenericDefaultValue<SheffieldPithCode> pithCodeField = (GenericDefaultValue<SheffieldPithCode>) tvDefaults.getDefaultValue(DefaultFields.PITH_CODE);
				
				if(pithCodeField!=null)
				{
					tvDefaults.getStringDefaultValue(DefaultFields.INNER_RING_CODE).setValue(pithCodeField.getStringValue()+startRingsRemoved);									
				}
				else
				{
					tvDefaults.getStringDefaultValue(DefaultFields.INNER_RING_CODE).setValue("?"+startRingsRemoved);
				}
			}
			
		    
		    // Remove unmeasured end rings
			Integer endRingsRemoved = 0;
			try
			{
				ListIterator<TridasValue> it = theValues.getValues().listIterator(theValues.getValues().size());
			    while (it.hasPrevious()) {
			    	TridasValue val = it.previous();	
			    	Double dblval = Double.parseDouble(val.getValue());
			        if (dblval.compareTo(0.0)<=0) {
			        	
			            it.remove();
			            endRingsRemoved++;
			            log.debug("Removing unmeasured rings at end of sequence");
			        }
			        else
			        {
			        	break;
			        }
			    }
			} catch (NumberFormatException ex) {}
		    
			if(endRingsRemoved>0)
			{
				GenericDefaultValue<SheffieldEdgeCode> edgeCodeField = (GenericDefaultValue<SheffieldEdgeCode>) tvDefaults.getDefaultValue(DefaultFields.EDGE_CODE);
				
				if(edgeCodeField!=null)
				{
					tvDefaults.getStringDefaultValue(DefaultFields.OUTER_RING_CODE).setValue(edgeCodeField.getStringValue()+endRingsRemoved);									
				}
				else
				{
					tvDefaults.getStringDefaultValue(DefaultFields.OUTER_RING_CODE).setValue("U"+endRingsRemoved);
				}
			}
			
			
			// Fix ring count to account for rings being removed
			if(endRingsRemoved+startRingsRemoved > 0)
			{
				tvDefaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(
						tvDefaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()
						-endRingsRemoved
						-startRingsRemoved);
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
						tvDefaults.getBooleanDefaultValue(DefaultFields.WARN_MISSING_RINGS_FLAG).setValue(true);
					}
					else if (dblval.compareTo(0.0)<0)
					{
						val.setValue("1");
						this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
								I18n.getText("sheffield.negativeRingHandling")));
						tvDefaults.getBooleanDefaultValue(DefaultFields.WARN_MISSING_RINGS_FLAG).setValue(true);
					}
				}
				catch (Exception e2)
				{
					throw new ImpossibleConversionException(I18n.getText("general.ringValuesNotNumbers"));
				}
			}
			
			files.add(new SheffieldFile(tvDefaults, series, theValues));
							

		}
		
		return files;
		
	}
	
}
