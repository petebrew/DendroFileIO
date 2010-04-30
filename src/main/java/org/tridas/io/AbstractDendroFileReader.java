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

	private int currentLineNumber = 0;
	private static final SimpleLogger log = new SimpleLogger(AbstractDendroFileReader.class);
	private ArrayList<ConversionWarning> warnings =  new ArrayList<ConversionWarning>();
	private FileHelper fileHelper;
	private ArrayList<String> rawMetadata = new ArrayList<String>();
	private Class<? extends IMetadataFieldSet> defaultFieldsClass;
	
	public AbstractDendroFileReader(Class<? extends IMetadataFieldSet> argDefaultFieldsClass){
		if(argDefaultFieldsClass == null){
			throw new RuntimeException(I18n.getText("fileio.defaultsnull")); 
		}
		defaultFieldsClass = argDefaultFieldsClass;
		// see if we can construct an instance
		try {
			argDefaultFieldsClass.newInstance();
		} catch (Exception e) {
			log.error("Defaults class '"+argDefaultFieldsClass.getName()+"' does not have empty constructor.");
			log.dbe(DebugLevel.L2_ERROR, e);
			throw new RuntimeException("Defaults class must have empty constructor."); // TODO locale
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
			log.error("Defaults class '"+defaultFieldsClass.getName()+"' does not have empty constructor.");
			return null;
		} catch (IllegalAccessException e) {
			log.error("Defaults class cannot be created");
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
		String[] strings = fileHelper.loadStrings(argFilename);
		if(strings == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(strings, argDefaultFields);
	}
	
	@Override
	public void loadFile(String argFilename) throws IOException, InvalidDendroFileException{
		fileHelper = new FileHelper();
		log.debug("loading file from: "+argFilename);
		String[] strings = fileHelper.loadStrings(argFilename);
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
		String[] strings = fileHelper.loadStrings(argFilename);
		if(strings == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(strings, argDefaultFields);
	}
	
	@Override
	public void loadFile(String argPath, String argFilename) throws IOException, InvalidDendroFileException{
		fileHelper = new FileHelper(argPath);
		log.debug("loading file from: "+argFilename);
		String[] strings = fileHelper.loadStrings(argFilename);
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
	public int getCurrentLineNumber()
	{
		return this.currentLineNumber;
	}
	
	/**
	 * Get the current line number that is being processed.  Typically
	 * used to determine at which point the reader failed.
	 * 
	 * @return
	 */
	public String getCurrentLineNumberAsString()
	{
		return String.valueOf(this.currentLineNumber);
	}
	
	/**
	 * Set the line number currently being read.
	 * @param num
	 */
	protected void setCurrentLineNumber(int num)
	{
		this.currentLineNumber = num;
	}
}
