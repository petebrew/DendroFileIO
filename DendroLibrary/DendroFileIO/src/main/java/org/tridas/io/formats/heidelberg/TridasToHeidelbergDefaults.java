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
package org.tridas.io.formats.heidelberg;

import java.text.SimpleDateFormat;
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
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
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
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLastRingUnderBark;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToHeidelbergDefaults extends AbstractMetadataFieldSet implements IMetadataFieldSet {
	
	private static final SimpleLogger log = new SimpleLogger(TridasToHeidelbergDefaults.class);
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
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
		setDefaultValue(DefaultFields.KEYCODE, new StringDefaultValue("XXXXXX"));
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
		setDefaultValue(DefaultFields.PERS_ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.PITH, new StringDefaultValue());
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
		setDefaultValue(DefaultFields.WALDKANTE, new StringDefaultValue());
		
		
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
			
			if(argElement.getTaxon().getNormalId()!=null)
			{
				// Set the species field to the controlled voc id if possible 
				getStringDefaultValue(DefaultFields.SPECIES).setValue(ITRDBTaxonConverter.getNormalisedCode(argElement.getTaxon().getNormalId()));
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
				getStringDefaultValue(DefaultFields.SHAPE_OF_SAMPLE).setValue(argElement.getShape().getNormalTridas().value());
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
			
		// Dimensions
		if(argElement.isSetDimensions())
		{
			if(argElement.getDimensions().isSetHeight())
			{
				// Heights
				getStringDefaultValue(DefaultFields.TIMBER_HEIGHT).setValue(argElement.getDimensions().getHeight().toPlainString());
				getStringDefaultValue(DefaultFields.TREE_HEIGHT).setValue(argElement.getDimensions().getHeight().toPlainString());
			}
			if(argElement.getDimensions().isSetWidth())
			{
				// Width
				getStringDefaultValue(DefaultFields.TIMBER_WIDTH).setValue(argElement.getDimensions().getWidth().toPlainString());
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

		// Core Number / StemDiskNumber
		getStringDefaultValue(DefaultFields.CORE_NUMBER).setValue(argSample.getTitle());
		getStringDefaultValue(DefaultFields.STEM_DISK_NUMBER).setValue(argSample.getTitle());

		
		// Sampling Date
		if(argSample.isSetSamplingDate())
		{
			XMLGregorianCalendar xcal = argSample.getSamplingDate().getValue();
			Date dt = xcal.toGregorianCalendar().getTime();
			getStringDefaultValue(DefaultFields.DATE_OF_SAMPLING).setValue(dateFormat.format(dt));
		}
		
		// Sampling position
		if(argSample.isSetPosition())
		{
			getStringDefaultValue(DefaultFields.SAMPLING_POINT).setValue(argSample.getPosition());
		}

	}
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasValues(TridasValues argValues) {
		
		// Set Length
		getStringDefaultValue(DefaultFields.LENGTH).setValue(argValues.getValues().size()+"");
		
		GenericDefaultValue<FHDataType> variableField = (GenericDefaultValue<FHDataType>)getDefaultValue(DefaultFields.DATA_TYPE);
		
		// Data type (variable) = values.variable.normaltridas
		if(argValues.isSetVariable())
		{		
			if(argValues.getVariable().isSetNormalTridas())
			{
				switch(argValues.getVariable().getNormalTridas())
				{
				case RING_WIDTH:
					variableField.setValue(FHDataType.RING_WIDTH);
					break;
				case EARLYWOOD_WIDTH:
					variableField.setValue(FHDataType.EARLY_WOOD);
					break;	
				case MAXIMUM_DENSITY:
					variableField.setValue(FHDataType.MAX_DENSITY);
					break;
				case LATEWOOD_WIDTH:
					variableField.setValue(FHDataType.LATE_WOOD);
					break;
				case EARLYWOOD_DENSITY:
					variableField.setValue(FHDataType.EARLY_WOOD_DENSITY);
					break;
				case LATEWOOD_DENSITY:
					variableField.setValue(FHDataType.LATE_WOOD_DENSITY);
					break;		
				case LATEWOOD_PERCENT:
				case RING_DENSITY:
				default:
					break;
				}
			}
			else
			{
				addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
				variableField.setValue(FHDataType.RING_WIDTH);
			}
		}
		else
		{
			addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
			variableField.setValue(FHDataType.RING_WIDTH);
		}
		
		// Set units
		if(argValues.isSetUnit())
		{
			if (!argValues.getUnit().isSetNormalTridas()) {
				addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.invalidUnits")));	
			}
			else
			{
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
						addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.invalidUnits")));	
				}
			}
		}
		else
		{
			addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.invalidUnits")));	
		}
			



	}
	
	public void populateFromMS(TridasMeasurementSeries argSeries) {
		populateFromSeries(argSeries);
		
		if(argSeries.isSetAnalyst())
		{
			getStringDefaultValue(DefaultFields.PERS_ID).setValue(argSeries.getAnalyst());
		}
		else if (argSeries.isSetDendrochronologist())
		{
			getStringDefaultValue(DefaultFields.PERS_ID).setValue(argSeries.getDendrochronologist());
		}
	}
	
	public void populateFromDerivedSeries(TridasDerivedSeries argSeries) {
		populateFromSeries(argSeries);
		
		/*if (argSeries.isSetStandardizingMethod()) {
			getStringDefaultValue(DefaultFields.SERIES_TYPE).setValue(argSeries.getStandardizingMethod());
		}*/
		
		if(argSeries.isSetAuthor())
		{
			getStringDefaultValue(DefaultFields.PERS_ID).setValue(argSeries.getAuthor());
		}
	}
	
	private void populateFromSeries(ITridasSeries argSeries) {
		
		TridasIdentifier id = argSeries.getIdentifier();
		
		// KEYCODE
		String keycode = null;
		if(argSeries.isSetGenericFields())
		{
			for(TridasGenericField gf : argSeries.getGenericFields())
			{
				if(gf.getName().toLowerCase().equals("keycode"))
				{
					keycode = gf.getValue();
				}
			}
		}
		if(keycode!=null)
		{
			getStringDefaultValue(DefaultFields.KEYCODE).setValue(keycode);
		}
		else if (id != null) {
			if (id.isSetValue()) {
				getStringDefaultValue(DefaultFields.KEYCODE).setValue(id.getValue());
			}
		}
		else if (argSeries.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.KEYCODE).setValue(argSeries.getTitle());
		}
		
		// Dates begin and end
		TridasInterpretation interp = argSeries.getInterpretation();
		if (interp != null) {
			if (interp.isSetFirstYear()) {
				getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setValue(Integer.parseInt((new SafeIntYear(interp.getFirstYear()).toString())));
			}
			if (interp.isSetLastYear()) {
				getIntegerDefaultValue(DefaultFields.DATE_END).setValue(Integer.parseInt((new SafeIntYear(interp.getLastYear()).toString())));
			}
		}
		
		// First measurement date
		if(argSeries.isSetCreatedTimestamp())
		{
			XMLGregorianCalendar xcal = argSeries.getCreatedTimestamp().getValue();
			Date dt = xcal.toGregorianCalendar().getTime();
			getStringDefaultValue(DefaultFields.FIRST_MEASUREMENT_DATE).setValue(dateFormat.format(dt));
		}
		
		// Last revision date
		if(argSeries.isSetLastModifiedTimestamp())
		{
			XMLGregorianCalendar xcal = argSeries.getLastModifiedTimestamp().getValue();
			Date dt = xcal.toGregorianCalendar().getTime();
			getStringDefaultValue(DefaultFields.LAST_REVISION_DATE).setValue(dateFormat.format(dt));
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
		TridasWoodCompleteness wc = null;
		TridasSapwood sapwood = null;
		TridasBark bark = null;
		
		// Get the wood completeness from the series if possible, if not then try the radius
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
			bark = wc.getBark();
			PresenceAbsence barkPA = bark.getPresence();
			
			if(barkPA.equals(PresenceAbsence.PRESENT))
			{
				getStringDefaultValue(DefaultFields.BARK).setValue(FHBarkType.AVAILABLE.toCode());
			}
			else if (barkPA.equals(PresenceAbsence.ABSENT))
			{
				getStringDefaultValue(DefaultFields.BARK).setValue(FHBarkType.UNAVAILABLE.toCode());
			}
		}
		
		// Sapwood rings
		if(wc.isSetSapwood())
		{
			sapwood = wc.getSapwood();
			if(wc.getSapwood().isSetNrOfSapwoodRings())
			{
				getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).setValue(wc.getSapwood().getNrOfSapwoodRings());
			}
		}

		// Pith
		if(wc.isSetPith())
		{
			ComplexPresenceAbsence pith = wc.getPith().getPresence();
			
			if(pith.equals(ComplexPresenceAbsence.COMPLETE) ||
			   pith.equals(ComplexPresenceAbsence.INCOMPLETE))
			{
				getStringDefaultValue(DefaultFields.PITH).setValue(FHPith.PRESENT.toCode());
			}
			else if (pith.equals(ComplexPresenceAbsence.ABSENT))
			{
				getStringDefaultValue(DefaultFields.PITH).setValue(FHPith.ABSENT.toCode());
			}
		}
		
		// Missing Rings before
		if(wc.isSetNrOfUnmeasuredInnerRings())
		{
			getIntegerDefaultValue(DefaultFields.MISSING_RINGS_BEFORE).setValue(wc.getNrOfUnmeasuredInnerRings());

		}

		// Missing Rings after
		if(wc.isSetNrOfUnmeasuredOuterRings())
		{
			getIntegerDefaultValue(DefaultFields.MISSING_RINGS_AFTER).setValue(wc.getNrOfUnmeasuredOuterRings());

		}
		
		// WaldKante
		if(wc.isSetSapwood())
		{
			if(sapwood.isSetLastRingUnderBark())
			{
				// Try and get info from last ring under 
				TridasLastRingUnderBark lrub = sapwood.getLastRingUnderBark();
				if(lrub.getPresence().equals(PresenceAbsence.PRESENT))
				{
					// Last ring under bark is present so we have waldkante, but the 
					// 'content' for this field is not parsable into the Heidelberg
					// options so all we can do is say waldkante is present but unknown.
					// We add the content as a string after so that people can see we 
					// have more info
					getStringDefaultValue(DefaultFields.WALDKANTE).setValue(FHWaldKante.UNKNOWN.toCode()+" "+lrub.getContent());
				}
				else
				{
					// WaldKante is definitely absent
					getStringDefaultValue(DefaultFields.WALDKANTE).setValue(FHWaldKante.NONE.toCode());
				}
			}
			else if (sapwood.isSetPresence())
			{
				// Last ring under bark field is missing so we use sapwood to
				// determine what to put in here
				if(sapwood.getPresence().equals(ComplexPresenceAbsence.COMPLETE))
				{
					getStringDefaultValue(DefaultFields.WALDKANTE).setValue(FHWaldKante.UNKNOWN.toCode());
				}
				else if ((sapwood.getPresence().equals(ComplexPresenceAbsence.INCOMPLETE)) ||
						 (sapwood.getPresence().equals(ComplexPresenceAbsence.ABSENT)))
		        {
					// WaldKante is definitely absent
					getStringDefaultValue(DefaultFields.WALDKANTE).setValue(FHWaldKante.NONE.toCode());
				}
			}
		}
	
		
		
	}

	
}
