/**
 * Created on Apr 12, 2010, 1:37:53 PM
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
		setDefaultValue(TucsonField.SITE_NAME, new StringDefaultValue(I18n.getText("unnamed.object"), 50, 50));
		setDefaultValue(TucsonField.SPECIES_CODE, new StringDefaultValue("UNKN", 4, 4));
		setDefaultValue(TucsonField.SPECIES_NAME, new StringDefaultValue("Plantae", 8, 8));
		setDefaultValue(TucsonField.INVESTIGATOR, new StringDefaultValue(I18n.getText("unknown"), 61, 61));
		setDefaultValue(TucsonField.ELEVATION, new DoubleDefaultValue(null, -418.0, 8850.0, 10, 10)); // Heights of Dead Sea and Everest! ;-)
		setDefaultValue(TucsonField.LATLONG, new StringDefaultValue("", 11, 11));
		setDefaultValue(TucsonField.STATE_COUNTRY, new StringDefaultValue(I18n.getText("unknown"), 13, 13));
		setDefaultValue(TucsonField.COMP_DATE, new StringDefaultValue(getTodaysDateTucsonStyle(), 8, 8));
	}
	
	private String getTodaysDateTucsonStyle() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return dateFormat.format(calendar.getTime());
	}
	

	protected void populateFromTridasProject(TridasProject p)
	{
		if(getStringDefaultValue(TucsonField.INVESTIGATOR).setValue(p.getInvestigator())==false)
		{
			this.addTruncatedWarning(TucsonField.INVESTIGATOR, 
					I18n.getText("tucson.investigator.truncated", String.valueOf(p.getInvestigator().length())));
		}
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
		
		if(e.getAltitude()!=null)
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
