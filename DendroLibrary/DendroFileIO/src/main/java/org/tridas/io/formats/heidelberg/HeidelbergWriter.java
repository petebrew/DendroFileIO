package org.tridas.io.formats.heidelberg;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.schema.TridasProject;

public class HeidelbergWriter extends AbstractDendroCollectionWriter {

	IMetadataFieldSet defaults;

	
	public HeidelbergWriter(){
		super("heidelberg", TridasToHeidelbergDefaults.class);
	}
	
	public HeidelbergWriter(Class<? extends IMetadataFieldSet> argDefaultFieldsClass) {
		super("heidelberg", argDefaultFieldsClass);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getNamingConvention()
	 */
	@Override
	public INamingConvention getNamingConvention() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getName()
	 */
	@Override
	public String getName() {
		return "Heidelberg";
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		// TODO Auto-generated method stub
		return null;
	}

}
