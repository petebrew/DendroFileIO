package org.tridas.io.formats.fhx2;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.BooleanDefaultValue;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHBarkType;
import org.tridas.io.util.DateUtils;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasAddress;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasSlope;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;
import org.tridas.spatial.SpatialUtils;

public class FHX2ToTridasDefaults extends TridasMetadataFieldSet {
	public static enum DefaultFields {
		
		// Field marked with *** have been implemented
		
		FIRST_YEAR,
		LAST_YEAR,
		PITH,
		BARK,
		SERIES_NAME,

		SITE_CODE,
		COLLECTION_DATE,
		//COLLECTORS,
		CROSSDATERS,
		SPECIES_NAME,
		//HABITAT_TYPE,
		COUNTRY,
		//Park/Monument  : 
		//National Forest: Gila
		//Ranger district: 
		TOWN, 
		STATE,
		//Range          : 
		//Section        : 
		//Quarter section: 
		//UTM easting    : 
		//UTM northing   : 
		LATITUDE,
		LONGITUDE,
		//Topographic map: 
		//Lowest elev    : 1555 m
		//Highest elev   : 1555 m
		SLOPE, 
		ASPECT, 
		//Area sampled   : 
		//Substrate type : 
		COMMENTS;
	}
	
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.FIRST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.LAST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.PITH, new BooleanDefaultValue(null));
		setDefaultValue(DefaultFields.BARK, new BooleanDefaultValue(null));
		setDefaultValue(DefaultFields.SITE_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.COLLECTION_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.CROSSDATERS, new StringDefaultValue());
		setDefaultValue(DefaultFields.SPECIES_NAME, new GenericDefaultValue<ControlledVoc>());		
		setDefaultValue(DefaultFields.COUNTRY, new StringDefaultValue());
		setDefaultValue(DefaultFields.TOWN, new StringDefaultValue());
		setDefaultValue(DefaultFields.STATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.LATITUDE, new DoubleDefaultValue(null, -90.0, 90.0));
		setDefaultValue(DefaultFields.LONGITUDE, new DoubleDefaultValue(null, -180.0, 180.0));
		setDefaultValue(DefaultFields.SLOPE, new IntegerDefaultValue(null, 0, 360));
		setDefaultValue(DefaultFields.ASPECT, new IntegerDefaultValue(null, 0, 360));
		setDefaultValue(DefaultFields.COMMENTS, new StringDefaultValue());
	}
	
	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
	
		
		valuesGroup.setUnitless(new TridasUnitless());

		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
	
		return valuesGroup;
	}
	
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries(){
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		series.setTitle(getStringDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_TITLE).getStringValue());
		
		
		TridasInterpretation interp = new TridasInterpretation();
		interp.setFirstYear(getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue().toTridasYear(DatingSuffix.AD));
		interp.setLastYear(getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue().toTridasYear(DatingSuffix.AD));
		
		TridasDating dating = new TridasDating();
		dating.setType(NormalTridasDatingType.ABSOLUTE);
		interp.setDating(dating);
		

		
		TridasWoodCompleteness wc = new TridasWoodCompleteness();
		TridasPith pith = new TridasPith();
		
		if(getBooleanDefaultValue(DefaultFields.PITH).getValue().equals(true))
		{
			pith.setPresence(ComplexPresenceAbsence.COMPLETE);
			interp.setPithYear(getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue().toTridasYear(DatingSuffix.AD));

		}
		else
		{
			pith.setPresence(ComplexPresenceAbsence.ABSENT);
		}
		wc.setPith(pith);
		
		TridasBark bark = new TridasBark();
		if(getBooleanDefaultValue(DefaultFields.BARK).getValue().equals(true))
		{
			bark.setPresence(PresenceAbsence.PRESENT);
			interp.setDeathYear(getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue().toTridasYear(DatingSuffix.AD));
		}
		else
		{
			bark.setPresence(PresenceAbsence.ABSENT);
		}
		wc.setBark(bark);
		
		TridasHeartwood hw = new TridasHeartwood();
		TridasSapwood sw = new TridasSapwood();
		if(getBooleanDefaultValue(DefaultFields.PITH).getValue().equals(true) 
				&& getBooleanDefaultValue(DefaultFields.BARK).getValue().equals(true))
		{
			hw.setPresence(ComplexPresenceAbsence.COMPLETE);
			sw.setPresence(ComplexPresenceAbsence.COMPLETE);

		}
		else
		{
			hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
			sw.setPresence(ComplexPresenceAbsence.UNKNOWN);
		}
		
		wc.setHeartwood(hw);
		wc.setSapwood(sw);
		
		series.setInterpretation(interp);
		series.setWoodCompleteness(wc);
		
		
		if(getStringDefaultValue(DefaultFields.CROSSDATERS).getStringValue()!=null)
		{
			series.setDendrochronologist(getStringDefaultValue(DefaultFields.CROSSDATERS).getStringValue());
		}
		
		
		return series;
	}
	
	
	@Override
	protected TridasSample getDefaultTridasSample(){
		TridasSample s = super.getDefaultTridasSample();
		
		if(getDateTimeDefaultValue(DefaultFields.COLLECTION_DATE).getValue()!=null)
		{
			s.setSamplingDate(DateUtils.dateTimeToDate(getDateTimeDefaultValue(DefaultFields.COLLECTION_DATE).getValue()));
		}
		
		return s;
	}
	
	@Override
	protected TridasElement getDefaultTridasElement(){
		TridasElement e = super.getDefaultTridasElement();
		
		try{
		ControlledVoc v = (ControlledVoc) getDefaultValue(DefaultFields.SERIES_NAME).getValue();
		if(v!=null){
			if(v.isSetNormalId())
			{
				// Code was absent or invalid for ITRDB controlled voc so try the plain
				// species name instead
				if (getDefaultValue(DefaultFields.SPECIES_NAME).getValue()!=null)
				{
					v.setValue(getDefaultValue(DefaultFields.SPECIES_NAME).getValue().toString());
				}
			}
		}
		e.setTaxon(v);
		} catch (NullPointerException ex)
		{
			
		}
		


		
		if(getIntegerDefaultValue(DefaultFields.SLOPE).getValue()!=null)
		{
			TridasSlope slope = new TridasSlope();
			slope.setAngle(getIntegerDefaultValue(DefaultFields.SLOPE).getValue());
			
			if(getIntegerDefaultValue(DefaultFields.ASPECT).getValue()!=null)
			{
				slope.setAzimuth(getIntegerDefaultValue(DefaultFields.ASPECT).getValue());
			}
			e.setSlope(slope);
		}
		
		return e;
	}
	
	@Override
	protected TridasObject getDefaultTridasObject() {
		TridasObject o = super.getDefaultTridasObject();
		
		o.setTitle(getDefaultValue(TridasMandatoryField.OBJECT_TITLE).getStringValue());
		
		
		if(getDefaultValue(DefaultFields.SITE_CODE).getStringValue()!=null)
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("tellervo.objectLabCode");
			gf.setType("xs:tring");
			gf.setValue(getDefaultValue(DefaultFields.SITE_CODE).getStringValue());
		}
		
		if(getDefaultValue(DefaultFields.COUNTRY).getStringValue()!=null || 
		   getDefaultValue(DefaultFields.TOWN).getStringValue()!=null ||
		       (getDoubleDefaultValue(DefaultFields.LATITUDE).getValue()!=null && 
		        getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue()!=null)
		   )
		{
			TridasLocation location = new TridasLocation();
		
			if(getDoubleDefaultValue(DefaultFields.LATITUDE).getValue()!=null && 
			        getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue()!=null)
			{
				TridasLocationGeometry geom = SpatialUtils.getLocationGeometry(
						getDoubleDefaultValue(DefaultFields.LATITUDE).getValue(), 
						getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue());
				location.setLocationGeometry(geom);
			}
			
			if(getDefaultValue(DefaultFields.COUNTRY).getStringValue()!=null || 
					   getDefaultValue(DefaultFields.STATE).getStringValue()!=null|| 
					   getDefaultValue(DefaultFields.TOWN).getStringValue()!=null)
			{
				TridasAddress add = new TridasAddress();
				if(getDefaultValue(DefaultFields.COUNTRY).getStringValue()!=null)
				{
					add.setCountry(getDefaultValue(DefaultFields.COUNTRY).getStringValue());
				}
				
				if(getDefaultValue(DefaultFields.TOWN).getStringValue()!=null)
				{
					add.setCityOrTown(getDefaultValue(DefaultFields.TOWN).getStringValue());
				}
				
				if(getDefaultValue(DefaultFields.STATE).getStringValue()!=null)
				{
					add.setStateProvinceRegion(getDefaultValue(DefaultFields.STATE).getStringValue());
				}
				
				location.setAddress(add);
			}
			
			
			o.setLocation(location);
		}
		
		if(getDefaultValue(DefaultFields.COMMENTS).getStringValue()!=null)
		{
			o.setComments(getDefaultValue(DefaultFields.COMMENTS).getStringValue());
		}
		
		return o;
		
	}
	
}
