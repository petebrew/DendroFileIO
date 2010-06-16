package org.tridas.io.util;

import java.util.ArrayList;

import org.tridas.io.I18n;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class UnitUtils {

	/**
	 * Convert a data value from one unit to another
	 * 
	 * @param inputunits
	 * @param outputunits
	 * @param value
	 * @return
	 */
	public static Double convertValue(NormalTridasUnit inputunits, NormalTridasUnit outputunits, Double value)
	{
		Double val = value;

		// If either input or output units are not defined, then just return the value;
		if (inputunits==null || outputunits == null) {return value;}
		
		// Convert to METRES first
		switch(inputunits)
		{
		case METRES:
			break;
		case CENTIMETRES:
			val = value/ 100;
			break;
		case MILLIMETRES:
			val = value / 1000;
			break;
		case TENTH_MM:
			val = value / 10000;
			break;
		case HUNDREDTH_MM:
			val = value / 100000;
			break;
		case MICROMETRES:
			val = value / 1000000;
			break;
		default:
			return null;
		}
		
		// Now convert to the specified output units
		switch(outputunits)
		{
		case METRES:
			break;
		case CENTIMETRES:
			val = val * 100;
			break;
		case MILLIMETRES:
			val = val * 1000;
			break;
		case TENTH_MM:
			val = val * 10000;
			break;
		case HUNDREDTH_MM:
			val = val * 100000;
			break;
		case MICROMETRES:
			val = val * 1000000;
			break;
		default:
			return null;
		}
		
		// Return the value
		return val;
		
	}
	
	/**
	 * Convert a TridasValues tag from one unit to another.  The TridasValues must have 
	 * NormalTridasUnit set otherwise it throws an IncompleteTridasDataException.  If the
	 * outputAsIntegers flag is on, then the data values will be rounded to integers.
	 * 
	 * @param outputunits
	 * @param tv
	 * @param outputAsIntegers
	 * @return
	 * @throws NumberFormatException
	 * @throws IncompleteTridasDataException
	 */
	public static TridasValues convertTridasValues(NormalTridasUnit outputunits, TridasValues tv, Boolean outputAsIntegers) 
	throws NumberFormatException, IncompleteTridasDataException
	{
		ArrayList<TridasValue> values = (ArrayList<TridasValue>) tv.getValues();
		NormalTridasUnit inputunits = null;
		
		try{
			 inputunits = tv.getUnit().getNormalTridas();
		} catch (Exception e)
		{
			throw new IncompleteTridasDataException(I18n.getText("fileio.convertsOnlyTridasUnits"));
		}
		
		for (TridasValue value : values)
		{
			Double dblvalue = Double.parseDouble(value.getValue());
			dblvalue = UnitUtils.convertValue(inputunits, outputunits, dblvalue);
			
			if(outputAsIntegers)
			{
				value.setValue(String.valueOf(Math.round(dblvalue)));
			}
			else
			{
				value.setValue(String.valueOf(dblvalue));
			}
		}
		
		return tv;
	}
}
