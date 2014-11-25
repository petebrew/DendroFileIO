/*******************************************************************************
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.tridas.io;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.NothingToWriteException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.util.FileHelper;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;

/**
 * @author Daniel Murphy
 */
public abstract class AbstractDendroCollectionWriter{
	private static final Logger log = LoggerFactory.getLogger(AbstractDendroCollectionWriter.class);

	private final AbstractDendroFormat format;
	private ArrayList<IDendroFile> fileList = new ArrayList<IDendroFile>();
	private ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
	private Class<? extends IMetadataFieldSet> defaultFieldsClass;
	
	/**
	 * @param argDefaultFieldsClass
	 */
	public AbstractDendroCollectionWriter(Class<? extends IMetadataFieldSet> argDefaultFieldsClass, AbstractDendroFormat format) {
		if (argDefaultFieldsClass == null) {
			throw new RuntimeException(I18n.getText("fileio.defaultsnull"));
		}
		
		if(format ==null ) throw new RuntimeException("Null format description");
		
		this.format = format;
		
		try {
			if (argDefaultFieldsClass.getConstructor(new Class<?>[]{}) == null) {
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
	
	/**
	 * Loads a tridas container to convert into a legacy format, using the default metadata set
	 * 
	 * @param argProject
	 * @throws ImpossibleConversionException
	 * @throws ConversionWarningException
	 */
	public void load(TridasTridas argContainer) throws ImpossibleConversionException, ConversionWarningException {
		IMetadataFieldSet defaults = constructDefaultMetadata();
		parseTridasContainer(argContainer, defaults);
	}
	
	/**
	 * Construct the default metadata fields
	 * 
	 * @return
	 */
	public IMetadataFieldSet constructDefaultMetadata() {
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
	
	/**
	 * Loads a container to convert into a legacy format, using the given metadata set
	 * 
	 * @param argContainer
	 * @param argDefaults
	 * @throws ImpossibleConversionException
	 * @throws ConversionWarningException
	 * @throws IncorrectDefaultFieldsException
	 */
	public void load(TridasTridas argContainer, IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException, IncorrectDefaultFieldsException {
		if(argDefaults == null){
			load(argContainer);
		}
		if (!argDefaults.getClass().equals(defaultFieldsClass)) {
			throw new IncorrectDefaultFieldsException(defaultFieldsClass);
		}
		parseTridasContainer(argContainer, argDefaults);
	}
	
	/**
	 * Loads a TRiDaS project to convert into a legacy format, using the given metadata set
	 * 
	 * @param argProject
	 * @param argDefaults
	 * @throws ImpossibleConversionException
	 * @throws ConversionWarningException
	 * @throws IncorrectDefaultFieldsException
	 */
	public void load(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException, IncorrectDefaultFieldsException {
		if(argDefaults == null){
			load(argProject);
		}
		if (!argDefaults.getClass().equals(defaultFieldsClass)) {
			throw new IncorrectDefaultFieldsException(defaultFieldsClass);
		}
		parseTridasProject(argProject, argDefaults);
	}
	
	/**
	 * Loads a TRiDaS project to convert into a legacy format, using the default metadata set
	 * 
	 * @param argProject
	 * @throws ImpossibleConversionException
	 * @throws ConversionWarningException
	 */
	public void load(TridasProject argProject) throws ImpossibleConversionException, ConversionWarningException {
		IMetadataFieldSet defaults = constructDefaultMetadata();
		parseTridasProject(argProject, defaults);
	}
	
	/**
	 * Deprecated.  Use load(TridasProject argProject, IMetadataFieldSet argDefaults) instead
	 * 
	 * 
	 * @param argProject
	 * @param argDefaults
	 * @throws ImpossibleConversionException
	 * @throws ConversionWarningException
	 * @throws IncorrectDefaultFieldsException
	 * @deprecated @see org.tridas.io.AbstractDendroCollectionWriter#load(org.tridas.schema.TridasProject, org.tridas.io.default.IMetadataFieldSet)
	 */
	public void loadProject(TridasProject argProject, IMetadataFieldSet argDefaults)
	throws ImpossibleConversionException, ConversionWarningException, IncorrectDefaultFieldsException {
	}
	
	/**
	 * Deprecated.  Use load(TridasProject argProject) instead
	 * 
	 * @param argProject
	 * @throws ImpossibleConversionException
	 * @throws ConversionWarningException
	 * @deprecated @see org.tridas.io.AbstractDendroCollectionWriter#load(org.tridas.schema.TridasProject) 
	 */
	public void loadProject(TridasProject argProject) throws ImpossibleConversionException, ConversionWarningException {
		load(argProject);
	}
	
	
	/**
	 * Parse the project with the given defaults
	 * 
	 * @param argProject
	 * @param argDefaults
	 * @throws ImpossibleConversionException
	 * @throws ConversionWarningException
	 */
	protected abstract void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException;
	
	/**
	 * Parse the TRiDaS container with the given defaults
	 * 
	 * @param argContainer
	 * @param argDefaults
	 * @throws ImpossibleConversionException
	 * @throws ConversionWarningException
	 */
	protected void parseTridasContainer(TridasTridas argContainer,
			IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
	
		for(TridasProject project : argContainer.getProjects())
		{
			parseTridasProject(project, argDefaults);
		}	
	}
	
	/**
	 * Get the list of DendroFiles that are associated
	 * with this CollectionWriter
	 * 
	 * @return
	 */
	protected ArrayList<IDendroFile> getFileList() {
		return fileList;
	}
	
	/**
	 * Get the {@link IDendroFile}s generated from
	 * loading this project
	 * 
	 * @return
	 */
	public IDendroFile[] getFiles() {
		return fileList.toArray(new IDendroFile[0]);
	}
	
	/**
	 * Save all associated files to the disk
	 * in the same folder as the jar.
	 * 
	 * @throws ImpossibleConversionException 
	 */
	public void saveAllToDisk() throws NothingToWriteException {
		saveAllToDisk("");
	}
	
	/**
	 * Save all associated files to the disk
	 * 
	 * @param argOutputFolder
	 *            the folder to save the files to
	 * @throws ImpossibleConversionException 
	 */
	public void saveAllToDisk(String argOutputFolder) throws NothingToWriteException {
		
		if (!argOutputFolder.endsWith(File.separator) && !argOutputFolder.equals("")) {
			argOutputFolder += File.separator;
		}
		
		if(fileList == null || fileList.size()==0)
		{
			throw new NothingToWriteException();
		}
		
		
		for (IDendroFile dof : fileList) {
			String filename = getNamingConvention().getFilename(dof);
			saveFileToDisk(argOutputFolder, filename, dof);
		}
	}
		
	/**
	 * User specify where to save each file individually.
	 * 
	 * @param argOutputFolder
	 * @param argFile
	 *            must be a file from this writer
	 * @throws RuntimeException
	 *             if the file is not in this writer's filelist
	 */
	public void saveFileToDisk(String argOutputFolder, IDendroFile argFile) {
		if (!fileList.contains(argFile)) {
			throw new RuntimeException("File not found in file list.");
		}
		saveFileToDisk(argOutputFolder, getNamingConvention().getFilename(argFile), argFile);
	}
	
	/**
	 * Override to implement own file saving. Make sure to respect
	 * {@link TridasIO#getWritingCharset()}.
	 * 
	 * @param argOutputFolder
	 *            output folder can be absolute, and always ends with "/" unless it's an
	 *            empty string
	 * @param argFilename
	 *            filename of the file (without extension)
	 * @param argFile
	 *            a dendro file of this writer
	 */
	protected void saveFileToDisk(String argOutputFolder, String argFilename, IDendroFile argFile) 
	{
		saveFileToDisk(argOutputFolder, argFilename, null, argFile);
	}
		
	protected void saveFileToDisk(String argOutputFolder, String argFilename, String forceExtension, IDendroFile argFile) {
		FileHelper helper;
		boolean absolute = (new File(argOutputFolder)).isAbsolute();
		
		// add ending file separator
		if (!argOutputFolder.endsWith("\\") && !argOutputFolder.endsWith("/") && argOutputFolder.length() != 0) {
			argOutputFolder += File.separatorChar;
		}
		if (argOutputFolder.endsWith("\\")) {
			argOutputFolder = argOutputFolder.substring(0, argOutputFolder.length() - 1) + File.separatorChar;
		}
		
		if (absolute) {
			helper = new FileHelper(argOutputFolder);
		}
		else {
			helper = new FileHelper();
		}
		
		String[] file = argFile.saveToString();
		if (file == null) {
			log.error("File strings for file " + argFile.toString() + ", with the filename " + argFile + " was null");
			return;
		}
		
		
		String fullfilename;
		if(forceExtension!=null)
		{
			fullfilename = argFilename + "." + forceExtension;
	
		}
		else
		{
			fullfilename = argFilename + "." + argFile.getExtension();
		}
		
		if (absolute) {
			if (TridasIO.getWritingCharset() != null) {
				try {
					helper.saveStrings(fullfilename, file, TridasIO.getWritingCharset());
					return;
				} catch (UnsupportedEncodingException e) {
					// shouldn't happen, but
					// TODO add warning, log message
					e.printStackTrace();
				}
			}
			helper.saveStrings(fullfilename, file);
			
		}
		else {
			if (TridasIO.getWritingCharset() != null) {
				try {
					helper.saveStrings(argOutputFolder + fullfilename, file, TridasIO
							.getWritingCharset());
					return;
				} catch (UnsupportedEncodingException e) {
					log.error("Exception trying to save strings",e);
				}
			}
			helper.saveStrings(argOutputFolder + fullfilename, file);
		}
	}
	
	/**
	 * Add DendroFile to list of files to write
	 * 
	 * @param df
	 */
	protected void addToFileList(IDendroFile df) {
		fileList.add(df);
	}
	
	/**
	 * Clears the file list
	 */
	public void clearFiles() {
		fileList.clear();
	}
	
	/**
	 * Get a list of conversion warnings for this
	 * Collection writer
	 * 
	 * @return
	 */
	public ConversionWarning[] getWarnings() {
		return warnings.toArray(new ConversionWarning[0]);
	}
	
	/**
	 * Add a warning to our list of ConversionWarnings
	 * 
	 * @param warning
	 */
	public void addWarning(ConversionWarning warning) {
		warnings.add(warning);
	}
	
	/**
	 * Clear warning list
	 */
	public void clearWarnings() {
		warnings.clear();
	}
	
	/**
	 * Set the naming convention.  
	 * 
	 * @param argConvension
	 */
	public abstract void setNamingConvention(INamingConvention argConvension);
	
	/**
	 * Get the naming convention
	 * 
	 * @return
	 */
	public abstract INamingConvention getNamingConvention();
	
	/**
	 * Get the default values for this writer.
	 * 
	 * @return
	 */
	public abstract IMetadataFieldSet getDefaults();
	
	
	/**
	 * Returns a list of the file extensions for this file
	 * 
	 * @return
	 */
	public String[] getFileExtensions()
	{
		return format.getFileExtensions();
	}
	
	/**
	 * Get the short name of the format
	 * 
	 * @return
	 */
	public String getShortName()
	{
		return format.getShortName();
	}
	
	/**
	 * Get the full name of the format
	 * 
	 * @return
	 */
	public String getFullName()
	{
		return format.getFullName();
	}
	
	/**
	 * Get the description of the format
	 * 
	 * @return
	 */
	public String getDescription()
	{
		return format.getDescription();
	}
	
	/**
	 * Get a file filter for this format
	 * 
	 * @return
	 */
	public DendroFileFilter getDendroFileFilter()
	{
		return format.getDendroFileFilter();
	}
}
