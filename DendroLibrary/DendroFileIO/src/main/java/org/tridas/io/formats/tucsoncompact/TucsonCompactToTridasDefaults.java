package org.tridas.io.formats.tucsoncompact;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;

public class TucsonCompactToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	
	public static enum DefaultFields {
		
		SERIES_TITLE,
		RING_COUNT,
		START_YEAR;

	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.START_YEAR, new IntegerDefaultValue());
		
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		series.setTitle(getStringDefaultValue(DefaultFields.SERIES_TITLE).getValue());
		
		// Set first year
		if(getIntegerDefaultValue(DefaultFields.START_YEAR).getValue()!=null)
		{
			TridasInterpretation interp = new TridasInterpretation();
			SafeIntYear firstYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.START_YEAR).getValue());
			interp.setFirstYear(firstYear.toTridasYear(DatingSuffix.AD));
			series.setInterpretation(interp);		
		}
		
		
		return series;
		
		
	}
}
