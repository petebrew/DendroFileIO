package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHBarkType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataFormat;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDated;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHPith;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHSeriesType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHStartsOrEndsWith;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHWaldKante;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.StringUtils;
import org.tridas.io.warningsandexceptions.ConversionWarning;
import org.tridas.io.warningsandexceptions.InvalidDendroFileException;
import org.tridas.io.warningsandexceptions.ConversionWarning.WarningType;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasurementSeriesPlaceholder;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRadiusPlaceholder;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.SeriesLink.IdRef;

/**
 * @author daniel
 */
public class HeidelbergReader extends AbstractDendroFileReader {
	private static final SimpleLogger log = new SimpleLogger(HeidelbergReader.class);
	
	public static final int DATA_CHARS_PER_NUMBER_REG = 6;
	public static final int DATA_CHARS_PER_NUMBER_QUAD = 5;
	public static final int DATA_NUMS_PER_LINE_QUAD = 16;
	public static final int DATA_NUMS_PER_LINE_REG = 10;
	
	private HeidelbergToTridasDefaults defaults = null;
	private ArrayList<HeidelbergMeasurementSeries> series = new ArrayList<HeidelbergMeasurementSeries>();
	
	private int currentLineNum = 0;
	

	
	public HeidelbergReader() {
		super(HeidelbergToTridasDefaults.class);
	}
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		log.debug("Parsing: " + argFileString);
		defaults = (HeidelbergToTridasDefaults) argDefaultFields;
		
		// first lets see if we look like a heidelberg file
		checkFile(argFileString);
		int fileLength = argFileString.length;
		int lineNum = 0;
		HeidelbergMeasurementSeries currSeries = null;
		
		while (lineNum < fileLength) {
			currentLineNum = lineNum; // update line num
			String line = argFileString[lineNum];
			
			if (line.startsWith("HEADER:")) {
				lineNum++;// get off header line
				currentLineNum = lineNum; // update line num
				line = argFileString[lineNum];
				
				ArrayList<String> header = new ArrayList<String>();
				while (!line.startsWith("DATA")) {
					header.add(line);
					currentLineNum = ++lineNum; // update line num
					line = argFileString[lineNum];
				}
				currSeries = new HeidelbergMeasurementSeries();
				extractHeader(header.toArray(new String[0]), currSeries);
			}
			else if (line.startsWith("DATA:")) {
				// see what kind of data is here
				FHDataFormat dataType = FHDataFormat.valueOf(line.substring(line.indexOf(":") + 1));
				lineNum++;
				line = argFileString[lineNum];
				currentLineNum = lineNum; // update line num
				
				ArrayList<String> data = new ArrayList<String>();
				while (lineNum < fileLength) {
					line = argFileString[lineNum];
					if (line.startsWith("HEADER")) {
						break;
					}
					data.add(line);
					currentLineNum = ++lineNum; // update line num
				}
				if (currSeries == null) {
					currSeries = new HeidelbergMeasurementSeries();
				}
				currSeries.dataType = dataType;
				extractData(data.toArray(new String[0]), currSeries);
				series.add(currSeries);
				currSeries = null;
			}
			else {
				log.error("Found unknown line");
			}
		}
		
