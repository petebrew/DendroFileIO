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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.maventests.TridasTransformTests;
import org.tridas.schema.Certainty;
import org.tridas.schema.DateTime;
import org.tridas.schema.NormalTridasDatingType;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public class DateUtils {
	private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

	
	public enum DatePrecision {
		DAY, MONTH, YEAR;
	}
	
	
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
			
			// Remove time info
			requestedDate.setTimezone(DatatypeConstants.FIELD_UNDEFINED);  
			//requestedDate.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);  

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
		return DateUtils.getDateTime(day, month, year, hours, minutes, 0);
	}
	
	/**
	 * Create a DateTime from the constituent day, month, year, hours, minutes and seconds
	 * 
	 * @param day
	 * @param month
	 * @param year
	 * @param hours
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	public static DateTime getDateTime(Integer day, Integer month, Integer year, Integer hours, Integer minutes, Integer seconds) {
		try {
			// Month is base 0 in Gregorian Calendar so minus 1 so this function can 
			// have logical month integers!
			GregorianCalendar c = new GregorianCalendar(year, month-1, day, hours, minutes, seconds);
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
		if(timeParts.length>3) return null;
	
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
	 * Wrapper around parseDateFromDayMonthYearString assuming standard
	 * European order for date string
	 * 
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static DateTime parseDateFromDayMonthYearString(String date) throws Exception
	{
		try{
			DateTime dt =  parseDateFromDayMonthYearString(date, false);
			return dt;
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
	/**
	 * Try and parse a DateTime from a string.  If americanOrder=false then 
	 * supported styles are:
	 *   - dd/MM/yy
	 *   - dd.MM.yy
	 *   - dd-MM-yy
	 *   - dd/MM/yyyy
	 *   - dd.MM.yyyy
	 *   - dd-MM-yyyy
	 *   - ddMMyyyy
	 *   
	 *   Otherwise dd and MM are reversed
	 *   
	 *   For two digit years, yy>50 is interpreted as 19xx, and yy<50 is
	 *   interpreted as 20xx
	 *   
	 *   All other formats return null.
	 *   
	 * @param date
	 * @param americanOrder
	 * @return
	 * @throws Exception
	 */
	public static DateTime parseDateFromDayMonthYearString(String date, Boolean americanOrder) throws Exception
	{
		if(date==null) return null;
		
		date = date.trim();
		if(date.equals("")) return null;
	
		int partOne = 0;
		int partTwo = 0;
		int year = 0;

		if(date.matches("(\\d{1,2}(/|\\.|-)\\d{1,2}(/|\\.|-)(19|20)\\d\\d)$"))
		{
			// Old style dd/MM/yyyy or dd-MM-yyyy or dd.MM.yyyy
			log.debug("String matches dd/MM/yyyy or dd-MM-yyyy or dd.MM.yyyy");
			String[] parts = date.split("(/|-|\\.)");
			
			if(parts.length==3)
			{
				partOne = Integer.parseInt(parts[0]);
				partTwo = Integer.parseInt(parts[1]);
				year = Integer.parseInt(parts[2]);
			}
			
		}
		else if(date.matches("^(\\d{2}(/|\\.|-)\\d{2}(/|\\.|-)\\d\\d)$"))
		{
			// Old style dd/MM/yy or dd-MM-yy assuming 20th or 21st century year
			log.debug("String matches dd/MM/yy or dd-MM-yy assuming 20th or 21st century year");
			String[] parts = date.split("(/|-|\\.)");
			if(parts.length==3)
			{
				partOne = Integer.parseInt(parts[0]);
				partTwo = Integer.parseInt(parts[1]);
				year = Integer.parseInt(parts[2]);
			}
			if(year>50)
			{
				year = year+1900;
			}
			else
			{
				year = year+2000;
			}
			
		}
		else if(date.matches("(\\d{4}(19|20)\\d{2})$"))
		{
			// Newer style ddMMyyyy 
			log.debug("String matches ddMMyyyy");

			partOne = Integer.parseInt(date.substring(0,2));
			partTwo = Integer.parseInt(date.substring(2,4));
			year = Integer.parseInt(date.substring(4,8));
		}
		else if(date.matches("(\\d{4}\\d{2})$"))
		{
			// Shorter style ddMMyy
			log.debug("String matches ddMMyy");
			partOne = Integer.parseInt(date.substring(0,2));
			partTwo = Integer.parseInt(date.substring(2,4));
			year = Integer.parseInt(date.substring(4,6));
			if(year>50)
			{
				year = year+1900;
			}
			else
			{
				year = year+2000;
			}
		}
		else if(date.matches("((18|19|20)\\d\\d[- /\\.](0[1-9]|1[012])[- /\\.](0[1-9]|[12][0-9]|3[01]))$"))
		{
			log.debug("String matches YYYY-MM-DD, YYYY/MM/DD, YYYY MM DD or YYYY.MM.DD");
			// YYYY-MM-DD format
			String[] parts = date.split("(/|-|\\.)");
			year = Integer.parseInt(parts[0]);
			partTwo = Integer.parseInt(parts[2]);
			partOne = Integer.parseInt(parts[1]);
			
		}
		else if(date.matches("((18|19|20)\\d\\d[- /\\.](0[1-9]|1[012]))$"))
		{
			log.debug("String matches YYYY-MM");
			// YYYY-MM format
			String[] parts = date.split("(/|-|\\.)");
			year = Integer.parseInt(parts[0]);
			
			if(americanOrder)
			{
				partOne = Integer.parseInt(parts[1]);
				partTwo = 1;	
			}
			else
			{
				partTwo = Integer.parseInt(parts[1]);
				partOne = 1;	
			}
			
			
			
		}	
		else if(date.matches("((18|19|20)\\d\\d)$"))
		{
			log.debug("String matches YYYY");
			// YYYY format
			String[] parts = date.split("(/|-|\\.)");
			
			if(parts.length>0)
			{
				year = Integer.parseInt(parts[0]);
				partOne = 1;
				partTwo = 1;
			}
			else
			{
				year =  Integer.parseInt(date);
				partOne = 1;
				partTwo = 1;
			}
			
		}	
		else
		{
			return null;
		}
		
		// Handle order
		int day = partOne;
		int month = partTwo;
		if(americanOrder)
		{
			day = partTwo;
			month = partOne;
		}
		
		// Check date is logical
		if(day>0 && month >0 && year > 0 && day<=31 && month<=12)
		{
			DateTime newDT = DateUtils.getDateTime(day, month, year);
			
			if(newDT.getValue().getDay()!=day)
			{
				// DateTime has been altered because day and month combination was invalid
				// e.g. 30th Feb.  Be safe and fail. 
				return null;
			}
						
			// Check date is not in the future
			DateTime currDT = DateUtils.getTodaysDateTime();
			if(newDT.getValue().toGregorianCalendar().compareTo(currDT.getValue().toGregorianCalendar())<=0)
			{
				return newDT;
			}
		}

		return null;
	}
	
	public static DatePrecision getDatePrecision(String date)
	{
		
		if(date.matches("((18|19|20)\\d\\d[- /.](0[1-9]|1[012]))$"))
		{
			//YYYY-MM
			return DatePrecision.MONTH;
			
		}	
		else if(date.matches("((18|19|20)\\d\\d)$"))
		{
			//YYYY
			return DatePrecision.YEAR;
		}	
		
		
		return DatePrecision.DAY;
		
		
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
	
	/**
	 * Converts a DateTime into a string formatted using the provided SimpleDateFormat
	 *  
	 * @param date
	 * @param format SimpleDateFormat
	 * @return
	 */
	public static String getFormattedDateTime(DateTime date, SimpleDateFormat dateFormat) {
				
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
	
	/**
	 * Converts a DateTime into a string formatted using the provided SimpleDateFormat
	 *  
	 * @param date
	 * @param format SimpleDateFormat
	 * @return
	 */
	public static String getFormattedDate(org.tridas.schema.Date date, SimpleDateFormat dateFormat) {
				
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
	
	/**
	 * Converts a DateTime into a PAST4 style date string (dd/MM/yyyy h:mm:ss a). If
	 * the supplied date is null, then it returns the correct string for todays date.
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateTimePast4Style(DateTime date) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy h:mm:ss a");
		
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
	
	/**
	 * Converts a DateTime into a TRIMS style date string (dd/MM/yyyy). If
	 * the supplied date is null, then it returns the correct string for todays date.
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateTimeTRIMSStyle(Date date) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		if(date==null)
		{
			Calendar calendar = Calendar.getInstance();	
			return dateFormat.format(calendar.getTime());
		}
		else
		{
			return dateFormat.format(date);
		}

	}
	
	/**
	 * Parse a DateTime from a PAST4 format string (dd/MM/yyyy hh:mm)
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static DateTime parseDateFromPast4String(String date) throws Exception
	{
		
		if(date==null) return null;
		
		date = date.trim();
		if(date.equals("")) return null;
	
		int day = 0;
		int month = 0;
		int year = 0;
		int hours = 0;
		int mins = 0;
		int seconds = 0;
		boolean pmFlag = false;
		
		if(date.toLowerCase().contains("pm"))
		{
			pmFlag = true;
		}
		
		if(date.matches("\\d{1,2}(/)\\d{1,2}(/)\\d{4} \\d{1,2}:\\d{2}([APMapm :\\d])*"))
		{
			String[] parts = date.split(" ");
			if(!(parts.length>=2))
			{
				throw new Exception();
			}
			
			String[] dateparts = parts[0].split("/");
			if(dateparts.length!=3)
			{
				throw new Exception();
			}
			day = Integer.parseInt(dateparts[0]);
			month = Integer.parseInt(dateparts[1]);
			year = Integer.parseInt(dateparts[2]);
			
			String[] timeparts = parts[1].split(":");
			if(!(timeparts.length>=2))
			{
				throw new Exception();
			}
			hours = Integer.parseInt(timeparts[0]);
			mins = Integer.parseInt(timeparts[1]);
			
			if(timeparts.length>=3)
			{
				seconds = Integer.parseInt(timeparts[2]);
			}
			
			if(pmFlag && hours>0)
			{
				hours = hours+12; 
			}
	
		}
		
		if(day>0 && month >0 && year > 0)
		{
			return DateUtils.getDateTime(day, month, year, hours, mins, seconds);
		}
		else
		{
			return null;
		}
		

		
	}
		
	public static DateTime parseDateTimeFromShortNaturalString(String str)
	{
		// Go the whole hog first and see if that works
		DateTime dt = parseDateTimeFromNaturalString(str);
		if(dt!=null) return dt;
		
		if(str.length()==4 && str.matches("^[1-2][0-9][0-9][0-9]"))
		{			
			return DateUtils.getDateTime(1,1,Integer.valueOf(str));
		}
				
		
		return null;
		
		
	}
	
	
	/**
	 * Attempt to parse a DateTime from a natural language string.
	 * 
	 * The first attempt is using regex for various international layouts
	 * (e.g. 28/10/2012).  If this fails, then try various American layouts
	 * (e.g. 10/28/2012).  If this also fails, then the last ditch chance
	 * is to use the Natty library.  Natty is a natural language parsing 
	 * library.  It has various limitations including being English
	 * specific, but can interpret a wide variety of layouts.
	 * 
	 * @param str
	 * @return
	 */
	public static DateTime parseDateTimeFromNaturalString(String str)
	{
	
		DateTime datetime = null;
		
		try {
			
			Locale currentLocale = Locale.getDefault();
			
			log.debug("Locale country is "+currentLocale.getCountry());
			
			if(currentLocale.getCountry()=="US")
			{
				
				
				// Try American format first
				datetime = parseDateFromDayMonthYearString(str, true);
				if(datetime!=null)	
				{
					return datetime;
				}
				
				// Failed so try International format
				datetime = parseDateFromDayMonthYearString(str, false);
				if(datetime!=null)	
				{
					return datetime;
				}
			}
			else
			{
			
				// Try international format first
				datetime = parseDateFromDayMonthYearString(str, false);
				if(datetime!=null)	
				{
					return datetime;
				}
				
				// Failed so try American format next
				datetime = parseDateFromDayMonthYearString(str, true);
				if(datetime!=null)	
				{
					return datetime;
				}
			}
		} catch (Exception e) {		
			
			e.printStackTrace();
			System.out.println("Failed to parse manually, now try Natty");
			
			
		}
		
		// Failed so now try using Natty		
		
		Parser parser = new Parser();
		ArrayList<DateGroup> groups = (ArrayList<DateGroup>) parser.parse(str);
		
		if(groups==null) return null;
		if(groups.size()==0) return null;
		
		DateGroup group = groups.get(0);
		if(group==null) return null;
		
		Date dt = group.getDates().get(0);
		
		if(dt==null) return null;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		
		datetime = DateUtils.getDateTime(cal.get(Calendar.DAY_OF_MONTH), 
				cal.get(Calendar.MONTH)+1, 
				cal.get(Calendar.YEAR));
		
		if(datetime== null) return null;
		
		Calendar cal2 = Calendar.getInstance();
		
		if(cal.get(Calendar.YEAR)==cal2.get(Calendar.YEAR) &&
				cal.get(Calendar.MONTH)==cal2.get(Calendar.MONTH) &&
				cal.get(Calendar.DAY_OF_MONTH)==cal2.get(Calendar.DAY_OF_MONTH))
		{
			// Same as today so probably failed to parse
			return null;
		}
		
		
		if(groups.size()>1 || group.getDates().size()>1)
		{
			datetime.setCertainty(Certainty.APPROXIMATELY);
		}
		else
		{
			datetime.setCertainty(Certainty.EXACT);
		}
		
		return datetime;

	}
	
	/**
	 * Convert from org.tridas.schema.DateTime 
	 * to org.tridas.schema.Date
	 * 
	 * @param dt
	 * @return
	 */
	public static org.tridas.schema.Date dateTimeToDate(DateTime dt)
	{
		if(dt==null) return null;
		
		XMLGregorianCalendar xcal = dt.getValue();
		org.tridas.schema.Date d = new org.tridas.schema.Date();
		d.setValue(xcal);
		d.setCertainty(dt.getCertainty());
		
		return d;

	}
	
}
