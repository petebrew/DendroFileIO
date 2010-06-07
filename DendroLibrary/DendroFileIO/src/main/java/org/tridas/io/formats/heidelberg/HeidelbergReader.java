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
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.StringUtils;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
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

import com.sun.xml.bind.v2.runtime.unmarshaller.IntArrayData;

/**
 * 
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
	
	private enum DATA_TYPE{
		Double, Single, Chrono, HalfChrono, Quadro, Tree // formatted this way to match the string
	}
	
	public HeidelbergReader() {
		super(HeidelbergToTridasDefaults.class);
	}

	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException {
		log.debug("Parsing: "+argFileString);
		defaults = (HeidelbergToTridasDefaults) argDefaultFields;
		
		// first lets see if we look like a heidelberg file
		checkFile(argFileString);
		int fileLength = argFileString.length;
		int lineNum = 0;
		HeidelbergMeasurementSeries currSeries = null;
		
		while(lineNum < fileLength){
			currentLineNum = lineNum; // update line num
			String line = argFileString[lineNum];
			
			if(line.startsWith("HEADER:")){
				lineNum++;// get off header line
				currentLineNum = lineNum; // update line num
				line = argFileString[lineNum];
				
				ArrayList<String> header = new ArrayList<String>();
				while(!line.startsWith("DATA")){
					header.add(line);
					currentLineNum = ++lineNum; // update line num
					line = argFileString[lineNum];
				}
				currSeries = new HeidelbergMeasurementSeries();
				extractHeader(header.toArray(new String[0]), currSeries);
			}
			else if(line.startsWith("DATA:")){
				// see what kind of data is here
				DATA_TYPE dataType = DATA_TYPE.valueOf(line.substring(line.indexOf(":")+1));
				lineNum++;
				line = argFileString[lineNum];
				currentLineNum = lineNum; // update line num
				
				ArrayList<String> data = new ArrayList<String>();
				while(lineNum<fileLength){
					line = argFileString[lineNum];
					if(line.startsWith("HEADER")){
						break;
					}
					data.add(line);
					currentLineNum = ++lineNum; // update line num
				}
				if(currSeries == null){
					currSeries = new HeidelbergMeasurementSeries();
				}
				currSeries.dataType = dataType;
				extractData(data.toArray(new String[0]), currSeries);
				series.add(currSeries);
				currSeries = null;
			}else{
				log.error("Found unknown line");
			}
		}
		
		populateHeaderInformation();
		populateDataInformation();
	}
	
	// check file to see if it generally looks like a Heidelberg file
	// this should catch any errors for reading the file, so we don't have to check later
	private void checkFile(String[] argStrings) throws InvalidDendroFileException{
		log.debug("Checking file to see if it looks like a Heidelberg file");
		
		if(!argStrings[0].startsWith("HEADER") && !argStrings[0].startsWith("DATA")){
			log.error(I18n.getText("heidelberg.firstLineWrong"));
			throw new InvalidDendroFileException(I18n.getText("heidelberg.firstLineWrong"), 1);
		}
		
		DATA_TYPE dataType = null;
		boolean inHeader = true;
		for(int i=0; i<argStrings.length; i++){
			currentLineNum = i; // update line number
			String s = argStrings[i];
			if(s.startsWith("HEADER")){
				inHeader = true;
				continue;
			}else if(s.startsWith("DATA")){
				inHeader = false;
				String dataTypeString = s.substring(s.indexOf(":")+1).trim();
				try{
					dataType = DATA_TYPE.valueOf(dataTypeString);
				}catch(Exception e){
					log.error(I18n.getText("heidelberg.failedToInterpretDataTypeString", dataTypeString));
					throw new InvalidDendroFileException(
							I18n.getText("heidelberg.failedToInterpretDataTypeString", 
									dataTypeString), i+1);
				}
				continue;
			}
			
			// so we're in the header or data
			
			if(s.equals("")){
				//empty line?
				continue;
			}
			
			String[] nums; // out here so we don't get compile errors
			if(inHeader){
				// in header!
				if(!s.contains("=")){
					log.error(I18n.getText("heidelberg.headerNotKeyValuePair"));
					throw new InvalidDendroFileException(I18n.getText("heidelberg.headerNotKeyValuePair"), i+1);
				}
			}else{
				switch (dataType) {
				case Chrono:
				case Quadro:
					// 4 nums per measurement
					nums = StringUtils.chopString(s, DATA_CHARS_PER_NUMBER_QUAD);
					if(nums.length != DATA_NUMS_PER_LINE_QUAD){
						throw new InvalidDendroFileException(
								I18n.getText("heidelberg.wrongNumValsPerLine", 
										"Quad", DATA_CHARS_PER_NUMBER_QUAD+""), 
										i+1);
					}
					try{
						for(String num : nums){
							Integer.parseInt(num.trim());
						}
					}catch(NumberFormatException e){
						log.error(I18n.getText("fileio.invalidDataValue"));
						throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i+1);
					}
					
					break;
				case HalfChrono:
				case Double:
					// 2 nums per measurement
					nums = StringUtils.chopString(s, DATA_CHARS_PER_NUMBER_REG);
					if(nums.length != DATA_NUMS_PER_LINE_REG){
						throw new InvalidDendroFileException(
								I18n.getText("heidelberg.wrongNumValsPerLine", 
										"Double", DATA_NUMS_PER_LINE_REG+""), 
										i+1);
					}
					try{
						for(String num : nums){
							Integer.parseInt(num.trim());
						}
					}catch(NumberFormatException e){
						log.error(I18n.getText("fileio.invalidDataValue"));
						throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i+1);
					}
					break;
				case Single:
				case Tree:
					if(s.length() >= 6 && s.contains(" ")){
						// multi-row format
						nums = StringUtils.chopString(s, DATA_CHARS_PER_NUMBER_REG);
						try{
							for(String num : nums){
								Integer.parseInt(num.trim());
							}
						}catch(NumberFormatException e){
							log.error(I18n.getText("fileio.invalidDataValue"));
							throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i+1);
						}
					}else{
						// single row format
						try{
							Integer.parseInt(s.trim());
						}catch(NumberFormatException e){
							log.error(I18n.getText("fileio.invalidDataValue"));
							throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i+1);
						}
					}
					break;
				}
			}
		}
	}

	private void extractHeader(String[] argHeader, HeidelbergMeasurementSeries argSeries){
		for(int i=0; i<argHeader.length; i++){
			String s = argHeader[i];
			currentLineNum = i+1; // +1 because of the HEADER line
			String[] split = s.split("=");
			if(split.length == 1){
				argSeries.fileMetadata.put(split[0], "");
			}else{
				argSeries.fileMetadata.put(split[0], split[1]);
			}
		}
	}
	
	private void extractData(String[] argData, HeidelbergMeasurementSeries argSeries){
		log.dbo(DebugLevel.L6_VERBOSE, "Data strings", argData);
		ArrayList<Integer> ints = new ArrayList<Integer>();
		switch(argSeries.dataType){
		case Chrono:
		case Quadro:
			for(int i=0; i<argData.length; i++){
				String line = argData[i];
				currentLineNum++;
				String[] s = StringUtils.chopString(line, DATA_CHARS_PER_NUMBER_QUAD);
				for(int j=0; j<s.length; j++){
					// ignore every 3rd and 4th entry
					if((j+2)%4 == 0 || (j+1)%4 == 0){
						continue;
					}
					ints.add(Integer.parseInt(s[j].trim()));						
				}
			}
			break;
		case Double:
		case HalfChrono:
			for(int i=0; i<argData.length; i++){
				String line = argData[i];
				currentLineNum++;
				String[] s = StringUtils.chopString(line, DATA_CHARS_PER_NUMBER_REG);
				for(int j=0; j<s.length; j++){
					ints.add(Integer.parseInt(s[j].trim()));						
				}
			}
			break;
		case Single:
		case Tree:
			if(argData[0].length() >= 6 && argData[0].contains(" ")){
				// we are in multi-column format
				for(int i=0; i<argData.length; i++){
					String line = argData[i];
					currentLineNum++;
					String[] s = StringUtils.chopString(line, DATA_CHARS_PER_NUMBER_REG);
					for(int j=0; j<s.length; j++){
						ints.add(Integer.parseInt(s[j].trim()));						
					}
				}
			}else{
				// single column format
				for(int i=0; i<argData.length; i++){
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
	private void populateHeaderInformation(){
		// clone a new default for each series, and add corresponding metadata from that series to it's defaults
		for(HeidelbergMeasurementSeries s : series){
			s.defaults = (HeidelbergToTridasDefaults) defaults.clone();
			HashMap<String, String> fileMetadata = s.fileMetadata;
			
			s.defaults.getStringDefaultValue(DefaultFields.SERIES_ID).setValue( fileMetadata.get("KeyCode"));
			GenericDefaultValue<ControlledVoc> val = (GenericDefaultValue<ControlledVoc>) s.defaults.getDefaultValue(DefaultFields.TAXON);
			val.setValue(ITRDBTaxonConverter.getControlledVocFromCode(fileMetadata.get("Species")));
			try{
				s.defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setValue( Integer.parseInt(fileMetadata.get("DateBegin")));
			}catch(Exception e){}
			try{
				s.defaults.getIntegerDefaultValue(DefaultFields.DATE_END).setValue( Integer.parseInt(fileMetadata.get("DateEnd")));
			}catch(Exception e){}
			
			GenericDefaultValue<TridasUnit> unit = (GenericDefaultValue<TridasUnit>) s.defaults.getDefaultValue(DefaultFields.UNIT);
			if(fileMetadata.containsKey("Unit")){
				String units = fileMetadata.get("Unit");
				TridasUnit value = new TridasUnit();
				if(units.equals("mm")){
					value.setNormalTridas(NormalTridasUnit.MILLIMETRES);
				}else if(units.equals("1/10 mm")){
					value.setNormalTridas(NormalTridasUnit.TENTH_MM);
				}else if(units.equals("1/100 mm")){
					value.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
				}else if(units.equals("1/1000 mm")){
					value.setNormalTridas(NormalTridasUnit.MICROMETRES);
				}else{
					value = null; 
					this.addWarning(new ConversionWarning(WarningType.NULL_VALUE, 
							I18n.getText("fileio.noUnitsDetected")));		
				}
				unit.setValue(value);
			}else{
				unit.setValue(null);
			}
			s.defaults.getStringDefaultValue(DefaultFields.STANDARDIZATION_METHOD).setValue( fileMetadata.get("SeriesType"));
		}
	}
	
	private void populateDataInformation(){
		// nothing to populate with
	}
	
	private TridasProject createProject(){
		TridasProject project = defaults.getProjectWithDefaults();
		TridasObject object = defaults.getObjectWithDefaults();
		// the defaults populate from the element downward, so I make a new
		// element for each series in the file
		ArrayList<TridasElement> elements = new ArrayList<TridasElement>();
		
		for(HeidelbergMeasurementSeries s : series){
			TridasElement element = s.defaults.getElementWithDefaults();
			TridasSample sample = s.defaults.getSampleWithDefaults();	
			
			DATA_TYPE dataType = s.dataType;
			switch(dataType){
			case Chrono:
			case Double:
			case HalfChrono:
			case Quadro:{
				// derived series
				String uuidKey = "XREF-"+UUID.randomUUID();
				TridasRadiusPlaceholder radius = new TridasRadiusPlaceholder();
				TridasMeasurementSeriesPlaceholder ms = new TridasMeasurementSeriesPlaceholder();
				radius.setMeasurementSeriesPlaceholder(ms);
				ms.setId(uuidKey);

				
				TridasDerivedSeries series = s.defaults.getDerivedSeriesWithDefaults();
				ArrayList<TridasValue> tridasValues = new ArrayList<TridasValue>();
				
				// Add values to nested value(s) tags
				TridasValues valuesGroup = s.defaults.getTridasValuesWithDefaults();
				valuesGroup.setValues(tridasValues);
				
				//link series to placeholder
				IdRef idref = new IdRef();
				idref.setRef(ms);
				SeriesLink link = new SeriesLink();
				link.setIdRef(idref);
				series.getLinkSeries().getSeries().add(link);
				series.getValues().add(valuesGroup);
				
				int numDataInts = s.dataInts.size();
				String slength = s.fileMetadata.get("Length");
				if(slength != null){
					try{
						numDataInts = Integer.parseInt(slength)*2; // count, value
					}catch(Exception e){}
				}
				for(int i=0; i<numDataInts; i+=2){
					int width = s.dataInts.get(i);
					int count = s.dataInts.get(i+1);
					TridasValue val = new TridasValue();
					val.setCount(count);
					val.setValue(width+"");
					tridasValues.add(val);
				}
				
				sample.setRadiusPlaceholder(radius);
				sample.setRadiuses(null);
				project.getDerivedSeries().add(series);
			}	break;
			case Single:
			case Tree:{
				TridasRadius radius = defaults.getRadiusWithDefaults(false);
				TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
				
				TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
				List<TridasValue> values = valuesGroup.getValues();
				
				
				int numDataInts = s.dataInts.size();
				String slength = s.fileMetadata.get("Length");
				if(slength != null){
					try{
						numDataInts = Integer.parseInt(slength); // value
					}catch(Exception e){}
				}
				if(numDataInts > s.dataInts.size()){
					log.error("Incorrect length: "+numDataInts);
					//throw ConversionWarning()
					numDataInts = s.dataInts.size();
				}
				for(int i=0; i<numDataInts; i++){
					TridasValue val = new TridasValue();
					val.setValue(s.dataInts.get(i)+"");
					values.add(val);
				}
				
				series.getValues().add(valuesGroup);
				radius.getMeasurementSeries().add(series);
				sample.getRadiuses().add(radius);
			}	break;
			}
			
			element.getSamples().add(sample);
			
			/* TODO We can't add all the tags as genericFields to the Tridas 
			 * element as many should be associated with other TRiDaS entities.
			 * 			 
			ArrayList<TridasGenericField> metaData = new ArrayList<TridasGenericField>();
			for(String key : s.fileMetadata.keySet()){
				TridasGenericField generic = new TridasGenericField();
				generic.setName(key);
				generic.setValue(s.fileMetadata.get(key));
				metaData.add(generic);
			}
			element.setGenericFields(metaData);*/
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
	 * @author daniel
	 *
	 */
	private static class HeidelbergMeasurementSeries{
		public DATA_TYPE dataType;
		public HeidelbergToTridasDefaults defaults;
		public final HashMap<String,String> fileMetadata = new HashMap<String, String>();
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
