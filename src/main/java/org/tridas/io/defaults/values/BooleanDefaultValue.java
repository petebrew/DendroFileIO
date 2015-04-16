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
import org.tridas.io.defaults.AbstractDefaultValue;

/**
 * @author Pete
 */
public class BooleanDefaultValue extends AbstractDefaultValue<Boolean> {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BooleanDefaultValue.class);
	
	protected Boolean value = null;
	
	public BooleanDefaultValue() {

	}
	
	public BooleanDefaultValue(Boolean argValue) {
		value = argValue;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public Boolean getValue() {
		return value;
	}
	
	@Override
	public String getStringValue() {
		if(value==null)
		{
			return "";
		}
		else if (value == true) {
			return "true";
		}
		else if (value == false) {
			return "false";
		}
		return null;
	}
	
	@Override
	protected boolean validateAndSetValue(Boolean argValue) {
		value = argValue;
		return true;
	}
	
}
