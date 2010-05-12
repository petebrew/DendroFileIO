package org.tridas.io.formats.heidelberg;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class HeidelbergToTridasDefaults extends TridasMetadataFieldSet {
	
	public static enum DefaultFields{
		SERIES_ID,
		DATE_BEGIN,
		DATE_END,
		TAXON,
		UNIT,
		STANDARDIZATION_METHOD
	}
	
	public void initDefaultValues(){
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SERIES_ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATE_BEGIN, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATE_END, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.TAXON, new GenericDefaultValue<ControlledVoc>());
		setDefaultValue(DefaultFields.UNIT, new GenericDefaultValue<TridasUnit>());
		setDefaultValue(DefaultFields.STANDARDIZATION_METHOD, new StringDefaultValue());
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasProject()
	 */
	@Override
	protected TridasProject getDefaultTridasProject() {
		TridasProject p = super.getDefaultTridasProject();
		p.setComments("Converted from Heidelberg file.");
		return p;
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasDerivedSeries()
	 */
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
		TridasDerivedSeries series = super.getDefaultTridasDerivedSeries();
		
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(getStringDefaultValue(DefaultFields.SERIES_ID).getStringValue());
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		series.setIdentifier(id);
		
		// FIXME detect ad/bc
		TridasInterpretation interp = new TridasInterpretation();
		SafeIntYear startYear = new SafeIntYear( getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue());
		SafeIntYear endYear = new SafeIntYear( getIntegerDefaultValue(DefaultFields.DATE_END).getValue());
		interp.setFirstYear(startYear.toTridasYear(DatingSuffix.AD));					
		interp.setLastYear(endYear.toTridasYear(DatingSuffix.AD));
		series.setInterpretation(interp);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime());
		
		series.setStandardizingMethod(getDefaultValue(DefaultFields.STANDARDIZATION_METHOD).getStringValue());
		
		return series;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(getStringDefaultValue(DefaultFields.SERIES_ID).getStringValue());
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		series.setIdentifier(id);
		
		// FIXME detect ad/bc
		TridasInterpretation interp = new TridasInterpretation();
		SafeIntYear startYear = new SafeIntYear( getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue());
		SafeIntYear endYear = new SafeIntYear( getIntegerDefaultValue(DefaultFields.DATE_END).getValue());
		interp.setFirstYear(startYear.toTridasYear(DatingSuffix.AD));					
		interp.setLastYear(endYear.toTridasYear(DatingSuffix.AD));
		series.setInterpretation(interp);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime() );
		
		return series;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		ControlledVoc v = (ControlledVoc) getDefaultValue(DefaultFields.TAXON).getValue();
		e.setTaxon(v);
		return e;
	}
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults(){
		TridasValues valuesGroup = new TridasValues();
		
		GenericDefaultValue<TridasUnit> units = (GenericDefaultValue<TridasUnit>) getDefaultValue(DefaultFields.UNIT);
		if(units.getValue() == null){
			valuesGroup.setUnitless(new TridasUnitless());
		}else{
			valuesGroup.setUnit(units.getValue());
		}
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		return valuesGroup;
	}
}
