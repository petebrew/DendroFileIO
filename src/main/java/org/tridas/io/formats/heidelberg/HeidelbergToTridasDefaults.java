package org.tridas.io.formats.heidelberg;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.ControlledVocDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasProject;

public class HeidelbergToTridasDefaults extends TridasMetadataFieldSet {
	
	public static enum DefaultFields{
		SERIES_ID,
		DATE_BEGIN,
		DATE_END,
		TAXON
	}
	
	public void initDefaultValues(){
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SERIES_ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATE_BEGIN, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATE_END, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.TAXON, new ControlledVocDefaultValue());
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
}
