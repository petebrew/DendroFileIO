package org.tridas.io.formats.past4;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.grlea.log.SimpleLogger;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.past4.Past4ToTridasDefaults.HeaderFields;
import org.tridas.io.formats.past4.TridasToPast4Defaults.DefaultFields;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasurementSeriesPlaceholder;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRadiusPlaceholder;
import org.tridas.schema.TridasRemark;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Past4Reader extends AbstractDendroFileReader {
	private static final SimpleLogger log = new SimpleLogger(Past4Reader.class);
	private Past4ToTridasDefaults defaults = null;
	private Past4ToTridasDefaults originalDefaults = null;
	private Integer numOfRecords = 0;
	private Integer numOfGroups = 0;
	private Integer length = 0;
	private TridasProject project;
	
	public Past4Reader() {
		super(Past4ToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}



	@Override
	public String[] getFileExtensions() {
		return new String[]{"P4P"};
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("past4.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("past4.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("past4.about.shortName");
	}
	
	/**
	 * Get XML Document from array of strings
	 * 
	 * @param argFileString
	 * @return
	 * @throws InvalidDendroFileException
	 */
	private Document getDocument(String[] argFileString) throws InvalidDendroFileException
	{
		// Recombine lines of file into a single string
		String fullXMLString = "";
		for(String str : argFileString)
		{
			fullXMLString+=str+System.getProperty("line.separator");
		}
		
		// Parse into document
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document doc;
		try {
			 db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.debug(e.getLocalizedMessage());
			throw new InvalidDendroFileException(I18n.getText("past4.errorParsingXML"));
		}
        
		InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(fullXMLString));
        
        try {
			doc = db.parse(is);
		} catch (SAXException e) {
			log.debug(e.getLocalizedMessage());
			throw new InvalidDendroFileException(I18n.getText("past4.errorParsingXML"));
		} catch (IOException e) {
			log.debug(e.getLocalizedMessage());
			throw new InvalidDendroFileException(I18n.getText("past4.errorParsingXML"));
		}
		
		return doc;
	}
	
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		log.debug("Parsing: " + argFileString);
		defaults = (Past4ToTridasDefaults) argDefaultFields;
		originalDefaults = defaults;
		
		Document doc = getDocument(argFileString); 

		// Handle PROJECT tag
		extractProjectInfo(doc);
		
		// Handle GROUPS tags
		extractGroupInfo(doc);
			
		// Handle RECORDS tags
		extractRecordInfo(doc);			
		
		
	}

	/**
	 * Extract information from the GROUP tags of a PAST4 XML File
	 * 
	 * @param doc
	 * @throws InvalidDendroFileException
	 */
	@SuppressWarnings("unchecked")
	private void extractGroupInfo(Document doc) throws InvalidDendroFileException
	{

		NodeList groups = doc.getElementsByTagName("GROUP");
		if(groups.getLength()!=this.numOfGroups)
		{
			throw new InvalidDendroFileException(I18n.getText("past4.numOfGroupsDiscrepancy"));
		}
		
		HashMap<Integer, TridasObject> objMap = new HashMap<Integer, TridasObject>();
		
		for(int i=0; i<groups.getLength(); i++)
		{
			Element grp = (Element) groups.item(i);
			objMap.put(i, createObjectFromGroup(grp, i));			
		}
		
		Set set = objMap.entrySet();
		Iterator  i = set.iterator();
		
		while(i.hasNext())
		{
			Map.Entry<Integer, TridasObject> hm = (Map.Entry<Integer, TridasObject>)i.next();
			
			Integer ownerIndex = getOwnerIndexFromObject(hm.getValue());
		
			if(ownerIndex==null)
			{
				throw new InvalidDendroFileException("past4.groupsCorrupted");
			}
			else if(ownerIndex==-1)
			{
				project.getObjects().add(hm.getValue());
			}
			else
			{
				TridasObject parentObject = getObjectByIndex(ownerIndex);
				
				if(parentObject==null)
				{
					throw new InvalidDendroFileException("past4.groupsCorrupted");
				}
				parentObject.getObjects().add(hm.getValue());
			}	
		}
		
	}
	
	/**
	 * Get the TridasObject by the position index in the original file 
	 * 
	 * @param index
	 * @return
	 */
	private TridasObject getObjectByIndex(Integer index)
	{
		for(TridasObject o: TridasUtils.getObjectList(project))
		{
			Integer thisIndex = getIndexFromObject(o);
			
			
			if(thisIndex.equals(index))
			{
				return o;
			}
		}
		
		return null;
	}
	
	/**
	 * Get the position index of the GROUP tag that is the 'owner' of the specified
	 * TridasObject
	 * 
	 * @param obj
	 * @return
	 */
	private Integer getOwnerIndexFromObject(TridasObject obj)
	{
	
		for(TridasGenericField gf : obj.getGenericFields())
		{
			if(gf.getName().equals("past4.ownerIndex"))
			{
				return Integer.valueOf(gf.getValue());
			}
		}
		
		return null;	
	}
	
	/**
	 * Get the position in the file of the GROUP tag that is represented by
	 * the specified TridasObject
	 *  
	 * @param obj
	 * @return
	 */
	private Integer getIndexFromObject(TridasObject obj)
	{
	
		for(TridasGenericField gf : obj.getGenericFields())
		{
			if(gf.getName().equals("past4.thisIndex"))
			{
				return Integer.valueOf(gf.getValue());
			}
		}
		
		return null;	
	}
	
	/**
	 * Create a TridasObject from a PAST4 GROUP tag.  The index at which this group is within the 
	 * file must be supplied
	 * 
	 * @param group
	 * @param index
	 * @return
	 * @throws InvalidDendroFileException
	 */
	private TridasObject createObjectFromGroup(Element group, Integer index) throws InvalidDendroFileException
	{
		Past4ToTridasDefaults objDefaults = (Past4ToTridasDefaults) defaults.clone();
		
		// Set the project name	
		if(group.hasAttribute("Name"))
		{
			objDefaults.getStringDefaultValue(DefaultFields.GRP_NAME).setValue(group.getAttribute("Name").toString());
		}
		else
		{
			throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "group", "name"));
		}
			
		
		// Set the project name	
		if(group.hasAttribute("Owner"))
		{
			try {
				objDefaults.getIntegerDefaultValue(DefaultFields.GRP_OWNER)
					.setValue(Integer.valueOf(group.getAttribute("Owner")));
			} catch (NumberFormatException e) {
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidGroupOwner"));
			}
		}
		else
		{
			throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "group", "owner"));
		}
		
		// Set the quality	
		if(group.hasAttribute("Quality"))
		{
			try {
				objDefaults.getIntegerDefaultValue(DefaultFields.GRP_QUALITY)
					.setValue(Integer.valueOf(group.getAttribute("Quality")));
			} catch (NumberFormatException e) {	}
		}
		
		
		// Generate the object
		TridasObject object = objDefaults.getObjectWithDefaults();
		
		TridasGenericField gf = new TridasGenericField();
		gf.setName("past4.thisIndex");
		gf.setType("xs:int");
		gf.setValue(String.valueOf(index));
		object.getGenericFields().add(gf);
		
		return object;
	}
	
	
	/**
	 * Extract information from the PROJECT tag of a PAST4 XML file
	 * 
	 * @param doc
	 * @throws InvalidDendroFileException
	 */
	private void extractProjectInfo(Document doc) throws InvalidDendroFileException
	{
		// Grab PROJECT tag from XML document
		NodeList nodes = doc.getElementsByTagName("PROJECT");
		
		// If there is not just 1 PROJECT tag then fail.
		if(nodes.getLength()!=1)
		{
			log.debug("More than one project tag in Past4 file");
			throw new InvalidDendroFileException(I18n.getText("past4.onlyOneProjectAllowed"));
		}
		
		// Check its the supported version
		Element projTag = (Element) nodes.item(0);
		if(projTag.hasAttribute("Version"))
		{
			if (!projTag.getAttribute("Version").toString().equals("400"))
			{
				throw new InvalidDendroFileException(I18n.getText("past4.onlyVersion4Supported"));
			}
		}
		
		// Set the project name	
		if(projTag.hasAttribute("Name"))
		{
			defaults.getStringDefaultValue(DefaultFields.PROJ_NAME).setValue(projTag.getAttribute("Name").toString());
		}
		else
		{
			throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "project", "name"));
		}
			
		// Set the persid 
		if(projTag.hasAttribute("PersID"))
		{
			defaults.getStringDefaultValue(DefaultFields.PROJ_PERSID).setValue(projTag.getAttribute("PersID").toString());
		}
		
		// Set the creation date	
		if(projTag.hasAttribute("CreationDate"))
		{	
			try {
				
				defaults.getDateTimeDefaultValue(DefaultFields.PROJ_CREATION_DATE)
					.setValue(DateUtils.parseDateFromPast4String(projTag.getAttribute("CreationDate").toString()));
			} catch (Exception e) {
				this.addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("fileio.invalidDate"), 
						"CreationDate"));
			}
		}
		
		// Set the edit date	
		if(projTag.hasAttribute("EditDate"))
		{	
			try {
				
				defaults.getDateTimeDefaultValue(DefaultFields.PROJ_EDIT_DATE)
					.setValue(DateUtils.parseDateFromPast4String(projTag.getAttribute("EditDate").toString()));
			} catch (Exception e) {
				this.addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("fileio.invalidDate"), 
						"EditDate"));
			}
		}
		
		if(projTag.hasAttribute("Groups"))
		{
			try{
				this.numOfGroups = Integer.valueOf(projTag.getAttribute("Groups"));
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "project", "groups"));
			}
		}
		else
		{
			throw new InvalidDendroFileException(I18n.getText("past4.missingNumOfGroups", "project", "groups"));
		}
		
		if(projTag.hasAttribute("Records"))
		{
			try{
				this.numOfRecords = Integer.valueOf(projTag.getAttribute("Records"));
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "project", "records"));
			}
		}
		else
		{
			throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "project", "records"));
		}
		
		
		project = defaults.getProjectWithDefaults();
	}
	
	/**
	 * Extract series information from the XML document
	 * 
	 * @param doc
	 * @throws InvalidDendroFileException
	 */
	private void extractRecordInfo(Document doc) throws InvalidDendroFileException
	{
		Past4ToTridasDefaults currDefaults = (Past4ToTridasDefaults) defaults.clone();
		
		NodeList records = doc.getElementsByTagName("RECORD");
		if(records.getLength()!=this.numOfRecords)
		{
			throw new InvalidDendroFileException(I18n.getText("past4.numOfRecordsDiscrepancy"));
		}
		
		for (int reci=0; reci<records.getLength(); reci++)
		{		
			Element record = (Element) records.item(reci);	

			if(record==null)
			{
				continue;
			}
			
			// Set the series name	
			if(record.hasAttribute("Keycode"))
			{
				currDefaults.getStringDefaultValue(DefaultFields.KEYCODE).setValue(record.getAttribute("Keycode").toString());
			}
			else
			{
				throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "keycode"));
			}
			
			// Set the index of the owner element of this record
			if(record.hasAttribute("Owner"))
			{
				try{
					currDefaults.getIntegerDefaultValue(DefaultFields.OWNER).setValue(Integer.valueOf(record.getAttribute("Owner")));
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "owner"));
				}
			}
			else
			{
				throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "owner"));
	
			}
			
			// Set the length of this series
			if(record.hasAttribute("Length"))
			{
				try{
					length = Integer.valueOf(record.getAttribute("Length"));
					currDefaults.getIntegerDefaultValue(DefaultFields.LENGTH).setValue(length);
					} catch (NumberFormatException e)
					{
						throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "length"));
					}
			}
			else
			{
				throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "length"));
	
			}
	
			// Set the offset of this series
			if(record.hasAttribute("Offset"))
			{
				try{
					currDefaults.getIntegerDefaultValue(DefaultFields.OFFSET).setValue(Integer.valueOf(record.getAttribute("Offset")));
					} catch (NumberFormatException e)
					{
						throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "offset"));
					}
			}
			else
			{
				throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "offset"));
	
			}
			
			// Set the offset of this series
			if(record.hasAttribute("Species"))
			{
				currDefaults.getStringDefaultValue(DefaultFields.SPECIES).setValue(record.getAttribute("Species"));
			}
	
			// Is this a dynamic mean?
			Boolean isDerivedSeries = null;
			if(record.hasAttribute("IsMeanValue"))
			{
				currDefaults.getPast4BooleanDefaultValue(DefaultFields.IS_MEAN_VALUE).setValueFromString(record.getAttribute("IsMeanValue"));
				isDerivedSeries = defaults.getPast4BooleanDefaultValue(DefaultFields.IS_MEAN_VALUE).getValue();
			}
			
			// Is filtered?
			/*Boolean isFilteredSeries = null;
			if(record.hasAttribute("Filter"))
			{
				defaults.getPast4BooleanDefaultValue(DefaultFields.FILTER).setValueFromString(record.getAttribute("Filter"));
				isFilteredSeries = defaults.getPast4BooleanDefaultValue(DefaultFields.FILTER).getValue();
				isDerivedSeries = true;
			}*/
			
			
			// Pith
			if(record.hasAttribute("Pith"))
			{
				currDefaults.getPast4BooleanDefaultValue(DefaultFields.PITH).setValueFromString(record.getAttribute("Pith"));
			}
			
			// Sapwood
			if(record.hasAttribute("SapWood"))
			{
				try{
					currDefaults.getIntegerDefaultValue(DefaultFields.SAPWOOD).setValue(Integer.valueOf(record.getAttribute("SapWood")));
				} catch (NumberFormatException e)
				{
					
				}
			}
			
			
			TridasElement el = currDefaults.getDefaultTridasElement();
			TridasSample samp = currDefaults.getDefaultTridasSample();
			TridasRadius radius = currDefaults.getDefaultTridasRadius();
			
			// Set series depending on whether it's a mean or not
			ITridasSeries series;
			List<TridasValues> originalValues;
			if(isDerivedSeries!=null)
			{
				if(isDerivedSeries)
				{
					series = currDefaults.getDefaultTridasDerivedSeries();
				}
				else
				{
					series = currDefaults.getDefaultTridasMeasurementSeries();
				}
			}
			else
			{
				series = currDefaults.getDefaultTridasMeasurementSeries();
			}
			
			// Now grab the actual data
			NodeList children = record.getChildNodes();
			for(int i=0; i<children.getLength(); i++)
			{
				if(children.item(i) instanceof Element)
				{
					Element child = (Element) children.item(i);
					if(child.getTagName().equals("DATA"))
					{
						if(child.getFirstChild() instanceof CDATASection)
						{
							CDATASection content = (CDATASection) child.getFirstChild();
							series.setValues(extractTridasValuesInfoFromContent(
									content.getTextContent(),isDerivedSeries));
							
							/*if(isFilteredSeries)
							{
								originalValues = extractTridasValuesInfoFromContent(
									content.getTextContent(),isDerivedSeries, true);
							}
							continue;*/
						}
					}
					else if (child.getTagName().equals("HEADER"))
					{
						extractMetadataFromHeader(child);
					}
				}
			}
			
			if(isDerivedSeries)
			{
				/*TridasRadiusPlaceholder rph = new TridasRadiusPlaceholder();
				TridasMeasurementSeriesPlaceholder mph = new TridasMeasurementSeriesPlaceholder();
				rph.setMeasurementSeriesPlaceholder(mph);
				samp.setRadiusPlaceholder(rph);
				el.getSamples().add(samp);*/
				//project.getObjects().get(0).getElements().add(el);
				project.getDerivedSeries().add((TridasDerivedSeries) series);
			}
			else
			{
				radius.getMeasurementSeries().add((TridasMeasurementSeries)series);
				samp.getRadiuses().add(radius);
				el.getSamples().add(samp);
				
				// Add to the correct part of the hierarchy
				TridasObject parentObject = getObjectByIndex(currDefaults.getIntegerDefaultValue(DefaultFields.OWNER).getValue());
				
				if(parentObject!=null)
				{
					parentObject.getElements().add(el);
				}
				else
				{
					throw new InvalidDendroFileException(I18n.getText("past4.groupsCorrupted"));
				}
				
			}
		}
		

	}
	
	/**
	 * Extract key=value metadata from XML Element
	 * 
	 * @param header
	 */
	private void extractMetadataFromHeader(Element header)
	{
		String blah = header.getFirstChild().getTextContent();
		String[] lines = blah.split(System.getProperty("line.separator"));
		
		for (String line : lines)
		{
			String[] split = line.split("=");
			if(split.length!=2) return;
			
			if (split[0].trim().equalsIgnoreCase("PersID"))
			{
				defaults.getStringDefaultValue(HeaderFields.PERSID).setValue(split[1].trim());
			}
			
				
			
		}
		
	}
	
	/**
	 * Extract a list of TridasValues from the content section of a PAST4 file
	 * 
	 * @param cdata
	 * @param isDerivedSeries
	 * @return
	 * @throws InvalidDendroFileException
	 */
	private List<TridasValues> extractTridasValuesInfoFromContent(String cdata, Boolean isDerivedSeries) throws InvalidDendroFileException
	{
		return extractTridasValuesInfoFromContent(cdata, isDerivedSeries, false);
	}
	
	
	/**
	 * Extract a list of TridasValues from the content section of a PAST4 file
	 * 
	 * @param cdata
	 * @param isDerivedSeries
	 * @param getUnfilteredData
	 * @return
	 * @throws InvalidDendroFileException
	 */
	private List<TridasValues> extractTridasValuesInfoFromContent(String cdata, Boolean isDerivedSeries, Boolean getUnfilteredData) throws InvalidDendroFileException
	{
		TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
		ArrayList<TridasValue> dataValues = new ArrayList<TridasValue>();
		
		String[] dataLines = cdata.split("\\n");
		
		if(length!=dataLines.length)
		{
			throw new InvalidDendroFileException(I18n.getText("past4.numOfValuesDiscrepancy"));
		}
		
		for (String dataline : dataLines)
		{
			String[] bitsOfData = dataline.split("\\t");
			TridasValue val = new TridasValue();
			
			// Set the actual value
			val.setValue(bitsOfData[0]);
			
			// Set sample count if 
			if(isDerivedSeries)
			{
				try{
					Integer sampleCount = Integer.parseInt(bitsOfData[1]);
					val.setCount(sampleCount);					
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("past4.sampleCountInvalid"));
				}
			}
			
			// Add comments as remarks if present
			if(bitsOfData.length>=6)
			{
				if(bitsOfData[5].trim().length()>0)
				{
					TridasRemark remark = new TridasRemark();
					remark = TridasUtils.getRemarkFromString(bitsOfData[5]);
					val.getRemarks().add(remark);
				}
			}
			
			dataValues.add(val);			
		}
		
		valuesGroup.setValues(dataValues);
		
		ArrayList<TridasValues> lst = new ArrayList<TridasValues>();
		lst.add(valuesGroup);
		
		return lst;
	}
		
	
	@Override
	public TridasProject getProject() {
		
		// Strip out temporary index genericFields
		for(TridasObject o : TridasUtils.getObjectList(project))
		{
			TridasGenericField ownerIndex = null;
			TridasGenericField thisIndex = null;
			
			for(TridasGenericField gf : o.getGenericFields())
			{
				if(gf.getName().equals("past4.ownerIndex"))
				{
					ownerIndex = gf;
				}
				else if(gf.getName().equals("past4.thisIndex"))
				{
					thisIndex = gf;
				}
				
			}
			if(ownerIndex!=null) o.getGenericFields().remove(ownerIndex);
			if(thisIndex!=null) o.getGenericFields().remove(thisIndex);
		}
		
		return project;
	}
	
	@Override
	protected void resetReader() {
		project = null;
		defaults = null;
		originalDefaults = null;
		numOfRecords = 0;
		numOfGroups = 0;
		length = 0;
	}

}
