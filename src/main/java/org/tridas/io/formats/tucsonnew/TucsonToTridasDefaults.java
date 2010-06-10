package org.tridas.io.formats.tucsonnew;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasObject;

/**
 * here for the library user to create and pass in the loadFile() arguments
 * 
 * @author Daniel
 */
public class TucsonToTridasDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet {
	
	public enum TucsonDefaultField {
		SITE_CODE, SITE_NAME, SPECIES_CODE, SPECIES_NAME, INVESTIGATOR, ELEVATION, LATLONG, 
		STATE_COUNTRY, COMP_DATE, UNITS;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		setDefaultValue(TucsonDefaultField.SITE_CODE, new StringDefaultValue(UUID.randomUUID().toString().substring(0, 6), 6,
				6));
		setDefaultValue(TucsonDefaultField.SITE_NAME, new StringDefaultValue(I18n.getText("unnamed.object"), 50, 50));
		setDefaultValue(TucsonDefaultField.SPECIES_CODE, new StringDefaultValue("UNKN", 4, 4));
		setDefaultValue(TucsonDefaultField.SPECIES_NAME, new StringDefaultValue("Plantae", 8, 8));
		setDefaultValue(TucsonDefaultField.INVESTIGATOR, new StringDefaultValue(I18n.getText("unknown"), 61, 61));
		setDefaultValue(TucsonDefaultField.ELEVATION, new IntegerDefaultValue(null, -999999999, Integer.MAX_VALUE, 10, 10));
		setDefaultValue(TucsonDefaultField.LATLONG, new StringDefaultValue("", 11, 11));
		setDefaultValue(TucsonDefaultField.STATE_COUNTRY, new StringDefaultValue(I18n.getText("unknown"), 13, 13));
		setDefaultValue(TucsonDefaultField.COMP_DATE, new StringDefaultValue(getTodaysDateTucsonStyle(), 8, 8));
		setDefaultValue(TucsonDefaultField.UNITS, new GenericDefaultValue<NormalTridasUnit>(NormalTridasUnit.HUNDREDTH_MM));

		// TODO range
	}
	
	private String getTodaysDateTucsonStyle() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return dateFormat.format(calendar.getTime());
	}
}
