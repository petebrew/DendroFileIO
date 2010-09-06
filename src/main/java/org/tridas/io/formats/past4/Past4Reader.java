package org.tridas.io.formats.past4;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
import org.tridas.io.formats.past4.TridasToPast4Defaults.DefaultFields;
import org.tridas.io.util.DateUtils;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Past4Reader extends AbstractDendroFileReader {
	private static final SimpleLogger log = new SimpleLogger(Past4Reader.class);
	private Past4ToTridasDefaults defaults = null;
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
		
		Document doc = getDocument(argFileString); 

		// Handle PROJECT tag
		extractProjectInfo(doc);
		
		project = defaults.getProjectWithDefaults();
		
	
		
		// Handle GROUPS tags
		NodeList groups = doc.getElementsByTagName("GROUP");
		if(groups.getLength()!=this.numOfGroups)
		{
			throw new InvalidDendroFileException(I18n.getText("past4.numOfGroupsDiscrepancy"));
		}
		
		TridasObject object = defaults.getObjectWithDefaults();

		
			
		
		
		// Handle RECORDS tags
		NodeList records = doc.getElementsByTagName("RECORD");
		if(records.getLength()!=this.numOfRecords)
		{
			throw new InvalidDendroFileException(I18n.getText("past4.numOfRecordsDiscrepancy"));
		}
		
		for (int i=0; i<records.getLength(); i++)
		{		
			Element record = (Element) records.item(i);
			
			
			TridasElement el = getTridasElementFromRecord(record);
			object.getElements().add(el);

			
		}

		
		project.getObjects().add(object);
		
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
		
	}
	
	/**
	 * Extract series information from a RECORD tag
	 * 
	 * @param record
	 * @throws InvalidDendroFileException
	 */
	private TridasElement getTridasElementFromRecord(Element record) throws InvalidDendroFileException
	{
		if(record==null)
		{
			return null;
		}
		
		// Set the series name	
		if(record.hasAttribute("Keycode"))
		{
			defaults.getStringDefaultValue(DefaultFields.KEYCODE).setValue(record.getAttribute("Keycode").toString());
		}
		else
		{
			throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "keycode"));
		}
		
		// Set the index of the owner element of this record
		if(record.hasAttribute("Owner"))
		{
			try{
			defaults.getIntegerDefaultValue(DefaultFields.OWNER).setValue(Integer.valueOf(record.getAttribute("Owner")));
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
				defaults.getIntegerDefaultValue(DefaultFields.LENGTH).setValue(length);
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
				defaults.getIntegerDefaultValue(DefaultFields.OFFSET).setValue(Integer.valueOf(record.getAttribute("Offset")));
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "offset"));
				}
		}
		else
		{
			throw new InvalidDendroFileException(I18n.getText("past4.missingMandatoryField", "record", "offset"));

		}
		
		TridasElement el = defaults.getDefaultTridasElement();
		TridasSample samp = defaults.getDefaultTridasSample();
		TridasRadius radius = defaults.getDefaultTridasRadius();
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		
		
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
						series.setValues(extractTridasValuesInfoFromContent(content.getTextContent()));
					}
				}
				else if (child.getTagName().equals("HEADER"))
				{
					
				}
			}
		}
		
		radius.getMeasurementSeries().add(series);
		samp.getRadiuses().add(radius);
		el.getSamples().add(samp);
		return el;
	}
	
	
	private List<TridasValues> extractTridasValuesInfoFromContent(String cdata) throws InvalidDendroFileException
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
			val.setValue(bitsOfData[0]);
			dataValues.add(val);			
		}
		
		valuesGroup.setValues(dataValues);
		
		ArrayList<TridasValues> lst = new ArrayList<TridasValues>();
		lst.add(valuesGroup);
		
		return lst;
	}
	
	private TridasProject createProject()
	{
		return null;
	}
	
	
	@Override
	public TridasProject getProject() {
				
		return project;
	}
	
	@Override
	protected void resetReader() {
		defaults = null;
		numOfRecords = 0;
		numOfGroups = 0;
		length = 0;

	}

}
