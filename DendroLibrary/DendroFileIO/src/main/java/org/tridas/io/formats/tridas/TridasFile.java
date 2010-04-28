package org.tridas.io.formats.tridas;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.DendroFile;
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


	TridasProject project;
	
	
	public TridasFile() {
		this.setExtension("xml");
	}
	
	public void setProject(TridasProject p){
		project = p;
	}
	
	@SuppressWarnings("restriction")
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
			m.marshal(project, writer);
			//m.marshal(project,new File("/tmp/test.xml"));
		} catch (JAXBException e) {
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
