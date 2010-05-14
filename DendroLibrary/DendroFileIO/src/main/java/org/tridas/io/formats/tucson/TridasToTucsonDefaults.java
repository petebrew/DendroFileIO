/**
 * Created on Apr 12, 2010, 1:37:53 PM
 */
package org.tridas.io.formats.tucson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.StringUtils;

/**
 * Place to hold and change default fields for the Tucson filetype
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TridasToTucsonDefaults extends AbstractMetadataFieldSet {

	public enum TucsonField{
		SITE_CODE,
		SITE_NAME,
		SPECIES_CODE,
		SPECIES_NAME,
		INVESTIGATOR,
		ELEVATION,
		LATLONG,
		STATE_COUNTRY,
		COMP_DATE,
		RANGE;
	}
	

	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		setDefaultValue(TucsonField.SITE_CODE, new StringDefaultValue(UUID.randomUUID().toString().substring(0,6), 6, 6));
		setDefaultValue(TucsonField.SITE_NAME, new StringDefaultValue(I18n.getText("unnamed.object"), 50, 50));
		setDefaultValue(TucsonField.SPECIES_CODE, new StringDefaultValue("UNKN", 4, 4));
		setDefaultValue(TucsonField.SPECIES_NAME, new StringDefaultValue("Plantae", 8, 8));
		setDefaultValue(TucsonField.INVESTIGATOR, new StringDefaultValue(I18n.getText("unknown"), 61, 61));
		setDefaultValue(TucsonField.ELEVATION, new IntegerDefaultValue(null, -999999999, Integer.MAX_VALUE, 10, 10));
		setDefaultValue(TucsonField.LATLONG, new StringDefaultValue("", 11, 11));
		setDefaultValue(TucsonField.STATE_COUNTRY, new StringDefaultValue(I18n.getText("unknown"), 13, 13));
		setDefaultValue(TucsonField.COMP_DATE, new StringDefaultValue(getTodaysDateTucsonStyle(), 8, 8));
		// TODO range
	}
		
	private String getTodaysDateTucsonStyle(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(calendar.getTime());
	}
}
