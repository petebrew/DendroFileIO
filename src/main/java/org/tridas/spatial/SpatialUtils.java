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
package org.tridas.spatial;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * Various static helper functions for working with coordinates
 * 
 * @author peterbrewer
 */
public class SpatialUtils {
	
	//private static final Logger log = LoggerFactory.getLogger(SpatialUtils.class);	

	// TODO
	// Dumbed down for now - remember to check all uses of this when changing back to
	// the correct URN style
	
	// Full URN requires coordinates in y,x or lat,long order
	//public static String WGS84_FULL_URN = "urn:ogc:def:crs:EPSG:6.6:4326";
	
	// Simplified reference requires coordinates in x,y or long,lat order
	public static String WGS84 = "EPSG:4326";
		
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
				coords = coords + Double.valueOf(minutes) / 60.0;
			}
			else
			{
				throw new NumberFormatException("Minutes out of bounds");
			}
		}
		
		if (seconds != null) {
			if(seconds>=0.0 && seconds < 60.0)
			{
				Double secpart = ((Double.valueOf(seconds) / 60.0) / 60.0);
				coords = coords + secpart;
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
			return coords;
		}
		else if (sign.equalsIgnoreCase("N") || sign.equalsIgnoreCase("E")) {
			return coords;
		}
		
		throw new NumberFormatException("Coordinate direction must be one of N,S,E or W, but direction was '"+sign+"'");
		
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
		
		sign = sign.trim();
		
		if (sign.equalsIgnoreCase("S") || sign.equalsIgnoreCase("W")) {
			coords = 0 - coords;
			return coords;
		}
		else if (sign.equalsIgnoreCase("N") || sign.equalsIgnoreCase("E")) {
			return coords;
		}
		
		throw new NumberFormatException("Coordinate direction must be one of N,S,E or W, but direction was '"+sign+"'");

	}
	
	/**
	 * Create a TridasLocationGeometry from decimal latitude and longitudes in WGS84
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static TridasLocationGeometry getWGS84LocationGeometry(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			return null;
		}
		
		Pos pos = new Pos();
		ArrayList<Double> values = new ArrayList<Double>();
				
		TridasLocationGeometry geometry = new TridasLocationGeometry();
		PointType point = new PointType();
		
		
		// Lat,Long order when using full URN coordinate reference
		//point.setSrsName(SpatialUtils.WGS84_FULL_URN);
		//values.add(latitude);
		//values.add(longitude);

		// Long,Lat order when using simplied WGS84 coordinate reference
		point.setSrsName(SpatialUtils.WGS84);
		values.add(longitude);
		values.add(latitude);
		
		pos.setValues(values);
		
		point.setPos(pos);
		geometry.setPoint(point);
		return geometry;
				
	}
	
	/**
	 * Helper function for getting the JMapProjLib Projection
	 * for the British National Grid using PROJ4 specification
	 * 
	 * @return
	 */
	public static Projection getBritishNationalGrid()
	{
		return ProjectionFactory.fromPROJ4Specification(
				new String[] {	
						"+proj=tmerc",
						"+lat_0=49",
						"+lon_0=-2",
						"+k=0.9996012717",
						"+x_0=400000",
						"+y_0=-100000",
						"+ellps=airy",
						"+datum=OSGB36",
						"+units=m +no_defs" 	
				}
			);
	}
	

	
	/**
	 * Converts a SN****** style British National Grid coordinate into EPSG:4326 
	 * lat long coordinates. As BNG refs refer to tiles, this function also sets
	 * the precision of the reference depending on the size of the tile referred 
	 * to.  For instance 12 character reference refers to 1m tiles, therefore 
	 * precision is set to 1m.
	 * 
	 * @param bngStr
	 * @return
	 */
	public static TridasLocation getLocationGeometryFromBNG(String bngStr)
	{


		Projection  projBNG = getBritishNationalGrid();
		
		Point2D.Double pnt = BNGLetterReftoBNGNumberRef(bngStr);
		Point2D.Double des = new Point2D.Double();
		projBNG.inverseTransform(pnt, des);

		
		TridasLocationGeometry geom = getWGS84LocationGeometry(des.getY(), des.getX());
		TridasLocation loc = new TridasLocation();
		loc.setLocationGeometry(geom);
		
		// Set precision using number of digits in BNG ref
		switch(bngStr.length())
		{
			case 14: loc.setLocationPrecision("0.1"); break;
			case 12: loc.setLocationPrecision("1"); break;
			case 10: loc.setLocationPrecision("10"); break;
			case 8: loc.setLocationPrecision("100"); break;
			case 6: loc.setLocationPrecision("1000"); break;
			case 4: loc.setLocationPrecision("10000"); break;
		}
				
		return loc;
	}
	
	/**
	 * Convert a British National Grid number reference to standard
	 * letter+number reference.
	 * 
	 * @param e - easting
	 * @param n - nothing
	 * @param digits - number of digits to use
	 * @return
	 */
	public static String BNGNumRefToBNGLetRef(Double e, Double n, Integer digits) {
		  // get the 100km-grid indices
		  Double e100k = Math.floor(e/100000), n100k = Math.floor(n/100000);
		  
		  if (e100k<0 || e100k>6 || n100k<0 || n100k>12) 
		  {
			  throw new NumberFormatException("Coordinates outside bounds of British National Grid");
		  }

		  // translate those into numeric equivalents of the grid letters
		  Double d1 = (19-n100k) - (19-n100k)%5 + Math.floor((e100k+10)/5);
		  Double d2 = (19-n100k)*5%25 + e100k%5;
		  
		  int l1 = d1.intValue();
		  int l2 = d2.intValue();

		  // compensate for skipped 'I' and build grid letter-pairs
		  if (l1 > 7) l1++;
		  if (l2 < 7) l2++;
		  		  
		  String.valueOf((char) l1);
		  
		  //String letPair = String.fromCharCode(l1+"A".charCodeAt(0), l2+'A'.charCodeAt(0));
		  String letPair = String.valueOf((char)l1)+String.valueOf((char)l1);
		  
		  // strip 100km-grid indices from easting &amp; northing, and reduce precision
		  e = Math.floor((e%100000)/Math.pow(10,5-digits/2));
		  n = Math.floor((n%100000)/Math.pow(10,5-digits/2));

		  String gridRef = letPair + StringUtils.leftPad(e+"", digits/2) + StringUtils.leftPad(n+"", digits/2);

		  return gridRef;
		}
	
	/**
	 * Converts British National Grid reference with letter prefix to full numeric
	 * reference style.  As standard SN****** style codes refer to tiles, this 
	 * function returns the coordinate as the centre of the referenced tile. Works on
	 * references up to 12 characters long (=1m grid tiles)
	 * 
	 * @param gridref
	 * @return
	 */
	public static Point2D.Double BNGLetterReftoBNGNumberRef(String gridref) {
		
		gridref = gridref.trim();
		
		if(gridref==null) return null;
		
		gridref = gridref.replace(" ", "");
		
		if(gridref==null) return null;
		
		if(gridref.length()<4)
		{
			throw new NumberFormatException("Unable to extract coordinates from string.  String must be 4 or more characters");
		}
		if(gridref.length()>12)
		{
			throw new NumberFormatException("Unable to extract coordinates from string.  String must be 12 or less characters");
		}
		if(!gridref.matches("([A-Za-z]){2}([0-9])+"))
		{
			throw new NumberFormatException("String does not match the British National Grid coordinate style");
		}

		
		
		// get numeric values of letter references
		String letters = "ABCDEFGHJKLMNOPQRSTUVWXYZ";
		String firstlet = gridref.substring(0,1).toUpperCase();
		String secondlet = gridref.substring(1,2).toUpperCase();
		int l1 = letters.indexOf(firstlet);
		int l2 = letters.indexOf(secondlet);
		
		// convert grid letters into 100km-square indexes from false origin (grid square SV):
		Integer e = (((l1-2)%5)*5 + (l2%5)) * 100000;
		Integer n =  ((int) ((19-Math.floor(l1/5)*5) - Math.floor(l2/5))) * 100000;
		
		
		String eastnumstr = gridref.substring(2,(gridref.length()/2)+1);
		String northnumstr = gridref.substring((gridref.length()/2)+1);
		
		Integer eastnum = Integer.parseInt(eastnumstr);
		Integer northnum = Integer.parseInt(northnumstr);
		
		
		
		  // normalise to 1m grid, rounding up to centre of grid square:
		  switch (gridref.length()) {
		    case 4: 
		    	eastnum = eastnum*1000;
		    	eastnum += 500; 
		    	northnum = northnum*1000;
		    	northnum += 500; 
		    	break;
		    case 6: 
		    	eastnum = eastnum*1000;
		    	eastnum += 500; 
		    	northnum = northnum*1000;
		    	northnum += 500; 
		    	break;
		    case 8: 
		    	eastnum = eastnum*100;
		    	eastnum += 50; 
		    	northnum = northnum*100;
		    	northnum += 50; 
		    	break;
		    case 10: 
		    	eastnum = eastnum*10;
		    	eastnum += 5; 
		    	northnum = northnum*10;
		    	northnum += 5; 
		    	break;
		    // 12-digit refs are already 1m
		  }
		
		  eastnum = eastnum+e;
		  northnum = northnum+n;
		  
		  Point2D.Double point = new Point2D.Double();
		  point.x = eastnum.doubleValue();
		  point.y = northnum.doubleValue();
		  
		  return point;
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
