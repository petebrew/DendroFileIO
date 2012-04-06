package org.tridas.io.formats.kinsys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.InvalidDendroFileException.PointerType;
import org.tridas.io.formats.heidelberg.HeidelbergReader;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults;
import org.tridas.io.formats.kinsys.KinsysToTridasDefaults.DefaultFields;
import org.tridas.io.formats.kinsys.KinsysToTridasDefaults.KinsysDataType;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DateTime;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class KinsysReader extends AbstractDendroFileReader {
	private static final Logger log = LoggerFactory.getLogger(KinsysReader.class);
	private KinsysToTridasDefaults defaults = null;
	private Integer currentLineNumber = 0;
	private ArrayList<KinsysSeries> seriesList = new ArrayList<KinsysSeries>();


	public KinsysReader() {
		super(KinsysToTridasDefaults.class);
		defaults = new KinsysToTridasDefaults();
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		log.debug("Parsing: " + argFileString);
		
		checkFile(argFileString);
		
		// Loop through file compiling chunks of
		// lines that represent a single series
		ArrayList<String> tempArray = null;
		for(String line : argFileString)
		{
			currentLineNumber++;
			
			// Starting a new series
			if(tempArray==null) 
			{
				tempArray = new ArrayList<String>();
			}
			
			// End of series 
			if(line.trim().equals("*** DATA END ***"))
			{
				processSeries(tempArray, currentLineNumber-tempArray.size());
				tempArray=null;
				continue;
			}
			
			tempArray.add(line);
		}
		
		
	}

	private void processSeries(ArrayList<String> lines, Integer currLineStart) throws InvalidDendroFileException
	{
		KinsysSeries series = new KinsysSeries();
		series.defaults = (KinsysToTridasDefaults) defaults.clone();
		Integer thisLineNum = currLineStart;
		Boolean skipThisSeries = false;
		
		// Date Line
		String headerDateTime = lines.get(0).trim();
		String[] headerDateTimeParts = headerDateTime.split("\\s");
		if(headerDateTimeParts.length==2)
		{
			try{
				String[] dateParts = headerDateTimeParts[0].split("[‐./]");
				String[] timeParts = headerDateTimeParts[1].split("[-.:]");
				DateTime measurementDate = DateUtils.getDateTime(
						Integer.parseInt(dateParts[0]), 
						Integer.parseInt(dateParts[1]), 
						Integer.parseInt(dateParts[2]), 
						Integer.parseInt(timeParts[0]), 
						Integer.parseInt(timeParts[1]), 
						Integer.parseInt(timeParts[2]));
				
				series.defaults.getDateTimeDefaultValue(DefaultFields.CREATION_DATE).setValue(measurementDate);

			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException("Format of date line is invalid", thisLineNum, PointerType.LINE);
			}
			
		}
		else
		{
			throw new InvalidDendroFileException("Format of date line is invalid", thisLineNum, PointerType.LINE);
		}
		
		
		// #0 Line
		thisLineNum++;
		String header0 = lines.get(1).substring(4);
		String[] header0parts = header0.split("/");
		if(header0parts.length==3)
		{
			series.defaults.getStringDefaultValue(DefaultFields.PROJECT_CODE).setValue(header0parts[0]);
			series.defaults.getStringDefaultValue(DefaultFields.PROJECT_NAME).setValue(header0parts[1]);
			series.defaults.getStringDefaultValue(DefaultFields.INVESTIGATOR).setValue(header0parts[2]);
		}
		else if (header0parts.length==2)
		{
			series.defaults.getStringDefaultValue(DefaultFields.PROJECT_NAME).setValue(header0parts[0]);
			series.defaults.getStringDefaultValue(DefaultFields.INVESTIGATOR).setValue(header0parts[1]);
		}
		else
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #0 - line "+thisLineNum));
		}
		
		// #1 Line
		thisLineNum++;
		String header1 = lines.get(2).substring(4);
		String[] header1parts = header1.split("\\.");
		if(header1parts.length==3)
		{
			try{
				DateTime samplingDate = DateUtils.getDateTime(Integer.valueOf(header1parts[0]), 
													 Integer.valueOf(header1parts[1]), 
													 Integer.valueOf(header1parts[2]));
				series.defaults.getDateTimeDefaultValue(DefaultFields.SAMPLING_DATE).setValue(samplingDate);
			} catch (Exception e)
			{
				this.addWarning(new ConversionWarning(WarningType.INVALID, "Failed to parse sampling date in header line #1 - line "+thisLineNum));
			}
		}
		else
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #1 - line "+thisLineNum));
		}
		
		// #2 Line
		thisLineNum++;
		String header2 = lines.get(3).substring(4);
		String[] header2parts = header2.split("/");
		if(header2parts.length>1)
		{
			try{
				Double yref = Double.parseDouble(header2parts[0]);
				Double xref = Double.parseDouble(header2parts[1]);
				//series.defaults.getDoubleDefaultValue(DefaultFields.FINNISH_Y_COORD).setValue(yref);
				//series.defaults.getDoubleDefaultValue(DefaultFields.FINNISH_X_COORD).setValue(xref);
				this.addWarning(new ConversionWarning(WarningType.IGNORED, "The projection of points from the Finnish KKJ coordinate system to WGS84 is still experimental and is currently disabled"));
			} catch (NumberFormatException e)
			{
				this.addWarning(new ConversionWarning(WarningType.INVALID, "Unable to parse coordinate data from line "+thisLineNum));
			}
		}
		if(header2parts.length>2)
		{
			try{
				Double elevation = Double.parseDouble(header2parts[2]);
				series.defaults.getDoubleDefaultValue(DefaultFields.ELEVATION).setValue(elevation);
			}
			catch (NumberFormatException e)
			{
				this.addWarning(new ConversionWarning(WarningType.INVALID, "Unable to parse elevation value from line "+thisLineNum));
			}
		}
		if(header2parts.length>3 || header2parts.length<1)
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #2 - line "+thisLineNum));
		}
		
		// #3 Line
		thisLineNum++;
		String header3 = lines.get(4).substring(4).trim();
		String[] header3parts = header3.split("/");
		if(header3parts.length==3)
		{
			series.defaults.getStringDefaultValue(DefaultFields.EXPERIMENT_NAME).setValue(header3parts[0]);
			series.defaults.getStringDefaultValue(DefaultFields.PERIOD_OF_MEASUREMENT).setValue(header3parts[1]);
			series.defaults.getStringDefaultValue(DefaultFields.LOCATION_NAME).setValue(header3parts[2]);
		}
		else
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #3 - line "+thisLineNum));
		}
			
			
		// #4 Line
		thisLineNum++;
		String header4 = lines.get(5).substring(4);
		String[] header4parts = header4.split("/");
		if(header4parts.length>0)
		{
			series.defaults.getStringDefaultValue(DefaultFields.PLOT).setValue(header4parts[0]);
		}
		if(header4parts.length>1)
		{
			series.defaults.getStringDefaultValue(DefaultFields.SUBPLOT).setValue(header4parts[1]);
		}
		
		
		// #5 Line
		//TODO Ambiguities in documentation for this line.  Confirm this is correct
		thisLineNum++;
		String header5 = lines.get(6).substring(4).trim();
		String[] header5parts = header5.split("/");
		if(header5parts.length==2)
		{
			series.defaults.getStringDefaultValue(DefaultFields.RUNNING_MEASUREMENT_CODE).setValue(header5parts[0]);
			series.defaults.getStringDefaultValue(DefaultFields.ID_NUMBER).setValue(header5parts[1]);	
		}
		else
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #5 - line "+thisLineNum));
		}
		
		// #6 Line
		thisLineNum++;
		String taxonStr = lines.get(7).substring(4).trim();
		ControlledVoc taxon = ITRDBTaxonConverter.getControlledVocFromCode(taxonStr);
		if(taxon.isSetValue() && taxon.getValue().equals(taxonStr))
		{
			// Code not found in ITRDB so try Finnish VMI code instead
			//TODO
			
			taxon.setNormalStd("VMI - Finnish National Forest Inventory");
			taxon.setNormalId(taxonStr);
			taxon.setNormal(I18n.getText("unknown"));
			taxon.setValue(null);
		}
		@SuppressWarnings("unchecked")
		GenericDefaultValue<ControlledVoc> speciesField = (GenericDefaultValue<ControlledVoc>) 
									series.defaults.getDefaultValue(DefaultFields.SPECIES);
		speciesField.setValue(taxon);

		// #7 Line
		thisLineNum++;
		String header7 = lines.get(8).substring(4).trim();
		String[] header7parts = header7.split("/");
		if(header7parts.length>0)
		{
			try{
				SafeIntYear lastYear = new SafeIntYear(header7parts[0]);
				series.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_MEASUREMENT_YEAR).setValue(lastYear);
			} catch (Exception e)
			{
				throw new InvalidDendroFileException("Unable to parse last measurement year on line ", thisLineNum);
			}
		}
		else
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #7 - line "+thisLineNum));
		}
		if(header7parts.length>1)
		{
			//TODO Sub-sample code
		}
		if(header7parts.length>2)
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #7 - line "+thisLineNum));
		}
		
		
		// #8 Line
		thisLineNum++;
		String header8 = lines.get(9).substring(4).trim();
		String[] header8parts = header8.split("/");
		if(header8parts.length>0)
		{
			if(header8parts[0].equals("1"))
			{
				series.defaults.getBooleanDefaultValue(DefaultFields.INCOMPLETE_GROWTH_RING_MEASURED).setValue(true);
			}
		}
		else
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #8 - line "+thisLineNum));
		}
		if(header8parts.length>1)
		{
			series.defaults.getStringDefaultValue(DefaultFields.ESTIMATED_AGE_INCREASE).setValue(header8parts[1]);
		}
		if(header8parts.length>2)
		{
			if(header8parts[2].equals("1"))
			{
				series.defaults.getStringDefaultValue(DefaultFields.LAST_RING_TYPE).setValue("Earlywood");
			}
			else if(header8parts[2].equals("2"))
			{
				series.defaults.getStringDefaultValue(DefaultFields.LAST_RING_TYPE).setValue("Latewood");
			}
			else
			{
				this.addWarning(new ConversionWarning(WarningType.INVALID, "Invalid last ring earlywood/latewood code on header line #8 - line "+thisLineNum));
			}
		}
		if(header8parts.length>3)
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #8 - line "+thisLineNum));
		}
		
		
		// #9 Line
		//TODO
		thisLineNum++;
		String header9 = lines.get(10).substring(4).trim();
		String[] header9parts = header9.split("/");
		if(header9parts.length>0)
		{
			series.defaults.getStringDefaultValue(DefaultFields.MEASUREMENT_RADIUS).setValue(header9parts[0]);
			
			try{
				Double azimuth = Double.parseDouble(header9parts[0]);
				series.defaults.getDoubleDefaultValue(DefaultFields.SAMPLE_AZIMUTH).setValue(azimuth);	
			} catch (NumberFormatException e){}
		}
		else
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #9 - line "+thisLineNum));
		}
		if(header9parts.length>1)
		{
			if(header9parts[1].trim().equals("0"))
			{
				series.defaults.getBooleanDefaultValue(DefaultFields.PITH_TO_BARK).setValue(false);	
			}
			else if (header9parts[1].trim().equals("1"))
			{
				series.defaults.getBooleanDefaultValue(DefaultFields.PITH_TO_BARK).setValue(true);	
			}
			else
			{
				throw new InvalidDendroFileException("Invalid code found determining whether sample was measured pith-to-bark or bark-to-pith", thisLineNum, PointerType.LINE);
			}
		}
		if(header9parts.length>2)
		{
			try{
				Double sampleheight = Double.parseDouble(header9parts[2]);
				series.defaults.getDoubleDefaultValue(DefaultFields.HEIGHT).setValue(sampleheight);	
			} catch (NumberFormatException e)
			{
				this.addWarning(new ConversionWarning(WarningType.INVALID, "Unable to parse sampling height from header line #9 - line "+thisLineNum));

			}
		}
		if(header9parts.length>3)
		{
			//TODO Height code?
		}
		if(header9parts.length>4)
		{
			this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Incorrect number of values in header line #9 - line "+thisLineNum));
		}
		
		// #10 Line
		thisLineNum++;
		series.defaults.getStringDefaultValue(DefaultFields.USER_PARAM).setValue(lines.get(11).substring(4).trim());	

		// #12 Line - Note out of sequence because we need this value in Line #11
		thisLineNum = thisLineNum +2;
		Integer ringCount = null;
		Integer ringCountlineNum = thisLineNum;
		try
		{
			ringCount = Integer.parseInt(lines.get(13).substring(4).trim());
			series.defaults.getIntegerDefaultValue(DefaultFields.MEASURED_RING_COUNT).setValue(ringCount);	
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException("Unable to parse ring count in line #12", thisLineNum);
		}
		
		// #11 Line - Note out of sequence
		thisLineNum = thisLineNum -1;
		String header11 = lines.get(12).substring(4).trim();
		String[] header11parts = header11.split("/");
		if(header11parts.length>0)
		{
			if(header11parts[0].trim().equals("."))
			{
				series.defaults.getIntegerDefaultValue(DefaultFields.TOTAL_RING_COUNT).setValue(ringCount);
			}
			else
			{
				try{
					series.defaults.getIntegerDefaultValue(DefaultFields.TOTAL_RING_COUNT).setValue(Integer.parseInt(header11parts[0]));
				} catch (NumberFormatException e)
				{
					this.addWarning(new ConversionWarning(WarningType.NOT_STRICT, "Unable to parse data values in header line #11 - line "+thisLineNum));
				}
			}
		}
		@SuppressWarnings("unchecked")
		GenericDefaultValue<KinsysDataType> dataTypeField = (GenericDefaultValue<KinsysDataType>) 
									series.defaults.getDefaultValue(DefaultFields.DATA_TYPE);
		if(header11parts.length>1)
		{
			try
			{
				KinsysDataType dataType = KinsysDataType.fromCode(header11parts[1]);
				dataTypeField.setValue(dataType);
				
				// Skip series if it's a data type we don't support
				if(dataType.equals(KinsysDataType.HEIGHT_SHOOTS) ||
						dataType.equals(KinsysDataType.VOLUME_GROWTHS) 	)
				{
					skipThisSeries = true;
					this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, "The data type '"+dataType.toString()+"' is not currently supported.  Skipping series from lines "+this.currentLineNumber+" - "+(this.currentLineNumber+lines.size())));

				}
				
			} catch (Exception e)
			{
				dataTypeField.setValue(KinsysDataType.RINGWIDTH);
				this.addWarning(new ConversionWarning(WarningType.INVALID, "Unknown data type code on line "+thisLineNum+".  Assuming ring-width data."));
			}
		}
		else
		{
			dataTypeField.setValue(KinsysDataType.RINGWIDTH);
		}
		if(header11parts.length>2)
		{
			try{
				Integer columnnum = Integer.parseInt(header11parts[2]);
				if(!columnnum.equals(1)) 
				{
					skipThisSeries = true;
					this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, "TRiCYCLE only supports KINSYS files with 1 column.  Skipping series from lines "+this.currentLineNumber+" - "+(this.currentLineNumber+lines.size())));
				}		
			} catch (NumberFormatException e){}
		}
		if(header11parts.length>3)
		{
			// Number of decimals is irrelevant
		}
		thisLineNum = thisLineNum +2;
		
		// Data section
		thisLineNum++;
		for(int i = 14; i<lines.size(); i++)
		{
			try{
				Integer.parseInt(lines.get(i));
				TridasValue value = new TridasValue();
				value.setValue(lines.get(i));			
				series.dataVals.add(value);
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException("Invalid data value", thisLineNum);
			}
			
			thisLineNum++;
			
		}
		
		// Warn if ring count is invalid
		if(series.dataVals.size()!=ringCount)
		{
			this.addWarning(new ConversionWarning(WarningType.INVALID, "The ring count on line " + ringCountlineNum + " does not match the number of data values ("+series.dataVals.size()+")provided"));
		}
		
		// Reverse data values if measured bark-to-pith
		if(series.defaults.getBooleanDefaultValue(DefaultFields.PITH_TO_BARK).getValue().equals(false))
		{
			Collections.reverse(series.dataVals);
		}
		
		// Add series to list
		if(!skipThisSeries)	this.seriesList.add(series);
		
		
	}
	
	
	private void checkFile(String[] argStrings) throws InvalidDendroFileException {
		
		if(argStrings==null) throw new InvalidDendroFileException(I18n.getText("kinsys.notEnoughData"));
		if(argStrings.length<13) throw new InvalidDendroFileException(I18n.getText("kinsys.notEnoughData"));
		
		if(!argStrings[1].trim().startsWith("# 0")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 2, PointerType.LINE);
		if(!argStrings[2].trim().startsWith("# 1")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 3, PointerType.LINE);
		if(!argStrings[3].trim().startsWith("# 2")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 4, PointerType.LINE);
		if(!argStrings[4].trim().startsWith("# 3")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 5, PointerType.LINE);
		if(!argStrings[5].trim().startsWith("# 4")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 6, PointerType.LINE);
		if(!argStrings[6].trim().startsWith("# 5")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 7, PointerType.LINE);
		if(!argStrings[7].trim().startsWith("# 6")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 8, PointerType.LINE);
		if(!argStrings[8].trim().startsWith("# 7")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 9, PointerType.LINE);
		if(!argStrings[9].trim().startsWith("# 8")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 10, PointerType.LINE);
		if(!argStrings[10].trim().startsWith("# 9")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 11, PointerType.LINE);
		if(!argStrings[11].trim().startsWith("# 10")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 12, PointerType.LINE);
		if(!argStrings[12].trim().startsWith("# 11")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 13, PointerType.LINE);
		if(!argStrings[13].trim().startsWith("# 12")) 
			throw new InvalidDendroFileException(I18n.getText("kinsys.invalidHeaderLine"), 14, PointerType.LINE);

		
	}

	@Override
	protected void resetReader() {
		// TODO Auto-generated method stub
		currentLineNumber=0;
		seriesList = new ArrayList<KinsysSeries>();
	}

	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"MIT"};
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		
		ArrayList<TridasProject> projlist = new ArrayList<TridasProject>();
		
		for(KinsysSeries series : seriesList)
		{			
			TridasMeasurementSeries ser = series.defaults.getDefaultTridasMeasurementSeries(); 
			TridasValues tvgr = series.defaults.getTridasValuesWithDefaults();	
			tvgr.setValues(series.dataVals);
			ser.getValues().add(tvgr);

			TridasRadius r = series.defaults.getDefaultTridasRadius();
			r.getMeasurementSeries().add(ser);
			
			TridasSample s = series.defaults.getDefaultTridasSample();
			s.getRadiuses().add(r);
			
			TridasElement e = series.defaults.getDefaultTridasElement();
			e.getSamples().add(s);		
			
			// Handle plots/sub-plots and objects/sub-objects
			TridasObject o = null;
			if(series.defaults.getStringDefaultValue(DefaultFields.SUBPLOT).getValue()!=null)
			{
				o = series.defaults.getDefaultTridasObject();
				TridasObject subobj =  series.defaults.getDefaultTridasSubObject();
				subobj.getElements().add(e);
				o.getObjects().add(subobj);
			}
			else
			{
				o = series.defaults.getDefaultTridasObject();	
				o.getElements().add(e);
			}
		
			TridasProject p = series.defaults.getDefaultTridasProject();
			p.getObjects().add(o);
			
			projlist.add(p);
		}
		
		// Consolidate projects and return as array
		ArrayList<TridasProject> returnProj = TridasUtils.consolidateProjects(projlist);
		return returnProj.toArray(new TridasProject[returnProj.size()]);
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

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"MIT"};
		
		return new DendroFileFilter(exts, getShortName());

	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("kinsys.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("kinsys.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("kinsys.about.shortName");
	}
	

	private static class KinsysSeries
	{
		public final ArrayList<TridasValue> dataVals = new ArrayList<TridasValue>();
		public KinsysToTridasDefaults defaults; 
	}
	
}
