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
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Column;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.I18n;
import org.tridas.io.formats.csvmatrix.CSVMatrixFile;

public class ODFMatrixFile extends CSVMatrixFile {
	
	private static final Logger log = LoggerFactory.getLogger(ODFMatrixFile.class);

	
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
		SpreadsheetDocument outputDocument;

		try {
			outputDocument = SpreadsheetDocument.newSpreadsheetDocument();
			
			Table table; 
			
			table = outputDocument.appendSheet(I18n.getText("general.data"));

			ArrayList<String[]> matrix = getMatrix();
			
			for(int rowind=0; rowind<matrix.get(0).length; rowind++)
			{			
				Row therow = table.getRowByIndex(rowind);
				
				for(int colind=0; colind<matrix.size(); colind++)
				{									
					if(matrix.get(colind)[rowind]==null || matrix.get(colind)[rowind].getBytes().length==0) continue;
					
					Cell cell = therow.getCellByIndex(colind);

					if(rowind==0)
					{		

						therow.setHeight(12, false);
						cell.setStringValue(matrix.get(colind)[rowind]);
						//cell.setCellBackgroundColor(Color.AQUA);
						cell.setTextWrapped(true);
						Column thecol = table.getColumnByIndex(colind);
						thecol.setWidth(40);
					}
					else 
					{
						//cell.setCellBackgroundColor(Color.WHITE);

						try{
							therow.getCellByIndex(colind).setDoubleValue(Double.valueOf(matrix.get(colind)[rowind]));
						} catch (Exception e)
						{
							therow.getCellByIndex(colind).setStringValue(matrix.get(colind)[rowind]);
						}
					}
					
					
					
				}
			}
			
			Table table2; 
			
			table2 = outputDocument.appendSheet(I18n.getText("general.metadata"));
			matrix = getMetadataMatrix();
			
			for(int rowind=0; rowind<matrix.get(0).length; rowind++)
			{			
				Row therow = table2.getRowByIndex(rowind);
				
				for(int colind=0; colind<matrix.size(); colind++)
				{									
					if(matrix.get(colind)[rowind]==null || matrix.get(colind)[rowind].getBytes().length==0) continue;
					
					Cell cell = therow.getCellByIndex(colind);

					if(rowind==0)
					{		

						therow.setHeight(12, false);
						cell.setStringValue(matrix.get(colind)[rowind]);
						//cell.setCellBackgroundColor(Color.AQUA);
						cell.setTextWrapped(true);
						Column thecol = table2.getColumnByIndex(colind);
						thecol.setWidth(40);
					}
					else 
					{
						//cell.setCellBackgroundColor(Color.WHITE);

						try{
							therow.getCellByIndex(colind).setDoubleValue(Double.valueOf(matrix.get(colind)[rowind]));
						} catch (Exception e)
						{
							therow.getCellByIndex(colind).setStringValue(matrix.get(colind)[rowind]);
						}
					}
					
					
					
				}
			}
			
			outputDocument.removeSheet(0);
						
			outputDocument.save(os);

			
		} catch (Exception e) {

			throw new IOException(e.getLocalizedMessage());
		}
		
	}
}
