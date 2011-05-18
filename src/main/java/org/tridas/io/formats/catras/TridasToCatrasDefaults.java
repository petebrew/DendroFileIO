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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASFileType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASLastRing;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASProtection;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASScope;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASSource;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASVariableType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToCatrasDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {
	private static final Logger log = LoggerFactory.getLogger(TridasToCatrasDefaults.class);
	
	@Override
	public void initDefaultValues() {
		setDefaultValue(DefaultFields.SERIES_NAME, new StringDefaultValue(I18n.getText("unnamed.series"), 32, 32));
		setDefaultValue(DefaultFields.SERIES_CODE, new StringDefaultValue("", 8, 8));
		setDefaultValue(DefaultFields.FILE_EXTENSION, new StringDefaultValue("CAT ", 4, 4 ));
		setDefaultValue(DefaultFields.SERIES_LENGTH, new IntegerDefaultValue(null, 0, 32767));
		getDefaultValue(DefaultFields.SERIES_LENGTH).setMaxLength(2);
		getDefaultValue(DefaultFields.SERIES_LENGTH).setMinLength(2);
		
		setDefaultValue(DefaultFields.SAPWOOD_LENGTH, new IntegerDefaultValue(null, 0, 32767));
		setDefaultValue(DefaultFields.FIRST_VALID_YEAR, new IntegerDefaultValue(null, 0, 32767));
		setDefaultValue(DefaultFields.LAST_VALID_YEAR, new IntegerDefaultValue(null, 0, 32767));
		setDefaultValue(DefaultFields.SCOPE, new GenericDefaultValue<CATRASScope>(CATRASScope.UNSPECIFIED));
		setDefaultValue(DefaultFields.LAST_RING, new GenericDefaultValue<CATRASLastRing>(CATRASLastRing.COMPLETE));
		setDefaultValue(DefaultFields.NUMBER_OF_CHARS_IN_TITLE, new IntegerDefaultValue(null, 0, 32));
		setDefaultValue(DefaultFields.QUALITY_CODE, new IntegerDefaultValue(0, 0, 5));
		setDefaultValue(DefaultFields.NUMBER_FORMAT, new IntegerDefaultValue(1, 1, 1));

		setDefaultValue(DefaultFields.START_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.END_YEAR, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.SPECIES_CODE, new IntegerDefaultValue(0, 0, 32767));
		setDefaultValue(DefaultFields.CREATION_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.UPDATED_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATED, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILE_TYPE, new GenericDefaultValue<CATRASFileType>(CATRASFileType.RAW));
		setDefaultValue(DefaultFields.USER_ID, new StringDefaultValue("----", 4, 4));
		setDefaultValue(DefaultFields.VARIABLE_TYPE, new GenericDefaultValue<CATRASVariableType>(CATRASVariableType.RINGWIDTH));
		setDefaultValue(DefaultFields.SOURCE, new GenericDefaultValue<CATRASSource>(CATRASSource.EXTERNAL));
		setDefaultValue(DefaultFields.PROTECTION, new GenericDefaultValue<CATRASProtection>(CATRASProtection.NONE));
	}
	
	
	public void populateFromTridasProject(TridasProject p) {


	}
	
	public void populateFromTridasObject(TridasObject o) {


	}
	
	public void populateFromTridasElement(TridasElement e) {

		
	}
	
	public void populateFromTridasSample(TridasSample s) {


	}
	
	public void populateFromTridasRadius(TridasRadius r) {


	}
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries ms) {
		populateFromTridasSeries(ms);
		
		GenericDefaultValue<CATRASFileType> fileTypeField = (GenericDefaultValue<CATRASFileType>) getDefaultValue(DefaultFields.FILE_TYPE);
		fileTypeField.setValue(CATRASFileType.RAW);
		
		if(ms.isSetDendrochronologist() && StringUtils.getIntialsFromName(ms.getDendrochronologist(), 4)!=null)
		{
			getStringDefaultValue(DefaultFields.USER_ID).setValue(StringUtils.getIntialsFromName(ms.getDendrochronologist(), 4));
		}

	}
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasDerivedSeries(TridasDerivedSeries ds) {
		
		populateFromTridasSeries(ds);
		GenericDefaultValue<CATRASFileType> fileTypeField = (GenericDefaultValue<CATRASFileType>) getDefaultValue(DefaultFields.FILE_TYPE);
		fileTypeField.setValue(CATRASFileType.CHRONOLOGY);
		
		if(ds.isSetAuthor() && StringUtils.getIntialsFromName(ds.getAuthor(), 4)!=null)
		{
			getStringDefaultValue(DefaultFields.USER_ID).setValue(StringUtils.getIntialsFromName(ds.getAuthor(), 4));
		}
	}
	

	private void populateFromTridasSeries(ITridasSeries ser){
		
		
		if (ser.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SERIES_NAME).setValue(ser.getTitle());
			getIntegerDefaultValue(DefaultFields.NUMBER_OF_CHARS_IN_TITLE).setValue(
					getStringDefaultValue(DefaultFields.SERIES_NAME).getStringValue().length());
		}
		
		if (ser.isSetIdentifier())
		{
			getStringDefaultValue(DefaultFields.SERIES_CODE).setValue(ser.getIdentifier().getValue());
		}
		else
		{
			getStringDefaultValue(DefaultFields.SERIES_CODE).setValue(ser.getTitle());
		}
		
		// Default to zero = undated
		if(ser.isSetInterpretation())
		{
			if(ser.getInterpretation().isSetFirstYear())
			{
				SafeIntYear firstYear = new SafeIntYear(ser.getInterpretation().getFirstYear());
				getSafeIntYearDefaultValue(DefaultFields.START_YEAR).setValue(firstYear);
			}
		}
		
		if(ser.isSetCreatedTimestamp())
		{			
			getDateTimeDefaultValue(DefaultFields.CREATION_DATE).setValue(ser.getCreatedTimestamp());		
		}
		
		if(ser.isSetLastModifiedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.UPDATED_DATE).setValue(ser.getLastModifiedTimestamp());
		}
		
	}
	

	
	@SuppressWarnings("unchecked")
	public void populateFromTridasValues(TridasValues argValues){
		
		getIntegerDefaultValue(DefaultFields.SERIES_LENGTH).setValue(argValues.getValues().size());
		
		GenericDefaultValue<CATRASVariableType> varField = (GenericDefaultValue<CATRASVariableType>) getDefaultValue(DefaultFields.VARIABLE_TYPE);
		varField.setValue(CATRASVariableType.RINGWIDTH);
		if(argValues.isSetVariable())
		{
			if(argValues.getVariable().isSetNormalTridas())
			{
				if(argValues.getVariable().getNormalTridas().equals(NormalTridasVariable.EARLYWOOD_WIDTH))
				{
					varField.setValue(CATRASVariableType.EARLYWOODWIDTH);
				}
				else if(argValues.getVariable().getNormalTridas().equals(NormalTridasVariable.LATEWOOD_WIDTH))
				{
					varField.setValue(CATRASVariableType.LATEWOODWIDTH);
				}
				else if(argValues.getVariable().getNormalTridas().equals(NormalTridasVariable.RING_WIDTH))
				{
					varField.setValue(CATRASVariableType.RINGWIDTH);
				}
				else
				{
					// Shouldn't get here
					log.warn("Unsupported variable type in TridasToCatrasDefaults");
				}
			}
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public void populateFromWoodCompleteness(TridasMeasurementSeries series, TridasRadius radius){
	
		TridasWoodCompleteness wc = null;
		TridasSapwood sapwood = new TridasSapwood();
		TridasBark bark = new TridasBark();
		TridasPith pith = new TridasPith();
		
		// Get the wood completeness from the series if possible, if not then try the radius
		if (series.isSetWoodCompleteness())
		{
			wc = series.getWoodCompleteness();
		}
		else if (radius.isSetWoodCompleteness())
		{
			wc = radius.getWoodCompleteness();
		}
		else
		{
			return;
		}
		
		if(wc.isSetSapwood())
		{
			sapwood =wc.getSapwood();
			if(sapwood.isSetNrOfSapwoodRings())
			{
				getIntegerDefaultValue(DefaultFields.SAPWOOD_LENGTH).setValue(wc.getSapwood().getNrOfSapwoodRings());
			}
		}
		
		if(wc.isSetBark())
		{
			bark = wc.getBark();
			
		}
	
		
		GenericDefaultValue<CATRASScope> scopeField = (GenericDefaultValue<CATRASScope>) getDefaultValue(DefaultFields.SCOPE);
		if(wc.isSetPith())
		{
			pith = wc.getPith();
			
			if(pith.getPresence().equals(ComplexPresenceAbsence.COMPLETE) || 
					pith.getPresence().equals(ComplexPresenceAbsence.INCOMPLETE))
			{

				if(bark.getPresence().equals(PresenceAbsence.PRESENT))
				{
					scopeField.setValue(CATRASScope.PITH_TO_BARK);
				}
				else if (sapwood.getPresence().equals(ComplexPresenceAbsence.COMPLETE) )
				{
					scopeField.setValue(CATRASScope.PITH_TO_WALDKANTE);
				}
				else
				{
					scopeField.setValue(CATRASScope.PITH);
				}
			}
			else if (bark.getPresence().equals(PresenceAbsence.PRESENT))
			{
				scopeField.setValue(CATRASScope.BARK);
			}
			else if (sapwood.getPresence().equals(ComplexPresenceAbsence.COMPLETE))
			{
				scopeField.setValue(CATRASScope.WALDKANTE);
			}
			
		}
		
		
		
		
	}
}
