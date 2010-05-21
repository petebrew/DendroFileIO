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
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldPithCode;
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
		

		for (int i=0; i<24; i++)
		{	
			String lineString = argFileString[i];	
			
			// Line 1 - Site name/sample number
			if(i==0)
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
				defaults.getStringDefaultValue(DefaultFields.OBJECT_NAME).setValue(lineString);
			}
			
			// Line 2 - Number of rings
			if(i==1)
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
			if(i==2)
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
			if(i==3)
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
			if(i==4)
			{
				GenericDefaultValue<SheffieldDataType> dataTypeField = (GenericDefaultValue<SheffieldDataType>) defaults.getDefaultValue(DefaultFields.SHEFFIELD_DATA_TYPE); 
				dataTypeField.setValue(SheffieldDataType.fromCode(lineString));
			}
			
			// Line 6 - sapwood number or number of timbers 
			// TODO needs completing
			if(i==5)
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
			
			
			// Line 7 - edge code or chronology type TODO
			if(i==6)
			{
				
			}
			
			// Line 8 - comment TODO
			if(i==7)
			{
				if (lineString.length()>64)
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.lineNTooBig", String.valueOf(lineString.length()))));
				}
				defaults.getStringDefaultValue(DefaultFields.SERIES_COMMENT).setValue(lineString);
			}
			
			// Line 9 - UK Grid coords
			if(i==8)
			{
				// TODO We could add PROJ4 lib to convert these but not sure its worth it
			}
			
			// Line 10 - Lat/Long coords
			if(i==9)
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
					ArrayList<Double> latlongs = new ArrayList<Double>();
					latlongs.add(northing);
					latlongs.add(easting);
					Pos pos = new Pos();
					pos.setValues(latlongs);
					GenericDefaultValue<Pos> posField = (GenericDefaultValue<Pos>) defaults.getDefaultValue(DefaultFields.LAT_LONG); 
					posField.setValue(pos);
				}
				
			}
			
			// Line 11 - Pith
			if(i==10)
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
			
			
			// Line 12 - Major dimension
			if(i==11)
			{
				
			}
			
			// Line 12 - Minor dimension
			if(i==11)
			{
				
			}
			
			// Line 18 - Short title 
			if (i==17)
			{
				if (lineString.length()>=8)
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.line18TooBig")));
				}
				
				defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(lineString);
			}

			
		}
		
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
			project = defaults.getProjectWithDefaults(true);
			TridasObject o = project.getObjects().get(0);
			TridasElement e = o.getElements().get(0);
			TridasSample s = e.getSamples().get(0);
			
			if(mseriesList.size()>0)
			{
				TridasRadius r = s.getRadiuses().get(0);
				r.setMeasurementSeries(mseriesList);
			}

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
