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
package org.tridas.io.formats.besancon;

import org.apache.commons.lang.WordUtils;
import org.tridas.io.I18n;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.BooleanDefaultValue;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasLastRingUnderBark;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasWoodCompleteness;

public class BesanconToTridasDefaults extends TridasMetadataFieldSet {
	
	public static enum DefaultFields {
		SERIES_TITLE, DATE, RING_COUNT, // LON
		SPECIES, // ESP
		PITH, // MOE
		SAPWOOD_START, // AUB
		SAPWOOD_COUNT,
		CAMBIUM, // CAM
		BARK, // ECO
		FIRST_YEAR, // ORI
		LAST_YEAR, // TER
		POSITION_IN_MEAN, // POS
		DATED, // ORI is not null
		UNMEASURED_RINGS_AT_START,
		UNMEASURED_RINGS_AT_END;
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue());
		setDefaultValue(DefaultFields.PITH, new BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.SAPWOOD_START, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.CAMBIUM, new GenericDefaultValue<BesanconCambiumType>());
		setDefaultValue(DefaultFields.BARK, new BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.FIRST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.LAST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.POSITION_IN_MEAN, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATED, new BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.UNMEASURED_RINGS_AT_START, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.UNMEASURED_RINGS_AT_END, new IntegerDefaultValue());
	}
	
	@Override
	public TridasProject getDefaultTridasProject() {
		TridasProject project = super.getDefaultTridasProject();
		return project;
	}
	
	@Override
	public TridasObject getDefaultTridasObject() {
		TridasObject object = super.getDefaultTridasObject();
		return object;
	}
	
	@Override
	public TridasElement getDefaultTridasElement() {
		TridasElement element = super.getDefaultTridasElement();
		ControlledVoc taxon = ITRDBTaxonConverter.getControlledVocFromCode(getStringDefaultValue(DefaultFields.SPECIES)
				.getStringValue());
		element.setTaxon(taxon);
		return element;
	}
	
	@Override
	public TridasSample getDefaultTridasSample() {
		TridasSample sample = super.getDefaultTridasSample();
		return sample;
	}
	
	@Override
	public TridasRadius getDefaultTridasRadius() {
		TridasRadius radius = super.getDefaultTridasRadius();
		return radius;
	}
	
	@SuppressWarnings("unchecked")
	public TridasMeasurementSeries getDefaultMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		// Last modified date
		if (getDateTimeDefaultValue(DefaultFields.DATE).getValue() != null) {
			series.setLastModifiedTimestamp(getDateTimeDefaultValue(DefaultFields.DATE).getValue());
		}
		
		// Set series title
		series.setTitle(getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue());
		
		TridasDating dating = new TridasDating();
		if(getBooleanDefaultValue(DefaultFields.DATED).getValue())
		{
			
			// Set first and last years
			series.getInterpretation().setFirstYear(
					getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue().toTridasYear(DatingSuffix.AD));
			series.getInterpretation().setLastYear(
					getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).getValue().toTridasYear(DatingSuffix.AD));
			
			// Dating Type
			dating.setType(NormalTridasDatingType.ABSOLUTE);
			series.getInterpretation().setDating(dating);
		}

		
		// Set ring count
		TridasWoodCompleteness wc = new ObjectFactory().createTridasWoodCompleteness();
		wc.setRingCount(getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue());
		
		// Set pith info
		TridasPith pith = new TridasPith();

		if (getBooleanDefaultValue(DefaultFields.PITH).getValue() != null) {
			if(getBooleanDefaultValue(DefaultFields.PITH).getValue())
			{
				pith.setPresence(ComplexPresenceAbsence.COMPLETE);
			}
			else
			{
				pith.setPresence(ComplexPresenceAbsence.ABSENT);
			}
		}
		else
		{
			pith.setPresence(ComplexPresenceAbsence.ABSENT);
		}
		wc.setPith(pith);
		
		// Set bark info
		TridasBark bark = new TridasBark();
		if (getBooleanDefaultValue(DefaultFields.BARK).getValue() != null) 
		{
			if(getBooleanDefaultValue(DefaultFields.BARK).getValue())
			{		
				bark.setPresence(PresenceAbsence.PRESENT);
			}
			else
			{
				bark.setPresence(PresenceAbsence.ABSENT);
			}
		}
		else
		{
			bark.setPresence(PresenceAbsence.ABSENT);
		}
		wc.setBark(bark);
		
		// Create sapwood with default presence
		TridasSapwood sapwood = new TridasSapwood();
		sapwood.setPresence(ComplexPresenceAbsence.UNKNOWN);
		
		// Set last ring info
		GenericDefaultValue<BesanconCambiumType> cambium = (GenericDefaultValue<BesanconCambiumType>) getDefaultValue(DefaultFields.CAMBIUM);
		if (cambium.getValue() != null) {
			TridasLastRingUnderBark lrub = new TridasLastRingUnderBark();
			lrub.setPresence(PresenceAbsence.PRESENT);
			
			// Mark season if possible
			if (cambium.getValue().equals(BesanconCambiumType.SPRING)) {
				lrub.setContent(I18n.getText("seasons.spring"));
			}
			else if (cambium.getValue().equals(BesanconCambiumType.SUMMER)) {
				lrub.setContent(I18n.getText("seasons.summer"));
			}
			else if (cambium.getValue().equals(BesanconCambiumType.WINTER)) {
				lrub.setContent(I18n.getText("seasons.winter"));
			}
			else {
				lrub.setContent(" ");
			}
			
			sapwood.setLastRingUnderBark(lrub);
			
		}
		
		// Set sapwood info
		if (getIntegerDefaultValue(DefaultFields.SAPWOOD_START).getValue() != null) 
		{
			if(getBooleanDefaultValue(DefaultFields.BARK).getValue() != null)
			{		
				if (getBooleanDefaultValue(DefaultFields.BARK).getValue() || cambium.getValue() != null) {
					// Bark or cambium is present as well as sapwood so sapwood must be
					// complete
					sapwood.setPresence(ComplexPresenceAbsence.COMPLETE);
				}
				else
				{
					// No bark or cambium so sapwood incomplete
					sapwood.setPresence(ComplexPresenceAbsence.INCOMPLETE);
				}
			}
			else {
				// No bark or cambium so sapwood incomplete
				sapwood.setPresence(ComplexPresenceAbsence.INCOMPLETE);
			}
			
			// Calculate number of sapwood rings
			if ((getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue() != null) && 
			   (getIntegerDefaultValue(DefaultFields.SAPWOOD_START).getValue() != null))
			   {
					if(getIntegerDefaultValue(DefaultFields.SAPWOOD_START).getValue()!=0)
					{	
						Integer sapwoodCount = getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()
								- getIntegerDefaultValue(DefaultFields.SAPWOOD_START).getValue()+1;
						sapwood.setNrOfSapwoodRings(sapwoodCount);
					}
			   }
		}
		
		// Add sapwood details
		wc.setSapwood(sapwood);

		// Heartwood
		// No heartwood details are given but if pith is present and sapwood is present then we can 
		// assume heartwood is complete.  
		TridasHeartwood heartwood = new TridasHeartwood();
		heartwood.setPresence(ComplexPresenceAbsence.UNKNOWN);
		if(     ((pith.getPresence().equals(ComplexPresenceAbsence.COMPLETE)) || 
		        (pith.getPresence().equals(ComplexPresenceAbsence.INCOMPLETE)))		
			&& 
				((sapwood.getPresence().equals(ComplexPresenceAbsence.COMPLETE)) || 
				(sapwood.getPresence().equals(ComplexPresenceAbsence.INCOMPLETE)))
		   )
		{
			heartwood.setPresence(ComplexPresenceAbsence.COMPLETE);
		}	
		wc.setHeartwood(heartwood);
		
		if(getIntegerDefaultValue(DefaultFields.UNMEASURED_RINGS_AT_START).getValue()!=null)
		{
			wc.setNrOfUnmeasuredInnerRings(getIntegerDefaultValue(DefaultFields.UNMEASURED_RINGS_AT_START).getValue());
		}
		
		if(getIntegerDefaultValue(DefaultFields.UNMEASURED_RINGS_AT_END).getValue()!=null)
		{
			wc.setNrOfUnmeasuredOuterRings(getIntegerDefaultValue(DefaultFields.UNMEASURED_RINGS_AT_END).getValue());
		}
			
		// Add wood completeness to series
		series.setWoodCompleteness(wc);
		
		return series;
	}
	
	public enum BesanconCambiumType {
		CAMBIUM_PRESENT_SEASON_UNKOWN(""), WINTER("HIV"), SUMMER("ETE"), SPRING("PRI");
		
		private String code;
		
		BesanconCambiumType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static BesanconCambiumType fromCode(String code) {
			for (BesanconCambiumType val : BesanconCambiumType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
}
