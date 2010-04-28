/**
 * Created on Apr 21, 2010, 12:51:17 AM
 */
package org.tridas.io.defaults.values;

import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.schema.TridasCategory;

/**
 * @author daniel
 *
 */
public class TridasCategoryDefaultValue extends AbstractDefaultValue<TridasCategory>{

	private TridasCategory value = null;
	
	public TridasCategoryDefaultValue(){
		
	}
	
	public TridasCategoryDefaultValue(TridasCategory argCategory){
		value = argCategory;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#getValue()
	 */
	@Override
	public TridasCategory getValue() {
		return value;
	}

	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#validateAndSetValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(TridasCategory argValue) {
		value = argValue;
		return true;
	}

}
