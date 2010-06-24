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
package org.tridas.io.formats.corina;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.corina.CorinaToTridasDefaults.DefaultFields;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldDataType;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldShapeCode;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToCorinaDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	@Override
	protected void initDefaultValues() {

		setDefaultValue(DefaultFields.ID, new StringDefaultValue(null, 6, 6));
		setDefaultValue(DefaultFields.NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATING, new StringDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_PRE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_POST, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.COMMENTS2, new StringDefaultValue());
		setDefaultValue(DefaultFields.TYPE, new GenericDefaultValue<CorinaSampleType>());
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.PITH, new StringDefaultValue());
		setDefaultValue(DefaultFields.TERMINAL, new GenericDefaultValue<CorinaTerminalRing>());
		setDefaultValue(DefaultFields.CONTINUOUS, new StringDefaultValue());
		setDefaultValue(DefaultFields.QUALITY, new StringDefaultValue());
		setDefaultValue(DefaultFields.FORMAT, new StringDefaultValue());
		setDefaultValue(DefaultFields.INDEX_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILENAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.RECONCILED, new StringDefaultValue());
		setDefaultValue(DefaultFields.START_YEAR, new SafeIntYearDefaultValue(new SafeIntYear(1001)));
		setDefaultValue(DefaultFields.USERNAME, new StringDefaultValue(I18n.getText("unknown")));
	}

	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries argSeries) {
		
		populateFromTridasSeries(argSeries);
			
		if(argSeries.isSetDendrochronologist())
		{
			getStringDefaultValue(DefaultFields.USERNAME).setValue(argSeries.getDendrochronologist());
		}
		
	}
	
	public void populateFromTridasDerivedSeries(TridasDerivedSeries argSeries) {
		
		populateFromTridasSeries(argSeries);
		
		if(argSeries.isSetAuthor())
		{
			getStringDefaultValue(DefaultFields.USERNAME).setValue(argSeries.getAuthor());
		}
		
	}
	
	private void populateFromTridasSeries(ITridasSeries argSeries)
	{
		// Name
		if(argSeries.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.NAME).setValue(argSeries.getTitle());
		}
		
		// ID 
		if(argSeries.isSetIdentifier())
		{
			if (argSeries.getIdentifier().isSetValue())
			{
				getStringDefaultValue(DefaultFields.ID).setValue(argSeries.getIdentifier().getValue());
			}
		}
		
		// Comments
		if(argSeries.isSetComments())
		{
			getStringDefaultValue(DefaultFields.ID).setValue(argSeries.getComments());
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public void populateFromWoodCompleteness(TridasMeasurementSeries ms, TridasRadius r)
	{
		TridasWoodCompleteness wc = null;
		TridasSapwood sapwood = null;
		TridasBark bark = null;
		
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
		
		if(wc.isSetSapwood())
		{
			sapwood = wc.getSapwood();
			// wc.sapwood.nrofsapwoodrings = sapwood count
			if(sapwood.isSetNrOfSapwoodRings())
			{
				getIntegerDefaultValue(DefaultFields.SAPWOOD).setValue(sapwood.getNrOfSapwoodRings());
			}
		}
		
		// Unmeas_pre
		if (wc.isSetNrOfUnmeasuredInnerRings())
		{
			getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).setValue(wc.getNrOfUnmeasuredInnerRings());
		}
		
		// Unmeas_post
		if (wc.isSetNrOfUnmeasuredOuterRings())
		{
			getIntegerDefaultValue(DefaultFields.UNMEAS_POST).setValue(wc.getNrOfUnmeasuredOuterRings());
		}

		// Terminal
		GenericDefaultValue<CorinaTerminalRing> terminalField = (GenericDefaultValue<CorinaTerminalRing>)getDefaultValue(DefaultFields.TERMINAL);
		if (wc.isSetBark())
		{
			bark = wc.getBark();
			if (bark.getPresence().equals(PresenceAbsence.PRESENT))
			{
				terminalField.setValue(CorinaTerminalRing.BARK);
			}
			else if (bark.getPresence().equals(PresenceAbsence.ABSENT))
			{
				if(sapwood.isSetPresence())
				{
					if(sapwood.getPresence().equals(ComplexPresenceAbsence.COMPLETE))
					{
						terminalField.setValue(CorinaTerminalRing.WANEY_EDGE);
					}
				}
			}
			
		}
		
	}
	
	public void populateFromTridasElement(TridasElement e)
	{
		// Species
		if(e.isSetTaxon())
		{
			if(e.getTaxon().isSetNormal())
			{
				getStringDefaultValue(DefaultFields.SPECIES).setValue(e.getTaxon().getNormal());
			}
			else if (e.getTaxon().isSetValue())
			{
				getStringDefaultValue(DefaultFields.SPECIES).setValue(e.getTaxon().getValue());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasSample(TridasSample s)
	{
		if(s.isSetType())
		{
			GenericDefaultValue<CorinaSampleType> typeField = (GenericDefaultValue<CorinaSampleType>)getDefaultValue(DefaultFields.TYPE);

			if(s.getType().isSetNormalStd())
			{
				if (s.getType().getNormalStd().equals("Corina"))
				{
					if(s.getType().getNormal().equals("Section"))
					{
						typeField.setValue(CorinaSampleType.SECTION);
					}
					else if(s.getType().getNormal().equals("Core"))
					{
						typeField.setValue(CorinaSampleType.CORE);
					}
					else if(s.getType().getNormal().equals("Charcoal"))
					{
						typeField.setValue(CorinaSampleType.CHARCOAL);
					}
				}
			}
		}
	}
	
	public void populateFromTridasValues(TridasValues tvs)
	{
		
	}
	
	public enum CorinaDatingType {
		ABSOLUTE("A"), RELATIVE("R");
		
		private String code;
		
		CorinaDatingType(String c) {
			code = c;
		}
		
		public final String toCode() {
			return code;
		}
		
		@Override
		public final String toString() {
			return name();
		}
		
		public static CorinaDatingType fromCode(String code) {
			for (CorinaDatingType val : CorinaDatingType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CorinaTerminalRing {
		BARK("B"), WANEY_EDGE("W"), NEAR_END("v"), UNKNOWN("vv");
		
		private String code;
		
		CorinaTerminalRing(String c) {
			code = c;
		}
		
		public final String toCode() {
			return code;
		}
		
		@Override
		public final String toString() {
			return name();
		}
		
		public static CorinaTerminalRing fromCode(String code) {
			for (CorinaTerminalRing val : CorinaTerminalRing.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum CorinaSampleType {
		CORE("C"), SECTION("S"), CHARCOAL("H");
		
		private String code;
		
		CorinaSampleType(String c) {
			code = c;
		}
		
		public final String toCode() {
			return code;
		}
		
		@Override
		public final String toString() {
			return name();
		}
		
		public static CorinaSampleType fromCode(String code) {
			for (CorinaSampleType val : CorinaSampleType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
}
