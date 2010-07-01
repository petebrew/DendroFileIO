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
package org.tridas.io.formats.vformat;

import org.tridas.io.I18n;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.CoordinatesUtils;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;

	enum DefaultFields{
		SERIES_ID,
		PROJECT_CODE,
		REGION_CODE,
		OBJECT_CODE,
		TREE_CODE,
		HEIGHT_CODE,
		DATA_TYPE,
		STAT_CODE,
		PARAMETER_CODE,
		UNIT,
		COUNT,
		SPECIES,
		LAST_YEAR,
		FIRST_YEAR,
		DESCRIPTION,
		CREATED_DATE,
		ANALYST,
		UPDATED_DATE,
		FORMAT_VERSION,
		UNMEAS_PRE,
		UNMEAS_PRE_ERR,
		UNMEAS_POST,
		UNMEAS_POST_ERR,
		FREE_TEXT_FIELD,
		LATITUDE,
		LONGITUDE,
		ELEVATION;	
	}

public class VFormatToTridasDefaults extends TridasMetadataFieldSet {
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		super.initDefaultValues();
		
		setDefaultValue(DefaultFields.SERIES_ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.REGION_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.TREE_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.HEIGHT_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATA_TYPE, new GenericDefaultValue<VFormatDataType>());
		setDefaultValue(DefaultFields.STAT_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.PARAMETER_CODE, new GenericDefaultValue<VFormatParameter>());
		setDefaultValue(DefaultFields.UNIT, new StringDefaultValue());
		setDefaultValue(DefaultFields.COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue());
		setDefaultValue(DefaultFields.LAST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.FIRST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.DESCRIPTION, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.CREATED_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.ANALYST, new StringDefaultValue());
		setDefaultValue(DefaultFields.UPDATED_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.FORMAT_VERSION, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_PRE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_PRE_ERR, new StringDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_POST, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_POST_ERR, new StringDefaultValue());
		setDefaultValue(DefaultFields.FREE_TEXT_FIELD, new StringDefaultValue());
		setDefaultValue(DefaultFields.LATITUDE, new DoubleDefaultValue(null, -90.0, 90.0));
		setDefaultValue(DefaultFields.LONGITUDE, new DoubleDefaultValue(null, -180.0, 180.0));
		setDefaultValue(DefaultFields.ELEVATION, new DoubleDefaultValue(null));

	}
	
	
	@Override
	public TridasProject getDefaultTridasProject()
	{
		TridasProject p = super.getDefaultTridasProject();
		/*
		// Project id
		if(getStringDefaultValue(DefaultFields.PROJECT_CODE).getValue()!=null)
		{
			TridasIdentifier id = new TridasIdentifier();
			id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
			id.setValue(getStringDefaultValue(DefaultFields.PROJECT_CODE).getValue());
			p.setIdentifier(id);
			p.setTitle(getStringDefaultValue(DefaultFields.PROJECT_CODE).getValue());
		}*/
		
		return p;
		
	}
	
	@Override
	public TridasObject getDefaultTridasObject()
	{
		TridasObject o = super.getDefaultTridasObject();
		
		// Object id
		if(getStringDefaultValue(DefaultFields.OBJECT_CODE).getValue()!=null)
		{
			TridasIdentifier id = new TridasIdentifier();
			id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
			id.setValue(getStringDefaultValue(DefaultFields.OBJECT_CODE).getValue());
			o.setIdentifier(id);
			o.setTitle(getStringDefaultValue(DefaultFields.OBJECT_CODE).getValue());
		}
		
		return o;
	}
	
	@Override
	public TridasElement getDefaultTridasElement()
	{
		TridasElement e = super.getDefaultTridasElement();
	
		// Element id
		if(getStringDefaultValue(DefaultFields.TREE_CODE).getValue()!=null)
		{
			TridasIdentifier id = new TridasIdentifier();
			id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
			id.setValue(getStringDefaultValue(DefaultFields.TREE_CODE).getValue());
			e.setIdentifier(id);
			e.setTitle(getStringDefaultValue(DefaultFields.TREE_CODE).getValue());
		}
		
		// Species
		if(getStringDefaultValue(DefaultFields.SPECIES).getValue()!=null)
		{
			e.setTaxon(ITRDBTaxonConverter.getControlledVocFromCode(getStringDefaultValue(DefaultFields.SPECIES).getValue()));
		}
		
		// Location
		if(getDoubleDefaultValue(DefaultFields.LATITUDE).getValue()!=null &&
		   getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue()!=null)
		{
			TridasLocation location = new TridasLocation();
			location.setLocationGeometry(CoordinatesUtils.getLocationGeometry(
					getDoubleDefaultValue(DefaultFields.LATITUDE).getValue(), 
					getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue()));
			e.setLocation(location);
		}
		
		// Elevation
		if(getDoubleDefaultValue(DefaultFields.ELEVATION).getValue()!=null)
		{
			e.setAltitude(getDoubleDefaultValue(DefaultFields.ELEVATION).getValue());
		}
		
		return e;
	}
	
	@Override
	public TridasSample getDefaultTridasSample()
	{
		TridasSample s = super.getDefaultTridasSample();
		
		
		if(getStringDefaultValue(DefaultFields.HEIGHT_CODE).getValue()!=null)
		{
			// Height code: 1=1m, 2=2m ... 9=9m, A=10m, B=11m... S=30cm, T=130cm
			String map = "0123456789ABCDEFGHIJKLMNOPQRST";	
			Integer height = map.indexOf(getStringDefaultValue(DefaultFields.HEIGHT_CODE).getStringValue());
		
			if(height!=null)
			{
				if (height!=0)
				{
					s.setPosition(String.valueOf(height)+"m");
				}
				else if (height==28)
				{
					s.setPosition("30cm");
				}
				else if (height==29)
				{
					s.setPosition("130cm");
				}
			}
		}
		
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
		TridasInterpretation interp = new TridasInterpretation();
		
				
		// Series id
		if(getStringDefaultValue(DefaultFields.SERIES_ID).getValue()!=null)
		{
			TridasIdentifier id = new TridasIdentifier();
			id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
			id.setValue(getStringDefaultValue(DefaultFields.SERIES_ID).getValue());
			ms.setIdentifier(id);
		}

		// Description / label
		if(getStringDefaultValue(DefaultFields.DESCRIPTION).getValue()!=null)
		{
			ms.setTitle(getStringDefaultValue(DefaultFields.DESCRIPTION).getValue());
		}
	
		// Last Year
		if(getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue()!=null)
		{
			interp.setLastYear(getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue()
					.toTridasYear(DatingSuffix.AD));
		}
		
		// First year
		if(getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue()!=null)
		{
			interp.setLastYear(getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue()
					.toTridasYear(DatingSuffix.AD));
		}
		
		// Created date
		if(getDateTimeDefaultValue(DefaultFields.CREATED_DATE).getValue()!=null)
		{
			ms.setCreatedTimestamp(getDateTimeDefaultValue(DefaultFields.CREATED_DATE).getValue());
		}
		
		// Last updated
		if(getDateTimeDefaultValue(DefaultFields.UPDATED_DATE).getValue()!=null)
		{
			ms.setLastModifiedTimestamp(getDateTimeDefaultValue(DefaultFields.UPDATED_DATE).getValue());
		}
		
		// Analyst
		if(getStringDefaultValue(DefaultFields.ANALYST).getValue()!=null)
		{
			ms.setAnalyst(getStringDefaultValue(DefaultFields.ANALYST).getValue());
		}
		
		// Free text comments
		if(getStringDefaultValue(DefaultFields.FREE_TEXT_FIELD).getValue()!=null)
		{
			ms.setComments(getStringDefaultValue(DefaultFields.FREE_TEXT_FIELD).getValue());
		}
		
		if(getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).getValue()!=null || getIntegerDefaultValue(DefaultFields.UNMEAS_POST).getValue()!=null)
		{
			TridasWoodCompleteness wc = super.getDefaultWoodCompleteness();

			// Unmeas pre
			if(getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).getValue()!=null)
			{
				wc.setNrOfUnmeasuredInnerRings(getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).getValue());
			}
			
			// Unmeas post
			if(getIntegerDefaultValue(DefaultFields.UNMEAS_POST).getValue()!=null)
			{
				wc.setNrOfUnmeasuredOuterRings(getIntegerDefaultValue(DefaultFields.UNMEAS_POST).getValue());
			}
			
			ms.setWoodCompleteness(wc);
		}
		
		ms.setInterpretation(interp);
		return ms;
	}
	
	@Override
	public TridasDerivedSeries getDefaultTridasDerivedSeries()
	{
		TridasDerivedSeries ds = super.getDefaultTridasDerivedSeries();
		
		// Series id
		if(getStringDefaultValue(DefaultFields.SERIES_ID).getValue()!=null)
		{
			ds.setTitle(getStringDefaultValue(DefaultFields.SERIES_ID).getValue());
		}
		
		return ds;
	}
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
		TridasUnit units = new TridasUnit();
				
		if (getDefaultValue(DefaultFields.PARAMETER_CODE).getValue() != null) {
			TridasVariable variable = new TridasVariable();
			switch ((VFormatParameter) getDefaultValue(DefaultFields.PARAMETER_CODE).getValue()) 
			{		
				case RING_WIDTH :
					variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
					units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
					break;
				case EARLYWOOD_WIDTH :
					variable.setNormalTridas(NormalTridasVariable.EARLYWOOD_WIDTH);
					units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
					break;
				case LATEWOOD_WIDTH :
					variable.setNormalTridas(NormalTridasVariable.LATEWOOD_WIDTH);
					units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
					break;
				case MIN_DENSITY :
					variable.setNormalId("I");
					variable.setNormalStd("Sheffield D-Format");
					variable.setNormal("Minimum density");
					variable.setValue("Minimum density");
					units.setValue(I18n.getText("unknown"));
					break;
				case MAX_DENSITY :
					variable.setNormalTridas(NormalTridasVariable.MAXIMUM_DENSITY);
					units.setValue(I18n.getText("unknown"));
					break;
				case PERC_LATEWOOD :
					variable.setNormalTridas(NormalTridasVariable.LATEWOOD_PERCENT);
					units.setValue("%");
					break;
				case MEAN_DENSITY:
					variable.setNormalId("D");
					variable.setNormalStd("VFormat Parameter");
					variable.setNormal("Mean density");
					units.setValue(I18n.getText("unknown"));
					break;
				default :
					variable.setValue(I18n.getText("unknown"));
					units.setValue(I18n.getText("unknown"));
					break;
			}
			valuesGroup.setVariable(variable);
			valuesGroup.setUnit(units);
		}
		else {
			GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
			valuesGroup.setVariable(variable.getValue());
			units.setValue(I18n.getText("unknown"));
			valuesGroup.setUnit(units);
		}
		
		return valuesGroup;
	}
	
	
	
	public enum ParamMeasured {
		MEAN_DENSITY("D"), EARLYWOOD_WIDTH("F"), MAX_DENSITY("G"), RING_WIDTH("J"), MIN_DENSITY("K");
		
		private String code;
		
		ParamMeasured(String code) {
			this.code = code;
		}
		
	}
	
	
	public enum VFormatDataType {
		SINGLE("!"), PARTIAL("%"), CHRONOLOGY("#");
		
		private String code;
		
		VFormatDataType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static VFormatDataType fromCode(String code) {
			for (VFormatDataType val : VFormatDataType.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
		
	}
	
	public enum VFormatParameter {
		RING_WIDTH("J"), MEAN_DENSITY("D"), EARLYWOOD_WIDTH("F"), MAX_DENSITY("G"), MIN_DENSITY("K"),
		PERC_LATEWOOD("P"), LATEWOOD_WIDTH("S");
		
		private String code;
		
		VFormatParameter(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static VFormatParameter fromCode(String code) {
			for (VFormatParameter val : VFormatParameter.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
		
	}
	
	public enum VFormatStatType {
		FREQ_FILTERED_SERIES("F"), INDEX("I"), MEAN_OF_SERIES("M"), ORIGINAL("O"), POINTER_YEAR("P"),
		CULSTER_POINTER_YEAR("Q"), RESIDUAL("R"), MOVING_DEVIATION("S"), TREND_FITTED_CURVE("T"), 
		TRANSFORMED_WUCHSWERT("W"), STANDARDISED_MEAN_AND_VARIANCE("X"), CENTRAL_MOMENT("Z");
		
		private String code;
		
		VFormatStatType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static VFormatStatType fromCode(String code) {
			for (VFormatStatType val : VFormatStatType.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
		
	}
}
