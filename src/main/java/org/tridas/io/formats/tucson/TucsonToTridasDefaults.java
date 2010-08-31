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

import java.util.ArrayList;
import java.util.UUID;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasurementSeriesPlaceholder;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

/**
 * here for the library user to create and pass in the loadFile() arguments
 * 
 * @author Daniel
 */
public class TucsonToTridasDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet {
	
	public enum TucsonDefaultField {
		SITE_CODE, SITE_NAME, SPECIES_CODE, SPECIES_NAME, INVESTIGATOR, ELEVATION, LATLONG, 
		STATE_COUNTRY, COMP_DATE, UNITS, VARIABLE, SERIES_CODE, FIRST_YEAR, LAST_YEAR;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(TucsonDefaultField.SITE_CODE, new StringDefaultValue(UUID.randomUUID().toString()));
		setDefaultValue(TucsonDefaultField.SITE_NAME, new StringDefaultValue(I18n.getText("unnamed.object"), 50, 50));
		setDefaultValue(TucsonDefaultField.SPECIES_CODE, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.SPECIES_NAME, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.INVESTIGATOR, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.ELEVATION, new DoubleDefaultValue());
		setDefaultValue(TucsonDefaultField.LATLONG, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.STATE_COUNTRY, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.COMP_DATE, new DateTimeDefaultValue());
		setDefaultValue(TucsonDefaultField.UNITS, new GenericDefaultValue<NormalTridasUnit>());
		setDefaultValue(TucsonDefaultField.VARIABLE, new GenericDefaultValue<NormalTridasVariable>());
		setDefaultValue(TucsonDefaultField.SERIES_CODE, new StringDefaultValue(UUID.randomUUID().toString()));
		setDefaultValue(TucsonDefaultField.FIRST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(TucsonDefaultField.LAST_YEAR, new SafeIntYearDefaultValue());		
	}

	
	@Override
	public TridasProject getDefaultTridasProject()
	{
		TridasProject p = super.getDefaultTridasProject();
		
		// Investigator
		if(getStringDefaultValue(TucsonDefaultField.INVESTIGATOR).getValue()!=null)
		{
			p.setInvestigator(getStringDefaultValue(TucsonDefaultField.INVESTIGATOR).getValue().trim());
		}
		
		return p;
		
	}
	
	
	@Override
	public TridasObject getDefaultTridasObject()
	{
		TridasObject o = super.getDefaultTridasObject();
		
		// Object code and title
		o.setTitle(getStringDefaultValue(TucsonDefaultField.SITE_NAME).getValue().trim());
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(getStringDefaultValue(TucsonDefaultField.SITE_CODE).getValue());
		o.setIdentifier(id);
		
		// Lat Long
		ArrayList<TridasGenericField> genericFields = new ArrayList<TridasGenericField>();
		
		if (getStringDefaultValue(TucsonDefaultField.LATLONG).getValue() != null) {
			TridasGenericField coords = new TridasGenericField();
			coords.setName("tucson.Coords");
			coords.setType("xs:string");
			coords.setValue(getStringDefaultValue(TucsonDefaultField.LATLONG).getValue());
			genericFields.add(coords);
		}
		
		// State / Country
		if (getStringDefaultValue(TucsonDefaultField.STATE_COUNTRY).getValue() != null) {
			TridasGenericField country = new TridasGenericField();
			country.setName("tucson.StateOrCountry");
			country.setType("xs:string");
			country.setValue(getStringDefaultValue(TucsonDefaultField.STATE_COUNTRY).getValue().trim());
			genericFields.add(country);
		}
	
		o.setGenericFields(genericFields);
		
		
		return o;
		
	}
	
	
	@Override
	public TridasElement getDefaultTridasElement()
	{
		TridasElement e = super.getDefaultTridasElement();
		
		// Taxon
		if (getStringDefaultValue(TucsonDefaultField.SPECIES_CODE).getValue() != null) {
			ControlledVoc taxon = ITRDBTaxonConverter.getControlledVocFromCode(getStringDefaultValue(
					TucsonDefaultField.SPECIES_CODE).getValue());
			e.setTaxon(taxon);
		}
		else 
		{
			ControlledVoc taxon = ITRDBTaxonConverter.getControlledVocFromCode(getStringDefaultValue(
					TucsonDefaultField.SPECIES_NAME).getValue());
			e.setTaxon(taxon);
		}

		
		// Elevation
		if(getDoubleDefaultValue(TucsonDefaultField.ELEVATION).getValue()!=null)
		{
			e.setAltitude(getDoubleDefaultValue(TucsonDefaultField.ELEVATION).getValue());
		}
		

		
		return e;
		
	}
	
