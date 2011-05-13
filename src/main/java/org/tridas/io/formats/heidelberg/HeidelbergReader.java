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
package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHBarkType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataFormat;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDated;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHPith;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHSeriesType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHStartsOrEndsWith;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHWaldKante;
import org.tridas.io.util.CoordinatesUtils;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**
 * @author daniel
 */
public class HeidelbergReader extends AbstractDendroFileReader {
	private static final Logger log = LoggerFactory.getLogger(HeidelbergReader.class);
	
	public static final int DATA_CHARS_PER_NUMBER_REG = 6;
	public static final int DATA_CHARS_PER_NUMBER_QUAD = 5;
	public static final int DATA_NUMS_PER_LINE_QUAD = 16;
	public static final int DATA_NUMS_PER_LINE_REG = 10;
	
	private HeidelbergToTridasDefaults defaults = null;
	private ArrayList<HeidelbergSeries> series = new ArrayList<HeidelbergSeries>();
	
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
		HeidelbergSeries currSeries = null;
		
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
				currSeries = new HeidelbergSeries();
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
					currSeries = new HeidelbergSeries();
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
		

		// Loop through all the series to check dates are logical
		for (HeidelbergSeries thisSeries : series)
		{
			SafeIntYear startYear = null;
			SafeIntYear endYear = null;
			Integer yearCount = thisSeries.dataInts.size();
			Integer length = null;
			
			if(thisSeries.defaults.getStringDefaultValue(DefaultFields.LENGTH).getValue()!=null)
			{
				try{
					length = Integer.valueOf(thisSeries.defaults.getStringDefaultValue(DefaultFields.LENGTH).getValue());
				} catch (Exception e)
				{
					length = yearCount;
				}
			}
			else
			{
				length = yearCount;
			}
			
			// Check that DateBegin, DateEnd and number of values agree
			if(thisSeries.defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue()!=null)
			{
				startYear = new SafeIntYear(thisSeries.defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue().toString(), true);
			}
			if(thisSeries.defaults.getIntegerDefaultValue(DefaultFields.DATE_END).getValue()!=null)
			{
				endYear = new SafeIntYear(thisSeries.defaults.getIntegerDefaultValue(DefaultFields.DATE_END).getValue().toString(), true);
			}
			
			if(startYear!=null && endYear!=null)
			{
				// Check the difference is the same as the yearCount
				if(endYear.diff(startYear)!=length-1)
				{
					/*thisSeries.defaults.addConversionWarning(
							new ConversionWarning(
									WarningType.AMBIGUOUS, 
									I18n.getText("heidelberg.inconsistentDates"))
							);*/
					throw new InvalidDendroFileException(I18n.getText("heidelberg.inconsistentDates"));
				}
			}
			else if (startYear!=null && endYear==null)
			{
				// Use start year to set endYear based on the length
				thisSeries.defaults.getIntegerDefaultValue(DefaultFields.DATE_END).setValue(Integer.parseInt(startYear.add(length-1).toString()));					
			}
			else if (startYear==null && endYear!=null)
			{
				// Set startYear based on endYear and length
				thisSeries.defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setValue(Integer.parseInt(endYear.add(1-length).toString()));					
			}
			else
			{
				// both start and end year are null
				addWarning(new ConversionWarning(WarningType.NULL_VALUE, I18n.getText("heidelberg.noStartOrEndDate")));
			}
		}

		
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
	
	private void extractHeader(String[] argHeader, HeidelbergSeries argSeries) {
		for (int i = 0; i < argHeader.length; i++) {
			String s = argHeader[i];
			currentLineNum = i + 1; // +1 because of the HEADER line
			String[] split = s.split("=");
			if (split.length == 1) {
				argSeries.fileMetadata.put(split[0].toLowerCase(), "");
			}
			else {
				argSeries.fileMetadata.put(split[0].toLowerCase(), split[1]);
			}
		}
	}
	
