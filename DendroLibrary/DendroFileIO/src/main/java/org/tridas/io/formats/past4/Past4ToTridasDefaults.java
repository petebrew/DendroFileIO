package org.tridas.io.formats.past4;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.Past4BooleanDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.past4.TridasToPast4Defaults.DefaultFields;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;


public class Past4ToTridasDefaults extends TridasMetadataFieldSet {

	
	
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
		setDefaultValue(DefaultFields.GRP_OWNER, new IntegerDefaultValue(-1));
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
	
	
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		
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
		
		valuesGroup.setUnitless(new TridasUnitless());

		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
	
		return valuesGroup;
	}
}
