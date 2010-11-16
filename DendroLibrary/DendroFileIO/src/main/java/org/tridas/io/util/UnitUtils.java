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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.tridas.io.I18n;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class UnitUtils {

	
	
	/**
	 * Convert a BigDecimal value from one unit to another
	 * 
	 * @param inputunits
	 * @param outputunits
	 * @param value
	 * @return
	 */
	public static BigDecimal convertBigDecimal(NormalTridasUnit inputunits, NormalTridasUnit outputunits, BigDecimal value)
	{
		Double val = value.doubleValue();
		
		return BigDecimal.valueOf(UnitUtils.convertDouble(inputunits, outputunits, val));
	}
	
	/**
	 * Convert a BigInteger value from one unit to another
	 * 
	 * @param inputunits
	 * @param outputunits
	 * @param value
	 * @return
	 */
	public static BigInteger convertBigInteger(NormalTridasUnit inputunits, NormalTridasUnit outputunits, BigInteger value)
	{
		Double val = value.doubleValue();
		
		return BigInteger.valueOf(Math.round(UnitUtils.convertDouble(inputunits, outputunits, val)));
	}
	
	/**
	 * Parse a NormalTridasUnit from a string
	 * 
	 * @param str
	 * @return
	 */
	public static NormalTridasUnit parseUnitString(String str) throws Exception
	{
		str = str.trim();
		if ((str==null) || (str.equals(""))) return null;
	
		Integer val;
		Boolean mmDetected = false;
		
		//Remove leading fraction
		if(str.startsWith("1/")){ str = str.substring(2);}
		
		// Remove 'ths'
		if(str.contains("ths")){ str = str.replace("ths", "");}
		
		// Remove 'th'
		if(str.contains("th")){ str = str.replace("th", "");}
		
		// Remove 'mm'
		if(str.contains("mm"))
		{ 
			str = str.replace("mm", "");
			mmDetected = true;
		}
		if(str.contains("millimet"))
		{ 
			str = str.replace("millimetres", "");
			str = str.replace("millimeters", "");
			str = str.replace("millimetre", "");
			str = str.replace("millimeter", "");
			mmDetected = true;
		}
		
		if(str.length()==0 && mmDetected)
		{
			return NormalTridasUnit.MILLIMETRES;
		}
		
		try{
			val = Integer.parseInt(str.trim());
		} catch (NumberFormatException e)
		{
			throw new Exception("Unable to parse units from units string");
		}
		
		switch(val)
		{	
			case 10:   return NormalTridasUnit.TENTH_MM; 
			case 20:   return NormalTridasUnit.TWENTIETH_MM;
			case 50:   return NormalTridasUnit.FIFTIETH_MM;
			case 100:  return NormalTridasUnit.HUNDREDTH_MM; 
			case 1000: return NormalTridasUnit.MICROMETRES; 
		}
		
		throw new Exception("Unable to parse units from units string");
	}
	
	/**
	 * Convert a data value from one unit to another
	 * 
	 * @param inputunits
	 * @param outputunits
	 * @param value
	 * @return
	 */
	public static Double convertDouble(NormalTridasUnit inputunits, NormalTridasUnit outputunits, Double value)
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
		case TWENTIETH_MM:
			val = value / 20000;
			break;
		case FIFTIETH_MM:
			val = value / 50000;
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
		case TWENTIETH_MM:
			val = val * 20000;
			break;
		case FIFTIETH_MM:
			val = val * 50000;
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
	 * Convert a TridasValues tag from one unit to another.  The TridasVales must have 
	 * NormalTridasUnit set otherwise it throws an IncompleteTridasDataException.  The 
	 * values returns are integers limited to maxIntChars in length.  If any values 
	 * are longer it throws an NumberFormatException.
	 * 
	 * @param outputunits
	 * @param tv
	 * @param maxIntChars
	 * @return
	 * @throws NumberFormatException
	 * @throws IncompleteTridasDataException
	 */
	public static TridasValues convertTridasValues(NormalTridasUnit outputunits, TridasValues tv, Integer maxIntChars) 
	throws NumberFormatException, ConversionWarningException
	{
		ArrayList<TridasValue> values = (ArrayList<TridasValue>) tv.getValues();
		NormalTridasUnit inputunits = null;
		
		try{
			 inputunits = tv.getUnit().getNormalTridas();
		} catch (Exception e)
		{
			throw new ConversionWarningException(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.convertsOnlyTridasUnits")));
		}
		
		for (TridasValue value : values)
		{
			Double dblvalue = Double.parseDouble(value.getValue());
			dblvalue = UnitUtils.convertDouble(inputunits, outputunits, dblvalue);
			
			if(String.valueOf(Math.round(dblvalue)).length()>maxIntChars)
			{
				throw new NumberFormatException(I18n.getText("fileio.integerTooLong", maxIntChars.toString()));
			}
			else
			{
				value.setValue(String.valueOf(Math.round(dblvalue)));
			}
		}
		
		return tv;
	}
	
	/**
	 * This function returns the DecimalFormat with the correct number of significant figures when converting
	 * from one unit type to another.
	 * 
	 * @param inputunits
	 * @param outputunits
	 * @return
	 */
	public static DecimalFormat getDecimalFormatForSigFigs(NormalTridasUnit inputunits, NormalTridasUnit outputunits)
	{
		// If either input out output format is null return as integer
		if (inputunits==null || outputunits==null)
		{
			return new DecimalFormat("0");
		}
		
		int sigfigs = 0;
		
		switch(inputunits)
		{
		case METRES:
			break;
		case CENTIMETRES:
			sigfigs = 2;
			break;
		case MILLIMETRES:
			sigfigs = 3;
			break;
		case TENTH_MM:
		case TWENTIETH_MM:
		case FIFTIETH_MM:
			sigfigs = 4;
			break;
		case HUNDREDTH_MM:
			sigfigs = 5;
			break;
		case MICROMETRES:
			sigfigs = 6;
			break;
		default:
		}
		
		switch(outputunits)
		{
		case METRES:
			break;
		case CENTIMETRES:
			sigfigs = sigfigs - 2;
			break;
		case MILLIMETRES:
			sigfigs = sigfigs - 3;
			break;
		case TENTH_MM:
		case TWENTIETH_MM:
		case FIFTIETH_MM:
			sigfigs = sigfigs - 4;
			break;
		case HUNDREDTH_MM:
			sigfigs = sigfigs - 5;
			break;
		case MICROMETRES:
			sigfigs = sigfigs - 6;
			break;
		default:
		}
		
		if(sigfigs>0)
		{
			String format = "0.";
			for (int i=1; i<=sigfigs; i++)
			{
				format = format+"0";
			}
			return new DecimalFormat(format);
		}
		else
		{
			return new DecimalFormat("0");
		}
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
	throws NumberFormatException, ConversionWarningException
	{
		ArrayList<TridasValue> values = (ArrayList<TridasValue>) tv.getValues();
		NormalTridasUnit inputunits = null;
		
		try{
			 inputunits = tv.getUnit().getNormalTridas();
		} catch (Exception e)
		{
			throw new ConversionWarningException(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.convertsOnlyTridasUnits")));
		}
		
		for (TridasValue value : values)
		{
			Double dblvalue = Double.parseDouble(value.getValue());
			dblvalue = UnitUtils.convertDouble(inputunits, outputunits, dblvalue);
			
			if(outputAsIntegers)
			{
				value.setValue(String.valueOf(Math.round(dblvalue)));
			}
			else
			{
				// Make sure only the correct number of significant decimals are shown
				DecimalFormat dformat = getDecimalFormatForSigFigs(inputunits, outputunits);
				value.setValue(String.valueOf(dformat.format(dblvalue)));
			}
		}
		
		return tv;
	}
}
