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


	private ITRDBTaxonConverter(){}
		
	private static void initializeMap(){
		FileHelper fh = new FileHelper();
		String[] file = fh.loadStrings("spmap.csv");
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> map2 = new HashMap<String, String>();
		for( String s: file){
			String key = s.substring(0, s.indexOf(","));
			String value = s.substring(s.indexOf(",")+1).trim();
			if(key != null && value != null){
				map.put(key, value);
				map2.put(value, key);
			}
		}
		convertionMap = map;
		convertionMapToCode = map2;
	}

	/**
	 * Get the full latin name of a taxon from the ITRDB 4 letter
	 * code.  If code is not found, then return code unchanged. 
	 * 
	 * @param argCode
	 * @return
	 */
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
	
	/**
	 * Get the four letter code for this latin name.  Note that this is
	 * a simple text string match so the latin name must be precisely 
	 * the same (including authority) to get a hit. 
	 * 
	 * @param argName
	 * @return
	 */
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
	
	/**
	 * Get a controlled vocabulary version of this taxon from
	 * the 4 letter code.  If no match is found a simple tag is
	 * returned with no vocab info.
	 * 
	 * @param code
	 * @return
	 */
	public static ControlledVoc getControlledVocFromCode(String code){
		
		ControlledVoc taxon = new ControlledVoc();
		
		if(code==null)	
		{
			code = "UNKN";
		}
		else if (code.equals(""))
		{
			code = "UNKN";
		}
		
		if(ITRDBTaxonConverter.getNameFromCode(code)!=code)
		{
			// Match found so set controlled vocab
			taxon.setNormalStd("ITRDB/WSL Dendrochronology Species Database");
			taxon.setNormalId(code);
			taxon.setNormal(ITRDBTaxonConverter.getNameFromCode(code));
			taxon.setValue(ITRDBTaxonConverter.getNameFromCode(code));
		}
		else
		{
			// Not match so make simple tag with supplied value
			taxon.setValue(code);
		}
		
		return taxon;
		
	}
	
	/**
	 * Get a controlled vocabulary version of this taxon from the 
	 * full latin name.  If no match is found a simple tag is 
	 * returned with no vocab info.
	 * 
	 * @param name
	 * @return
	 */
	public static ControlledVoc getControlledVocFromName(String name){
		
		ControlledVoc taxon = new ControlledVoc();
		
		if(name.equals("") || name==null)
		{
			name = "Plantae";
		}
		
		if(ITRDBTaxonConverter.getCodeFromName(name)!=name)
		{
			// Match found so set controlled vocab
			taxon.setNormalStd("ITRDB/WSL Dendrochronology Species Database");
			taxon.setNormalId(ITRDBTaxonConverter.getCodeFromName(name));
			taxon.setNormal(name);
			taxon.setValue(name);
		}
		else
		{
			// Not match so make simple tag with supplied value
			taxon.setValue(name);
		}
		
		return taxon;
		
	}
	
}
