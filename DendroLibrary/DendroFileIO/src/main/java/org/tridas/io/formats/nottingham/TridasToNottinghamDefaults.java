package org.tridas.io.formats.nottingham;

import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.nottingham.NottinghamToTridasDefaults.DefaultFields;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValues;

public class TridasToNottinghamDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	@Override
	protected void initDefaultValues() {

		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed"), 9, 9));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
	}

	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries argSeries) {
		
		if(argSeries.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(argSeries.getTitle());
		}
		
	}
	
	public void populateFromTridasValues(TridasValues tvs)
	{
		getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(tvs.getValues().size());
	}
	
}
