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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;

import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
 
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasNamespacePrefixMapper;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.util.IOUtils;
import org.tridas.schema.TridasProject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reader for the TRiDaS file format. This is little more than a
 * wrapper around the JaXB unmarshaller
 * 
 * @see org.tridas.io.formats.tridas
 * @author peterbrewer
 */
@SuppressWarnings("restriction")
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
		if(file==null)
		{
			log.error(I18n.getText("tridas.schemaMissing"));
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("tridas.schemaMissing")));
		}
		else
		{
		
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
				if(!isTridasNamespaceCorrect(fileString.toString()))
				{
					throw new InvalidDendroFileException(I18n.getText("tridas.schemaException", "TRiDaS schema version is either incorrect or missing"));
				}
				throw new InvalidDendroFileException(I18n.getText("tridas.schemaException", ex.getLocalizedMessage()));
				
			} catch (IOException e) {
				throw new InvalidDendroFileException(I18n.getText("tridas.schemaException", e.getLocalizedMessage()));
			}
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
	
	/**
	 * Check that the current Tridas namespace is mentioned in the
	 * provided XML file
	 * 
	 * @param xmlfile
	 * @return
	 */
	private Boolean isTridasNamespaceCorrect(String xmlfile)
	{
		try{
	    DocumentBuilderFactory domFactory = 
	        DocumentBuilderFactory.newInstance();
	              domFactory.setNamespaceAware(true); 
	        DocumentBuilder builder = domFactory.newDocumentBuilder();
	        Document doc = builder.parse(xmlfile);
	        XPath xpath = XPathFactory.newInstance().newXPath();
	        XPathExpression expr = xpath.compile("//*[namespace-uri()='"+
	        		TridasNamespacePrefixMapper.getTridasNamespaceURI()+"']");

	        Object result = expr.evaluate(doc, XPathConstants.NODESET);
	        NodeList nodes = (NodeList) result;
	        
	        if(nodes.getLength()>0)
	        {
	        	return true;
	        }
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
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
