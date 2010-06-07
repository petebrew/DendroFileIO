package org.tridas.io.formats.heidelberg;

import org.apache.commons.lang.WordUtils;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.besancon.BesanconToTridasDefaults.BesanconCambiumType;
import org.tridas.io.formats.besancon.BesanconToTridasDefaults.DefaultFields;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class HeidelbergToTridasDefaults extends TridasMetadataFieldSet {
	
	public static enum DefaultFields {
		
		// Field marked with *** have been implemented
		
		ACCEPT_DATE,					
		//AGE,							//CALCULABLE
		//AUTOCORRELATION,				//USED FOR TUCSON
		BARK,							//***
		BHD,
		//Bibliography[n]
		//BibliographyCount
		BUNDLE,
		//CardinalPoint
		//ChronologyType				//USED FOR SHEFFIELD
		CHRONO_MEMBER_COUNT,
		CHRONO_MEMBER_KEYCODES,
		CIRCUMFERENCE,
		CLIENT,
		CLIENT_NO,
		COLLECTOR,
		//Comment[n]
		//CommentCount
		CONTINENT,				
		CORE_NUMBER,					//***
		COUNTRY,						//***
		//CREATION_DATE,				//DEPRECATED
		DATA_FORMAT,
		DATA_TYPE,						//***
		DATE_BEGIN,						//***
		DATED,							//***
		DATE_END,						//***
		//DATE_END_REL,					//DEPRECATED
		DATE_OF_SAMPLING,				//***
		//DateRelBegin[n]
		//DateRelEnd[n]
		//DateRelReferenceKey[n]
		//DateRelCount
		DELTA_MISSING_RINGS_AFTER,
		DELTA_MISSING_RINGS_BEFORE,
		DELTA_RINGS_FROM_SEED_TO_PITH,
		//DISK,							//USED FOR INRA
		DISTRICT,						//***
		//EDGE_INFORMATION,				//USED FOR SHEFFIELD
		//EffectiveAutoCorrelation      //USED FOR CATRAS
		//EffectiveMean                 //USED FOR CATRAS
		//EffectiveMeanSensitivity      //USED FOR CATRAS
		//EffectiveNORFAC               //USED FOR CATRAS
		//EffectiveNORFM                //USED FOR CATRAS
		//EffectiveStandardDeviation    //USED FOR CATRAS
		//Eigenvalue					//DEPRECATED
		ELEVATION,						//***
		ESTIMATED_TIME_PERIOD,			//***
		EXPOSITION,
		FIELD_NO,
		//FilmNo					  	//USED FOR BIREMSDORF
		FIRST_MEASUREMENT_DATE,			//***
		FIRST_MEASUREMENT_PERS_ID,
		//FromSeedToDateBegin			//DEPRECATED
		//GlobalMathComment[n]
		//GlobalMathCommentCount
		//GraphParam					//DEPRECATED
		//Group							//USED FOR SHEFFIELD
		HOUSE_NAME,						//***
		HOUSE_NUMBER,					//***
		//ImageCellRow					//DEPRECATED
		//ImageComment[n]
		//ImageFile[n]
		//ImageCount
		//ImageFile
		//Interpretation				//USED FOR SHEFFIELD
		INVALID_RINGS_AFTER,
		INVALID_RINGS_BEFORE,
		JUVENILE_WOOD,
		KEYCODE,						//***
		KEY_NUMBER,
		LAB_CODE,						//***
		LAST_REVISION_DATE,				//***
		LAST_REVISION_PERS_ID,			//***
		LATITUDE,						//***
		LEAVE_LOSS,
		LENGTH,							//***
		LOCATION,						//***
		LOCATION_CHARACTERISTICS,		//***
		LONGITUDE,						//***
		// MAJOR_DIMENSION,			    //USED FOR SHEFFIELD
		//MathComment
		//MathComment[n]
		//MathCommentCount
		//MeanSensitivity				//USED FOR TUCSON
		//MinorDimension				//USED FOR SHEFFIELD
		MISSING_RINGS_AFTER,			//***
		MISSING_RINGS_BEFORE,			//***
		//NumberOfSamplesInChrono		//DEPRECATED
		//NumberOfTreesInChrono			//DEPRECATED
		PERS_ID,
		PITH,							//***
		PROJECT,						//***
		//ProtectionCode				//USED FOR CATRAS
		PROVINCE,						//***
		QUALITY_CODE,
		//RADIUS,						//USED FOR INRA
		RADIUS_NUMBER,					//***
		RELATIVE_GROUND_WATER_LEVEL,	
		RINGS_FROM_SEED_TO_PITH,
		//SampleType					//USED FOR SHEFFIELD
		SAMPLING_HEIGHT,				//***
		SAMPLING_POINT,
		SAPWOOD_RINGS,					//***
		SEQUENCE,
		SERIES_END,						//***
		SERIES_START,					//***
		SERIES_TYPE,					//***
		SHAPE_OF_SAMPLE,				//***
		//SITE,							//USED FOR INRA
		SITE_CODE,						//***
		SOCIAL_STAND,
		SOIL_TYPE,
		SPECIES,						//***
		SPECIES_NAME,					//***
		//StandardDeviation				//USED FOR TUCSON
		STATE,							//***
		STEM_DISK_NUMBER,				//***
		STREET,							//***
		//TIMBER,						//USED FOR SHEFFIELD
		TIMBER_HEIGHT,					//***	
		TIMBER_TYPE,
		TIMBER_WIDTH,					//***
		//TotalAutoCorrelation			//USED FOR CATRAS
		//TotalMean						//USED FOR CATRAS
		//TotalMeanSensitivity			//USED FOR CATRAS
		//TotalNORFAC					//USED FOR CATRAS
		//TotalNORFM					//USED FOR CATRAS
		//TotalStandardDeviation		//USED FOR CATRAS
		TOWN,							//***
		TOWN_ZIP_CODE,					//***
		//Tree							//USED FOR INRA AND SHEFFIELD
		TREE_HEIGHT,					//***
		TREE_NUMBER,					//***
		UNIT,							//***
		//UNMEASURED_INNER_RINGS,		//USED FOR SHEFFIELD			
		//UNMEASURED_OUTER_RINGS,		//USED FOR SHEFFIELD
		WALDKANTE,						//***
		WOOD_MATERIAL_TYPE,
		WORK_TRACES
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.BARK, new GenericDefaultValue<FHBarkType>());
		setDefaultValue(DefaultFields.CORE_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.COUNTRY, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATA_FORMAT, new GenericDefaultValue<FHDataFormat>());
		setDefaultValue(DefaultFields.DATA_TYPE, new GenericDefaultValue<FHDataType>());
		setDefaultValue(DefaultFields.DATE_BEGIN, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATED, new GenericDefaultValue<FHDated>());
		setDefaultValue(DefaultFields.DATE_END, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATE_OF_SAMPLING, new StringDefaultValue());
		setDefaultValue(DefaultFields.DISTRICT, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEVATION, new StringDefaultValue());
		setDefaultValue(DefaultFields.ESTIMATED_TIME_PERIOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.FIRST_MEASUREMENT_DATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.HOUSE_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.HOUSE_NUMBER, new StringDefaultValue());	
		setDefaultValue(DefaultFields.KEYCODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.LAB_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.LAST_REVISION_DATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.LAST_REVISION_PERS_ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.LATITUDE, new StringDefaultValue());
		setDefaultValue(DefaultFields.LENGTH, new StringDefaultValue());
		setDefaultValue(DefaultFields.LOCATION, new StringDefaultValue());
		setDefaultValue(DefaultFields.LOCATION_CHARACTERISTICS, new StringDefaultValue());
		setDefaultValue(DefaultFields.LONGITUDE, new StringDefaultValue());
		setDefaultValue(DefaultFields.MISSING_RINGS_AFTER, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.MISSING_RINGS_BEFORE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.PITH, new GenericDefaultValue<FHPith>());
		setDefaultValue(DefaultFields.PROJECT, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROVINCE, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLING_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD_RINGS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SERIES_END, new GenericDefaultValue<FHStartsOrEndsWith>());
		setDefaultValue(DefaultFields.SERIES_START, new GenericDefaultValue<FHStartsOrEndsWith>());
		setDefaultValue(DefaultFields.SERIES_TYPE, new GenericDefaultValue<FHSeriesType>());
		setDefaultValue(DefaultFields.SHAPE_OF_SAMPLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SITE_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SOIL_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SPECIES, new GenericDefaultValue<ControlledVoc>());
		setDefaultValue(DefaultFields.SPECIES_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.STATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.STEM_DISK_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.STREET, new StringDefaultValue());
		setDefaultValue(DefaultFields.TIMBER_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.TIMBER_WIDTH, new StringDefaultValue());
		setDefaultValue(DefaultFields.TOWN, new StringDefaultValue());
		setDefaultValue(DefaultFields.TOWN_ZIP_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.TREE_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.TREE_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.UNIT, new GenericDefaultValue<TridasUnit>());	
		setDefaultValue(DefaultFields.WALDKANTE, new GenericDefaultValue<FHWaldKante>());
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasDerivedSeries()
	 */
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
		TridasDerivedSeries series = super.getDefaultTridasDerivedSeries();
		
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue());
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		series.setIdentifier(id);
		
		// FIXME detect ad/bc
		TridasInterpretation interp = new TridasInterpretation();
		if (getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue() != null) {
			SafeIntYear startYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue());
			interp.setFirstYear(startYear.toTridasYear(DatingSuffix.AD));
		}
		if (getIntegerDefaultValue(DefaultFields.DATE_END).getValue() != null) {
			SafeIntYear endYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.DATE_END).getValue());
			interp.setLastYear(endYear.toTridasYear(DatingSuffix.AD));
		}
		series.setInterpretation(interp);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime());
		
		series.setStandardizingMethod(getDefaultValue(DefaultFields.SERIES_TYPE).getStringValue());
		
		return series;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue());
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		series.setIdentifier(id);
		
		// FIXME detect ad/bc
		TridasInterpretation interp = new TridasInterpretation();
		if (getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue() != null) {
			SafeIntYear startYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue());
			interp.setFirstYear(startYear.toTridasYear(DatingSuffix.AD));
		}
		if (getIntegerDefaultValue(DefaultFields.DATE_END).getValue() != null) {
			SafeIntYear endYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.DATE_END).getValue());
			interp.setLastYear(endYear.toTridasYear(DatingSuffix.AD));
		}
		series.setInterpretation(interp);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime());
		
		return series;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		ControlledVoc v = (ControlledVoc) getDefaultValue(DefaultFields.SPECIES).getValue();
		e.setTaxon(v);
		return e;
	}
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
		
		GenericDefaultValue<TridasUnit> units = (GenericDefaultValue<TridasUnit>) getDefaultValue(DefaultFields.UNIT);
		if (units.getValue() == null) {
			valuesGroup.setUnitless(new TridasUnitless());
		}
		else {
			valuesGroup.setUnit(units.getValue());
		}
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		return valuesGroup;
	}
	
	
	
	
	
	
	public enum FHBarkType {
		AVAILABLE("B"), UNAVAILABLE("-");
		
		private String code;
		
		FHBarkType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHBarkType fromCode(String code) {
			for (FHBarkType val : FHBarkType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHDataFormat {
		Double, Single, Chrono, HalfChrono, Quadro, Tree, Table, Unknown;
		// formatted this way to match the string
	}
	
	public enum FHDataType {
		RING_WIDTH("Ringwidth"),
		EARLY_WOOD("Earlywood"),
		LATE_WOOD("Latewood"),
		EARLY_LATE_WOOD("EarlyLateWood"),
		MIN_DENSITY("Min density"),
		MAX_DENSITY("Max density"),
		EARLY_WOOD_DENSITY("Earlywood density"),
		LATE_WOOD_DENSITY("Latewood density"),
		PITH_AGE("Pith age"),
		WEIGHT_OF_RING("Weight of ring");
		
		private String code;
		
		FHDataType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
				
		public static FHDataType fromCode(String code) {
			for (FHDataType val : FHDataType.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHDated {
		Undated, Dated, RelDated;
	}
	
	public enum FHPith {
		PRESENT("P"), ABSENT("-");
		
		private String code;
		
		FHPith(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHPith fromCode(String code) {
			for (FHPith val : FHPith.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHStartsOrEndsWith {
		RING_WIDTH("Ring width"), EARLYWOOD("Earlywood"), LATEWOOD("Latewood");
		
		private String code;
		
		FHStartsOrEndsWith(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHStartsOrEndsWith fromCode(String code) {
			for (FHStartsOrEndsWith val : FHStartsOrEndsWith.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHSeriesType {
		SINGLE_CURVE("Single curve"), 
		MEAN_CURVE("Mean curve"), 
		RADIUS("Radius"),
		CHRONOLOGY("Chronology"),
		AUTOCORRELATION("Autocorrelation");
		
		private String code;
		
		FHSeriesType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHSeriesType fromCode(String code) {
			for (FHSeriesType val : FHSeriesType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHWaldKante {
		EARLYWOOD("WKE"),
		LATEWOOD("WKL"),
		UNKNOWN("WKX"),
		INDISTINCT("WK?"),
		NONE("---");
		
		private String code;
		
		FHWaldKante(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHWaldKante fromCode(String code) {
			for (FHWaldKante val : FHWaldKante.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
}