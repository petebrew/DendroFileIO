package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.TridasVariableDefaultValue;
import org.tridas.io.formats.heidelberg.TridasToHeidelbergDefaults.HeidelbergField;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeriesPlaceholder;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRadiusPlaceholder;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.SeriesLink.IdRef;

public class HeidelbergReader extends AbstractDendroFileReader {

private static final SimpleLogger log = new SimpleLogger(HeidelbergReader.class);
	
	private TridasProject project = null;
	private HeidelbergToTridasDefaults defaults = null;
	
	private HashMap<String,String> fileMetadata = null;
	private Integer[] dataInts;
	
	static {
		TridasIO.registerFileReader(HeidelbergReader.class);
	}
	
	public HeidelbergReader() {
		super(HeidelbergToTridasDefaults.class);
	}

	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) {
		log.debug("Parsing: "+argFileString);
		defaults = (HeidelbergToTridasDefaults) argDefaultFields;
		fileMetadata = new HashMap<String, String>();
		
		int lineNum = 0;
		int fileLength = argFileString.length;
		if(argFileString[lineNum].trim().equals("HEADER:")){
			lineNum++;
		}
		
		// HEADER
		
		ArrayList<String> header = new ArrayList<String>();
		String line = argFileString[lineNum];
		while(!line.startsWith("DATA")){
			header.add(line);
			line = argFileString[++lineNum];
			if(lineNum==fileLength){
				log.error(I18n.getText("heidelberg.meta.error"));
				//addWarningToList(new ConversionWarning(WarningType., warningMessage))
				// TODO add warning of data not found
				// TODO split up metadata warnings and general file conversion warnings
				break;
			}
		}
		processHeader(header.toArray(new String[0]));
		
		// DATA
		lineNum++; // we're still on the "DATA" line, so move forward
		
		ArrayList<String> data = new ArrayList<String>();
		while(lineNum<fileLength){
			data.add(argFileString[lineNum]);
			lineNum++;
		}
		processData(data.toArray(new String[0]));
		
		createProject();
	}

	private void processHeader(String[] argHeader){
		HashMap<String,String> data = new HashMap<String, String>();
		for(String s: argHeader){
			String[] split = s.split("=");
			if(split.length != 2){
				// TODO locale
				new ConversionWarning(WarningType.INVALID, "Could not determine key and value", split[0]);
				continue;
			}
			data.put(split[0], split[1]);
		}
		
		//load all data
		for(String key :data.keySet()){
			fileMetadata.put(key, data.get(key));
		}
	}
	
	private void processData(String[] argData){
		
		ArrayList<Integer> ints = new ArrayList<Integer>();
		for(String line : argData){
			String[] s = StringUtils.chopString(line, 6);
			for(String entry : s){
				try{
					ints.add(Integer.parseInt(entry.trim()));						
				}catch (Exception e) {
					log.error("Cannot parse an integer from '"+entry.trim()+"'.");
					// TODO throw warning
				}
			}
		}
		dataInts = ints.toArray(new Integer[0]);
	}
	
	private void createProject(){
		project = defaults.getProjectWithDefaults();
		TridasObject object = defaults.getObjectWithDefaults();
		TridasElement element = defaults.getElementWithDefaults();
		TridasSample sample = defaults.getSampleWithDefaults();		
		
		// to metadata stuff first
		// FIXME i guess this is only AD stuff for now
		SafeIntYear startYear = new SafeIntYear( Integer.parseInt(fileMetadata.get("DateBegin")));
		SafeIntYear endYear = new SafeIntYear( Integer.parseInt(fileMetadata.get("DateEnd")));
		String keyCode = fileMetadata.get("KeyCode");
		
		String dataFormat = fileMetadata.get("DataFormat").trim();
		
		if(dataFormat.equals("Chrono") || dataFormat.equals("HalfChrono")){
			// chronology
			
			String uuidKey = "XREF-"+UUID.randomUUID();
			TridasRadiusPlaceholder radius = new TridasRadiusPlaceholder();
			TridasMeasurementSeriesPlaceholder ms = new TridasMeasurementSeriesPlaceholder();
			radius.setMeasurementSeriesPlaceholder(ms);
			ms.setId(uuidKey);
			
			TridasInterpretation interp = new TridasInterpretation();
			
			// Build interpretation group for series	
			interp.setFirstYear(startYear.toTridasYear(DatingSuffix.AD));					
			interp.setLastYear(endYear.toTridasYear(DatingSuffix.AD));
			
			// Build identifier for series
			TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
			seriesId.setValue(keyCode.trim());
			seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
			
			TridasDerivedSeries series = defaults.getDerivedSeriesWithDefaults();
			ArrayList<TridasValue> tridasValues = new ArrayList<TridasValue>();
			
			// Add values to nested value(s) tags
			TridasValues valuesGroup = new TridasValues();
			valuesGroup.setValues(tridasValues);
			valuesGroup.setUnitless(new TridasUnitless());
			TridasVariableDefaultValue variable = (TridasVariableDefaultValue) defaults.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
			valuesGroup.setVariable(variable.getValue());
			
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
			
			series.setIdentifier(seriesId);
			series.setInterpretation(interp);
			
			sample.setRadiusPlaceholder(radius);
			sample.setRadiuses(null);
			project.getDerivedSeries().add(series);
		}
		
		project.getObjects().add(object);
		object.getElements().add(element);
		element.getSamples().add(sample);
	}
	
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"fh"};
	}

	@Override
	public TridasProject getProject() {
		return project;
	}
}
