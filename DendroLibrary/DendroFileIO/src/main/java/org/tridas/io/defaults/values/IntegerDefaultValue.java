/**
 * Created at: 8:12:54 PM, Apr 19, 2010
 */
package org.tridas.io.defaults.values;

import org.grlea.log.SimpleLogger;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractDefaultValue;

/**
 * @author Daniel
 */
public class IntegerDefaultValue extends AbstractDefaultValue<Integer> {
	private static final SimpleLogger log = new SimpleLogger(IntegerDefaultValue.class);
	
	private Integer value = null;
	private int max = Integer.MAX_VALUE;
	private int min = Integer.MIN_VALUE;
	private boolean changeToRange = false;
	
	public IntegerDefaultValue() {

	}
	
	public IntegerDefaultValue(Integer argInteger) {
		this(argInteger, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public IntegerDefaultValue(Integer argInteger, int argMin, int argMax) {
		this(argInteger, argMin, argMax, -1, -1);
	}
	
	public IntegerDefaultValue(int argMin, int argMax) {
		this(null, argMin, argMax);
	}
	
	/**
	 * Constructor for a default field of type integer
	 * 
	 * @param argInteger
	 * @param argMin
	 * @param argMax
	 * @param argMinLength
	 * @param argMaxLength
	 */
	public IntegerDefaultValue(Integer argInteger, int argMin, int argMax, int argMinLength, int argMaxLength) {
		super(argMaxLength, argMinLength);
		min = argMin;
		max = argMax;
		value = argInteger;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public Integer getValue() {
		return value;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#setValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(Integer argValue) {
		if (argValue == null) {
			value = null;
			return true;
		}
		argValue = validValue(argValue);
		if (argValue != null) {
			value = argValue;
			return true;
		}
		return false;
	}
	
	private Integer validValue(Integer argValue) {
		if (argValue == null) {
			return null;
		}
		if (argValue.intValue() > max) {
			String text = I18n.getText("fileio.defaults.numTooBig", "" + argValue, "" + max, "" + changeToRange);
			log.warn(text);
			if (getParent() == null) {
				log.error(I18n.getText("fileio.defaults.nullParent"));
			}
			else {
				getParent().addIgnoredWarning(getKey(), text);
			}
			if (changeToRange) {
				return max;
			}
			else {
				return null;
			}
		}
		if (argValue < min) {
			String text = I18n.getText("fileio.defaults.numTooSmall", "" + argValue, "" + min, "" + changeToRange);
			log.warn(text);
			if (getParent() == null) {
				log.error(I18n.getText("fileio.defaults.nullParent"));
			}
			else {
				getParent().addIgnoredWarning(getKey(), text);
			}
			if (changeToRange) {
				return min;
			}
			else {
				return null;
			}
		}
		return argValue;
	}
	
	/**
	 * Maximum the integer can be
	 * 
	 * @param max
	 *            the max to set
	 */
	public void setMax(int max) {
		this.max = max;
		setValue(getValue());
	}
	
	/**
	 * Maximum the integer can be
	 * 
	 * @return the max
	 */
	public int getMax() {
		return max;
	}
	
	/**
	 * Minimum the integer can be
	 * 
	 * @param min
	 *            the min to set
	 */
	public void setMin(int min) {
		this.min = min;
		setValue(getValue());
	}
	
	/**
	 * Minimum the integer can be
	 * 
	 * @return the min
	 */
	public int getMin() {
		return min;
	}
	
	/**
	 * if true, it will move the value into the range instead of disregarding
	 * the modification. default of false
	 * 
	 * @param changeToRange
	 *            the changeToRange to set
	 */
	public void setFriendlyRangeValidation(boolean changeToRange) {
		this.changeToRange = changeToRange;
	}
	
	/**
	 * if true, it will move the value into the range instead of disregarding
	 * the modification. default of false
	 * 
	 * @return the changeToRange
	 */
	public boolean isFriendlyRangeValidation() {
		return changeToRange;
	}
}
