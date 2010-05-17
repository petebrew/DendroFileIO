package org.tridas.io.util;

import java.util.HashMap;

/**
 * Class for converting between ITRDB 4 char taxon code
 * and proper latin name.
 * 
 * @author sbr00pwb
 */
public class ITRDBTaxonConverter {

	private static HashMap<String, String> convertionMap = null;
	private static HashMap<String, String> convertionMapToCode = null;


	private ITRDBTaxonConverter(){}
	
	public static String getNameFromCode(String argCode){
		if(convertionMap == null){
			initializeMap();
		}
		if(convertionMap.containsKey(argCode)){
			return convertionMap.get(argCode);
		}else{
			return argCode;
		}
	}
	
	private static void initializeMap(){
		FileHelper fh = new FileHelper();
		String[] file = fh.loadStrings("org/tridas/io/util/spmap.txt");
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> map2 = new HashMap<String, String>();
		for( String s: file){
			String key = s.substring(0, s.indexOf(" "));
			String value = s.substring(s.indexOf(" ")).trim();
			if(key != null && value != null){
				map.put(key, value);
				map2.put(value, key);
			}
		}
		convertionMap = map;
		convertionMapToCode = map2;
	}

	public static String getCodeFromName(String argName) {
		if(convertionMapToCode == null){
			initializeMap();
		}
		
		if(convertionMapToCode.containsKey(argName)){
			return convertionMapToCode.get(argName);
		}else{
			return argName;
		}
	}	
	
}
