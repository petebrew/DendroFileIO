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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.formats.csvmatrix.CSVMatrixFile;

public class CSVMetadataFile extends CSVMatrixFile {
	
	private static final Logger log = LoggerFactory.getLogger(CSVMetadataFile.class);

	
	public CSVMetadataFile() {
		
	}
	

	
	@Override
	public String getExtension() {
		return "csv";
	}
		
	@Override
	public String[] saveToString() {
			
		
			
			ArrayList<String[]> matrix = getMetadataMatrix();
			
			String[] lines = new String[matrix.get(0).length];
			
			
			for(int i=0; i< lines.length; i++)
			{
				String line = "";
				for(String[] column : matrix)
				{
					if(column[i]==null)
					{
						line += ",";
					}
					else
					{
						line += column[i]+",";
					}
				}
				lines[i] = line.substring(0, line.length()-1);
			}
			
			
			return lines;
		
	}
	
}
