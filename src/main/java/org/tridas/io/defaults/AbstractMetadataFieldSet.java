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
package org.tridas.io.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.values.BooleanDefaultValue;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.SheffieldStringDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;

/**
 * @author daniel
 */
public abstract class AbstractMetadataFieldSet implements IMetadataFieldSet {
	
	private final static SimpleLogger log = new SimpleLogger(AbstractMetadataFieldSet.class);
	
	private HashMap<Enum<?>, AbstractDefaultValue<?>> valueMap = new HashMap<Enum<?>, AbstractDefaultValue<?>>();
	private ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
	
	public AbstractMetadataFieldSet() {
		initDefaultValues();
	}
	
	/**
	 * initialize the default values in this set
	 */
	protected abstract void initDefaultValues();
	
	public Set<Enum<?>> getEnumKeyset() {
		return valueMap.keySet();
	}
	
	/**
	 * @see org.tridas.io.defaults.IMetadataFieldSet#getDefaultValue(java.lang.Enum)
	 */
	@Override
	public AbstractDefaultValue<?> getDefaultValue(Enum<?> argValueType) {
		return valueMap.get(argValueType);
	}
	
	/**
	 * Helper method to return the {@link IntegerDefaultValue} object
	 * 
	 * @param argValueType
	 * @return the {@link IntegerDefaultValue} if mapped, or null if not mapped or
	 *         the key isn't mapped to an {@link IntegerDefaultValue}.
	 */
	public IntegerDefaultValue getIntegerDefaultValue(Enum<?> argValueType) {
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if (val instanceof IntegerDefaultValue) {
			return (IntegerDefaultValue) val;
		}
		else {
			log.debug("The default value object returned by the field '" + argValueType + "' was not"
					+ " an IntegerDefaultValue");
			return null;
		}
	}
	
	/**
	 * Helper method to return the {@link SafeIntYearDefaultValue} object
	 * 
	 * @param argValueType
	 * @return the {@link SafeIntYearDefaultValue} if mapped, or null if not mapped or
	 *         the key isn't mapped to an {@link SafeIntYearDefaultValue}.
	 */
	public SafeIntYearDefaultValue getSafeIntYearDefaultValue(Enum<?> argValueType) {
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if (val instanceof SafeIntYearDefaultValue) {
			return (SafeIntYearDefaultValue) val;
		}
		else {
			log.debug("The default value object returned by the field '" + argValueType + "' was not"
					+ " an SafeIntYearDefaultValue");
			return null;
		}
	}
	
	/**
	 * Helper method to return the {@link DoubleDefaultValue} object
	 * 
	 * @param argValueType
	 * @return the {@link DoubleDefaultValue} if mapped, or null if not mapped or
	 *         the key isn't mapped to an {@link DoubleDefaultValue}.
	 */
	public DoubleDefaultValue getDoubleDefaultValue(Enum<?> argValueType) {
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if (val instanceof DoubleDefaultValue) {
			return (DoubleDefaultValue) val;
		}
		else {
			log.debug("The default value object returned by the field '" + argValueType + "' was not"
					+ " a DoubleDefaultValue");
			return null;
		}
	}
	
	/**
	 * Helper method to return the {@link BooleanDefaultValue} object;
	 * 
	 * @param argValueType
	 * @return the {@link BooleanDefaultValue} if mapped, or null if not mapped or
	 *         the key isn't mapped to an {@link BooleanDefaultValue}.
	 */
	public BooleanDefaultValue getBooleanDefaultValue(Enum<?> argValueType) {
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if (val instanceof BooleanDefaultValue) {
			return (BooleanDefaultValue) val;
		}
		else {
			log.debug("The default value object returned by the field '" + argValueType + "' was not"
					+ " a BooleanDefaultValue");
			return null;
		}
	}
	
