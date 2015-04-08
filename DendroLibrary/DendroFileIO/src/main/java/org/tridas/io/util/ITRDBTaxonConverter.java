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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.schema.ControlledVoc;

/**
 * Class for converting between ITRDB 4 char taxon code
 * and proper latin name.
 * 
 * @author sbr00pwb
 */
public class ITRDBTaxonConverter {
	
	private static HashMap<String, String> codeToNameMap = null;
	private static HashMap<String, String> nameToCodeMap = null;
	private static HashMap<String, String> namenoauthToCodeMap = null;
	private static String defaultCode = "UNKN";
	private static String defaultTaxon = "Plantae";
	private static String defaultDictionary = "ITRDB/WSL Dendrochronology Species Database";
	
	private ITRDBTaxonConverter() {}
	
	
	private static void initializeMap() {
		FileHelper fh = new FileHelper();
		String[] file = fh.loadStrings("spmap.csv");
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> map2 = new HashMap<String, String>();
		HashMap<String, String> map3 = new HashMap<String, String>();

		for (String s : file) {
			String key = s.substring(0, s.indexOf(","));
			String value = s.substring(s.indexOf(",") + 1).trim();
			
			int secondspacepos = org.apache.commons.lang.StringUtils.ordinalIndexOf(value, " ", 2);
			
			if(secondspacepos>0)
			{
				map3.put(value.substring(0, secondspacepos), key);
			}
			
			if (key != null && value != null) {
				map.put(key, value);
				map2.put(value, key);
			}
		}
		codeToNameMap = map;
		nameToCodeMap = map2;
		namenoauthToCodeMap = map3;
	}
	
	/**
	 * Returns a ControlledVoc of the best match from the ITRDB taxon dictionary.  Takes a string ArrayList
	 * of possible search terms which can be codes and/or full names.  If conflicting search terms are 
	 * provided (e.g. QUER, Pinus sylvestris) then it returns null.  If no matches are found then it returns null.
	 * 
	 * @param searchStrings
	 * @return
	 */
	public static ControlledVoc getBestSpeciesMatch(ArrayList<String> searchStrings) throws ConversionWarningException
	{
		if (searchStrings==null || searchStrings.size()==0) return ITRDBTaxonConverter.getControlledVocFromCode("UNKN");
		
		HashSet<String> codelist = new HashSet<String>();

		for(String str : searchStrings)
		{
			if(str==null) continue;
			if(str.length()==0) continue;
		
			if(!getNameFromCode(str).equals(str.toUpperCase()))
			{
				codelist.add(str);
			}
			else if (!getCodeFromName(str).equals(str))
			{
				codelist.add(getCodeFromName(str));
			}
		}
		
		if(codelist.size()==0) 
		{
			// None of the search strings matched 
			if(searchStrings.size()==1)
			{
				// Only one string passed to this function so just return as an non-standard string
				return getControlledVocFromName(searchStrings.get(0));
			}
			else
			{
				// Multiple strings passed so we have no way of knowing which is valid
				throw new ConversionWarningException(new ConversionWarning(WarningType.AMBIGUOUS, "Ambiguous taxon information supplied."));
			}
		}
		else if(codelist.size()==2 && codelist.contains("UNKN"))
		{
			// List contains 2 values, one of which is UNKN.  Just remove it and use the other
			codelist.remove("UNKN");
		}
		else if(codelist.size()>1)
		{
			throw new ConversionWarningException(new ConversionWarning(WarningType.AMBIGUOUS, "Ambiguous taxon information supplied."));
		}
		
		Iterator<String> itr = codelist.iterator();
		return getControlledVocFromCode(itr.next());
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
		if (codeToNameMap == null) {
			initializeMap();
		}
		if (codeToNameMap.containsKey(argCode)) {
			return argCode.toUpperCase();
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
		
		if(argCode==null) return null;
		
		//Upper case the code
		argCode = argCode.toUpperCase();
		
		if (codeToNameMap == null) {
			initializeMap();
		}
		if (codeToNameMap.containsKey(argCode)) {
			return codeToNameMap.get(argCode);
		}
		else {
			return argCode;
		}
	}
	
	/**
	 * Creates a taxon ControlledVoc from the given string.  The string
	 * should be either an ITRDB code or a species name.  
	 * 
	 * @param str
	 * @return
	 */
	public static ControlledVoc getControlledVocFromString(String str)
	{
		if(str.length()==4)
		{
			// String is 4 chars long so probably an ITRDB code
			ControlledVoc cv = getControlledVocFromCode(str);
			if(cv.isSetNormal())
			{
				// A controlled vocab has been returned so all is well
				return cv;
			}
			else
			{
				// Non-standardised response so lets try again as if its a name
				return getControlledVocFromName(str);
			}
		}
		else
		{
			// Probably a taxon name
			return getControlledVocFromName(str);
		}
	}
	
	/**
	 * Get the four letter code for this latin name. Note that this is
	 * a simple text string match so the latin name must be precisely
	 * the same to get a hit.  However, names can be with or without 
	 * authority 
	 * 
	 * @param argName
	 * @return
	 */
	public static String getCodeFromName(String argName) {
		if (nameToCodeMap == null) {
			initializeMap();
		}
		
		if (nameToCodeMap.containsKey(argName)) {
			return nameToCodeMap.get(argName);
		}
		else if (namenoauthToCodeMap.containsKey(argName))
		{
			return namenoauthToCodeMap.get(argName);
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
	public static ControlledVoc getControlledVocFromCode(String incode) {
		
		ControlledVoc taxon = new ControlledVoc();
		String code;
		if (incode == null) {
			code = defaultCode;
		}
		else if (incode.equals("")) {
			code = defaultCode;
		}
		else
		{
			code = incode.toUpperCase();
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
			taxon.setValue(incode);
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
		
		if (name == null || name.equals("") ) {
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
