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
package org.tridas.io.formats.tucson;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

/**
 * Place to hold and change default fields for the Tucson filetype
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TridasToTucsonDefaults extends AbstractMetadataFieldSet {

	public enum TucsonField {
		SITE_CODE, SITE_NAME, SPECIES_CODE, SPECIES_NAME, INVESTIGATOR, ELEVATION, LATLONG, STATE_COUNTRY, COMP_DATE, RANGE;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		setDefaultValue(TucsonField.SITE_CODE, new StringDefaultValue(UUID.randomUUID().toString().substring(0, 6), 6,
				6));
		setDefaultValue(TucsonField.SITE_NAME, new StringDefaultValue(I18n.getText("unnamed.object"), 52, 52));
		setDefaultValue(TucsonField.SPECIES_CODE, new StringDefaultValue("UNKN", 4, 4));
		setDefaultValue(TucsonField.SPECIES_NAME, new StringDefaultValue("Plantae", 18, 18));
		setDefaultValue(TucsonField.INVESTIGATOR, new StringDefaultValue(I18n.getText("unknown"), 63, 63));
		setDefaultValue(TucsonField.ELEVATION, new DoubleDefaultValue(null, -418.0, 8850.0, 7, 7)); // Heights of Dead Sea and Everest! ;-)
		setDefaultValue(TucsonField.LATLONG, new StringDefaultValue("", 10, 10));
		setDefaultValue(TucsonField.STATE_COUNTRY, new StringDefaultValue(I18n.getText("unknown"), 13, 13));
		setDefaultValue(TucsonField.COMP_DATE, new StringDefaultValue(DateUtils.getDateTimeTucsonStyle(null), 8, 8));
	}
	

	protected void populateFromTridasProject(TridasProject p)
	{

	}
	
	protected void populateFromTridasObject(TridasObject o)
	{
		if(o.getIdentifier()!=null)
		{
			getStringDefaultValue(TucsonField.SITE_CODE).setValue(o.getIdentifier().getValue());
		}
		
		if(o.getTitle()!=null)
		{
			getStringDefaultValue(TucsonField.SITE_NAME).setValue(o.getTitle());
		}
		
		if(o.getLocation()!=null)
		{
			if(o.getLocation().getLocationGeometry()!=null)
			{
				getStringDefaultValue(TucsonField.LATLONG).setValue(
						getStringCoordsFromGeometry(o.getLocation().getLocationGeometry()));
			}
		}

	}
	
	protected void populateFromTridasElement(TridasElement e)
	{		
		if(e.isSetTaxon()){
			if(e.getTaxon().getNormalId()!=null)
			{
				getStringDefaultValue(TucsonField.SPECIES_CODE).setValue(e.getTaxon().getNormalId());
			}
			
			if(e.getTaxon().getNormal()!=null)
			{
				getStringDefaultValue(TucsonField.SPECIES_NAME).setValue(e.getTaxon().getNormal());
			}
			else if (e.getTaxon().getValue()!=null)
			{
				getStringDefaultValue(TucsonField.SPECIES_NAME).setValue(e.getTaxon().getValue());
			}
		}
		
		if(e.isSetAltitude())
		{
			getDoubleDefaultValue(TucsonField.ELEVATION).setValue(e.getAltitude());
		}

	}
	
	protected void populateFromTridasSample(TridasSample s)
	{
		
	}
	
	protected void populateFromTridasRadius(TridasRadius r)
	{
		
	}
	
	protected void populateFromTridasMeasurementSeries(TridasMeasurementSeries ms)
	{
		if(ms.isSetAnalyst())
		{
			getStringDefaultValue(TucsonField.INVESTIGATOR).setValue(StringUtils.parseInitials(ms.getAnalyst()));
		}
		else if (ms.isSetDendrochronologist())
		{
			getStringDefaultValue(TucsonField.INVESTIGATOR).setValue(StringUtils.parseInitials(ms.getDendrochronologist()));
		}
		
		if(ms.isSetCreatedTimestamp())
		{
			getStringDefaultValue(TucsonField.COMP_DATE).setValue(DateUtils.getDateTimeTucsonStyle(ms.getCreatedTimestamp()));
		}
		else if (ms.isSetLastModifiedTimestamp())
		{
			getStringDefaultValue(TucsonField.COMP_DATE).setValue(DateUtils.getDateTimeTucsonStyle(ms.getLastModifiedTimestamp()));
		}
		
	}
	
	
	protected void populateFromTridasDerivedSeries(TridasDerivedSeries ds)
	{
		if(ds.isSetAuthor())
		{
			getStringDefaultValue(TucsonField.INVESTIGATOR).setValue(StringUtils.parseInitials(ds.getAuthor()));
		}
		
		if(ds.isSetCreatedTimestamp())
		{
			getStringDefaultValue(TucsonField.COMP_DATE).setValue(DateUtils.getDateTimeTucsonStyle(ds.getCreatedTimestamp()));
		}
		else if (ds.isSetLastModifiedTimestamp())
		{
			getStringDefaultValue(TucsonField.COMP_DATE).setValue(DateUtils.getDateTimeTucsonStyle(ds.getLastModifiedTimestamp()));
		}
		

	}
	
	protected void populateFromTridasValues(TridasValues tvs)
	{
		
	}
	
	/**
	 * Get a Tucson string representation of a TridasLocationGeometry
	 * 
	 * @param geom
	 * @return
	 */
	public static String getStringCoordsFromGeometry(TridasLocationGeometry geom)
	{
		String latstr = null;
		String longstr = null;
		
		List<Double> points = geom.getPoint().getPos().getValues();
		DecimalFormat TucsonDigits = new DecimalFormat("#.#");
		if(points.size()==2)
		{
			if(points.get(0)<0)
			{
				latstr = "S"+TucsonDigits.format(points.get(0)).substring(1);
			}
			else
			{
				latstr = "N"+TucsonDigits.format(points.get(0));
			}
			
			if(points.get(1)<0)
			{
				longstr = "W"+TucsonDigits.format(points.get(1)).substring(1);
			}
			else
			{
				longstr = "E"+TucsonDigits.format(points.get(1));
			}
		}
		
		return latstr+longstr;
	}
}
