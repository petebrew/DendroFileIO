/*******************************************************************************
 * Copyright 2011 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
