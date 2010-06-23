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

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.tridas.schema.TridasLocationGeometry;

/**
 * Various static helper functions for working with coordinates
 * 
 * @author peterbrewer
 */
public class CoordinatesUtils {
	

	
	/**
	 * Convert DMS format coordinate into decimal degrees, where W and S are
	 * indicated by negative degrees.
	 * 
	 * @param degrees
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	public static Double getDecimalCoords(Integer degrees, Integer minutes, Integer seconds) {
		Double coords = 0.0;
		
		if (degrees != null) {
			coords = Double.valueOf(degrees);
		}
		
		if (minutes != null) {
			coords = coords + Double.valueOf(minutes) / 60;
		}
		
		if (seconds != null) {
			coords = coords + (Double.valueOf(minutes) / 60) / 60;
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
	public static Double getDecimalCoords(String sign, Integer degrees, Integer minutes, Integer seconds) {
		Double coords = getDecimalCoords(degrees, minutes, seconds);
		
		if (sign.equalsIgnoreCase("S") || sign.equalsIgnoreCase("W")) {
			coords = 0 - coords;
		}
		
		return coords;
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
}
