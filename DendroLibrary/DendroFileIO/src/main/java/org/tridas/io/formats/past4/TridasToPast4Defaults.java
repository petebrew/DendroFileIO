package org.tridas.io.formats.past4;

import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.Past4BooleanDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.DateUtils;

public class TridasToPast4Defaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	enum DefaultFields{
		PROJ_ACTIVE_GROUP,
		PROJ_EDIT_DATE,
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
		setDefaultValue(DefaultFields.PROJ_EDIT_DATE, new StringDefaultValue(DateUtils.getDateTimePast4Style(null)));
		setDefaultValue(DefaultFields.PROJ_GROUPS, new IntegerDefaultValue(1));
		setDefaultValue(DefaultFields.PROJ_LOCKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.PROJ_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJ_PASSWORD, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJ_PERSID, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROJ_RECORDS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.PROJ_REFERENCE, new IntegerDefaultValue(-1));
		setDefaultValue(DefaultFields.PROJ_SAMPLE, new IntegerDefaultValue(-1));
		setDefaultValue(DefaultFields.PROJ_VERSION, new IntegerDefaultValue(400));
		setDefaultValue(DefaultFields.PROJ_DESCRIPTION, new StringDefaultValue());

		setDefaultValue(DefaultFields.GRP_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.GRP_VISIBLE, new Past4BooleanDefaultValue(true));
		setDefaultValue(DefaultFields.GRP_LOCKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_CHANGED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_EXPANDED, new Past4BooleanDefaultValue(true));
		setDefaultValue(DefaultFields.GRP_USE_COLOR, new Past4BooleanDefaultValue(true));
		setDefaultValue(DefaultFields.GRP_HAS_MEAN_VALUE, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_IS_CHRONO, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_CHECKED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_SELECTED, new Past4BooleanDefaultValue(false));
		setDefaultValue(DefaultFields.GRP_COLOR, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.GRP_QUALITY, new IntegerDefaultValue());
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
		setDefaultValue(DefaultFields.OFFSET, new IntegerDefaultValue());
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

}
