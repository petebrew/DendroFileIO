package org.tridas.io.formats.tridas;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.DendroFile;
import org.tridas.io.TridasNamespacePrefixMapper;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.schema.TridasProject;


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
public class TridasFile extends DendroFile {

	private final static SimpleLogger log = new SimpleLogger(TridasFile.class);
	
	TridasProject project;
	
	
	public TridasFile() {
		super("tridas");
	}
	
	public void setProject(TridasProject p){
		project = p;
	}
	
	@Override
	public String[] saveToString(){
		if(project == null){
			return null;
		}
		
		StringWriter writer = new StringWriter();
		// Marshaller code goes here... 
	    JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("org.tridas.schema");
			Marshaller m = jc.createMarshaller() ;
	        m.setProperty("com.sun.xml.bind.namespacePrefixMapper",new TridasNamespacePrefixMapper());
			m.marshal(project, writer);
			//m.marshal(project,new File("/tmp/test.xml"));
		} catch (JAXBException e) {
			log.error("Jaxb error");
			log.dbe(DebugLevel.L2_ERROR, e);
			return null;
		}
		
		return writer.getBuffer().toString().split("\n");
	}
	
	/**
	 * NOT REQUIRED
	 */
	
	@Override
	public void setSeries(ITridasSeries series)
			throws ConversionWarningException {
		return;
	}

	@Override
	public void addSeries(ITridasSeries series)
			throws ConversionWarningException {
		return;
	}

	
}