	@Override
	public TridasSample getDefaultTridasSample()
	{
		TridasSample s = super.getDefaultTridasSample();
		
		return s;
		
	}
	
	@Override
	public TridasRadius getDefaultTridasRadius()
	{
		TridasRadius r = super.getDefaultTridasRadius();
		
		return r;
		
	}
	
	@Override
	public TridasMeasurementSeries getDefaultTridasMeasurementSeries()
	{
		TridasMeasurementSeries ms = super.getDefaultTridasMeasurementSeries();
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(getStringDefaultValue(TucsonDefaultField.SERIES_CODE).getValue());
		ms.setIdentifier(id);
		ms.setTitle(getStringDefaultValue(TucsonDefaultField.SERIES_CODE).getValue());
		// Investigator
		if(getStringDefaultValue(TucsonDefaultField.INVESTIGATOR).getValue()!=null)
		{
			ms.setDendrochronologist(getStringDefaultValue(TucsonDefaultField.INVESTIGATOR).getValue().trim());
		}
		
		TridasInterpretation interp = new TridasInterpretation();
		
		if(getSafeIntYearDefaultValue(TucsonDefaultField.FIRST_YEAR).getValue()!=null)
		{
			interp.setFirstYear(getSafeIntYearDefaultValue(TucsonDefaultField.FIRST_YEAR).getValue().toTridasYear(DatingSuffix.AD));
		}
		ms.setInterpretation(interp);
		
		return ms;
		
	}
	
	@Override
	public TridasDerivedSeries getDefaultTridasDerivedSeries()
	{
		TridasDerivedSeries ds = super.getDefaultTridasDerivedSeries();
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(getStringDefaultValue(TucsonDefaultField.SERIES_CODE).getValue());
		ds.setIdentifier(id);
		ds.setTitle(getStringDefaultValue(TucsonDefaultField.SERIES_CODE).getValue());
		// Investigator
		if(getStringDefaultValue(TucsonDefaultField.INVESTIGATOR).getValue()!=null)
		{
			ds.setAuthor(getStringDefaultValue(TucsonDefaultField.INVESTIGATOR).getValue().trim());
		}
		return ds;
		
	}
	
	public TridasMeasurementSeriesPlaceholder getDefaultTridasMeasurementSeriesPlaceholder()
	{
		TridasMeasurementSeriesPlaceholder msph = new TridasMeasurementSeriesPlaceholder();				
		msph.setId("XREF-" + UUID.randomUUID().toString());
		return msph;
	}
	
	public TridasValues getDefaultTridasValues()
	{		
		TridasValues values = new TridasValues();
		TridasVariable variable = new TridasVariable();
		
		// Variable
		if(getDefaultValue(TucsonDefaultField.VARIABLE).getValue()!=null)
		{	
			variable.setNormalTridas((NormalTridasVariable) getDefaultValue(TucsonDefaultField.VARIABLE).getValue());
		}
		else
		{
			variable.setValue(I18n.getText("unknown"));
		}
		values.setVariable(variable);
		
		// Units
		TridasUnit unit = new TridasUnit();
		if (getDefaultValue(TucsonDefaultField.UNITS).getValue()!=null)
		{
			unit.setNormalTridas((NormalTridasUnit) getDefaultValue(TucsonDefaultField.UNITS).getValue());
			values.setUnit(unit);
		}
		else
		{		
			values.setUnitless(new TridasUnitless());
		}

		return values;
		
	}
	
}
