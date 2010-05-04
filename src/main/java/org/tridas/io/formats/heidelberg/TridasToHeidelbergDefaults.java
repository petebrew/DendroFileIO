package org.tridas.io.formats.heidelberg;

import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;

public class TridasToHeidelbergDefaults extends AbstractMetadataFieldSet implements IMetadataFieldSet {

	private static final SimpleLogger log = new SimpleLogger(TridasToHeidelbergDefaults.class);

	public enum HeidelbergField{
		KEY_CODE,
		DATA_FORMAT,
		SERIES_TYPE,
		LENGTH,
		DATEBEGIN,
		DATEEND,
		DATED,
		LOCATION,
		SPECIES
	}
	
	@Override
	protected void initDefaultValues() {
		setDefaultValue(HeidelbergField.KEY_CODE, new StringDefaultValue("1234567"));
		setDefaultValue(HeidelbergField.DATA_FORMAT, new StringDefaultValue("Tree"));
		setDefaultValue(HeidelbergField.SERIES_TYPE, new StringDefaultValue("Single curve"));
		setDefaultValue(HeidelbergField.LENGTH, new IntegerDefaultValue(null, 0, 99999));
		setDefaultValue(HeidelbergField.DATEBEGIN, new IntegerDefaultValue());
		setDefaultValue(HeidelbergField.DATEEND, new IntegerDefaultValue());
		// TODO do the rest of these
	}
	
}
