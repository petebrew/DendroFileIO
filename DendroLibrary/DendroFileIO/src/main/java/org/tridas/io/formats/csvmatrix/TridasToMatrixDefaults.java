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
package org.tridas.io.formats.csvmatrix;

import java.text.SimpleDateFormat;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.BooleanDefaultValue;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.TridasAddress;
import org.tridas.schema.TridasBedrock;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasDimensions;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasSlope;
import org.tridas.schema.TridasSoil;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;
import org.tridas.spatial.GMLPointSRSHandler;

public class TridasToMatrixDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	public static enum DefaultFields {
		
		PROJECT_TITLE,
		PROJECT_IDENTIFIER,
		PROJECT_CREATED_TIMESTAMP,
		PROJECT_UPDATED_TIMESTAMP,
		PROJECT_COMMENTS,
		PROJECT_TYPE,
		PROJECT_DESCRIPTION,
		//PROJECT_LABORATORY_NAME,
		//PROJECT_LABORATORY_IDENTIFIER,
		//PROJECT_LABORATORY_ADDRESS_LINE1,
		//PROJECT_LABORATORY_ADDRESS_LINE2,
		//PROJECT_LABORATORY_ADDRESS_CITY,
		//PROJECT_LABORATORY_ADDRESS_STATE,
		//PROJECT_LABORATORY_ADDRESS_POSTALCODE,
		//PROJECT_LABORATORY_ADDRESS_COUNTRY,
		PROJECT_CATEGORY,
		PROJECT_INVESTIGATOR,
		PROJECT_PERIOD,
		PROJECT_REQUEST_DATE,
		PROJECT_COMMISSIONER,
		//PROJECT_REFERENCE,
		//PROJECT_RESEARCH
		
		OBJECT_TITLE,
		OBJECT_IDENTIFIER,
		OBJECT_CREATED_TIMESTAMP,
		OBJECT_UPDATED_TIMESTAMP,
		OBJECT_COMMENTS,
		OBJECT_TYPE,
		OBJECT_DESCRIPTION,
		OBJECT_LOCATION_LAT,
		OBJECT_LOCATION_LON,
		OBJECT_LOCATION_TYPE,
		OBJECT_LOCATION_PRECISION,
		OBJECT_LOCATION_COMMENT,
		OBJECT_LOCATION_CITYTOWN,
		OBJECT_LOCATION_STATE,
		OBJECT_LOCATION_COUNTRY,
		
		ELEMENT_TITLE,
		ELEMENT_IDENTIFIER,
		ELEMENT_CREATED_TIMESTAMP,
		ELEMENT_UPDATED_TIMESTAMP,
		ELEMENT_COMMENTS,
		ELEMENT_TYPE,
		ELEMENT_DESCRIPTION,
		ELEMENT_TAXON,
		ELEMENT_SHAPE,
		ELEMENT_DIMENSIONS_UNIT,
		ELEMENT_DIMENSIONS_HEIGHT,
		ELEMENT_DIMENSIONS_WIDTH,
		ELEMENT_DIMENSIONS_DEPTH,
		ELEMENT_DIMENSIONS_DIAMETER,
		ELEMENT_AUTHENTICITY,
		ELEMENT_LOCATION_LAT,
		ELEMENT_LOCATION_LON,
		ELEMENT_LOCAITON_TYPE,
		ELEMENT_LOCATION_PRECISION,
		ELEMENT_LOCATION_COMMENT,
		ELEMENT_LOCATION_CITYTOWN,
		ELEMENT_LOCATION_STATE,
		ELEMENT_LOCATION_COUNTRY,
		ELEMENT_PROCESSING,
		ELEMENT_MARKS,
		ELEMENT_ELEVATION,
		ELEMENT_SLOPE_ANGLE,
		ELEMENT_SLOPE_AZIMUTH,
		ELEMENT_SOIL_DEPTH,
		ELEMENT_SOIL_DESCRIPTION,
		ELEMENT_BEDROCK,
		
		SAMPLE_TITLE,
		SAMPLE_IDENTIFIER,
		SAMPLE_CREATED_TIMESTAMP,
		SAMPLE_UPDATED_TIMESTAMP,
		SAMPLE_COMMENTS,
		SAMPLE_TYPE,
		SAMPLE_DESCRIPTION,
		SAMPLE_SAMPLING_DATE,
		SAMPLE_POSITION,
		SAMPLE_STATE,
		SAMPLE_KNOTS,
		
