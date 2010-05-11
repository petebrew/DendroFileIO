package org.tridas.io.formats.tridas;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.TridasProject;
import org.xml.sax.SAXException;

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
			IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException{
		
		// Build the string array into a FileReader
		StringBuilder fileString = new StringBuilder();
		for(String s : argFileString){
			fileString.append(s+"\n");
		}
		StringReader reader = new StringReader(fileString.toString());
		
		// Validate the file against the TRiDaS schema
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		File schemaLocation = new File("/Users/peterbrewer/dev/sourceforge/tridas/XMLSchema/1.2.1/tridas-1.2.1.xsd");
		Schema schema = null;
		try {
			schema = factory.newSchema(schemaLocation);
		} catch (SAXException e) {
			throw new InvalidDendroFileException(
					I18n.getText("tridas.schemaMissing", 
							e.getLocalizedMessage()), 1);
		}
		Validator validator = schema.newValidator();
		StreamSource source = new StreamSource();
		source.setReader(reader);
		try{
			validator.validate(source);
		}catch (SAXException ex) {
			throw new InvalidDendroFileException(
					I18n.getText("tridas.schemaException", 
							ex.getLocalizedMessage()), 1);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new InvalidDendroFileException(
					I18n.getText("tridas.schemaException", 
							e.getLocalizedMessage()), 1);
		}
		
		
		// All is ok so now unmarshall to Java classes
		JAXBContext jc;
		reader = new StringReader(fileString.toString());
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
