/**
 * Created at: 7:41:33 PM, Apr 19, 2010
 */
package org.tridas.io.defaults.values;

import org.grlea.log.SimpleLogger;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.DateTime;

/**
 * @author Pete
 *
 */
public class DateTimeDefaultValue extends AbstractDefaultValue<DateTime> {
	
	private static final SimpleLogger log = new SimpleLogger(DateTimeDefaultValue.class);
	
	private DateTime value = null;
	
	public DateTimeDefaultValue(){
		
	}
	
	public DateTimeDefaultValue(DateTime argValue){
		value = argValue;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public DateTime getValue() {
		return value;
	}
	
	@Override
	public String getStringValue(){
		if (value!=null)
		{
			value.toString();
		}
		return null;
	}

	@Override
	protected boolean validateAndSetValue(DateTime argValue) {
		value = argValue;
		return true;
	}
	
	protected boolean validateAndSetValue(Integer day, Integer month, Integer year) {
		
		value = DateUtils.getDateTime(day, month, year);
		if(value!=null)
		{
			return true;
		}
		else 
		{
			return false;
		}
	}

}
