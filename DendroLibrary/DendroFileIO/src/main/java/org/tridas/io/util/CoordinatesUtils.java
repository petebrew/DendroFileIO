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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.tridas.schema.TridasLocationGeometry;

/**
 * Various static helper functions for working with coordinates
 * 
 * @author peterbrewer
 */
public class CoordinatesUtils {
	
	public static String WGS84 = "urn:ogc:def:crs:EPSG:6.6:4326";

	
	/**
	 * Convert DMS format coordinate into decimal degrees, where W and S are
	 * indicated by negative degrees.
	 * 
	 * @param degrees
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	public static Double getDecimalCoords(Integer degrees, Integer minutes, Integer seconds) throws NumberFormatException {
		Double deg = null;
		Double min = null;
		Double sec = null;
		
		if (degrees != null) {
			deg = Double.valueOf(degrees);
		}
		
		if (minutes != null) {
			min = Double.valueOf(minutes);
		}
		
		if (seconds != null) {
			sec = Double.valueOf(minutes);
		}
		
		return getDecimalCoords(deg, min, sec);
	}
	
	/**
	 * Convert DMS format coordinate into decimal degrees, where W and S are
	 * indicated by negative degrees.
	 * 
	 * @param degrees
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	public static Double getDecimalCoords(Double degrees, Double minutes, Double seconds) throws NumberFormatException
	{
		Double coords = 0.0;
		
		if (degrees != null) {
			if(degrees<=180.0 && degrees>=-180.0)
			{
				coords = degrees;
			}
			else
			{
				throw new NumberFormatException("Degrees out of bounds");
			}
		}
		else
		{
			throw new NumberFormatException("Degrees cannot be null in a coordinate");
		}
		
		if (minutes != null) {
			if(minutes>=0.0 && minutes <60.0)
			{
				coords = coords + Double.valueOf(minutes) / 60;
			}
			else
			{
				throw new NumberFormatException("Minutes out of bounds");
			}
		}
		
		if (seconds != null) {
			if(seconds>=0.0 && seconds < 60.0)
			{
				coords = coords + (Double.valueOf(minutes) / 60) / 60;
			}
			else
			{
				throw new NumberFormatException("Seconds out of bounds");
			}
		}
		
		return coords;
	}
	
	/**
	 * Convert DMS with NSEW sign into decimal coordinates
	 * 
	 * @param sign
	 * @param degrees
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	public static Double getDecimalCoords(String sign, Integer degrees, Integer minutes, Integer seconds) throws NumberFormatException{
		Double coords = getDecimalCoords(degrees, minutes, seconds);
				
		if (sign.equalsIgnoreCase("S") || sign.equalsIgnoreCase("W")) {
			coords = 0 - coords;
		}
		else if (sign.equalsIgnoreCase("N") || sign.equalsIgnoreCase("E")) {
			return coords;
		}
		
		throw new NumberFormatException("Coordinate direction must be one of N,S,E or W");
		
	}
	
	/**
	 * Convert DMS with NSEW sign into decimal coordinates
	 * 
	 * @param sign
	 * @param degrees
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	public static Double getDecimalCoords(String sign, Double degrees, Double minutes, Double seconds) throws NumberFormatException {
		Double coords = getDecimalCoords(degrees, minutes, seconds);
		
		if (sign.equalsIgnoreCase("S") || sign.equalsIgnoreCase("W")) {
			coords = 0 - coords;
		}
		else if (sign.equalsIgnoreCase("N") || sign.equalsIgnoreCase("E")) {
			return coords;
		}
		
		throw new NumberFormatException("Coordinate direction must be one of N,S,E or W");
	}
	
	/**
	 * Create a TridasLocationGeometry from decimal latitude and longitudes
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static TridasLocationGeometry getLocationGeometry(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			return null;
		}
		
		Pos pos = new Pos();
		ArrayList<Double> values = new ArrayList<Double>();
		
		values.add(latitude);
		values.add(longitude);
		pos.setValues(values);
		return getLocationGeometry(pos);
		
	}
	
	/**
	 * Create a TridasLocationGeoemtry from a GML pos
	 * 
	 * @param pos
	 * @return
	 */
	public static TridasLocationGeometry getLocationGeometry(Pos pos) {
		TridasLocationGeometry geometry = new TridasLocationGeometry();
		PointType point = new PointType();
		point.setSrsName(CoordinatesUtils.WGS84);
		point.setPos(pos);
		geometry.setPoint(point);
		return geometry;
		
	}
	
