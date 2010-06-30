package org.tridas.io.formats.vformat;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatDataType;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatParameter;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatStatType;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToVFormatDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	@Override
	protected void initDefaultValues() {
		
		setDefaultValue(DefaultFields.PROJECT_CODE, new StringDefaultValue(null, 1, 1));
		setDefaultValue(DefaultFields.REGION_CODE, new StringDefaultValue("X", 1, 1));
		setDefaultValue(DefaultFields.OBJECT_CODE, new StringDefaultValue("XX", 2, 2));
		setDefaultValue(DefaultFields.TREE_CODE, new StringDefaultValue("XX", 2, 2));
		setDefaultValue(DefaultFields.HEIGHT_CODE, new StringDefaultValue("0", 1, 1));
		setDefaultValue(DefaultFields.DATA_TYPE, new GenericDefaultValue<VFormatDataType>(VFormatDataType.SINGLE));
		setDefaultValue(DefaultFields.STAT_CODE, new GenericDefaultValue<VFormatStatType>(VFormatStatType.ORIGINAL));
		setDefaultValue(DefaultFields.PARAMETER_CODE, new GenericDefaultValue<VFormatParameter>(VFormatParameter.RING_WIDTH));
		setDefaultValue(DefaultFields.UNIT, new StringDefaultValue("mm", 3, 3));
		setDefaultValue(DefaultFields.COUNT, new IntegerDefaultValue(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 5, 5));
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue("UNKN", 4, 4));
		setDefaultValue(DefaultFields.LAST_YEAR, new SafeIntYearDefaultValue(6, 6));
		setDefaultValue(DefaultFields.FIRST_YEAR, new SafeIntYearDefaultValue(6, 6));
		setDefaultValue(DefaultFields.DESCRIPTION, new StringDefaultValue(I18n.getText("unnamed.series"), 20, 20));
		setDefaultValue(DefaultFields.CREATED_DATE, new DateTimeDefaultValue(DateUtils.getTodaysDateTime(), new SimpleDateFormat("ddMMyyyy")));
		setDefaultValue(DefaultFields.ANALYST, new StringDefaultValue("", 2, 2));
		setDefaultValue(DefaultFields.UPDATED_DATE, new DateTimeDefaultValue(DateUtils.getTodaysDateTime(), new SimpleDateFormat("ddMMyyyy")));
		setDefaultValue(DefaultFields.FORMAT_VERSION, new IntegerDefaultValue(12, 12, 12, 2, 2));
		setDefaultValue(DefaultFields.UNMEAS_PRE, new IntegerDefaultValue(0, 0, Integer.MAX_VALUE, 3, 3));
		setDefaultValue(DefaultFields.UNMEAS_PRE_ERR, new StringDefaultValue("", 2, 2));
		setDefaultValue(DefaultFields.UNMEAS_POST, new IntegerDefaultValue(0, 0, Integer.MAX_VALUE, 3, 3));
		setDefaultValue(DefaultFields.UNMEAS_POST_ERR, new StringDefaultValue("", 2, 2));
		setDefaultValue(DefaultFields.FREE_TEXT_FIELD, new StringDefaultValue("", 80, 80));
		setDefaultValue(DefaultFields.LATITUDE, new DoubleDefaultValue(null, -90.0, 90.0, 10, 10));
		setDefaultValue(DefaultFields.LONGITUDE, new DoubleDefaultValue(null, -180.0, 180.0, 10, 10));
		setDefaultValue(DefaultFields.ELEVATION, new DoubleDefaultValue(null, -418.0, 8850.0, 10, 10)); // Heights of Dead Sea and Everest! ;-)

	}

	public void populateFromTridasProject(TridasProject p) {

		// Project code
		if(p.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.PROJECT_CODE).setValue(p.getTitle());
		}
	}
	
	public void populateFromTridasObject(TridasObject o) {

		// Object code
		if(o.isSetTitle())
		{
				getStringDefaultValue(DefaultFields.OBJECT_CODE).setValue(o.getTitle());
		}
	}
	
	public void populateFromTridasElement(TridasElement e) {

		// Tree code
		if(e.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.TREE_CODE).setValue(e.getTitle());
		}
		
		// Height code
		if(e.isSetDimensions())
		{
			setHeightFromElement(e);
		}
		
		// Species
		if(e.isSetTaxon())
		{
			if(e.getTaxon().isSetNormalId())
			{			
				getStringDefaultValue(DefaultFields.SPECIES).setValue(ITRDBTaxonConverter.getNormalisedCode(e.getTaxon().getNormalId()));
			}
			else if(e.getTaxon().isSetValue())
			{
				getStringDefaultValue(DefaultFields.SPECIES).setValue(ITRDBTaxonConverter.getCodeFromName(e.getTaxon().getValue()));
			}
		}
	
		// Elevation
		if(e.isSetAltitude())
		{
			getDoubleDefaultValue(DefaultFields.ELEVATION).setValue(e.getAltitude());
		}
	}
	
	/**
	 * Calculations to convert from a valid TRiDaS element height dimension
	 * to the single character VFormat height code.
	 * 
	 * @param e
	 */
	private void setHeightFromElement(TridasElement e)
	{
		if(e.getDimensions().isSetHeight())
		{
			// Determine units
			NormalTridasUnit unit = null;
			if (e.getDimensions().isSetUnit())
			{
				if (e.getDimensions().getUnit().isSetNormalTridas())
				{
					unit = e.getDimensions().getUnit().getNormalTridas();
				}
			}
			
			// No units so return 
			if (unit==null)
			{
				this.addConversionWarning(new ConversionWarning(WarningType.DEFAULT, I18n.getText("vformat.noUnitsOnHeight")));
				return;
			}
			
			// Grab height and convert to metres
			BigDecimal height = e.getDimensions().getHeight();
			height = UnitUtils.convertBigDecimal(unit, NormalTridasUnit.METRES, height);
			
			// Round to nearest metre
			Integer heightInt = Math.round(height.floatValue());
			
			// Check range
			if(heightInt>27)
			{
				this.addConversionWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, I18n.getText("vformat.heightOutOfRange")));
				return;
			}
			else if (heightInt<=0)
			{
				return;
			}
			
			// Convert to VFormat code character
			String ref = "0123456789ABCDEFGHIJKLMNOPQRX";
			getStringDefaultValue(DefaultFields.HEIGHT_CODE).setValue(ref.substring(heightInt, heightInt+1));
		}
	}

	public void populateFromTridasSample(TridasSample s) {


	}
	
	public void populateFromTridasRadius(TridasRadius r) {


	}
	
	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries ms) {

		populateFromTridasSeries(ms);

		// Analyst
		if(ms.isSetAnalyst())
		{
			getStringDefaultValue(DefaultFields.ANALYST).setValue(ms.getAnalyst());
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasDerivedSeries(TridasDerivedSeries ds) {
		
		populateFromTridasSeries(ds);
		
		// Data Type
		GenericDefaultValue<VFormatDataType> dataTypeField = (GenericDefaultValue<VFormatDataType>)getDefaultValue(DefaultFields.DATA_TYPE);
		dataTypeField.setValue(VFormatDataType.CHRONOLOGY);
		
		// Analyst
		if(ds.isSetAuthor())
		{
			getStringDefaultValue(DefaultFields.ANALYST).setValue(ds.getAuthor());
		}
		
	}
	
	private void populateFromTridasSeries(ITridasSeries ser)
	{
		
		if(ser.isSetInterpretation())
		{
			// First Year
			if(ser.getInterpretation().isSetFirstYear())
			{
				getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).setValue(new SafeIntYear(ser.getInterpretation().getFirstYear()));
			}
			
			// Last Year
			if(ser.getInterpretation().isSetLastYear())
			{
				getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).setValue(new SafeIntYear(ser.getInterpretation().getLastYear()));
			}
			
		}
		
		// Set the description to the series title
		if(ser.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.DESCRIPTION).setValue(ser.getTitle());
		}
		
		// Created date
		if(ser.isSetCreatedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.CREATED_DATE).setValue(ser.getCreatedTimestamp());
		}
		
		// Updated date
		if(ser.isSetLastModifiedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.UPDATED_DATE).setValue(ser.getLastModifiedTimestamp());
		}
		
		// Free text comments
		if(ser.isSetComments())
		{
			getStringDefaultValue(DefaultFields.FREE_TEXT_FIELD).setValue(ser.getComments());
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasValues(TridasValues argValues) {

		// Variable and units
		if(argValues.getVariable().isSetNormalTridas())
		{
			GenericDefaultValue<VFormatParameter> parameterField = 
				(GenericDefaultValue<VFormatParameter>)getDefaultValue(DefaultFields.PARAMETER_CODE);
			switch(argValues.getVariable().getNormalTridas())
			{
			case RING_WIDTH:
				parameterField.setValue(VFormatParameter.RING_WIDTH);
				break;
			case EARLYWOOD_WIDTH:
				parameterField.setValue(VFormatParameter.EARLYWOOD_WIDTH);
				break;
			case LATEWOOD_PERCENT:
				parameterField.setValue(VFormatParameter.PERC_LATEWOOD);
				getStringDefaultValue(DefaultFields.UNIT).setValue("%%");
				break;
			case LATEWOOD_WIDTH:
				parameterField.setValue(VFormatParameter.LATEWOOD_WIDTH);
				break;
			case MAXIMUM_DENSITY:
				parameterField.setValue(VFormatParameter.MAX_DENSITY);
				//TODO Find out correct density units
				getStringDefaultValue(DefaultFields.UNIT).setValue("XX");
				break;
			case RING_DENSITY:
				parameterField.setValue(VFormatParameter.MEAN_DENSITY);
				//TODO Find out correct density units
				getStringDefaultValue(DefaultFields.UNIT).setValue("XX");
				break;
			case LATEWOOD_DENSITY:
			case EARLYWOOD_DENSITY:
				// Unsupported
			}	
		}
		else
		{
			this.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
		}
		
		// Count
		getIntegerDefaultValue(DefaultFields.COUNT).setValue(argValues.getValues().size());
	}
	
	
	public void populateFromWoodCompleteness(TridasMeasurementSeries ms, TridasRadius r){
		
		TridasWoodCompleteness wc = null;
		
		// Get the wood completeness from the series if possible, if not then try the radius
		if (ms.isSetWoodCompleteness())
		{
			wc = ms.getWoodCompleteness();
		}
		else if (r.isSetWoodCompleteness())
		{
			wc = r.getWoodCompleteness();
		}
		
		// Woodcompleteness not there so return without doing anything
		if(wc==null) {return ;}
		
		// Unmeas pre
		if(wc.isSetNrOfUnmeasuredInnerRings())
		{
			getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).setValue(wc.getNrOfUnmeasuredInnerRings());
		}
		
		// Unmeas post
		if(wc.isSetNrOfUnmeasuredOuterRings())
		{
			getIntegerDefaultValue(DefaultFields.UNMEAS_POST).setValue(wc.getNrOfUnmeasuredOuterRings());
		}
	}
	
	/**
	 * Populate location fields from TridasObject and TridasElement entities.  If 
	 * location info is in the TridasElement use this as it is more detailed, 
	 * otherwise use the data from the TridasObject.
	 * 
	 * @param o
	 * @param e
	 */
	public void populateFromTridasLocation(TridasObject o, TridasElement e)
	{
		// Grab location from element, or object
		TridasLocation location = null;
		if(e.isSetLocation())
		{
			location = e.getLocation();
		}
		else if (o.isSetLocation())
		{
			location = o.getLocation();
		}	
		if (location==null) {return; }
		
		// Do Coordinate fields
		if(location.isSetLocationGeometry())
		{	
			try{
				List<Double> points = null;
				points = location.getLocationGeometry().getPoint().getPos().getValues();
				if(points.size()!=2) { return;}
				getDoubleDefaultValue(DefaultFields.LATITUDE).setValue(points.get(0));
				getDoubleDefaultValue(DefaultFields.LONGITUDE).setValue(points.get(1));
			} catch (Exception ex){	}
		}
		
	}
	
}
