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
package org.tridas.io.formats.ooxml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.FileHelper;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;

public class OOXMLWriter extends AbstractDendroCollectionWriter {
	private static final Logger log = LoggerFactory.getLogger(OOXMLWriter.class);
	
	IMetadataFieldSet defaults;
	INamingConvention naming = new NumericalNamingConvention();
	
	public OOXMLWriter() {
		super(TridasToOOXMLDefaults.class, new OOXMLFormat());
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		defaults = argDefaults;
		
		ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
		
		// Grab all derivedSeries from project
		try {
			List<TridasDerivedSeries> lst = argProject.getDerivedSeries();
			for (TridasDerivedSeries ds : lst) {
				// add to list
				seriesList.add(ds);
			}
		} catch (NullPointerException e) {}
		
		try {
			List<TridasMeasurementSeries> lst = TridasUtils.getMeasurementSeriesFromTridasProject(argProject);
			for (TridasMeasurementSeries ser : lst) {
				// add to list
				seriesList.add(ser);
			}
		} catch (NullPointerException e) {}
		
		// No series found
		if (seriesList.size() == 0) {
			clearWarnings();
			throw new ImpossibleConversionException(I18n.getText("fileio.noData"));
		}
		
		OOXMLFile file = new OOXMLFile(argDefaults);
		
		file.setSeriesList(seriesList);
		addToFileList(file);
		naming.registerFile(file, argProject, null);
		
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
		
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}
	
	@Override
	public void setNamingConvention(INamingConvention argConvention) {
		naming = argConvention;
	}
	
	@Override
	public void saveFileToDisk(String argOutputFolder, String argFilename, IDendroFile argFile) {
		
		FileHelper helper;
		
		boolean absolute = argOutputFolder.startsWith("/");
		
		if (absolute) {
			helper = new FileHelper(argOutputFolder);
		}
		else {
			helper = new FileHelper();
			argFilename = argOutputFolder + argFilename;
		}
		
		try {
			((OOXMLFile) argFile).saveToDisk(helper.createOutput(argFilename + "." + argFile.getExtension()));
		} catch (IOException e) {
			log.error("Error saving file to disk", e);
		}
	}
}
