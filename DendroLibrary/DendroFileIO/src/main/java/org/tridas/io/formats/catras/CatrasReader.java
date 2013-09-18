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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.InvalidDendroFileException.PointerType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASScope;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASFileType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASLastRing;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASProtection;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASSource;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.CATRASVariableType;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.FileHelper;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**
 * @author peterbrewer
 */
public class CatrasReader extends AbstractDendroFileReader {
	
	private static final Logger log = LoggerFactory.getLogger(CatrasReader.class);
	// defaults given by user
	private CatrasToTridasDefaults defaults = null;
	
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	private ArrayList<TridasDerivedSeries> dseriesList = new ArrayList<TridasDerivedSeries>();;
	private ArrayList<Integer> ringWidthValues = new ArrayList<Integer>();
	private ArrayList<Integer> sampleDepthValues = new ArrayList<Integer>();
		
	public CatrasReader() {
		super(CatrasToTridasDefaults.class, new CatrasFormat());
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
	
		// Check there are at least 128 bytes and that the number of bytes is divisible by 128
		if (argFileBytes == null) {
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 
					1, PointerType.BYTE );
		}
		else if (argFileBytes.length < 128) {
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 
					argFileBytes.length, PointerType.BYTE );
		}
		else if (argFileBytes.length % 128!=0)
		{
			throw new InvalidDendroFileException(I18n.getText("catras.invalidFileSize"), 
					argFileBytes.length, PointerType.BYTE );
		}
		
		if(CatrasReader.getIntFromByte(argFileBytes[66])!=1)
		{
			throw new InvalidDendroFileException(I18n.getText("catras.invalidNumberFormat"), 
					67, PointerType.BYTE );
		}
		
	}
	
	/**
	 * @param argFileBytes
	 * @param argDefaultFields
	 */
	@SuppressWarnings("unchecked")
	protected void parseFile(byte[] argFileBytes, IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException {
		
		defaults = (CatrasToTridasDefaults) argDefaultFields;
		log.debug("starting catras file parsing");
		
		checkFile(argFileBytes);
		
		/**  Print debug info */
		//this.debugAsIntSingleByte(0, 128, argFileBytes);
		//this.debugAsIntBytePairs(0, 128, argFileBytes);
		//this.debugAsStringBytePairs(0, 128, argFileBytes);
		
		
		// Series Title - bytes 1-32
		defaults.getStringDefaultValue(DefaultFields.SERIES_NAME)
			.setValue(new String(getSubByteArray(argFileBytes, 0, 31)).trim());
		
		// Series code - bytes 33-40
		defaults.getStringDefaultValue(DefaultFields.SERIES_CODE)
			.setValue(new String(getSubByteArray(argFileBytes, 32, 39)).trim());
		
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
		defaults.getIntegerDefaultValue(DefaultFields.FIRST_VALID_YEAR)
		.setValue(getIntFromBytePair(getSubByteArray(argFileBytes, 48, 49)));
		
		// 51-52 valid end
		defaults.getIntegerDefaultValue(DefaultFields.LAST_VALID_YEAR)
		.setValue(getIntFromBytePair(getSubByteArray(argFileBytes, 50, 51)));		
		
		// 53 1=pith 2=waldkante 3=pith to waldkante
		CATRASScope comp = CATRASScope.fromCode(getIntFromByte(argFileBytes[52]));
		if(comp!=null)
		{
			GenericDefaultValue<CATRASScope> compField = (GenericDefaultValue<CATRASScope>) defaults
				.getDefaultValue(DefaultFields.SCOPE);
			compField.setValue(comp);
		}
		
		// 54 1 = ew only last ring
		CATRASLastRing lastring = CATRASLastRing.fromCode(getIntFromByte(argFileBytes[53]));
		if(lastring!=null)
		{
			GenericDefaultValue<CATRASLastRing> compField = (GenericDefaultValue<CATRASLastRing>) defaults
				.getDefaultValue(DefaultFields.LAST_RING);
			compField.setValue(lastring);
		}
		
		// Start year- bytes 55-56
		Integer startYear = getIntFromBytePairByPos(argFileBytes, 54);
		/*byte[] bp = getBytePairByPos(argFileBytes, 54);
		log.debug("Start year read from CATRAS as Integer = "+startYear);
		Byte byteOne = new Byte(bp[0]);
		Byte byteTwo = new Byte(bp[1]);
		log.debug("Hex value of first byte = "+Integer.toHexString(byteOne.intValue()));
		log.debug("Hex value of second byte = "+Integer.toHexString(byteTwo.intValue()));
		log.debug("Int value of second byte = "+byteOne.intValue());
		log.debug("Int value of second byte = "+byteTwo.intValue());

		log.debug("First byte from pair = "+bp[0]);
		log.debug("Second byte from pair = "+bp[1]);
		log.debug("Byte pair = "+bp);*/
		
		defaults.getSafeIntYearDefaultValue(DefaultFields.START_YEAR)
			.setValue(new SafeIntYear(startYear));
		log.debug("Start year converted to safeintyear = "+new SafeIntYear(startYear).toString());
		
		// Number of characters in series name - byte 57
		defaults.getIntegerDefaultValue(DefaultFields.NUMBER_OF_CHARS_IN_TITLE)
			.setValue(getIntFromByte(argFileBytes[56]));
		
		// Quality code - byte 58
		defaults.getIntegerDefaultValue(DefaultFields.QUALITY_CODE)
			.setValue(getIntFromByte(argFileBytes[57]));	
		
		// Species code - byte 59 - 60  (not much use without associated dictionary file)
		defaults.getIntegerDefaultValue(DefaultFields.SPECIES_CODE)
			.setValue(getIntFromBytePair(getSubByteArray(argFileBytes, 58, 59)));
		
		// 61, 62, 63 creation date- dd, mm, yy respectively
		try{
			Integer day   = getIntFromByte(argFileBytes[60]);
			Integer month = getIntFromByte(argFileBytes[61]);
			Integer year  = getIntFromByte(argFileBytes[62])+1900;
			
			if(year>1900){ defaults.getDateTimeDefaultValue(DefaultFields.CREATION_DATE)
				.setValue(DateUtils.getDateTime(day, month, year));
			}
		} catch (Exception e)
		{
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("catras.creationDateInvalid")));

		}
		
		// 64, 65, 66 - amended date - day, month year 
		try{
			Integer day   = getIntFromByte(argFileBytes[63]);
			Integer month = getIntFromByte(argFileBytes[64]);
			Integer year  = getIntFromByte(argFileBytes[65])+1900;
			
			if(year>1900){ defaults.getDateTimeDefaultValue(DefaultFields.UPDATED_DATE)
				.setValue(DateUtils.getDateTime(day, month, year));
			}
			
		} catch (Exception e)
		{
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("catras.updatedDateInvalid")));
		}
	
		// 67 Real number format 
		// Always 1 = IEEE
		
		// 68 - Series type
		CATRASVariableType vartype = CATRASVariableType.fromCode(getIntFromByte(argFileBytes[67]));
		if(vartype!=null)
		{
			GenericDefaultValue<CATRASVariableType> field = (GenericDefaultValue<CATRASVariableType>) defaults
				.getDefaultValue(DefaultFields.VARIABLE_TYPE);
			field.setValue(vartype);
		}
		
		// 69-81 Not used
		
		// 82 - Source
		CATRASSource source = CATRASSource.fromCode(String.valueOf(getIntFromBytePairByPos(argFileBytes, 81)));
		if(source!=null)
		{
			GenericDefaultValue<CATRASSource> field = (GenericDefaultValue<CATRASSource>) defaults
				.getDefaultValue(DefaultFields.SOURCE);
			field.setValue(source);
		}
		
		// 83 - Protection
		CATRASProtection protection = CATRASProtection.fromCode(getIntFromByte(argFileBytes[82]));
		if(protection!=null)
		{
			GenericDefaultValue<CATRASProtection> field = (GenericDefaultValue<CATRASProtection>) defaults
				.getDefaultValue(DefaultFields.PROTECTION);
			field.setValue(protection);
		}		
		
		// 84 - File type
		CATRASFileType filetype = CATRASFileType.fromCode(getIntFromByte(argFileBytes[83]));
		Boolean isChronology = false;
		if(filetype!=null)
		{
			GenericDefaultValue<CATRASFileType> field = (GenericDefaultValue<CATRASFileType>) defaults
				.getDefaultValue(DefaultFields.FILE_TYPE);
			field.setValue(filetype);
			if(!filetype.equals(CATRASFileType.RAW)) isChronology = true;
		}		
		
		// Userid - bytes 85-88
		defaults.getStringDefaultValue(DefaultFields.USER_ID)
			.setValue(new String(getSubByteArray(argFileBytes, 84, 87)).trim());
		
		// 89-128 Statistics - 
		// Ignored
				
		// Extract the data
		byte[] theData = getSubByteArray(argFileBytes, 127, 127+(length*2));
		//boolean reachedStopMarker = false;
		for (int i = 1; i < theData.length; i = i + 2) {
			int valueFromFile = getIntFromBytePairByPos(theData, i);
			/*if (valueFromFile == 999) {
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
					//log.debug("sample depth as int = " + String.valueOf(getIntFromBytePairByPos(theData, i)));
				}
			}*/
			if (valueFromFile == -1)
			{
				// Ignore.  This is a padding value found in files created with older versions
				// of CATRAS
			}
			else if (valueFromFile < -1)
			{
				// Missing ring value
				ringWidthValues.add(0);
				log.debug("CATRAS negative ring width value is being interpreted as a missing ring");
			}
			else {
				// Handle normal ring width values
				ringWidthValues.add(valueFromFile);
				//log.debug("value = " + String.valueOf(valueFromFile));
			}
		}
		
		if(isChronology)
		{
			int fileBytes = argFileBytes.length;
			int fileSizeDataOnly = CatrasFile.roundToNext128(length*2)+128;
			if(fileBytes == fileSizeDataOnly)
			{
				log.debug("No count info in file");
				
				// Set the sample depth values to 1
				for (int i = 0; i < ringWidthValues.size(); i++) {
					sampleDepthValues.add(1);
				}
			}
			else
			{
				try{
					int firstDataByte = 128;
					int lengthOfDataInBytes = CatrasFile.roundToNext128(length*2);
					int firstCountByte = firstDataByte + lengthOfDataInBytes;
					int lastCountByte = firstCountByte+(length*2)-1;
				    
				    log.debug("Number of rings         = "+length);
				    log.debug("Number of bytes (rings) = "+lengthOfDataInBytes);
				    log.debug("Length of file in bytes = "+argFileBytes.length);
				    log.debug("Remaining size in bytes = "+(argFileBytes.length-firstDataByte-lengthOfDataInBytes));
				    log.debug("Data starts at byte     = "+firstDataByte);
				    log.debug("First count byte is at  = "+firstCountByte);
				    log.debug("Last count byte is at   = "+lastCountByte);
				    
					byte[] theDepths = getSubByteArray(argFileBytes, firstCountByte, lastCountByte);
					
					log.debug("Size of depths array    = "+theDepths.length);
					
					for (int i = 0; i < theDepths.length; i = i + 2) {
						int valueFromFile = getIntFromBytePairByPos(theDepths, i);
						log.debug("Byte 1 ["+i+"]: "+theDepths[i]);
						log.debug("Byte 2 ["+(i+1)+"]: "+theDepths[i+1]);
						log.debug("Value : "+valueFromFile);
						sampleDepthValues.add(valueFromFile);
					}
				} catch (Exception ex)
				{
					log.error("Failed to get count data");
				}
			}
		}
		
		
		// Check length metadata and number of ring width values match
		/*if (ringWidthValues.size() > getIntFromBytePair(getSubByteArray(argFileBytes, 44, 45))) {
			//addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("fileio.valueCountMismatch", ringWidthValues.size()+"", length+"")));
			// Trim off extra ring width values
			ArrayList<Integer> trimmedRingValues = new ArrayList<Integer>();
			trimmedRingValues.ensureCapacity(length);
			
			for (int j=0; j<length; j++)
			{
				trimmedRingValues.add(ringWidthValues.get(j));
			}
			ringWidthValues = trimmedRingValues;
		}*/
		
		
		if (ringWidthValues.size() < length) 
		{
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("fileio.valueCountMismatch", ringWidthValues.size()+"", length+"")));
			log.warn("Less ring width values in file than there should be according to the length metadata ("+
					ringWidthValues.size() + " not " + length+")");
		}
	
		// Check for lead-in and lead-out missing rings.  If present remove
		// them, updating ring count, start and end dates accordingly.
		if(ringWidthValues.get(0).equals(0))
		{
			int numRingsToDelete = 0;
			for(Integer val : ringWidthValues)
			{
				if(val!=0) break;
				numRingsToDelete++;
				ringWidthValues.remove(val);
			}
			
			length = length - numRingsToDelete;
						
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
	
	public static byte[] getSubByteArray(byte[] bytes, int start, int end) {
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
	public static byte[] getBytePairByPos(byte[] bytes, int pos) {
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
	public static int getIntFromBytePair(byte[] wBytes) {
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
	public static int getIntFromBytePair(byte[] wBytes, Boolean littleEndian) {
		
		short lsb = 0; // least significant byte
		short msb = 0; // most significant byte
		int w = -1; // actual value
		// Depending on endian, extract bytes in correct order. Java is always
		// big endian, but Windoze i386 are always (?) little endian. As CATRAS
		// is the only (?) program that writes CATRAS files then they should
		// always be little endian I think!
		if (littleEndian) {
			lsb = (short)((wBytes[0]) & 0xff); 
			msb = (short)((wBytes[1]) & 0xff); 
	
			//lsb = (0x000000FF & (wBytes[0])); // least significant byte
			//msb = (0x000000FF & (wBytes[1])); // most significant byte
			//lsb = ((Byte)wBytes[0]).intValue();
			//msb = ((Byte)wBytes[1]).intValue();
		}
		else {
			lsb = (short)((wBytes[1]) & 0xff); 
			msb = (short)((wBytes[0]) & 0xff); 
			//	lsb = (0x000000FF & (wBytes[1])); // least significant byte
			//	msb = (0x000000FF & (wBytes[0])); // most significant byte
		}
		
		//log.debug("LSB value of "+ wBytes[0] + " = "+String.valueOf(lsb));
		//log.debug("MSB value of "+ wBytes[1] + " = "+String.valueOf(msb));
		
		
		if (msb>128)
		{
			// Large MSB values appear to indicate negative values.  
			// Explaining what this means is too hard, I hope you 
			// can understand from the code!  
			w = (-1-((255-lsb)+(256*(255-msb))));
		}
		else if (msb<128)
		{
			// Small MSB values are for positive numbers
			w = lsb + (256*msb);
		}
				
		return w;
		
	}
	
	/**
	 * Extract a single integer value from a byte
	 * 
	 * @param argBytes
	 * @param littleEndian
	 *            use little-endian?
	 * @return
	 */
	public static int getIntFromByte(byte argByte) {
		
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
			
	
	@SuppressWarnings("unchecked")
	private TridasProject getProject() {
		
		// Create entities
		TridasProject p = defaults.getProjectWithDefaults();
		TridasObject o = defaults.getObjectWithDefaults();
		TridasElement e = defaults.getElementWithDefaults();
		TridasSample s = defaults.getSampleWithDefaults();
		TridasRadius r = defaults.getRadiusWithDefaults(false);
		
		ITridasSeries series;
		
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
		ArrayList<TridasValues> vlist = new ArrayList<TridasValues>();
		vlist.add(valuesGroup);
		
		ArrayList<TridasSample> sList = new ArrayList<TridasSample>();
		sList.add(s);
		e.setSamples(sList);
		
		ArrayList<TridasElement> eList = new ArrayList<TridasElement>();
		eList.add(e);
		o.setElements(eList);
		
		ArrayList<TridasObject> oList = new ArrayList<TridasObject>();
		oList.add(o);		
		p.setObjects(oList);
		
		GenericDefaultValue<CATRASFileType> typeField = (GenericDefaultValue<CATRASFileType>) defaults.getDefaultValue(DefaultFields.FILE_TYPE);
		CATRASFileType typeValue = CATRASFileType.RAW;
		try{
			typeValue = typeField.getValue();
			typeField.getValue();
			
		} catch (Exception e2){}
		
		
		// TODO Esther requested that TREE_CURVE be treated as measurementSeries.  I
		// Is this correct?
		if(   typeValue.equals(CATRASFileType.CHRONOLOGY) 
		   || typeValue.equals(CATRASFileType.TREE_CURVE)
			)
		{
			// Derived Series
			series = defaults.getDerivedSeriesWithDefaults();

			// Link to sample
			SeriesLink link = new SeriesLink();		
			TridasIdentifier id = s.getIdentifier();
			link.setIdentifier(id);
			((TridasDerivedSeries)series).getLinkSeries().getSeries().add(link);

			
			// Compile project
			series.setValues(vlist);
			
			ArrayList<TridasDerivedSeries> seriesList = new ArrayList<TridasDerivedSeries>();
			seriesList.add((TridasDerivedSeries) series);
			p.setDerivedSeries(seriesList);
		
			
		}
		else
		{
			log.debug("CATRAS data type is :" + typeValue);
			
			// Now build up our measurementSeries
			series = defaults.getMeasurementSeriesWithDefaults();

			// Compile project
			series.setValues(vlist);
			
			ArrayList<TridasMeasurementSeries> seriesList = new ArrayList<TridasMeasurementSeries>();
			seriesList.add((TridasMeasurementSeries) series);
			r.setMeasurementSeries(seriesList);
		
			ArrayList<TridasRadius> rList = new ArrayList<TridasRadius>();
			rList.add(r);
			s.setRadiuses(rList);
	
		}

		
		return p;
		
	}
	
	
	// *******************************
	// NOT SUPPORTED - BINARY FORMAT
	// *******************************
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) {
		throw new UnsupportedOperationException(I18n.getText("general.binaryNotText"));
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
	
	public static void debugAsIntSingleByte(int first, int last, byte[] argFileBytes)
	{
		for (int i=first; i<=last; i++)
		{
			byte[] byteArray = getSubByteArray(argFileBytes, i, i+1);
			System.out.println("As Integer - Sing byte " + String.valueOf(i)+": "+ String.valueOf(getIntFromByte(byteArray[0])));
			log.debug("As Integer - Sing byte " + String.valueOf(i)+": "+ String.valueOf(getIntFromByte(byteArray[0])));

		}
	}
	
	public static void debugAsIntBytePairs(int first, int last, byte[] argFileBytes)
	{
		for (int i=first; i<=last; i++)
		{
			byte[] byteArray = getSubByteArray(argFileBytes, i, i+1);
			log.debug("As Integer - Byte pair " + String.valueOf(i)+": "+ String.valueOf(getIntFromBytePair(byteArray)));

		}
	}
	
	@SuppressWarnings("unused")
	private void debugAsStringBytePairs(int first, int last, byte[] argFileBytes)
	{
		for (int i=first; i<=last; i++)
		{
			byte[] byteArray = getSubByteArray(argFileBytes, i, i+1);
			log.debug("As String  - Byte pair " + String.valueOf(i)+": "+ new String(byteArray));	

		}
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"cat"};
		
		return new DendroFileFilter(exts, getShortName());

	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		TridasProject projects[] = new TridasProject[1];
		projects[0] = this.getProject();
		return projects;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getTridasContainer()
	 */
	public TridasTridas getTridasContainer() {
		TridasTridas container = new TridasTridas();
		List<TridasProject> list = Arrays.asList(getProjects());
		container.setProjects(list);
		return container;
	}
}
