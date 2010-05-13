/**
 * Created on Apr 12, 2010, 1:37:53 PM
 */
package org.tridas.io.formats.belfastapple;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;

/**
 * Place to hold and change default fields for the TRIMS filetype
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TridasToBelfastAppleDefaults extends AbstractMetadataFieldSet {

	public enum BelfastAppleField{
		OBJECT_TITLE,
		SAMPLE_TITLE;
	}
	

	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		setDefaultValue(BelfastAppleField.OBJECT_TITLE, new StringDefaultValue(I18n.getText("unnamed.object")));
		setDefaultValue(BelfastAppleField.SAMPLE_TITLE, new StringDefaultValue(I18n.getText("unnamed.sample")));

	}
			
	
}
