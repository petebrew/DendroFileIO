/**
 * Created on Apr 12, 2010, 1:37:53 PM
 */
package org.tridas.io.formats.trims;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;

/**
 * Place to hold and change default fields for the TRIMS filetype
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TridasToTrimsDefaults extends AbstractMetadataFieldSet {

	public enum TrimsField{
		MEASURING_DATE,
		AUTHOR,
		START_YEAR;
	}
	

	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		setDefaultValue(TrimsField.MEASURING_DATE, new StringDefaultValue(getTodaysDateTrimsStyle()));
		setDefaultValue(TrimsField.AUTHOR, new StringDefaultValue("XX", 2, 2));
		setDefaultValue(TrimsField.START_YEAR, new IntegerDefaultValue(1001));
	}
		
	private String getTodaysDateTrimsStyle(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(calendar.getTime());
	}
	
	
}
