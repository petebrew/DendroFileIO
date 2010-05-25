package org.tridas.io.formats.sheffield;

import java.util.ArrayList;

import net.opengis.gml.schema.Pos;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.sheffield.SheffieldToTridasDefaults.DefaultFields;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldDataType;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldDateType;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldEdgeCode;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldPeriodCode;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldPithCode;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldShapeCode;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldVariableCode;
import org.tridas.io.util.CoordinatesUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**
 * Reader for the file format produced by Ian Tyers' 
 * Dendro for Windows software.
 * 
 * @author peterbrewer
 *
 */
public class SheffieldReader extends AbstractDendroFileReader {
	private static final SimpleLogger log = new SimpleLogger(SheffieldReader.class);
	private TridasProject project = null;
	private SheffieldToTridasDefaults defaults = new SheffieldToTridasDefaults();
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	
	SheffieldDateType dateType =  SheffieldDateType.RELATIVE;
	
	public SheffieldReader() {
		super(SheffieldToTridasDefaults.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {

		// Check the file is valid
		checkFile(argFileString);
		
		
		ArrayList<TridasValue> ringWidthValues = new ArrayList<TridasValue>();
		

		for (int lineNum=1; lineNum<=22; lineNum++)
		{	
			String lineString = argFileString[lineNum-1];	
			
			// Line 1 - Series title
			if(lineNum==1)
			{	
				if (lineString.length()>64)
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.lineOneTooBig")));
				}
				if (SheffieldFile.containsSpecialChars(lineString))
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.specialCharWarning")));
				}		
				defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(lineString);
			}
			
			// Line 2 - Number of rings
			else if(lineNum==2)
			{
				try{
					int ringCount = Integer.valueOf(lineString);
					defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(ringCount);
				} catch (NumberFormatException e)
				{
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidDataValue")));
				}
	
			}		
			
			// Line 3 - Date type 
			// TODO How are we handling relative series?
			else if(lineNum==3)
			{		
				if (!lineString.equalsIgnoreCase("A") && (!lineString.equalsIgnoreCase("R")))
				{
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("sheffield.invalidDateType")));
					continue;
				}	
				
				dateType = SheffieldDateType.fromCode(lineString);
			}
			
			// Line 4 - Start date
			else if(lineNum==4)
			{
				try{
					int yearNum = Integer.valueOf(lineString.trim());
					SafeIntYear startYear;
					
					// Handle offset for absolute dates
					if(dateType==SheffieldDateType.ABSOLUTE)
					{
						if(yearNum>=10001)
						{
							yearNum = yearNum-10000;
						}
						else
						{
							yearNum = yearNum-10001;
						}
					}	
					
					GenericDefaultValue<SafeIntYear> startYearField = (GenericDefaultValue<SafeIntYear>) defaults.getDefaultValue(DefaultFields.START_YEAR); 
					startYearField.setValue(new SafeIntYear(yearNum));
					
				} catch (NumberFormatException e) { 
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidStartYear")));	
				}
			}
			
			// Line 5 - Data type
			else if(lineNum==5)
			{
				GenericDefaultValue<SheffieldDataType> dataTypeField = (GenericDefaultValue<SheffieldDataType>) defaults.getDefaultValue(DefaultFields.SHEFFIELD_DATA_TYPE); 
				dataTypeField.setValue(SheffieldDataType.fromCode(lineString));
			}
			
