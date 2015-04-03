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
package org.tridas.io.formats.csvmetadatamatrix;

import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.csvmatrix.CSVMatrixWriter;
import org.tridas.io.formats.csvmatrix.TridasToMatrixDefaults;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;

public class CSVMetadataWriter extends CSVMatrixWriter {
	
	IMetadataFieldSet defaults;
	INamingConvention naming = new NumericalNamingConvention();
	
	public CSVMetadataWriter() {
		super(TridasToMatrixDefaults.class, new CSVMetadataFormat());
		setSkipDataParsing(true);
		clazz = CSVMetadataFile.class;
	}
	


}
