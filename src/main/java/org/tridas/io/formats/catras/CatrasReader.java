package org.tridas.io.formats.catras;

import java.io.IOException;
import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults;
import org.tridas.io.formats.tucson.TucsonReader;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults;
import org.tridas.io.util.FileHelper;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;

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
		byte[] dated = new byte[7];
		byte[] startyear = new byte[1];
		byte[] userid = new byte[2];
		
		log.debug("Whole meta = ["+new String(argFileBytes)+"]");
		log.debug("Header text = ["+headertext+"]");
		log.debug("Series Code = ["+seriesCode+"]");
		log.debug("Length = "+String.valueOf(length));
		log.debug("Sapwood? = ["+sapwood+"]");

		byte[] theData = getSubByteArray(argFileBytes, 129, argFileBytes.length-1);
		
		for(int i = 1 ; i<theData.length; i=i+2)
		{
			log.debug("value = "+String.valueOf(this.getIntFromBytePairByPos(theData, i)));
			
		}
		
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
		if(pos<0)
		{
			return null;
		}
		
		byte wBytes[] = new byte[2];
		wBytes[0] = bytes[pos];
		wBytes[1] = bytes[pos+1];		
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
		
		// Depending on endian, extract bytes in correct order
		if(littleEndian)
		{
			lsb = wBytes[0]; 
			msb = wBytes[1]; 
		}
		else
		{
			lsb = wBytes[1]; // least significant byte
			msb = wBytes[0]; // most significant byte
		}
		
		
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
		} else if (lsb < 0) {
			w = -2; // signal!

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
		// TODO Auto-generated method stub
		return null;
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
