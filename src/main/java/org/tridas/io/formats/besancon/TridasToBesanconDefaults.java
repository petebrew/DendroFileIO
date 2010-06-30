package org.tridas.io.formats.besancon;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.BooleanDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.besancon.BesanconToTridasDefaults.BesanconCambiumType;
import org.tridas.io.formats.besancon.BesanconToTridasDefaults.DefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToBesanconDefaults extends AbstractMetadataFieldSet
		implements IMetadataFieldSet {

	@Override
	protected void initDefaultValues() {
		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.DATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue("Plantae"));
		setDefaultValue(DefaultFields.PITH, new BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.SAPWOOD_START, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.CAMBIUM, new GenericDefaultValue<BesanconCambiumType>());
		setDefaultValue(DefaultFields.BARK, new BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.FIRST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.LAST_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DefaultFields.POSITION_IN_MEAN, new IntegerDefaultValue(1));

	}

	
	public void populateFromTridasProject(TridasProject p) {


	}
	
	public void populateFromTridasObject(TridasObject o) {


	}
	
	public void populateFromTridasElement(TridasElement e) {

		// Species
		if(e.isSetTaxon())
		{
			if(e.getTaxon().isSetNormal())
			{
				getStringDefaultValue(DefaultFields.SPECIES).setValue(e.getTaxon().getNormal());
			}
			else if(e.getTaxon().isSetValue())
			{
				getStringDefaultValue(DefaultFields.SPECIES).setValue(e.getTaxon().getValue());
			}
		}
	
	}
	

	public void populateFromTridasSample(TridasSample s) {


	}
	
	public void populateFromTridasRadius(TridasRadius r) {


	}
	
	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries ms) {

		populateFromTridasSeries(ms);

	}
	
	public void populateFromTridasDerivedSeries(TridasDerivedSeries ds) {
		
		populateFromTridasSeries(ds);
		
	}
	
	private void populateFromTridasSeries(ITridasSeries ser)
	{
		
		// Series Title
		if(ser.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(ser.getTitle());

		}
		
		// Date
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		if(ser.isSetLastModifiedTimestamp())
		{
			Date dt = ser.getLastModifiedTimestamp().getValue().toGregorianCalendar().getTime();
			getStringDefaultValue(DefaultFields.DATE).setValue(sdf.format(dt.getTime()));
		}
		else if (ser.isSetCreatedTimestamp())
		{
			Date dt = ser.getCreatedTimestamp().getValue().toGregorianCalendar().getTime();
			getStringDefaultValue(DefaultFields.DATE).setValue(sdf.format(dt.getTime()));			
		}
		
		// First and Last years
		if(ser.isSetInterpretation())
		{
			if(ser.getInterpretation().isSetFirstYear())
			{
				getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).setValue(new SafeIntYear(ser.getInterpretation().getFirstYear()));
			}
			if(ser.getInterpretation().isSetLastYear())
			{
				getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).setValue(new SafeIntYear(ser.getInterpretation().getLastYear()));
			}
		}
	}
	
	
	public void populateFromTridasValues(TridasValues argValues) {
		
		// Ring count
		getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(argValues.getValues().size());

	}
	
	
	@SuppressWarnings("unchecked")
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
		
		// Pith
		if(wc.isSetPith())
		{
			switch(wc.getPith().getPresence())
			{
			case COMPLETE:
			case INCOMPLETE:
				getBooleanDefaultValue(DefaultFields.PITH).setValue(true);
				break;
			default:
				break;
			}
		}
		
		// Cambium	and Sapwood count
		GenericDefaultValue<BesanconCambiumType> cambiumField = (GenericDefaultValue<BesanconCambiumType>)getDefaultValue(DefaultFields.CAMBIUM);	
		if(wc.isSetSapwood())
		{
			// Cambium
			if(wc.getSapwood().isSetLastRingUnderBark())
			{
				if(wc.getSapwood().getLastRingUnderBark().getPresence().equals(PresenceAbsence.PRESENT))
				{
					cambiumField.setValue(BesanconCambiumType.CAMBIUM_PRESENT_SEASON_UNKOWN);
				}
			}
			else if(wc.getSapwood().isSetPresence())
			{
				if (wc.getSapwood().getPresence().equals(ComplexPresenceAbsence.COMPLETE))
				{
					cambiumField.setValue(BesanconCambiumType.CAMBIUM_PRESENT_SEASON_UNKOWN);
				}
			}
			
			// Sapwood count
			if(wc.getSapwood().isSetNrOfSapwoodRings())
			{
				getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).setValue(wc.getSapwood().getNrOfSapwoodRings());
			}
		}

		// Bark
		if(wc.isSetBark())
		{
			if(wc.getBark().isSetPresence())
			{
				if(wc.getBark().getPresence().equals(PresenceAbsence.PRESENT))
				{
					getBooleanDefaultValue(DefaultFields.BARK).setValue(true);
				}
			}
		}		
	}
}
