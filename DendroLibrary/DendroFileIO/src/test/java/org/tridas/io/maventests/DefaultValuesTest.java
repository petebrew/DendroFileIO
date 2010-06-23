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
package org.tridas.io.maventests;

import junit.framework.TestCase;

import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;

public class DefaultValuesTest extends TestCase {
	
	public void testStringResizing() {
		StringDefaultValue defString = new StringDefaultValue("Hello", -1, -1);
		assertEquals("Hello", defString.getValue());
		defString.setMinLength(10);
		assertEquals("Hello     ", defString.getValue());
		
		IntegerDefaultValue defInt = new IntegerDefaultValue(3);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		assertEquals("3", defInt.getStringValue());
		defInt.setMinLength(5);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		assertEquals("3    ", defInt.getStringValue());
	}
	
	public void testIntegerRanging() {
		IntegerDefaultValue defInt = new IntegerDefaultValue(3);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		assertEquals("3", defInt.getStringValue());
		defInt.setMinLength(5);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		assertEquals("3    ", defInt.getStringValue());
		
		defInt.setMax(5);
		defInt.setMin(0);
		defInt.setValue(10);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		defInt.setValue(-1);
		assertEquals(Integer.valueOf(3), defInt.getValue());
		
		defInt.setFriendlyRangeValidation(true);
		defInt.setValue(10);
		assertEquals(Integer.valueOf(5), defInt.getValue());
		defInt.setValue(-1);
		assertEquals(Integer.valueOf(0), defInt.getValue());
	}
	
	public void testOverriding() {
		StringDefaultValue defString = new StringDefaultValue("Hello", -1, -1);
		assertEquals("Hello", defString.getValue());
		defString.setMinLength(10);
		defString.setOverridingValue("Goodbye");
		defString.setValue("wait!");
		assertEquals("Goodbye   ", defString.getValue());
		
		IntegerDefaultValue defInt = new IntegerDefaultValue(3);
		defInt.setOverridingValue(10);
		defInt.setValue(0);
		assertEquals(Integer.valueOf(10), defInt.getValue());
	}
}