	/**
	 * Attempt to extract a decimal latitude from a complete lat long string.
	 * If unsuccessful it returns null.
	 * 
	 * @param str
	 * @return
	 */
	public static Double parseLatitudeFromLatLongString(String str)
	{
		
		return null;
	}
	
	/**
	 * Attempt to extract a decimal longitude from a complete lat long string.
	 * If unsuccessful it returns null.
	 * 
	 * @param str
	 * @return
	 */
	public static Double parseLongitudeFromLatLongString(String str)
	{
		
		return null;
	}
	
	
	/**
	 * Attempt to convert a string to a decimal lat or lon value.
	 * 
	 * @param str
	 * @return
	 */
	public static Double parseLatLonFromHalfLatLongString(String str) throws NumberFormatException
	{
		str = str.trim();
		str = str.toUpperCase();
		if(str==null) return null;
		if(str=="")   return null;
				
		String regex;
		Pattern p;
		Matcher m;
		Double deg = null;
		Double min = null;
		Double sec = null;
		String sign = null;
		
		// CHECK FOR DECIMAL DEGREE NOTATION
		regex = "[^\\d.-]";
		p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		m = p.matcher(str);
		if (!m.find())
		{ 
			// Only digits, minus sign and/or decimal point found
			Double val =  Double.parseDouble(str);
			if(val<=180.0 && val>=-180.0)
			{
				// Value is within likely bounds so return
				return val;
			}
			else
			{
				// Value too big or small for a lat/long so return null
				throw new NumberFormatException("Parsed coordinate value outside lat/long bounds");
			}
		}
		
		// CHECK FOR DMS NOTATION
		regex = "[^\\d|^.|^-]+";
		String[] val = str.split(regex);
		if(val.length==0)
		{
			// Either no numbers found, or more than three numbers were found
			throw new NumberFormatException("Coordinate string in unknown format");
		}
		else if (val.length>3)
		{
			throw new NumberFormatException("Coordinate string in unknown format");
		}
		
		try{
			deg = Double.parseDouble(val[0]);
		} catch (Exception e)
		{
			throw new NumberFormatException("Coordinate string in unknown format");
		}
		
		
		if(val.length>=2) min = Double.parseDouble(val[1]);
		if(val.length==3) sec = Double.parseDouble(val[2]);
		
		
		// Determine if direction sign is present
		String firstChar = str.substring(0,1);
		String lastChar = str.substring(str.length()-1, str.length());
		if(str.startsWith("N") || str.startsWith("S") || str.startsWith("E") || str.startsWith("W"))
		{
			sign = firstChar;
		}
		else if (str.endsWith("N") || str.endsWith("S") || str.endsWith("E") || str.endsWith("W"))
		{
			sign = lastChar;
		}
		else{
			// First and last chars aren't NSEW so check they are digits otherwise fail
			regex = "[\\d|-]";
			p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			m = p.matcher(firstChar);
			if (!m.find())
			{
				throw new NumberFormatException("Invalid direction sign found in coordinate string.  Direction sign must be one of N,S,E or W");
			}
			regex = "[\\d|.]";
			p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			m = p.matcher(lastChar);
			if (!m.find())
			{
				throw new NumberFormatException("Invalid direction sign found in coordinate string.  Direction sign must be one of N,S,E or W");
			}
		}
		
		if(sign!=null)
		{
			return getDecimalCoords(sign, deg, min, sec);
		}
		else
		{
			return getDecimalCoords(deg, min, sec);
		}
		
	}
}
