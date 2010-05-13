package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.util.StringUtils;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
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

public class HeidelbergReader extends AbstractDendroFileReader {
	private static final SimpleLogger log = new SimpleLogger(HeidelbergReader.class);
	
	public static final int DATA_CHARS_PER_NUMBER = 6;
	
	private HeidelbergToTridasDefaults defaults = null;
	private HashMap<String,String> fileMetadata = null;
	private Integer[] dataInts;
	
	private int currentLineNum = 0;
	private int headerNumLines = 0;
	
	public HeidelbergReader() {
		super(HeidelbergToTridasDefaults.class);
	}

	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException {
		log.debug("Parsing: "+argFileString);
		defaults = (HeidelbergToTridasDefaults) argDefaultFields;
		fileMetadata = new HashMap<String, String>();
		
		// first lets see if we look like a heidelberg file
		checkFile(argFileString);

		
		int lineNum = 1; // first line is just HEADER:
		int fileLength = argFileString.length;
		currentLineNum = lineNum; // update line num
		
		// HEADER
		ArrayList<String> header = new ArrayList<String>();
		String line = argFileString[lineNum];
		while(!line.startsWith("DATA")){
			currentLineNum = lineNum; // update line num
			header.add(line);
			line = argFileString[++lineNum];
		}
		extractHeader(header.toArray(new String[0]));
		
		// DATA
		lineNum++; // we're still on the "DATA" line, so move forward
		currentLineNum = lineNum; // update line num
		headerNumLines = lineNum;
		
		ArrayList<String> data = new ArrayList<String>();
		while(lineNum<fileLength){
			currentLineNum = lineNum; // update line num
			data.add(argFileString[lineNum]);
			lineNum++;
		}
		extractData(data.toArray(new String[0]));
		
		currentLineNum = 0; // now for processing
		
		populateHeaderInformation();
		populateDataInformation();
	}
	
