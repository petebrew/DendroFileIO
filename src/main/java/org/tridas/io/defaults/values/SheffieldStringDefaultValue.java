package org.tridas.io.defaults.values;

import org.grlea.log.SimpleLogger;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractDefaultValue;
import org.tridas.io.util.StringUtils;

public class SheffieldStringDefaultValue extends AbstractDefaultValue<String> {
	
	private static final SimpleLogger log = new SimpleLogger(SheffieldStringDefaultValue.class);
	private String value = null;
	
	public SheffieldStringDefaultValue(){
		
	}
	
	public SheffieldStringDefaultValue(String argValue){
		value = argValue;
	}
	
	public SheffieldStringDefaultValue(String argValue, int argMinLength, int argMaxLength){
		super(argMaxLength, argMinLength);
		value = argValue;
	}
	
	/**
	 * @see org.tridas.io.defaults.IDefaultValue#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}
	
	// we already validate the string as we would in AbstractDefaultValue
	@Override
	public String getStringValue(){
		if(value == null){
			return "";
		}
		return value;
	}

	
	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#validateAndSetValue(java.lang.Object)
	 */
	@Override
	protected boolean validateAndSetValue(String argValue) {
		if(argValue == null){
			value = null;
			return true;
		}
		argValue = validValue(argValue);
		if(argValue != null){
			log.verbose("string value stored: "+argValue);
			value = argValue;
			return true;
		}
		return false;
	}



	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#setMaxLength(int)
	 */
	@Override
	public void setMaxLength(int argMaxLength) {
		super.setMaxLength(argMaxLength);
		setValue(getValue());
	}

	/**
	 * @see org.tridas.io.defaults.AbstractDefaultValue#setMinLength(int)
	 */
	@Override
	public void setMinLength(int argMinLength) {
		super.setMinLength(argMinLength);
		setValue(getValue());
	}

	
	// basically recreation of getStringValue from AbstractDefaultValue, as it
	// works with strings as well
	private String validValue(String argValue){
		String value = argValue.toString();
		
		// Replace restricted characters with something that looks similar
		value.replaceAll("(", "[");
		value.replaceAll(")", "]");
		value.replaceAll(",", ";");
		value.replaceAll("\"", "''");
				
		if(getMaxLength() != -1){
			if(value.length() > getMaxLength()){
				log.warn(I18n.getText("fileio.defaults.stringTooBig",value, getMaxLength()+""));
				if(getParent() == null){
					log.error(I18n.getText("fileio.defaults.nullParent"));
				}else{
					getParent().addTruncatedWarning(getKey(), I18n.getText("fileio.defaults.stringTooBig",value, getMaxLength()+""));
				}
				return StringUtils.rightPadWithTrim(value, getMaxLength());
			}
		}
		if(getMinLength() != -1){
			if(value.length() < getMinLength()){
				log.debug(I18n.getText("fileio.defaults.stringTooSmall",value, isPadRight()+""));
				if(isPadRight()){
					return StringUtils.rightPad(value, getMinLength());					
				}else{
					return StringUtils.leftPad(value, getMinLength());
				}
			}
		}
		return value;
	}

	
}
