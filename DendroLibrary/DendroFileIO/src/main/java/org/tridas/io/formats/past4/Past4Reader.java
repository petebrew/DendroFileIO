package org.tridas.io.formats.past4;

import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults;
import org.tridas.schema.TridasProject;

public class Past4Reader extends AbstractDendroFileReader {

	public Past4Reader() {
		super(Past4ToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getFileExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TridasProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void resetReader() {
		// TODO Auto-generated method stub

	}

}
