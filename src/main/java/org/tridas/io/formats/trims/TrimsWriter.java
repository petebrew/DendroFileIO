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
package org.tridas.io.formats.trims;

import java.util.ArrayList;
import java.util.List;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class TrimsWriter extends AbstractDendroCollectionWriter {
	
	private TridasToTrimsDefaults defaults;
	INamingConvention naming = new NumericalNamingConvention();
	
	public TrimsWriter() {
		super(TridasToTrimsDefaults.class, new TrimsFormat());
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		defaults =  (TridasToTrimsDefaults) argDefaults;
			
		// Grab all derivedSeries from project
		try {
			List<TridasDerivedSeries> lst = argProject.getDerivedSeries();
			for (TridasDerivedSeries ds : lst) {
				
				
				if(!ds.isSetValues()) continue;
				if(!ds.getValues().get(0).isSetValues()) continue;
							
				TridasToTrimsDefaults def = (TridasToTrimsDefaults) defaults.clone();
				def.populateFromTridasDerivedSeries(ds);
				
				for(TridasValues values : ds.getValues())
				{
					if(!values.isSetValues() || values.getValues().size()==0)
					{
						this.addWarning(new ConversionWarning(WarningType.NULL_VALUE, I18n.getText("fileio.noData")));
						continue;
					}
					
					if(values.isSetVariable())
					{
						if(values.getVariable().isSetNormalTridas() && 
								values.getVariable().getNormalTridas().equals(NormalTridasVariable.RING_WIDTH))
						{
							// Great!
						}
						else if (values.getVariable().isSetNormalTridas())
						{
							this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, "TRIMS format does not support "+values.getVariable().getNormalTridas().toString().toLowerCase().replace("_", " ")+ " data."));
							continue;

						}
						else 
						{
							this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, "Standard data variable not specified in input file.  Assuming data is whole ring widths"));
						}
					}
					else
					{
						throw new ConversionWarningException(new ConversionWarning(WarningType.AMBIGUOUS, "Standard data variable not specified in input file.  Assuming data is whole ring widths"));

					}
					
					
					ArrayList<Integer> intvals = new ArrayList<Integer>();
					
					for(TridasValue datavals : values.getValues())
					{
						try{
							intvals.add(Integer.parseInt(datavals.getValue()));
						} catch (NumberFormatException e)
						{
							this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, "TRIMS format only supports numeric ring width data"));
							continue;
						}
					}
					
					// Create a TrimsFile for each and add to file list
					TrimsFile file = new TrimsFile(def, intvals);
					naming.registerFile(file, argProject, ds);
					addToFileList(file);
					
				}
				
				

			}
		} catch (NullPointerException e) {}
		
		// Loop through Objects
		List<TridasObject> obList;
		try {
			obList = argProject.getObjects();
		} catch (NullPointerException e) {
			throw new ImpossibleConversionException(I18n.getText("fileio.objectMissing"));
		}
		for (TridasObject obj : obList) {
			
			// Loop through Elements
			ArrayList<TridasElement> elList;
			try {
				elList = TridasUtils.getElementList(obj);
			} catch (NullPointerException e) {
				throw new ImpossibleConversionException(I18n.getText("fileio.elementMissing"));
			}
			
			for (TridasElement el : elList) {
				// Loop through Samples
				List<TridasSample> sList;
				try {
					sList = el.getSamples();
				} catch (NullPointerException e) {
					throw new ImpossibleConversionException(I18n.getText("fileio.sampleMissing"));
				}
				
				for (TridasSample s : sList) {
					// Check this isn't a placeholder
					/*TridasRadiusPlaceholder rph = null;
					try {
						rph = s.getRadiusPlaceholder();
					} catch (NullPointerException e) {}
					
					if (rph != null) {
						continue;
					}*/
					
					// Loop through radii
					List<TridasRadius> rList;
					try {
						rList = s.getRadiuses();
					} catch (NullPointerException e) {
						throw new ImpossibleConversionException(I18n.getText("fileio.radiusMissing"));
					}
					
					for (TridasRadius r : rList) {
						// Loop through series
						List<TridasMeasurementSeries> serList = null;
						try {
							serList = r.getMeasurementSeries();
						} catch (NullPointerException e) {}
						
						if (serList != null) {

							for (TridasMeasurementSeries ms : serList) 
							{
								
							
								if(!ms.isSetValues()) continue;
								if(!ms.getValues().get(0).isSetValues()) continue;
								
								if(!ms.isSetValues()) continue;
								if(!ms.getValues().get(0).isSetValues()) continue;
											
								TridasToTrimsDefaults def = (TridasToTrimsDefaults) defaults.clone();
								def.populateFromTridasMeasurementSeries(ms);
								
								for(TridasValues values : ms.getValues())
								{
									if(!values.isSetValues() || values.getValues().size()==0)
									{
										this.addWarning(new ConversionWarning(WarningType.NULL_VALUE, I18n.getText("fileio.noData")));
										continue;
									}
									
									if(values.isSetVariable())
									{
										if(values.getVariable().isSetNormalTridas() && 
												values.getVariable().getNormalTridas().equals(NormalTridasVariable.RING_WIDTH))
										{
											// Great!
										}
										else if (values.getVariable().isSetNormalTridas())
										{
											this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, "TRIMS format does not support "+values.getVariable().getNormalTridas().toString().toLowerCase().replace("_", " ")+ " data."));
											continue;

										}
										else 
										{
											this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, "Standard data variable not specified in input file.  Assuming data is whole ring widths"));
										}
									}
									else
									{
										throw new ConversionWarningException(new ConversionWarning(WarningType.AMBIGUOUS, "Standard data variable not specified in input file.  Assuming data is whole ring widths"));

									}
									
									
									ArrayList<Integer> intvals = new ArrayList<Integer>();
									
									for(TridasValue datavals : values.getValues())
									{
										try{
											intvals.add(Integer.parseInt(datavals.getValue()));
										} catch (NumberFormatException e)
										{
											this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, "TRIMS format only supports numeric ring width data"));
											continue;
										}
									}
								
								
								// Create a TrimsFile for each and add to file list
								TrimsFile file = new TrimsFile(def, intvals);
								naming.registerFile(file, argProject, obj, el, s, r, ms);
								addToFileList(file);
								}
							}
						}
					}
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
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
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
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		naming = argConvension;
	}
}