		populateHeaderInformation();
		populateDataInformation();
	}
	
	// check file to see if it generally looks like a Heidelberg file
	// this should catch any errors for reading the file, so we don't have to check later
	private void checkFile(String[] argStrings) throws InvalidDendroFileException {
		log.debug("Checking file to see if it looks like a Heidelberg file");
		
		if (!argStrings[0].startsWith("HEADER") && !argStrings[0].startsWith("DATA")) {
			log.error(I18n.getText("heidelberg.firstLineWrong"));
			throw new InvalidDendroFileException(I18n.getText("heidelberg.firstLineWrong"), 1);
		}
		
		FHDataFormat dataFormat = null;
		boolean inHeader = true;
		for (int i = 0; i < argStrings.length; i++) {
			currentLineNum = i; // update line number
			String s = argStrings[i];
			if (s.startsWith("HEADER")) {
				inHeader = true;
				continue;
			}
			else if (s.startsWith("DATA")) {
				inHeader = false;
				String dataTypeString = s.substring(s.indexOf(":") + 1).trim();
				try {
					dataFormat = FHDataFormat.valueOf(dataTypeString);
				} catch (Exception e) {
					log.error(I18n.getText("heidelberg.failedToInterpretDataTypeString", dataTypeString));
					throw new InvalidDendroFileException(I18n.getText("heidelberg.failedToInterpretDataTypeString",
							dataTypeString), i + 1);
				}
				continue;
			}
			
			// so we're in the header or data
			
			if (s.equals("")) {
				// empty line?
				continue;
			}
			
			String[] nums; // out here so we don't get compile errors
			if (inHeader) {
				// in header!
				if (!s.contains("=")) {
					log.error(I18n.getText("heidelberg.headerNotKeyValuePair"));
					throw new InvalidDendroFileException(I18n.getText("heidelberg.headerNotKeyValuePair"), i + 1);
				}
			}
			else {
				switch (dataFormat) {
					case Chrono :
					case Quadro :
						// 4 nums per measurement
						nums = StringUtils.chopString(s, DATA_CHARS_PER_NUMBER_QUAD);
						if (nums.length != DATA_NUMS_PER_LINE_QUAD) {
							throw new InvalidDendroFileException(I18n.getText("heidelberg.wrongNumValsPerLine", "Quad",
									DATA_CHARS_PER_NUMBER_QUAD + ""), i + 1);
						}
						try {
							for (String num : nums) {
								Integer.parseInt(num.trim());
							}
						} catch (NumberFormatException e) {
							log.error(I18n.getText("fileio.invalidDataValue"));
							throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i + 1);
						}
						
						break;
					case HalfChrono :
					case Double :
						// 2 nums per measurement
						nums = StringUtils.chopString(s, DATA_CHARS_PER_NUMBER_REG);
						if (nums.length != DATA_NUMS_PER_LINE_REG) {
							throw new InvalidDendroFileException(I18n.getText("heidelberg.wrongNumValsPerLine",
									"Double", DATA_NUMS_PER_LINE_REG + ""), i + 1);
						}
						try {
							for (String num : nums) {
								Integer.parseInt(num.trim());
							}
						} catch (NumberFormatException e) {
							log.error(I18n.getText("fileio.invalidDataValue"));
							throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i + 1);
						}
						break;
					case Single :
					case Tree :
						if (s.length() >= 6 && s.contains(" ")) {
							// multi-row format
							nums = StringUtils.chopString(s, DATA_CHARS_PER_NUMBER_REG);
							try {
								for (String num : nums) {
									Integer.parseInt(num.trim());
								}
							} catch (NumberFormatException e) {
								log.error(I18n.getText("fileio.invalidDataValue"));
								throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i + 1);
							}
						}
						else {
							// single row format
							try {
								Integer.parseInt(s.trim());
							} catch (NumberFormatException e) {
								log.error(I18n.getText("fileio.invalidDataValue"));
								throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i + 1);
							}
						}
						break;
				}
			}
		}
	}
	
	private void extractHeader(String[] argHeader, HeidelbergMeasurementSeries argSeries) {
		for (int i = 0; i < argHeader.length; i++) {
			String s = argHeader[i];
			currentLineNum = i + 1; // +1 because of the HEADER line
			String[] split = s.split("=");
			if (split.length == 1) {
				argSeries.fileMetadata.put(split[0], "");
			}
			else {
				argSeries.fileMetadata.put(split[0], split[1]);
			}
		}
	}
	
	private void extractData(String[] argData, HeidelbergMeasurementSeries argSeries) {
		log.dbo(DebugLevel.L6_VERBOSE, "Data strings", argData);
		ArrayList<Integer> ints = new ArrayList<Integer>();
		switch (argSeries.dataType) {
			case Chrono :
			case Quadro :
				for (int i = 0; i < argData.length; i++) {
					String line = argData[i];
					currentLineNum++;
					String[] s = StringUtils.chopString(line, DATA_CHARS_PER_NUMBER_QUAD);
					for (int j = 0; j < s.length; j++) {
						// ignore every 3rd and 4th entry
						if ((j + 2) % 4 == 0 || (j + 1) % 4 == 0) {
							continue;
						}
						ints.add(Integer.parseInt(s[j].trim()));
					}
				}
				break;
			case Double :
			case HalfChrono :
				for (int i = 0; i < argData.length; i++) {
					String line = argData[i];
					currentLineNum++;
					String[] s = StringUtils.chopString(line, DATA_CHARS_PER_NUMBER_REG);
					for (int j = 0; j < s.length; j++) {
						ints.add(Integer.parseInt(s[j].trim()));
					}
				}
				break;
			case Single :
			case Tree :
				if (argData[0].length() >= 6 && argData[0].contains(" ")) {
					// we are in multi-column format
					for (int i = 0; i < argData.length; i++) {
						String line = argData[i];
						currentLineNum++;
						String[] s = StringUtils.chopString(line, DATA_CHARS_PER_NUMBER_REG);
						for (int j = 0; j < s.length; j++) {
							ints.add(Integer.parseInt(s[j].trim()));
						}
					}
				}
				else {
					// single column format
					for (int i = 0; i < argData.length; i++) {
						String line = argData[i];
						currentLineNum++;
						ints.add(Integer.parseInt(line.trim()));
					}
				}
				break;
		}
		
		argSeries.dataInts.addAll(ints);
	}
	
	@SuppressWarnings("unchecked")
	private void populateHeaderInformation() {
		// clone a new default for each series, and add corresponding metadata from that
		// series to it's defaults
		for (HeidelbergMeasurementSeries s : series) {
			s.defaults = (HeidelbergToTridasDefaults) defaults.clone();
			HashMap<String, String> fileMetadata = s.fileMetadata;
			
			//BARK, new GenericDefaultValue<FHBarkType>());
			if(fileMetadata.containsKey("Bark")){
				GenericDefaultValue<FHBarkType> bark = (GenericDefaultValue<FHBarkType>) s.defaults.getDefaultValue(DefaultFields.BARK);
				bark.setValue(FHBarkType.fromCode(fileMetadata.get("Bark")));
			}
			
			//CORE_NUMBER, new StringDefaultValue());
			if(fileMetadata.containsKey("CoreNo")){
				s.defaults.getStringDefaultValue(DefaultFields.CORE_NUMBER).setValue(fileMetadata.get("CoreNo"));
			}
			
			//COUNTRY, new StringDefaultValue());
			if(fileMetadata.containsKey("Country")){
				s.defaults.getStringDefaultValue(DefaultFields.COUNTRY).setValue(fileMetadata.get("Country"));
			}
			
			//DATA_FORMAT, new GenericDefaultValue<FHDataFormat>());
			if(fileMetadata.containsKey("DataFormat")){
				GenericDefaultValue<FHDataFormat> dataFormatField = (GenericDefaultValue<FHDataFormat>) s.defaults
				.getDefaultValue(DefaultFields.DATA_FORMAT);
				dataFormatField.setValue(FHDataFormat.valueOf(fileMetadata.get("DataFormat")));
			}
			
			//DATA_TYPE, new GenericDefaultValue<FHDataType>());
			if(fileMetadata.containsKey("DataType")){
				GenericDefaultValue<FHDataType> dataTypeField = (GenericDefaultValue<FHDataType>) s.defaults
				.getDefaultValue(DefaultFields.DATA_TYPE);
				dataTypeField.setValue(FHDataType.fromCode(fileMetadata.get("DataType")));
			}
			
			//DATE_BEGIN, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("DateBegin")){
					s.defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setValue(Integer.parseInt(fileMetadata.get("DateBegin")));
				}
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("fileio.invalidNumber", fileMetadata.get("DateBegin")),
						"DateBegin"));		
			}
			
			//DATED, new GenericDefaultValue<FHDated>());
			if(fileMetadata.containsKey("Dated")){
				GenericDefaultValue<FHDated> datedField = (GenericDefaultValue<FHDated>) s.defaults
				.getDefaultValue(DefaultFields.DATED);
				datedField.setValue(FHDated.valueOf(fileMetadata.get("Dated")));
			}
			
			//DATE_END, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("DateEnd")){
					s.defaults.getIntegerDefaultValue(DefaultFields.DATE_END).setValue(Integer.parseInt(fileMetadata.get("DateEnd")));
				}
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("fileio.invalidNumber", fileMetadata.get("DateEnd")),
						"DateEnd"));
			}
			
			//DATE_OF_SAMPLING, new StringDefaultValue());
			if(fileMetadata.containsKey("DateOfSampling")){
				s.defaults.getStringDefaultValue(DefaultFields.DATE_OF_SAMPLING).setValue(fileMetadata.get("DateOfSampling"));
			}
			
			//DISTRICT, new StringDefaultValue());
			if(fileMetadata.containsKey("District")){
				s.defaults.getStringDefaultValue(DefaultFields.DISTRICT).setValue(fileMetadata.get("District"));
			}
			
			//ELEVATION, new StringDefaultValue());
			if(fileMetadata.containsKey("Elevation")){
				try{
				Boolean success = s.defaults.getDoubleDefaultValue(DefaultFields.ELEVATION).setValue(Double.parseDouble(fileMetadata.get("Elevation")));
				if(success==false)
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("heidelberg.invalidElevationValue", fileMetadata.get("Elevation")),
							"Elevation"));
				}
				} catch(NumberFormatException e){ 
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidNumber", fileMetadata.get("Elevation")),
							"Latitude"));
				}
			}
			
			//ESTIMATED_TIME_PERIOD, new StringDefaultValue());
			if(fileMetadata.containsKey("EstimatedTimePeriod")){
				s.defaults.getStringDefaultValue(DefaultFields.ESTIMATED_TIME_PERIOD).setValue(fileMetadata.get("EstimatedTimePeriod"));
			}
			
			//FIRST_MEASUREMENT_DATE, new StringDefaultValue());
			if(fileMetadata.containsKey("FirstMeasurementDate")){
				s.defaults.getStringDefaultValue(DefaultFields.FIRST_MEASUREMENT_DATE).setValue(fileMetadata.get("FirstMeasurementDate"));
			}
			
			//HOUSE_NAME, new StringDefaultValue());
			if(fileMetadata.containsKey("HouseName")){
				s.defaults.getStringDefaultValue(DefaultFields.HOUSE_NAME).setValue(fileMetadata.get("HouseName"));
			}
			
			//HOUSE_NUMBER, new StringDefaultValue());	
			if(fileMetadata.containsKey("HouseNumber")){
				s.defaults.getStringDefaultValue(DefaultFields.HOUSE_NUMBER).setValue(fileMetadata.get("HouseNumber"));
			}
			
			//KEYCODE, new StringDefaultValue());
			if(fileMetadata.containsKey("KeyCode")){
				s.defaults.getStringDefaultValue(DefaultFields.KEYCODE).setValue(fileMetadata.get("KeyCode"));
			}
			
			//LAB_CODE, new StringDefaultValue());
			//TODO the TSAP manual has this field spelled as 'LabotaryCode' in the manual. Check this works
			if(fileMetadata.containsKey("LaboratoryCode")){
				s.defaults.getStringDefaultValue(DefaultFields.LAB_CODE).setValue(fileMetadata.get("LaboratoryCode"));
			}
			
			//LAST_REVISION_DATE, new StringDefaultValue());
			if(fileMetadata.containsKey("LastRevisionDate")){
				s.defaults.getStringDefaultValue(DefaultFields.LAST_REVISION_DATE).setValue(fileMetadata.get("LastRevisionDate"));
			}
			
			//LAST_REVISION_PERS_ID, new StringDefaultValue());
			if(fileMetadata.containsKey("LastRevisionPersID")){
				s.defaults.getStringDefaultValue(DefaultFields.LAST_REVISION_PERS_ID).setValue(fileMetadata.get("LastRevisionPersID"));
			}
			
			//LATITUDE, new DoubleDefaultValue());
			if(fileMetadata.containsKey("Latitude")){
				try{
				Boolean success = s.defaults.getDoubleDefaultValue(DefaultFields.LATITUDE).setValue(Double.parseDouble(fileMetadata.get("Latitude")));
				if(success==false)
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("location.latitude.invalid", fileMetadata.get("Latitude")),
							"Latitude"));
				}
				} catch(NumberFormatException e){ 
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("heidelberg.invalidCoordinate"),
							"Latitude"));
				}
			}
			
			//LENGTH, new StringDefaultValue());
			if(fileMetadata.containsKey("Length")){
				s.defaults.getStringDefaultValue(DefaultFields.LENGTH).setValue(fileMetadata.get("Length"));
			}
			
			//LOCATION, new StringDefaultValue());			
			if(fileMetadata.containsKey("Location")){
				s.defaults.getStringDefaultValue(DefaultFields.LOCATION).setValue(fileMetadata.get("Location"));
			}
			
			//LOCATION_CHARACTERISTICS, new StringDefaultValue());
			if(fileMetadata.containsKey("LocationCharacteristics")){
				s.defaults.getStringDefaultValue(DefaultFields.LOCATION_CHARACTERISTICS).setValue(fileMetadata.get("LocationCharacteristics"));
			}
			
			//LONGITUDE, new StringDefaultValue());
			if(fileMetadata.containsKey("Longitude")){
				try{
				Boolean success = s.defaults.getDoubleDefaultValue(DefaultFields.LONGITUDE).setValue(Double.parseDouble(fileMetadata.get("Longitude")));
				if(success==false)
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("location.longitude.invalid", fileMetadata.get("Longitude")),
							"Longitude"));
				}
				} catch(NumberFormatException e){ 
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("heidelberg.invalidCoordinate"),
							"Longitude"));
				}
			}
			
			//MISSING_RINGS_AFTER, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("MissingRingsAfter")){
					s.defaults.getIntegerDefaultValue(DefaultFields.MISSING_RINGS_AFTER).setValue(Integer.parseInt(fileMetadata.get("MissingRingsAfter")));
				}
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("fileio.invalidNumber", fileMetadata.get("MissingRingsAfter")),
						"MissingRingsAfter"));
			}
			
			//MISSING_RINGS_BEFORE, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("MissingRingsBefore")){
					s.defaults.getIntegerDefaultValue(DefaultFields.MISSING_RINGS_BEFORE).setValue(Integer.parseInt(fileMetadata.get("MissingRingsBefore")));
				}
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("fileio.invalidNumber", fileMetadata.get("MissingRingsBefore")),
						"MissingRingsBefore"));
			}
			
			//PITH, new GenericDefaultValue<FHPith>());
			if(fileMetadata.containsKey("Pith")){
				GenericDefaultValue<FHPith> pithField = (GenericDefaultValue<FHPith>) s.defaults
				.getDefaultValue(DefaultFields.PITH);
				pithField.setValue(FHPith.fromCode(fileMetadata.get("Pith")));
			}
			
			//PROJECT, new StringDefaultValue());
			if(fileMetadata.containsKey("Project")){
				s.defaults.getStringDefaultValue(DefaultFields.PROJECT).setValue(fileMetadata.get("Project"));
			}
			
			//PROVINCE, new StringDefaultValue());
			if(fileMetadata.containsKey("Province")){
				s.defaults.getStringDefaultValue(DefaultFields.PROVINCE).setValue(fileMetadata.get("Province"));
			}
			
			//RADIUS_NUMBER, new StringDefaultValue());
			if(fileMetadata.containsKey("RadiusNumber")){
				s.defaults.getStringDefaultValue(DefaultFields.RADIUS_NUMBER).setValue(fileMetadata.get("RadiusNumber"));
			}
			
			//SAMPLING_HEIGHT, new StringDefaultValue());
			if(fileMetadata.containsKey("SamplingHeight")){
				s.defaults.getStringDefaultValue(DefaultFields.SAMPLING_HEIGHT).setValue(fileMetadata.get("SamplingHeight"));
			}
			
			//SAPWOOD_RINGS, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("SapWoodRings")){
					s.defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).setValue(Integer.parseInt(fileMetadata.get("SapWoodRings")));
				}
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("fileio.invalidNumber", fileMetadata.get("SapWoodRings")),
						"SapWoodRings"));
			}
			
			//SERIES_END, new GenericDefaultValue<FHStartsOrEndsWith>());
			if(fileMetadata.containsKey("SeriesEnd")){
				GenericDefaultValue<FHStartsOrEndsWith> seriesEndField = (GenericDefaultValue<FHStartsOrEndsWith>) s.defaults
				.getDefaultValue(DefaultFields.SERIES_END);
				seriesEndField.setValue(FHStartsOrEndsWith.fromCode(fileMetadata.get("SeriesEnd")));
			}
			
			//SERIES_START, new GenericDefaultValue<FHStartsOrEndsWith>());
			if(fileMetadata.containsKey("SeriesStart")){
				GenericDefaultValue<FHStartsOrEndsWith> seriesStartField = (GenericDefaultValue<FHStartsOrEndsWith>) s.defaults
				.getDefaultValue(DefaultFields.SERIES_START);
				seriesStartField.setValue(FHStartsOrEndsWith.fromCode(fileMetadata.get("SeriesStart")));
			}
			
			//SERIES_TYPE, new GenericDefaultValue<FHSeriesType>());
			if(fileMetadata.containsKey("SeriesType")){
				GenericDefaultValue<FHSeriesType> seriesTypeField = (GenericDefaultValue<FHSeriesType>) s.defaults
				.getDefaultValue(DefaultFields.SERIES_TYPE);
				seriesTypeField.setValue(FHSeriesType.fromCode(fileMetadata.get("SeriesType")));
			}
			
			//SHAPE_OF_SAMPLE, new StringDefaultValue());
			if(fileMetadata.containsKey("ShapeOfSample")){
				s.defaults.getStringDefaultValue(DefaultFields.SHAPE_OF_SAMPLE).setValue(fileMetadata.get("ShapeOfSample"));
			}
			
			//SITE_CODE, new StringDefaultValue());
			if(fileMetadata.containsKey("SiteCode")){
				s.defaults.getStringDefaultValue(DefaultFields.SITE_CODE).setValue(fileMetadata.get("SiteCode"));
			}
			
			//SOIL_TYPE, new StringDefaultValue());
			if(fileMetadata.containsKey("SoilType")){
				s.defaults.getStringDefaultValue(DefaultFields.SOIL_TYPE).setValue(fileMetadata.get("SoilType"));
			}
			
			//SPECIES, new GenericDefaultValue<ControlledVoc>());
			if(fileMetadata.containsKey("Species")){
				GenericDefaultValue<ControlledVoc> speciesField = (GenericDefaultValue<ControlledVoc>) s.defaults
				.getDefaultValue(DefaultFields.SPECIES);
				speciesField.setValue(ITRDBTaxonConverter.getControlledVocFromCode(fileMetadata.get("Species")));
			}
							
			//SPECIES_NAME, new StringDefaultValue());
			if(fileMetadata.containsKey("SpeciesName")){
				s.defaults.getStringDefaultValue(DefaultFields.SPECIES_NAME).setValue(fileMetadata.get("SpeciesName"));
			}
			
			//STATE, new StringDefaultValue());
			if(fileMetadata.containsKey("State")){
				s.defaults.getStringDefaultValue(DefaultFields.STATE).setValue(fileMetadata.get("State"));
			}
			
			//STEM_DISK_NUMBER, new StringDefaultValue());
			if(fileMetadata.containsKey("StemDiskNo")){
				s.defaults.getStringDefaultValue(DefaultFields.STEM_DISK_NUMBER).setValue(fileMetadata.get("StemDiskNo"));
			}
			
			//STREET, new StringDefaultValue());
			if(fileMetadata.containsKey("Street")){
				s.defaults.getStringDefaultValue(DefaultFields.STREET).setValue(fileMetadata.get("Street"));
			}
			
			//TIMBER_HEIGHT, new StringDefaultValue());
			if(fileMetadata.containsKey("TimberHeight")){
				s.defaults.getStringDefaultValue(DefaultFields.TIMBER_HEIGHT).setValue(fileMetadata.get("TimberHeight"));
			}
			
			//TIMBER_WIDTH, new StringDefaultValue());
			if(fileMetadata.containsKey("TimberWidth")){
				s.defaults.getStringDefaultValue(DefaultFields.TIMBER_WIDTH).setValue(fileMetadata.get("TimberWidth"));
			}
			
			//TOWN, new StringDefaultValue());
			if(fileMetadata.containsKey("Town")){
				s.defaults.getStringDefaultValue(DefaultFields.TOWN).setValue(fileMetadata.get("Town"));
			}
			
			//TOWN_ZIP_CODE, new StringDefaultValue());
			if(fileMetadata.containsKey("TownZipCode")){
				s.defaults.getStringDefaultValue(DefaultFields.TOWN_ZIP_CODE).setValue(fileMetadata.get("TownZipCode"));
			}
			
			//TREE_HEIGHT, new StringDefaultValue());
			if(fileMetadata.containsKey("TreeHeight")){
				s.defaults.getStringDefaultValue(DefaultFields.TREE_HEIGHT).setValue(fileMetadata.get("TreeHeight"));
			}
			
			//TREE_NUMBER, new StringDefaultValue());
			if(fileMetadata.containsKey("TreeNumber")){
				s.defaults.getStringDefaultValue(DefaultFields.TREE_NUMBER).setValue(fileMetadata.get("TreeNumber"));
			}
			
			//UNIT, new GenericDefaultValue<TridasUnit>());	
			GenericDefaultValue<TridasUnit> unit = (GenericDefaultValue<TridasUnit>) s.defaults
			.getDefaultValue(DefaultFields.UNIT);
			if (fileMetadata.containsKey("Unit")) {
				String units = fileMetadata.get("Unit");
				TridasUnit value = new TridasUnit();
				if (units.equals("mm")) {
					value.setNormalTridas(NormalTridasUnit.MILLIMETRES);
				}
				else if (units.equals("1/10 mm")) {
					value.setNormalTridas(NormalTridasUnit.TENTH_MM);
				}
				else if (units.equals("1/100 mm")) {
					value.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
				}
				else if (units.equals("1/1000 mm")) {
					value.setNormalTridas(NormalTridasUnit.MICROMETRES);
				}
				else {
					value = null;
					addWarning(new ConversionWarning(WarningType.NULL_VALUE, I18n.getText("fileio.noUnitsDetected")));
				}
				unit.setValue(value);
			}
			else {
				unit.setValue(null);
			}
			
			//WALDKANTE, new GenericDefaultValue<FHWaldKante>());
			if(fileMetadata.containsKey("WaldKante")){
				GenericDefaultValue<FHWaldKante> waldKanteField = (GenericDefaultValue<FHWaldKante>) s.defaults
				.getDefaultValue(DefaultFields.WALDKANTE);
				waldKanteField.setValue(FHWaldKante.fromCode(fileMetadata.get("WaldKante")));
			}
		}
	}
	
	private void populateDataInformation() {
	// nothing to populate with
	}
	
	private TridasProject createProject() {
		TridasProject project = defaults.getProjectWithDefaults();
		TridasObject object = defaults.getObjectWithDefaults();
		// the defaults populate from the element downward, so I make a new
		// element for each series in the file
		ArrayList<TridasElement> elements = new ArrayList<TridasElement>();
		
		for (HeidelbergMeasurementSeries s : series) {
			TridasElement element = s.defaults.getElementWithDefaults();
			TridasSample sample = s.defaults.getSampleWithDefaults();
			
			FHDataFormat dataType = s.dataType;
			switch (dataType) {
				case Chrono :
				case Double :
				case HalfChrono :
				case Quadro : {
					// derived series
					String uuidKey = "XREF-" + UUID.randomUUID();
					TridasRadiusPlaceholder radius = new TridasRadiusPlaceholder();
					TridasMeasurementSeriesPlaceholder ms = new TridasMeasurementSeriesPlaceholder();
					radius.setMeasurementSeriesPlaceholder(ms);
					ms.setId(uuidKey);
					
					TridasDerivedSeries series = s.defaults.getDerivedSeriesWithDefaults();
					ArrayList<TridasValue> tridasValues = new ArrayList<TridasValue>();
					
					// Add values to nested value(s) tags
					TridasValues valuesGroup = s.defaults.getTridasValuesWithDefaults();
					valuesGroup.setValues(tridasValues);
					
					// link series to placeholder
					IdRef idref = new IdRef();
					idref.setRef(ms);
					SeriesLink link = new SeriesLink();
					link.setIdRef(idref);
					series.getLinkSeries().getSeries().add(link);
					series.getValues().add(valuesGroup);
					
					int numDataInts = s.dataInts.size();
					String slength = s.fileMetadata.get("Length");
					if (slength != null) {
						try {
							numDataInts = Integer.parseInt(slength) * 2; // count, value
						} catch (Exception e) {}
					}
					for (int i = 0; i < numDataInts; i += 2) {
						int width = s.dataInts.get(i);
						int count = s.dataInts.get(i + 1);
						TridasValue val = new TridasValue();
						val.setCount(count);
						val.setValue(width + "");
						tridasValues.add(val);
					}
					
					sample.setRadiusPlaceholder(radius);
					sample.setRadiuses(null);
					project.getDerivedSeries().add(series);
				}
					break;
				case Single :
				case Tree : {
					TridasRadius radius = defaults.getRadiusWithDefaults(false);
					TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
					
					TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
					List<TridasValue> values = valuesGroup.getValues();
					
					int numDataInts = s.dataInts.size();
					String slength = s.fileMetadata.get("Length");
					if (slength != null) {
						try {
							numDataInts = Integer.parseInt(slength); // value
						} catch (Exception e) {}
					}
					if (numDataInts > s.dataInts.size()) {
						log.error("Incorrect length: " + numDataInts);
						// throw ConversionWarning()
						numDataInts = s.dataInts.size();
					}
					for (int i = 0; i < numDataInts; i++) {
						TridasValue val = new TridasValue();
						val.setValue(s.dataInts.get(i) + "");
						values.add(val);
					}
					
					series.getValues().add(valuesGroup);
					radius.getMeasurementSeries().add(series);
					sample.getRadiuses().add(radius);
				}
					break;
			}
			
			element.getSamples().add(sample);
			
			/*
			 * TODO We can't add all the tags as genericFields to the Tridas
			 * element as many should be associated with other TRiDaS entities.
			 * ArrayList<TridasGenericField> metaData = new
			 * ArrayList<TridasGenericField>();
			 * for(String key : s.fileMetadata.keySet()){
			 * TridasGenericField generic = new TridasGenericField();
			 * generic.setName(key);
			 * generic.setValue(s.fileMetadata.get(key));
			 * metaData.add(generic);
			 * }
			 * element.setGenericFields(metaData);
			 */
			elements.add(element);
		}
		
		project.getObjects().add(object);
		object.setElements(elements);
		
		return project;
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"fh"};
	}
	
	@Override
	public TridasProject getProject() {
		return createProject();
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
		return currentLineNum + 1; // plus one because line numbers start at 1, not 0
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("heidelberg.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("heidelberg.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("heidelberg.about.shortName");
	}
	
	/**
	 * Class to store the measurement series data
	 * 
	 * @author daniel
	 */
	private static class HeidelbergMeasurementSeries {
		public FHDataFormat dataType;
		public HeidelbergToTridasDefaults defaults;
		public final HashMap<String, String> fileMetadata = new HashMap<String, String>();
		public final ArrayList<Integer> dataInts = new ArrayList<Integer>();
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		currentLineNum = -1;
		defaults = null;
		series.clear();
	}
}
