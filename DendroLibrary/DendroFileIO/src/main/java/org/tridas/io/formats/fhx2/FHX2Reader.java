package org.tridas.io.formats.fhx2;

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
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.InvalidDendroFileException.PointerType;
import org.tridas.io.formats.fhx2.FHX2File.FHXMarker;
import org.tridas.io.formats.fhx2.FHX2ToTridasDefaults.DefaultFields;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataFormat;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldShapeCode;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DateTime;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRemark;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.spatial.SpatialUtils;

public class FHX2Reader extends AbstractDendroFileReader {
	private Integer lineNumDataBegins = null;
	private FHX2ToTridasDefaults defaults = new FHX2ToTridasDefaults();
	private SafeIntYear startYear;
	private Integer numberOfSamples;
	private Integer codeLength;
	private ArrayList<FHX2Series> seriesList = new ArrayList<FHX2Series>();
	private static final Logger log = LoggerFactory.getLogger(FHX2Reader.class);

	
	
	
	public FHX2Reader() {
		super(FHX2ToTridasDefaults.class);
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		
		
		for(int i=0; i<argFileString.length; i++)
		{
			String line = argFileString[i].trim();
			if(line.equalsIgnoreCase("FHX2 FORMAT") || line.equalsIgnoreCase("FIRE2 FORMAT"))
			{
				if(lineNumDataBegins!=null) throw new InvalidDendroFileException ("FHX2 FORMAT line found in file more than once.", i);
				lineNumDataBegins = i;
			}
			
		}
		
		if(lineNumDataBegins==null) throw new InvalidDendroFileException ("FHX2 FORMAT line not found in file");
		
		parseMetadata(argFileString);
		parseData(argFileString);
		
		

	}

	private String getMetadataValue(String line)
	{
		if(line==null || line.length()==0) return null;
		if(!line.contains(":")) return null;
		
		int ind = line.indexOf(":")+1;
		
		return line.substring(ind).trim();
		
		
	}
	
	private void parseMetadata(String[] argFileString) throws InvalidDendroFileException
	{
		Integer metadataLineNum = lineNumDataBegins+1;
		String[] parts = argFileString[metadataLineNum].split("\\s+");
		if(parts.length!=3) throw new InvalidDendroFileException("Metadata line does not contain start year, sample number and code length as expected. Values should be whole numbers separated by single spaces", metadataLineNum+1);
		
		
		try{
			startYear = new SafeIntYear(parts[0]);
			numberOfSamples = Integer.parseInt(parts[1]);
			codeLength = Integer.parseInt(parts[2]);
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException("Metadata in unexpected format", metadataLineNum);
		}
		
		for(int i=0; i<lineNumDataBegins-2; i++)
		{
			String line = argFileString[i];
			
			if(line.startsWith("Name of site"))
			{
				String value = getMetadataValue(line);
				if(value!=null)
				{
					defaults.getStringDefaultValue(TridasMandatoryField.OBJECT_TITLE).setValue(value);
				}
			}
			
			else if(line.startsWith("Site code"))
			{
				String value = getMetadataValue(line);
				if(value!=null)
				{
					defaults.getStringDefaultValue(DefaultFields.SITE_CODE).setValue(value);
				}
			}			
			
			else if(line.startsWith("Collection date"))
			{
				DateTime dt = DateUtils.parseDateTimeFromNaturalString(getMetadataValue(line));
				if(dt!=null)
				{
					defaults.getDateTimeDefaultValue(DefaultFields.COLLECTION_DATE).setValue(dt);
				}
				else if(getMetadataValue(line).trim().length()>0)
				{
					this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, "Unable to parse date from free text string", "Collection date"));
				}
			}			

			/*else if(line.startsWith("Collectors"))
			{
				String value = getMetadataValue(line);
				if(value!=null)
				{
					defaults.getStringDefaultValue(DefaultFields.COLLECTORS).setValue(value);

				}
			}	*/		
			
			else if(line.startsWith("Crossdaters"))
			{
				String value = getMetadataValue(line);
				if(value!=null)
				{
					defaults.getStringDefaultValue(DefaultFields.CROSSDATERS).setValue(value);

				}
			}			
			
			/*else if(line.startsWith("Number samples"))
			{
				String value = getMetadataValue(line);
			}	*/		
			
