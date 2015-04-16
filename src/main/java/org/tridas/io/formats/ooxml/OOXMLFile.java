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
import java.util.ArrayList;

import jxl.write.WriteException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
	
		ArrayList<String[]> matrix = getMatrix();
				
		for(int rowind=0; rowind<matrix.get(0).length; rowind++)
		{
			Row rw = dataSheet.createRow(rowind);
			
			for(int colind=0; colind<matrix.size(); colind++)
			{
				
				if(matrix.get(colind)[rowind]==null || matrix.get(colind)[rowind].getBytes().length==0) continue;
				
				if(rowind==0)
				{
					rw.createCell(colind).setCellValue(matrix.get(colind)[rowind]);

				}
				else if(colind==0)
				{
					rw.createCell(colind).setCellValue(Integer.valueOf(matrix.get(colind)[rowind]));

				}
				else 
				{
					try{
						rw.createCell(colind).setCellValue(Double.valueOf(matrix.get(colind)[rowind]));
					} catch (Exception e)
					{
						try{
							rw.createCell(colind).setCellValue(Integer.valueOf(matrix.get(colind)[rowind]));
						} catch (Exception e2)
						{
							rw.createCell(colind).setCellValue(matrix.get(colind)[rowind]);
						}
					}
				}
				
				
			}
			
		}
		
		Sheet metadataSheet = workbook.createSheet(I18n.getText("general.metadata"));
	
		matrix = this.getMetadataMatrix();
				
		for(int rowind=0; rowind<matrix.get(0).length; rowind++)
		{
			Row rw = metadataSheet.createRow(rowind);
			
			for(int colind=0; colind<matrix.size(); colind++)
			{
				
				if(matrix.get(colind)[rowind]==null || matrix.get(colind)[rowind].getBytes().length==0) continue;
				
				if(rowind==0)
				{
					rw.createCell(colind).setCellValue(matrix.get(colind)[rowind]);

				}
				else 
				{
					try{
						rw.createCell(colind).setCellValue(Double.valueOf(matrix.get(colind)[rowind]));
					} catch (Exception e)
					{
						try{
							rw.createCell(colind).setCellValue(Integer.valueOf(matrix.get(colind)[rowind]));
						} catch (Exception e2)
						{
							rw.createCell(colind).setCellValue(matrix.get(colind)[rowind]);
						}
					}
				}
				
				
			}
			
		}
				
		workbook.write(os);
		
		
		os.close();
		
	}

}
