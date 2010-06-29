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
package org.tridas.io.util;

import java.util.HashMap;

import org.tridas.schema.ControlledVoc;

/**
 * Class for converting between ITRDB 4 char taxon code
 * and proper latin name.
 * 
 * @author sbr00pwb
 */
public class ITRDBTaxonConverter {
	
	private static HashMap<String, String> convertionMap = null;
	private static HashMap<String, String> convertionMapToCode = null;
	private static String defaultCode = "UNKN";
	private static String defaultTaxon = "Plantae";
	private static String defaultDictionary = "ITRDB/WSL Dendrochronology Species Database";
	
	private ITRDBTaxonConverter() {}
	
	private static void initializeMap() {
		FileHelper fh = new FileHelper();
		String[] file = fh.loadStrings("spmap.csv");
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> map2 = new HashMap<String, String>();
		for (String s : file) {
			String key = s.substring(0, s.indexOf(","));
			String value = s.substring(s.indexOf(",") + 1).trim();
			if (key != null && value != null) {
				map.put(key, value);
				map2.put(value, key);
			}
		}
		convertionMap = map;
		convertionMapToCode = map2;
	}
	
	/**
	 * Check that the specified code is in the dictionary.  If not then
	 * return the default standardised code.
	 * 
	 * @param argCode
	 * @return
	 */
	public static String getNormalisedCode(String argCode)
	{
		if (convertionMap == null) {
			initializeMap();
		}
		if (convertionMap.containsKey(argCode)) {
			return argCode;
		}
		else {
			return defaultCode;
		}
	}
	
	/**
	 * Get the full latin name of a taxon from the ITRDB 4 letter
	 * code. If code is not found, then return code unchanged.
	 * 
	 * @param argCode
	 * @return
	 */
	public static String getNameFromCode(String argCode) {
		
		argCode = argCode.toUpperCase();
		if (convertionMap == null) {
			initializeMap();
		}
		if (convertionMap.containsKey(argCode)) {
			return convertionMap.get(argCode);
		}
		else {
			return argCode;
		}
	}
	
	/**
	 * Get the four letter code for this latin name. Note that this is
	 * a simple text string match so the latin name must be precisely
	 * the same (including authority) to get a hit.
	 * 
	 * @param argName
	 * @return
	 */
	public static String getCodeFromName(String argName) {
		if (convertionMapToCode == null) {
			initializeMap();
		}
		
		if (convertionMapToCode.containsKey(argName)) {
			return convertionMapToCode.get(argName);
		}
		else {
			return argName;
		}
	}
	
	/**
	 * Get a controlled vocabulary version of this taxon from
	 * the 4 letter code. If no match is found a simple tag is
	 * returned with no vocab info.
	 * 
	 * @param code
	 * @return
	 */
	public static ControlledVoc getControlledVocFromCode(String code) {
		
		ControlledVoc taxon = new ControlledVoc();
		
		if (code == null) {
			code = defaultCode;
		}
		else if (code.equals("")) {
			code = defaultCode;
		}
		else
		{
			code = code.toUpperCase();
		}
		
		if (ITRDBTaxonConverter.getNameFromCode(code) != code) {
			// Match found so set controlled vocab
			taxon.setNormalStd(defaultDictionary);
			taxon.setNormalId(code);
			taxon.setNormal(ITRDBTaxonConverter.getNameFromCode(code));
			taxon.setValue(ITRDBTaxonConverter.getNameFromCode(code));
		}
		else {
			// Not match so make simple tag with supplied value
			taxon.setValue(code);
		}
		
		return taxon;
		
	}
	
	/**
	 * Get a controlled vocabulary version of this taxon from the
	 * full latin name. If no match is found a simple tag is
	 * returned with no vocab info.
	 * 
	 * @param name
	 * @return
	 */
	public static ControlledVoc getControlledVocFromName(String name) {
		
		ControlledVoc taxon = new ControlledVoc();
		
		if (name.equals("") || name == null) {
			name = defaultTaxon;
		}
		
		if (ITRDBTaxonConverter.getCodeFromName(name) != name) {
			// Match found so set controlled vocab
			taxon.setNormalStd(defaultDictionary);
			taxon.setNormalId(ITRDBTaxonConverter.getCodeFromName(name));
			taxon.setNormal(name);
			taxon.setValue(name);
		}
		else {
			// Not match so make simple tag with supplied value
			taxon.setValue(name);
		}
		
		return taxon;
		
	}
	
}
