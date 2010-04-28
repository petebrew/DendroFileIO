package org.tridas.io.maventests;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;

import junit.framework.TestCase;

public class JaxbTest extends TestCase {
	public void testLoading() throws Exception    
	{        
		String filename = "complexexamplev121.xml";
		filename = "/Users/peterbrewer/dev/java/DendroFileIOLibrary/TestData/TRiDaS/Tridas1.xml";
		//FileInputStream is = new FileInputStream (filename);       
		InputStream is = this.getClass().getResourceAsStream(filename);       
		JAXBContext jaxbContext = JAXBContext.newInstance("org.tridas.schema");       
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();       
		Object o = unmarshaller.unmarshal(is);       
		//logger.info("Unmarshalled object of class: " + o.getClass().getName());       
		Marshaller marshaller = jaxbContext.createMarshaller();        
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");        
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		//testing     
		java.io.StringWriter sw = new StringWriter();    
		marshaller.marshal(o, sw);       
		System.out.print(sw.toString());    
		}   
	
	public void testMarshalling() throws Exception    {        
		TridasProject projectTridas = new TridasProject();       
		projectTridas.setTitle("my test project");       
		// Add a tridas object entity      
		TridasObject objectTridas = new TridasObject();    
		objectTridas.setTitle("my test object");       
		projectTridas.getObjects().add(objectTridas);   
		JAXBContext jaxbContext = JAXBContext.newInstance("org.tridas.schema");     
		Marshaller marshaller = jaxbContext.createMarshaller();      
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");     
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		//testing        
		java.io.StringWriter sw = new StringWriter();    
		marshaller.marshal(projectTridas, sw);    
		System.out.print(sw.toString());    
	}
}
