package org.tridas.io.util;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.sheffield.SheffieldToTridasDefaults.DefaultFields;
import org.tridas.schema.TridasLocationGeometry;

public class CoordinatesUtils {

	
	public static Double getDecimalCoords(Integer degrees, Integer minutes, Integer seconds)
	{
		Double coords = 0.0;
		
		if(degrees!=null)
		{
			coords = Double.valueOf(degrees);
		}
		
		if(minutes!=null)
		{
			coords = coords + Double.valueOf(minutes)/60;
		}
		
		if(seconds!=null)
		{
			coords = coords + (Double.valueOf(minutes)/60)/60;
		}
		
		return coords;
	}
	
	public static Double getDecimalCoords(String sign, Integer degrees, Integer minutes, Integer seconds)
	{
		Double coords = getDecimalCoords(degrees,minutes,seconds);
		
		if(sign.equalsIgnoreCase("S") || sign.equalsIgnoreCase("W"))
		{
			coords = 0 - coords;
		}
		
		return coords;
	}
	
	public static TridasLocationGeometry getLocationGeometry(Pos pos)
	{
		TridasLocationGeometry geometry = new TridasLocationGeometry();
		PointType point = new PointType();
		point.setPos(pos);
		geometry.setPoint(point);
		return geometry;

	}
}
