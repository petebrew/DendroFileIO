/**
 * Created on Apr 21, 2010, 12:57:33 AM
 */
package org.tridas.io.defaults.values;

import java.util.ArrayList;

import org.tridas.io.defaults.AbstractDefaultValue;

/**
 * @author daniel
 *
 */
public class ArrayListDefaultValue<E> extends AbstractDefaultValue<ArrayList<E>>{

	private ArrayList<E> value = null;
	
	public ArrayListDefaultValue(){
		
	}
	
	public ArrayListDefaultValue(ArrayList<E> argArray){
		value = argArray;
	}
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#getValue()
	 */
	@Override
	public ArrayList<E> getValue() {
		return value;
	}

	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#validateAndSetValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(ArrayList<E> argValue) {
		value = argValue;
		return true;
	}
	
}