			// Line 6 - sapwood number or number of timbers 
			else if(lineNum==6)
			{
				Integer val = 0;
				try{
					val = Integer.parseInt(lineString);
				} catch (NumberFormatException e)
				{
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidDataValue"), "Sapwood count"));	
					continue;
				}
				
				GenericDefaultValue<SheffieldDataType> dataTypeField = (GenericDefaultValue<SheffieldDataType>) defaults.getDefaultValue(DefaultFields.SHEFFIELD_DATA_TYPE); 
				if (dataTypeField.getValue().equals(SheffieldDataType.ANNUAL_RAW_RING_WIDTH))
				{
					defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).setValue(val);
				}
				else
				{
					// Field contains number of timbers/chronologies.  This is not required
					// as it can be taken from the 'count' of the value tags
				}
			}
			
			
			// Line 7 - edge code or chronology type 
			else if(lineNum==7)
			{
				GenericDefaultValue<SheffieldEdgeCode> edgeCodeField = (GenericDefaultValue<SheffieldEdgeCode>) defaults.getDefaultValue(DefaultFields.SHEFFIELD_EDGE_CODE); 

				if(SheffieldEdgeCode.fromCode(lineString.trim())!=null)
				{
					edgeCodeField.setValue(SheffieldEdgeCode.fromCode(lineString.trim()));
				}
				else
				{
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("sheffield.invalidEdgeCode")));	
					continue;
				}
			}
			
			// Line 8 - comment
			else if(lineNum==8)
			{
				if (lineString.length()>64)
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.lineNTooBig", String.valueOf(lineString.length()))));
				}
				defaults.getStringDefaultValue(DefaultFields.SERIES_COMMENT).setValue(lineString);
			}
			
			// Line 9 - UK Grid coords
			else if(lineNum==9)
			{
				// We could add PROJ4 lib to convert these but not sure its worth it
				// For now just add as genericField
				if(!lineString.equals("?"))
				{
					defaults.getStringDefaultValue(DefaultFields.UK_COORDS).setValue(lineString);
				}
			}
			
			// Line 10 - Lat/Long coords
			else if(lineNum==10)
			{
				Double northing;
				Double easting;
				String[] coords = lineString.split(" ");
				if(coords.length!=2)
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.errorParsingCoords")));
					continue;
				}
				
				if(lineString.contains("^"))
				{
					// Convert old style coordinates
					try {
						northing = convertCoordsToDD(coords[0], NorthingEasting.NORTH_SOUTH);
						easting = convertCoordsToDD(coords[1], NorthingEasting.EAST_WEST);
					} catch (ConversionWarningException e) {
						addWarningToList(e.getWarning());
						continue;
					}
				}
				else
				{
					try{
						northing = Double.valueOf(coords[0]);
						easting = Double.valueOf(coords[1]);
					} catch (NumberFormatException e)
					{
						addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
								I18n.getText("sheffield.errorParsingCoords")));
						continue;
					}
				}
				
				if(northing!=null && easting!=null)
				{
					
					defaults.getDoubleDefaultValue(DefaultFields.LATITUDE).setValue(northing);
					defaults.getDoubleDefaultValue(DefaultFields.LONGITUDE).setValue(easting);
				}
				
			}
			
			// Line 11 - Pith
			else if(lineNum==11)
			{
				GenericDefaultValue<ComplexPresenceAbsence> pithField = (GenericDefaultValue<ComplexPresenceAbsence>) defaults.getDefaultValue(DefaultFields.PITH); 

				if(lineString.equalsIgnoreCase("C"))
				{
					pithField.setValue(ComplexPresenceAbsence.COMPLETE);
				}
				else if (lineString.equalsIgnoreCase("?"))
				{
					pithField.setValue(ComplexPresenceAbsence.UNKNOWN);
				}
				else
				{
					pithField.setValue(ComplexPresenceAbsence.ABSENT);
					// Sheffield format includes some extra info about pith that does not map
					// to TRiDaS so this info will be stored as a generic field
					defaults.getStringDefaultValue(DefaultFields.PITH_DESCRIPTION).setValue(
							SheffieldPithCode.fromCode(lineString).toString());

				}
			}
			
			// Line 12 - Cross-section code
			else if(lineNum==12)
			{
				GenericDefaultValue<SheffieldShapeCode> shapeField = (GenericDefaultValue<SheffieldShapeCode>) defaults.getDefaultValue(DefaultFields.SHEFFIELD_SHAPE_CODE);
				
				if(SheffieldShapeCode.fromCode(lineString)!=null)
				{
					shapeField.setValue(SheffieldShapeCode.fromCode(lineString));
				}
				else
				{
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidDataValue"), "Cross-section code"));	
					continue;	
				}
			}
			
			// Line 13 - Major dimension
			else if(lineNum==13)
			{
				Double dim;
				try{dim = Double.parseDouble(lineString);
					defaults.getDoubleDefaultValue(DefaultFields.MAJOR_DIM).setValue(dim);
				} catch (NumberFormatException e){
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidDataValue"), "Major dimension"));	
					continue;
				}
			}
			
			// Line 14 - Minor dimension
			else if(lineNum==14)
			{
				Double dim;
				try{dim = Double.parseDouble(lineString);
					defaults.getDoubleDefaultValue(DefaultFields.MINOR_DIM).setValue(dim);
				} catch (NumberFormatException e){
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidDataValue"), "Minor dimension"));	
					continue;
				}
			}
			
			// Line 15 - Unmeasured inner rings
			else if(lineNum==15)
			{
				try{Integer ringCount = Integer.parseInt(lineString.substring(1));
					defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_INNER_RINGS).setValue(ringCount);
				} catch (NumberFormatException e){
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidDataValue"), "Unmeasured inner rings"));	
					continue;
				}
			}

			// Line 16 - Unmeasured outer rings
			else if(lineNum==16)
			{
				try{Integer ringCount = Integer.parseInt(lineString.substring(1));
				defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_OUTER_RINGS).setValue(ringCount);
				} catch (NumberFormatException e){
				addWarningToList(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("fileio.invalidDataValue"), "Unmeasured outer rings"));	
				continue;
				}
			}
			
			// Line 17 - Group/phase 
			else if (lineNum==17)
			{
				if (lineString.length()>=14)
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.line17TooBig")));
				}
				else if (!lineString.equals("?"))
				{
					defaults.getStringDefaultValue(DefaultFields.GROUP_PHASE).setValue(lineString);
				}
			}
			
			// Line 18 - Short title 
			else if (lineNum==18)
			{
				if (lineString.length()>=8)
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.line18TooBig")));
				}
				
				defaults.getStringDefaultValue(DefaultFields.OBJECT_NAME).setValue(lineString);
			}

			
			// Line 19 - Period 
			else if (lineNum==19)
			{
				GenericDefaultValue<SheffieldPeriodCode> periodField = (GenericDefaultValue<SheffieldPeriodCode>) defaults.getDefaultValue(DefaultFields.SHEFFIELD_PERIOD_CODE);
				
				if(SheffieldPeriodCode.fromCode(lineString)!=null)
				{
					periodField.setValue(SheffieldPeriodCode.fromCode(lineString));
				}
				else
				{
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidDataValue"), "Period code"));	
					continue;	
				}
			}
			
			// Line 20 - Species code 
			else if (lineNum==20)
			{
				if(!lineString.equals("?"))
				{
					defaults.getStringDefaultValue(DefaultFields.TAXON_CODE).setValue(lineString);
				}
			}
			
			// Line 21 - Interpretation and anatomical notes 
			// TODO - Parse value.remarks from these notes
			else if (lineNum==21)
			{
				if(!lineString.equals("?"))
				{
					String[] notesArray = lineString.split("~");
					
					if(notesArray.length % 3 != 0)
					{
						// Notes array does not split into threes
						addWarningToList(new ConversionWarning(WarningType.INVALID, 
								I18n.getText("sheffield.interpInvalid")));
						continue;
					}
					
					for (int i=0; i<notesArray.length; i=i+3)
					{
						if(!notesArray[i].equalsIgnoreCase("I") || !notesArray[i].equalsIgnoreCase("A"))
						{
							// Each note must begin with an I (interpretation) or an A (anatomy)
							addWarningToList(new ConversionWarning(WarningType.INVALID, 
									I18n.getText("sheffield.interpNotIorA")));
							continue;
						}
					}
					
					for (int i=1; i<notesArray.length; i=i+3)
					{
						try{Integer val = Integer.parseInt(notesArray[i]);
						} catch (NumberFormatException e)
						{
							// The second field of each note must be a year or ring number
							addWarningToList(new ConversionWarning(WarningType.INVALID, 
									I18n.getText("sheffield.interpNoNumber")));
							continue;
						}
					}
					
					defaults.getStringDefaultValue(DefaultFields.INTERPRETATION_NOTES).setValue(lineString);
				}
			}
			
			// Line 22 - Variable type
			else if (lineNum==22)
			{
				GenericDefaultValue<SheffieldVariableCode> variableField = (GenericDefaultValue<SheffieldVariableCode>) defaults.getDefaultValue(DefaultFields.SHEFFIELD_VARIABLE_TYPE);
				
				if(SheffieldVariableCode.fromCode(lineString)!=null)
				{
					variableField.setValue(SheffieldVariableCode.fromCode(lineString));
				}
				else
				{
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidDataValue"), "Data variable code"));	
					continue;	
				}
			}
			
			
		}
		
		int lineNum = 23;
		// Extract actual values
		for (int i=23; i<argFileString.length; i++)
		{

			TridasValue v = new TridasValue();
			
			if(!argFileString[i].trim().equals("H") && 
			   !argFileString[i].trim().equals("R") &&
			   !argFileString[i].trim().equals("F"))
			{
			
			v.setValue(argFileString[i].trim());
			ringWidthValues.add(v);
			log.debug("value = "+String.valueOf(argFileString[i]));
			}
			else
			{
				lineNum = i+1;
				break;
			}
		}
		
		
		// See if we can get counts
		if(argFileString[lineNum-1].trim().equals("H"))
		{
			for (int i=lineNum; i<argFileString.length; i++)
			{
				TridasValue v = null;
				try{
					v = ringWidthValues.get(i-lineNum);
				} catch (Exception e){break;}
				
				if(!argFileString[i].trim().equals("H") && 
				   !argFileString[i].trim().equals("R") &&
				   !argFileString[i].trim().equals("F"))
				{
				
				Integer count;
					
				try{ count = Integer.parseInt(argFileString[i]);
				} catch (NumberFormatException e){
					break;
				}
					
				v.setCount(count);
				log.debug("count = "+String.valueOf(count));
				}
				else
				{
					lineNum = i;
					break;
				}
			}
			
		}
		
		// Check ring count matches number of values in file
		if(defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()!=ringWidthValues.size())
		{
			this.addWarningToList(new ConversionWarning(
					WarningType.INVALID, 
					I18n.getText("fileio.valueCountMismatch")));
						
			defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(ringWidthValues.size());
		}
		
		
		
		// Now build up our measurementSeries	
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		
		// Add values to nested value(s) tags
		TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
		valuesGroup.setValues(ringWidthValues);
		ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
		valuesGroupList.add(valuesGroup);	
		
		// Add all the data to the series
		series.setValues(valuesGroupList);

		// Add series to our list
		mseriesList.add(series);


	}


	
	
	
	@Override
	public String[] getFileExtensions() {
		return new String[] {"d"};
		
	}

	@Override
	public TridasProject getProject() {
		TridasProject project = null;
		
		try{
			project = defaults.getProjectWithDefaults(false);
			TridasObject o = defaults.getDefaultTridasObject();
			TridasElement e = defaults.getDefaultTridasElement();
			TridasSample s = defaults.getDefaultTridasSample();
			TridasRadius r = defaults.getDefaultTridasRadius();
			
			if(mseriesList.size()>0)
			{
				r.setMeasurementSeries(mseriesList);
			}
			
			ArrayList<TridasRadius> radii = new ArrayList<TridasRadius>();
			radii.add(r);
			s.setRadiuses(radii);
			
			ArrayList<TridasSample> samples = new ArrayList<TridasSample>();
			samples.add(s);
			e.setSamples(samples);
			
			ArrayList<TridasElement> elements = new ArrayList<TridasElement>();
			elements.add(e);
			
			// Handle subobjects
			if(o.getObjects()!=null)
			{
				o.getObjects().get(0).setElements(elements);
			}
			else
			{
				o.setElements(elements);
			}
			
			ArrayList<TridasObject> objList = new ArrayList<TridasObject>();
			objList.add(o);
			project.setObjects(objList);
	
			} catch (NullPointerException e){
				
			} catch (IndexOutOfBoundsException e2){
				
			}
			
			
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
		return I18n.getText("sheffield.about.description");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("sheffield.about.fullName");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("sheffield.about.shortName");
	}
	
	/**
	 * Check that this is a valid Sheffield DFormat file
	 * 
	 * @param argStrings
	 * @throws InvalidDendroFileException
	 */
	private void checkFile(String[] argStrings) throws InvalidDendroFileException{
		log.debug("Checking file to see if it looks like a D Format file");
	
		// File too short to be valid
		if(argStrings.length<25)
		{
			throw new InvalidDendroFileException(I18n.getText("sheffield.incompleteHeader"), argStrings.length);
		}
		
		
		// Check none of the header lines are empty
		for (int i=0; i<24; i++)
		{
			if(argStrings[i]=="" || argStrings[i]==null)
			{
				throw new InvalidDendroFileException(I18n.getText("sheffield.blankLine"), i+1);
			}
		}
		
	}
	
	private enum NorthingEasting{
		NORTH_SOUTH,
		EAST_WEST;
	}
	
	private Double convertCoordsToDD (String coordString, NorthingEasting ne) throws ConversionWarningException
	{
		// Convert from old skool coordinate style
		if (ne == NorthingEasting.NORTH_SOUTH)
		{
		
			if(!coordString.toUpperCase().startsWith("N") && !coordString.toUpperCase().startsWith("S"))
			{
				throw new ConversionWarningException(new ConversionWarning(WarningType.NOT_STRICT, 
						I18n.getText("sheffield.errorParsingCoords")));
			}
		}
		else 
		{
			if(!coordString.toUpperCase().startsWith("E") && !coordString.toUpperCase().startsWith("W"))
			{
				throw new ConversionWarningException(new ConversionWarning(WarningType.NOT_STRICT, 
						I18n.getText("sheffield.errorParsingCoords")));
			}	
			
		}
			
		String sign = coordString.substring(0, 1);
			
		
		Integer degrees = null;
		Integer minutes = null;
		
		String[] coordArray = coordString.split("\\^");
		if(coordArray.length>2)
		{
			throw new ConversionWarningException(new ConversionWarning(WarningType.NOT_STRICT, 
					I18n.getText("sheffield.errorParsingCoords")));
		}
		
		if (coordArray.length==2)
		{
			try{
				minutes = Integer.valueOf(coordArray[1]);
			} catch (NumberFormatException e)
			{
				throw new ConversionWarningException(new ConversionWarning(WarningType.NOT_STRICT, 
						I18n.getText("sheffield.errorParsingCoords")));
			}
		}
		
		if (coordArray.length>=1)
		{
			try{
				degrees = Integer.valueOf(coordArray[0].substring(1));
			} catch (NumberFormatException e)
			{
				throw new ConversionWarningException(new ConversionWarning(WarningType.NOT_STRICT, 
						I18n.getText("sheffield.errorParsingCoords")));
			}
		}
			
		return CoordinatesUtils.getDecimalCoords(sign, degrees, minutes, null);
	}
		
		
}
