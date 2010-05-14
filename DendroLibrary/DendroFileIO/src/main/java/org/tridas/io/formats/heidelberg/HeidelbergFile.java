package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.heidelberg.TridasToHeidelbergDefaults.HeidelbergField;
import org.tridas.io.util.StringUtils;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class HeidelbergFile implements IDendroFile {
	private static final SimpleLogger log = new SimpleLogger(HeidelbergFile.class);
	
	public static final int DATA_CHARS_PER_NUMBER = 6;

	private IDendroCollectionWriter writer;
	private ITridasSeries series = null;
	private TridasToHeidelbergDefaults defaults;
	
	private boolean chrono;
	private Integer[] dataInts;
	private int valuesIndex;
	private int numTridasValues;
	
	public HeidelbergFile(IDendroCollectionWriter argWriter, TridasToHeidelbergDefaults argDefaults){
		writer = argWriter;
		defaults = argDefaults;
	}
	
	@Override
	public String getExtension() {
		return "fh";
	}
	
	public void setSeries(ITridasSeries argSeries, int argValuesIndex){
		series = argSeries;
		valuesIndex = argValuesIndex;
		
		if(argSeries instanceof TridasDerivedSeries){
			chrono = true;
		}else{
			chrono = false;
		}
		
		extractData();
		verifyData();
		populateDefaults();
	}
	
	private void extractData(){
		ArrayList<Integer> ints = new ArrayList<Integer>();
		
		TridasValues vals = series.getValues().get(valuesIndex);
		numTridasValues = vals.getValues().size();
		for(TridasValue v : vals.getValues()){
			ints.add(Integer.parseInt(v.getValue()));
			if(chrono){
				ints.add(v.getCount());
			}
		}
		dataInts = ints.toArray(new Integer[0]);
	}
	
	private void verifyData(){
		int maximumLength = DATA_CHARS_PER_NUMBER;
		for(Integer i : dataInts){
			String si = i+"";
			if(si.length() > maximumLength){
				maximumLength = si.length();
			}
		}
		if(maximumLength > DATA_CHARS_PER_NUMBER){
			log.warn("Data integers are too big for storing in heidelberg file, only 6 characters are allowed.  Increasing units.");
			writer.getWarnings().add( new ConversionWarning(
					WarningType.WORK_AROUND,"Data numbers were too long for the allowed space of 6 characters.  Increasing units and reducing numbers"));
			reduceUnits();
			for(int i=0; i< dataInts.length; i++){
				for(int j=0; j< maximumLength - DATA_CHARS_PER_NUMBER; j++){
					dataInts[i] /= 10;
				}
			}
		}
	}
	
	private void reduceUnits(){
		log.debug("reducing units");
		StringDefaultValue sdv = defaults.getStringDefaultValue(HeidelbergField.UNIT);
		
		if(sdv.getStringValue() == "" ){
			log.error("No units, could not reduce.");
			return;
		}
		if(sdv.getStringValue().equals("mm")){
			log.error("Could not reduce units");
			writer.getWarnings().add(new ConversionWarning(WarningType.IGNORED, "Could not reduce units, removing the units field."));
			sdv.setValue(null);
		}else if(sdv.getStringValue().equals("1/10 mm")){
			sdv.setValue("mm");
		}else if(sdv.getStringValue().equals("1/100 mm")){
			sdv.setValue("1/10 mm");
		}else if(sdv.getStringValue().equals("1/1000 mm")){
			sdv.setValue("1/100 mm");
		}
	}

	private void populateDefaults(){
		if(chrono){
			defaults.getStringDefaultValue(HeidelbergField.DATA_FORMAT).setValue("Chrono");
			String standardizationMethod = ((TridasDerivedSeries)series).getStandardizingMethod();
			defaults.getStringDefaultValue(HeidelbergField.SERIES_TYPE).setValue(standardizationMethod);
		}else{
			defaults.getStringDefaultValue(HeidelbergField.DATA_FORMAT).setValue("Tree");
		}
		defaults.getIntegerDefaultValue(HeidelbergField.LENGTH).setValue(numTridasValues);
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		return new ITridasSeries[]{ series };
	}

	@Override
	public IDendroCollectionWriter getWriter() {
		return writer;
	}

	@Override
	public String[] saveToString() {
		ArrayList<String> file = new ArrayList<String>();
		file.add("HEADER:");
		addIfNotNull("KeyCode",HeidelbergField.KEY_CODE, file);
		addIfNotNull("DataFormat",HeidelbergField.DATA_FORMAT, file);
		addIfNotNull("SeriesType",HeidelbergField.SERIES_TYPE, file);
		addIfNotNull("Length",HeidelbergField.LENGTH, file);
		addIfNotNull("DateBegin",HeidelbergField.DATEBEGIN, file);
		addIfNotNull("DateEnd",HeidelbergField.DATEEND, file);
		addIfNotNull("Dated",HeidelbergField.DATED, file);
		addIfNotNull("Species",HeidelbergField.SPECIES, file);
		addIfNotNull("Unit",HeidelbergField.UNIT, file);
		
		if(chrono){
			file.add("DATA:Double");
		}else{
			file.add("DATA:Single");
		}
		
		int j=0;
		int lines = (int) Math.ceil( dataInts.length*1.0/10.0);
		for(int i=0; i<lines; i++){
			StringBuilder line = new StringBuilder();
			for(int k=0; k<10; k++){
				if(j<dataInts.length){
					line.append(StringUtils.leftPad(dataInts[j]+"", DATA_CHARS_PER_NUMBER));
				}else{
					line.append(StringUtils.leftPad("0", DATA_CHARS_PER_NUMBER));
				}
				j++;
			}
			file.add(line.toString());
		}
		return file.toArray(new String[0]);
	}
	
	private void addIfNotNull(String argKeyString, HeidelbergField argEnum, ArrayList<String> argList){
		if( defaults.getDefaultValue(argEnum).getStringValue().equals("") ){
			return;
		}
		argList.add(argKeyString+"="+defaults.getDefaultValue(argEnum).getStringValue());
	}
}