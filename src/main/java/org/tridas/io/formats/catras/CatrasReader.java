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
package org.tridas.io.formats.catras;

import java.io.IOException;
import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.FileHelper;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**
 * @author peterbrewer
 */
public class CatrasReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(CatrasReader.class);
	// defaults given by user
	private CatrasToTridasDefaults defaults = null;
	
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	private ArrayList<TridasDerivedSeries> dseriesList = new ArrayList<TridasDerivedSeries>();;
	private ArrayList<Integer> ringWidthValues = new ArrayList<Integer>();
	private ArrayList<Integer> sampleDepthValues = new ArrayList<Integer>();
		
	public CatrasReader() {
		super(CatrasToTridasDefaults.class);
	}
	
	/**
	 * @throws IncorrectDefaultFieldsException
	 * @throws InvalidDendroFileException
	 * @see org.tridas.io.IDendroCollectionWriter#loadFile(java.lang.String)
	 */
	@Override
	public void loadFile(String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		FileHelper fileHelper = new FileHelper();
		log.debug("loading file from: " + argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		if (bytes == null) {
			throw new IOException(I18n.getText("fileio.loadfailed"));
		}
		loadFile(bytes, argDefaultFields);
	}
	
	@Override
	public void loadFile(String argPath, String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		FileHelper fileHelper = new FileHelper(argPath);
		log.debug("loading file from: " + argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		if (bytes == null) {
			throw new IOException(I18n.getText("fileio.loadfailed"));
		}
		loadFile(bytes, argDefaultFields);
	}
	
	public void loadFile(byte[] argFileBytes, IMetadataFieldSet argDefaults) throws IncorrectDefaultFieldsException,
			InvalidDendroFileException {
		if (!argDefaults.getClass().equals(getDefaultFieldsClass())) {
			throw new IncorrectDefaultFieldsException(getDefaultFieldsClass());
		}
		parseFile(argFileBytes, argDefaults);
	}
	
	public void loadFile(byte[] argFileBytes) throws InvalidDendroFileException {
		parseFile(argFileBytes, constructDefaultMetadata());
	}
	
	/**
	 * Check this is a valid CATRAS file
	 * 
	 * @param argFileBytes
	 * @throws InvalidDendroFileException
	 */
	protected void checkFile(byte[] argFileBytes) throws InvalidDendroFileException{
	
		// Check there are at least 128 bytes
		if (argFileBytes == null) {
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 1);
		}
		else if (argFileBytes.length < 128) {
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 1);
		}
	}
	
	/**
	 * @param argFileBytes
	 * @param argDefaultFields
	 */
	protected void parseFile(byte[] argFileBytes, IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException {
		
		defaults = (CatrasToTridasDefaults) argDefaultFields;
		log.debug("starting catras file parsing");
		
		checkFile(argFileBytes);
		
		
		this.debugAsIntSingleByte(0, 128, argFileBytes);
		this.debugAsIntBytePairs(0, 128, argFileBytes);
		this.debugAsStringBytePairs(0, 128, argFileBytes);
		
		// Series Title - bytes 1-32
		defaults.getStringDefaultValue(DefaultFields.SERIES_NAME)
			.setValue(new String(getSubByteArray(argFileBytes, 0, 31)));
		
		// Series code - bytes 33-40
		defaults.getStringDefaultValue(DefaultFields.SERIES_CODE)
			.setValue(new String(getSubByteArray(argFileBytes, 32, 39)));
		
		// File extension - bytes 41-44
		defaults.getStringDefaultValue(DefaultFields.FILE_EXTENSION)
			.setValue(new String(getSubByteArray(argFileBytes, 40, 43)));
		
		// Length of series - bytes 45-46
		defaults.getIntegerDefaultValue(DefaultFields.SERIES_LENGTH)
			.setValue(getIntFromBytePair(getSubByteArray(argFileBytes, 44, 45)));
		int length = getIntFromBytePair(getSubByteArray(argFileBytes, 44, 45));
		
		// Length of sapwood - bytes 47-48
		defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_LENGTH)
			.setValue(getIntFromBytePair(getSubByteArray(argFileBytes, 46, 47)));
		
		// 49-50 valid start
		// 51-52 valid end
		// 53 1=pith 2=waldkante 3=pith to waldkante
		// 54 1 = ew only last ring
		
		// Start year- bytes 55-56
		defaults.getSafeIntYearDefaultValue(DefaultFields.START_YEAR)
			.setValue(new SafeIntYear(String.valueOf(getIntFromBytePairByPos(argFileBytes, 54)), true));
		
		// 57-58 Unknown
		
		// Species code - byte 54  **** Not convinced this is the correct byte - not sure how to check
		//defaults.getStringDefaultValue(DefaultFields.SPECIES_CODE)
		//	.setValue(String.valueOf(getIntFromBytePairByPos(argFileBytes, 53)));
		
		// 61, 62, 63 creation date- dd, mm, yy respectively
		try{
			Integer day   = getIntFromByte(argFileBytes[60], true);
			Integer month = getIntFromByte(argFileBytes[61], true);
			Integer year  = getIntFromByte(argFileBytes[62], true);
			
			// Year is only two digit style so if after 70 presume 19xx
			// Obviously this will break if someone is still using CATRAS
			// in 2070 but then they deserve it! ;-) 
			if(year>70) {
				year = year+1900;
			} else {
				year = year+2000;
			}
			
			defaults.getDateTimeDefaultValue(DefaultFields.CREATION_DATE)
				.setValue(DateUtils.getDateTime(day, month, year));
		} catch (Exception e)
		{
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("catras.creationDateInvalid")));

		}
		
		// 64, 65, 66 - amended date - day, month year 
		try{
			Integer day   = getIntFromByte(argFileBytes[63], true);
			Integer month = getIntFromByte(argFileBytes[64], true);
			Integer year  = getIntFromByte(argFileBytes[65], true);
			
			// Year is only two digit style so if after 70 presume 19xx
			// Obviously this will break if someone is still using CATRAS
			// in 2070 but then they deserve it! ;-) 
			if(year>70) {
				year = year+1900;
			} else {
				year = year+2000;
			}
			
			defaults.getDateTimeDefaultValue(DefaultFields.UPDATED_DATE)
				.setValue(DateUtils.getDateTime(day, month, year));
		} catch (Exception e)
		{
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("catras.updatedDateInvalid")));

		}
	
		
		// String sapwood = new String(getSubByteArray(argFileBytes, 66, 67)); //67
		// 67-68 1=valid stats
		// 69-83 Unknown
		// 84 0=raw 1=treecurve 2=chronology
		// String dated = new String(getSubByteArray(argFileBytes, 68, 74)); //69-75
		
		// Userid - bytes 85-86
		defaults.getStringDefaultValue(DefaultFields.USER_ID)
			.setValue(new String(getSubByteArray(argFileBytes, 84, 87)).trim());
		
		// 89-92 Float av width
		// 93-95 Float std dev
		// 96-100 Foat autocorr
		// 101-104 Float sens
		// 105-128 Unknown
				
		// Extract the data
		byte[] theData = getSubByteArray(argFileBytes, 127, argFileBytes.length - 1);
		boolean reachedStopMarker = false;
		for (int i = 1; i < theData.length; i = i + 2) {
			int valueFromFile = getIntFromBytePairByPos(theData, i);
			if (valueFromFile == 999) {
				// Stop marker found
				// There are 32 bytes (inclusive) after the data and before
				// possible sample depth values, but we have
				// no idea what they mean so skip them and continue;
				i = i + 42;
				reachedStopMarker = true;
				continue;
			}
			else if (reachedStopMarker) {
				// Handle count values if present
				if (valueFromFile == 0) {
					log.debug("reached end of count values");
					break;
				}
				else if (valueFromFile == -1)
				{
					// Ignore.  This is a padding value found in files created with older versions
					// of CATRAS
				}
				else {
					sampleDepthValues.add(valueFromFile);
					log.debug("sample depth as int = " + String.valueOf(getIntFromBytePairByPos(theData, i)));
				}
			}
			else if (valueFromFile == -1)
			{
				// Ignore.  This is a padding value found in files created with older versions
				// of CATRAS
			}
			else {
				// Handle normal ring width values
				ringWidthValues.add(valueFromFile);
				log.debug("value = " + String.valueOf(valueFromFile));
			}
		}
		
		// Check length metadata and number of ring width values match
		if (ringWidthValues.size() != getIntFromBytePair(getSubByteArray(argFileBytes, 44, 45))) {
			//addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("fileio.valueCountMismatch", ringWidthValues.size()+"", length+"")));
			// Trim off extra ring width values
			ArrayList<Integer> trimmedRingValues = new ArrayList<Integer>();
			trimmedRingValues.ensureCapacity(length);
			
			for (int j=0; j<length; j++)
			{
				trimmedRingValues.add(ringWidthValues.get(j));
			}
			ringWidthValues = trimmedRingValues;
		}
		
		// Check sample depth values count is valid
		if (sampleDepthValues.size() > 0) {
			if (sampleDepthValues.size() != ringWidthValues.size()) {
				// TODO All sample depths seem to be shorter than ring width depths.
				// FIX!
				
				/**
				 * throw new InvalidDendroFileException(I18n.getText(
				 * "fileio.countsAndValuesDontMatch",
				 * new String [] {String.valueOf(ringWidthValues.size()),
				 * String.valueOf(sampleDepthValues.size())}),
				 * 129, PointerType.BYTE);
				 */
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("fileio.countsAndValuesDontMatch",
						new String[]{String.valueOf(ringWidthValues.size()), String.valueOf(sampleDepthValues.size())})));
				sampleDepthValues.clear();
			}
		}
		
		// Set end date 
		if (defaults.getSafeIntYearDefaultValue(DefaultFields.START_YEAR).getValue()!=null)
		{
			if(defaults.getIntegerDefaultValue(DefaultFields.SERIES_LENGTH).getValue()!=null)
			{
				defaults.getSafeIntYearDefaultValue(DefaultFields.END_YEAR).setValue(
						defaults.getSafeIntYearDefaultValue(DefaultFields.START_YEAR).getValue()
							.add(defaults.getIntegerDefaultValue(DefaultFields.SERIES_LENGTH).getValue()-1));				
			}
		}
		

		
	}
	
	private byte[] getSubByteArray(byte[] bytes, int start, int end) {
		end++;
		if (start > end) {
			return null;
		}
		
		byte[] outarr = new byte[end - start];
		
		int i = start;
		int i2 = 0;
		for (; i < end; i++) {
			outarr[i2] = bytes[i];
			i2++;
		}
		
		return outarr;
	}
	
	/**
	 * Extract a byte pair from a larger byte array by specifying the position
	 * of the first byte. Assumes bytes are little-endian.
	 * 
	 * @param bytes
	 * @param pos
	 * @return
	 */
	private byte[] getBytePairByPos(byte[] bytes, int pos) {
		byte wBytes[] = new byte[2];
		
		if (pos < 0) {
			return null;
		}
		try {
			
			wBytes[0] = bytes[pos];
			wBytes[1] = bytes[pos + 1];
		} catch (ArrayIndexOutOfBoundsException e) {

		}
		
		return wBytes;
	}
	
	/**
	 * Wrapper for getIntFromBytePair() with default little-endian
	 * 
	 * @param wBytes
	 * @return
	 */
	private int getIntFromBytePair(byte[] wBytes) {
		return getIntFromBytePair(wBytes, true);
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
	private int getIntFromBytePair(byte[] wBytes, Boolean littleEndian) {
		
		int lsb; // least significant byte
		int msb; // most significant byte
		int w = -1; // actual value
		
		// Depending on endian, extract bytes in correct order. Java is always
		// big endian, but Windoze i386 are always (?) little endian. As CATRAS
		// is the only (?) program that writes CATRAS files then they should
		// always be little endian I think!
		if (littleEndian) {
			lsb = (0x000000FF & (wBytes[0])); // least significant byte
			msb = (0x000000FF & (wBytes[1])); // most significant byte
		}
		else {
			lsb = (0x000000FF & (wBytes[1])); // least significant byte
			msb = (0x000000FF & (wBytes[0])); // most significant byte
		}
		
		// log.debug("LSB = "+String.valueOf(lsb)+"  MSB = "+String.valueOf(lsb));
		

		if (msb>128)
		{
			// Large MSB values appear to indicate negative values.  
			// Explaining what this means is too hard, I hope you 
			// can understand from the code!  
			w = 0-((255-lsb)+(256*(255-msb)));
		}
		else if (msb<128)
		{
			// Small MSB values are for positive numbers
			w = lsb + (256*msb);
		}
				
		return w;
		
	}
	
	/**
	 * Extract the integer value from a byte according to endianess
	 * 
	 * @param argBytes
	 * @param littleEndian
	 *            use little-endian?
	 * @return
	 */
	private int getIntFromByte(byte argByte, Boolean littleEndian) {
		
		return (0x000000FF & (argByte));
		
	}
	
	
	/**
	 * Extract a byte pair from a large byte array according to the position
	 * value of the first byte. Then extract the integer value assuming
	 * data is little-endian.
	 * 
	 * @param wBytes
	 * @param pos
	 * @return
	 */
	private int getIntFromBytePairByPos(byte[] wBytes, int pos) {
		return getIntFromBytePair(getBytePairByPos(wBytes, pos));
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"cat"};
	}
	
	@Override
	public TridasProject getProject() {
		
		// Create entities
		TridasProject p = defaults.getProjectWithDefaults();
		TridasObject o = defaults.getObjectWithDefaults();
		TridasElement e = defaults.getElementWithDefaults();
		TridasSample s = defaults.getSampleWithDefaults();
		TridasRadius r = defaults.getRadiusWithDefaults(false);
				
		// Now build up our measurementSeries
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		
		// Compile TridasValues array
		ArrayList<TridasValue> tridasValues = new ArrayList<TridasValue>();
		for (int i = 0; i < ringWidthValues.size(); i++) {
			TridasValue v = new TridasValue();
			v.setValue(String.valueOf(ringWidthValues.get(i)));
			if (sampleDepthValues.size() > 0) {
				v.setCount(sampleDepthValues.get(i));
			}
			tridasValues.add(v);
		}
		TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
		valuesGroup.setValues(tridasValues);

		// Compile project
		ArrayList<TridasValues> vlist = new ArrayList<TridasValues>();
		vlist.add(valuesGroup);
		series.setValues(vlist);
		
		ArrayList<TridasMeasurementSeries> seriesList = new ArrayList<TridasMeasurementSeries>();
		seriesList.add(series);
		r.setMeasurementSeries(seriesList);
	
		ArrayList<TridasRadius> rList = new ArrayList<TridasRadius>();
		rList.add(r);
		s.setRadiuses(rList);
		
		ArrayList<TridasSample> sList = new ArrayList<TridasSample>();
		sList.add(s);
		e.setSamples(sList);
		
		ArrayList<TridasElement> eList = new ArrayList<TridasElement>();
		eList.add(e);
		o.setElements(eList);
		
		ArrayList<TridasObject> oList = new ArrayList<TridasObject>();
		oList.add(o);		
		p.setObjects(oList);
		
		return p;
		
	}
	
	private char[] byteArr2CharArr(byte[] byteArr) {
		
		char[] charArr = new char[byteArr.length];
		String str = new String(byteArr);
		charArr = str.toCharArray();
		
		return charArr;
	}
	
	// *******************************
	// NOT SUPPORTED - BINARY FORMAT
	// *******************************
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) {
		throw new UnsupportedOperationException("Binary file type, cannot load from strings");
	}
	
	@Override
	public void loadFile(String[] argFileStrings) throws InvalidDendroFileException {
		throw new UnsupportedOperationException("Binary file type, cannot load from strings");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getCurrentLineNumber()
	 */
	@Override
	public int getCurrentLineNumber() {
		// TODO track this
		return 0;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("catras.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("catras.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("catras.about.shortName");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		defaults = null;
		dseriesList.clear();
		mseriesList.clear();
		ringWidthValues.clear();
		sampleDepthValues.clear();
	}
	
	
	private void debugAsIntSingleByte(int first, int last, byte[] argFileBytes)
	{
		for (int i=first; i<=last; i++)
		{
			byte[] byteArray = getSubByteArray(argFileBytes, i, i+1);
			log.debug("As Integer - Sing byte " + String.valueOf(i)+": "+ String.valueOf(getIntFromByte(byteArray[0], true)));

		}
	}
	
	private void debugAsIntBytePairs(int first, int last, byte[] argFileBytes)
	{
		for (int i=first; i<=last; i++)
		{
			byte[] byteArray = getSubByteArray(argFileBytes, i, i+1);
			log.debug("As Integer - Byte pair " + String.valueOf(i)+": "+ String.valueOf(getIntFromBytePair(byteArray)));

		}
	}
	
	private void debugAsStringBytePairs(int first, int last, byte[] argFileBytes)
	{
		for (int i=first; i<=last; i++)
		{
			byte[] byteArray = getSubByteArray(argFileBytes, i, i+1);
			log.debug("As String  - Byte pair " + String.valueOf(i)+": "+ new String(byteArray));	

		}
	}
	
}
