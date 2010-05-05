package org.tridas.io.formats.vformat;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.besancon.BesanconReader;
import org.tridas.io.formats.besancon.BesanconToTridasDefaults;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

public class VFormatReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(VFormatReader.class);
	// defaults given by user
	private VFormatToTridasDefaults defaults = new VFormatToTridasDefaults();
	
	public VFormatReader() {
		super(VFormatToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		// TODO Auto-generated method stub

	}

	@Override
	public IMetadataFieldSet getDefaults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getFileExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TridasProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

}
