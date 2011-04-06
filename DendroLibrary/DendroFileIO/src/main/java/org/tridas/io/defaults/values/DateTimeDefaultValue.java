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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.schema.DateTime;

/**
 * @author Pete
 */
public class DateTimeDefaultValue extends AbstractDefaultValue<DateTime> {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DateTimeDefaultValue.class);
	
	private DateTime value = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	
	public DateTimeDefaultValue() {

	}
	
	public DateTimeDefaultValue(DateTime argValue) {
		value = argValue;
	}
	
	public DateTimeDefaultValue(DateTime argValue, SimpleDateFormat format) {
		sdf = format;
		value = argValue;
	}
	
	/**
	 * Set the formatter that should be used by the toStringValue function
	 * @param format
	 */
	public void setSimpleDateFormat(SimpleDateFormat format)
	{
		sdf = format;
	}
	
	/**
	 * Get the formatter used for saving this DateTime to string
	 * @param format
	 * @return
	 */
	public SimpleDateFormat getSimpleDateFormat(SimpleDateFormat format)
	{
		return sdf;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public DateTime getValue() {
		return value;
	}
	
	@Override
	public String getStringValue() {
		if (value != null) {
			value.toString();
			Date dt = value.getValue().toGregorianCalendar().getTime();
			return sdf.format(dt.getTime());
		}
		return null;
	}
	
	@Override
	protected boolean validateAndSetValue(DateTime argValue) {
		value = argValue;
		return true;
	}
	
	protected boolean validateAndSetValue(Integer day, Integer month, Integer year) {
		
		value = DateUtils.getDateTime(day, month, year);
		if (value != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
}
