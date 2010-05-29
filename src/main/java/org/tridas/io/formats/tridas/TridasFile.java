package org.tridas.io.formats.tridas;

import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;
import org.tridas.io.TridasNamespacePrefixMapper;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.IOUtils;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.TridasProject;
import org.xml.sax.SAXException;


/**
 * The TRiDaS file format is our standard format 
 * 
 * <h3>Reference</h3>
 *
 * <p>
 * See the <a href="http://www.tridas.org">Tree Ring Data Standard</a> website
 * for futher information.  
 * </p>
 * 
 * @author peterbrewer
 *
 */
public class TridasFile implements IDendroFile {

	private final static SimpleLogger log = new SimpleLogger(TridasFile.class);
	
	TridasProject project;

	private IMetadataFieldSet defaults;
	
	public TridasFile(IMetadataFieldSet argDefaults) {
		defaults = argDefaults;
	}
	
	public void setProject(TridasProject p){
		project = p;
	}
	
	@Override
	public String[] saveToString() {
		if(project == null){
			return null;
		}
		Schema schema = null;
		
		// Validate output against schema first
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL file = IOUtils.getFileInJarURL("tridas.xsd");
		try {
			schema = factory.newSchema(file);
		} catch (SAXException e) {
			log.error("Error getting TRiDaS schema for validation, not using.");
			log.dbe(DebugLevel.L2_ERROR, e);
			defaults.addConversionWarning(new ConversionWarning(WarningType.DEFAULT, "Error getting TRiDaS schema for validation, not using."));
		}
		
		
		StringWriter swriter = new StringWriter();
		// Marshaller code goes here... 
	    JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("org.tridas.schema");
			Marshaller m = jc.createMarshaller() ;
	        m.setProperty("com.sun.xml.bind.namespacePrefixMapper",new TridasNamespacePrefixMapper());
	        if(schema != null){
	        	m.setSchema(schema);
	        }
			m.marshal(project, swriter);

			//m.marshal(project,new File("/tmp/test.xml"));
		} catch (JAXBException e) {
			log.error("Jaxb error");
			log.dbe(DebugLevel.L2_ERROR, e);
			defaults.addConversionWarning(new ConversionWarning(WarningType.FILE_IGNORED, "Jaxb error, check log"));
			return null;
		}
		
		return swriter.getBuffer().toString().split("\n");
	}

	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		return "xml";
	}

	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		return null;
	}

	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
}
