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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.grlea.log.SimpleLogger;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.util.FileHelper;
import org.tridas.schema.TridasProject;

/**
 * @author Daniel Murphy
 */
public abstract class AbstractDendroCollectionWriter {
	
	private ArrayList<IDendroFile> fileList = new ArrayList<IDendroFile>();
	private SimpleLogger log = new SimpleLogger(AbstractDendroCollectionWriter.class);
	private ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
	private Class<? extends IMetadataFieldSet> defaultFieldsClass;
	
	/**
	 * @param argDefaultFieldsClass
	 */
	public AbstractDendroCollectionWriter(Class<? extends IMetadataFieldSet> argDefaultFieldsClass) {
		if (argDefaultFieldsClass == null) {
			throw new RuntimeException(I18n.getText("fileio.defaultsnull"));
		}
		
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
	 * Loads a project to convert into a legacy format, using the default metadata set
	 * 
	 * @param argProject
	 * @throws IncompleteTridasDataException
	 * @throws ConversionWarningException
	 */
	public void loadProject(TridasProject argProject) throws IncompleteTridasDataException, ConversionWarningException {
		IMetadataFieldSet defaults = constructDefaultMetadata();
		parseTridasProject(argProject, defaults);
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
	 * Loads a project to convert into a legacy format, using the given metadata set
	 * 
	 * @param argProject
	 * @param argDefaults
	 * @throws IncompleteTridasDataException
	 * @throws ConversionWarningException
	 * @throws IncorrectDefaultFieldsException
	 */
	public void loadProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException, IncorrectDefaultFieldsException {
		if(argDefaults == null){
			loadProject(argProject);
		}
		if (!argDefaults.getClass().equals(defaultFieldsClass)) {
			throw new IncorrectDefaultFieldsException(defaultFieldsClass);
		}
		parseTridasProject(argProject, argDefaults);
	}
	
	/**
	 * Parse the project with the given defaults
	 * 
	 * @param argProject
	 * @param argDefaults
	 * @throws IncompleteTridasDataException
	 * @throws ConversionWarningException
	 */
	protected abstract void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException;
	
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
	 */
	public void saveAllToDisk() {
		saveAllToDisk("");
	}
	
	/**
	 * Save all associated files to the disk
	 * 
	 * @param argOutputFolder
	 *            the folder to save the files to
	 */
	public void saveAllToDisk(String argOutputFolder) {
		
		if (!argOutputFolder.endsWith(File.separator) && !argOutputFolder.equals("")) {
			argOutputFolder += File.separator;
		}
		
		for (IDendroFile dof : fileList) {
			String filename = getNamingConvention().getFilename(dof);
			saveFileToDisk(argOutputFolder, filename, dof);
		}
	}
	
	/**
	 * Used specify where to save each file individually.
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
	protected void saveFileToDisk(String argOutputFolder, String argFilename, IDendroFile argFile) {
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
		
		if (absolute) {
			if (TridasIO.getWritingCharset() != null) {
				try {
					helper.saveStrings(argFilename + "." + argFile.getExtension(), file, TridasIO.getWritingCharset());
					return;
				} catch (UnsupportedEncodingException e) {
					// shouldn't happen, but
					// TODO add warning, log message
					e.printStackTrace();
				}
			}
			helper.saveStrings(argFilename + "." + argFile.getExtension(), file);
			
		}
		else {
			if (TridasIO.getWritingCharset() != null) {
				try {
					helper.saveStrings(argOutputFolder + argFilename + "." + argFile.getExtension(), file, TridasIO
							.getWritingCharset());
					return;
				} catch (UnsupportedEncodingException e) {
					// shouldn't happen, but
					// TODO add warning, log message
					e.printStackTrace();
				}
			}
			helper.saveStrings(argOutputFolder + argFilename + "." + argFile.getExtension(), file);
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
	 * Set the naming convention
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
	 * Get the short name of the format
	 * 
	 * @return
	 */
	public abstract String getShortName();
	
	/**
	 * Get the full name of the format
	 * 
	 * @return
	 */
	public abstract String getFullName();
	
	/**
	 * Get the description of the format
	 * 
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Get the default values for this writer.
	 * 
	 * @return
	 */
	public abstract IMetadataFieldSet getDefaults();
}
