package org.tridas.io.formats.besancon;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.catras.CatrasToTridasDefaults;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

public class BesanconReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(BesanconReader.class);
	// defaults given by user
	private BesanconToTridasDefaults defaults = new BesanconToTridasDefaults();
	
	public BesanconReader() {
		super(BesanconToTridasDefaults.class);
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

	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] {"txt"};
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getName()
	 */
	@Override
	public String getName() {
		return "Besancon";
	}

	@Override
	public TridasProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

}
