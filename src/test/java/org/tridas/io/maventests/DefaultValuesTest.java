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
