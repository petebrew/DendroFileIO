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
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASCompletion;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASFileType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASLastRing;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASProtection;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASSource;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASVariableType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

public class TridasToCatrasDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	@Override
	public void initDefaultValues() {
		setDefaultValue(DefaultFields.SERIES_NAME, new StringDefaultValue(I18n.getText("unnamed.series"), 32, 32));
		setDefaultValue(DefaultFields.SERIES_CODE, new StringDefaultValue("", 8, 8));
		setDefaultValue(DefaultFields.FILE_EXTENSION, new StringDefaultValue("cat"));
		setDefaultValue(DefaultFields.SERIES_LENGTH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD_LENGTH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.FIRST_VALID_YEAR, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.LAST_VALID_YEAR, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.COMPLETION, new GenericDefaultValue<CATRASCompletion>());
		setDefaultValue(DefaultFields.LAST_RING, new GenericDefaultValue<CATRASLastRing>());
		setDefaultValue(DefaultFields.NUMBER_OF_CHARS_IN_TITLE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.QUALITY_CODE, new IntegerDefaultValue(0));
				
		setDefaultValue(DefaultFields.START_YEAR, new SafeIntYearDefaultValue(null));
		setDefaultValue(DefaultFields.END_YEAR, new SafeIntYearDefaultValue(null));
		setDefaultValue(DefaultFields.SPECIES_CODE, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.CREATION_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.UPDATED_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATED, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILE_TYPE, new GenericDefaultValue<CATRASFileType>(CATRASFileType.RAW));
		setDefaultValue(DefaultFields.USER_ID, new StringDefaultValue());
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
	
		
	}
	
	public void populateFromTridasDerivedSeries(TridasDerivedSeries ds) {
		
		
	}
	

	public void populateFromTridasSeries(ITridasSeries ser){
		
		
		if (ser.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SERIES_NAME).setValue(ser.getTitle());

		}
		
	}
	
	public void populateFromTridasValues(TridasValues argValues) {
		
		
	}
	
	public void populateFromWoodCompleteness(TridasMeasurementSeries ms, TridasRadius r){
	
		
	}
}
