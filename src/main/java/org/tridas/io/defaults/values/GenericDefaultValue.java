/**
 * 
 */
package org.tridas.io.defaults.values;

import org.grlea.log.SimpleLogger;
import org.jvnet.jaxb2_commons.lang.Copyable;
import org.tridas.io.defaults.AbstractDefaultValue;

/**
 * @author daniel
 *
 */
public class GenericDefaultValue<E> extends AbstractDefaultValue<E> {
	private static final SimpleLogger log = new SimpleLogger(GenericDefaultValue.class);
	
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
	
	/**
	 * Overriding clone, used to do deep copy of jaxb elements that
	 * implement Copyable.  Otherwise, just a shallow copy.
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	public Object clone(){
		GenericDefaultValue<E> o = (GenericDefaultValue<E>) super.clone();
		
		// jaxb copyable
		if(value instanceof Copyable){
			E val;
			try {
				val = (E) value.getClass().newInstance();
			} catch (Exception e){
				log.warn("Could not create new instance of value, doing a shallow copy");
				return o;
			}
			
			((Copyable) value).copyTo(val);
			//log.debug("Copied value from '"+value+"' to '"+val+"'");// TODO locale
			o.setValue(val);
			return o;
		}else{
			return o;
		}
	}
}
