package org.tridas.io.formats.catras;

import java.io.IOException;
import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.FileHelper;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

/**
 * Reader for the CATRAS file format. This is a binary format for software written
 * by R. Aniol released in 1983. 
 * 
 * Several versions of CATRAS were released over the years, the most recent appears
 * to be 
 * 
 * There are no specifications published for the
 * format. This code is based on Matlab, Fortran and C code of Ronald Visser, Henri
 * Grissino-Mayer and Ian Tyers.
 * The following bytes are unaccounted for and are therefore likely to contain data
 * that we are ignoring:
 * 57-58
 * 69-82
 * 105-128
 * 
 * @author peterbrewer
 */
public class CatrasReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(CatrasReader.class);
	// defaults given by user
	private CatrasToTridasDefaults defaults = null;
	
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	private ArrayList<TridasDerivedSeries> dseriesList = new ArrayList<TridasDerivedSeries>();;
	
	int speciesCode;
	
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
	 * @param argFileBytes
	 * @param argDefaultFields
	 */
	protected void parseFile(byte[] argFileBytes, IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException {
		
		defaults = (CatrasToTridasDefaults) argDefaultFields;
		log.debug("starting catras file parsing");
		int index = 0;
		
		// Check there are at least 128 bytes
		if (argFileBytes == null) {
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 1);
		}
		else if (argFileBytes.length < 128) {
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 1);
		}
		
		// Extract basic metadata
		String headertext = new String(getSubByteArray(argFileBytes, 0, 31)); // 1-32
		String seriesCode = new String(getSubByteArray(argFileBytes, 32, 39)); // 33-40
		String fileExt = new String(getSubByteArray(argFileBytes, 40, 43)); // 41-44
		int length = getIntFromBytePair(getSubByteArray(argFileBytes, 44, 45)); // 45-46
		int saplength = getIntFromBytePair(getSubByteArray(argFileBytes, 46, 47)); // 47-48
		// 49-50 valid start
		// 51-52 valid end
		// 53 1=pith 2=waldkante 3=pith to waldkante
		// 54 1 = ew only last ring
		SafeIntYear startyear = new SafeIntYear(getIntFromBytePairByPos(argFileBytes, 54)); // 55-56
		// 57-58 Unknown
		// speciesCode = getIntFromBytePairByPos(argFileBytes, 44); //44
		// 59-60 species also needs a catras.wnm file
		// 61-63 creation date
		// 64-66 amended date
		// String sapwood = new String(getSubByteArray(argFileBytes, 66, 67)); //67
		// 67-68 1=valid stats
		// 69-83 Unknown
		// 84 0=raw 1=treecurve 2=chronology
		// String dated = new String(getSubByteArray(argFileBytes, 68, 74)); //69-75
		String userid = new String(getSubByteArray(argFileBytes, 84, 87)).trim(); // 85-86
		// 89-92 Float av width
		// 93-95 Float std dev
		// 96-100 Foat autocorr
		// 101-104 Float sens
		// 105-128 Unknown
		
		// Log the metadata
		log.debug("Whole meta = [" + new String(argFileBytes) + "]");
		log.debug("Header text = [" + headertext + "]");
		log.debug("Speices Code = [" + speciesCode + "]");
		log.debug("Series Code = [" + seriesCode + "]");
		log.debug("Length = " + String.valueOf(length));
		// log.debug("Sapwood? = ["+sapwood+"]");
		// log.debug("Dated = ["+dated+"]");
		log.debug("Start year = [" + startyear.toTridasYear(DatingSuffix.AD).getValue()
				+ startyear.toTridasYear(DatingSuffix.AD).getSuffix() + "]");
		log.debug("Userid = [" + userid + "]");
		
		// Species codes are not standardised so we cannot convert
		if (speciesCode > 0) {
			addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, I18n
					.getText("catras.speciesCodeNotConvertable")));
		}
		
		// Extract the data
		ArrayList<Integer> ringWidthValues = new ArrayList<Integer>();
		ArrayList<Integer> sampleDepthValues = new ArrayList<Integer>();
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
		if (ringWidthValues.size() != length) {
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("fileio.valueCountMismatch", ringWidthValues.size()+"", length+"")));
			// Trim off extra ring width values
			for (int j=length; j<ringWidthValues.size(); j++)
			{
				ringWidthValues.remove(j);
			}
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
		
		// Now build up our measurementSeries
		
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		TridasUnit units = new TridasUnit();
		
		// Set units to 1/100th mm. Is this always the case?
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		
		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(seriesCode.trim());
		seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		
		// Build interpretation group for series
		TridasInterpretation interp = new TridasInterpretation();
		interp.setFirstYear(startyear.toTridasYear(DatingSuffix.AD));
		interp.setLastYear(startyear.add(tridasValues.size()).toTridasYear(DatingSuffix.AD));
		
		// Add values to nested value(s) tags
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setValues(tridasValues);
		valuesGroup.setUnit(units);
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) defaults
				.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
		valuesGroupList.add(valuesGroup);
		
		// Add all the data to the series
		series.setValues(valuesGroupList);
		series.setInterpretation(interp);
		series.setIdentifier(seriesId);
		series.setTitle(headertext.trim());
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime());
		series.setDendrochronologist(userid);
		
		// Add series to our list
		mseriesList.add(series);
		
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
		
		if (msb == 1 && lsb >= 0) {
			w = lsb + 256;
		}
		else if (msb == 0 && lsb >= 0) {
			w = lsb;
		}
		else if (msb == 2 && lsb >= 0) {
			w = lsb + 512;
		}
		else if (msb == 3 && lsb >= 0) {
			w = lsb + 768;
		}
		else if (msb == 4 && lsb >= 0) {
			w = lsb + 1024;
		}
		
		return w;
		
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
		TridasProject project = null;
		
		try {
			project = defaults.getProjectWithDefaults(true);
			TridasObject o = project.getObjects().get(0);
			TridasElement e = o.getElements().get(0);
			TridasSample s = e.getSamples().get(0);
			
			if (speciesCode > 0) {
				ArrayList<TridasGenericField> genericFields = new ArrayList<TridasGenericField>();
				TridasGenericField gf = new TridasGenericField();
				gf.setName("catras.speciesCode");
				gf.setType("xs:int");
				gf.setValue(String.valueOf(speciesCode));
				genericFields.add(gf);
				e.setGenericFields(genericFields);
			}
			
			if (mseriesList.size() > 0) {
				TridasRadius r = s.getRadiuses().get(0);
				r.setMeasurementSeries(mseriesList);
			}
			
			if (dseriesList.size() > 0) {
				project.setDerivedSeries(dseriesList);
			}
			
		} catch (NullPointerException e) {

		} catch (IndexOutOfBoundsException e2) {

		}
		
		return project;
		
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
		speciesCode = 0;
	}
}
