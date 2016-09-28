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
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasEntity;
import org.tridas.schema.TridasFile;
import org.tridas.schema.TridasLaboratory;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.spatial.GMLPointSRSHandler;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * TRiDaS JSON is a simplified (flattened) form of TRiDaS in JSON notation.  It is aimed primarily 
 * at users of dendro data in stats/scripting environments such as R.
 * 
 * @author pbrewer
 *
 */
public class TridasJSONFile implements IDendroFile {
	
	private final static Logger log = LoggerFactory.getLogger(TridasJSONFile.class);
	
	private ArrayList<TridasProject> projects = new ArrayList<TridasProject>();
	
	private IMetadataFieldSet defaults;
	private StringWriter swriter;
	private Gson gson = new Gson();
	private Gson gsondate = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
	
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
		writeTridasEntityToJSON(output, entity, null);
	}
	
	@SuppressWarnings("unchecked")
	private void writeTridasEntityToJSON(JSONObject output, TridasEntity entity, Integer level)
	{
		String prefix = "";
		
		if(entity instanceof TridasProject)
		{
			prefix = "project";
		}
		else if(entity instanceof TridasObject)
		{
			prefix = "object";
			if(level!=null && level>0)
			{
				prefix+="["+level+"]";
			}
		}
		else if(entity instanceof TridasElement)
		{
			prefix = "element";
		}
		else if(entity instanceof TridasSample)
		{
			prefix = "sample";
		}
		else if(entity instanceof TridasRadius)
		{
			prefix = "radius";
		}
		
		try{ 
			output.put(prefix+".title", entity.getTitle());
		} catch (NullPointerException e)
		{
			output.put(prefix+".title", null);
		}
		
		try{
			output.put(prefix+".identifier", entity.getIdentifier().getValue());
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
		writeTridasEntityToJSON(output, project);
				
		try{
			if(project.isSetTypes())
			{
				ArrayList<String> values = new ArrayList<String>();
				
				for(ControlledVoc type : project.getTypes())
				{
					values.add(TridasUtils.controlledVocToString(type));
				}
				
				output.put("project.types", gson.toJson(values));
			}
		} catch (NullPointerException e)
		{
			output.put("project.types", null);
		}

		try{
			output.put("project.description", gson.toJson(project.getDescription()));
		} catch (NullPointerException e)
		{
			output.put("project.description", null);
		}
		
		try{
			if(project.isSetFiles())
			{
				ArrayList<String> values = new ArrayList<String>();
				
				for(TridasFile file : project.getFiles() )
				{
					values.add(file.getHref());
				}
				
				output.put("project.files", gson.toJson(values));
			}
		} catch (NullPointerException e)
		{
			output.put("project.files", null);
		}
		
		try{
			ArrayList<String> labs = new ArrayList<String>();
			for(TridasLaboratory lab : project.getLaboratories())
			{
				String  str =lab.getName().getValue();
				if(lab.getName().isSetAcronym()) str += "("+lab.getName().getAcronym()+")";
				labs.add(str);
			}
			
			if(labs.size()>0)
			{
				output.put("project.laboratories", gson.toJson(labs));
			}
			else
			{
				output.put("project.laboratories", null);
			}
		} catch (NullPointerException e)
		{
			output.put("project.laboratories", null);
		}

		
		try{
			output.put("project.category", gson.toJson(TridasUtils.controlledVocToString(project.getCategory())));
		} catch (NullPointerException e)
		{
			output.put("project.category", null);
		}
		
		try{
			output.put("project.investigator", gson.toJson(project.getInvestigator()));
		} catch (NullPointerException e)
		{
			output.put("project.investigator", null);
		}
		
		try{
			output.put("project.period", gson.toJson(project.getPeriod()));
		} catch (NullPointerException e)
		{
			output.put("project.period", null);
		}
		
		try{
			output.put("project.requestdate", gsondate.toJson(project.getRequestDate()));
		} catch (NullPointerException e)
		{
			output.put("project.requestdate", null);
		}
		
		try{
			output.put("project.commissioner", gson.toJson(project.getCommissioner()));
		} catch (NullPointerException e)
		{
			output.put("project.commissioner", null);
		}
		
		try{
						
			if(project.getReferences().size()>0)
			{
				output.put("project.references", gson.toJson(project.getReferences()));
			}
			else
			{
				output.put("project.references", null);
			}
		} catch (NullPointerException e)
		{
			output.put("project.references", null);
		}


		
		// Research missing

	}
	
	@SuppressWarnings("unchecked")
	private void writeObjectToJSON(JSONObject output, TridasObject object)
	{
		writeObjectToJSON(output, object, 0);
	}
	
	@SuppressWarnings("unchecked")
	private void writeObjectToJSON(JSONObject output, TridasObject object, Integer level)
	{
		writeTridasEntityToJSON(output, object, level);
		
		try{
			output.put("object.type", gson.toJson(TridasUtils.controlledVocToString(object.getType())));
		} catch (NullPointerException e)
		{
			output.put("object.type", null);
		}
		
		try{
			output.put("object.description", gson.toJson(object.getDescription()));
		} catch (NullPointerException e)
		{
			output.put("object.description", null);
		}
		
		// Link series missing
		
		try{
			if(object.isSetFiles())
			{
				ArrayList<String> values = new ArrayList<String>();
				
				for(TridasFile file : object.getFiles() )
				{
					values.add(file.getHref());
				}
				
				output.put("object.files", gson.toJson(values));
			}
		} catch (NullPointerException e)
		{
			output.put("object.files", null);
		}
		
		try{
			output.put("object.owner", gson.toJson(object.getOwner()));
		} catch (NullPointerException e)
		{
			output.put("object.owner", null);
		}
		
		try{
			output.put("object.creator", gson.toJson(object.getCreator()));
		} catch (NullPointerException e)
		{
			output.put("object.creator", null);
		}
		
		try{
			output.put("object.coverage.coverageTemporal", gson.toJson(object.getCoverage().getCoverageTemporal()));
		} catch (NullPointerException e)
		{
			output.put("object.coverage.coverageTemporal", null);
		}
		
		try{
			output.put("object.coverage.coverageTemporalFoundation", gson.toJson(object.getCoverage().getCoverageTemporalFoundation()));
		} catch (NullPointerException e)
		{
			output.put("object.coverage.coverageTemporalFoundation", null);
		}
		
		
		TridasLocation location = null;
		if(object.isSetLocation())
		{
			location = object.getLocation();
		}	
		writeLocationToJSON(output, location, "object");
		
		if(object.isSetObjects())
		{
			for(TridasObject o : object.getObjects())
			{
				level++;
				writeObjectToJSON(output, o, level);
			}
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void writeElementToJSON(JSONObject output, TridasElement element)
	{
		writeTridasEntityToJSON(output, element);
		
		try{
			output.put("element.type", gson.toJson(TridasUtils.controlledVocToString(element.getType())));
		} catch (NullPointerException e)
		{
			output.put("element.type", null);
		}
		
		try{
			output.put("element.description", gson.toJson(element.getDescription()));
		} catch (NullPointerException e)
		{
			output.put("element.description", null);
		}
		
		// LinkSeries missing
		
		try{
			if(element.isSetFiles())
			{
				ArrayList<String> values = new ArrayList<String>();
				
				for(TridasFile file : element.getFiles() )
				{
					values.add(file.getHref());
				}
				
				output.put("element.files", gson.toJson(values));
			}
		} catch (NullPointerException e)
		{
			output.put("element.files", null);
		}
		
		try{
			output.put("element.taxon", gson.toJson(TridasUtils.controlledVocToString(element.getTaxon())));
		} catch (NullPointerException e)
		{
			output.put("element.taxon", null);
		}
		
		try{
			output.put("element.shape", gson.toJson(TridasUtils.controlledVocToString(element.getShape())));
		} catch (NullPointerException e)
		{
			output.put("element.shape", null);
		}
		
		try{
			output.put("element.dimensions.unit", gson.toJson(TridasUtils.controlledVocToString(element.getDimensions().getUnit())));
		} catch (NullPointerException e)
		{
			output.put("element.dimensions.unit", null);
		}
		
		try{
			output.put("element.dimensions.height", gson.toJson(element.getDimensions().getHeight()));
		} catch (NullPointerException e)
		{
			output.put("element.dimensions.height", null);
		}
		
		try{
			output.put("element.dimensions.diameter", gson.toJson(element.getDimensions().getDiameter()));
		} catch (NullPointerException e)
		{
			output.put("element.dimensions.diameter", null);
		}
		
		try{
			output.put("element.dimensions.width", gson.toJson(element.getDimensions().getWidth()));
		} catch (NullPointerException e)
		{
			output.put("element.dimensions.width", null);
		}
		
		try{
			output.put("element.dimensions.depth", gson.toJson(element.getDimensions().getDepth()));
		} catch (NullPointerException e)
		{
			output.put("element.dimensions.depth", null);
		}
		
		try{
			output.put("element.authenticity", gson.toJson(element.getAuthenticity()));
		} catch (NullPointerException e)
		{
			output.put("element.authenticity", null);
		}
		
		TridasLocation location = null;
		if(element.isSetLocation())
		{
			location = element.getLocation();
		}	
		writeLocationToJSON(output, location, "element");
		
		try{
			output.put("element.processing", gson.toJson(element.getProcessing()));
		} catch (NullPointerException e)
		{
			output.put("element.processing", null);
		}
		
		try{
			output.put("element.marks", gson.toJson(element.getMarks()));
		} catch (NullPointerException e)
		{
			output.put("element.marks", null);
		}
		
		try{
			output.put("element.altitude", gson.toJson(element.getAltitude()));
		} catch (NullPointerException e)
		{
			output.put("element.altitude", null);
		}
		
		try{
			output.put("element.slope.angle", gson.toJson(element.getSlope().getAngle()));
		} catch (NullPointerException e)
		{
			output.put("element.slope.angle", null);
		}
		
		try{
			output.put("element.slope.azimuth", gson.toJson(element.getSlope().getAzimuth()));
		} catch (NullPointerException e)
		{
			output.put("element.slope.azimuth", null);
		}
		
		try{
			output.put("element.soil.description", gson.toJson(element.getSoil().getDescription()));
		} catch (NullPointerException e)
		{
			output.put("element.soil.description", null);
		}
		
		try{
			output.put("element.soil.depth", gson.toJson(element.getSoil().getDepth()));
		} catch (NullPointerException e)
		{
			output.put("element.soil.depth", null);
		}
		
		try{
			output.put("element.bedrock.description", gson.toJson(element.getBedrock().getDescription()));
		} catch (NullPointerException e)
		{
			output.put("element.bedrock.description", null);
		}

	}
	
	@SuppressWarnings("unchecked")
	private void writeSampleToJSON(JSONObject output, TridasSample sample)
	{
		writeTridasEntityToJSON(output, sample);
		
		try{
			output.put("sample.type", gson.toJson(TridasUtils.controlledVocToString(sample.getType())));
		} catch (NullPointerException e)
		{
			output.put("sample.type", null);
		}
		
		try{
			output.put("sample.description", gson.toJson(sample.getDescription()));
		} catch (NullPointerException e)
		{
			output.put("sample.description", null);
		}
			
		try{
			if(sample.isSetFiles())
			{
				ArrayList<String> values = new ArrayList<String>();
				
				for(TridasFile file : sample.getFiles() )
				{
					values.add(file.getHref());
				}
				
				output.put("sample.files", gson.toJson(values));
			}
		} catch (NullPointerException e)
		{
			output.put("sample.files", null);
		}
		
		try{
			output.put("sample.samplingdate", gsondate.toJson(sample.getSamplingDate().getValue().toGregorianCalendar().getTime()));
		} catch (NullPointerException e)
		{
			output.put("sample.samplingdate", null);
		}
		
		try{
			output.put("sample.position", gson.toJson(sample.getPosition()));
		} catch (NullPointerException e)
		{
			output.put("sample.position", null);
		}
		
		try{
			output.put("sample.state", gson.toJson(sample.getState()));
		} catch (NullPointerException e)
		{
			output.put("sample.state", null);
		}
		
		try{
			output.put("sample.knots", gson.toJson(sample.isKnots()));
		} catch (NullPointerException e)
		{
			output.put("sample.knots", null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeRadiusToJSON(JSONObject output, TridasRadius radius)
	{
		
		// Deliberately missing ring count
		// Deliberately missing average ring width
		
		try{
			output.put("radius.woodcompleteness.nrofunmeasuredinnerrings", gson.toJson(radius.getWoodCompleteness().getNrOfUnmeasuredInnerRings()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.nrofunmeasuredinnerrings", null);
		}
		
		try{
			output.put("radius.woodcompleteness.nrofunmeasuredouterrings", gson.toJson(radius.getWoodCompleteness().getNrOfUnmeasuredOuterRings()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.nrofunmeasuredouterrings", null);
		}
		
		try{
			output.put("radius.woodcompleteness.pith", gson.toJson(radius.getWoodCompleteness().getPith().getPresence().toString()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.pith", null);
		}
		
		try{
			output.put("radius.woodcompleteness.heartwood", gson.toJson(radius.getWoodCompleteness().getHeartwood().getPresence().toString()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.heartwood", null);
		}
		
		try{
			output.put("radius.woodcompleteness.heartwood.missingheartwoodringstopith", gson.toJson(radius.getWoodCompleteness().getHeartwood().getMissingHeartwoodRingsToPith()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.heartwood.missingheartwoodringstopith", null);
		}

		try{
			output.put("radius.woodcompleteness.heartwood.missingheartwoodringstopithfoundation", gson.toJson(radius.getWoodCompleteness().getHeartwood().getMissingHeartwoodRingsToPithFoundation()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.heartwood.missingheartwoodringstopithfoundation", null);
		}

		try{
			output.put("radius.woodcompleteness.sapwood", gson.toJson(radius.getWoodCompleteness().getSapwood().getPresence().toString()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.sapwood", null);
		}
		
		try{
			output.put("radius.woodcompleteness.sapwood.nrofsapwoodrings", gson.toJson(radius.getWoodCompleteness().getSapwood().getNrOfSapwoodRings()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.sapwood.nrofsapwoodrings", null);
		}
		
		try{
			output.put("radius.woodcompleteness.sapwood.lastringunderbark", gson.toJson(radius.getWoodCompleteness().getSapwood().getLastRingUnderBark().toString()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.sapwood.lastringunderbark", null);
		}
		
		try{
			output.put("radius.woodcompleteness.sapwood.missingsapwoodringstobark", gson.toJson(radius.getWoodCompleteness().getSapwood().getMissingSapwoodRingsToBark()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.sapwood.missingsapwoodringstobark", null);
		}
		
		try{
			output.put("radius.woodcompleteness.sapwood.missingsapwoodringstobarkfoundation", gson.toJson(radius.getWoodCompleteness().getSapwood().getMissingSapwoodRingsToBarkFoundation()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.sapwood.missingsapwoodringstobarkfoundation", null);
		}
		
		try{
			output.put("radius.woodcompleteness.bark", gson.toJson(radius.getWoodCompleteness().getBark().getPresence().toString()));
		} catch (NullPointerException e)
		{
			output.put("radius.woodcompleteness.bark", null);
		}
		
		try{
			output.put("radius.azimuth", gson.toJson(radius.getAzimuth()));
		} catch (NullPointerException e)
		{
			output.put("radius.azimuth", null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeLocationToJSON(JSONObject output, TridasLocation location, String prefix)
	{	
		try{
			GMLPointSRSHandler tph = new GMLPointSRSHandler(location.getLocationGeometry().getPoint());
			output.put(prefix+".location.latitude", gson.toJson(tph.getWGS84LatCoord()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.latitude", null);
		}
		
		try{
			GMLPointSRSHandler tph = new GMLPointSRSHandler(location.getLocationGeometry().getPoint());
			output.put(prefix+".location.longitude", gson.toJson(tph.getWGS84LongCoord()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.longitude", null);
		}
		
		try{
			output.put(prefix+".location.locationtype", gson.toJson(location.getLocationType().toString()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.locationtype", null);
		}
		
		try{
			output.put(prefix+".location.locationprecision", gson.toJson(location.getLocationPrecision()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.locationprecision", null);
		}
		
		try{
			output.put(prefix+".location.locationcomment", gson.toJson(location.getLocationComment()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.locationcomment", null);
		}
		
		try{
			output.put(prefix+".location.address.addressline1", gson.toJson(location.getAddress().getAddressLine1()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.address.addressline1", null);
		}
		
		try{
			output.put(prefix+".location.address.addressline2", gson.toJson(location.getAddress().getAddressLine2()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.address.addressline2", null);
		}
		
		try{
			output.put(prefix+".location.address.cityortown", gson.toJson(location.getAddress().getCityOrTown()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.address.cityortown", null);
		}
		
		try{
			output.put(prefix+".location.address.stateprovinceregion", gson.toJson(location.getAddress().getStateProvinceRegion()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.address.stateprovinceregion", null);
		}
		
		try{
			output.put(prefix+".location.address.postalcode", gson.toJson(location.getAddress().getPostalCode()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.address.postalcode", null);
		}
		
		try{
			output.put(prefix+".location.address.country", gson.toJson(location.getAddress().getCountry()));
		} catch (NullPointerException e)
		{
			output.put(prefix+".location.address.country", null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeSeriesToJSON(JSONObject output, ITridasSeries series)
	{
		try{
			output.put("series.title", gson.toJson(series.getTitle()));
		} catch (NullPointerException e)
		{
			output.put("series.title", null);
		}
		
		try{
			output.put("series.identifier", gson.toJson(series.getIdentifier().getValue()));
		} catch (NullPointerException e)
		{
			output.put("series.identifier", null);
		}
		
		try{
			output.put("series.createdtimestamp", gsondate.toJson(series.getCreatedTimestamp().getValue().toGregorianCalendar().getTime()));
		} catch (NullPointerException e)
		{
			output.put("series.createdtimestamp", null);
		}
		
		try{
			output.put("series.lastmodifiedtimestamp", gsondate.toJson(series.getLastModifiedTimestamp().getValue().toGregorianCalendar().getTime()));
		} catch (NullPointerException e)
		{
			output.put("series.lastmodifiedtimestamp", null);
		}
		
		try{
			output.put("series.comments", gson.toJson(series.getComments()));
		} catch (NullPointerException e)
		{
			output.put("series.comments", null);
		}
		
		try{
			output.put("series.interpretation.firstyear", series.getInterpretation().getFirstYear().getValue());
		} catch (NullPointerException e)
		{
			output.put("series.interpretation.firstyear", null);
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

		String labcode = "";
		for(TridasProject project : projects)
		{
			
			for(TridasObject object : project.getObjects())
			{
				try{
					labcode = TridasUtils.getGenericFieldByName(object, "tellervo.objectLabCode").getValue();
				} catch (NullPointerException e)
				{
					labcode = object.getTitle();
				}
				
				ArrayList<TridasElement> elementList = TridasUtils.getElementList(object);
				
				for(TridasElement element : elementList)
				{
					labcode+="-"+element.getTitle();
					for(TridasSample sample : element.getSamples())
					{
						labcode+="-"+sample.getTitle();
						for(TridasRadius radius : sample.getRadiuses())
						{
							labcode+="-"+radius.getTitle();
							for(TridasMeasurementSeries measurementseries : radius.getMeasurementSeries())
							{
								labcode+="-"+measurementseries.getTitle();

								for( TridasValues valuesgroup : measurementseries.getValues())
								{
									
									ArrayList<Integer> data = new ArrayList<Integer>();
									for(TridasValue value : valuesgroup.getValues())		
									{
										data.add(Integer.parseInt(value.getValue()));
									}

									JSONObject output = new JSONObject();
									writeProjectToJSON(output, project);
									writeObjectToJSON(output, object);
									writeElementToJSON(output, element);
									writeSampleToJSON(output, sample);
									writeRadiusToJSON(output, radius);
									writeSeriesToJSON(output, measurementseries);
									String variable = "";
									if(valuesgroup.isSetVariable())
									{
										if(valuesgroup.getVariable().isSetNormalTridas())
										{
											if(measurementseries.getValues().size()>1) {
												labcode+="-"+valuesgroup.getVariable().getNormalTridas().value();
												variable = "-"+valuesgroup.getVariable().getNormalTridas().value();
											}
											output.put("series.values.variable", valuesgroup.getVariable().getNormalTridas().value());
										}
										else if (valuesgroup.getVariable().isSetNormal())
										{
											if(measurementseries.getValues().size()>1) {
												labcode+="-"+valuesgroup.getVariable().getNormal();
												variable = "-"+valuesgroup.getVariable().getNormal();
											}
											output.put("series.values.variable", valuesgroup.getVariable().getNormal());
										}
										else
										{
											if(measurementseries.getValues().size()>1) {
												labcode+="-"+valuesgroup.getVariable().getValue();
												variable = "-"+valuesgroup.getVariable().getValue();
											}
											output.put("series.values.variable", valuesgroup.getVariable().getValue());
										}

									}
									else
									{
										output.put("series.values.variable", "unknown");

									}
									
									if(valuesgroup.isSetUnit())
									{
										if(valuesgroup.getUnit().isSetNormalTridas())
										{
											output.put("series.values.units", valuesgroup.getUnit().getNormalTridas().value());
										}
										else if (valuesgroup.getUnit().isSetNormal()){
											output.put("series.values.units", valuesgroup.getUnit().getNormal().toString());
										}
										else{
											output.put("series.values.units", valuesgroup.getUnit().getValue().toString());
										}
										
									}
									else
									{
										output.put("series.units", "unitless");
									}
									
									output.put("series.values", data);
									root.put(measurementseries.getIdentifier().getValue()+variable, output);
									gotData = true;
									
								}	
							}	
						}	
					}	
				}
			}
			
			// DERIVED SERIES
			for (TridasDerivedSeries derivedSeries : project.getDerivedSeries())
			{	
				labcode+= derivedSeries.getTitle();

				for( TridasValues valuesgroup : derivedSeries.getValues())
				{
					
					ArrayList<Integer> data = new ArrayList<Integer>();
					for(TridasValue value : valuesgroup.getValues())		
					{
						data.add(Integer.parseInt(value.getValue()));
					}

					JSONObject output = new JSONObject();
					writeProjectToJSON(output, project);
					writeSeriesToJSON(output, derivedSeries);
					String variable = "";
					if(valuesgroup.isSetVariable())
					{
						if(valuesgroup.getVariable().isSetNormalTridas())
						{
							if(derivedSeries.getValues().size()>1) {
								labcode+="-"+valuesgroup.getVariable().getNormalTridas().value();
								variable = "-"+valuesgroup.getVariable().getNormalTridas().value();
							}
							output.put("series.values.variable", valuesgroup.getVariable().getNormalTridas().value());
						}
						else if (valuesgroup.getVariable().isSetNormal())
						{
							if(derivedSeries.getValues().size()>1) {
								labcode+="-"+valuesgroup.getVariable().getNormal();
								variable = "-"+valuesgroup.getVariable().getNormal();
							}
							output.put("series.values.variable", valuesgroup.getVariable().getNormal());
						}
						else
						{
							if(derivedSeries.getValues().size()>1) {
								labcode+="-"+valuesgroup.getVariable().getValue();
								variable = "-"+valuesgroup.getVariable().getValue();
							}
							output.put("series.values.variable", valuesgroup.getVariable().getValue());
						}

					}
					else
					{
						output.put("series.values.variable", "unknown");

					}
					
					if(valuesgroup.isSetUnit())
					{
						if(valuesgroup.getUnit().isSetNormalTridas())
						{
							output.put("series.values.units", valuesgroup.getUnit().getNormalTridas().value());
						}
						else if (valuesgroup.getUnit().isSetNormal()){
							output.put("series.values.units", valuesgroup.getUnit().getNormal().toString());
						}
						else{
							output.put("series.values.units", valuesgroup.getUnit().getValue().toString());
						}
						
					}
					else
					{
						output.put("series.units", "unitless");
					}
					
					output.put("series.values", data);
					root.put(derivedSeries.getIdentifier().getValue()+variable, output);
					gotData = true;
					
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
