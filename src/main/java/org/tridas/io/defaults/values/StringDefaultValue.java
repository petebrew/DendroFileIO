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
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.io.util.StringUtils;

/**
 * @author Daniel
 */
public class StringDefaultValue extends AbstractDefaultValue<String> {
	
	private static final Logger log = LoggerFactory.getLogger(StringDefaultValue.class);
	
	private String value = null;
	
	public StringDefaultValue() {

	}
	
	public StringDefaultValue(String argValue) {
		value = argValue;
	}
	
	public StringDefaultValue(String argValue, int argMinLength, int argMaxLength) {
		super(argMaxLength, argMinLength);
		value = argValue;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}
	
	// we already validate the string as we would in AbstractDefaultValue
	@Override
	public String getStringValue() {
		if (value == null) {
			return "";
		}
		return value;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#validateAndSetValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(String argValue) {
		if (argValue == null) {
			value = null;
			return true;
		}
		argValue = validValue(argValue);
		if (argValue != null) {
			// log.verbose("string value stored: "+argValue);
			value = argValue;
			return true;
		}
		return false;
	}
	
	// basically recreation of getStringValue from AbstractDefaultValue, as it
	// works with strings as well
	private String validValue(String argValue) {
		String value = argValue.toString();
		if (getMaxLength() != -1) {
			if (value.length() > getMaxLength()) {
				log.warn(I18n.getText("fileio.defaults.stringTooBig", value, getMaxLength() + ""));
				if (getParent() == null) {
					log.error(I18n.getText("fileio.defaults.nullParent"));
				}
				else {
					getParent().addTruncatedWarning(getKey(),
							I18n.getText("fileio.defaults.stringTooBig", value, getMaxLength() + ""));
				}
				return StringUtils.rightPadWithTrim(value, getMaxLength());
			}
		}
		if (getMinLength() != -1) {
			if (value.length() < getMinLength()) {
				//log.debug(I18n.getText("fileio.defaults.stringTooSmall", value, isPadRight() + ""));
				if (isPadRight()) {
					return StringUtils.rightPad(value, getMinLength());
				}
				else {
					return StringUtils.leftPad(value, getMinLength());
				}
			}
		}
		return value;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#setMaxLength(int)
	 */
	@Override
	public void setMaxLength(int argMaxLength) {
		super.setMaxLength(argMaxLength);
		setValue(getValue());
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#setMinLength(int)
	 */
	@Override
	public void setMinLength(int argMinLength) {
		super.setMinLength(argMinLength);
		setValue(getValue());
	}
}
