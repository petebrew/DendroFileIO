package org.tridas.io.formats.heidelberg;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.schema.TridasProject;

public class HeidelbergToTridasDefaults extends TridasMetadataFieldSet {
	
	public void initDefaultValues(){
		super.initDefaultValues();
		
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasProject()
	 */
	@Override
	protected TridasProject getDefaultTridasProject() {
		TridasProject p = super.getDefaultTridasProject();
		p.setComments("Converted from Heidelberg file.");
		return p;
	}
}
