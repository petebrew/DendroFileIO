/**
 * Created at: 7:41:33 PM, Apr 19, 2010
 */
package org.tridas.io.defaults.values;

import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.io.util.SafeIntYear;

/**
 * @author Pete
 */
public class SafeIntYearDefaultValue extends AbstractDefaultValue<SafeIntYear> {
	
	private static final SimpleLogger log = new SimpleLogger(SafeIntYearDefaultValue.class);
	
	private SafeIntYear value = new SafeIntYear();
	
	public SafeIntYearDefaultValue() {

	}
	
	public SafeIntYearDefaultValue(SafeIntYear argValue) {
		value = argValue;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public SafeIntYear getValue() {
		return value;
	}
	
	@Override
	public String getStringValue() {
		return value.toString();
	}
	
	@Override
	protected boolean validateAndSetValue(SafeIntYear argValue) {
		value = argValue;
		return true;
	}
	
	protected boolean validateAndSetValue(Integer argValue) {
		value = new SafeIntYear(argValue);
		return true;
	}
	
	// Helper to allow setting SafeIntYear using a simple integer. This
	// assumes the integer is a BC/AD value.
	public boolean setValue(Integer argValue) {
		return validateAndSetValue(argValue);
	}
}
