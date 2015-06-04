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
package org.tridas.io.formats.lipdmetadata;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tridas.io.formats.csvmatrix.CSVMatrixFile;
import org.tridas.io.formats.lipd.TridasToLiPDDefaults;
import org.tridas.io.formats.lipd.TridasToLiPDDefaults.DefaultFields;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasValues;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * LiPD format is used to describe a wide range of paleoclimate data.  This variant produces just the 
 * JSON-LD metadata file
 * 
 * @author pbrewer
 *
 */
public class LiPDMetadataFile  extends CSVMatrixFile {
			
	protected TridasToLiPDDefaults defaults;
	protected List<TridasValues> values;

	
	public LiPDMetadataFile(TridasToLiPDDefaults argDefaults, List<TridasValues> values) {
		super();

		defaults = (TridasToLiPDDefaults) argDefaults;
		this.values = values;
	}
	
	@Override
	public String[] saveToString() {

		return this.getJSONFileString().split("\n");
		
	}
	
	@SuppressWarnings("unchecked")
	protected String getJSONFileString()
	{
		JSONObject root = new JSONObject();

		// ROOT
		root.put("@context", "context.jsonld");
		root.put("dataSetName", defaults.getStringDefaultValue(DefaultFields.DATA_SET_NAME).getStringValue());
		root.put("archiveType", "Tree rings");
		root.put("investigators", defaults.getStringDefaultValue(DefaultFields.INVESTIGATORS).getStringValue());
		root.put("dataDOI", defaults.getStringDefaultValue(DefaultFields.DATA_DOI).getStringValue());
		root.put("version", defaults.getStringDefaultValue(DefaultFields.VERSION).getStringValue());		
		
		// FUNDING
		JSONObject funding = new JSONObject();
		funding.put("agency", null);
		funding.put("grant", null);
		root.put("funding", funding);
		
		// PUB
		//JSONObject pub = new JSONObject();
		//JSONObject author = new JSONObject();

		// GEO
		JSONObject geo = new JSONObject();
		geo.put("type", "Feature");
		
		if(defaults.getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue()!=null && defaults.getDoubleDefaultValue(DefaultFields.LATITUDE).getValue()!=null)
		{
		
			JSONObject geometry = new JSONObject();
			geometry.put("type", "Point");
			ArrayList<Double> coords = new ArrayList<Double>();
			coords.add(defaults.getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue());
			coords.add(defaults.getDoubleDefaultValue(DefaultFields.LATITUDE).getValue());
			if(defaults.getDoubleDefaultValue(DefaultFields.ELEVATION).getValue()!=null)
			{
				coords.add(defaults.getDoubleDefaultValue(DefaultFields.ELEVATION).getValue());
			}
			geometry.put("coordinates", coords);
			geo.put("geometry", geometry);
		}
		JSONObject properties = new JSONObject();
		properties.put("siteName", defaults.getStringDefaultValue(DefaultFields.SITE_NAME).getStringValue());
		geo.put("properties", properties);
		root.put("geo", geo);
		
		//PaleoData
		JSONObject paleoData = new JSONObject();
		paleoData.put("paleoDataTableName", "data");
		paleoData.put("filename", "lipd-data.csv");
		JSONArray columns = new JSONArray();
		
		JSONObject col1 = new JSONObject();
		col1.put("number", 1);
		col1.put("parameter", "year");
		col1.put("parameterType", "measured");
		col1.put("description", "calendar year AD");
		col1.put("units", " AD");
		col1.put("datatype", "csvw:NumericFormat");
		columns.add(col1);
		
		int col=1;
		for(TridasValues valuegroup : values)
		{
			col++;
			
			String param = TridasUtils.controlledVocToString(valuegroup.getVariable());
			String units = TridasUtils.controlledVocToString(valuegroup.getUnit());
			
			JSONObject datacol = new JSONObject();
			datacol.put("number", col);
			datacol.put("parameter", param);
			datacol.put("parameterType", "measured");
			datacol.put("units", units);
			datacol.put("datatype", "csvw:NumericFormat");
			columns.add(datacol);	
		}

		paleoData.put("columns", columns);

		root.put("paleoData", paleoData);
		

		// Output JSON
		try {
			
			StringWriter swriter = new StringWriter();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();

			root.writeJSONString(swriter);
			JsonElement je = jp.parse(swriter.getBuffer().toString());
			return gson.toJson(je);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
		
	}
	
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		return "jsonld";
	}
	
}
