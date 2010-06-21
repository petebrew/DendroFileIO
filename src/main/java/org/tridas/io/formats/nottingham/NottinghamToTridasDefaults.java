package org.tridas.io.formats.nottingham;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.schema.TridasMeasurementSeries;

public class NottinghamToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	
	public static enum DefaultFields {
		
		SERIES_TITLE,
		RING_COUNT;

	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		series.setTitle(getStringDefaultValue(DefaultFields.SERIES_TITLE).getValue());
		
		return series;
		
		
	}
}
