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
package org.tridas.io.formats.sylphe;

import org.apache.commons.lang.WordUtils;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;

/**
 * @deprecated Use TridasToBesancon instead.
 * @author peterbrewer
 */
@Deprecated
public class TridasToSylpheDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet {
	
	public enum SylpheCambiumType {
		CAMBIUM_PRESENT_SEASON_UNKOWN(""), WINTER("HIV"), SUMMER("ETE"), SPRING("PRI");
		
		private String code;
		
		SylpheCambiumType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static SylpheCambiumType fromCode(String code) {
			for (SylpheCambiumType val : SylpheCambiumType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
}
