package org.tridas.io.formats.kinsys;

import java.math.BigDecimal;
import java.util.ArrayList;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.apache.commons.lang.WordUtils;
import org.tridas.io.I18n;
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
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLastRingUnderBark;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;
import org.tridas.spatial.SpatialUtils;

public class KinsysToTridasDefaults extends TridasMetadataFieldSet {

	public static enum DefaultFields {
		
		CREATION_DATE,
		PROJECT_CODE,
		PROJECT_NAME,
		INVESTIGATOR,
		SAMPLING_DATE,
		FINNISH_Y_COORD,
		FINNISH_X_COORD,
		ELEVATION,
		EXPERIMENT_NAME,
		PERIOD_OF_MEASUREMENT,
		LOCATION_NAME,
		PLOT,
		SUBPLOT,
		SAMPLE_TREE_NUMBER,
		RUNNING_MEASUREMENT_CODE,
		ID_NUMBER,
		SPECIES,
		LAST_MEASUREMENT_YEAR,
		SUBSAMPLE_CODE,
		INCOMPLETE_GROWTH_RING_MEASURED,
		ESTIMATED_AGE_INCREASE,
		LAST_RING_TYPE,
		LATEWOOD,
		MEASUREMENT_RADIUS,
		SAMPLE_AZIMUTH,
		PITH_TO_BARK,
		HEIGHT,
		HEIGHT_CODE,
		USER_PARAM,
		TOTAL_RING_COUNT,
		DATA_TYPE,
		MEASURED_RING_COUNT,
		/*COLUMN_COUNT,
		NUMBER_OF_DECIMALS,
		NUMBER_OF_MEASURED_RINGS*/
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		
		setDefaultValue(DefaultFields.CREATION_DATE, new DateTimeDefaultValue());
		
		setDefaultValue(DefaultFields.PROJECT_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_NAME, new StringDefaultValue(I18n.getText("unknown.project")));
		setDefaultValue(DefaultFields.INVESTIGATOR, new StringDefaultValue());
		
		setDefaultValue(DefaultFields.SAMPLING_DATE, new DateTimeDefaultValue());
	
		setDefaultValue(DefaultFields.FINNISH_X_COORD, new DoubleDefaultValue());
		setDefaultValue(DefaultFields.FINNISH_Y_COORD, new DoubleDefaultValue());
		setDefaultValue(DefaultFields.ELEVATION, new DoubleDefaultValue(null, -418.0, 8850.0)); // Heights of Dead Sea and Everest! ;-)

		
		setDefaultValue(DefaultFields.EXPERIMENT_NAME, new StringDefaultValue());		
		setDefaultValue(DefaultFields.PERIOD_OF_MEASUREMENT, new StringDefaultValue());
		setDefaultValue(DefaultFields.LOCATION_NAME, new StringDefaultValue());
		
		setDefaultValue(DefaultFields.PLOT, new StringDefaultValue());
		setDefaultValue(DefaultFields.SUBPLOT, new StringDefaultValue());
		
		setDefaultValue(DefaultFields.SAMPLE_TREE_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.RUNNING_MEASUREMENT_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.ID_NUMBER, new StringDefaultValue());
		
		setDefaultValue(DefaultFields.SPECIES, new GenericDefaultValue<ControlledVoc>());
		
		setDefaultValue(DefaultFields.LAST_MEASUREMENT_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.SUBSAMPLE_CODE, new StringDefaultValue());
		
		setDefaultValue(DefaultFields.INCOMPLETE_GROWTH_RING_MEASURED, new BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.ESTIMATED_AGE_INCREASE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.LAST_RING_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.LATEWOOD, new IntegerDefaultValue(1,2));
		
		setDefaultValue(DefaultFields.MEASUREMENT_RADIUS, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_AZIMUTH, new DoubleDefaultValue(null, 0.0, 360.0));
		setDefaultValue(DefaultFields.PITH_TO_BARK, new BooleanDefaultValue(true));
		setDefaultValue(DefaultFields.HEIGHT, new DoubleDefaultValue());
		setDefaultValue(DefaultFields.HEIGHT_CODE, new StringDefaultValue(null, 0, 1));
		
		setDefaultValue(DefaultFields.USER_PARAM, new StringDefaultValue());
	
		setDefaultValue(DefaultFields.TOTAL_RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATA_TYPE, new GenericDefaultValue<KinsysDataType>());
		
