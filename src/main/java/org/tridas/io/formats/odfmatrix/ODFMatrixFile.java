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
import java.io.OutputStream;
import java.util.ArrayList;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.tridas.io.I18n;
import org.tridas.io.formats.csvmatrix.CSVMatrixFile;

public class ODFMatrixFile extends CSVMatrixFile {
	

	
	public ODFMatrixFile() {
		
	}
	

	
	@Override
	public String getExtension() {
		return "ods";
	}
		
	@Override
	public String[] saveToString() {
		
		throw new UnsupportedOperationException(I18n.getText("fileio.binaryAsStringUnsupported"));
		
	}
	
	/**
	 * An alternative to the normal saveToString() as this is a binary format
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void saveToDisk(OutputStream os) throws IOException {
		OdfSpreadsheetDocument outputDocument;

		
		
		try {
			outputDocument = OdfSpreadsheetDocument.newSpreadsheetDocument();
			
			OdfTable table; 
			
			table = outputDocument.getTableByName("Sheet1");
			table.setTableName(I18n.getText("general.data"));

			ArrayList<String[]> matrix = getMatrix();
			
			for(int rowind=0; rowind<matrix.get(0).length; rowind++)
			{
				for(int colind=0; colind<matrix.size(); colind++)
				{
					if(matrix.get(colind)[rowind]!=null )
					{
						table.getCellByPosition(colind, rowind).setStringValue(matrix.get(colind)[rowind]);
					}

				}
			}
			
			
			/*for(int rowind=0; rowind<getMatrix().size(); rowind++)
			{
				String[] row = getMatrix().get(rowind);
				for(int colind=0; colind<row.length; colind++)
				{
					table.getCellByPosition(colind, rowind).setStringValue(row[colind]);
				}
				
			}*/
						
			outputDocument.save(os);
			
			
			
		} catch (Exception e) {

			throw new IOException(e.getLocalizedMessage());
		}
		
	}
}