			else if(line.startsWith("Species name"))
			{
				String value = getMetadataValue(line);


				GenericDefaultValue<ControlledVoc> speciesField = (GenericDefaultValue<ControlledVoc>) defaults.getDefaultValue(DefaultFields.SPECIES_NAME);
				ControlledVoc voc = ITRDBTaxonConverter.getControlledVocFromCode(value);
				log.debug("Species found to be "+voc.getNormal()+" authority "+voc.getNormalStd());
				speciesField.setValue(voc);
				
				((GenericDefaultValue<ControlledVoc>) defaults.getDefaultValue(DefaultFields.SPECIES_NAME)).setValue(voc);
			}			
			
			/*else if(line.startsWith("Common name"))
			{
				String value = getMetadataValue(line);
			}*/			
			
			/*else if(line.startsWith("Habitat type"))
			{
				String value = getMetadataValue(line);
			}*/			
			
			else if(line.startsWith("Country"))
			{
				String value = getMetadataValue(line);
				if(value!=null)
				{
					defaults.getStringDefaultValue(DefaultFields.COUNTRY).setValue(value);
				}
			}			
			
			else if(line.startsWith("State"))
			{
				String value = getMetadataValue(line);
				if(value!=null)
				{
					defaults.getStringDefaultValue(DefaultFields.STATE).setValue(value);
				}
			}			
			
			/*else if(line.startsWith("County"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Park/Monument"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("National Forest"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Ranger district"))
			{
				String value = getMetadataValue(line);
			}	*/		
			
			else if(line.startsWith("Township"))
			{
				String value = getMetadataValue(line);
				if(value!=null)
				{
					defaults.getStringDefaultValue(DefaultFields.TOWN).setValue(value);
				}
			}			
			
			/*else if(line.startsWith("Range"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Section"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Quarter section"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("UTM easting"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("UTM northing"))
			{
				String value = getMetadataValue(line);
			}*/			
			
			else if(line.startsWith("Latitude"))
			{
				String value = getMetadataValue(line);

				if(value.length()>0)
				{
				
					try{
						Double lat = SpatialUtils.parseLatLonFromHalfLatLongString(value);		
						if(lat!=null) defaults.getDoubleDefaultValue(DefaultFields.LATITUDE).setValue(lat);
					} catch (NumberFormatException e)
					{
						this.addWarning(new ConversionWarning(WarningType.INVALID, "Unable to parse latitude from free text string", "Latitude"));
	
					}
				}
			}			
			
			else if(line.startsWith("Longitude"))
			{
				String value = getMetadataValue(line);

				if(value.length()>0)
				{
					try{
						Double lon = SpatialUtils.parseLatLonFromHalfLatLongString(value);		
						if(lon!=null) defaults.getDoubleDefaultValue(DefaultFields.LONGITUDE).setValue(lon);
					} catch (NumberFormatException e)
					{
						this.addWarning(new ConversionWarning(WarningType.INVALID, "Unable to parse longitude from free text string", "Longitude"));
	
					}
				}
			}			
			
			/*else if(line.startsWith("Topographic map"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Lowest elev"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Highest elev"))
			{
				String value = getMetadataValue(line);
			}*/			
			
