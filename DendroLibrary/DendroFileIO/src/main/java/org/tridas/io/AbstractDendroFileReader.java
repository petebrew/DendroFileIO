package org.tridas.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.FileHelper;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;

public abstract class AbstractDendroFileReader implements IDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(AbstractDendroFileReader.class);
	private ArrayList<ConversionWarning> warnings =  new ArrayList<ConversionWarning>();
	private FileHelper fileHelper;
	private ArrayList<String> rawMetadata = new ArrayList<String>();
	private final Class<? extends IMetadataFieldSet> defaultFieldsClass;
	private DendroFormatInfo formatInformation;
	
	public AbstractDendroFileReader(String baseTagName, Class<? extends IMetadataFieldSet> argDefaultFieldsClass){
		
		formatInformation = new DendroFormatInfo(baseTagName);
		
		if(argDefaultFieldsClass == null){
			throw new RuntimeException(I18n.getText("fileio.defaultsnull")); 
		}
		
		try {
			if(argDefaultFieldsClass.getConstructor(new Class<?>[]{}) == null){
				log.error("Defaults class '"+argDefaultFieldsClass.getName()+"' does not have empty constructor.");
				throw new RuntimeException(I18n.getText("runtimeExceptions.emptyConstructor")); 
			}
		} catch (SecurityException e) {
			throw new RuntimeException(I18n.getText("runtimeExceptions.emptyConstructor")); 
		} catch (NoSuchMethodException e) {
			log.error(I18n.getText("runtimeExceptions.emptyConstructor"));
			throw new RuntimeException(I18n.getText("runtimeExceptions.emptyConstructor")); 
		}
		
		defaultFieldsClass = argDefaultFieldsClass;
	}
	
	public Class<? extends IMetadataFieldSet> getDefaultFieldsClass(){
		return defaultFieldsClass;
	}
	
	/**
	 * Get a list of conversion warnings for this 
	 * Collection writer
	 * 
	 * @return
	 */
	public List<ConversionWarning> getWarnings()
	{
		return warnings;
	}
	
	/**
	 * @see org.tridas.io.DendroFormatInfo#getShortName()
	 */
	public String getShortName()
	{
		return formatInformation.getShortName();
	}
	
	/**
	 * @see org.tridas.io.DendroFormatInfo#getFullName()
	 */
	public String getFullName()
	{
		return formatInformation.getFullName();
	}
	
	/**
	 * @see org.tridas.io.DendroFormatInfo#getDescription()
	 */
	public String getDescription()
	{
		return formatInformation.getDescription();
	}
	
	/**
	 * @see org.tridas.io.DendroFormatInfo#getPreferredFileExtension()
	 */
	public String getPreferredFileExtension()
	{
		return formatInformation.getPreferredFileExtension();
	}
	
	/**
	 * Add a warning to our list of ConversionWarnings
	 * 
	 * @param warning
	 */
	protected void addWarningToList(ConversionWarning warning)
	{
		warnings.add(warning);
	}
	
	/**
	 * Clear list of warnings
	 */
	protected void clearWarnings()
	{
		warnings.clear();
	}
	
	
	public IMetadataFieldSet constructDefaults(){
		try {
			return defaultFieldsClass.newInstance();
		} catch (InstantiationException e) {
			log.error(I18n.getText("runtimeExceptions.emptyConstructor"));
			return null;
		} catch (IllegalAccessException e) {
			log.error(I18n.getText("fileio.defaults.cantConstruct"));
			return null;
		}
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
		String[] strings;
		if(TridasIO.isCharsetDetection()){
			strings = fileHelper.loadStringsFromDetectedCharset(argFilename);
		}else{
			strings = fileHelper.loadStrings(argFilename);
		}
		if(strings == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(strings, argDefaultFields);
	}
	
	@Override
	public void loadFile(String argFilename) throws IOException, InvalidDendroFileException{
		fileHelper = new FileHelper();
		log.debug("loading file from: "+argFilename);
		String[] strings;
		if(TridasIO.isCharsetDetection()){
			strings = fileHelper.loadStringsFromDetectedCharset(argFilename);
		}else{
			strings = fileHelper.loadStrings(argFilename);
		}
		if(strings == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(strings);
	}

	@Override
	public void loadFile(String argPath, String argFilename,
			IMetadataFieldSet argDefaultFields) throws IOException, IncorrectDefaultFieldsException, InvalidDendroFileException{
		fileHelper = new FileHelper(argPath);
		log.debug("loading file from: "+argFilename);
		String[] strings;
		if(TridasIO.isCharsetDetection()){
			strings = fileHelper.loadStringsFromDetectedCharset(argFilename);
		}else{
			strings = fileHelper.loadStrings(argFilename);
		}
		if(strings == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(strings, argDefaultFields);
	}
	
	@Override
	public void loadFile(String argPath, String argFilename) throws IOException, InvalidDendroFileException{
		fileHelper = new FileHelper(argPath);
		log.debug("loading file from: "+argFilename);
		String[] strings;
		if(TridasIO.isCharsetDetection()){
			strings = fileHelper.loadStringsFromDetectedCharset(argFilename);
		}else{
			strings = fileHelper.loadStrings(argFilename);
		}
		if(strings == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(strings);
	}
	
	@Override
	public void loadFile(String[] argFileStrings, IMetadataFieldSet argDefaults) throws IncorrectDefaultFieldsException, InvalidDendroFileException{
		if(!argDefaults.getClass().equals(defaultFieldsClass)){
			throw new IncorrectDefaultFieldsException(defaultFieldsClass);
		}
		parseFile(argFileStrings, argDefaults);
	}
	
	@Override
	public void loadFile(String[] argFileStrings) throws InvalidDendroFileException{
		parseFile(argFileStrings, constructDefaults());
	}
	
	
	protected abstract void parseFile( String[] argFileString, IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException;

	/**
	 * Get the raw metadata for this file
	 * 
	 * @return
	 */
	public ArrayList<String> getRawMetadata(){
		return rawMetadata;
	}
	
	/**
	 * Add a line to the list of metadata lines
	 * 
	 * @param line
	 */
	protected void addRawMetadataLine(String argLine){
		if(argLine == null){
			return;
		}
		
		rawMetadata.add(argLine);
	}
	
	/**
	 * Get the current line number that is being processed.  Typically
	 * used to determine at which point the reader failed.
	 * 
	 * @return
	 */
	public abstract int getCurrentLineNumber();
}
