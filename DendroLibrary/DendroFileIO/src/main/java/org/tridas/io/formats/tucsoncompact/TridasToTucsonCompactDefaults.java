package org.tridas.io.formats.tucsoncompact;

import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.tucsoncompact.TucsonCompactToTridasDefaults.DefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValues;

public class TridasToTucsonCompactDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	@Override
	protected void initDefaultValues() {

		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed"), 46, 46));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 8, 8));
		setDefaultValue(DefaultFields.START_YEAR, new IntegerDefaultValue(1001, Integer.MIN_VALUE, Integer.MAX_VALUE, 8, 8));
		getDefaultValue(DefaultFields.START_YEAR).setPadRight(false);
		getDefaultValue(DefaultFields.RING_COUNT).setPadRight(false);
	}

	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries argSeries) {
		
		if(argSeries.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(argSeries.getTitle());
		}
		
		if(argSeries.isSetInterpretation())
		{
			if(argSeries.getInterpretation().isSetFirstYear())
			{
				SafeIntYear startYear = new SafeIntYear(argSeries.getInterpretation().getFirstYear());
				getIntegerDefaultValue(DefaultFields.START_YEAR).setValue(Integer.parseInt(startYear.toString()));
			}
		}
		
	}
	
	public void populateFromTridasValues(TridasValues tvs)
	{
		getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(tvs.getValues().size());
	}
	
	
	
}
