package org.tridas.io.defaults.values;

public class Past4BooleanDefaultValue extends BooleanDefaultValue {

	public Past4BooleanDefaultValue() {

	}
	
	public Past4BooleanDefaultValue(Boolean argValue) {
		value = argValue;
	}
	
	public Past4BooleanDefaultValue(String argValue)
	{
		setValueFromString(argValue);
		
	}
	
	public void setValueFromString(String argValue)
	{
		if(argValue==null)
		{
			value = null;
		}
		else if(argValue.equalsIgnoreCase("TRUE") || argValue.equals("1"))
		{
			value = true;
		}
		else if (argValue.equalsIgnoreCase("FALSE") || argValue.equals("0"))
		{
			value = false;
		}
		else
		{
			value = null;
		}
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
