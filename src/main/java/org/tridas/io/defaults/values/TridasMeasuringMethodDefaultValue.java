/**
 * Created on Apr 21, 2010, 12:54:06 AM
 */
package org.tridas.io.defaults.values;

import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.schema.TridasMeasuringMethod;

/**
 * @author daniel
 *
 */
public class TridasMeasuringMethodDefaultValue extends AbstractDefaultValue<TridasMeasuringMethod>{

	private TridasMeasuringMethod value = null;
	
	public TridasMeasuringMethodDefaultValue(){
		
	}
	
	public TridasMeasuringMethodDefaultValue(TridasMeasuringMethod argMethod){
		value = argMethod;
	}
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#getValue()
	 */
	@Override
	public TridasMeasuringMethod getValue() {
		return value;
	}

	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#validateAndSetValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(TridasMeasuringMethod argValue) {
		value = argValue;
		return true;
	}

}
