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
		KEY_CODE("KeyCode"),
		DATA_FORMAT("DataFormat"),
		SERIES_TYPE("SeriesType"),
		LENGTH("Length"),
		DATEBEGIN("DateBegin"),
		DATEEND("DateEnd"),
		DATED("Dated"),
		LOCATION("Location"),
		SPECIES("Species");
		
		private String key;
		private HeidelbergField(String argKey){
			key = argKey;
		}

		public String getKeyString() {
			return key;
		}
	}
	
	public void loadData(String argKey, String argValue){
		HeidelbergField[] fields = HeidelbergField.values();
		boolean found = false;
		for(HeidelbergField field : fields){
			if(field.getKeyString().equalsIgnoreCase(argKey.trim())){
				AbstractDefaultValue<?> val = getDefaultValue(field);
				if(val == null){
					log.warn("Default value object for field '"+field+"' was not found, ignoring."); // TODO locale
					continue;
				}
				if(val instanceof StringDefaultValue){
					((StringDefaultValue)val).setValue(argValue.trim());
				}else if(val instanceof IntegerDefaultValue){
					((IntegerDefaultValue)val).setValue(Integer.parseInt(argValue.trim()));
				}
				found = true;
			}
		}
		if(!found){
			// TODO locale
			log.warn("For key/value pair "+argKey+"/"+argValue+", the key was not found in the enumeration");
		}
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
