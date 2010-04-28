/**
 * Created on Apr 19, 2010, 10:49:08 PM
 */
package org.tridas.io.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarning.WarningType;

/**
 * @author daniel
 *
 */
public abstract class AbstractMetadataFieldSet implements IMetadataFieldSet {

	private final static SimpleLogger log = new SimpleLogger(AbstractMetadataFieldSet.class);
	
	private final HashMap<Enum<?>, AbstractDefaultValue<?>> valueMap = new HashMap<Enum<?>, AbstractDefaultValue<?>>();
	private final ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
	
	public AbstractMetadataFieldSet(){
		initDefaultValues();
	}
	
	/**
	 * initialize the default values in this set
	 */
	protected abstract void initDefaultValues();
	
	
	public Set<Enum<?>> getEnumKeyset(){
		return valueMap.keySet();
	}
	
	
	/**
	 * @see org.tridas.io.defaults.IMetadataFieldSet2#getDefaultValue(java.lang.Enum)
	 */
	@Override
	public AbstractDefaultValue<?> getDefaultValue(Enum<?> argValueType) {
		return valueMap.get(argValueType);
	}
	
	
	/**
	 * Helper method to return the {@link IntegerDefaultValue} object
	 * @param argValueType
	 * @return the {@link IntegerDefaultValue} if mapped, or null if not mapped or 
	 * the key isn't mapped to an {@link IntegerDefaultValue}.
	 */
	public IntegerDefaultValue getIntegerDefaultValue(Enum<?> argValueType){
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if(val instanceof IntegerDefaultValue){
			return (IntegerDefaultValue) val;
		}else{
			log.debug("The default value object returned by the field '"+argValueType+"' was not" +
			" an IntegerDefaultValue");
			return null;
		}
	}
	
	/**
	 * Helper method to return the {@link StringDefaultValue} object;
	 * @param argValueType
	 * @return the {@link StringDefaultValue} if mapped, or null if not mapped or 
	 * the key isn't mapped to an {@link StringDefaultValue}.
	 */
	public StringDefaultValue getStringDefaultValue(Enum<?> argValueType){
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if(val instanceof StringDefaultValue){
			return (StringDefaultValue) val;
		}else{
			log.debug("The default value object returned by the field '"+argValueType+"' was not" +
					" a StringDefaultValue");
			return null;
		}
	}

	/**
	 * sets the default value object for the given Enum
	 * @param argValueType
	 * @param argValue
	 */
	protected void setDefaultValue(Enum<?> argValueType,
			AbstractDefaultValue<?> argValue) {
		argValue.setKey(argValueType);
		argValue.setParent(this);
		valueMap.put(argValueType, argValue);
	}

	/**
	 * @see org.tridas.io.defaults.IMetadataFieldSet2#addIgnoredWarning(java.lang.Enum, java.lang.String)
	 */
	@Override
	public void addIgnoredWarning(Enum<?> argKey, String argText) {
		ConversionWarning w = new ConversionWarning(WarningType.IGNORED, argText, argKey.toString());
		log.debug("Warning from defaults: "+w);
		warnings.add(w);
	}

	/**
	 * @see org.tridas.io.defaults.IMetadataFieldSet2#addTruncatedWarning(java.lang.Enum, java.lang.String)
	 */
	@Override
	public void addTruncatedWarning(Enum<?> argKey, String argText) {
		ConversionWarning w = new ConversionWarning(WarningType.TRUNCATED, argText, argKey.toString());
		log.debug("Warning from defaults: "+w);
		warnings.add(w);
	}

	/**
	 * @see org.tridas.io.defaults.IMetadataFieldSet2#getConversionWarnings()
	 */
	@Override
	public ArrayList<ConversionWarning> getConversionWarnings() {
		return warnings;
	}
}
