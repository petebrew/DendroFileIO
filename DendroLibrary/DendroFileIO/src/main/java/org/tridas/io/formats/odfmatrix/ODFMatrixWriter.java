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
package org.tridas.io.formats.odfmatrix;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.csvmatrix.CSVMatrixWriter;
import org.tridas.io.formats.csvmatrix.TridasToMatrixDefaults;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.FileHelper;

public class ODFMatrixWriter extends CSVMatrixWriter {
	private static final Logger log = LoggerFactory.getLogger(ODFMatrixWriter.class);
	
	IMetadataFieldSet defaults;
	INamingConvention naming = new NumericalNamingConvention();
	
	public ODFMatrixWriter() {
		super(TridasToMatrixDefaults.class, new ODFMatrixFormat());
		clazz = ODFMatrixFile.class;
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
			((ODFMatrixFile) argFile).saveToDisk(helper.createOutput(argFilename + "." + argFile.getExtension()));
		} catch (IOException e) {
			log.error("Error saving file to disk", e);
		}
	}

}
