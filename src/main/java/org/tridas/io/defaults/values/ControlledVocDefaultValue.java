/**
 * Created on Apr 21, 2010, 12:49:18 AM
 */
package org.tridas.io.defaults.values;

import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.schema.ControlledVoc;

/**
 * @author daniel
 *
 */
public class ControlledVocDefaultValue extends AbstractDefaultValue<ControlledVoc> {

	private ControlledVoc value = null;
	
	public ControlledVocDefaultValue(){
		
	}
	
	public ControlledVocDefaultValue(ControlledVoc argVoc){
		value = argVoc;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#getValue()
	 */
	@Override
	public ControlledVoc getValue() {
		return value;
	}

	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#validateAndSetValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(ControlledVoc argValue) {
		value = argValue;
		return true;
	}

}
