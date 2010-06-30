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
	
	public SafeIntYearDefaultValue(SafeIntYear argValue, int argMinLength, int argMaxLength) {
		super(argMaxLength, argMinLength);
		value = argValue;
	}
	
	public SafeIntYearDefaultValue(int argMinLength, int argMaxLength) {
		super(argMaxLength, argMinLength);
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public SafeIntYear getValue() {
		return value;
	}
		
	@Override
	protected boolean validateAndSetValue(SafeIntYear argValue) {
		value = argValue;
		return true;
	}
	
	// Helper to allow setting SafeIntYear using a simple integer. This
	// assumes the integer is a BC/AD value.
	public boolean setValue(Integer argValue) {
		//return validateAndSetValue(argValue);
		return setValue(new SafeIntYear(argValue)); // djm fix: didn't handle overriding
	}
}