	// check file to see if it generally looks like a Heidelberg file
	private void checkFile(String[] argStrings) throws InvalidDendroFileException{
		log.debug("Checking file to see if it looks like a Heidelberg file");
		
		if(!argStrings[0].startsWith("HEADER") && !argStrings[0].startsWith("DATA")){
			log.error("First line is 'HEADER' or 'DATA'");
			throw new InvalidDendroFileException(I18n.getText("heidelberg.firstLineWrong"), 1);
		}
		
		boolean pastHeader = false;
		for(int i=0; i<argStrings.length; i++){
			currentLineNum = i; // update line number
			String s = argStrings[i];
			if(s.startsWith("HEADER")){
				if(pastHeader){
					log.error(I18n.getText("heidelberg.headerAfterData"));
					throw new InvalidDendroFileException(I18n.getText("heidelberg.headerAfterData"), 1);
				}
				continue;
			}else if(s.startsWith("DATA")){
				pastHeader = true;
				continue;
			}
			
			// so we're in the header or data
			
			if(s.equals("")){
				//empty line?
				continue;
			}
			
			if(!pastHeader){
				// in header!
				if(s.split("=").length != 2){
					log.error(I18n.getText("heidelberg.headerNotKeyValuePair"));
					throw new InvalidDendroFileException(I18n.getText("heidelberg.headerNotKeyValuePair"), i+1);
				}
			}else{
				// in data!
				String[] nums = StringUtils.chopString(s, DATA_CHARS_PER_NUMBER);
				if(nums.length != 10){
					throw new InvalidDendroFileException("Data must have 10 numbers per line", i+1);
				}
				try{
					for(String num : nums){
						Integer.parseInt(num.trim());
					}
				}catch(NumberFormatException e){
					log.error(I18n.getText("fileio.invalidDataValue"));
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i+1);
				}
			}
		}
		if(!pastHeader){
			log.error(I18n.getText("fileio.noData"));
			throw new InvalidDendroFileException(I18n.getText("fileio.noData"), currentLineNum);
		}
	}

	private void extractHeader(String[] argHeader){
		for(int i=0; i<argHeader.length; i++){
			String s = argHeader[i];
			currentLineNum = i+1; // +1 because of the HEADER line
			String[] split = s.split("=");
			fileMetadata.put(split[0], split[1]);
		}
	}
	
	private void extractData(String[] argData){
		
		ArrayList<Integer> ints = new ArrayList<Integer>();
		for(int i=0; i<argData.length; i++){
			String line = argData[i];
			currentLineNum = headerNumLines + i;
			String[] s = StringUtils.chopString(line, DATA_CHARS_PER_NUMBER);
			for(String entry : s){
				ints.add(Integer.parseInt(entry.trim()));						
			}
		}
		dataInts = ints.toArray(new Integer[0]);
	}
	
	@SuppressWarnings("unchecked")
	private void populateHeaderInformation(){
		defaults.getStringDefaultValue(DefaultFields.SERIES_ID).setValue( fileMetadata.get("KeyCode"));
		GenericDefaultValue<ControlledVoc> val = (GenericDefaultValue<ControlledVoc>) defaults.getDefaultValue(DefaultFields.TAXON);
		ControlledVoc v = new ControlledVoc();
		v.setValue(fileMetadata.get("Species"));
		val.setValue(v);
		try{
			defaults.getIntegerDefaultValue(DefaultFields.DATE_BEGIN).setValue( Integer.parseInt(fileMetadata.get("DateBegin")));
		}catch(Exception e){}
		try{
			defaults.getIntegerDefaultValue(DefaultFields.DATE_END).setValue( Integer.parseInt(fileMetadata.get("DateEnd")));
		}catch(Exception e){}
		
		GenericDefaultValue<TridasUnit> unit = (GenericDefaultValue<TridasUnit>) defaults.getDefaultValue(DefaultFields.UNIT);
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
				this.addWarningToList(new ConversionWarning(WarningType.NULL_VALUE, 
						I18n.getText("fileio.noUnitsDetected")));		
			}
			unit.setValue(value);
		}else{
			unit.setValue(null);
		}
		defaults.getStringDefaultValue(DefaultFields.STANDARDIZATION_METHOD).setValue( fileMetadata.get("SeriesType"));
	}
	
	private void populateDataInformation(){
		// nothing to populate with
	}
	
	private TridasProject createProject(){
		TridasProject project = defaults.getProjectWithDefaults();
		TridasObject object = defaults.getObjectWithDefaults();
		TridasElement element = defaults.getElementWithDefaults();
		TridasSample sample = defaults.getSampleWithDefaults();	
		
		String dataFormat = fileMetadata.get("DataFormat").trim();
		
		if(dataFormat.equals("Chrono") || dataFormat.equals("HalfChrono")){
			// chronology
			
			String uuidKey = "XREF-"+UUID.randomUUID();
			TridasRadiusPlaceholder radius = new TridasRadiusPlaceholder();
			TridasMeasurementSeriesPlaceholder ms = new TridasMeasurementSeriesPlaceholder();
			radius.setMeasurementSeriesPlaceholder(ms);
			ms.setId(uuidKey);

			
			TridasDerivedSeries series = defaults.getDerivedSeriesWithDefaults();
			ArrayList<TridasValue> tridasValues = new ArrayList<TridasValue>();
			
			// Add values to nested value(s) tags
			TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
			valuesGroup.setValues(tridasValues);
			
			//link series to placeholder
			IdRef idref = new IdRef();
			idref.setRef(ms);
			SeriesLink link = new SeriesLink();
			link.setIdRef(idref);
			series.getLinkSeries().getSeries().add(link);
			series.getValues().add(valuesGroup);
			
			
			for(int i=0; i<dataInts.length; i+=2){
				int width = dataInts[i];
				int count = dataInts[i+1];
				TridasValue val = new TridasValue();
				val.setCount(count);
				val.setValue(width+"");
				tridasValues.add(val);
			}
			
			sample.setRadiusPlaceholder(radius);
			sample.setRadiuses(null);
			project.getDerivedSeries().add(series);
		}else if(dataFormat.equals("Tree")){
			TridasRadius radius = defaults.getRadiusWithDefaults(false);
			TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
			
			TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
			List<TridasValue> values = valuesGroup.getValues();
			
			for(int i=0; i<dataInts.length; i++){
				log.debug("Value: "+dataInts[i]);
				TridasValue val = new TridasValue();
				val.setValue(dataInts[i]+"");
				values.add(val);
			}
			
			series.getValues().add(valuesGroup);
			radius.getMeasurementSeries().add(series);
			sample.getRadiuses().add(radius);
		}
		
		project.getObjects().add(object);
		object.getElements().add(element);
		element.getSamples().add(sample);
		
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
}
