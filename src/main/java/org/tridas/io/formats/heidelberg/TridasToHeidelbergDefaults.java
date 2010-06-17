package org.tridas.io.formats.heidelberg;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHBarkType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataFormat;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDated;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHPith;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHSeriesType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHStartsOrEndsWith;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHWaldKante;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToHeidelbergDefaults extends AbstractMetadataFieldSet implements IMetadataFieldSet {
	
	private static final SimpleLogger log = new SimpleLogger(TridasToHeidelbergDefaults.class);
	
	
	@Override
	protected void initDefaultValues() {
		/*setDefaultValue(HeidelbergField.KEY_CODE, new StringDefaultValue(UUID.randomUUID().toString()));
		setDefaultValue(HeidelbergField.DATA_FORMAT, new StringDefaultValue("Tree"));
		setDefaultValue(HeidelbergField.SERIES_TYPE, new StringDefaultValue());
		setDefaultValue(HeidelbergField.LENGTH, new IntegerDefaultValue());
		setDefaultValue(HeidelbergField.DATEBEGIN, new IntegerDefaultValue(1001));
		setDefaultValue(HeidelbergField.DATEEND, new IntegerDefaultValue());
		setDefaultValue(HeidelbergField.DATED, new StringDefaultValue());
		setDefaultValue(HeidelbergField.SPECIES, new StringDefaultValue("UNKN"));
		setDefaultValue(HeidelbergField.UNIT, new StringDefaultValue());
		setDefaultValue(HeidelbergField.PROJECT, new StringDefaultValue());
		*/
		setDefaultValue(DefaultFields.BARK, new StringDefaultValue());
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
		setDefaultValue(DefaultFields.LATITUDE, new DoubleDefaultValue(null, -90.0, 90.0));
		setDefaultValue(DefaultFields.LENGTH, new StringDefaultValue());
		setDefaultValue(DefaultFields.LOCATION, new StringDefaultValue());
		setDefaultValue(DefaultFields.LOCATION_CHARACTERISTICS, new StringDefaultValue());
		setDefaultValue(DefaultFields.LONGITUDE, new DoubleDefaultValue(null, -180.0, 180.0));
		setDefaultValue(DefaultFields.MISSING_RINGS_AFTER, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.MISSING_RINGS_BEFORE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.PITH, new GenericDefaultValue<FHPith>());
		setDefaultValue(DefaultFields.PROJECT, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROVINCE, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLING_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLING_POINT, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD_RINGS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SERIES_END, new GenericDefaultValue<FHStartsOrEndsWith>());
		setDefaultValue(DefaultFields.SERIES_START, new GenericDefaultValue<FHStartsOrEndsWith>());
		setDefaultValue(DefaultFields.SERIES_TYPE, new GenericDefaultValue<FHSeriesType>());
		setDefaultValue(DefaultFields.SHAPE_OF_SAMPLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SITE_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SOIL_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue(ITRDBTaxonConverter.getCodeFromName(null)));
		setDefaultValue(DefaultFields.SPECIES_NAME, new StringDefaultValue(ITRDBTaxonConverter.getNameFromCode(null)));
		setDefaultValue(DefaultFields.STATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.STEM_DISK_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.STREET, new StringDefaultValue());
		setDefaultValue(DefaultFields.TIMBER_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.TIMBER_WIDTH, new StringDefaultValue());
		setDefaultValue(DefaultFields.TOWN, new StringDefaultValue());
		setDefaultValue(DefaultFields.TOWN_ZIP_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.TREE_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.TREE_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.UNIT, new StringDefaultValue());	
		setDefaultValue(DefaultFields.WALDKANTE, new GenericDefaultValue<FHWaldKante>());
		
		
	}
	
	public void populateFromTridasProject(TridasProject argProject) {
		getStringDefaultValue(DefaultFields.PROJECT).setValue(argProject.getTitle());
		

	}
	
	public void populateFromTridasElement(TridasElement argElement) {
		
		// Tree Number
		if(argElement.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.TREE_NUMBER).setValue(argElement.getTitle());
		}
		
		// Set Taxon fields
		if (argElement.isSetTaxon()) {
			
			if(argElement.getTaxon().getNormal()!=null)
			{
				// Set the species field to the controlled voc id if possible 
				getStringDefaultValue(DefaultFields.SPECIES).setValue(argElement.getTaxon().getNormalId());
			}
			
			// Use the value of the taxon tag as the human readable species name field
			getStringDefaultValue(DefaultFields.SPECIES_NAME).setValue(argElement.getTaxon().getValue());
		}
		
		// Altitude
		if(argElement.isSetAltitude())
		{
			getStringDefaultValue(DefaultFields.ELEVATION).setValue(argElement.getAltitude().toString()+"m");
		}
		
		// Shape
		if(argElement.isSetShape())
		{
			if(argElement.getShape().isSetNormal())
			{
				getStringDefaultValue(DefaultFields.SHAPE_OF_SAMPLE).setValue(argElement.getShape().getNormalTridas().toString());
			}
			else 
			{
				getStringDefaultValue(DefaultFields.SHAPE_OF_SAMPLE).setValue(argElement.getShape().getValue());
			}
		}
		
		// Soil description
		if(argElement.isSetSoil())
		{
			if(argElement.getSoil().isSetDescription())
			{
				getStringDefaultValue(DefaultFields.SOIL_TYPE).setValue(argElement.getSoil().getDescription());
			}
		}
			
	}
	
	public void populateFromTridasRadius(TridasRadius argRadius){
		
		// Radius Number
		if(argRadius.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.RADIUS_NUMBER).setValue(argRadius.getTitle());
		}
		
	}
	
	public void populateFromTridasSample(TridasSample argSample) {

		// Core Number
		getStringDefaultValue(DefaultFields.CORE_NUMBER).setValue(argSample.getTitle());

		// Sampling Date
		if(argSample.isSetSamplingDate())
		{
			XMLGregorianCalendar xcal = argSample.getSamplingDate().getValue();
			Date dt = xcal.toGregorianCalendar().getTime();
			getStringDefaultValue(DefaultFields.DATE_OF_SAMPLING).setValue(dt.toString());
		}
		
		// Sampling position
		if(argSample.isSetPosition())
		{
			getStringDefaultValue(DefaultFields.SAMPLING_POINT).setValue(argSample.getPosition());
		}
		
		
		
	}
	
	public void populateFromTridasValues(TridasValues argValues) {
		if (argValues.isSetUnitless() || !argValues.isSetUnit()) {
			return;
		}
		
		if (argValues.getUnit().getNormalTridas() == null) {
			return;
		}
		
		TridasUnit units = argValues.getUnit();
		StringDefaultValue val = getStringDefaultValue(DefaultFields.UNIT);
		switch (units.getNormalTridas()) {
			case HUNDREDTH_MM :
				val.setValue("1/100 mm");
				break;
			case MICROMETRES :
				val.setValue("1/1000 mm");
				break;
			case MILLIMETRES :
				val.setValue("mm");
				break;
			case TENTH_MM :
				val.setValue("1/10 mm");
				break;
			default :
				addIgnoredWarning(DefaultFields.UNIT, I18n.getText("fileio.invalidUnits"));
		}
	}
	
	public void populateFromMS(TridasMeasurementSeries argSeries) {
		populateFromSeries(argSeries);
	}
	
	public void populateFromDerivedSeries(TridasDerivedSeries argSeries) {
		populateFromSeries(argSeries);
		
		/*if (argSeries.isSetStandardizingMethod()) {
			getStringDefaultValue(DefaultFields.SERIES_TYPE).setValue(argSeries.getStandardizingMethod());
		}*/
	}
	
	private void populateFromSeries(ITridasSeries argSeries) {
		
		TridasIdentifier id = argSeries.getIdentifier();
		
		if (id != null) {
			if (id.isSetValue()) {
				getStringDefaultValue(DefaultFields.KEYCODE).setValue(id.getValue());
			}
		}
		
		TridasInterpretation interp = argSeries.getInterpretation();
		if (interp != null) {
			if (interp.isSetFirstYear()) {
				getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setValue(Integer.parseInt((new SafeIntYear(interp.getFirstYear()).toString())));
			}
			if (interp.isSetLastYear()) {
				getIntegerDefaultValue(DefaultFields.DATE_END).setValue(Integer.parseInt((new SafeIntYear(interp.getLastYear()).toString())));
			}

		}
	}
	
	public void populateFromTridasObject(TridasObject o)
	{
		// Site Code
		if(o.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SITE_CODE).setValue(o.getTitle());
		}
		
		// Estimated Time Period
		if(o.isSetCoverage())
		{
			if (o.getCoverage().isSetCoverageTemporal())
			{
				getStringDefaultValue(DefaultFields.ESTIMATED_TIME_PERIOD).setValue(o.getCoverage().getCoverageTemporal());
			}
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
		
		// Do Address fields
		if(location.isSetAddress())
		{
			// Country
			if(location.getAddress().isSetCountry())
			{
				getStringDefaultValue(DefaultFields.COUNTRY).setValue(location.getAddress().getCountry());
			}			
			
			// State / Province / Region
			if(location.getAddress().isSetStateProvinceRegion())
			{
				getStringDefaultValue(DefaultFields.STATE).setValue(location.getAddress().getStateProvinceRegion());
				getStringDefaultValue(DefaultFields.PROVINCE).setValue(location.getAddress().getStateProvinceRegion());
			}
			
			// Town/City
			if(location.getAddress().isSetCityOrTown())
			{
				getStringDefaultValue(DefaultFields.TOWN).setValue(location.getAddress().getCityOrTown());
			}
			
			// Postal code
			if(location.getAddress().isSetPostalCode())
			{
				getStringDefaultValue(DefaultFields.TOWN_ZIP_CODE).setValue(location.getAddress().getPostalCode());
			}
			
			// Street
			if(location.getAddress().isSetAddressLine2())
			{
				getStringDefaultValue(DefaultFields.STREET).setValue(location.getAddress().getAddressLine2());
			}
			
			// House Name
			if(location.getAddress().isSetAddressLine1())
			{
				getStringDefaultValue(DefaultFields.HOUSE_NAME).setValue(location.getAddress().getAddressLine1());
			}
		}
		
		
		// Do Coordinate fields
		if(location.isSetLocationGeometry())
		{	
			try{
				List<Double> points = null;
				points = location.getLocationGeometry().getPoint().getPos().getValues();
				if(points.size()!=2) { return;}
				getStringDefaultValue(DefaultFields.LATITUDE).setValue(points.get(0).toString());
				getStringDefaultValue(DefaultFields.LONGITUDE).setValue(points.get(1).toString());
			} catch (Exception ex){	}
		}
		
		// Location comment
		if(location.isSetLocationComment())
		{
			getStringDefaultValue(DefaultFields.LOCATION).setValue(location.getLocationComment());
		}
		
	}
	
	@SuppressWarnings("unchecked") 
	public void populateFromWoodCompleteness(TridasMeasurementSeries series, TridasRadius radius)
	{
		// Get the wood completeness from the series if possible, if not then try the radius
		TridasWoodCompleteness wc = null;
		if (series.isSetWoodCompleteness())
		{
			wc = series.getWoodCompleteness();
		}
		else if (radius.isSetWoodCompleteness())
		{
			wc = radius.getWoodCompleteness();
		}
		
		// Woodcompleteness not there so return without doing anything
		if(wc==null) {return ;}
		
		// Bark
		if(wc.isSetBark())
		{
			PresenceAbsence bark = wc.getBark().getPresence();
			
			if(bark.equals(PresenceAbsence.PRESENT))
			{
				getStringDefaultValue(DefaultFields.BARK).setValue(FHBarkType.AVAILABLE.toCode());
			}
			else if (bark.equals(PresenceAbsence.ABSENT))
			{
				getStringDefaultValue(DefaultFields.BARK).setValue(FHBarkType.UNAVAILABLE.toCode());
			}
		}
		
		// Sapwood rings
		if(wc.isSetSapwood())
		{
			if(wc.getSapwood().isSetNrOfSapwoodRings())
			{
				getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).setValue(wc.getSapwood().getNrOfSapwoodRings());
			}
		}


		
		
		
		
	}

	
}
