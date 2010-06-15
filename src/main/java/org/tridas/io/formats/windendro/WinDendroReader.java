package org.tridas.io.formats.windendro;

import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.windendro.WinDendroToTridasDefaults.WDDefaultField;
import org.tridas.io.formats.windendro.WinDendroToTridasDefaults.WinDendroDataType;
import org.tridas.io.util.DateUtils;
import org.tridas.io.warningsandexceptions.ConversionWarning;
import org.tridas.io.warningsandexceptions.InvalidDendroFileException;
import org.tridas.io.warningsandexceptions.ConversionWarning.WarningType;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class WinDendroReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(WinDendroReader.class);	
	private ArrayList<WinDendroSeries> seriesList = new ArrayList<WinDendroSeries>();
	
	private WinDendroToTridasDefaults defaults = null;      // defaults given by user
	Integer currentLineNumber = -1;
	
	public WinDendroReader() {
		super(WinDendroToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getDescription() {
		return I18n.getText("windendro.about.description");
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"txt"};
	}

	@Override
	public String getFullName() {
		return I18n.getText("windendro.about.fullName");
	}

	@Override
	public TridasProject getProject() {
		TridasProject project = defaults.getProjectWithDefaults(false);
		ArrayList<TridasObject> olist = new ArrayList<TridasObject>();
		
		for (WinDendroSeries series : seriesList)
		{
			TridasObject o = series.defaults.getObjectWithDefaults();
			TridasElement e = series.defaults.getElementWithDefaults();
			TridasSample s = series.defaults.getSampleWithDefaults();
			
			// Measurement Series
			
			TridasRadius r = series.defaults.getRadiusWithDefaults(false);
			TridasMeasurementSeries ms = series.defaults.getMeasurementSeriesWithDefaults();
			ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
			TridasValues values = series.defaults.getDefaultTridasValues();
			ArrayList<TridasValue> valuesList = new ArrayList<TridasValue>();
			
			for (Double val : series.dataDoubles)
			{
				TridasValue value = new TridasValue();
				value.setValue(String.valueOf(val));
				valuesList.add(value);
			}
			
			values.setValues(valuesList);
			valuesGroupList.add(values);
			ms.setValues(valuesGroupList);
			

			
			ArrayList<TridasMeasurementSeries> mslist = new ArrayList<TridasMeasurementSeries>();
			mslist.add(ms);
			r.setMeasurementSeries(mslist);
			ArrayList<TridasRadius> rlist = new ArrayList<TridasRadius>();
			rlist.add(r);
			s.setRadiuses(rlist);
			ArrayList<TridasSample> slist = new ArrayList<TridasSample>();
			slist.add(s);
			e.setSamples(slist);
			ArrayList<TridasElement> elist = new ArrayList<TridasElement>();
			elist.add(e);
			o.setElements(elist);
			olist.add(o);
						
		}
		
		project.setObjects(olist);
		return project;
	}

	@Override
	public String getShortName() {
		return I18n.getText("windendro.about.shortName");
	}

	@Override
	protected void resetReader() {
		// TODO Auto-generated method stub

	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		log.debug("Parsing: " + argFileString);
		defaults = (WinDendroToTridasDefaults) argDefaultFields;
		
		checkFileIsValid(argFileString);

		// Loop through all the data lines
		for (int i=2; i<argFileString.length; i++)
		{
			// Skip blank or tab filled lines
			if (argFileString[i].replaceAll("\\t", "").length()<50) continue;
			
			String[] data = argFileString[i].split("\\t");
			WinDendroToTridasDefaults seriesdefaults = (WinDendroToTridasDefaults) defaults.clone();
			WinDendroSeries currentSeries = new WinDendroSeries(seriesdefaults);
			this.currentLineNumber = i+1; 

			/**
			 * SET ALL VERSION 3 FIELDS
			 */
			// Tree name
			currentSeries.defaults.getStringDefaultValue(WDDefaultField.TREE_NAME).setValue(data[0]);
			
			// Path ID		
			if(!data[1].trim().equals(""))
			{
				currentSeries.defaults.getStringDefaultValue(WDDefaultField.PATH_ID).setValue(data[1]);
			}

			// Site ID
			currentSeries.defaults.getStringDefaultValue(WDDefaultField.SITE_ID).setValue(data[2]);

			// Last Ring year
			try{
				currentSeries.defaults.getSafeIntYearDefaultValue(WDDefaultField.LAST_RING_YEAR).setValue(Integer.parseInt(data[3]));
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidLastRing", currentLineNumber+"")));
			}
			
			// Sapwood Distance
			try{
				currentSeries.defaults.getDoubleDefaultValue(WDDefaultField.SAPWOOD_DISTANCE).setValue(Double.parseDouble(data[4]));
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidSapwoodDistance", currentLineNumber+"")));
			}
			
			// Tree height
			try{
				currentSeries.defaults.getDoubleDefaultValue(WDDefaultField.TREE_HEIGHT).setValue(Double.parseDouble(data[5]));
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidTreeHeight", currentLineNumber+"")));
			}
			
			// Tree age
			try{
				currentSeries.defaults.getIntegerDefaultValue(WDDefaultField.TREE_AGE).setValue(Integer.parseInt(data[6]));
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidTreeAge", currentLineNumber+"")));
			}
			
			// Section height
			try{
				currentSeries.defaults.getDoubleDefaultValue(WDDefaultField.SECTION_HEIGHT).setValue(Double.parseDouble(data[7]));
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidSectionHeight", currentLineNumber+"")));
			}
			
			// User variable
			currentSeries.defaults.getStringDefaultValue(WDDefaultField.USER_VARIABLE).setValue(data[8]);
			
			// Ring count
			try{
				currentSeries.defaults.getIntegerDefaultValue(WDDefaultField.RING_COUNT).setValue(Integer.parseInt(data[9]));
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidRingCount", currentLineNumber+"")));
			}
			
			// Data type
			GenericDefaultValue<WinDendroDataType> dataTypeField = (GenericDefaultValue<WinDendroDataType>) currentSeries.defaults
				.getDefaultValue(WDDefaultField.WD_DATA_TYPE);
			if(WinDendroDataType.fromCode(data[10])!=null)
			{
				dataTypeField.setValue(WinDendroDataType.fromCode(data[10]));
			}
			else
			{
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidDataType", currentLineNumber+"")));
			}
			
			// Offset to next 
			try{
				currentSeries.defaults.getIntegerDefaultValue(WDDefaultField.OFFSET_TO_NEXT).setValue(Integer.parseInt(data[11]));
			} catch (NumberFormatException e){
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidOffsetToNext", currentLineNumber+"")));
			}
						
			/**
			 * SET VERSION 4 FIELDS
			 */
			if(getFormatVersion(argFileString)>=4)
			{
				// Average disk diameter
				try{
					currentSeries.defaults.getDoubleDefaultValue(WDDefaultField.DISK_AVG_DIAM).setValue(Double.parseDouble(data[30]));
				} catch (NumberFormatException e){
					addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidDiskAvDiam", currentLineNumber+"")));
				}
			
				// Path Length
				try{
					currentSeries.defaults.getDoubleDefaultValue(WDDefaultField.PATH_LENGTH).setValue(Double.parseDouble(data[34]));
				} catch (NumberFormatException e){
					addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidPathLength", currentLineNumber+"")));
				}
				
				// Analysis timestamp
				if(!data[13].trim().equals(""))
				{
					if(DateUtils.getDateTimeFromWinDendroTimestamp(data[13])!=null)
					{
						currentSeries.defaults.getDateTimeDefaultValue(WDDefaultField.ANALYSIS_TIMESTAMP).
							setValue(DateUtils.getDateTimeFromWinDendroTimestamp(data[13]));
					}
					else
					{
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("windendro.invalidAnalysisTimestamp", currentLineNumber+"")));
					}
				}
				
			}
			
			
			/**
			 * EXTRACT ACTUAL DATA
			 */
			for (int col=getColWhereDataStarts(argFileString)-1; col < data.length; col++)
			{
				if(data[col].equals(""))
				{
					break;
				}
				
				try{
				currentSeries.dataDoubles.add(Double.parseDouble(data[col]));
				} catch (NumberFormatException e)
				{
					addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("fileio.invalidDataValue") + ": "+ data[col] ));
				}
			}
			if(currentSeries.dataDoubles.size()>0)
			{
				seriesList.add(currentSeries);
			}
		}
		
		
	}
	
	/**
	 * Get the column where the data values start
	 * 
	 * @param argFileString
	 * @return
	 */
	private Integer getColWhereDataStarts(String[] argFileString)
	{
		String[] parts = argFileString[0].split("\\t");
		if (parts.length!=8)
		{
			return null;
		}
		
		try{
		return Integer.parseInt(parts[3]);
		} catch (NumberFormatException e){}
		
		return null;
	}
	
	
	private void checkFileIsValid(String[] argFileString) throws InvalidDendroFileException{
		
		if(argFileString.length<3)
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.filetooshort"));
		}
		
		
		// Catch problems with the header line 1
		String[] headerParts = argFileString[0].split("\\t");
		if (headerParts.length!=8)
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidHeaderLine"), 1);
		}
		
		if (!headerParts[0].equalsIgnoreCase("WINDENDRO"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidHeaderWindendro"), 1);
		}
		
		if (!headerParts[1].equalsIgnoreCase("3") && !headerParts[1].equalsIgnoreCase("4"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidVersion", headerParts[1]), 1);
		}
		
		if (!headerParts[2].equalsIgnoreCase("R") )
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.onlyRowWiseSupported"), 1);
		}
		
		if (!headerParts[3].equalsIgnoreCase("13") && !headerParts[3].equalsIgnoreCase("36"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidVersion", I18n.getText("unknown")), 1);
		}
		
		if (!headerParts[4].equalsIgnoreCase("P") && !headerParts[4].equalsIgnoreCase("B"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidOrdering"), 1);
		}
		
		if (!headerParts[5].equalsIgnoreCase("I"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.incrementalOnly"), 1);
		}
		
		if (!headerParts[6].equalsIgnoreCase("Y") && !headerParts[6].equalsIgnoreCase("N"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidBarkYesNo"), 1);
		}
		
		if (!headerParts[7].equalsIgnoreCase("RING"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidHeaderRING"), 1);
		}
				
	}
	
	/**
	 * Extract the version number from the header
	 * 
	 * @param argFileString
	 * @return
	 */
	private Integer getFormatVersion(String[] argFileString)
	{
		// Catch problems with the header line 1
		String[] headerParts = argFileString[0].split("\\t");
		
		if (headerParts.length!=8) return null;
		
		try{ 
			return Integer.parseInt(headerParts[1]);
		} catch (NumberFormatException e){}
		
		return null;

	}
	
	
	/**
	 * Class to store the measurement series data
	 * 
	 * @author peterbrewer
	 */
	private static class WinDendroSeries{
		public WinDendroToTridasDefaults defaults;
		public final ArrayList<Double> dataDoubles = new ArrayList<Double>();
		
		private WinDendroSeries(WinDendroToTridasDefaults df)
		{
			defaults =df;
		}
	}
	
}
