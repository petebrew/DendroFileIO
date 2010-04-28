/**
 * Created on Apr 21, 2010, 12:53:03 AM
 */
package org.tridas.io.defaults.values;

import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.schema.TridasVariable;

/**
 * @author daniel
 *
 */
public class TridasVariableDefaultValue extends AbstractDefaultValue<TridasVariable> {
	
	private TridasVariable value = null;

	public TridasVariableDefaultValue(){
		
	}
	
	public TridasVariableDefaultValue(TridasVariable argVariable){
		value = argVariable;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#getValue()
	 */
	@Override
	public TridasVariable getValue() {
		return value;
	}

	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#validateAndSetValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(TridasVariable argValue) {
		value = argValue;
		return true;
	}
}
