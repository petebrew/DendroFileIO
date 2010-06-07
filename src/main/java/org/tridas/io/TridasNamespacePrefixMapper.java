package org.tridas.io;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Provides user friendly namespace prefixes to in our XML
 * 
 * @author peterbrewer
 */
public class TridasNamespacePrefixMapper extends NamespacePrefixMapper {
	
	private final static String SCHEMAS_USED[][] = {
			// Order is important!
			// namespace, filename, prefix
			{"http://www.w3.org/1999/xlink", "xlinks.xsd", "xlink"},
			{"http://www.opengis.net/gml", "gmlsf.xsd", "gml"},
			{"http://www.tridas.org/1.2.1", "tridas.xsd", "tridas"},};
	
	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		String[][] schemas = TridasNamespacePrefixMapper.SCHEMAS_USED;
		
		for (String[] schema : schemas) {
			if (schema[0].equals(namespaceUri)) {
				return schema[2];
			}
		}
		
		return suggestion;
	}
	
}
