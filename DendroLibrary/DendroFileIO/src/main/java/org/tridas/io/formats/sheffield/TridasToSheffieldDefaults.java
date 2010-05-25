package org.tridas.io.formats.sheffield;

import org.apache.commons.lang.WordUtils;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SheffieldStringDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;

public class TridasToSheffieldDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	public static enum DefaultFields{
		SITE_NAME,
		RING_COUNT,
		DATE_TYPE,
		START_DATE,
		DATA_TYPE,
		SAPWOOD_COUNT,
		TIMBER_COUNT,
		EDGE_CODE,
		CHRONOLOGY_TYPE,
		COMMENT,
		UK_COORDS,
		LAT_LONG,
		PITH_CODE,
		SHAPE_CODE,
		MAJOR_DIM,
		MINOR_DIM,
		INNER_RING_CODE,
		OUTER_RING_CODE,
		PHASE,
		SHORT_TITLE,
		PERIOD,
		TAXON,
		INTERPRETATION_COMMENT,
		VARIABLE_TYPE;
	}

	public void initDefaultValues(){
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SITE_NAME, new SheffieldStringDefaultValue(I18n.getText("unnamed.object"), 1, 64));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue(1, 2147483647));
		setDefaultValue(DefaultFields.DATE_TYPE, new GenericDefaultValue<SheffieldDateType>(SheffieldDateType.RELATIVE));
		setDefaultValue(DefaultFields.START_DATE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATA_TYPE, new GenericDefaultValue<SheffieldDataType>(SheffieldDataType.ANNUAL_RAW_RING_WIDTH));
		setDefaultValue(DefaultFields.SAPWOOD_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.TIMBER_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.EDGE_CODE, new GenericDefaultValue<SheffieldEdgeCode>(SheffieldEdgeCode.NO_SPECFIC_EDGE));
		setDefaultValue(DefaultFields.CHRONOLOGY_TYPE, new GenericDefaultValue<SheffieldChronologyType>(SheffieldChronologyType.RAW));
		setDefaultValue(DefaultFields.COMMENT, new SheffieldStringDefaultValue("?", 1, 64));
		setDefaultValue(DefaultFields.UK_COORDS, new StringDefaultValue("?", 1, 14));
		setDefaultValue(DefaultFields.LAT_LONG, new StringDefaultValue("?", 1, 64));
		setDefaultValue(DefaultFields.PITH_CODE, new GenericDefaultValue<SheffieldPithCode>(SheffieldPithCode.UNKNOWN));
		setDefaultValue(DefaultFields.SHAPE_CODE, new GenericDefaultValue<SheffieldShapeCode>(SheffieldShapeCode.UNKNOWN));
		setDefaultValue(DefaultFields.MAJOR_DIM, new IntegerDefaultValue(0, 2147483647));
		setDefaultValue(DefaultFields.MINOR_DIM, new IntegerDefaultValue(0, 2147483647));
		setDefaultValue(DefaultFields.INNER_RING_CODE, new StringDefaultValue("N", 1, 5));
		setDefaultValue(DefaultFields.OUTER_RING_CODE, new StringDefaultValue("N", 1, 5));
		setDefaultValue(DefaultFields.PHASE, new SheffieldStringDefaultValue(I18n.getText("unknown"), 1, 14));
		setDefaultValue(DefaultFields.SHORT_TITLE, new SheffieldStringDefaultValue(I18n.getText("unknown"), 1, 8));
		setDefaultValue(DefaultFields.PERIOD, new GenericDefaultValue<SheffieldPeriodCode>(SheffieldPeriodCode.UNKNOWN));
		setDefaultValue(DefaultFields.TAXON, new StringDefaultValue("UNKN", 4, 4));
		setDefaultValue(DefaultFields.INTERPRETATION_COMMENT, new SheffieldStringDefaultValue("?", 1, 64));
		setDefaultValue(DefaultFields.VARIABLE_TYPE, new GenericDefaultValue<SheffieldVariableCode>(SheffieldVariableCode.RING_WIDTHS));
	}

	
	
	public enum SheffieldDateType{
		RELATIVE("R"),
		ABSOLUTE("A");
		
		private String code;
		
		SheffieldDateType(String c){
			code = c;
		}
		
		public final String toString(){ return this.code;}
		
		public static SheffieldDateType fromCode(String code)
		{ 
			for (SheffieldDateType val : SheffieldDateType.values()){
				if (val.toString().equalsIgnoreCase(code)) return val;
			}
			return null;	
		}
	}
	
	public enum SheffieldDataType{
		ANNUAL_RAW_RING_WIDTH("R"), // A, B, E, F, P, S, U, are all equal to R
		TIMBER_MEAN_WITH_SIGNATURES("W"),
		CHRON_MEAN_WITH_SIGNATURES("X"),
		TIMBER_MEAN("T"),
		CHRON_MEAN("C"),
		UNWEIGHTED_MASTER("M");
		
		private String code;
		
		SheffieldDataType(String c){
			code = c;
		}
		
		public final String toString(){ return this.code;}
		
		public static SheffieldDataType fromCode(String code)
		{ 
			for (SheffieldDataType val : SheffieldDataType.values()){
				if (val.toString().equalsIgnoreCase(code))
				{
					return val;
				}
				else if ( (code.equalsIgnoreCase("A")) || (code.equalsIgnoreCase("B")) || (code.equalsIgnoreCase("E")) || (code.equalsIgnoreCase("F"))
						|| (code.equalsIgnoreCase("P")) || (code.equalsIgnoreCase("S")) || (code.equalsIgnoreCase("U")) )
				{
					// All synoynms of ANNUAL_RAW_RING_WIDTH
					return SheffieldDataType.ANNUAL_RAW_RING_WIDTH;
				}
				
			}
			return null;	
		}
		
	}
	
	public enum SheffieldEdgeCode{
		BARK("Y"),
		POSS_BARK("!"),
		WINTER("W"),
		SUMMER("S"),
		HS_BOUNDARY("B"),
		POSS_HS_BOUNDARY("?"),
		NO_SPECFIC_EDGE("N"),
		SAP_BARK_UNKNOWN("U"),
		CHARRED("C"),
		POSSIBLY_CHARRED("P");
		
		private String code;
		
		SheffieldEdgeCode(String c){
			code = c;
		}
		
		public final String toString(){ return this.code;}
		
		public static SheffieldEdgeCode fromCode(String code)
		{ 
			for (SheffieldEdgeCode val : SheffieldEdgeCode.values()){
				if (val.toString().equalsIgnoreCase(code)) return val;
			}
			return null;	
		}
		
	}
	
	public enum SheffieldChronologyType{
		RAW("R"), 
		FIVE_YEAR_MEAN("5"), 
		INDEXED("I"),
		UNKNOWN_MEAN("U");
		
		private String code;
		
		SheffieldChronologyType(String c){
			code = c;
		}
		
		public final String toString(){ return WordUtils.capitalize(this.name().toLowerCase());}
		
		public final String toCode(){ return this.code;}
	
		public static SheffieldChronologyType fromCode(String code)
		{ 
			for (SheffieldChronologyType val : SheffieldChronologyType.values()){
				if (val.toCode().equalsIgnoreCase(code)) return val;
			}
			return null;	
		}
	}
	
	public enum SheffieldPithCode{
		CENTRE("C"),
		WITHIN_FIVE_YEARS("V"),
		FIVE_TO_TEN_YEARS("F"),
		GREATER_THAN_TEN("G"),
		UNKNOWN("?");
		
		private String code;
		
		SheffieldPithCode(String c){
			code = c;
		}
		
		public final String toString(){ return this.code;}
	
		public static SheffieldPithCode fromCode(String code)
		{ 
			for (SheffieldPithCode val : SheffieldPithCode.values()){
				if (val.toString().equalsIgnoreCase(code)) return val;
			}
			return null;	
		}
	}
	
	public enum SheffieldShapeCode{
		WHOLE_ROUND_UNTRIMMED("A1"),
		WHOLE_ROUND_TRIMMED("A2"),
		WHOLE_ROUND_IRREGULARLY_TRIMMED("AX"),
		HALF_ROUND_UNTRIMMED("B1"),
		HALF_ROUND_TRIMMED("B2"),
		HALF_ROUND_IRREGULARLY_TRIMMED("BX"),
		QUARTERED_UNTRIMMED("C1"),
		QUARTERED_TRIMMED("C2"),
		QUARTERED_IRREGULARLY_TRIMMED("CX"),
		RADIAL_PLANK_UNTRIMMED("D1"),
		RADIAL_PLANK_TRIMMED("D2"),
		RADIAL_PLANK_IRREGULARLY_TRIMMED("DX"),
		TANGENTIAL_PLANK_UNTRIMMED("E1"),
		TANGENTIAL_PLANK_TRIMMED("E2"),
		TANGENTIAL_PLANK_IRREGULARLY_TRIMMED("EX"),
		UNKNOWN("?"),
		CORE_UNCLASSIFIABLE("X");
		
		private String code;
		
		SheffieldShapeCode(String c){
			code = c;
		}
		
		public final String toString(){ return this.code;}
	
		public static SheffieldShapeCode fromCode(String code)
		{ 
			for (SheffieldShapeCode val : SheffieldShapeCode.values()){
				if (val.toString().equalsIgnoreCase(code)) return val;
			}
			return null;	
		}
	}
		
	public enum SheffieldPeriodCode{
		MODERN("C"),
		POST_MEDIEVAL("P"),
		MEDIEVAL("M"),
		SAXON("S"),
		ROMAN("R"),
		PRE_ROMAN("A"),
		DUPLICATE("2"),
		MULTI_PERIOD("B"),
		UNKNOWN("?");

		
		private String code;
		
		SheffieldPeriodCode(String c){
			code = c;
		}
		
		public final String toCode(){ return this.code; }
		
		public final String toString(){ return this.name(); }
	
		public static SheffieldPeriodCode fromCode(String code)
		{ 
			for (SheffieldPeriodCode val : SheffieldPeriodCode.values()){
				if (val.toCode().equalsIgnoreCase(code)) return val;
			}
			return null;	
		}
	}
	
	public enum SheffieldVariableCode{
		RING_WIDTHS("D"),
		EARLY_WOOD_WIDTHS("E"),
		LATE_WOOD_WIDTHS("L"),
		EARLY_AND_LATE_WOOD_WIDTHS_REVERSED("R"),
		MINIMUM_DENSITY("I"),
		MAXIMUM_DENSITY("A"),
		EARLY_AND_LATE_SEQUENTIALLY("S"), //????
		MIXED("M");

		private String code;
		
		SheffieldVariableCode(String c){
			code = c;
		}
		
		public final String toString(){ return this.code;}
	
		public static SheffieldVariableCode fromCode(String code)
		{ 
			for (SheffieldVariableCode val : SheffieldVariableCode.values()){
				if (val.toString().equalsIgnoreCase(code)) return val;
			}
			return null;	
		}
	}
}
