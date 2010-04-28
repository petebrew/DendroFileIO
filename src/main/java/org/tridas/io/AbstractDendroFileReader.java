package org.tridas.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.FileHelper;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;

public abstract class AbstractDendroFileReader implements IDendroFileReader {

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
	
	
	
	/**
	 * @throws IncorrectDefaultFieldsException 
	 * @see org.tridas.io.IDendroCollectionWriter#loadFile(java.lang.String)
	 */
	@Override
	public void loadFile(String argFilename, IMetadataFieldSet argDefaultFields) throws IOException, IncorrectDefaultFieldsException{
		fileHelper = new FileHelper();
		log.debug("loading file from: "+argFilename);
		String[] strings = fileHelper.loadStrings(argFilename);
		if(strings == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(strings, argDefaultFields);
	}

	@Override
	public void loadFile(String argPath, String argFilename,
			IMetadataFieldSet argDefaultFields) throws IOException, IncorrectDefaultFieldsException{
		fileHelper = new FileHelper(argPath);
		log.debug("loading file from: "+argFilename);
		String[] strings = fileHelper.loadStrings(argFilename);
		if(strings == null){
			throw new IOException(I18n.getText("fileio.loadfailed")); 
		}
		loadFile(strings, argDefaultFields);
	}
	
	@Override
	public void loadFile(String[] argFileStrings, IMetadataFieldSet argDefaults) throws IncorrectDefaultFieldsException{
		if(!argDefaults.getClass().equals(defaultFieldsClass)){
			throw new IncorrectDefaultFieldsException(defaultFieldsClass);
		}
		parseFile(argFileStrings, argDefaults);
	}
	
	
	protected abstract void parseFile( String[] argFileString, IMetadataFieldSet argDefaultFields);

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
}
