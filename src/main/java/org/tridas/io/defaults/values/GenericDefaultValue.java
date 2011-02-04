/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io.defaults.values;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jvnet.jaxb2_commons.lang.Copyable;
import org.tridas.io.defaults.AbstractDefaultValue;

/**
 * @author daniel
 */
public class GenericDefaultValue<E> extends AbstractDefaultValue<E> {
	private static final Logger log = LoggerFactory.getLogger(GenericDefaultValue.class);
	
	private E value = null;
	
	public GenericDefaultValue() {}
	
	public GenericDefaultValue(E argValue) {
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
	 * implement Copyable. Otherwise, just a shallow copy.
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		GenericDefaultValue<E> o = (GenericDefaultValue<E>) super.clone();
		
		// jaxb copyable
		if (value instanceof Copyable) {
			E val;
			try {
				val = (E) value.getClass().newInstance();
			} catch (Exception e) {
				log.warn("Could not create new instance of value, doing a shallow copy");
				return o;
			}
			
			((Copyable) value).copyTo(val);
			// log.debug("Copied value from '"+value+"' to '"+val+"'");// TODO locale
			o.setValue(val);
			return o;
		}
		else {
			return o;
		}
	}
}
