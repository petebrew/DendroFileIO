package org.tridas.io.formats.past4;

import org.tridas.io.I18n;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.Past4BooleanDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.past4.TridasToPast4Defaults.DefaultFields;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;


public class Past4ToTridasDefaults extends TridasMetadataFieldSet {
	
	enum HeaderFields{
		PERSID;
	}
	
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.PROJ_ACTIVE_GROUP, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.PROJ_CREATION_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.PROJ_EDIT_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.PROJ_GROUPS, new IntegerDefaultValue(1));
		setDefaultValue(DefaultFields.PROJ_LOCKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.PROJ_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJ_PASSWORD, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJ_PERSID, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJ_RECORDS, new IntegerDefaultValue(1));
		setDefaultValue(DefaultFields.PROJ_REFERENCE, new IntegerDefaultValue(-1));
		setDefaultValue(DefaultFields.PROJ_SAMPLE, new IntegerDefaultValue(-1));
		setDefaultValue(DefaultFields.PROJ_VERSION, new IntegerDefaultValue(400));
		setDefaultValue(DefaultFields.PROJ_DESCRIPTION, new StringDefaultValue());

		setDefaultValue(DefaultFields.GRP_NAME, new StringDefaultValue());
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
		setDefaultValue(DefaultFields.GRP_OWNER, new IntegerDefaultValue(-1, -1, Integer.MAX_VALUE));
		setDefaultValue(DefaultFields.GRP_DESCRIPTION, new StringDefaultValue());

		setDefaultValue(DefaultFields.KEYCODE, new StringDefaultValue());
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
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue());
		setDefaultValue(DefaultFields.LOCATION, new StringDefaultValue());
		setDefaultValue(DefaultFields.WALDKANTE, new StringDefaultValue());
		setDefaultValue(DefaultFields.FIRST_VALID_RING, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.LAST_VALID_RING, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.USE_VALID_RINGS_ONLY, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.QUALITY, new IntegerDefaultValue());
		
		setDefaultValue(HeaderFields.PERSID, new StringDefaultValue());

	}
	
	protected TridasWoodCompleteness getDefaultWoodCompleteness(){
		Boolean include = false;
		TridasWoodCompleteness wc = new TridasWoodCompleteness();
		TridasPith pith = new TridasPith();
		TridasSapwood sapwood = new TridasSapwood();
		TridasHeartwood heartwood = new TridasHeartwood();
		TridasBark bark = new TridasBark();
		
		heartwood.setPresence(ComplexPresenceAbsence.UNKNOWN);
		bark.setPresence(PresenceAbsence.ABSENT);
		
		if(getPast4BooleanDefaultValue(DefaultFields.PITH).getValue()!=null)
		{
			if(getPast4BooleanDefaultValue(DefaultFields.PITH).getValue()==true)
			{
				pith.setPresence(ComplexPresenceAbsence.COMPLETE);
				include = true;
			}
			else 
			{
				pith.setPresence(ComplexPresenceAbsence.ABSENT);
				include = true;
			}
		}
		else
		{
			pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
		}
		
		if(getIntegerDefaultValue(DefaultFields.SAPWOOD).getValue()!=null)
		{
			sapwood.setNrOfSapwoodRings(getIntegerDefaultValue(DefaultFields.SAPWOOD).getValue());
			sapwood.setPresence(ComplexPresenceAbsence.UNKNOWN);
			include = true;
		}
		else
		{
			sapwood.setPresence(ComplexPresenceAbsence.UNKNOWN);
		}
		
		if(include)
		{
			wc.setSapwood(sapwood);
			wc.setHeartwood(heartwood);
			wc.setPith(pith);
			wc.setBark(bark);
			return wc;
		}
		
		return null;
		
	}
		
	protected TridasDerivedSeries getDefaultTridasDerivedSeries()
	{
		TridasDerivedSeries ds = super.getDefaultTridasDerivedSeries();
		
		// Title
		if(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue()!=null)
		{
			ds.setTitle(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue());
		}
		
		// Author
		if(getStringDefaultValue(HeaderFields.PERSID).getStringValue()!=null)
		{
			ds.setAuthor(getStringDefaultValue(HeaderFields.PERSID).getStringValue());
		}
		
		return ds;
	}
	
	
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries()
	{
		TridasMeasurementSeries ms = super.getDefaultTridasMeasurementSeries();
		
		// Title
		if(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue()!=null)
		{
			ms.setTitle(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue());
		}
		
		// Analyst
		if(getStringDefaultValue(HeaderFields.PERSID).getStringValue()!=null)
		{
			ms.setAnalyst(getStringDefaultValue(HeaderFields.PERSID).getStringValue());
		}
		
		ms.setWoodCompleteness(getDefaultWoodCompleteness());
		
		return ms;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasProject()
	 */
	@Override
	protected TridasProject getDefaultTridasProject() {
		TridasProject p = super.getDefaultTridasProject();
		
		if((getStringDefaultValue(DefaultFields.PROJ_NAME).getStringValue()!=null) &&
		   (getStringDefaultValue(DefaultFields.PROJ_NAME).getStringValue())!="")
		{
			p.setTitle(getStringDefaultValue(DefaultFields.PROJ_NAME).getStringValue());
		}
		
		if(getDateTimeDefaultValue(DefaultFields.PROJ_CREATION_DATE).getValue()!=null) 
		{
			p.setCreatedTimestamp(getDateTimeDefaultValue(DefaultFields.PROJ_CREATION_DATE).getValue());
		}
		
		if(getDateTimeDefaultValue(DefaultFields.PROJ_EDIT_DATE).getValue()!=null) 
		{
			p.setLastModifiedTimestamp(getDateTimeDefaultValue(DefaultFields.PROJ_EDIT_DATE).getValue());
		}
		
		if((getStringDefaultValue(DefaultFields.PROJ_PERSID).getStringValue()!=null) &&
				   (getStringDefaultValue(DefaultFields.PROJ_PERSID).getStringValue())!="")
		{
					p.setInvestigator(getStringDefaultValue(DefaultFields.PROJ_PERSID).getStringValue());
		}
		
		
		return p;
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasObject()
	 */
	@Override
	protected TridasObject getDefaultTridasObject() {
		TridasObject o = super.getDefaultTridasObject();
		
		// Name
		if(getStringDefaultValue(DefaultFields.GRP_NAME).getValue()!=null)
		{
			o.setTitle(getStringDefaultValue(DefaultFields.GRP_NAME).getValue());
		}
		
		// Type
		ControlledVoc type = new ControlledVoc();
		type.setValue("PAST4 Group");
		o.setType(type);
		
		// Owner
		if(getIntegerDefaultValue(DefaultFields.GRP_OWNER).getValue()!=null)
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("past4.ownerIndex");
			gf.setValue(String.valueOf(getIntegerDefaultValue(DefaultFields.GRP_OWNER).getValue()));
			gf.setType("xs:int");
			o.getGenericFields().add(gf);
		}
		
		// Quality
		if(getIntegerDefaultValue(DefaultFields.GRP_QUALITY).getValue()!=null)
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("past4.qualityIndex");
			gf.setValue(String.valueOf(getIntegerDefaultValue(DefaultFields.GRP_QUALITY).getValue()));
			gf.setType("xs:int");
			o.getGenericFields().add(gf);
		}
		
		return o;
	}
	
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		
		if(getStringDefaultValue(DefaultFields.SPECIES).getStringValue()!=null)
		{
			e.setTaxon(ITRDBTaxonConverter.getControlledVocFromString(getStringDefaultValue(DefaultFields.SPECIES).getStringValue()));
		}
		
		
		
		return e;
	}
	
	protected TridasSample getDefaultTridasSample(){
		TridasSample s = super.getDefaultTridasSample();
		
		return s;
	}
	
	protected TridasRadius getDefaultTridasRadius(){
		TridasRadius r = super.getDefaultTridasRadius();
		return r;
	}
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
		
		//valuesGroup.setUnitless(new TridasUnitless());
		TridasUnit units = new TridasUnit();
		units.setValue(I18n.getText("unknown"));
		valuesGroup.setUnit(units);

		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
	
		return valuesGroup;
	}
}