		setDefaultValue(DefaultFields.MEASURED_RING_COUNT, new IntegerDefaultValue());

	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasProject()
	 */
	@Override
	protected TridasProject getDefaultTridasProject() {
		TridasProject p = super.getDefaultTridasProject();
		
		// Title
		if(getStringDefaultValue(DefaultFields.PROJECT_NAME).getStringValue()!=null)
		{
			p.setTitle(getStringDefaultValue(DefaultFields.PROJECT_NAME).getStringValue());
		}
		
		// Investigator
		if(getStringDefaultValue(DefaultFields.INVESTIGATOR).getStringValue()!=null)
		{
			p.setInvestigator(getStringDefaultValue(DefaultFields.INVESTIGATOR).getStringValue());
		}
		
		// ID Code
		if(getStringDefaultValue(DefaultFields.PROJECT_CODE).getStringValue()!=null)
		{
			TridasIdentifier id = new TridasIdentifier();
			id.setDomain(I18n.getText("domain.value"));
			id.setValue(getStringDefaultValue(DefaultFields.PROJECT_CODE).getStringValue());
			p.setIdentifier(id);
		}
		
		return p;
		
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasObject()
	 */
	@Override
	protected TridasObject getDefaultTridasObject() {
		TridasObject o = getDefaultObj(false);
		
		if(getDoubleDefaultValue(DefaultFields.FINNISH_X_COORD).getValue()!=null && 
				getDoubleDefaultValue(DefaultFields.FINNISH_Y_COORD).getValue()!=null)
		{
			TridasLocationGeometry geometry = new TridasLocationGeometry();
			Pos pos = new Pos();
			ArrayList<Double> values = new ArrayList<Double>();
			
			values.add(getDoubleDefaultValue(DefaultFields.FINNISH_X_COORD).getValue());
			values.add(getDoubleDefaultValue(DefaultFields.FINNISH_Y_COORD).getValue());
			pos.setValues(values);
			
			// Finnish KKJ
			PointType point = new PointType();
			point.setSrsName("EPSG:2393");
			point.setPos(pos);
			geometry.setPoint(point);
			
			
			TridasLocation location = new TridasLocation();
			location.setLocationGeometry(geometry);
			location.setLocationPrecision("1");
			
			if(getStringDefaultValue(DefaultFields.LOCATION_NAME).getStringValue()!=null)
			{
				location.setLocationComment(getStringDefaultValue(DefaultFields.LOCATION_NAME).getStringValue());
			}
			
			
			
			o.setLocation(location);

		}
		
		
		return o;
	}
	
	protected TridasObject getDefaultTridasSubObject() {
		return getDefaultObj(true);
	}
	
	private TridasObject getDefaultObj(Boolean isSub)
	{
		TridasObject o = super.getDefaultTridasObject();
		
		if(isSub)
		{
			// Title
			if(getStringDefaultValue(DefaultFields.SUBPLOT).getStringValue()!=null)
			{
				o.setTitle(getStringDefaultValue(DefaultFields.SUBPLOT).getStringValue());
			}
			
			// Type
			ControlledVoc type = new ControlledVoc();
			type.setValue("Sub-plot");
			o.setType(type);
		}
		else
		{
			// Title
			if(getStringDefaultValue(DefaultFields.PLOT).getStringValue()!=null)
			{
				o.setTitle(getStringDefaultValue(DefaultFields.PLOT).getStringValue());
			}
			
			// Type
			ControlledVoc type = new ControlledVoc();
			type.setValue("Plot");
			o.setType(type);
		}
		
		return o;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		
		// Title
		if(getStringDefaultValue(DefaultFields.SAMPLE_TREE_NUMBER).getStringValue()!=null)
		{
			e.setTitle(getStringDefaultValue(DefaultFields.SAMPLE_TREE_NUMBER).getStringValue());
		}
		
		
		// Altitude
		if(getDoubleDefaultValue(DefaultFields.ELEVATION).getStringValue()!=null)
		{
			e.setAltitude(getDoubleDefaultValue(DefaultFields.ELEVATION).getValue());
		}
		
		// Species
		GenericDefaultValue<ControlledVoc> speciesField = (GenericDefaultValue<ControlledVoc>) getDefaultValue(DefaultFields.SPECIES);
		if(speciesField.getValue()!=null)
		{
			e.setTaxon(speciesField.getValue());
		}
		
		return e;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasSample()
	 */
	@Override
	protected TridasSample getDefaultTridasSample() {
		TridasSample s = super.getDefaultTridasSample();
		
		// Sampling date
		if(getDateTimeDefaultValue(DefaultFields.SAMPLING_DATE).getValue()!=null)
		{
			s.setSamplingDate(DateUtils.dateTimeToDate(
					getDateTimeDefaultValue(DefaultFields.SAMPLING_DATE).getValue()));
		}

		// Height
		if(getDoubleDefaultValue(DefaultFields.HEIGHT).getValue()!=null)
		{
			s.setPosition("Height of sample: "+getDoubleDefaultValue(DefaultFields.HEIGHT).getValue());
		}
		

		
		return s;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasRadius()
	 */
	@Override
	protected TridasRadius getDefaultTridasRadius() {
		TridasRadius r = super.getDefaultTridasRadius();
		
		TridasWoodCompleteness wc = new TridasWoodCompleteness();
		TridasPith pith = new TridasPith();
		TridasHeartwood hw = new TridasHeartwood();
		TridasSapwood sw = new TridasSapwood();
		TridasBark bark = new TridasBark();
		
		pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
		hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
		sw.setPresence(ComplexPresenceAbsence.UNKNOWN);
		bark.setPresence(PresenceAbsence.UNKNOWN);
		
		if(getIntegerDefaultValue(DefaultFields.MEASURED_RING_COUNT).getValue()!=null)
		{
			wc.setRingCount(getIntegerDefaultValue(DefaultFields.MEASURED_RING_COUNT).getValue());
		}

		if(getBooleanDefaultValue(DefaultFields.INCOMPLETE_GROWTH_RING_MEASURED).getValue()==false)
		{
			wc.setNrOfUnmeasuredOuterRings(1);
		}
		
		if(getStringDefaultValue(DefaultFields.LAST_RING_TYPE).getValue()!=null)
		{
			TridasLastRingUnderBark lrub = new TridasLastRingUnderBark();
			lrub.setPresence(PresenceAbsence.PRESENT);
			lrub.setContent(getStringDefaultValue(DefaultFields.LAST_RING_TYPE).getValue());
			sw.setLastRingUnderBark(lrub);
		}
		
		wc.setPith(pith);
		wc.setHeartwood(hw);
		wc.setSapwood(sw);
		wc.setBark(bark);
		r.setWoodCompleteness(wc);
		
		
		// Azimuth
		if(getDoubleDefaultValue(DefaultFields.SAMPLE_AZIMUTH).getValue()!=null)
		{
			r.setAzimuth(BigDecimal.valueOf(getDoubleDefaultValue(DefaultFields.SAMPLE_AZIMUTH).getValue()));
		}
		
		return r;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();	
		
		if(getStringDefaultValue(DefaultFields.ID_NUMBER).getStringValue()!=null)
		{
			series.setTitle(getStringDefaultValue(DefaultFields.ID_NUMBER).getStringValue());
		}
		
		if(getStringDefaultValue(DefaultFields.USER_PARAM).getStringValue()!=null)
		{
			series.setComments(getStringDefaultValue(DefaultFields.SUBPLOT).getStringValue());
		}
		
		if(getSafeIntYearDefaultValue(DefaultFields.LAST_MEASUREMENT_YEAR).getValue()!=null)
		{
			TridasInterpretation interp = new TridasInterpretation();
			interp.setLastYear(getSafeIntYearDefaultValue(DefaultFields.LAST_MEASUREMENT_YEAR).getValue().toTridasYear(DatingSuffix.AD));
			
			if(getIntegerDefaultValue(DefaultFields.MEASURED_RING_COUNT).getValue()!=null)
			{
				SafeIntYear firstYear = getSafeIntYearDefaultValue(DefaultFields.LAST_MEASUREMENT_YEAR).getValue().add(0-(getIntegerDefaultValue(DefaultFields.MEASURED_RING_COUNT).getValue()-1));
				interp.setFirstYear(firstYear.toTridasYear(DatingSuffix.AD));
			}
			series.setInterpretation(interp);
			
			TridasDating dating = new TridasDating();
			dating.setType(NormalTridasDatingType.ABSOLUTE);
			interp.setDating(dating);

		}
		
		return series;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasDerivedSeries()
	 */
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
		TridasDerivedSeries series = super.getDefaultTridasDerivedSeries();
	
		return series;
	}
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
		
		// Set units
		TridasUnit units = new TridasUnit();
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		valuesGroup.setUnit(units);
		
		// Set variable
		GenericDefaultValue<KinsysDataType> datatype = (GenericDefaultValue<KinsysDataType>) getDefaultValue(DefaultFields.DATA_TYPE);
		TridasVariable variable = new TridasVariable();
		if(datatype.equals(KinsysDataType.RINGWIDTH))
		{
			variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		}
		else if (datatype.equals(KinsysDataType.EARLY_WOOD))
		{
			variable.setNormalTridas(NormalTridasVariable.EARLYWOOD_WIDTH);
		}
		else if (datatype.equals(KinsysDataType.LATE_WOOD))
		{
			variable.setNormalTridas(NormalTridasVariable.LATEWOOD_WIDTH);
		}
		else
		{
			variable.setValue(datatype.getStringValue());
		}
		valuesGroup.setVariable(variable);
	
		
		return valuesGroup;
	}
	
	
	public enum KinsysDataType {
		RINGWIDTH("90"),
		HEIGHT_SHOOTS("91"),
		VOLUME_GROWTHS("92"),
		EARLY_WOOD("93"),
		LATE_WOOD("94");
		
		private String code;
		
		KinsysDataType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static KinsysDataType fromCode(String code) {
			for (KinsysDataType val : KinsysDataType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
}
