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
package org.tridas.io.formats.trims;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;

/**
 * Place to hold and change default fields for the TRIMS filetype
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TridasToTrimsDefaults extends AbstractMetadataFieldSet {
	
	public enum TrimsField {
		MEASURING_DATE, AUTHOR, START_YEAR;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		setDefaultValue(TrimsField.MEASURING_DATE, new StringDefaultValue(getTodaysDateTrimsStyle()));
		setDefaultValue(TrimsField.AUTHOR, new StringDefaultValue("XX", 2, 2));
		setDefaultValue(TrimsField.START_YEAR, new IntegerDefaultValue(1001));
	}
	
	private String getTodaysDateTrimsStyle() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		return dateFormat.format(calendar.getTime());
	}
	
}
