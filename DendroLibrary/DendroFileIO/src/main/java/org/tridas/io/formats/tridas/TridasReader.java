package org.tridas.io.formats.tridas;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.TridasProject;

/**
 * Reader for the TRiDaS file format.  This is little more than a
 * wrapper around the JaXB unmarshaller
 * 
 * @see org.tridas.io.formats.tridas
 * @author peterbrewer
 */
public class TridasReader extends AbstractDendroFileReader {

	private TridasProject project = null;
	
	
	public TridasReader() {
		super("tridas", TridasMetadataFieldSet.class);
	}
	
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields) {
		
		StringBuilder fileString = new StringBuilder();
		for(String s : argFileString){
			fileString.append(s+"\n");
		}
		StringReader reader = new StringReader(fileString.toString());
		
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance( "org.tridas.schema" );
			Unmarshaller u = jc.createUnmarshaller();
			// Read the file into the project
			project = (TridasProject) u.unmarshal(reader);
		} catch (JAXBException e2) {
			addWarningToList(new ConversionWarning(WarningType.DEFAULT, I18n.getText("fileio.loadfailed")));
		} 
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[] {"xml"};
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getProject()
	 */
	@Override
	public TridasProject getProject() {
		return project;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return null;
	}

	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}
}
