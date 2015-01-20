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
import java.io.OutputStream;

import jxl.write.WriteException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.formats.csvmatrix.CSVMatrixFile;

public class OOXMLFile extends CSVMatrixFile {
	
	public OOXMLFile() {
		
	}

	@Override
	public String getExtension() {
		return "xlsx";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		return seriesList.toArray(new ITridasSeries[0]);

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
	 * @throws WriteException
	 */
	public void saveToDisk(OutputStream os) throws IOException {
		
		Workbook workbook = new XSSFWorkbook(); 	
		Sheet dataSheet = workbook.createSheet(I18n.getText("general.data"));
	
		for(int rowind=0; rowind<getMatrix().size(); rowind++)
		{
			String[] row = getMatrix().get(rowind);
			Row rw = dataSheet.createRow(rowind);
			
			for(int colind=0; colind<row.length; colind++)
			{
				rw.createCell(colind).setCellValue(row[colind]);
			}
			
		}
				
		workbook.write(os);
		os.close();
		
	}

}