			else if(line.startsWith("Slope"))
			{
				String value = getMetadataValue(line);

				if(value.length()>0)
				{
					try{
						Integer integer = Integer.parseInt(value);
						defaults.getIntegerDefaultValue(DefaultFields.SLOPE).setValue(integer);
					} catch (NumberFormatException e)
					{
						this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, "Unable to parse slope angle from free text string", "Slope"));
	
					}
				}
				
			}			
			
			else if(line.startsWith("Aspect"))
			{
				String value = getMetadataValue(line);

				if(value.length()>0)
				{			
					try{
						Integer integer = Integer.parseInt(value);
						defaults.getIntegerDefaultValue(DefaultFields.ASPECT).setValue(integer);
					} catch (NumberFormatException e)
					{
						this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, "Unable to parse aspect angle from free text string", "Aspect"));
	
					}
				}
			}			
			
			/*else if(line.startsWith("Area sampled"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Substrate type"))
			{
				String value = getMetadataValue(line);
			}			
	*/		
			
		}
		

	}
	
	private void parseData(String[] argFileString) throws InvalidDendroFileException
	{
		
		for(int samplenumber=0; samplenumber<numberOfSamples; samplenumber++)
		{
			// Loop through sample columns
			FHX2Series series = new FHX2Series((FHX2ToTridasDefaults) defaults.clone());
			String seriesname = "";
			
			
			for(int i=lineNumDataBegins+2; i<lineNumDataBegins+2+codeLength; i++)
			{
				String line = argFileString[i];
				try{
				seriesname+= line.substring(samplenumber,samplenumber+1);
				} catch (StringIndexOutOfBoundsException ex)
				{
					log.debug("Ignoring series name character");
				}
			}
			
			seriesname = seriesname.trim();
			
			series.defaults.getStringDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_TITLE).setValue(seriesname);
			
			if(argFileString[lineNumDataBegins+2+codeLength].trim().length()>0)
			{
				throw new InvalidDendroFileException("The line immediately before the data matrix should be empty", lineNumDataBegins+2+codeLength+1);
			}
			
			
			for(int i=lineNumDataBegins+2+codeLength+1; i<argFileString.length; i++)
			{
				try{
					String line = argFileString[i];
					

					series.datachars.add(line.substring(samplenumber,samplenumber+1));
				} catch (IndexOutOfBoundsException e)
				{
					break;
				}
			}
			
			seriesList.add(series);
		}
		
		Integer length = seriesList.get(0).datachars.size();
		int samplenumber = 0;
		for(FHX2Series s : seriesList)
		{
			String seriesname = s.defaults.getStringDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_TITLE).getStringValue();

			
			samplenumber++;
			if(s.datachars.size()!=length){ 
				throw new InvalidDendroFileException("All series must contain the same number of years. Sample "+seriesname+"(column "+samplenumber+") contains "+s.datachars.size()+" whereas the previous sample contained "+length);
			}
			
			
			Boolean started = false;
			Boolean finished = false;
			
			s.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).setValue(startYear);
			ArrayList<String> tempdata = new ArrayList<String>();
			
			int i=-1;
			for(String value : s.datachars)
			{
				i++;
				if(started==false)
				{
					if(value.equals("."))
					{
						s.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).setValue(
								s.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue().add(1)
								);
						continue;
					}
					else if (value.equals("]") || value.equals("}"))
					{
						throw new InvalidDendroFileException("Data value '"+value+"' found before start of sequence in sample "+seriesname+" (column "+samplenumber+")", lineNumDataBegins+codeLength+i+4);
					}
					else if (value.equals("[")  
							|| value.equals("{")  
							|| value.equals("U") 
							|| value.equals("u")
							|| value.equals("A")
							|| value.equals("a")
							|| value.equals("L")
							|| value.equals("l")
							|| value.equals("M")
							|| value.equals("m")
							|| value.equals("E")
							|| value.equals("e")
							|| value.equals("D")
							|| value.equals("d")
							|| value.equals("|")
							|| value.equals("."))
					{
						started=true;
						tempdata.add(value);
						
						if(value.equals("["))
						{
							s.defaults.getBooleanDefaultValue(DefaultFields.PITH).setValue(true);
						}
						else
						{
							s.defaults.getBooleanDefaultValue(DefaultFields.PITH).setValue(false);
						}
						
						continue;
					}
					else
					{
						throw new InvalidDendroFileException("Data value '"+value+"' unrecognised in "+seriesname+" (column "+samplenumber+")", lineNumDataBegins+codeLength+i+4);

					}

				}
				else if(finished==false)
				{
					tempdata.add(value);
					
					if(value.equals("[") || value.equals("{"))
					{
						throw new InvalidDendroFileException("Second start indicator found in sample "+seriesname+" (column "+samplenumber+")", lineNumDataBegins+codeLength+i+4);
					}
					else if(value.equals("]") || value.equals("}"))
					{
						
						s.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).setValue(
								s.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue().add(tempdata.size()-1)
								);
						finished = true;
						
						if(value.equals("]"))
						{
							s.defaults.getBooleanDefaultValue(DefaultFields.BARK).setValue(true);
						}
						else
						{
							s.defaults.getBooleanDefaultValue(DefaultFields.BARK).setValue(false);
						}
						
						
						
					}
					else if(!value.equals("U") 
								&& !value.equals("u")
								&& !value.equals("A")
								&& !value.equals("a")
								&& !value.equals("L")
								&& !value.equals("l")
								&& !value.equals("M")
								&& !value.equals("m")
								&& !value.equals("E")
								&& !value.equals("e")
								&& !value.equals("D")
								&& !value.equals("d")
								&& !value.equals("|")
								&& !value.equals("."))
					{
						this.addWarning(new ConversionWarning(WarningType.IGNORED, "Non-standard '"+value+"' found in sample "+seriesname+" (column "+samplenumber+"), line "+lineNumDataBegins+codeLength+i+4));
					}
						
				}
				else
				{
					if(!value.equals(".") && !value.equals("\u001A"))
					{
						throw new InvalidDendroFileException("Data value '"+value+"' found after end of the sequence in sample "+seriesname+" (column "+samplenumber+")", lineNumDataBegins+codeLength+i+4);
					}
					else if (value.equals("\u001A"))
					{
						length = length-1;
					}
				}
				
				
			}
			
			if(finished=false)
			{
				throw new InvalidDendroFileException("Reached end of data in  sample "+seriesname+" (column "+samplenumber+")"+" without reaching the bark or end of series identifier", lineNumDataBegins+codeLength+i+4);
			}
			
			// Replace original data values
			s.datachars = tempdata;
		}
		
	}
	
	
	@Override
	protected void resetReader() {
		lineNumDataBegins = null;
		defaults = new FHX2ToTridasDefaults();

	}

	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"fhx"};
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("fhx2.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("fhx2.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("fhx2.about.shortName");
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	private TridasProject getProject(){
		TridasProject project = defaults.getProjectWithDefaults();
		
		TridasObject object = defaults.getObjectWithDefaults();
		
		
		for(FHX2Series s : seriesList)
		{
			
			

			
			TridasElement element = s.defaults.getElementWithDefaults();
			TridasSample sample = s.defaults.getSampleWithDefaults();
			TridasRadius radius = s.defaults.getRadiusWithDefaults(false);
			TridasMeasurementSeries series = s.defaults.getMeasurementSeriesWithDefaults();
			
			TridasValues valuesGroup = s.defaults.getTridasValuesWithDefaults();
			valuesGroup.setValues(getTridasValuesFromStringArray(s.datachars));
			
			series.getValues().add(valuesGroup);
			radius.getMeasurementSeries().add(series);
			sample.getRadiuses().add(radius);
			element.getSamples().add(sample);
			object.getElements().add(element);
		}
		
		
		
		project.getObjects().add(object);
		
		return project;
		
	}
	
	
	private ArrayList<TridasValue> getTridasValuesFromStringArray(ArrayList<String> arr)
	{
		ArrayList<TridasValue> values = new ArrayList<TridasValue>();
		
		for(String v : arr)
		{
			TridasValue value = new TridasValue();
			value.setValue("0");
			TridasRemark remark = getRemarkForMarker(v);
			if(remark!=null) value.getRemarks().add(remark);		
			values.add(value);
		}
		
		
		return values;
		
	}
	
	private TridasRemark getRemarkForMarker(String v)
	{
		FHXMarker marker;
		try{
			marker = FHXMarker.fromCode(v);
		} catch (IllegalArgumentException e)
		{
			return null;
		}
		
		if(marker==null) return null;
	
		
		TridasRemark remark = new TridasRemark();
		remark.setNormalStd(FHX2File.FHX_DOMAIN);
		remark.setNormalId(marker.getCode());
		remark.setNormal(marker.getDescription());
		
		return remark;
	}
	
	@Override
	public TridasProject[] getProjects() {
		TridasProject projects[] = new TridasProject[1];
		projects[0] = this.getProject();
		return projects;
	}

	@Override
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

		String[] exts = new String[] {"fhx"};
		
		return new DendroFileFilter(exts, getShortName());

	}
	
	private static class FHX2Series {

		public ArrayList<String> datachars = new ArrayList<String>();		
		public FHX2ToTridasDefaults defaults;
		
		public FHX2Series(FHX2ToTridasDefaults d)
		{
			defaults = d;
		}
	}
	
	

}
