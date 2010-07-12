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
package org.tridas.io.naming;

import java.util.ArrayList;
import java.util.HashMap;

import org.grlea.log.SimpleLogger;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * @author daniel
 */
public abstract class AbstractNamingConvention implements INamingConvention {
	
	private static final SimpleLogger log = new SimpleLogger(AbstractNamingConvention.class);
	public static String DEFAULT_FILENAME = "unknown";
	
	private HashMap<String, ArrayList<IDendroFile>> nameMap = new HashMap<String, ArrayList<IDendroFile>>();
	private HashMap<IDendroFile, String> fileMap = new HashMap<IDendroFile, String>();
	
	/**
	 * @see org.tridas.io.naming.INamingConvention#registerFile(org.tridas.io.IDendroFile,
	 *      org.tridas.schema.TridasProject, org.tridas.schema.TridasObject,
	 *      org.tridas.schema.TridasElement, org.tridas.schema.TridasSample,
	 *      org.tridas.schema.TridasRadius, org.tridas.schema.TridasMeasurementSeries)
	 */
	@Override
	public synchronized void registerFile(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries) {
		
		String filename = getDendroFilename(argFile, argProject, argObject, argElement, argSample, argRadius, argSeries);
		registerFile(argFile, filename);
	}
	
	/**
	 * @see org.tridas.io.naming.INamingConvention#registerFile(org.tridas.io.IDendroFile,
	 *      org.tridas.schema.TridasProject, org.tridas.schema.TridasDerivedSeries)
	 */
	@Override
	public synchronized void registerFile(IDendroFile argFile, TridasProject argProject, TridasDerivedSeries argSeries) {
		
		String filename = getDendroFilename(argFile, argProject, argSeries);
		registerFile(argFile, filename);
	}
	
	private synchronized void registerFile(IDendroFile argFile, String argFilename){
		if (argFilename == null) {
			log.error(I18n.getText("fileio.usingDefaultFilename", DEFAULT_FILENAME));
			argFilename = DEFAULT_FILENAME;
		}
		
		if (!nameMap.containsKey(argFilename)) {
			nameMap.put(argFilename, new ArrayList<IDendroFile>());
		}
		
		ArrayList<IDendroFile> files = nameMap.get(argFilename);
		files.add(argFile);
		fileMap.put(argFile, argFilename);
	}
	
	private synchronized boolean unregisterFile(IDendroFile argFile){
		if(argFile == null){
			return false;
		}
		if(!fileMap.containsKey(argFile)){
			return false;
		}
		
		String filename = fileMap.remove(argFile);
		ArrayList<IDendroFile> files = nameMap.get(filename);
		return files.remove(argFile);
	}
	
	protected abstract String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries);
	
	protected abstract String getDendroFilename(IDendroFile argFile, TridasProject argProject,
			TridasDerivedSeries argSeries);
	
	public synchronized void clearRegisteredFiles() {
		nameMap.clear();
		fileMap.clear();
	}
	
	/**
	 * @see org.tridas.io.naming.INamingConvention#getFilename(org.tridas.io.IDendroFile)
	 */
	@Override
	public synchronized String getFilename(IDendroFile argFile) {
		String baseFilename = fileMap.get(argFile);
		ArrayList<IDendroFile> files = nameMap.get(baseFilename);
		if (files == null || files.size() == 0) {
			log.error(I18n.getText("fileio.fileNotRegistered"));
		}
		
		if (files.size() == 1) {
			return baseFilename;
		}
		
		int numDigits = (files.size() + "").length();
		
		int i;
		for (i = 0; i < files.size(); i++) {
			if (files.get(i) == argFile) {
				break;
			}
		}
		if (i == files.size()) {
			log.error(I18n.getText("fileio.fileNotFound", baseFilename));
		}
		i++; // so we start at 1
		return baseFilename + "(" + StringUtils.addLefthandZeros(i, numDigits) + ")";
	}
	
	/**
	 * @see org.tridas.io.naming.INamingConvention#setOverridingFilename(org.tridas.io.IDendroFile, java.lang.String)
	 */
	@Override
	public void setFilename(IDendroFile argFile, String argFilename) {
		unregisterFile(argFile);
		registerFile(argFile, argFilename);
	}
	
	protected static class DendoFileInfo {
		IDendroFile file;
		String filename;
	}
	
	/**
	 * @see org.tridas.io.naming.INamingConvention#getDescription()
	 */
	public abstract String getDescription();
	
	public abstract String getName();
}