		RADIUS_TITLE,
		RADIUS_IDENTIFIER,
		RADIUS_CREATED_TIMESTAMP,
		RADIUS_UPDATED_TIMESTAMP,
		RADIUS_COMMENTS,
		RADIUS_WC_RING_COUNT,
		RADIUS_WC_AVERAGE_RING_WIDTH,
		RADIUS_WC_NR_UNMEASURED_INNER_RINGS,
		RADIUS_WC_NR_UNMEASURED_OUTER_RINGS,
		RADIUS_WC_PITH,
		RADIUS_WC_HEARTWOOD_PRESENCE,
		RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH,
		RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH_FOUNDATION,
		RADIUS_WC_SAPWOOD_PRESENCE,
		RADIUS_WC_SAPWOOD_NR_SAPWOOD_RINGS,
		RADIUS_WC_SAPWOOD_LAST_RING_UNDER_BARK_PRESENCE,
		RADIUS_WC_SAPWOOD_LAST_RING_UNDER_BARK_CONTENT,
		RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK,
		RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK_FOUNDATION,
		RADIUS_WC_BARK,
		RADIUS_AZIMUTH,
		
		
		SERIES_TITLE,
		SERIES_IDENTIFIER,
		SERIES_CREATED_TIMESTAMP,
		SERIES_UPDATED_TIMESTAMP,
		SERIES_COMMENTS,
		SERIES_MEASURING_DATE,
		SERIES_ANALYST,
		SERIES_DENDROCHRONOLOGIST,
		SERIES_MEASURING_METHOD,
		
