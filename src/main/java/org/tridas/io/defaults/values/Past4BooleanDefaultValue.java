package org.tridas.io.defaults.values;

public class Past4BooleanDefaultValue extends BooleanDefaultValue {

	public Past4BooleanDefaultValue() {

	}
	
	public Past4BooleanDefaultValue(Boolean argValue) {
		value = argValue;
	}
	
	@Override
	public String getStringValue() {
		if (value == true) {
			return "TRUE";
		}
		else if (value == false) {
			return "FALSE";
		}
		return null;
	}
	
}
