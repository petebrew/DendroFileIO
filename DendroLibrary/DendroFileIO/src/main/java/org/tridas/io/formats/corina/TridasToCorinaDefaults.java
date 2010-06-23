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
package org.tridas.io.formats.corina;

import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.corina.CorinaToTridasDefaults.DefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValues;

public class TridasToCorinaDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	@Override
	protected void initDefaultValues() {

		setDefaultValue(DefaultFields.ID, new StringDefaultValue(null, 6, 6));
		setDefaultValue(DefaultFields.NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATING, new StringDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_PRE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_POST, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.COMMENTS2, new StringDefaultValue());
		setDefaultValue(DefaultFields.TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.PITH, new StringDefaultValue());
		setDefaultValue(DefaultFields.TERMINAL, new StringDefaultValue());
		setDefaultValue(DefaultFields.CONTINUOUS, new StringDefaultValue());
		setDefaultValue(DefaultFields.QUALITY, new StringDefaultValue());
		setDefaultValue(DefaultFields.FORMAT, new StringDefaultValue());
		setDefaultValue(DefaultFields.INDEX_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILENAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.RECONCILED, new StringDefaultValue());
		setDefaultValue(DefaultFields.START_YEAR, new SafeIntYearDefaultValue(new SafeIntYear(1001)));
		setDefaultValue(DefaultFields.USERNAME, new StringDefaultValue());
	}

	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries argSeries) {
		
		
	}
	
	public void populateFromTridasValues(TridasValues tvs)
	{
		
	}
	
	public enum CorinaDatingType {
		ABSOLUTE("A"), RELATIVE("R");
		
		private String code;
		
		CorinaDatingType(String c) {
			code = c;
		}
		
		public final String toCode() {
			return code;
		}
		
		@Override
		public final String toString() {
			return name();
		}
		
		public static CorinaDatingType fromCode(String code) {
			for (CorinaDatingType val : CorinaDatingType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CorinaTerminalRing {
		BARK("B"), WANEY_EDGE("W"), NEAR_END("v"), UNKNOWN("vv");
		
		private String code;
		
		CorinaTerminalRing(String c) {
			code = c;
		}
		
		public final String toCode() {
			return code;
		}
		
		@Override
		public final String toString() {
			return name();
		}
		
		public static CorinaTerminalRing fromCode(String code) {
			for (CorinaTerminalRing val : CorinaTerminalRing.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CorinaSampleType {
		CORE("C"), SECTION("S"), CHARCOAL("H");
		
		private String code;
		
		CorinaSampleType(String c) {
			code = c;
		}
		
		public final String toCode() {
			return code;
		}
		
		@Override
		public final String toString() {
			return name();
		}
		
		public static CorinaSampleType fromCode(String code) {
			for (CorinaSampleType val : CorinaSampleType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
}
