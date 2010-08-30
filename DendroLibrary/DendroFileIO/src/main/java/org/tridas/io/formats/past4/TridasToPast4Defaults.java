package org.tridas.io.formats.past4;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.Past4BooleanDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToPast4Defaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	enum DefaultFields{
		PROJ_ACTIVE_GROUP,
		PROJ_EDIT_DATE,
		PROJ_CREATION_DATE,
		PROJ_GROUPS,
		PROJ_LOCKED,
		PROJ_NAME,
		PROJ_PASSWORD,
		PROJ_PERSID,
		PROJ_RECORDS,
		PROJ_REFERENCE,
		PROJ_SAMPLE,
		PROJ_VERSION,
		PROJ_DESCRIPTION,
		GRP_NAME,
		GRP_VISIBLE,
		GRP_FIXED,
		GRP_LOCKED,
		GRP_CHANGED,
		GRP_EXPANDED,
		GRP_USE_COLOR,
		GRP_HAS_MEAN_VALUE,
		GRP_IS_CHRONO,
		GRP_CHECKED,
		GRP_SELECTED,
		GRP_COLOR,
		GRP_QUALITY,
		GRP_MV_KEYCODE,
		GRP_OWNER,
		GRP_DESCRIPTION,
		KEYCODE,
		LENGTH,
		OWNER,
		CHRONO,
		LOCKED,
		FILTER,
		FILTER_INDEX,
		FILTER_S1,
		FILTER_S2,
		FILTER_B1,
		FILTER_WEIGHT,
		OFFSET,
		COLOR,
		CHECKED,
		VSHIFT,
		IS_MEAN_VALUE,
		PITH,
		SAPWOOD,
		LOCATION,
		WALDKANTE,
		FIRST_VALID_RING,
		LAST_VALID_RING,
		USE_VALID_RINGS_ONLY,
		QUALITY;
	}
	
	
	@Override
	protected void initDefaultValues() {
		setDefaultValue(DefaultFields.PROJ_ACTIVE_GROUP, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.PROJ_CREATION_DATE, new StringDefaultValue(DateUtils.getDateTimePast4Style(null)));
		setDefaultValue(DefaultFields.PROJ_EDIT_DATE, new StringDefaultValue(DateUtils.getDateTimePast4Style(null)));
		setDefaultValue(DefaultFields.PROJ_GROUPS, new IntegerDefaultValue(1));
		setDefaultValue(DefaultFields.PROJ_LOCKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.PROJ_NAME, new StringDefaultValue(I18n.getText("unnamed.project")));
		setDefaultValue(DefaultFields.PROJ_PASSWORD, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJ_PERSID, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJ_RECORDS, new IntegerDefaultValue(1));
		setDefaultValue(DefaultFields.PROJ_REFERENCE, new IntegerDefaultValue(-1));
		setDefaultValue(DefaultFields.PROJ_SAMPLE, new IntegerDefaultValue(-1));
		setDefaultValue(DefaultFields.PROJ_VERSION, new IntegerDefaultValue(400));
		setDefaultValue(DefaultFields.PROJ_DESCRIPTION, new StringDefaultValue());

		setDefaultValue(DefaultFields.GRP_NAME, new StringDefaultValue(I18n.getText("past4.unnamedGroup")));
		setDefaultValue(DefaultFields.GRP_VISIBLE, new Past4BooleanDefaultValue(true));
		setDefaultValue(DefaultFields.GRP_FIXED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_LOCKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_CHANGED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_EXPANDED, new Past4BooleanDefaultValue(true));
		setDefaultValue(DefaultFields.GRP_USE_COLOR, new Past4BooleanDefaultValue(true));
		setDefaultValue(DefaultFields.GRP_HAS_MEAN_VALUE, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_IS_CHRONO, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_CHECKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_SELECTED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_COLOR, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.GRP_QUALITY, new IntegerDefaultValue(null));
		setDefaultValue(DefaultFields.GRP_MV_KEYCODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.GRP_OWNER, new IntegerDefaultValue(-1));
		setDefaultValue(DefaultFields.GRP_DESCRIPTION, new StringDefaultValue());

		setDefaultValue(DefaultFields.KEYCODE, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.LENGTH, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.OWNER, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.CHRONO, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.LOCKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.FILTER, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.FILTER_INDEX, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.FILTER_S1, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.FILTER_S2, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.FILTER_B1, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.FILTER_WEIGHT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.OFFSET, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.COLOR, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.CHECKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.VSHIFT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.IS_MEAN_VALUE, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.PITH, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.SAPWOOD, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.LOCATION, new StringDefaultValue());
		setDefaultValue(DefaultFields.WALDKANTE, new StringDefaultValue());
		setDefaultValue(DefaultFields.FIRST_VALID_RING, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.LAST_VALID_RING, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.USE_VALID_RINGS_ONLY, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.QUALITY, new IntegerDefaultValue());
	}

	
	
	public void populateFromTridasProject(TridasProject p) {
	
		// Project name
		if(p.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.PROJ_NAME).setValue(p.getTitle());
		}

		// Project investigator
		if(p.isSetInvestigator())
		{
			getStringDefaultValue(DefaultFields.PROJ_PERSID).setValue(p.getInvestigator());
		}
		
		// Project description
		if(p.isSetDescription())
		{
			getStringDefaultValue(DefaultFields.PROJ_DESCRIPTION).setValue(p.getDescription());

		}
		
		// Created date
		if(p.isSetCreatedTimestamp())
		{
			getStringDefaultValue(DefaultFields.PROJ_CREATION_DATE).setValue(
					DateUtils.getDateTimePast4Style(p.getCreatedTimestamp()));
		}
		
		// Edited date
		if(p.isSetLastModifiedTimestamp())
		{
			getStringDefaultValue(DefaultFields.PROJ_EDIT_DATE).setValue(
					DateUtils.getDateTimePast4Style(p.getLastModifiedTimestamp()));
		}
		
	}

	public void populateFromTridasObject(TridasObject o)
	{
		// Group name
		if(o.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.GRP_NAME).setValue(o.getTitle());
		}
	}
	
	public void populateFromTridasElement(TridasElement e) {
		
	}
	
	public void populateFromTridasSample(TridasSample s) {

	}
	
	public void populateFromTridasRadius(TridasRadius r){
		
	}
	
	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries series) {
	
		populateFromTridasSeries(series);

	}
	
	public void populateFromTridasDerivedSeries(TridasDerivedSeries series) {
	
		populateFromTridasSeries(series);
		
		// Group name
		getStringDefaultValue(DefaultFields.GRP_NAME).setValue(I18n.getText("past4.derivedSeriesGroup"));
	}
	
	private void populateFromTridasSeries(ITridasSeries series)
	{
	
		// Keycode
		if(series.isSetIdentifier())
		{
			if (series.getIdentifier().isSetValue())
			{
				getStringDefaultValue(DefaultFields.KEYCODE).setValue(series.getIdentifier().getValue());
			}
			else if (series.isSetTitle())
			{
				getStringDefaultValue(DefaultFields.KEYCODE).setValue(series.getTitle());
			}
		}
		else if (series.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.KEYCODE).setValue(series.getTitle());
		}
		
		if(series.isSetInterpretation())
		{
			if(series.getInterpretation().isSetFirstYear())
			{
				getIntegerDefaultValue(DefaultFields.OFFSET).setValue(series.getInterpretation().getFirstYear().getValue().intValue());
			}

		}
		
	}
	
	public void populateFromWoodCompleteness(TridasMeasurementSeries series, TridasRadius radius)
	{
		TridasWoodCompleteness wc = null;
		TridasSapwood sapwood = null;
		TridasPith pith = null;
		String waldkante = "";
		
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
		
		// Pith
		if(wc.isSetPith())
		{
			pith = wc.getPith();
			
			switch(pith.getPresence())
			{
			case COMPLETE:
			case INCOMPLETE:
				getPast4BooleanDefaultValue(DefaultFields.PITH).setValue(true);
				break;
			default:		
			}
		}
		
		// Sapwood
		if(wc.isSetSapwood())
		{
			sapwood = wc.getSapwood();
			if(sapwood.isSetNrOfSapwoodRings())
			{	
				getIntegerDefaultValue(DefaultFields.SAPWOOD).setValue(sapwood.getNrOfSapwoodRings());
			}
			
			if(sapwood.isSetPresence())
			{
				waldkante += "Sapwood: "+sapwood.getPresence().toString().toLowerCase() +". "; 
			}
			
			if(sapwood.isSetLastRingUnderBark())
			{
				waldkante += "Last ring: "+sapwood.getLastRingUnderBark().getPresence().toString().toLowerCase() +", ";
				waldkante += sapwood.getLastRingUnderBark().getContent() +". ";
			}
				
		}
		
		// Bark
		if(wc.isSetBark())
		{
			if (wc.getBark().isSetPresence())
			{
				waldkante += "Bark: "+wc.getBark().getPresence().toString().toLowerCase() +". "; 
			}
		}
		
		// Waldkante
		getStringDefaultValue(DefaultFields.WALDKANTE).setValue(waldkante);
	
	}
	
	public void populateFromTridasValues(TridasValues argValues) {
		
		if(!argValues.isSetValues()) return;

		// Length
		getIntegerDefaultValue(DefaultFields.LENGTH).setValue(argValues.getValues().size());

		
	}
	
	public void populateFromTridasLocation(TridasObject o, TridasElement e)
	{
		
	}
	
}