	private void extractData(String[] argData, HeidelbergSeries argSeries) {
		log.debug("Data strings", argData);
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
		for (HeidelbergSeries s : series) {
			s.defaults = (HeidelbergToTridasDefaults) defaults.clone();
			HashMap<String, String> fileMetadata = s.fileMetadata;
			
			//BARK, new GenericDefaultValue<FHBarkType>());
			if(fileMetadata.containsKey("bark")){
				GenericDefaultValue<FHBarkType> bark = (GenericDefaultValue<FHBarkType>) s.defaults.getDefaultValue(DefaultFields.BARK);
				bark.setValue(FHBarkType.fromCode(fileMetadata.get("bark")));
			}
			
			//CORE_NUMBER, new StringDefaultValue());
			if(fileMetadata.containsKey("coreno")){
				s.defaults.getStringDefaultValue(DefaultFields.CORE_NUMBER).setValue(fileMetadata.get("coreno"));
			}
			
			//COUNTRY, new StringDefaultValue());
			if(fileMetadata.containsKey("country")){
				s.defaults.getStringDefaultValue(DefaultFields.COUNTRY).setValue(fileMetadata.get("country"));
			}
			
			//DATA_FORMAT, new GenericDefaultValue<FHDataFormat>());
			if(fileMetadata.containsKey("dataformat")){
				GenericDefaultValue<FHDataFormat> dataFormatField = (GenericDefaultValue<FHDataFormat>) s.defaults
				.getDefaultValue(DefaultFields.DATA_FORMAT);
				dataFormatField.setValue(FHDataFormat.valueOf(fileMetadata.get("dataformat")));
			}
			
			//DATA_TYPE, new GenericDefaultValue<FHDataType>());
			if(fileMetadata.containsKey("datatype")){
				GenericDefaultValue<FHDataType> dataTypeField = (GenericDefaultValue<FHDataType>) s.defaults
				.getDefaultValue(DefaultFields.DATA_TYPE);
				dataTypeField.setValue(FHDataType.fromCode(fileMetadata.get("datatype")));
			}
						
			//DATED, new GenericDefaultValue<FHDated>());
			FHDated dated = FHDated.Undated;
			if(fileMetadata.containsKey("dated")){
				dated = FHDated.valueOf(fileMetadata.get("dated"));
				GenericDefaultValue<FHDated> datedField = (GenericDefaultValue<FHDated>) s.defaults
				.getDefaultValue(DefaultFields.DATED);
				datedField.setValue(dated);
			}
			else
			{
				if((fileMetadata.containsKey("datebegin")) || (fileMetadata.containsKey("dateend")))
				{
					// Dated not specified, but begin and/or end date is so set to relatively dated
					dated = FHDated.RelDated;
				}
				else
				{
					// Dated not specified, neither is begin or end date, so assume undated
					dated = FHDated.Undated;
				}
				GenericDefaultValue<FHDated> datedField = (GenericDefaultValue<FHDated>) s.defaults
				.getDefaultValue(DefaultFields.DATED);
				datedField.setValue(dated);
			}
			
			
			
			//DATE_BEGIN, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("datebegin")){
					Integer val = Integer.parseInt(fileMetadata.get("datebegin"));
					if(val<=0 && dated==FHDated.Dated)
					{
						addWarning(new ConversionWarning(WarningType.AMBIGUOUS, 
								I18n.getText("general.astronomicalWarning")));
					}
					s.defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setValue(val);
				}
			} catch (NumberFormatException e){
				if(fileMetadata.get("datebegin")!="")
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidNumber", fileMetadata.get("datebegin")),
							"datebegin"));
				}
			}
			

			
			//DATE_END, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("dateend")){
					s.defaults.getIntegerDefaultValue(DefaultFields.DATE_END).setValue(Integer.parseInt(fileMetadata.get("dateend")));
				}
			} catch (NumberFormatException e){
				if(fileMetadata.get("dateend")!="")
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidNumber", fileMetadata.get("dateend")),
							"DateEnd"));
				}
			}
			

			//DATE_OF_SAMPLING, new StringDefaultValue());
			if(fileMetadata.containsKey("dateofsampling")){
				s.defaults.getStringDefaultValue(DefaultFields.DATE_OF_SAMPLING).setValue(fileMetadata.get("dateofsampling"));
			}
			
			//DISTRICT, new StringDefaultValue());
			if(fileMetadata.containsKey("district")){
				s.defaults.getStringDefaultValue(DefaultFields.DISTRICT).setValue(fileMetadata.get("district"));
			}
			
			//ELEVATION, new StringDefaultValue());
			if(fileMetadata.containsKey("elevation")){
				try{
				Boolean success = s.defaults.getDoubleDefaultValue(DefaultFields.ELEVATION).setValue(Double.parseDouble(fileMetadata.get("elevation")));
				if(success==false)
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("heidelberg.invalidElevationValue", fileMetadata.get("elevation")),
							"Elevation"));
				}
				} catch(NumberFormatException e){ 
					if(fileMetadata.get("elevation")!="")
					{
						addWarning(new ConversionWarning(WarningType.INVALID, 
								I18n.getText("fileio.invalidNumber", fileMetadata.get("elevation")),
								"Latitude"));
					}
				}
			}
			
			//ESTIMATED_TIME_PERIOD, new StringDefaultValue());
			if(fileMetadata.containsKey("estimatedtimeperiod")){
				s.defaults.getStringDefaultValue(DefaultFields.ESTIMATED_TIME_PERIOD).setValue(fileMetadata.get("estimatedtimeperiod"));
			}
			
			//FIRST_MEASUREMENT_DATE, new StringDefaultValue());
			if(fileMetadata.containsKey("firstmeasurementdate")){
				s.defaults.getStringDefaultValue(DefaultFields.FIRST_MEASUREMENT_DATE).setValue(fileMetadata.get("firstmeasurementdate"));
			}
			
			//HOUSE_NAME, new StringDefaultValue());
			if(fileMetadata.containsKey("housename")){
				s.defaults.getStringDefaultValue(DefaultFields.HOUSE_NAME).setValue(fileMetadata.get("housename"));
			}
			
			//HOUSE_NUMBER, new StringDefaultValue());	
			if(fileMetadata.containsKey("housenumber")){
				s.defaults.getStringDefaultValue(DefaultFields.HOUSE_NUMBER).setValue(fileMetadata.get("housenumber"));
			}
			
			//KEYCODE, new StringDefaultValue());
			if(fileMetadata.containsKey("keycode")){
				System.out.println(fileMetadata.get("keycode"));
				s.defaults.getStringDefaultValue(DefaultFields.KEYCODE).setValue(fileMetadata.get("keycode"));
			}
			
			//LAB_CODE, new StringDefaultValue());
			//TODO the TSAP manual has this field spelled as 'LabotaryCode' in the manual. Check this works
			if(fileMetadata.containsKey("laboratorycode")){
				s.defaults.getStringDefaultValue(DefaultFields.LAB_CODE).setValue(fileMetadata.get("laboratorycode"));
			}
			
			//LAST_REVISION_DATE, new StringDefaultValue());
			if(fileMetadata.containsKey("lastrevisiondate")){
				s.defaults.getStringDefaultValue(DefaultFields.LAST_REVISION_DATE).setValue(fileMetadata.get("lastrevisiondate"));
			}
			
			//LAST_REVISION_PERS_ID, new StringDefaultValue());
			if(fileMetadata.containsKey("lastrevisionpersid")){
				s.defaults.getStringDefaultValue(DefaultFields.LAST_REVISION_PERS_ID).setValue(fileMetadata.get("lastrevisionpersid"));
			}
			
			//LATITUDE, new DoubleDefaultValue());
			if(fileMetadata.containsKey("latitude")){
				try{
					Boolean success = s.defaults.getDoubleDefaultValue(DefaultFields.LATITUDE).setValue(CoordinatesUtils.parseLatLonFromHalfLatLongString(fileMetadata.get("latitude")));
					if(success==false)
					{
						addWarning(new ConversionWarning(WarningType.INVALID, 
								I18n.getText("location.latitude.invalid", fileMetadata.get("latitude")),
								"Latitude"));
					}
				} catch (NumberFormatException e)
				{
					addWarning(new ConversionWarning(WarningType.INVALID,
							e.getMessage(),
							"Latitude"));
				} catch(Exception e){ 
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("heidelberg.invalidCoordinate"),
							"Latitude"));
				}
			}
			
			//LENGTH, new StringDefaultValue());
			if(fileMetadata.containsKey("length")){
				s.defaults.getStringDefaultValue(DefaultFields.LENGTH).setValue(fileMetadata.get("length"));
			}
			
			//LOCATION, new StringDefaultValue());			
			if(fileMetadata.containsKey("location")){
				s.defaults.getStringDefaultValue(DefaultFields.LOCATION).setValue(fileMetadata.get("location"));
			}
			
			//LOCATION_CHARACTERISTICS, new StringDefaultValue());
			if(fileMetadata.containsKey("locationcharacteristics")){
				s.defaults.getStringDefaultValue(DefaultFields.LOCATION_CHARACTERISTICS).setValue(fileMetadata.get("locationcharacteristics"));
			}
			
			//LONGITUDE, new StringDefaultValue());
			if(fileMetadata.containsKey("longitude")){
				try{
					Boolean success = s.defaults.getDoubleDefaultValue(DefaultFields.LONGITUDE).setValue(CoordinatesUtils.parseLatLonFromHalfLatLongString(fileMetadata.get("longitude")));
					if(success==false)
					{
						addWarning(new ConversionWarning(WarningType.INVALID, 
								I18n.getText("location.longitude.invalid", fileMetadata.get("longitude")),
								"Longitude"));
					}
				}catch (NumberFormatException e)
				{
					addWarning(new ConversionWarning(WarningType.INVALID,
							e.getMessage(),
							"Longitude"));
				} catch(Exception e){ 
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("heidelberg.invalidCoordinate"),
							"Longitude"));
				}
			}
			
			//MISSING_RINGS_AFTER, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("missingringsafter")){
					s.defaults.getIntegerDefaultValue(DefaultFields.MISSING_RINGS_AFTER).setValue(Integer.parseInt(fileMetadata.get("missingringsafter")));
				}
			} catch (NumberFormatException e){
				if(fileMetadata.get("missingringsafter")!="")
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidNumber", fileMetadata.get("missingringsafter")),
							"MissingRingsAfter"));
				}
			}
			
			//MISSING_RINGS_BEFORE, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("missingringsbefore")){
					s.defaults.getIntegerDefaultValue(DefaultFields.MISSING_RINGS_BEFORE).setValue(Integer.parseInt(fileMetadata.get("missingringsbefore")));
				}
			} catch (NumberFormatException e){
				if(fileMetadata.get("missingringsbefore")!="")
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidNumber", fileMetadata.get("missingringsbefore")),
							"MissingRingsBefore"));
				}				
			}
			
			//PERSID, new StringDefaultValue());
			if(fileMetadata.containsKey("persid")){
				s.defaults.getStringDefaultValue(DefaultFields.PERS_ID).setValue(fileMetadata.get("persid"));
			}
			
			//PITH, new GenericDefaultValue<FHPith>());
			if(fileMetadata.containsKey("pith")){
				GenericDefaultValue<FHPith> pithField = (GenericDefaultValue<FHPith>) s.defaults
				.getDefaultValue(DefaultFields.PITH);
				pithField.setValue(FHPith.fromCode(fileMetadata.get("pith")));
			}
			
			//PROJECT, new StringDefaultValue());
			if(fileMetadata.containsKey("project")){
				s.defaults.getStringDefaultValue(DefaultFields.PROJECT).setValue(fileMetadata.get("project"));
			}
			
			//PROVINCE, new StringDefaultValue());
			if(fileMetadata.containsKey("province")){
				s.defaults.getStringDefaultValue(DefaultFields.PROVINCE).setValue(fileMetadata.get("province"));
			}
			
			//RADIUS_NUMBER, new StringDefaultValue());
			if(fileMetadata.containsKey("radiusnumber")){
				s.defaults.getStringDefaultValue(DefaultFields.RADIUS_NUMBER).setValue(fileMetadata.get("radiusnumber"));
			}
			
			//SAMPLING_HEIGHT, new StringDefaultValue());
			if(fileMetadata.containsKey("samplingheight")){
				s.defaults.getStringDefaultValue(DefaultFields.SAMPLING_HEIGHT).setValue(fileMetadata.get("samplingheight"));
			}
			
			//SAPWOOD_RINGS, new IntegerDefaultValue());
			try{
				if(fileMetadata.containsKey("sapwoodrings")){
					s.defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).setValue(Integer.parseInt(fileMetadata.get("sapwoodrings")));
				}
			} catch (NumberFormatException e){
				if(fileMetadata.get("sapwoodrings")!="")
				{
					addWarning(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidNumber", fileMetadata.get("sapwoodrings")),
							"SapWoodRings"));
				}
			}
			
			//SERIES_END, new GenericDefaultValue<FHStartsOrEndsWith>());
			if(fileMetadata.containsKey("SeriesEnd")){
				GenericDefaultValue<FHStartsOrEndsWith> seriesEndField = (GenericDefaultValue<FHStartsOrEndsWith>) s.defaults
				.getDefaultValue(DefaultFields.SERIES_END);
				seriesEndField.setValue(FHStartsOrEndsWith.fromCode(fileMetadata.get("SeriesEnd")));
			}
			
			//SERIES_START, new GenericDefaultValue<FHStartsOrEndsWith>());
			if(fileMetadata.containsKey("seriesstart")){
				GenericDefaultValue<FHStartsOrEndsWith> seriesStartField = (GenericDefaultValue<FHStartsOrEndsWith>) s.defaults
				.getDefaultValue(DefaultFields.SERIES_START);
				seriesStartField.setValue(FHStartsOrEndsWith.fromCode(fileMetadata.get("seriesstart")));
			}
			
			//SERIES_TYPE, new GenericDefaultValue<FHSeriesType>());
			if(fileMetadata.containsKey("seriestype")){
				GenericDefaultValue<FHSeriesType> seriesTypeField = (GenericDefaultValue<FHSeriesType>) s.defaults
				.getDefaultValue(DefaultFields.SERIES_TYPE);
				seriesTypeField.setValue(FHSeriesType.fromCode(fileMetadata.get("seriestype")));
			}
			
			//SHAPE_OF_SAMPLE, new StringDefaultValue());
			if(fileMetadata.containsKey("shapeofsample")){
				s.defaults.getStringDefaultValue(DefaultFields.SHAPE_OF_SAMPLE).setValue(fileMetadata.get("shapeofsample"));
			}
			
			//SITE_CODE, new StringDefaultValue());
			if(fileMetadata.containsKey("sitecode")){
				s.defaults.getStringDefaultValue(DefaultFields.SITE_CODE).setValue(fileMetadata.get("sitecode"));
			}
			
			//SOIL_TYPE, new StringDefaultValue());
			if(fileMetadata.containsKey("soiltype")){
				s.defaults.getStringDefaultValue(DefaultFields.SOIL_TYPE).setValue(fileMetadata.get("soiltype"));
			}
			
			//SPECIES, new GenericDefaultValue<ControlledVoc>());
			GenericDefaultValue<ControlledVoc> speciesField = (GenericDefaultValue<ControlledVoc>) s.defaults
			.getDefaultValue(DefaultFields.SPECIES);
			if(fileMetadata.containsKey("species")){
				speciesField.setValue(ITRDBTaxonConverter.getControlledVocFromCode(fileMetadata.get("species")));
			}
			else
			{
				speciesField.setValue(ITRDBTaxonConverter.getControlledVocFromCode("UNKN"));
			}
							
			//SPECIES_NAME, new StringDefaultValue());
			if(fileMetadata.containsKey("speciesname")){
				s.defaults.getStringDefaultValue(DefaultFields.SPECIES_NAME).setValue(fileMetadata.get("speciesname"));
			}
			
			//STATE, new StringDefaultValue());
			if(fileMetadata.containsKey("state")){
				s.defaults.getStringDefaultValue(DefaultFields.STATE).setValue(fileMetadata.get("state"));
			}
			
			//STEM_DISK_NUMBER, new StringDefaultValue());
			if(fileMetadata.containsKey("stemdiskno")){
				s.defaults.getStringDefaultValue(DefaultFields.STEM_DISK_NUMBER).setValue(fileMetadata.get("stemdiskno"));
			}
			
			//STREET, new StringDefaultValue());
			if(fileMetadata.containsKey("street")){
				s.defaults.getStringDefaultValue(DefaultFields.STREET).setValue(fileMetadata.get("street"));
			}
			
			//TIMBER_HEIGHT, new StringDefaultValue());
			if(fileMetadata.containsKey("timberheight")){
				s.defaults.getStringDefaultValue(DefaultFields.TIMBER_HEIGHT).setValue(fileMetadata.get("timberheight"));
			}
			
			//TIMBER_WIDTH, new StringDefaultValue());
			if(fileMetadata.containsKey("timberwidth")){
				s.defaults.getStringDefaultValue(DefaultFields.TIMBER_WIDTH).setValue(fileMetadata.get("timberwidth"));
			}
			
			//TOWN, new StringDefaultValue());
			if(fileMetadata.containsKey("town")){
				s.defaults.getStringDefaultValue(DefaultFields.TOWN).setValue(fileMetadata.get("town"));
			}
			
			//TOWN_ZIP_CODE, new StringDefaultValue());
			if(fileMetadata.containsKey("townzipcode")){
				s.defaults.getStringDefaultValue(DefaultFields.TOWN_ZIP_CODE).setValue(fileMetadata.get("townzipcode"));
			}
			
			//TREE_HEIGHT, new StringDefaultValue());
			if(fileMetadata.containsKey("treeheight")){
				s.defaults.getStringDefaultValue(DefaultFields.TREE_HEIGHT).setValue(fileMetadata.get("treeheight"));
			}
			
			//TREE_NUMBER, new StringDefaultValue());
			if(fileMetadata.containsKey("treenumber")){
				s.defaults.getStringDefaultValue(DefaultFields.TREE_NUMBER).setValue(fileMetadata.get("treenumber"));
			}
			
			//UNIT, new GenericDefaultValue<TridasUnit>());	
			GenericDefaultValue<TridasUnit> unit = (GenericDefaultValue<TridasUnit>) s.defaults
			.getDefaultValue(DefaultFields.UNIT);
			if (fileMetadata.containsKey("unit")) {
				
				TridasUnit value = new TridasUnit();
				try {
					value.setNormalTridas(UnitUtils.parseUnitString(fileMetadata.get("unit")));
				} catch (Exception e) {
					addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("fileio.invalidUnits")));
					value = null;
				}
				unit.setValue(value);
				
			}
			else {
				unit.setValue(null);
			}
			
			//WALDKANTE, new GenericDefaultValue<FHWaldKante>());
			if(fileMetadata.containsKey("waldkante")){
				GenericDefaultValue<FHWaldKante> waldKanteField = (GenericDefaultValue<FHWaldKante>) s.defaults
				.getDefaultValue(DefaultFields.WALDKANTE);
				waldKanteField.setValue(FHWaldKante.fromCode(fileMetadata.get("waldkante")));
			}
		}
	}
	
	private void populateDataInformation() {
	// nothing to populate with
	}
	
	private TridasProject createProject() {
		TridasProject project = defaults.getDefaultTridasProject();
		TridasObject object = defaults.getDefaultTridasObject();
		// the defaults populate from the element downward, so I make a new
		// element for each series in the file
		ArrayList<TridasElement> elements = new ArrayList<TridasElement>();
		
		for (HeidelbergSeries s : series) {
			TridasElement element = s.defaults.getDefaultTridasElement();
			TridasSample sample = s.defaults.getDefaultTridasSample();
			
			FHDataFormat dataType = s.dataType;
			switch (dataType) {
				case Chrono :
				case Double :
				case HalfChrono :
				case Quadro : {
					// derived series
					/*String uuidKey = "XREF-" + UUID.randomUUID();
					TridasRadiusPlaceholder radius = new TridasRadiusPlaceholder();
					TridasMeasurementSeriesPlaceholder ms = new TridasMeasurementSeriesPlaceholder();
					radius.setMeasurementSeriesPlaceholder(ms);
					ms.setId(uuidKey);*/
					
					TridasDerivedSeries series = s.defaults.getDefaultTridasDerivedSeries();
					ArrayList<TridasValue> tridasValues = new ArrayList<TridasValue>();
					
					// Add values to nested value(s) tags
					TridasValues valuesGroup = s.defaults.getTridasValuesWithDefaults();
					valuesGroup.setValues(tridasValues);
					
					// link series to sample					
					SeriesLink link = new SeriesLink();					
					link.setIdentifier(sample.getIdentifier());
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
					
					//sample.setRadiusPlaceholder(radius);
					sample.setRadiuses(null);
					project.getDerivedSeries().add(series);
				}
					break;
				case Single :
				case Tree : {
					TridasRadius radius = s.defaults.getDefaultTridasRadius();
					TridasMeasurementSeries series = s.defaults.getDefaultTridasMeasurementSeries();
					
					TridasValues valuesGroup = s.defaults.getTridasValuesWithDefaults();
					List<TridasValue> values = valuesGroup.getValues();
					
					int numDataInts = s.dataInts.size();
					String slength = s.fileMetadata.get("length");
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
		
		object.setElements(elements);
		project.getObjects().add(object);
		
		
		return project;
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"fh"};
	}
	
	private TridasProject getProject() {
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
	private static class HeidelbergSeries {
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
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"fh"};
		
		return new DendroFileFilter(exts, getShortName());

	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		TridasProject projects[] = new TridasProject[1];
		projects[0] = this.getProject();
		return projects;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getTridasContainer()
	 */
	public TridasTridas getTridasContainer() {
		TridasTridas container = new TridasTridas();
		List<TridasProject> list = Arrays.asList(getProjects());
		container.setProjects(list);
		return container;
	}
}
