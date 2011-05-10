package org.tridas.io.formats.catras;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASScope;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASFileType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASLastRing;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASProtection;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASSource;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASVariableType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToCatrasDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	@Override
	public void initDefaultValues() {
		setDefaultValue(DefaultFields.SERIES_NAME, new StringDefaultValue(I18n.getText("unnamed.series"), 32, 32));
		setDefaultValue(DefaultFields.SERIES_CODE, new StringDefaultValue("", 8, 8));
		setDefaultValue(DefaultFields.FILE_EXTENSION, new StringDefaultValue("CAT ", 4, 4 ));
		setDefaultValue(DefaultFields.SERIES_LENGTH, new IntegerDefaultValue(null, 0, 32767));
		getDefaultValue(DefaultFields.SERIES_LENGTH).setMaxLength(2);
		getDefaultValue(DefaultFields.SERIES_LENGTH).setMinLength(2);
		
		setDefaultValue(DefaultFields.SAPWOOD_LENGTH, new IntegerDefaultValue(null, 0, 32767));
		setDefaultValue(DefaultFields.FIRST_VALID_YEAR, new IntegerDefaultValue(null, 0, 32767));
		setDefaultValue(DefaultFields.LAST_VALID_YEAR, new IntegerDefaultValue(null, 0, 32767));
		setDefaultValue(DefaultFields.SCOPE, new GenericDefaultValue<CATRASScope>(CATRASScope.UNSPECIFIED));
		setDefaultValue(DefaultFields.LAST_RING, new GenericDefaultValue<CATRASLastRing>(CATRASLastRing.COMPLETE));
		setDefaultValue(DefaultFields.NUMBER_OF_CHARS_IN_TITLE, new IntegerDefaultValue(null, 0, 32767));
		setDefaultValue(DefaultFields.QUALITY_CODE, new IntegerDefaultValue(null, 0, 32767));
				
		setDefaultValue(DefaultFields.START_YEAR, new SafeIntYearDefaultValue(null));
		setDefaultValue(DefaultFields.END_YEAR, new SafeIntYearDefaultValue(null));
		setDefaultValue(DefaultFields.SPECIES_CODE, new IntegerDefaultValue(0, 0, 32767));
		setDefaultValue(DefaultFields.CREATION_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.UPDATED_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATED, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILE_TYPE, new GenericDefaultValue<CATRASFileType>(CATRASFileType.RAW));
		setDefaultValue(DefaultFields.USER_ID, new StringDefaultValue(null, 4, 4));
		setDefaultValue(DefaultFields.VARIABLE_TYPE, new GenericDefaultValue<CATRASVariableType>(CATRASVariableType.RINGWIDTH));
		setDefaultValue(DefaultFields.SOURCE, new GenericDefaultValue<CATRASSource>());
		setDefaultValue(DefaultFields.PROTECTION, new GenericDefaultValue<CATRASProtection>(CATRASProtection.NONE));
	}
	
	
	public void populateFromTridasProject(TridasProject p) {


	}
	
	public void populateFromTridasObject(TridasObject o) {


	}
	
	public void populateFromTridasElement(TridasElement e) {

		
	}
	
	public void populateFromTridasSample(TridasSample s) {


	}
	
	public void populateFromTridasRadius(TridasRadius r) {


	}
	
	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries ms) {
		populateFromTridasSeries(ms);
		
	}
	
	public void populateFromTridasDerivedSeries(TridasDerivedSeries ds) {
		
		populateFromTridasSeries(ds);
	}
	

	private void populateFromTridasSeries(ITridasSeries ser){
		
		
		if (ser.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SERIES_NAME).setValue(ser.getTitle());
			getIntegerDefaultValue(DefaultFields.NUMBER_OF_CHARS_IN_TITLE).setValue(
					getStringDefaultValue(DefaultFields.SERIES_NAME).getStringValue().length());
		}
		
		if (ser.isSetIdentifier())
		{
			getStringDefaultValue(DefaultFields.SERIES_CODE).setValue(ser.getIdentifier().getValue());
		}
		else
		{
			getStringDefaultValue(DefaultFields.SERIES_CODE).setValue(ser.getTitle());
		}
		
	}
	

	
	public void populateFromTridasValues(TridasValues argValues) {
		
		getIntegerDefaultValue(DefaultFields.SERIES_LENGTH).setValue(argValues.getValues().size());
		
	}
	
	public void populateFromWoodCompleteness(TridasMeasurementSeries series, TridasRadius radius){
	
		TridasWoodCompleteness wc = null;
		TridasSapwood sapwood = null;
		TridasBark bark = null;
		
		// Get the wood completeness from the series if possible, if not then try the radius
		if (series.isSetWoodCompleteness())
		{
			wc = series.getWoodCompleteness();
		}
		else if (radius.isSetWoodCompleteness())
		{
			wc = radius.getWoodCompleteness();
		}
		
		if(wc.isSetSapwood())
		{
			if(wc.getSapwood().isSetNrOfSapwoodRings())
			{
				getIntegerDefaultValue(DefaultFields.SAPWOOD_LENGTH).setValue(wc.getSapwood().getNrOfSapwoodRings());
			}
		}
		
		
	}
}
