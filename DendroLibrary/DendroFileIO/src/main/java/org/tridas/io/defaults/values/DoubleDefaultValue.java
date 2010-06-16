package org.tridas.io.defaults.values;

import org.apache.commons.lang.StringUtils;
import org.grlea.log.SimpleLogger;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractDefaultValue;

public class DoubleDefaultValue extends AbstractDefaultValue<Double> {
	private static final SimpleLogger log = new SimpleLogger(DoubleDefaultValue.class);
	
	private Double value = null;
	private double max = Double.MAX_VALUE;
	private double min = Double.MIN_VALUE;
	private boolean changeToRange = false;
	
	public DoubleDefaultValue() {

	}
	
	public DoubleDefaultValue(Double argDouble) {
		this(argDouble, Double.MIN_VALUE, Double.MAX_VALUE);
	}
	
	public DoubleDefaultValue(Double argDouble, Double argMin, Double argMax) {
		this(argDouble, argMin, argMax, -1, -1);
	}
	
	public DoubleDefaultValue(Double argMin, Double argMax) {
		this(null, argMin, argMax);
	}
	
	/**
	 * Constructor for a default field of type double
	 * 
	 * @param argDouble
	 * @param argMin
	 * @param argMax
	 * @param argMinLength
	 * @param argMaxLength
	 */
	public DoubleDefaultValue(Double argDouble, Double argMin, Double argMax, int argMinLength, int argMaxLength) {
		super(argMaxLength, argMinLength);
		min = argMin;
		max = argMax;
		value = argDouble;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public Double getValue() {
		return value;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#setValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(Double argValue) {
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
	
	private Double validValue(Double argValue) {
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
	 * Maximum the double can be
	 * 
	 * @param max
	 *            the max to set
	 */
	public void setMax(double max) {
		this.max = max;
		setValue(getValue());
	}
	
	/**
	 * Maximum the double can be
	 * 
	 * @return the max
	 */
	public double getMax() {
		return max;
	}
	
	/**
	 * Minimum the double can be
	 * 
	 * @param min
	 *            the min to set
	 */
	public void setMin(double min) {
		this.min = min;
		setValue(getValue());
	}
	
	/**
	 * Minimum the double can be
	 * 
	 * @return the min
	 */
	public double getMin() {
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
