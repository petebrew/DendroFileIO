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
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLastRingUnderBark;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasuringMethod;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;

public class CatrasToTridasDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet {

	public static enum DefaultFields {
		SERIES_NAME,
		SERIES_CODE,
	    FILE_EXTENSION,
		SERIES_LENGTH,
		SAPWOOD_LENGTH,
		FIRST_VALID_YEAR,
		LAST_VALID_YEAR,
		SCOPE,
		LAST_RING,
		NUMBER_OF_CHARS_IN_TITLE,
		QUALITY_CODE,
		NUMBER_FORMAT,
		START_YEAR,
		END_YEAR,
		SPECIES_CODE,
		CREATION_DATE,
		UPDATED_DATE,
		SAPWOOD,
		DATED,
		FILE_TYPE,
		USER_ID,
		VARIABLE_TYPE,
		SOURCE,
		PROTECTION;
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SERIES_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILE_EXTENSION, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_LENGTH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD_LENGTH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.FIRST_VALID_YEAR, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.LAST_VALID_YEAR, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SCOPE, new GenericDefaultValue<CATRASScope>(CATRASScope.UNSPECIFIED));
		setDefaultValue(DefaultFields.LAST_RING, new GenericDefaultValue<CATRASLastRing>());
		setDefaultValue(DefaultFields.NUMBER_OF_CHARS_IN_TITLE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.QUALITY_CODE, new IntegerDefaultValue());
				
		setDefaultValue(DefaultFields.START_YEAR, new SafeIntYearDefaultValue(null));
		setDefaultValue(DefaultFields.END_YEAR, new SafeIntYearDefaultValue(null));
		setDefaultValue(DefaultFields.SPECIES_CODE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.CREATION_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.UPDATED_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.NUMBER_FORMAT, new IntegerDefaultValue(1, 1, 1));

