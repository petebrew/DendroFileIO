package org.tridas.io.formats.catras;

import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.schema.TridasObject;


public class CatrasToTridasDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet {

	
	// example of customizing
	@Override
	protected TridasObject getDefaultTridasObject(){
		TridasObject object = super.getDefaultTridasObject();
		object.setComments("Converted from Tucson file");
		return object;
	}
}
