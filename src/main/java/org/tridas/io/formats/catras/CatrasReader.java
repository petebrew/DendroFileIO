package org.tridas.io.formats.catras;

import java.io.IOException;
import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.TridasVariableDefaultValue;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults;
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
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**
 * Reader for the CATRAS file format.  This is a binary format for software written 
 * by R. Aniol released in 1983.  There are no specifications published for the 
 * format.  This code is based on Matlab and Fortran code of Ronald Visser and Henri 
 * Grissino-Mayer.
 * 
 * @author peterbrewer
 *
 */
public class CatrasReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(CatrasReader.class);
	// defaults given by user
	private CatrasToTridasDefaults defaults = new CatrasToTridasDefaults();
	
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	private ArrayList<TridasDerivedSeries> dseriesList = new ArrayList<TridasDerivedSeries>();;
	
	
	
	static {
		TridasIO.registerFileReader(CatrasReader.class);
	}
	
	public CatrasReader() {
		super(CatrasToTridasDefaults.class);
	}
	
	
	public CatrasReader(Class<? extends IMetadataFieldSet> argDefaultFieldsClass) {
		super(argDefaultFieldsClass);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @throws IncorrectDefaultFieldsException 
	 * @throws InvalidDendroFileException 
	 * @see org.tridas.io.IDendroCollectionWriter#loadFile(java.lang.String)
	 */
	@Override
	public void loadFile(String argFilename, IMetadataFieldSet argDefaultFields) throws IOException, IncorrectDefaultFieldsException, InvalidDendroFileException{
		fileHelper = new FileHelper();
		log.debug("loading file from: "+argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		if(bytes == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(bytes, argDefaultFields);
	}
	
	@Override
	public void loadFile(String argFilename) throws IOException, InvalidDendroFileException{
		fileHelper = new FileHelper();
		log.debug("loading file from: "+argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		if(bytes == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(bytes);
	}

	@Override
	public void loadFile(String argPath, String argFilename,
			IMetadataFieldSet argDefaultFields) throws IOException, IncorrectDefaultFieldsException, InvalidDendroFileException{
		fileHelper = new FileHelper(argPath);
		log.debug("loading file from: "+argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		if(bytes == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(bytes, argDefaultFields);
	}
	
	@Override
	public void loadFile(String argPath, String argFilename) throws IOException, InvalidDendroFileException{
		fileHelper = new FileHelper(argPath);
		log.debug("loading file from: "+argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		if(bytes == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(bytes);
	}
	

	public void loadFile(byte[] argFileBytes, IMetadataFieldSet argDefaults) throws IncorrectDefaultFieldsException, InvalidDendroFileException{
		if(!argDefaults.getClass().equals(defaultFieldsClass)){
			throw new IncorrectDefaultFieldsException(defaultFieldsClass);
		}
		parseFile(argFileBytes, argDefaults);
	}
		
	
	public void loadFile(byte[] argFileBytes) throws InvalidDendroFileException{
		parseFile(argFileBytes, constructDefaults());
	}
	
	
	

	/**
	 *  
	 * 
	 * @param argFileBytes
	 * @param argDefaultFields
	 */
	protected void parseFile(byte[] argFileBytes,
			IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException{
		
		defaults = (CatrasToTridasDefaults) argDefaultFields;
		log.debug("starting catras file parsing");
		int index=0;
			
		// Check there are at least 128 bytes
		if(argFileBytes==null)
		{
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 1);
		}	
		else if (argFileBytes.length<128)
		{
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 1);	
		}
				
		// Extract basic metadata
		String headertext = new String(getSubByteArray(argFileBytes, 0, 31));    //1-32
		String seriesCode = new String(getSubByteArray(argFileBytes, 32, 39));   //33-40
		byte[] speciesCode = null;												 //44
		int length = getIntFromBytePair(getSubByteArray(argFileBytes, 44, 45));  //45-46
		byte[] unknown1 = new byte[2];											 //47-48
		String unknowntext = new String(getSubByteArray(argFileBytes, 48, 52));	 //49-53
		//String date = new String(getSubByteArray(argFileBytes, 58, 64));		 //59-65
		String sapwood = new String(getSubByteArray(argFileBytes, 66, 67));		 //67
		String dated = new String(getSubByteArray(argFileBytes, 68, 74));    	 //69-75
		//int startyear = getIntFromBytePairByPos(argFileBytes, 80);	 //81
		SafeIntYear startyear = new SafeIntYear(2010);
		String userid = new String(getSubByteArray(argFileBytes, 84, 85));       //85-86
		
		// Log the metadata
		log.debug("Whole meta = ["+new String(argFileBytes)+"]");
		log.debug("Header text = ["+headertext+"]");
		log.debug("Series Code = ["+seriesCode+"]");
		log.debug("Length = "+String.valueOf(length));
		log.debug("Sapwood? = ["+sapwood+"]");
		log.debug("Dated = ["+dated+"]");
		log.debug("Start year = ["+String.valueOf(startyear)+"]");
		log.debug("Userid = ["+userid+"]");

		// Extract the data 
		ArrayList<TridasValue> ringWidthValues = new ArrayList<TridasValue>();
		byte[] theData = getSubByteArray(argFileBytes, 127, argFileBytes.length-1);		
		for(int i = 1 ; i<theData.length; i=i+2)
		{
			int ringwidth = this.getIntFromBytePairByPos(theData, i);
			TridasValue v = new TridasValue();
			
			if (ringwidth==999)
			{
				// Stop marker found so break
				// There are several bytes after this but we have
				// no idea what they mean.
				break;
			}
			
			v.setValue(String.valueOf(this.getIntFromBytePairByPos(theData, i)));
			ringWidthValues.add(v);
			log.debug("value = "+String.valueOf(ringwidth));
		}
						
		// Check length metadata is valid for the number of ring width values
		if(ringWidthValues.size()!=length)
		{
			this.addWarningToList(new ConversionWarning(
					WarningType.INVALID, 
					I18n.getText("catras.valueCountMismatch")));
			length = ringWidthValues.size();
			
		}
		
		// Now build up our measurementSeries
		
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		TridasUnit units = new TridasUnit();
		
		// Set units to 1/100th mm.  Is this always the case?
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		
		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(seriesCode.trim());
		seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		
		// Build interpretation group for series
		TridasInterpretation interp = new TridasInterpretation();
		interp.setFirstYear(startyear.toTridasYear(DatingSuffix.AD));

		// Add values to nested value(s) tags
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setValues(ringWidthValues);
		valuesGroup.setUnit(units);
		TridasVariableDefaultValue variable = (TridasVariableDefaultValue) defaults.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
		valuesGroupList.add(valuesGroup);	
		
		// Add all the data to the series
		series.setValues(valuesGroupList);
		series.setInterpretation(interp);
		series.setIdentifier(seriesId);
		series.setTitle(headertext.trim());
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime() );
		series.setDendrochronologist(userid);

		// Add series to our list
		mseriesList.add(series);
		
		
		
	}
	
	private byte[] getSubByteArray(byte[] bytes, int start, int end)
	{
		end++;
		if(start>end) return null;
		
		
		byte[] outarr = new byte[end-start];
			
		int i = start;
		int i2 = 0;
		for( ; i < end; i++){
			outarr[i2] = bytes[i];
			i2++;
		}
		
		return outarr;
	}
	
	/**
	 * Extract a byte pair from a larger byte array by specifying the position
	 * of the first byte.  Assumes bytes are little-endian.
	 * 
	 * @param bytes
	 * @param pos
	 * @return
	 */
	private byte[] getBytePairByPos(byte[] bytes, int pos)
	{
		byte wBytes[] = new byte[2];
		
		if(pos<0)
		{
			return null;
		}
		try{
			
			wBytes[0] = bytes[pos];
			wBytes[1] = bytes[pos+1];
		} catch (ArrayIndexOutOfBoundsException e){
			
		}

		return wBytes;
	}
	
	
	/**
	 * Wrapper for getIntFromBytePair() with default little-endian
	 * 
	 * @param wBytes
	 * @return
	 */
	private int getIntFromBytePair(byte[] wBytes)
	{
		return getIntFromBytePair(wBytes, true);
	}
	
	/**
	 * Extract the integer value from a byte pair according to endianess
	 * 
	 *  Horror! Java byte is signed!
	 *  See: http://www.darksleep.com/player/JavaAndUnsignedTypes.html
	 * 
	 * @param wBytes
	 * @param littleEndian use little-endian?
	 * @return
	 */
	private int getIntFromBytePair(byte[] wBytes, Boolean littleEndian)
	{
		
		int lsb;    // least significant byte
		int msb;    // most significant byte
		int w = -1; // actual value
		
		// Depending on endian, extract bytes in correct order.  Java is always
		// big endian, but Windoze i386 are always (?) little endian.  As CATRAS
		// is the only (?) program that writes CATRAS files then they should
		// always be little endian I think!
		if(littleEndian)
		{
			lsb = (0x000000FF & ((int)wBytes[0]));  // least significant byte
			msb = (0x000000FF & ((int)wBytes[1]));  // most significant byte
		}
		else
		{
			lsb = (0x000000FF & ((int)wBytes[1]));  // least significant byte
			msb = (0x000000FF & ((int)wBytes[0]));  // most significant byte
		}
		
		//log.debug("LSB = "+String.valueOf(lsb)+"  MSB = "+String.valueOf(lsb));
				
        if (msb == 1 && lsb >= 0) {
            w = lsb+256;
        } else if (msb == 0 && lsb >= 0) {
            w = lsb;
        } else if (msb == 2 && lsb >= 0) {
            w = lsb+512;
        } else if (msb == 3 && lsb >= 0) {
            w = lsb+768;
        } else if ( msb == 4 && lsb >= 0 ) {
            w = lsb+1024;
		} 
 		
        return w;
		
	}
	
	/**
	 * Extract a byte pair from a large byte array according to the position
	 * value of the first byte.  Then extract the integer value assuming
	 * data is little-endian.
	 * 
	 * @param wBytes
	 * @param pos
	 * @return
	 */
	private int getIntFromBytePairByPos(byte[] wBytes, int pos)
	{
		return getIntFromBytePair(getBytePairByPos(wBytes, pos));
	}
	
	
	@Override
	public String[] getFileExtensions() {
		return new String[] {"cat"};
	}

	@Override
	public TridasProject getProject() {
		TridasProject project = null;
		
		try{
			project = defaults.getProjectWithDefaults(true);
			TridasObject o = project.getObjects().get(0);
			TridasElement e = o.getElements().get(0);
			TridasSample s = e.getSamples().get(0);
			
			if(mseriesList.size()>0)
			{
				TridasRadius r = s.getRadiuses().get(0);
				r.setMeasurementSeries(mseriesList);
			}
			
			if(dseriesList.size()>0)
			{
				project.setDerivedSeries(dseriesList);
			}
			
			} catch (NullPointerException e){
				
			} catch (IndexOutOfBoundsException e2){
				
			}
			
			
			return project;
		
	}

	private char[] byteArr2CharArr(byte[] byteArr) {

		char[] charArr = new char[byteArr.length] ;
		String str = new String(byteArr) ;
		charArr = str.toCharArray() ;

		return charArr;
	}
		
	
	
	//*******************************
	//  NOT SUPPORTED - BINARY FORMAT
	//*******************************
	
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields) {}
	
	@Override
	public void loadFile(String[] argFileStrings) throws InvalidDendroFileException{}
	
	
	
}