		VARIABLE,
		DATING_TYPE;
		

	}
	
	
	@Override
	protected void initDefaultValues() {

		setDefaultValue(DefaultFields.PROJECT_TITLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_IDENTIFIER, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_CREATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_UPDATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_DESCRIPTION, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_CATEGORY, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_INVESTIGATOR, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_PERIOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_REQUEST_DATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJECT_COMMISSIONER, new StringDefaultValue());

		
		setDefaultValue(DefaultFields.OBJECT_TITLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_IDENTIFIER, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_CREATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_UPDATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_DESCRIPTION, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_LOCATION_LAT, new DoubleDefaultValue(null, -90.0, 90.0));
		setDefaultValue(DefaultFields.OBJECT_LOCATION_LON, new DoubleDefaultValue(null, -180.0, 180.0));
		setDefaultValue(DefaultFields.OBJECT_LOCATION_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_LOCATION_PRECISION, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_LOCATION_COMMENT, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_LOCATION_CITYTOWN, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_LOCATION_STATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.OBJECT_LOCATION_COUNTRY, new StringDefaultValue());

		setDefaultValue(DefaultFields.ELEMENT_TITLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_IDENTIFIER, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_CREATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_UPDATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_DESCRIPTION, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_TAXON, new StringDefaultValue("Plantae"));
		setDefaultValue(DefaultFields.ELEMENT_SHAPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_UNIT, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_HEIGHT, new DoubleDefaultValue(null, 0.0, Double.MAX_VALUE));
		setDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_WIDTH, new DoubleDefaultValue(null, 0.0, Double.MAX_VALUE));
		setDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DEPTH, new DoubleDefaultValue(null, 0.0, Double.MAX_VALUE));
		setDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DIAMETER, new DoubleDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_AUTHENTICITY, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_LOCATION_LAT, new DoubleDefaultValue(null, -90.0, 90.0));
		setDefaultValue(DefaultFields.ELEMENT_LOCATION_LON, new DoubleDefaultValue(null, -180.0, 180.0));
		setDefaultValue(DefaultFields.ELEMENT_LOCAITON_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_LOCATION_PRECISION, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_LOCATION_COMMENT, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_LOCATION_CITYTOWN, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_LOCATION_STATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_LOCATION_COUNTRY, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_PROCESSING, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_MARKS, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_ELEVATION, new DoubleDefaultValue(null, -418.0, 8850.0));
		setDefaultValue(DefaultFields.ELEMENT_SLOPE_ANGLE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_SLOPE_AZIMUTH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_SOIL_DEPTH, new DoubleDefaultValue(null, 0.0, Double.MAX_VALUE));
		setDefaultValue(DefaultFields.ELEMENT_SOIL_DESCRIPTION, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEMENT_BEDROCK, new StringDefaultValue());		
		
		setDefaultValue(DefaultFields.SAMPLE_TITLE, new StringDefaultValue());		
		setDefaultValue(DefaultFields.SAMPLE_IDENTIFIER,  new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_CREATED_TIMESTAMP,  new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_UPDATED_TIMESTAMP,  new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_COMMENTS,  new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_TYPE,  new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_DESCRIPTION,  new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_SAMPLING_DATE,  new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_POSITION,  new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_STATE,  new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLE_KNOTS,  new BooleanDefaultValue());
		
		setDefaultValue(DefaultFields.RADIUS_TITLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_IDENTIFIER, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_CREATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_UPDATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_AVERAGE_RING_WIDTH, new DoubleDefaultValue(null, 0.0, Double.MAX_VALUE));
		setDefaultValue(DefaultFields.RADIUS_WC_NR_UNMEASURED_INNER_RINGS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_NR_UNMEASURED_OUTER_RINGS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_PITH, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_PRESENCE, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH_FOUNDATION, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_PRESENCE, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_NR_SAPWOOD_RINGS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_LAST_RING_UNDER_BARK_PRESENCE, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_LAST_RING_UNDER_BARK_CONTENT, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK_FOUNDATION, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_WC_BARK, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_AZIMUTH, new DoubleDefaultValue(null, 0.0, Double.MAX_VALUE));

		setDefaultValue(DefaultFields.SERIES_IDENTIFIER, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed")));
		setDefaultValue(DefaultFields.SERIES_CREATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.SERIES_UPDATED_TIMESTAMP, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.SERIES_COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_MEASURING_DATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_ANALYST, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_DENDROCHRONOLOGIST, new StringDefaultValue());
		setDefaultValue(DefaultFields.SERIES_MEASURING_METHOD, new StringDefaultValue());
		
		setDefaultValue(DefaultFields.VARIABLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATING_TYPE, new StringDefaultValue());
		
				
	}

	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries argSeries) {
		
		populateFromTridasSeries(argSeries);
		
		if(argSeries.isSetMeasuringDate())
		{
			getStringDefaultValue(DefaultFields.SERIES_MEASURING_DATE).setValue(argSeries.getMeasuringDate().toString());
		}
		
		if(argSeries.isSetAnalyst())
		{
			getStringDefaultValue(DefaultFields.SERIES_ANALYST).setValue(argSeries.getAnalyst());
		}
		
		if(argSeries.isSetDendrochronologist())
		{
			getStringDefaultValue(DefaultFields.SERIES_DENDROCHRONOLOGIST).setValue(argSeries.getDendrochronologist());
		}
		
		if(argSeries.isSetMeasuringMethod())
		{
			getStringDefaultValue(DefaultFields.SERIES_MEASURING_METHOD).setValue(TridasUtils.controlledVocToString(argSeries.getMeasuringMethod()));
		}

	}
	
	public void populateFromTridasDerivedSeries(TridasDerivedSeries argSeries)
	{
		populateFromTridasSeries(argSeries);
	}
	
	public void populateFromTridasSeries(ITridasSeries argSeries)
	{
		if(argSeries.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(argSeries.getTitle());
		}
		
		if(argSeries.isSetCreatedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.SERIES_CREATED_TIMESTAMP).setValue(argSeries.getCreatedTimestamp());
		}
		
		if(argSeries.isSetLastModifiedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.SERIES_UPDATED_TIMESTAMP).setValue(argSeries.getLastModifiedTimestamp());
		}
		
		if(argSeries.isSetComments())
		{
			getStringDefaultValue(DefaultFields.SERIES_COMMENTS).setValue(argSeries.getComments());
		}
		
	}
	
	public void populateFromTridasValues(TridasValues values)
	{
		if(!values.isSetVariable()) return;
		
		if(values.getVariable().isSetNormalTridas())
		{
			getStringDefaultValue(DefaultFields.VARIABLE).setValue(values.getVariable().getNormalTridas().value().toString());
		}
		else if (values.getVariable().isSetNormal())
		{
			getStringDefaultValue(DefaultFields.VARIABLE).setValue(values.getVariable().getNormal().toString());
		}
		else if (values.getVariable().isSetValue())
		{
			getStringDefaultValue(DefaultFields.VARIABLE).setValue(values.getVariable().getValue());
		}
	}

	public void populateFromTridasRadius(TridasRadius radius)
	{
		if(radius.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.RADIUS_TITLE).setValue(radius.getTitle());
		}
		
		if(radius.isSetIdentifier())
		{
			getStringDefaultValue(DefaultFields.RADIUS_IDENTIFIER).setValue(radius.getIdentifier().getValue());
		}
		
		if(radius.isSetCreatedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.RADIUS_CREATED_TIMESTAMP).setValue(radius.getCreatedTimestamp());
		}
		
		if(radius.isSetLastModifiedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.RADIUS_UPDATED_TIMESTAMP).setValue(radius.getLastModifiedTimestamp());
		}
		
		if(radius.isSetComments())
		{
			getStringDefaultValue(DefaultFields.RADIUS_COMMENTS).setValue(radius.getComments());
		}
		
		if(radius.isSetWoodCompleteness())
		{
			TridasWoodCompleteness wc = radius.getWoodCompleteness();
			
			if(wc.isSetRingCount())
			{
				getIntegerDefaultValue(DefaultFields.RADIUS_WC_RING_COUNT).setValue(wc.getRingCount());
			}
			
			if(wc.isSetAverageRingWidth())
			{
				getDoubleDefaultValue(DefaultFields.RADIUS_WC_AVERAGE_RING_WIDTH).setValue(wc.getAverageRingWidth());
			}
			
			if(wc.isSetNrOfUnmeasuredInnerRings())
			{
				getIntegerDefaultValue(DefaultFields.RADIUS_WC_NR_UNMEASURED_INNER_RINGS).setValue(wc.getNrOfUnmeasuredInnerRings());
			}
			
			if(wc.isSetNrOfUnmeasuredOuterRings())
			{
				getIntegerDefaultValue(DefaultFields.RADIUS_WC_NR_UNMEASURED_OUTER_RINGS).setValue(wc.getNrOfUnmeasuredOuterRings());
			}
			
			if(wc.isSetPith() && wc.getPith().isSetPresence())
			{
				getStringDefaultValue(DefaultFields.RADIUS_WC_PITH).setValue(wc.getPith().getPresence().value());
			}
			
			if(wc.isSetHeartwood())
			{
				TridasHeartwood hw = wc.getHeartwood();
				if(hw.isSetPresence())
				{
					getStringDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_PRESENCE).setValue(hw.getPresence().value());
				}
				if(hw.isSetMissingHeartwoodRingsToPith())
				{
					getIntegerDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH).setValue(hw.getMissingHeartwoodRingsToPith());
				}
				if(hw.isSetMissingHeartwoodRingsToPithFoundation())
				{
					getStringDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH_FOUNDATION).setValue(hw.getMissingHeartwoodRingsToPithFoundation());
				}
			}
			if(wc.isSetSapwood())
			{
				TridasSapwood sw = wc.getSapwood();
				if(sw.isSetPresence())
				{
					getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_PRESENCE).setValue(sw.getPresence().value());
				}
				if(sw.isSetMissingSapwoodRingsToBark())
				{
					getIntegerDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK).setValue(sw.getMissingSapwoodRingsToBark());
				}
				if(sw.isSetMissingSapwoodRingsToBarkFoundation())
				{
					getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK_FOUNDATION).setValue(sw.getMissingSapwoodRingsToBarkFoundation());
				}
				if(sw.isSetNrOfSapwoodRings())
				{
					getIntegerDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_NR_SAPWOOD_RINGS).setValue(sw.getNrOfSapwoodRings());
				}
				if(sw.isSetLastRingUnderBark())
				{
					if(sw.getLastRingUnderBark().isSetPresence())
					{
						getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_LAST_RING_UNDER_BARK_PRESENCE).setValue(sw.getLastRingUnderBark().getPresence().value());
					}
					if(sw.getLastRingUnderBark().isSetContent())
					{
						getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_LAST_RING_UNDER_BARK_CONTENT).setValue(sw.getLastRingUnderBark().getContent());
					}
				}
			}
			if(wc.isSetBark())
			{
				if(wc.getBark().isSetPresence())
				{
					getStringDefaultValue(DefaultFields.RADIUS_WC_BARK).setValue(wc.getBark().getPresence().value());
				}
			}
			
		}
		if(radius.isSetAzimuth())
		{
			getDoubleDefaultValue(DefaultFields.RADIUS_AZIMUTH).setValue(radius.getAzimuth().doubleValue());
		}

		
	}
	
	public void populateFromTridasSample(TridasSample sample)
	{
		if(sample.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SAMPLE_TITLE).setValue(sample.getTitle());
		}
		
		if(sample.isSetIdentifier())
		{
			getStringDefaultValue(DefaultFields.SAMPLE_IDENTIFIER).setValue(sample.getIdentifier().getValue());
		}
		
		if(sample.isSetCreatedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.SAMPLE_CREATED_TIMESTAMP).setValue(sample.getCreatedTimestamp());
		}
		
		if(sample.isSetLastModifiedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.SAMPLE_UPDATED_TIMESTAMP).setValue(sample.getLastModifiedTimestamp());
		}
		
		if(sample.isSetComments())
		{
			getStringDefaultValue(DefaultFields.SAMPLE_COMMENTS).setValue(sample.getComments());
		}

		if(sample.isSetType())
		{
			getStringDefaultValue(DefaultFields.SAMPLE_TYPE).setValue(TridasUtils.controlledVocToString(sample.getType()));
		}
	
		if(sample.isSetDescription())
		{
			getStringDefaultValue(DefaultFields.SAMPLE_DESCRIPTION).setValue(sample.getDescription());
		}
		
		if(sample.isSetSamplingDate())
		{			
			getStringDefaultValue(DefaultFields.SAMPLE_SAMPLING_DATE).setValue(DateUtils.getFormattedDate(sample.getSamplingDate(), new SimpleDateFormat("dd/MM/yyyy")));
		}
		
		if(sample.isSetPosition())
		{
			getStringDefaultValue(DefaultFields.SAMPLE_POSITION).setValue(sample.getPosition());
		}
		
		if(sample.isSetState())
		{
			getStringDefaultValue(DefaultFields.SAMPLE_STATE).setValue(sample.getState());
		}
		
		if(sample.isSetKnots())
		{
			getBooleanDefaultValue(DefaultFields.SAMPLE_KNOTS).setValue(sample.isKnots());
		}		
	}

	
	
	public void populateFromTridasElement(TridasElement element)
	{
		if(element.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_TITLE).setValue(element.getTitle());
		}
		
		if(element.isSetIdentifier())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_IDENTIFIER).setValue(element.getIdentifier().getValue());
		}
		
		if(element.isSetCreatedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.ELEMENT_CREATED_TIMESTAMP).setValue(element.getCreatedTimestamp());
		}
		
		if(element.isSetLastModifiedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.ELEMENT_UPDATED_TIMESTAMP).setValue(element.getLastModifiedTimestamp());
		}
		
		if(element.isSetComments())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_COMMENTS).setValue(element.getComments());
		}

		if(element.isSetType())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_TYPE).setValue(TridasUtils.controlledVocToString(element.getType()));
		}
	
		if(element.isSetDescription())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_DESCRIPTION).setValue(element.getDescription());
		}
		
		if(element.isSetTaxon())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_TAXON).setValue(TridasUtils.controlledVocToString(element.getTaxon()));
		}
		
		if(element.isSetShape())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_SHAPE).setValue(TridasUtils.controlledVocToString(element.getShape()));
		}

		if(element.isSetDimensions())
		{
			TridasDimensions dimensions = element.getDimensions();
			if(dimensions.isSetUnit())
			{
				getStringDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_UNIT).setValue(TridasUtils.controlledVocToString(dimensions.getUnit()));
			}
			if(dimensions.isSetHeight())
			{
				getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_HEIGHT).setValue(dimensions.getHeight().doubleValue());
			}
			if(dimensions.isSetWidth())
			{
				getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_WIDTH).setValue(dimensions.getWidth().doubleValue());
			}
			if(dimensions.isSetDepth())
			{
				getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DEPTH).setValue(dimensions.getDepth().doubleValue());
			}
			if(dimensions.isSetDiameter())
			{
				getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DIAMETER).setValue(dimensions.getDiameter().doubleValue());
			}
		}
		
		if(element.isSetAuthenticity())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_AUTHENTICITY).setValue(element.getAuthenticity());
		}

		
		// Set coordinates using the projection handler to make sure we're reading correctly
		if(element.isSetLocation())
		{
			TridasLocation location = element.getLocation();
			if(location.isSetLocationGeometry())
			{
				if(location.getLocationGeometry().isSetPoint())
				{
					GMLPointSRSHandler tph = new GMLPointSRSHandler(location.getLocationGeometry().getPoint());
					getDoubleDefaultValue(DefaultFields.ELEMENT_LOCATION_LAT).setValue(tph.getWGS84LatCoord());
					getDoubleDefaultValue(DefaultFields.ELEMENT_LOCATION_LON).setValue(tph.getWGS84LongCoord());
				}
			}
			
			if(location.isSetLocationType())
			{
				getStringDefaultValue(DefaultFields.ELEMENT_LOCAITON_TYPE).setValue(location.getLocationType().toString());
			}
			
			if(location.isSetLocationPrecision())
			{
				getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_PRECISION).setValue(location.getLocationPrecision());
			}
			
			if(location.isSetLocationComment())
			{
				getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_COMMENT).setValue(location.getLocationComment());
			}
			
			if(location.isSetAddress())
			{
				TridasAddress address = location.getAddress();
				if(address.isSetCityOrTown())
				{
					getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_CITYTOWN).setValue(address.getCityOrTown());
				}
				
				if(address.isSetStateProvinceRegion())
				{
					getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_STATE).setValue(address.getStateProvinceRegion());
				}
				
				if(address.isSetCountry())
				{
					getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_COUNTRY).setValue(address.getCountry());
				}
			}
		}

		if(element.isSetProcessing())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_PROCESSING).setValue(element.getProcessing());
		}
		
		if(element.isSetMarks())
		{
			getStringDefaultValue(DefaultFields.ELEMENT_MARKS).setValue(element.getMarks());
		}
		
		if(element.isSetAltitude())
		{
			getDoubleDefaultValue(DefaultFields.ELEMENT_ELEVATION).setValue(element.getAltitude());
		}
		
		if(element.isSetSlope())
		{
			TridasSlope slope = element.getSlope();
			if(slope.isSetAngle())
			{
				getIntegerDefaultValue(DefaultFields.ELEMENT_SLOPE_ANGLE).setValue(slope.getAngle());
			}
			
			if(slope.isSetAzimuth())
			{
				getIntegerDefaultValue(DefaultFields.ELEMENT_SLOPE_AZIMUTH).setValue(slope.getAzimuth());

			}
		}
		
		if(element.isSetSoil())
		{
			TridasSoil soil = element.getSoil();
			if(soil.isSetDepth())
			{
				getDoubleDefaultValue(DefaultFields.ELEMENT_SOIL_DEPTH).setValue(soil.getDepth());
			}
			
			if(soil.isSetDescription())
			{
				getStringDefaultValue(DefaultFields.ELEMENT_SOIL_DESCRIPTION).setValue(soil.getDescription());
			}
		}

		if(element.isSetBedrock())
		{
			TridasBedrock bedrock = element.getBedrock();
			if(bedrock.isSetDescription())
			{
				getStringDefaultValue(DefaultFields.ELEMENT_BEDROCK).setValue(bedrock.getDescription());
			}
		}
	}
	
	public void populateFromTridasObject(TridasObject object)
	{
		if(object.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.OBJECT_TITLE).setValue(object.getTitle());
		}
		
		if(object.isSetIdentifier())
		{
			getStringDefaultValue(DefaultFields.OBJECT_IDENTIFIER).setValue(object.getIdentifier().getValue());
		}
		
		if(object.isSetCreatedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.OBJECT_CREATED_TIMESTAMP).setValue(object.getCreatedTimestamp());
		}
		
		if(object.isSetLastModifiedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.OBJECT_UPDATED_TIMESTAMP).setValue(object.getLastModifiedTimestamp());
		}
		
		if(object.isSetComments())
		{
			getStringDefaultValue(DefaultFields.OBJECT_COMMENTS).setValue(object.getComments());
		}
		
		if(object.isSetType())
		{
			getStringDefaultValue(DefaultFields.OBJECT_TYPE).setValue(TridasUtils.controlledVocToString(object.getType()));
		}
		
		if(object.isSetDescription())
		{
			getStringDefaultValue(DefaultFields.OBJECT_DESCRIPTION).setValue(object.getDescription());
		}
		
		// Set coordinates using the projection handler to make sure we're reading correctly
		if(object.isSetLocation())
		{
			TridasLocation location = object.getLocation();
			if(location.isSetLocationGeometry())
			{
				if(location.getLocationGeometry().isSetPoint())
				{
					GMLPointSRSHandler tph = new GMLPointSRSHandler(location.getLocationGeometry().getPoint());
					getDoubleDefaultValue(DefaultFields.OBJECT_LOCATION_LAT).setValue(tph.getWGS84LatCoord());
					getDoubleDefaultValue(DefaultFields.OBJECT_LOCATION_LON).setValue(tph.getWGS84LongCoord());
				}
			}
			
			if(location.isSetLocationType())
			{
				getStringDefaultValue(DefaultFields.OBJECT_LOCATION_TYPE).setValue(location.getLocationType().toString());
			}
			
			if(location.isSetLocationPrecision())
			{
				getStringDefaultValue(DefaultFields.OBJECT_LOCATION_PRECISION).setValue(location.getLocationPrecision());
			}
			
			if(location.isSetLocationComment())
			{
				getStringDefaultValue(DefaultFields.OBJECT_LOCATION_COMMENT).setValue(location.getLocationComment());
			}
			
			if(location.isSetAddress())
			{
				TridasAddress address = location.getAddress();
				if(address.isSetCityOrTown())
				{
					getStringDefaultValue(DefaultFields.OBJECT_LOCATION_CITYTOWN).setValue(address.getCityOrTown());
				}
				
				if(address.isSetStateProvinceRegion())
				{
					getStringDefaultValue(DefaultFields.OBJECT_LOCATION_STATE).setValue(address.getStateProvinceRegion());
				}
				
				if(address.isSetCountry())
				{
					getStringDefaultValue(DefaultFields.OBJECT_LOCATION_COUNTRY).setValue(address.getCountry());
				}
			}
		}
	}
	
	public void populateFromTridasProject(TridasProject project)
	{
		if(project.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.PROJECT_TITLE).setValue(project.getTitle());
		}
		
		if(project.isSetIdentifier())
		{
			getStringDefaultValue(DefaultFields.PROJECT_IDENTIFIER).setValue(project.getIdentifier().getValue());
		}
		
		if(project.isSetCreatedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.PROJECT_CREATED_TIMESTAMP).setValue(project.getCreatedTimestamp());
		}
		
		if(project.isSetLastModifiedTimestamp())
		{
			getDateTimeDefaultValue(DefaultFields.PROJECT_UPDATED_TIMESTAMP).setValue(project.getLastModifiedTimestamp());
		}
		
		if(project.isSetComments())
		{
			getStringDefaultValue(DefaultFields.PROJECT_COMMENTS).setValue(project.getComments());
		}

		String types = "";
		for(ControlledVoc type : project.getTypes())
		{
			types += TridasUtils.controlledVocToString(type)+"; ";
		}
		getStringDefaultValue(DefaultFields.PROJECT_TYPE).setValue(types);
		
		if(project.isSetDescription())
		{
			getStringDefaultValue(DefaultFields.PROJECT_DESCRIPTION).setValue(project.getDescription());
		}
		
		if(project.isSetCategory())
		{
			getStringDefaultValue(DefaultFields.PROJECT_CATEGORY).setValue(TridasUtils.controlledVocToString(project.getCategory()));
		}
		
		if(project.isSetInvestigator())
		{
			getStringDefaultValue(DefaultFields.PROJECT_INVESTIGATOR).setValue(project.getInvestigator());
		}
		
		if(project.isSetPeriod())
		{
			getStringDefaultValue(DefaultFields.PROJECT_PERIOD).setValue(project.getPeriod());
		}
		
		if(project.isSetRequestDate())
		{
			getStringDefaultValue(DefaultFields.PROJECT_REQUEST_DATE).setValue(DateUtils.getFormattedDate(project.getRequestDate(), new SimpleDateFormat("dd/MM/yyyy")));
		}
		
		if(project.isSetCommissioner())
		{
			getStringDefaultValue(DefaultFields.PROJECT_COMMISSIONER).setValue(project.getCommissioner());
		}
		
		
		
	}
	
}
