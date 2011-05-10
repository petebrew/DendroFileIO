package org.tridas.io.formats.catras;

import java.io.IOException;
import java.io.OutputStream;

import jxl.write.WriteException;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASLastRing;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASScope;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASFileType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class CatrasFile implements IDendroFile {

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
		return "cat";
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
	
	private byte[] getDataAsArray() throws IOException
	{
		byte[] dataArr = new byte[getSizeOfFileInBytes()-128];
		int i = 0;
		for(TridasValue val : dataValues.getValues())
		{
			insertIntoArray(dataArr, CatrasFile.getIntAsBytePair(Integer.parseInt(val.getValue()), true), i, null);
			i = i+2;
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
	public byte[] getFileAsBytes() throws IOException{
	
		byte[] file = new byte[getSizeOfFileInBytes()];
		
		// Header
		insertIntoArray(file, 
			defaults.getStringDefaultValue(DefaultFields.SERIES_NAME).getValue().getBytes(),
			0, 31);
		insertIntoArray(file,
			defaults.getStringDefaultValue(DefaultFields.SERIES_CODE).getValue().getBytes(),
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
			defaults.getDefaultValue(DefaultFields.FIRST_VALID_YEAR).getStringValue().getBytes(),
			48, 49);
		insertIntoArray(file,
			defaults.getDefaultValue(DefaultFields.LAST_VALID_YEAR).getStringValue().getBytes(),
			50, 51);	
		insertIntoArray(file, 
			((GenericDefaultValue<CATRASScope>) defaults.getDefaultValue(DefaultFields.SCOPE)).getValue().toCode().toString().getBytes(),
			52, 52);
		insertIntoArray(file, 
			((GenericDefaultValue<CATRASLastRing>) defaults.getDefaultValue(DefaultFields.LAST_RING)).getValue().toCode().toString().getBytes(),
			53, 53);
		
		
		// Data
		insertIntoArray(file,
				this.getDataAsArray(),
				127, null);
			


		return file;

		
	}
	
	@SuppressWarnings("unchecked")
	private int getSizeOfFileInBytes()
	{
		
		int i = 128;
		
		Integer ringcount = dataValues.getValues().size();
				
		Integer datasize = ((ringcount / i) * i);
		
		if(ringcount % i >0)
		{
			datasize = datasize + i;
		}
		
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
		

        
        if(value<=0)
        {
        	value--;
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
