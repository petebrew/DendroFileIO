/*******************************************************************************
 * Copyright 2011 Daniel Murphy and Peter Brewer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.tridas.io.formats.catras;

import java.io.IOException;
import java.io.OutputStream;

import jxl.write.WriteException;

import org.apache.poi.util.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASLastRing;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASProtection;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASScope;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASFileType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASSource;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASVariableType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.schema.DateTime;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class CatrasFile implements IDendroFile {

	private static final Logger log = LoggerFactory.getLogger(CatrasFile.class);

	private TridasToCatrasDefaults defaults;
	private ITridasSeries series;
	private TridasValues dataValues;
	
	public CatrasFile (TridasToCatrasDefaults defaults)
	{
		this.defaults = defaults;
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getExtension() {
		return "CAT";
	}

	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		return new ITridasSeries[]{series};
	}
		
	/**
	 * Set the series that this file represents
	 * @param ser
	 */
	public void setSeries(ITridasSeries ser)
	{
		series = ser;
	}
	
	
	/**
	 * 
	 * @param vals
	 */
	public void setDataValues(TridasValues vals)
	{	
		dataValues = vals;
	}
	
	private byte[] getDataAsByteArray() throws IOException
	{
		byte[] dataArr = new byte[getSizeOfFileInBytes()-128];
		int i = 0;
		for(TridasValue val : dataValues.getValues())
		{
			String intval = val.getValue();
			try{
				byte[] bytePair = CatrasFile.getIntAsBytePair(Integer.parseInt(intval), true);
				insertIntoArray(dataArr, bytePair, i, i+1);
				i = i+2;
			} catch (NumberFormatException e)
			{
				throw new ImpossibleConversionException("Data value is beyond range that can be held by CATRAS format");
			}

		}
		
		return dataArr;
	}
	
	private byte[] insertIntoArray(byte[] arr, byte[] valToInsert, Integer startIndex, 
			Integer expectedEndInd) throws IOException
	{
		if(valToInsert == null || valToInsert.length==0)
		{
			return arr;
		}
		
		if(expectedEndInd!=null)
		{
			if(expectedEndInd-startIndex!=valToInsert.length-1)
			{
				throw new IOException("Array wrong size.  Expecting it to be "+(expectedEndInd-startIndex)+" but it was actually "+(valToInsert.length-1));
			}
		}
		
		if(valToInsert.length+startIndex > arr.length)
		{
			throw new IOException("Array out of bounds exception");
		}
			
		
		int j = startIndex;
		for(int i = 0; i <valToInsert.length; i++)
		{
			byte b = valToInsert[i];
			arr[j] = b;
			j++;			             
		}
				
		return arr;
	}
	
	@SuppressWarnings("unchecked")
	public byte[] getFileAsBytes() throws IOException, ImpossibleConversionException{
	
		byte[] file = new byte[getSizeOfFileInBytes()];
		
		// Header
		insertIntoArray(file, 
			defaults.getStringDefaultValue(DefaultFields.SERIES_NAME).getValue().getBytes(),
			0, 31);
		insertIntoArray(file,
			defaults.getStringDefaultValue(DefaultFields.SERIES_CODE).getValue().toUpperCase().getBytes(),
			32, 39);
		insertIntoArray(file,
			defaults.getStringDefaultValue(DefaultFields.FILE_EXTENSION).getValue().getBytes(),
			40, 43);
		insertIntoArray(file,
			CatrasFile.getIntAsBytePair(defaults.getIntegerDefaultValue(DefaultFields.SERIES_LENGTH).getValue()),
			44, 45);
		insertIntoArray(file,
			CatrasFile.getIntAsBytePair(defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_LENGTH).getValue()),
			46, 47);
		insertIntoArray(file,
			CatrasFile.getIntAsBytePair(defaults.getIntegerDefaultValue(DefaultFields.FIRST_VALID_YEAR).getValue()),
			48, 49);
		insertIntoArray(file,
			CatrasFile.getIntAsBytePair(defaults.getIntegerDefaultValue(DefaultFields.LAST_VALID_YEAR).getValue()),
			50, 51);	
		insertIntoArray(file, 
			CatrasFile.getIntAsByteArray(((GenericDefaultValue<CATRASScope>) defaults.getDefaultValue(DefaultFields.SCOPE)).getValue().toCode()),
			52, 52);
		insertIntoArray(file, 
			CatrasFile.getIntAsByteArray(((GenericDefaultValue<CATRASLastRing>) defaults.getDefaultValue(DefaultFields.LAST_RING)).getValue().toCode()),
			53, 53);
		Integer startyear = defaults.getIntegerDefaultValue(DefaultFields.START_YEAR).getValue();
		log.debug("Writing year to CATRAS file as "+startyear);
		insertIntoArray(file,
			CatrasFile.getIntAsBytePair(startyear),
			54, 55);
		insertIntoArray(file,				
			CatrasFile.getIntAsByteArray(defaults.getIntegerDefaultValue(DefaultFields.NUMBER_OF_CHARS_IN_TITLE).getValue()),
			56, 56);
		insertIntoArray(file,				
			CatrasFile.getIntAsByteArray(defaults.getIntegerDefaultValue(DefaultFields.QUALITY_CODE).getValue()),
			57, 57);
		insertIntoArray(file,
			CatrasFile.getIntAsBytePair(defaults.getIntegerDefaultValue(DefaultFields.SPECIES_CODE).getValue()),
			58, 59);
		insertIntoArray(file,
			getDateTimeAsBytes(defaults.getDateTimeDefaultValue(DefaultFields.CREATION_DATE).getValue()),
			60, 62);
		insertIntoArray(file,
			getDateTimeAsBytes(defaults.getDateTimeDefaultValue(DefaultFields.UPDATED_DATE).getValue()),
			63, 65);		
		insertIntoArray(file, 
			CatrasFile.getIntAsByteArray(defaults.getIntegerDefaultValue(DefaultFields.NUMBER_FORMAT).getValue()),
			66, 66);
		insertIntoArray(file, 
			CatrasFile.getIntAsByteArray(((GenericDefaultValue<CATRASVariableType>) defaults.getDefaultValue(DefaultFields.VARIABLE_TYPE)).getValue().toCode()),
			67, 67);		
		insertIntoArray(file,
			((GenericDefaultValue<CATRASSource>) defaults.getDefaultValue(DefaultFields.SOURCE)).getValue().toCode().getBytes(),
			81, 81);
		insertIntoArray(file, 
			CatrasFile.getIntAsByteArray(((GenericDefaultValue<CATRASProtection>) defaults.getDefaultValue(DefaultFields.PROTECTION)).getValue().toCode()),
			82, 82);
		insertIntoArray(file, 
			CatrasFile.getIntAsByteArray(((GenericDefaultValue<CATRASFileType>) defaults.getDefaultValue(DefaultFields.FILE_TYPE)).getValue().toCode()),
			83, 83);	
		insertIntoArray(file, 
			defaults.getStringDefaultValue(DefaultFields.USER_ID).getValue().getBytes(),
			84, 87);
		
		// Data
		insertIntoArray(file, getDataAsByteArray(), 128, 128+getDataAsByteArray().length-1);
			


		return file;

		
	}
	
	
	private byte[] getDateTimeAsBytes(DateTime dt)
	{
		if(dt==null) return null;
		int day = dt.getValue().getDay();
		int month = dt.getValue().getMonth();
		int year = dt.getValue().getYear()-1900;
		
		byte[] rawBytes = new byte[3];
		rawBytes[0] = getIntAsByte(day);
		rawBytes[1] = getIntAsByte(month);
		rawBytes[2] = getIntAsByte(year);
		
		return rawBytes;
		
	}
	
	/**
	 * Takes an integer and rounds it to the nearest multiple of 128
	 * 
	 * @param ringcount
	 * @return
	 */
	public static Integer roundToNext128(Integer ringcount)
	{
		int i = 128;
				
		Integer datasize = ((ringcount / i) * i);
		
		if(ringcount % i >0)
		{
			datasize = datasize + i;
		}
		
		return datasize;
	}
	
	
	@SuppressWarnings("unchecked")
	private int getSizeOfFileInBytes()
	{
						
		Integer datasize = roundToNext128(dataValues.getValues().size()*2);
		
		GenericDefaultValue<CATRASFileType> fileTypeField = (GenericDefaultValue<CATRASFileType>) defaults
		.getDefaultValue(DefaultFields.FILE_TYPE);
		CATRASFileType fileType = fileTypeField.getValue();
		
		if (!fileType.equals(CATRASFileType.RAW))
		{
			datasize = datasize * 3;
		}
				

		
		return datasize+128;
	}
	
	public static byte[] getIntAsBytePair(Integer value) {
		return CatrasFile.getIntAsBytePair(value, true);
	}
	
	/**
	 * Extract the integer value from a byte pair according to endianess
	 * Horror! Java byte is signed!
	 * See: http://www.darksleep.com/player/JavaAndUnsignedTypes.html
	 * 
	 * @param value        - Integer value to convert (-32511 to 32767)
	 * @param littleEndian - use little-endian?
	 * @return
	 */
	public static byte[] getIntAsBytePair(Integer value, Boolean littleEndian) {
		
        int index = -1;
        byte[] rawBytes = new byte[2];
        
		if(value==null)
		{
			return rawBytes;
		}
		
		if(value>32767 || value<-32511)
		{
			throw new NumberFormatException("Number out of range");
		}
		
        if (littleEndian) {
            rawBytes[++index] = (byte) ((value & 0x000000FFL));
            rawBytes[++index] = (byte) ((value & 0x0000FF00L) >> 8);
        } else {
            rawBytes[++index] = (byte) ((value & 0x0000FF00L) >> 8);
            rawBytes[++index] = (byte) ((value & 0x000000FFL));
        }
        
        return rawBytes;
		
	}
	
	public static byte[] getIntAsByteArray(Integer value)
	{
		byte[] rawBytes = new byte[1];
		
		if(value==null)
		{
			return rawBytes;
		}
		
		rawBytes[0] = getIntAsByte(value);
		return rawBytes;
	}
	
	public static byte getIntAsByte(Integer value)
	{
		
		if(value > 255 || value <0)
		{
			throw new NumberFormatException("Unable to represent integer as byte.  Value "+value+" not within range (0-255)");
		}
		value.byteValue();
		return (byte) ((value & 0x000000FFL));
	}
	

	/**
	 * An alternative to the normal saveToString() as this is a binary format
	 * 
	 * @param os
	 * @throws IOException
	 * @throws WriteException
	 */
	public void saveToDisk(OutputStream os) throws IOException, WriteException {

		os.write(getFileAsBytes());
		os.close();
	}
	
	@Override
	public String[] saveToString() {

		throw new UnsupportedOperationException(I18n.getText("fileio.binaryAsStringUnsupported"));
		
	}

}
