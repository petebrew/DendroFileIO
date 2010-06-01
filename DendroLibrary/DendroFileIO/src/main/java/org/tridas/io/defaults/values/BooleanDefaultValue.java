/**
 * Created at: 7:41:33 PM, Apr 19, 2010
 */
package org.tridas.io.defaults.values;

import org.grlea.log.SimpleLogger;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.io.util.StringUtils;

/**
 * @author Pete
 *
 */
public class BooleanDefaultValue extends AbstractDefaultValue<Boolean> {
	
	private static final SimpleLogger log = new SimpleLogger(BooleanDefaultValue.class);
	
	private Boolean value = null;
	
	public BooleanDefaultValue(){
		
	}
	
	public BooleanDefaultValue(Boolean argValue){
		value = argValue;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public Boolean getValue() {
		return value;
	}
	
	@Override
	public String getStringValue(){
		if (value==true)
		{
			return "true";
		}
		else if (value==false)
		{
			return "false";
		}
		return null;
	}

	@Override
	protected boolean validateAndSetValue(Boolean argValue) {
		value = argValue;
		return true;
	}

}
