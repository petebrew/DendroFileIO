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
package org.tridas.io.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.tridas.io.I18n;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.schema.DateTime;

public class DateUtils {
	
	/**
	 * Create a DateTime for right now
	 * 
	 * @return
	 */
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
	
	/**
	 * Create a DateTime from the constituent day, month, year
	 * 
	 * @param day
	 * @param month
	 * @param year
	 * @param hours
	 * @param minutes
	 * @return
	 */
	public static DateTime getDateTime(Integer day, Integer month, Integer year) {
		try {
			// Month is base 0 in Gregorian Calendar so minus 1 so this function can 
			// have logical month integers!
			GregorianCalendar c = new GregorianCalendar(year, month-1, day);
			XMLGregorianCalendar requestedDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			DateTime returnval = new DateTime();
			returnval.setValue(requestedDate);
			return returnval;
		} catch (DatatypeConfigurationException e) {}
		return null;
	}
	
	/**
	 * Create a DateTime from the constituent day, month, year, hours and minutes
	 * 
	 * @param day
	 * @param month
	 * @param year
	 * @param hours
	 * @param minutes
	 * @return
	 */
	public static DateTime getDateTime(Integer day, Integer month, Integer year, Integer hours, Integer minutes) {
		try {
			// Month is base 0 in Gregorian Calendar so minus 1 so this function can 
			// have logical month integers!
			GregorianCalendar c = new GregorianCalendar(year, month-1, day, hours, minutes);
			XMLGregorianCalendar requestedDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			DateTime returnval = new DateTime();
			returnval.setValue(requestedDate);
			return returnval;
		} catch (DatatypeConfigurationException e) {}
		return null;
	}
	
	/**
	 * Get a DateTime from a WinDendro style timestamp.  String should be of the format:
	 * d/m/yyyy kk:mm
	 * 
	 * @param timestamp
	 * @return
	 */
	public static DateTime getDateTimeFromWinDendroTimestamp(String timestamp)
	{
		// First use regex to test string is in expected form
		String regex = "^[1-3]{0,1}[0-9]{1}/[0-1]{0,1}[0-9]{1}/[1-2]{1}[0-9]{3} [0-2]{1}[0-9]{1}:[0-5]{1}[0-9]{1}";
		Pattern p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m1 = p1.matcher(timestamp);
		if (!m1.find()){ return null;}
		
		// Split into date and time chunks
		String[] dateAndTime = timestamp.split(" ");
		if(dateAndTime.length!=2) return null;
		
		// Split date chunk into day/month/year
		String[] dateParts = dateAndTime[0].split("/");
		if(dateParts.length!=3) return null;

		// Split time into hours:minutes
		String[] timeParts = dateAndTime[1].split(":");
		if(timeParts.length!=2) return null;
	
		// Convert all to Integers
		Integer day   = null;
		Integer month = null;
		Integer year  = null;
		Integer hours = null;
		Integer mins  = null;
		try{
			day   = Integer.parseInt(dateParts[0]);
			month = Integer.parseInt(dateParts[1]);
			year  = Integer.parseInt(dateParts[2]);
			hours = Integer.parseInt(timeParts[0]);
			mins  = Integer.parseInt(timeParts[1]);
		} catch (NumberFormatException e)
		{
			return null;
		}
		
		
		return DateUtils.getDateTime(day, month, year, hours, mins);

	}
	
	/**
	 * Try and parse a DateTime from a string.  Supported styles are:
	 *   - dd/MM/yy
	 *   - dd.MM.yy
	 *   - dd-MM-yy
	 *   - ddMMyyyy
	 *   
	 *   For two digit years, yy>50 is interpreted as 19xx, and yy<50 is
	 *   interpreted as 20xx
	 *   
	 *   All other formats return null.
	 *   
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static DateTime parseDateFromDayMonthYearString(String date) throws Exception
	{
		if(date==null) return null;
		
		date = date.trim();
		if(date.equals("")) return null;
	
		int day = 0;
		int month = 0;
		int year = 0;

		if(date.matches("\\d\\d(.|-/|)\\d\\d(.|-|/)\\d\\d"))
		{
			// Old style dd/MM/yy or dd-MM-yy assuming 20th century year
			day = Integer.parseInt(date.substring(0,2));
			month = Integer.parseInt(date.substring(3,5));
			year = Integer.parseInt(date.substring(6,8));
			if(year>50)
			{
				year = year+1900;
			}
			else
			{
				year = year+2000;
			}
			
		}
		else
		{
			// Newer style ddMMyyyy
			day = Integer.parseInt(date.substring(0,2));
			month = Integer.parseInt(date.substring(2,4));
			year = Integer.parseInt(date.substring(4,8));
		}
		
		if(day>0 && month >0 && year > 0)
		{
			return DateUtils.getDateTime(day, month, year);
		}
		else
		{
			return null;
		}
			

	}
	
	/**
	 * Converts a DateTime into a Tucson style date string (yyyyMMdd). If
	 * the supplied date is null, then it returns the correct string for
	 * todays date.
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateTimeTucsonStyle(DateTime date) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		if(date==null)
		{
			Calendar calendar = Calendar.getInstance();	
			return dateFormat.format(calendar.getTime());
		}
		else
		{
			return dateFormat.format(date.getValue().toGregorianCalendar().getTime());
		}

	}
	
}
