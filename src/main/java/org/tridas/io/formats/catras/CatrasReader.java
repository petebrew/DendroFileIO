package org.tridas.io.formats.catras;

import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.schema.TridasProject;

public class CatrasReader extends AbstractDendroFileReader {

	public CatrasReader(Class<? extends IMetadataFieldSet> argDefaultFieldsClass) {
		super(argDefaultFieldsClass);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getFileExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TridasProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

}
