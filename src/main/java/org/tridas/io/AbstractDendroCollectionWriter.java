package org.tridas.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.FileHelper;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.schema.TridasProject;

public abstract class AbstractDendroCollectionWriter implements IDendroCollectionWriter{
	
	public ArrayList<DendroFile> fileList = new ArrayList<DendroFile>();
	private SimpleLogger log = new SimpleLogger(AbstractDendroCollectionWriter.class);
	private ArrayList<ConversionWarning> warnings =  new ArrayList<ConversionWarning>();
	private Class<? extends IMetadataFieldSet> defaultFieldsClass;
	private DendroFormatInfo formatInformation;
	
	public AbstractDendroCollectionWriter(String baseTag, Class<? extends IMetadataFieldSet> argDefaultFieldsClass){
		formatInformation = new DendroFormatInfo(baseTag);
		
		if(argDefaultFieldsClass == null){
			throw new RuntimeException(I18n.getText("fileio.defaultsnull")); 
		}
		
		try {
			if(argDefaultFieldsClass.getConstructor(new Class<?>[]{}) == null){
				log.error(I18n.getText("runtimeExceptions.emptyConstructor"));
				throw new RuntimeException(); 
			}
		} catch (SecurityException e) {
			throw new RuntimeException(I18n.getText("runtimeExceptions.emptyConstructor")); 
		} catch (NoSuchMethodException e) {
			log.error(I18n.getText("runtimeExceptions.emptyConstructor"));
			throw new RuntimeException(I18n.getText("runtimeExceptions.emptyConstructor")); 
		}
		
		defaultFieldsClass = argDefaultFieldsClass;
	}
	
	public void loadProject(TridasProject argProject) throws IncompleteTridasDataException, ConversionWarningException{
		IMetadataFieldSet defaults = constructDefaults();
		parseTridasProject(argProject, defaults);
	}
	
	public IMetadataFieldSet constructDefaults(){
		try {
			return defaultFieldsClass.newInstance();
		} catch (InstantiationException e) {
			log.error(I18n.getText("runtimeExceptions.emptyConstructor"));
			return null;
		} catch (IllegalAccessException e) {
			log.error("Defaults class cannot be created");
			return null;
		}
	}
	
	@Override
	public void loadProject(TridasProject argProject, IMetadataFieldSet argDefaults) throws IncompleteTridasDataException, ConversionWarningException, IncorrectDefaultFieldsException{
		if(!argDefaults.getClass().equals(defaultFieldsClass)){
			throw new IncorrectDefaultFieldsException(defaultFieldsClass);
		}
		parseTridasProject(argProject, argDefaults);
	}
	
	protected abstract void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults) throws IncompleteTridasDataException, ConversionWarningException;
	
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
	
	public String getFileExtension()
	{
		return getPreferredFileExtension();
	}
	
	/**
	 * Get a count of how many DendroFiles are associated with this
	 * CollectionWriter
	 * 
	 * @return
	 */
	public int getFileCount(){
		return fileList.size();
	}
	
	/**
	 * Get the list of DendroFiles that are associated 
	 * with this CollectionWriter
	 * 
	 * @return
	 */
	protected ArrayList<DendroFile> getFileList(){
		return fileList;
	}
	
	public DendroFile[] getFiles(){
		return fileList.toArray(new DendroFile[0]);
	}
	
	/**
	 * Save all associated files to disk
	 */
	public void saveAllToDisk(){
		saveAllToDisk("");
	}
	
	/**
	 * Save all associated files to disk
	 */
	public void saveAllToDisk(String argOutputFolder){
		FileHelper helper;
		
		boolean absolute = argOutputFolder.startsWith("/") || argOutputFolder.startsWith("\\");
		// add ending file separator
		if(!argOutputFolder.endsWith("\\") && !argOutputFolder.endsWith("/") && argOutputFolder.length() != 0){
			argOutputFolder += File.separatorChar;
		}
		
		if(absolute){
			helper = new FileHelper(argOutputFolder);
		}else{
			helper = new FileHelper();
		}
		
		for (DendroFile dof: fileList){
			String filename = getNamingConvention().getFilename(dof);
			if(absolute){
				helper.saveStrings(filename+"."+dof.getExtension(), dof.saveToString());
			}else{
				helper.saveStrings(argOutputFolder+filename+"."+dof.getExtension(), dof.saveToString());
			}
		}
	}
	
	/**
	 * Add DendroFile to list of files to write
	 * 
	 * @param df
	 */
	public void addToFileList(DendroFile df){
		fileList.add(df);
	}

	/**
	 * Get a list of conversion warnings for this 
	 * Collection writer
	 * 
	 * @return
	 */
	public List<ConversionWarning> getWarnings(){
		return warnings;
	}
	
	/**
	 * Add a warning to our list of ConversionWarnings
	 * 
	 * @param warning
	 */
	protected void addWarningToList(ConversionWarning warning){
		warnings.add(warning);
	}
	
	/**
	 * Clear list of warnings
	 */
	protected void clearWarnings(){
		warnings.clear();
	}
	
	
}