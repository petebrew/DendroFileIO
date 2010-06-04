package org.tridas.io.formats.sylphe;

import org.apache.commons.lang.WordUtils;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldChronologyType;

/**
 * @deprecated Use TridasToBesancon instead.
 * @author peterbrewer
 *
 */
public class TridasToSylpheDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	
	public enum SylpheCambiumType{
		CAMBIUM_PRESENT_SEASON_UNKOWN(""),
		WINTER("HIV"),
		SUMMER("ETE"),
		SPRING("PRI");
		
		private String code;
		
		SylpheCambiumType(String c){
			code = c;
		}
		
		public final String toString(){ return WordUtils.capitalize(this.name().toLowerCase().replace("_", " "));}
		
		public final String toCode(){ return this.code;}
	
		public static SylpheCambiumType fromCode(String code)
		{ 
			for (SylpheCambiumType val : SylpheCambiumType.values()){
				if (val.toCode().equalsIgnoreCase(code)) return val;
			}
			return null;	
		}
	}
	
}
