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
package org.tridas.io.formats.catras;

import java.util.ArrayList;

import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.sheffield.SheffieldToTridasDefaults.DefaultFields;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class CatrasToTridasDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet {

	public static enum DefaultFields {
		SERIES_NAME,
		SERIES_CODE,
	    FILE_EXTENSION,
		SERIES_LENGTH,
		SAPWOOD_LENGTH,
		START_YEAR,
		END_YEAR,
		SPECIES_CODE,
		CREATION_DATE,
		UPDATED_DATE,
		SAPWOOD,
		DATED,
		TYPE,
		USER_ID;
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SERIES_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILE_EXTENSION, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_LENGTH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD_LENGTH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.START_YEAR, new SafeIntYearDefaultValue(null));
		setDefaultValue(DefaultFields.END_YEAR, new SafeIntYearDefaultValue(null));
		setDefaultValue(DefaultFields.SPECIES_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.CREATION_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.UPDATED_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATED, new StringDefaultValue());
		setDefaultValue(DefaultFields.TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.USER_ID, new StringDefaultValue());

		
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		
		if(getStringDefaultValue(DefaultFields.SPECIES_CODE).getValue()!=null)
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("catras.labSpecificSpeciesCode");
			gf.setType("xs:string");
			gf.setValue(getStringDefaultValue(DefaultFields.SPECIES_CODE).getValue());
			ArrayList<TridasGenericField> gfList = new ArrayList<TridasGenericField>();
			gfList.add(gf);
			e.setGenericFields(gfList);
		}
		
		return e;
	
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();

		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(getStringDefaultValue(DefaultFields.SERIES_CODE).getStringValue());
		seriesId.setDomain(super.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		series.setIdentifier(seriesId);
		
		// Title 
		if(getStringDefaultValue(DefaultFields.SERIES_NAME).getStringValue()!=null)
		{
			series.setTitle(getStringDefaultValue(DefaultFields.SERIES_NAME).getStringValue());
		}
		
		// Creation date
		if(getDateTimeDefaultValue(DefaultFields.CREATION_DATE).getValue()!=null)
		{
			series.setCreatedTimestamp(getDateTimeDefaultValue(DefaultFields.CREATION_DATE).getValue());
		}
		
		// Last modified date
		if(getDateTimeDefaultValue(DefaultFields.UPDATED_DATE).getValue()!=null)
		{
			series.setLastModifiedTimestamp(getDateTimeDefaultValue(DefaultFields.UPDATED_DATE).getValue());
		}
		
		// Build interpretation group for series
		TridasInterpretation interp = new TridasInterpretation();
		
		// Start and End Years
		if(getSafeIntYearDefaultValue(DefaultFields.START_YEAR).getValue()!=null)
		{
			TridasDating dating = new TridasDating();
			if(getSafeIntYearDefaultValue(DefaultFields.START_YEAR).getValue().equals(new SafeIntYear(-1)))
			{
				//dating.setType(NormalTridasDatingType.RELATIVE);
			}
			else
			{
				dating.setType(NormalTridasDatingType.ABSOLUTE);
				interp.setFirstYear(getSafeIntYearDefaultValue(DefaultFields.START_YEAR).getValue().toTridasYear(DatingSuffix.AD));
				
				// End Year
				if(getSafeIntYearDefaultValue(DefaultFields.END_YEAR).getValue()!=null)
				{
					interp.setLastYear(getSafeIntYearDefaultValue(DefaultFields.END_YEAR).getValue().toTridasYear(DatingSuffix.AD));
				}
				interp.setDating(dating);
			}
			
		}
	
		series.setInterpretation(interp);
		
		series.setDendrochronologist(getStringDefaultValue(DefaultFields.USER_ID).getStringValue());
		
		return series;
	}
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
		
		// Set units to 1/100th mm. Is this always the case?
		TridasUnit units = new TridasUnit();
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		valuesGroup.setUnit(units);
		
		// Set variable to ringwidth.  Is this always the case?
		TridasVariable variable = new TridasVariable();
		variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		valuesGroup.setVariable(variable);
		

		return valuesGroup;
	}
}