		setDefaultValue(DefaultFields.SAPWOOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATED, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILE_TYPE, new GenericDefaultValue<CATRASFileType>());
		setDefaultValue(DefaultFields.USER_ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.VARIABLE_TYPE, new GenericDefaultValue<CATRASVariableType>());
		setDefaultValue(DefaultFields.SOURCE, new GenericDefaultValue<CATRASSource>());
		setDefaultValue(DefaultFields.PROTECTION, new GenericDefaultValue<CATRASProtection>());


		
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasSample()
	 */
	@Override
	protected TridasSample getDefaultTridasSample() {
		
		TridasSample sample = super.getDefaultTridasSample();
		TridasIdentifier id = new TridasIdentifier();
		
		if(sample.isSetIdentifier())
		{
			id = sample.getIdentifier();
		}
		else
		{
			id.setDomain(I18n.getText("domain.value"));
			id.setValue(UUID.randomUUID().toString());
		}
		
		sample.setIdentifier(id);
		
		
		return sample;
		
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		
		if(getIntegerDefaultValue(DefaultFields.SPECIES_CODE).getValue()!=null)
		{
			ControlledVoc taxon = new ControlledVoc();
			taxon.setNormalStd("CATRAS");
			taxon.setNormalId(getIntegerDefaultValue(DefaultFields.SPECIES_CODE).getValue().toString());			
			taxon.setNormal("Unknown");
			e.setTaxon(taxon);
			
			TridasGenericField gf = new TridasGenericField();
			gf.setName("catras.labSpecificSpeciesCode");
			gf.setType("xs:int");
			gf.setValue(getIntegerDefaultValue(DefaultFields.SPECIES_CODE).getValue().toString());
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
		populateTridasSeries(series);
		
		series.setDendrochronologist(getStringDefaultValue(DefaultFields.USER_ID).getStringValue());
		
		TridasWoodCompleteness wc = new TridasWoodCompleteness();
		TridasSapwood sap = new TridasSapwood();
		sap.setPresence(ComplexPresenceAbsence.UNKNOWN);
		TridasHeartwood hw = new TridasHeartwood();
		hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
		TridasPith pith = new TridasPith();
		pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
		TridasBark bark = new TridasBark();
		bark.setPresence(PresenceAbsence.UNKNOWN);
		
		GenericDefaultValue<CATRASScope> scopeField = (GenericDefaultValue<CATRASScope>) getDefaultValue(DefaultFields.SCOPE);
		if(scopeField.getValue()!=null)
		{
			if(scopeField.getValue().equals(CATRASScope.BARK))
			{
				bark.setPresence(PresenceAbsence.PRESENT);
				pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
				sap.setPresence(ComplexPresenceAbsence.COMPLETE);
				hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
			}
			else if(scopeField.getValue().equals(CATRASScope.PITH))
			{
				bark.setPresence(PresenceAbsence.ABSENT);
				pith.setPresence(ComplexPresenceAbsence.COMPLETE);
				sap.setPresence(ComplexPresenceAbsence.UNKNOWN);
				hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
			}
			else if(scopeField.getValue().equals(CATRASScope.PITH_TO_BARK))
			{
				bark.setPresence(PresenceAbsence.PRESENT);
				pith.setPresence(ComplexPresenceAbsence.COMPLETE);
				sap.setPresence(ComplexPresenceAbsence.COMPLETE);
				hw.setPresence(ComplexPresenceAbsence.COMPLETE);
			}
			else if(scopeField.getValue().equals(CATRASScope.PITH_TO_WALDKANTE))
			{
				bark.setPresence(PresenceAbsence.ABSENT);
				pith.setPresence(ComplexPresenceAbsence.COMPLETE);
				sap.setPresence(ComplexPresenceAbsence.COMPLETE);
				hw.setPresence(ComplexPresenceAbsence.COMPLETE);
			}
			else if(scopeField.getValue().equals(CATRASScope.UNSPECIFIED))
			{
				bark.setPresence(PresenceAbsence.UNKNOWN);
				pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
				sap.setPresence(ComplexPresenceAbsence.UNKNOWN);
				hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
			}
			else if(scopeField.getValue().equals(CATRASScope.WALDKANTE))
			{
				bark.setPresence(PresenceAbsence.ABSENT);
				pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
				sap.setPresence(ComplexPresenceAbsence.COMPLETE);
				hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
			}
		}
		
		if(getIntegerDefaultValue(DefaultFields.SAPWOOD_LENGTH).getValue()!=null)
		{
			sap.setNrOfSapwoodRings(getIntegerDefaultValue(DefaultFields.SAPWOOD_LENGTH).getValue());
		}
		
		GenericDefaultValue<CATRASLastRing> lastRingField = (GenericDefaultValue<CATRASLastRing>) getDefaultValue(DefaultFields.LAST_RING);
		if(lastRingField.getValue()!=null)
		{
			
			TridasLastRingUnderBark lrub = new TridasLastRingUnderBark();
			lrub.setPresence(PresenceAbsence.PRESENT);
			sap.setLastRingUnderBark(lrub);
			sap.setPresence(ComplexPresenceAbsence.COMPLETE);
			
			if (lastRingField.equals(CATRASLastRing.EARLYWOOD))
			{
				lrub.setContent("earlywood");
			}
			else
			{
				lrub.setContent("complete");
			}
		}
		else
		{
			series.getWoodCompleteness().getSapwood().getLastRingUnderBark().setContent("Unknown");
		}
		
		wc.setSapwood(sap);
		wc.setHeartwood(hw);
		wc.setPith(pith);
		wc.setBark(bark);
		
		
		series.setWoodCompleteness(wc);
		
		GenericDefaultValue<CATRASSource> sourceField = (GenericDefaultValue<CATRASSource>) getDefaultValue(DefaultFields.SOURCE);
		if(sourceField.getValue()!=null)
		{
			if(sourceField.getValue().equals(CATRASSource.MANUAL) || sourceField.getValue().equals(CATRASSource.EXTERNAL))
			{
				TridasMeasuringMethod mm = series.getMeasuringMethod();
				mm.setNormalTridas(null);
				mm.setNormalStd("CATRAS");
				mm.setNormalId(sourceField.getValue().toCode());
				mm.setNormal(sourceField.getValue().toString());
				mm.setValue(sourceField.getValue().toString());
			}
		}
		
		
		TridasGenericField gf = new TridasGenericField();
		gf.setName("CATRAS.LastRing");
		gf.setType("xs:string");
		gf.setValue(lastRingField.getStringValue());
		series.getGenericFields().add(gf);
		
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
		
		GenericDefaultValue<CATRASVariableType> varField = (GenericDefaultValue<CATRASVariableType>) getDefaultValue(DefaultFields.VARIABLE_TYPE);
		if(varField.getValue().equals(CATRASVariableType.RINGWIDTH))
		{
			variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		}
		else if (varField.getValue().equals(CATRASVariableType.EARLYWOODWIDTH))
		{
			variable.setNormalTridas(NormalTridasVariable.EARLYWOOD_WIDTH);
		}
		else if (varField.getValue().equals(CATRASVariableType.LATEWOODWIDTH))
		{
			variable.setNormalTridas(NormalTridasVariable.LATEWOOD_WIDTH);
		}
		
		
		valuesGroup.setVariable(variable);
		

		return valuesGroup;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasDerivedSeries()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
	
		TridasDerivedSeries series = super.getDefaultTridasDerivedSeries();
		
		populateTridasSeries(series);
		
		// Try to determine what sort of series this is
		GenericDefaultValue<CATRASFileType> fileTypeField = (GenericDefaultValue<CATRASFileType>) getDefaultValue(DefaultFields.FILE_TYPE);
		if(fileTypeField.getValue()!=null)
		{
			ControlledVoc chronotype = new ControlledVoc();
			chronotype.setLang("en");
			chronotype.setNormal(fileTypeField.getValue().toString().replace("_", " "));
			chronotype.setNormalId(fileTypeField.getValue().toCode().toString());
			chronotype.setNormalStd("CATRAS");
			series.setType(chronotype);
		}
		
		
		return series;
	}
	
	@SuppressWarnings("unchecked")
	private ITridasSeries populateTridasSeries(ITridasSeries series)
	{
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
			if(getSafeIntYearDefaultValue(DefaultFields.START_YEAR).getValue().equals(new SafeIntYear(0)))
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
		
		GenericDefaultValue<CATRASProtection> protectionField = (GenericDefaultValue<CATRASProtection>) getDefaultValue(DefaultFields.PROTECTION);
		TridasGenericField gf2 = new TridasGenericField();
		gf2.setName("CATRAS.Protection");
		gf2.setType("xs:string");
		gf2.setValue(protectionField.getStringValue());
		series.getGenericFields().add(gf2);
		
		if(getIntegerDefaultValue(DefaultFields.FIRST_VALID_YEAR).getValue()>0)
		{
			TridasGenericField gf3 = new TridasGenericField();
			gf3.setName("CATRAS.FirstValidRing");
			gf3.setType("xs:int");
			gf3.setValue(getIntegerDefaultValue(DefaultFields.FIRST_VALID_YEAR).getStringValue());
			series.getGenericFields().add(gf3);
		}
		
		if(getIntegerDefaultValue(DefaultFields.LAST_VALID_YEAR).getValue()>0)
		{
			TridasGenericField gf4 = new TridasGenericField();
			gf4.setName("CATRAS.LastValidRing");
			gf4.setType("xs:int");
			gf4.setValue(getIntegerDefaultValue(DefaultFields.LAST_VALID_YEAR).getStringValue());
			series.getGenericFields().add(gf4);		
		}
		
		if(getIntegerDefaultValue(DefaultFields.QUALITY_CODE).getValue()!=null)
		{
			TridasGenericField gf5 = new TridasGenericField();
			gf5.setName("CATRAS.QualityCode");
			gf5.setType("xs:int");
			gf5.setValue(getIntegerDefaultValue(DefaultFields.QUALITY_CODE).getStringValue());
			series.getGenericFields().add(gf5);
		}
				
		return series;
	}


	public enum CATRASScope {
		UNSPECIFIED(0),
		PITH(1), 
		WALDKANTE(2), 
		PITH_TO_WALDKANTE(3), 
		BARK(4),
		PITH_TO_BARK(5);		
		
		private Integer code;
		
		CATRASScope(Integer c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase());
		}
		
		public final Integer toCode() {
			return code;
		}
		
		public static CATRASScope fromCode(Integer code) {
			for (CATRASScope val : CATRASScope.values()) {
				if (val.toCode().equals(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CATRASLastRing {
		COMPLETE(0), 
		EARLYWOOD(1);	
		
		private Integer code;
		
		CATRASLastRing(Integer c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase());
		}
		
		public final Integer toCode() {
			return code;
		}
		
		public static CATRASLastRing fromCode(Integer code) {
			for (CATRASLastRing val : CATRASLastRing.values()) {
				if (val.toCode().equals(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CATRASVariableType {
		RINGWIDTH(0), 
		EARLYWOODWIDTH(1),
		LATEWOODWIDTH(2);
		
		private Integer code;
		
		CATRASVariableType(Integer c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase());
		}
		
		public final Integer toCode() {
			return code;
		}
		
		public static CATRASVariableType fromCode(Integer code) {
			for (CATRASVariableType val : CATRASVariableType.values()) {
				if (val.toCode().equals(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CATRASSource {
		AVERAGED("A"), 
		DIGITIZED("D"),
		EXTERNAL("E"),
		MANUAL("H");
		
		private String code;
		
		CATRASSource(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase());
		}
		
		public final String toCode() {
			return code;
		}
		
		public static CATRASSource fromCode(String code) {
			for (CATRASSource val : CATRASSource.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CATRASProtection {
		NONE(0), 
		NOT_TO_BE_DELETED(1),
		NOT_TO_BE_AMENDED(2);
		
		private Integer code;
		
		CATRASProtection(Integer c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase());
		}
		
		public final Integer toCode() {
			return code;
		}
		
		public static CATRASProtection fromCode(Integer code) {
			for (CATRASProtection val : CATRASProtection.values()) {
				if (val.toCode().equals(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CATRASFileType {
		RAW(0), 
		TREE_CURVE(1),
		CHRONOLOGY(2);
		
		private Integer code;
		
		CATRASFileType(Integer c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase());
		}
		
		public final Integer toCode() {
			return code;
		}
		
		public static CATRASFileType fromCode(Integer code) {
			for (CATRASFileType val : CATRASFileType.values()) {
				if (val.toCode().equals(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	
}
