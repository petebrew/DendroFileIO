/**
 * 
 */
package org.tridas.io.defaults.values;

import org.tridas.io.defaults.AbstractDefaultValue;

/**
 * @author daniel
 *
 */
public class GenericDefaultValue<E> extends AbstractDefaultValue<E> {
	
	private E value = null;
	
	public GenericDefaultValue(){}
	
	public GenericDefaultValue(E argValue){
		value = argValue;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#getValue()
	 */
	@Override
	public E getValue() {
		return value;
	}

	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#validateAndSetValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(E argValue) {
		value = argValue;
		return true;
	}

}
