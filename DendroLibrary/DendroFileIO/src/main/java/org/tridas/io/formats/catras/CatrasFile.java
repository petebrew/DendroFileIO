package org.tridas.io.formats.catras;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import jxl.write.WriteException;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.schema.TridasGenericField;
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
		byte[] dataArr = new byte[128];
		int i = 0;
		for(TridasValue val : dataValues.getValues())
		{
			insertIntoArray(dataArr, this.getIntAsBytePair(Integer.parseInt(val.getValue()), true), i);
			i = i+2;
		}
		
		return dataArr;
	}
	
	private byte[] insertIntoArray(byte[] arr, byte[] valToInsert, Integer startval) throws IOException
	{
		if(valToInsert == null)
		{
			return arr;
		}
		
		if(valToInsert.length+startval > arr.length)
		{
			throw new IOException("Array out of bounds exception");
		}
			
		
		int j = startval;
		for(int i = 0; i <valToInsert.length; i++)
		{
			byte b = valToInsert[i];
			arr[j] = b;
			j++;			             
		}
				
		return arr;
	}
	
	public byte[] getFileAsBytes() throws IOException{
	
		byte[] file = new byte[256];
				
		insertIntoArray(file, 
			defaults.getStringDefaultValue(DefaultFields.SERIES_NAME).getValue().getBytes(),
			0);
		insertIntoArray(file,
			defaults.getStringDefaultValue(DefaultFields.SERIES_CODE).getValue().getBytes(),
			32);
		insertIntoArray(file,
				this.getDataAsArray(),
				127);
			


		return file;

		
	}
	
	/**
	 * Extract the integer value from a byte pair according to endianess
	 * Horror! Java byte is signed!
	 * See: http://www.darksleep.com/player/JavaAndUnsignedTypes.html
	 * 
	 * @param wBytes
	 * @param littleEndian
	 *            use little-endian?
	 * @return
	 */
	private byte[] getIntAsBytePair(int value, Boolean littleEndian) {
		return null;
	/*			
        short unsignedByte = 0;
        long unsignedInt = 0;
        int index = -1;
        byte[] rawBytes = new byte[2];
        
        unsignedByte = (short) (12 & 0XFF);
        rawBytes[++index] = (byte) ((unsignedByte & 0x00FF));
        
        unsignedInt = (long) (1234 & 0xFFFFFFFFL);
        if (littleEndian) {
            rawBytes[++index] = (byte) ((unsignedInt & 0x000000FFL));
            rawBytes[++index] = (byte) ((unsignedInt & 0x0000FF00L) >> 8);
            rawBytes[++index] = (byte) ((unsignedInt & 0x00FF0000L) >> 16);
            rawBytes[++index] = (byte) ((unsignedInt & 0xFF000000L) >> 24);
        } else {
            rawBytes[++index] = (byte) ((unsignedInt & 0xFF000000L) >> 24);
            rawBytes[++index] = (byte) ((unsignedInt & 0x00FF0000L) >> 16);
            rawBytes[++index] = (byte) ((unsignedInt & 0x0000FF00L) >> 8);
            rawBytes[++index] = (byte) ((unsignedInt & 0x000000FFL));
        }
        
        return rawBytes;
		*/
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
