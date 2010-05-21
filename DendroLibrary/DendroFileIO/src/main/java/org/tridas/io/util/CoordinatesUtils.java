package org.tridas.io.util;

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
	
}
