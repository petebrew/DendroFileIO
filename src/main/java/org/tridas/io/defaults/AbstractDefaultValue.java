/**
 * Created at: 8:28:56 PM, Apr 19, 2010
 */
package org.tridas.io.defaults;

import org.grlea.log.SimpleLogger;
import org.tridas.io.I18n;
import org.tridas.io.util.StringUtils;

/**
 * @author Daniel
 *
 */
public abstract class AbstractDefaultValue<E extends Object> implements Cloneable {
	private static final SimpleLogger log = new SimpleLogger(AbstractDefaultValue.class);
	
	private boolean overriding = false;
	private boolean padRight = true;
	private int maxLength = -1;
	private int minLength = -1;
	private IMetadataFieldSet parent;
	private Enum<?> key;
	
	public AbstractDefaultValue(){
		
	}
	
	/**
	 * 
	 * @param argMaxLength maximum length of returned string.  -1 disables
	 * @param argMinLength minimum length of returned string.  -1 disables
	 */
	public AbstractDefaultValue(int argMaxLength, int argMinLength){
		maxLength = argMaxLength;
		minLength = argMinLength;
	}
	
	protected void setKey(Enum<?> argKey){
		key = argKey;
	}
	
	/**
	 * The enum key that this object is mapped from
	 * @return
	 */
	public Enum<?> getKey(){
		return key;
	}
	
	protected void setParent(IMetadataFieldSet argParent){
		parent = argParent;
		setValue(getValue());
	}
	
	/**
	 * @return the parent
	 */
	public IMetadataFieldSet getParent() {
		return parent;
	}

	/**
	 * Sets the value of this field. 
	 * @param argValue
	 * @return if the value is set.  If false, that means the value wasn't valid, or
	 * it's being overridden (see {@link #setOverridingValue(Object)} and {@link #setOverriding(boolean)}).
	 */
	public boolean setValue(E argValue){
		if(overriding){
			return false;
		}
		return validateAndSetValue(argValue);
	}
	
	/**
	 * Validate and set the value.
	 * @param argValue the value, can be null.
	 * @return true if valid and set, false if not set and invalid
	 */
	protected abstract boolean validateAndSetValue(E argValue);
	
	/**
	 * Sets the value that will override other calls to {@link #setValue(Object)}.  To
	 * turn overriding off call {@link #setOverriding(boolean)}
	 * @param argValue
	 * @return if the value was set.  if false, that means it wasn't valid
	 */
	public boolean setOverridingValue(E argValue){
		overriding = true;
		return validateAndSetValue(argValue);
	}
	
	/**
	 * Gets the value.
	 * @return the value.  can be null
	 */
	public abstract E getValue();
	
	/**
	 * Gets the string representation of the value.  If {@link #setMinLength(int)} is set,
	 * then the string will be padded.  If {@link #getValue()} is null, an empty string is
	 * returned (if minLength is set, then it is padded as well).
	 * @return
	 */
	public String getStringValue(){
		String value;
		if(getValue() == null){
			value = "";
		}else{
			value = getValue().toString();
		}
		if(maxLength != -1){
			if(value.length() > maxLength){
				log.error(I18n.getText("fileio.defaults.stringTooBig",value, maxLength+""));
				if(getParent() == null){
					log.error(I18n.getText("fileio.defaults.nullParent"));
				}else{
					parent.addTruncatedWarning(getKey(),I18n.getText("fileio.defaults.stringTooBig",value, maxLength+""));
				}
				return StringUtils.rightPadWithTrim(value, maxLength);
			}
		}
		if(minLength != -1){
			if(value.length() < minLength){
				log.debug(I18n.getText("fileio.defaults.stringTooSmall",value,padRight+""));
				if(padRight){
					return StringUtils.rightPad(value, minLength);					
				}else{
					return StringUtils.leftPad(value, minLength);
				}
			}
		}
		return value;
	}
	
	/**
	 * If the value in this object is overriding.  If this is true, values set with
	 * {@link #setOverridingValue(Object)} do not change the value.
	 * Default of false.
	 * @return
	 */
	public boolean isOverriding(){
		return overriding;
	}
	/**
	 * If the value in this object is overriding.  If this is true, values set with
	 * {@link #setOverridingValue(Object)} do not change the value.
	 * Default of false.
	 * @param argOverriding
	 */
	public void setOverriding(boolean argOverriding){
		overriding = argOverriding;
	}
	
	
	/**
	 * Sets the maximum string length.  Use '-1' to 
	 * not enforce this
	 * @param maxLength the maxLength to set
	 */
	public void setMaxLength(int argMaxLength){
		maxLength = argMaxLength;
	}

	/**
	 * The maximum string length
	 * @return the maxLength
	 */
	public int getMaxLength(){
		return maxLength;
	}

	/**
	 * Sets the minimum string length.  Use '-1' to 
	 * not enforce this
	 * @param minLength the minLength to set
	 */
	public void setMinLength(int argMinLength){
		minLength = argMinLength;
	}

	/**
	 * The minimum string length
	 * @return the minLength
	 */
	public int getMinLength(){
		return minLength;
	}
	
	/**
	 * Sets if we pad from the right if the string is too short.
	 * If false, we pad from the left
	 * @param padRight the padRight to set
	 */
	public void setPadRight(boolean padRight) {
		this.padRight = padRight;
	}

	/**
	 * If we pad the string from the right if it's too short.
	 * @return the padRight
	 */
	public boolean isPadRight() {
		return padRight;
	}
	
	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Could not clone for some reason, bad bad bad");
			return this; // should now happen
		}
	}
}
