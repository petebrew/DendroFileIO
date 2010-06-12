package org.tridas.io.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
