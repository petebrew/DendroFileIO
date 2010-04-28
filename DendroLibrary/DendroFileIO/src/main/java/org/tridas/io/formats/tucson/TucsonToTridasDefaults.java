package org.tridas.io.formats.tucson;

import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.schema.TridasObject;

/**
 * here for the library user to create and pass in the loadFile() arguments
 * @author Daniel
 *
 */
public class TucsonToTridasDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet {

	
	// example of customizing
	@Override
	protected TridasObject getDefaultTridasObject(){
		TridasObject object = super.getDefaultTridasObject();
		object.setComments("Converted from Tucson file");
		return object;
	}
}