	/**
	 * Helper method to return the {@link DateTimeDefaultValue} object;
	 * 
	 * @param argValueType
	 * @return the {@link DateTimeDefaultValue} if mapped, or null if not mapped or
	 *         the key isn't mapped to an {@link DateTimeDefaultValue}.
	 */
	public DateTimeDefaultValue getDateTimeDefaultValue(Enum<?> argValueType) {
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if (val instanceof DateTimeDefaultValue) {
			return (DateTimeDefaultValue) val;
		}
		else {
			log.debug("The default value object returned by the field '" + argValueType + "' was not"
					+ " a DateTimeDefaultValue");
			return null;
		}
	}
	
	/**
	 * Helper method to return the {@link StringDefaultValue} object;
	 * 
	 * @param argValueType
	 * @return the {@link StringDefaultValue} if mapped, or null if not mapped or
	 *         the key isn't mapped to an {@link StringDefaultValue}.
	 */
	public StringDefaultValue getStringDefaultValue(Enum<?> argValueType) {
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if (val instanceof StringDefaultValue) {
			return (StringDefaultValue) val;
		}
		else {
			log.debug("The default value object returned by the field '" + argValueType + "' was not"
					+ " a StringDefaultValue");
			return null;
		}
	}
	
	
	/**
	 * Helper method to return the {@link SheffieldStringDefaultValue} object;
	 * 
	 * @param argValueType
	 * @return the {@link StringDefaultValue} if mapped, or null if not mapped or
	 *         the key isn't mapped to an {@link StringDefaultValue}.
	 */
	public SheffieldStringDefaultValue getSheffieldStringDefaultValue(Enum<?> argValueType) {
		AbstractDefaultValue<?> val = getDefaultValue(argValueType);
		if (val instanceof SheffieldStringDefaultValue) {
			return (SheffieldStringDefaultValue) val;
		}
		else {
			log.debug("The default value object returned by the field '" + argValueType + "' was not"
					+ " a SheffieldStringDefaultValue");
			return null;
		}
	}
	
	/**
	 * sets the default value object for the given Enum
	 * 
	 * @param argValueType
	 * @param argValue
	 */
	protected void setDefaultValue(Enum<?> argValueType, AbstractDefaultValue<?> argValue) {
		argValue.setKey(argValueType);
		argValue.setParent(this);
		valueMap.put(argValueType, argValue);
	}
	
	/**
	 * @see org.tridas.io.defaults.IMetadataFieldSet2#addIgnoredWarning(java.lang.Enum,
	 *      java.lang.String)
	 */
	@Override
	public void addIgnoredWarning(Enum<?> argKey, String argText) {
		ConversionWarning w = new ConversionWarning(WarningType.IGNORED, argText, argKey.toString());
		log.debug("Warning from defaults: " + w);
		warnings.add(w);
	}
	
	/**
	 * @see org.tridas.io.defaults.IMetadataFieldSet2#addTruncatedWarning(java.lang.Enum,
	 *      java.lang.String)
	 */
	@Override
	public void addTruncatedWarning(Enum<?> argKey, String argText) {
		ConversionWarning w = new ConversionWarning(WarningType.TRUNCATED, argText, argKey.toString());
		log.debug("Warning from defaults: " + w);
		warnings.add(w);
	}
	
	public void addConversionWarning(ConversionWarning argWarning) {
		warnings.add(argWarning);
	}
	
	/**
	 * @see org.tridas.io.defaults.IMetadataFieldSet2#getWarnings()
	 */
	@Override
	public ArrayList<ConversionWarning> getWarnings() {
		return warnings;
	}
	
	public Set<Enum<?>> keySet(){
		return valueMap.keySet();
	}
	
	@Override
	public Object clone() {
		AbstractMetadataFieldSet o;
		try {
			o = (AbstractMetadataFieldSet) super.clone();
		} catch (CloneNotSupportedException e) {
			return this; // should not happen
		}
		o.valueMap = new HashMap<Enum<?>, AbstractDefaultValue<?>>();
		o.warnings = new ArrayList<ConversionWarning>();
		
		for (Enum<?> e : valueMap.keySet()) {
			o.valueMap.put(e, (AbstractDefaultValue<?>) valueMap.get(e).clone());
		}
		for (ConversionWarning cw : warnings) {
			o.warnings.add(cw);
		}
		return o;
	}
}
