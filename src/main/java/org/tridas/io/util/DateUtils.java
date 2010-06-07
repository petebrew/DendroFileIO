package org.tridas.io.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.tridas.schema.DateTime;

public class DateUtils {
	
	public static DateTime getTodaysDateTime() {
		try {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(new Date());
			XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			DateTime blah = new DateTime();
			blah.setValue(now);
			return blah;
		} catch (DatatypeConfigurationException e) {}
		return new DateTime();
	}
	
	public static DateTime getDateTime(Integer day, Integer month, Integer year) {
		try {
			GregorianCalendar c = new GregorianCalendar(year, month, day);
			XMLGregorianCalendar requestedDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			DateTime returnval = new DateTime();
			returnval.setValue(requestedDate);
			return returnval;
		} catch (DatatypeConfigurationException e) {}
		return null;
	}
	
}
