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
