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
package org.tridas.io.formats.lipd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jxl.write.WriteException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.lipd.TridasToLiPDDefaults.DefaultFields;
import org.tridas.io.formats.lipdmetadata.LiPDMetadataFile;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasValues;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * LiPD format is used to describe a wide range of paleoclimate data.  
 * 
 * @author pbrewer
 *
 */
public class LiPDFile  extends LiPDMetadataFile {
	
		
	public LiPDFile(TridasToLiPDDefaults argDefaults, List<TridasValues> values) {
		super(argDefaults, values);
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
	public void saveToDisk(OutputStream os) throws IOException, WriteException {
		
		  // Create a buffer for reading the files
		  byte[] buf = new byte[1024];
		  
		  // Create the ZIP file
	      ZipOutputStream out = new ZipOutputStream(os);
	   
	      // Compress the files

          // Create JSON File
          out.putNextEntry(new ZipEntry("LiPD.jsonld"));
          String json = getJSONFileString();
          ByteArrayInputStream in =  new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));    
          // Transfer bytes from the file to the ZIP file
          int len;
          while ((len = in.read(buf)) > 0) {
              out.write(buf, 0, len);
          }
          // Complete the entry
          out.closeEntry();
          in.close();
          
          
          // Create CSV File
          out.putNextEntry(new ZipEntry("lipd-data.csv"));
          String[] matrix = flipMatrix();
          String data = "";
          
          // Remove header
          for (int i=1; i<matrix.length; i++)
          {
        	  data += matrix[i] + "\n";
          }
          in =  new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));    
          // Transfer bytes from the file to the ZIP file
          while ((len = in.read(buf)) > 0) {
              out.write(buf, 0, len);
          }
          // Complete the entry
          out.closeEntry();
          in.close();
          

        // Complete the ZIP file
        out.close();

	}
	
	private String[] flipMatrix() {
		
		String[] lines = new String[fileYearRange.span()+1];
		
		ArrayList<String[]> matrix = getMatrix();
		
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
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		return "lpd";
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		return null;
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	

}
