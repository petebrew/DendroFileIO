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
package org.tridas.io.formats.tridasjson;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.TridasIO;
import org.tridas.io.TridasNamespacePrefixMapper;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.transform.TridasVersionTransformer.TridasVersion;
import org.tridas.io.util.IOUtils;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasEntity;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * The TRiDaS file format is our standard format <h3>Reference</h3>
 * <p>
 * See the <a href="http://www.tridas.org">Tree Ring Data Standard</a> website for futher
 * information.
 * </p>
 * 
 * @author peterbrewer
 */
public class TridasJSONFile implements IDendroFile {
	
	private final static Logger log = LoggerFactory.getLogger(TridasJSONFile.class);
	
	private ArrayList<TridasProject> projects = new ArrayList<TridasProject>();
	
	private IMetadataFieldSet defaults;
	private StringWriter swriter;
	
	private TridasVersion outputVersion = TridasIO.tridasVersionUsedInternally;
	
	public TridasJSONFile(IMetadataFieldSet argDefaults) {
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
	
	@SuppressWarnings("unchecked")
	private void writeTridasEntityToJSON(JSONObject output, TridasEntity entity)
	{
		Gson gson = new Gson();
		Gson gsondate = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		String prefix = "";
		
		if(entity instanceof TridasProject)
		{
			prefix = "project";
		}
		else if(entity instanceof TridasObject)
		{
			prefix = "object";
		}
		
		
		
		
		try{ 
			output.put(prefix+".title", entity.getTitle());
		} catch (NullPointerException e)
		{
			output.put(prefix+".title", null);
		}
		
		try{
			output.put(prefix+".identifier", gson.toJson(entity.getIdentifier().getValue()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".identifier", null);
		}
		
		try{
			output.put(prefix+".createdtimestamp", gsondate.toJson(entity.getCreatedTimestamp().getValue().toGregorianCalendar().getTime()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".createdtimestamp", null);
		}
		
		try{
			output.put(prefix+".lastmodifiedtimestamp", gsondate.toJson(entity.getLastModifiedTimestamp().getValue().toGregorianCalendar().getTime()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".lastmodifiedtimestamp", null);
		}
		
		try{
			output.put(prefix+".comments", gson.toJson(entity.getComments()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".comments", null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeProjectToJSON(JSONObject output, TridasProject project)
	{
		Gson gson = new Gson();

		writeTridasEntityToJSON(output, project);
		
		try{
			output.put("project.description", gson.toJson(project.getDescription()));
		} catch (NullPointerException e)
		{
			output.put("project.description", null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeObjectToJSON(JSONObject output, TridasObject object)
	{
		Gson gson = new Gson();

		writeTridasEntityToJSON(output, object);
		
		try{
			output.put("object.description", gson.toJson(object.getDescription()));
		} catch (NullPointerException e)
		{
			output.put("object.description", null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeMeasurementSeriesToJSON(JSONObject output, TridasMeasurementSeries measurementseries)
	{
		Gson gson = new Gson();
		Gson gsondate = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		try{
			output.put("measurementseries.title", gson.toJson(measurementseries.getTitle()));
		} catch (NullPointerException e)
		{
			output.put("measurementseries.title", null);
		}
		
		try{
			output.put("measurementseries.identifier", gson.toJson(measurementseries.getIdentifier().getValue()));
		} catch (NullPointerException e)
		{
			output.put("measurementseries.identifier", null);
		}
		
		try{
			output.put("measurementseries.createdtimestamp", gsondate.toJson(measurementseries.getCreatedTimestamp().getValue().toGregorianCalendar().getTime()));
		} catch (NullPointerException e)
		{
			output.put("measurementseries.createdtimestamp", null);
		}
		
		try{
			output.put("measurementseries.lastmodifiedtimestamp", gsondate.toJson(measurementseries.getLastModifiedTimestamp().getValue().toGregorianCalendar().getTime()));
		} catch (NullPointerException e)
		{
			output.put("measurementseries.lastmodifiedtimestamp", null);
		}
		
		try{
			output.put("measurementseries.comments", gson.toJson(measurementseries.getComments()));
		} catch (NullPointerException e)
		{
			output.put("measurementseries.comments", null);
		}
		
		try{
			output.put("measurementseries.interpretation.firstyear", measurementseries.getInterpretation().getFirstYear().getValue());
		} catch (NullPointerException e)
		{
			output.put("measurementseries.interpretation.firstyear", null);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String[] saveToString() {
		if (projects == null) {
			return null;
		}

		boolean gotData = false;
		StringWriter swriter = new StringWriter();
		
		JSONObject root = new JSONObject();

		for(TridasProject project : projects)
		{
			
			for(TridasObject object : project.getObjects())
			{
				for(TridasElement element : object.getElements())
				{
					for(TridasSample sample : element.getSamples())
					{
						for(TridasRadius radius : sample.getRadiuses())
						{
							for(TridasMeasurementSeries measurementseries : radius.getMeasurementSeries())
							{
								JSONObject output = new JSONObject();
								writeProjectToJSON(output, project);
								writeObjectToJSON(output, object);
								writeMeasurementSeriesToJSON(output, measurementseries);
								for( TridasValues valuesgroup : measurementseries.getValues())
								{
									
									ArrayList<Integer> data = new ArrayList<Integer>();
									for(TridasValue value : valuesgroup.getValues())		
									{
										data.add(Integer.parseInt(value.getValue()));
									}
									
									/*if(valuesgroup.getVariable().getNormalTridas().equals(NormalTridasVariable.RING_WIDTH))
									{
										output.put("ring-widths", data);
									}*/
									 
									
									output.put("ring-widths", data);
									
								}	
								
								root.put(measurementseries.getTitle(), output);
								gotData = true;
							}	
						}	
					}	
				}
			}
		}
		
		if(!gotData)
		{	
			return "{}".split("\n");
		}
		
		try {
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();

			root.writeJSONString(swriter);
			JsonElement je = jp.parse(swriter.getBuffer().toString());
			String prettyJsonString = gson.toJson(je);
											
			return prettyJsonString.split("\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
		
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		return "json";
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
