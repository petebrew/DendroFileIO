/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io.formats.tridas;

import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.TridasIO;
import org.tridas.io.TridasNamespacePrefixMapper;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.transform.TridasVersionTransformer;
import org.tridas.io.transform.TridasVersionTransformer.TridasVersion;
import org.tridas.io.util.IOUtils;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;
import org.xml.sax.SAXException;

/**
 * The TRiDaS file format is our standard format <h3>Reference</h3>
 * <p>
 * See the <a href="http://www.tridas.org">Tree Ring Data Standard</a> website for futher
 * information.
 * </p>
 * 
 * @author peterbrewer
 */
public class TridasFile implements IDendroFile {
	
	private final static Logger log = LoggerFactory.getLogger(TridasFile.class);
	
	private ArrayList<TridasProject> projects = new ArrayList<TridasProject>();
	
	private IMetadataFieldSet defaults;
	private StringWriter swriter;
	
	private TridasVersion outputVersion = TridasIO.tridasVersionUsedInternally;
	
	public TridasFile(IMetadataFieldSet argDefaults) {
		defaults = argDefaults;
	}
	
	public void addTridasProject(TridasProject p) {
		projects.add(p);
	}
	
	public void clearTridasProjects()
	{
		projects = new ArrayList<TridasProject>();
	}
	
	public TridasTridas getTridasContainer()
	{
		TridasTridas container = new TridasTridas();
		container.setProjects(projects);
		return container;
	}
	
	public void validate() throws ImpossibleConversionException
	{
		Schema schema = null;
		
		// Validate output against schema first
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL file = IOUtils.getFileInJarURL("schemas/tridas.xsd");
		if(file == null){
			log.error("Could not find schema file");
		}else{
			try {
				schema = factory.newSchema(file);
			} catch (SAXException e) {
				log.error("Error getting TRiDaS schema for validation, not using.", e);
				throw new ImpossibleConversionException(I18n.getText("fileio.errorGettingSchema"));
			}
		}
		
		swriter = new StringWriter();
		// Marshaller code goes here...
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("org.tridas.schema");
			Marshaller m = jc.createMarshaller();
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new TridasNamespacePrefixMapper());
			if (schema != null) {
				m.setSchema(schema);
			}
			m.marshal(getTridasContainer(), swriter);

		} catch (Exception e) {
			log.error("Jaxb error", e);
			
			String cause = e.getCause().getMessage();
			if(cause!=null)
			{
				throw new ImpossibleConversionException(I18n.getText("fileio.jaxbError")+ " " + cause);
			}
			else
			{
				throw new ImpossibleConversionException(I18n.getText("fileio.jaxbError"));
			}

		}
		
	}
	
	@Override
	public String[] saveToString() {
		if (projects == null) {
			return null;
		}
		if(swriter==null)
		{
			try {
				validate();
			} catch (ImpossibleConversionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		
		try {
			// Try and convert to the requested version
			return TridasVersionTransformer.transformTridas(swriter.getBuffer().toString().split("\n"), outputVersion);
		} catch (Exception e) {
			log.error("Failed to convert TRiDaS to version: "+outputVersion.getVersionString()+".  Just sending what I've got.");
			return swriter.getBuffer().toString().split("\n");
		}
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
	

	/**
	 * Get the TRiDaS schema version that is being used to write this file
	 * 
	 * @return
	 */
	public TridasVersion getOutputVersion() {
		return outputVersion;
	}



	/**
	 * Set the TRiDaS schema version to use when writing this file
	 * 
	 * @param outputVersion
	 */
	public void setOutputVersion(TridasVersion outputVersion) {
		this.outputVersion = outputVersion;
	}

}
