package org.tridas.io.formats.tridas;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.util.IOUtils;
import org.tridas.schema.TridasProject;
import org.xml.sax.SAXException;

/**
 * Reader for the TRiDaS file format. This is little more than a
 * wrapper around the JaXB unmarshaller
 * 
 * @see org.tridas.io.formats.tridas
 * @author peterbrewer
 */
public class TridasReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(TridasReader.class);
	
	private TridasProject project = null;
	private TridasMetadataFieldSet defaults = null;
	
	public TridasReader() {
		super(TridasMetadataFieldSet.class);
	}
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		defaults = (TridasMetadataFieldSet) argDefaultFields;
		// Build the string array into a FileReader
		StringBuilder fileString = new StringBuilder();
		for (String s : argFileString) {
			fileString.append(s + "\n");
		}
		StringReader reader = new StringReader(fileString.toString());
		// Validate the file against the TRiDaS schema
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL file = IOUtils.getFileInJarURL("tridas.xsd");
		Schema schema = null;
		try {
			schema = factory.newSchema(file);
		} catch (Exception e) {
			// if we can't find the schema it's ok, doesn't mean it's not an invalid
			// dendro file
			log.error(I18n.getText("tridas.schemaMissing", e.getLocalizedMessage()));
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("tridas.schemaMissing", e
					.getLocalizedMessage())));
		}
		Validator validator = schema.newValidator();
		StreamSource source = new StreamSource();
		source.setReader(reader);
		try {
			validator.validate(source);
		} catch (SAXException ex) {
			throw new InvalidDendroFileException(I18n.getText("tridas.schemaException", ex.getLocalizedMessage()), 1);
			
		} catch (IOException e) {
			throw new InvalidDendroFileException(I18n.getText("tridas.schemaException", e.getLocalizedMessage()), 1);
		}
		
		// All is ok so now unmarshall to Java classes
		JAXBContext jc;
		reader = new StringReader(fileString.toString());
		try {
			jc = JAXBContext.newInstance("org.tridas.schema");
			Unmarshaller u = jc.createUnmarshaller();
			// Read the file into the project
			project = (TridasProject) u.unmarshal(reader);
		} catch (JAXBException e2) {
			addWarning(new ConversionWarning(WarningType.DEFAULT, I18n.getText("fileio.loadfailed")));
		}
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"xml"};
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
		return defaults;
	}
	
	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tridas.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tridas.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tridas.about.shortName");
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		project = null;
		defaults = null;
	}
}
