package org.tridas.io.formats.tucson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults.TucsonField;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.YearRange;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasAddress;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasurementSeriesPlaceholder;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRadiusPlaceholder;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.SeriesLink.IdRef;


/**
 * Reader for the Tucson file format.  
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 *
 */
public class TucsonReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(TucsonReader.class);
	// defaults given by user
	private TucsonToTridasDefaults defaults = null;
	
	// helper for parsing
	private TridasToTucsonDefaults tucsonFields = null;
	
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	private ArrayList<TridasDerivedSeries> dseriesList = new ArrayList<TridasDerivedSeries>();

	private String lastSeriesCode = null;
	private TridasProject project;
	private SafeIntYear lastYearMarker = null;
	private Boolean isChronology = null;
	
	private int currentLineNumber = 0;
	
	public TucsonReader() {
		super(TucsonToTridasDefaults.class);
	}
		
	
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException{
		defaults = (TucsonToTridasDefaults) argDefaultFields;
		tucsonFields = new TridasToTucsonDefaults();
		log.debug("starting tucson file parsing");
		
		project = defaults.getProjectWithDefaults(true);
		
		boolean isValidFile = isValidFile(argFileString);
		
		
		int index = 0;
		
		String headercache1 = argFileString[0];
		String headercache2 = argFileString[1];
		String headercache3 = argFileString[2];
				
		// Now continue and read data
		Boolean withinChronologyBlock = false;
		
		for( ; index < argFileString.length; index++){
			String line = argFileString[index];
			currentLineNumber = index+1;
			
			switch (getLineType(line))
			{
				case HEADER_LINE1:										
					if(withinChronologyBlock) break;
					headercache1 = line; headercache2 = null; headercache3 = null;
					break;
				case HEADER_LINE2:
					if(withinChronologyBlock) break;
					if (headercache1!=null && headercache2==null)
					{
						headercache2 = line; headercache3 = null;
					}
					else
					{
						if(headercache1!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
										I18n.getText("tucson.nonstandardHeaderLine")+": "+headercache1));}
						if(headercache2!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
										I18n.getText("tucson.nonstandardHeaderLine")+": "+headercache2));}
						if(headercache3!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
										I18n.getText("tucson.nonstandardHeaderLine")+": "+headercache3));}
						headercache1=null; headercache2=null; headercache3=null;
					}
					break;
				case HEADER_LINE3:
					if(withinChronologyBlock) break;					
					if (headercache2!=null && headercache3==null)
					{
						headercache3 = line;
					}
					else
					{
						if(headercache1!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
								I18n.getText("tucson.nonstandardHeaderLine")+": "+headercache1));}
						if(headercache2!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
								I18n.getText("tucson.nonstandardHeaderLine")+": "+headercache2));}
						if(headercache3!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
								I18n.getText("tucson.nonstandardHeaderLine")+": "+headercache3));}
						headercache1=null; headercache2=null; headercache3=null;
					}	
					
					// This is a complete three line header so go ahead and load metadata
					loadMetadata(headercache1, headercache2, headercache3);
					break;
				case CRN_DATA:
				case CRN_DATA_PARTIAL:
				case CRN_DATA_COMPLETE:
				case RWL_DATA:
				case RWL_DATA_PARTIAL:
				case RWL_DATA_COMPLETE:
					
					// Clear header cache and load data
					headercache1=null; headercache2=null; headercache3=null;
					
					if(this.isChronology)
					{
						if(withinChronologyBlock==false)
						{
							// Remove the skeleton radius entity and replace with radiusPlaceHolder 
							// and measurementSeriesPlaceHolder
							replaceRadiusWithPlaceholder();
						}
	
						// This is CRN data 
						withinChronologyBlock=true;
						loadCRNData(line);
						break;	
					}
					else
					{
						// This is RWL data 				
						loadRWLData(line);
						break;
					}
				default:
					if (line!=null){
						addWarningToList(new ConversionWarning(WarningType.IGNORED, 
								I18n.getText("tucson.nonstandardHeaderLine")+": "+line));
					}
					break;
				
			}
	
		}
	
		
	}
	
	
	private void replaceRadiusWithPlaceholder()
	{
		try{
		TridasObject o = project.getObjects().get(0);
		TridasElement e = o.getElements().get(0);
		TridasSample s = e.getSamples().get(0);
		s.setRadiuses(null);
		
		TridasRadiusPlaceholder rph = new TridasRadiusPlaceholder();
		TridasMeasurementSeriesPlaceholder msph = new TridasMeasurementSeriesPlaceholder();
		msph.setId("XREF-"+UUID.randomUUID().toString() );
		rph.setMeasurementSeriesPlaceholder(msph);
		
		s.setRadiusPlaceholder(rph);
		ArrayList<TridasSample> samplist= new ArrayList<TridasSample>();
		samplist.add(s);
		
		project.getObjects().get(0).getElements().get(0).setSamples(samplist);
		
		}
		catch (NullPointerException e)
		{
			
		}
	}
	
	/**
	 * Attempts to read a line of standard RWL format data.  The validity of the format
	 * of the line should have previously been checked using matchesLineType();
	 * 
	 * @param line
	 * @throws InvalidDendroFileException 
	 */
	private void loadRWLData(String line) throws InvalidDendroFileException
	{
		TridasUnit units = new TridasUnit();
		
		ArrayList<TridasValue> thisDecadesValues = new ArrayList<TridasValue>();
		
		// Work out if we're using 6 or 8 digit series codes
		String regex = "^[\\d\\w\\s]{8}[\\s\\d-]{4}";
	    Pattern p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m1 = p1.matcher(line);
	    int codeLength = 6;
	    if(m1.find()) codeLength=8;
				
	    // Extract the code and then remove
		String thisCode = line.substring(0, codeLength);
		line = line.substring(codeLength);
		
		// Extract the year value and remove from line
		SafeIntYear currentLineYearMarker = new SafeIntYear(line.substring(0,4));
		line = line.substring(5).trim();
		

		checkYearMarker(thisCode, currentLineYearMarker);
		
		// Split values into string array.  Limiting to 10 values (decade).
		String[] vals = line.split("\\s+", 10);
		Boolean lastYearFlag = false;
		
		// Intercept no data values and stop markers
		for(String value : vals)
		{
			TridasValue v = new TridasValue();
			
			if(value.equals("999"))
			{
				// 0.01mm stop marker
				units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
				lastYearFlag = true;
				break;
			}
			else if (value.equals("-9999"))
			{
				// 0.001mm stop marker
				units.setNormalTridas(NormalTridasUnit.MICROMETRES);
				lastYearFlag = true;
				break;
			}
			else if (value.equals("-999"))
			{
				// Missing data value - override to zero
				v.setValue("0");
				thisDecadesValues.add(v);
				break;
			}
			else if (value.matches("[.]"))
			{
				// This is a non-standard placeholder used after the
				// stop marker to indicate where a value would go 
				break;
			}
			else if (value.matches("[^\\d]"))
			{
				// Must contain letters so invalid
				this.addWarningToList(new ConversionWarning(
						WarningType.INVALID, 
						"Data values contain non-numeric characters"));
				return;	
			}
			else
			{
				// Standard numerical ring width value
				v.setValue(value);
				thisDecadesValues.add(v);
			}
		}
		
		if ((lastSeriesCode==null) || 
			(( lastSeriesCode!=null) && (!lastSeriesCode.equals(thisCode)))
		    )
		{
			// This must be the first bit of data we've come to
			// *OR* this is a new data series chunk.  Either way
			// we need a new series to add the data to...
			
			lastSeriesCode=thisCode;
						
			TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
			
			// Build identifier for series
			TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
			seriesId.setValue(thisCode.trim());
			seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
			
			// Build interpretation group for series
			TridasInterpretation interp = new TridasInterpretation();
			interp.setFirstYear(currentLineYearMarker.toTridasYear(DatingSuffix.AD));

			// Add values to nested value(s) tags
			TridasValues valuesGroup = new TridasValues();
			valuesGroup.setValues(thisDecadesValues);
			valuesGroup.setUnitless(new TridasUnitless());
			GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) defaults.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
			valuesGroup.setVariable(variable.getValue());
			ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
			valuesGroupList.add(valuesGroup);	
			
			// Add all the data to the series
			series.setValues(valuesGroupList);
			series.setInterpretation(interp);
			series.setIdentifier(seriesId);
			series.setTitle(thisCode.trim());
			series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime() );

			// Add series to our list
			mseriesList.add(series);
	
		}
		else if (lastSeriesCode.equals(thisCode))
		{
			// This is another line of the same series
			
			lastSeriesCode=thisCode;
			
			// Extract most recent measurementSeries from list
			TridasMeasurementSeries halfdone = mseriesList.get(mseriesList.size()-1);
			ArrayList<TridasValue> previouslygotvalues = (ArrayList<TridasValue>) halfdone.getValues().get(0).getValues();
			
			// Add this decades values to the series
			previouslygotvalues.addAll(thisDecadesValues);
			halfdone.getValues().get(0).setValues(previouslygotvalues);
			mseriesList.set(mseriesList.size()-1, halfdone);
			
			// If we've managed to detect the units then go ahead and set
			if(units!=null)
			{
				halfdone.getValues().get(0).setUnitless(null);
				halfdone.getValues().get(0).setUnit(units);
			}
			
			// If this is the last year in the block then set the interpretation accordingly
			if(lastYearFlag)
			{
				TridasInterpretation interp = halfdone.getInterpretation();
				interp.setLastYear(currentLineYearMarker.add(thisDecadesValues.size()-1).toTridasYear(DatingSuffix.AD));
			}
			
		}

		
	}
	
	/**
	 * Attempts to read a line of CRN style chronology data.  The validity
	 * of the line's format should have been checked previously using 
	 * matchesLineType().
	 *  
	 * @param line
	 */
	private void loadCRNData(String line) throws InvalidDendroFileException
	{
		
		ArrayList<TridasValue> thisDecadesValues = new ArrayList<TridasValue>();
		
		// Work out if we're using 6 or 8 digit series codes
		String regex = "^[\\d\\w\\s]{8}[\\s\\d-]{4}([\\s\\d-]{4}\\s((\\s\\d)|(\\d\\d))){10}";
	    Pattern p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m1 = p1.matcher(line);
	    int codeLength = 6;
	    if(m1.find()) codeLength=8;
				
	    // Extract the code and then remove
		String thisCode = line.substring(0, codeLength);
		line = line.substring(codeLength);
		
		// Extract the year value and remove
		SafeIntYear currentLineYearMarker = null;
		try {
			currentLineYearMarker = new SafeIntYear(line.substring(0,4));
			} catch (NumberFormatException e){
				throw new InvalidDendroFileException(I18n.getText("tucson.invalidDecadeMarker", 
						line.substring(0,4)), 
						this.currentLineNumber);
			}
		
		SafeIntYear oldestYear = currentLineYearMarker;
		Boolean containsYoungestYear = false;
		Boolean containsOldestYear = false;
		line = line.substring(4);
		
		// Check this year marker to see it is within expected range
		this.checkYearMarker(thisCode, currentLineYearMarker);
		
		// Extract values and counts
		ArrayList<Integer> vals = new ArrayList<Integer>();
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for(int charpos = 0; charpos<=line.length()-7; charpos = charpos+7)
		{
			try{
				Integer value = Integer.valueOf(line.substring(charpos, charpos+4).trim());
				Integer count = Integer.valueOf(line.substring(charpos+4, charpos+7).trim());
				vals.add(value);
				counts.add(count);
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"),
						getCurrentLineNumber());
			}
		}
		
		// number of values and counts *must* be the same
		if (vals.size()!= counts.size() ) 
		{ 
			throw new InvalidDendroFileException(I18n.getText("fileio.countsAndValuesDontMatch",
					new String [] {String.valueOf(vals.size()), String.valueOf(counts.size())}),
					getCurrentLineNumber());
		}
		
		Boolean inLeader = true;   // Flag to show if we're in the lead in 9990 values
		//Boolean inTrailer = false; // Flag to show if we're in the lead out 9990 values
		for (int i=0; i<=vals.size()-1; i++)
		{
			TridasValue v = new TridasValue();		
			
			// Check for special values
			if (vals.get(i).equals(Integer.valueOf("9990")))
			{
				// missing data value
				if (inLeader==false) 
				{
					// Not in the leader so we must have reached the lead out 
					// nodata values.  Therefore set the oldestYear value and
					// return
					oldestYear.add(i+1);
					containsOldestYear=true;
				}
				else
				{
					containsYoungestYear = true;
				}
				break;
					
			}
		
			if (inLeader==true)
			{
				// inLeader is true but but this is not a 9990 value so this must
				// be the first *proper* value.  Set the youngestYear and turn off
				// the leader flag.
				currentLineYearMarker.add(i+1);
				inLeader=false;
			}

			// Go ahead and set value
			v.setValue(vals.get(i).toString());
			v.setCount(counts.get(i));
			thisDecadesValues.add(v);
			
		}
		
		if ((lastSeriesCode==null) || 
				(( lastSeriesCode!=null) && (!lastSeriesCode.equals(thisCode)))
			    )
			{
				// This must be the first bit of data we've come to
				// *OR* this is a new data series chunk.  Either way
				// we need a new series to add the data to...
				
				lastSeriesCode=thisCode;
				
				TridasDerivedSeries series = defaults.getDerivedSeriesWithDefaults();
				TridasInterpretation interp = new TridasInterpretation();
				
				// Build interpretation group for series	
				interp.setFirstYear(currentLineYearMarker.toTridasYear(DatingSuffix.AD));					
				
				
				// Build identifier for series
				TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
				seriesId.setValue(thisCode.trim());
				seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
				
				// Add values to nested value(s) tags
				TridasValues valuesGroup = new TridasValues();
				valuesGroup.setValues(thisDecadesValues);
				valuesGroup.setUnitless(new TridasUnitless());
				
				GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) defaults.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
				valuesGroup.setVariable(variable.getValue());
				//TridasVariable variable = new TridasVariable();
				//variable.setValue(defaults.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE).getStringValue());
				//valuesGroup.setVariable(variable);
				
				ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
				valuesGroupList.add(valuesGroup);	
				
				
				// Do Link to measurementSeriesPlaceholder
				try{SeriesLink link = new ObjectFactory().createSeriesLink();
					IdRef ref = new ObjectFactory().createSeriesLinkIdRef();
					ArrayList<SeriesLink> linkList = new ArrayList<SeriesLink>();
					ref.setRef(project.getObjects().get(0).getElements().get(0).getSamples().get(0).getRadiusPlaceholder().getMeasurementSeriesPlaceholder());
					link.setIdRef(ref);
					linkList.add(link);
					series.getLinkSeries().setSeries(linkList);
				} catch (NullPointerException e)
				{
					
				}
								
				// Add all the data to the series
				series.setValues(valuesGroupList);
				series.setTitle(thisCode.trim());
				series.setIdentifier(seriesId);
				series.setInterpretation(interp);
				
				// Add series to our list
				dseriesList.add(series);
				
				
			}
		else if (lastSeriesCode.equals(thisCode))
		{
			// This is another line of the same series
			
			lastSeriesCode=thisCode;
			
			// Extract most recent measurementSeries from list
			TridasDerivedSeries halfdone = dseriesList.get(dseriesList.size()-1);
			ArrayList<TridasValue> previouslygotvalues = (ArrayList<TridasValue>) halfdone.getValues().get(0).getValues();
			
			// Add this decades values to the series
			previouslygotvalues.addAll(thisDecadesValues);
			halfdone.getValues().get(0).setValues(previouslygotvalues);
			dseriesList.set(dseriesList.size()-1, halfdone);
			
			// If we've managed to detect the units then go ahead and set
			/*if(units!=null)
			{
				halfdone.getValues().get(0).setUnitless(null);
				halfdone.getValues().get(0).setUnit(units);
			}
			*/
			// If this is the last year in the block then set the interpretation accordingly
			if(containsOldestYear)
			{
				TridasInterpretation interp = halfdone.getInterpretation();
				interp.setLastYear(oldestYear.toTridasYear(DatingSuffix.AD));
				int numberofvalues = previouslygotvalues.size();
				SafeIntYear firstYear = new SafeIntYear(interp.getFirstYear());
				
			}
			
		}			
	}
	
	
	/**
	 * Checks whether three strings are likely to be the header portion
	 * of a standard tucson file.  This only returns true if the three 
	 * lines match the standard definition from the NOAA website
	 * 
	 * @param line1
	 * @param line2
	 * @param line3
	 * @return
	 */
	private boolean isLikelyHeader(String line1, String line2, String line3)
	{
		// Check lines match the headerline1, 2 and 3 format respectively
		if(	(matchesLineType(TucsonLineType.HEADER_LINE1, line1)) &&
			(matchesLineType(TucsonLineType.HEADER_LINE2, line2)) &&
			(matchesLineType(TucsonLineType.HEADER_LINE3, line3)) 
		  )
		{
			// Check the first 6 characters are all the same
			if ((line1.substring(0,6).equals(line2.substring(0,6))) &&
				(line2.substring(0,6).equals(line3.substring(0,6)))
			)
			{
				return true;
			}
		}
				
		return false;
			
	}
	
	/**
	 * The year marker at the beginning of the row should typically be 10 years
	 * larger than the year marker from the previous row.  For the first and last
	 * row of a dataset, this could also be between 1 and 10 years larger.  
	 * Complications arise when we have a file with multiple series.  If each series
	 * has a valid header, then we're fine as the loadMetadata() call resets the 
	 * lastYearMarker.  If the header is malformed though, we have to rely on the
	 * series code changing.  If the series code remains the same *and* the year
	 * marker gets reset, we have to throw a fatal error.
	 * 
	 * @param thisCode
	 * @param currentLineYearMarker
	 * @throws InvalidDendroFileException
	 */
	private void checkYearMarker(String thisCode, SafeIntYear currentLineYearMarker) throws InvalidDendroFileException 
	{

		if(this.lastYearMarker==null || !lastSeriesCode.equals(thisCode))
		{
			// This is the first marker in the series so fine.
			this.lastYearMarker = currentLineYearMarker;
		}
		else
		{
			YearRange expectedRange = new YearRange(lastYearMarker.add(1), lastYearMarker.add(10));		
			if (expectedRange.contains(currentLineYearMarker))
			{
				// Marker is in expected range (1 to 10 years of last marker)
				lastYearMarker = currentLineYearMarker;
			}
			else if ((currentLineYearMarker.compareTo(lastYearMarker)<0) && 
					 (lastSeriesCode.equals(thisCode)))
			{
				// Marker is less than the previous marker. 
				throw new InvalidDendroFileException(I18n.getText("tucson.newSeriesSameCode", 
						currentLineYearMarker.toString()),
						this.getCurrentLineNumber());						
			}
			else
			{
				// This year marker is not within a decade of the last year marker 
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidDecadeMarker", 
						currentLineYearMarker.toString()),
						Integer.parseInt(lastYearMarker.toString()));
			}
		}
	}
	
	
	/**
	 * Check whether a line matches a specific line type.  This is simple 
	 * regexing so it isn't perfect, especially for headers.  The HEADER_LINE3
	 * inparticular is very likely to give false positives.
	 * 
	 * @param type
	 * @param line
	 * @return
	 */
	private boolean matchesLineType(TucsonLineType type, String line)
	{
		String regex = null;
		Pattern p1;
		Matcher m1;
		
		// If line is empty or very short save ourselves the hassle and return now
		if (line==null) return false;
		if(line.length()<=6){
			if(type.equals(TucsonLineType.NON_DATA)){ return true;}
			else {return false;}
		}
		
		switch(type){
		case RWL_DATA_COMPLETE:
			regex = "^[\\d\\w\\s]{8}[\\d-]{4}([\\s\\d-]{6}){10}";
		    p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    m1 = p1.matcher(line);
			return m1.find();		
		case RWL_DATA_PARTIAL:
			regex = "^[\\d\\w\\s]{8}[\\d-]{4}[\\s\\d-]{6}";
		    p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    m1 = p1.matcher(line);
		    if (!matchesLineType(TucsonLineType.RWL_DATA_COMPLETE, line)) return m1.find();
		    else return false;
		case RWL_DATA:
		    return matchesLineType(TucsonLineType.RWL_DATA_PARTIAL, line) || matchesLineType(TucsonLineType.RWL_DATA_COMPLETE, line);
		case CRN_DATA_COMPLETE:
			regex = "^([\\d\\w\\s]{8}|[\\d\\w\\s]{6})[\\d\\s-]{4}([\\s\\d-]{4}\\s((\\s\\d)|(\\d\\d))){10}";
		    p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    m1 = p1.matcher(line);
			return m1.find();			
		case CRN_DATA_PARTIAL:
			// I don't think this should exist.  NOAA webpage says all CRN data lines should contain 10 values 
			/*regex = "^([\\d\\w\\s]{8}|[\\d\\w\\s]{6})[\\d\\s-]{4}([\\s\\d-]{4}\\s((\\s\\d)|(\\d\\d)))";
		    p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    m1 = p1.matcher(line);
			if (!matchesLineType(TucsonLineType.CRN_DATA_COMPLETE, line)) return m1.find();
			else */return false;
		case CRN_DATA:
			return matchesLineType(TucsonLineType.CRN_DATA_PARTIAL, line) || matchesLineType(TucsonLineType.CRN_DATA_COMPLETE, line);   
		case DATA:
			if( (matchesLineType(TucsonLineType.RWL_DATA, line)) ||
				(matchesLineType(TucsonLineType.CRN_DATA, line)))
			{
				return true;
			}
			else
			{
				return false;
			}			
		case NON_DATA:
			if( (!matchesLineType(TucsonLineType.RWL_DATA, line)) &&
				(!matchesLineType(TucsonLineType.CRN_DATA, line)))
			{
				return true;
			}
			else
			{
				return false;
			}		
		case HEADER_LINE1:
			regex = "^[\\d\\w\\s]{9}[^\\n]{52}[A-Z]{4}";
		    p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    m1 = p1.matcher(line);
			return m1.find();				
		case HEADER_LINE2:
			regex = "^[\\d\\w\\s]{9}[^\\n]{21}[\\s]{10}[0-9mMft]{5}[\\s]{2}[0-9\\s]{10}[\\s]{10}[\\d\\s]{9}";
			p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    m1 = p1.matcher(line);
			return m1.find();		
		case HEADER_LINE3:
			regex = "^[\\d\\w\\s]{9}[\\w\\s.,-]{62}[\\d\\s]{8}";
			p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    m1 = p1.matcher(line);
			return m1.find();	
		case HEADER:
			if( (matchesLineType(TucsonLineType.HEADER_LINE1, line)) ||
				(matchesLineType(TucsonLineType.HEADER_LINE2, line)) ||
				(matchesLineType(TucsonLineType.HEADER_LINE3, line))
					)
				{
					return true;
				}
				else
				{
					return false;
				}
		default:
			return false;
		}
		

	}


	protected void loadMetadata(String line1, String line2, String line3){
		
		if(isLikelyHeader(line1, line2, line3))
		{
			if(line1.length()>64)
			{
				tucsonFields.getStringDefaultValue(TucsonField.SITE_CODE).setValue((line1.substring(0, 6)).trim());
				tucsonFields.getStringDefaultValue(TucsonField.SITE_NAME).setValue((line1.substring(9, 61)).trim());
				tucsonFields.getStringDefaultValue(TucsonField.SPECIES_CODE).setValue((line1.substring(61, 65)).trim());
				log.debug("Site Code='"+((line1.substring(0, 6)).trim())+"'");
				log.debug("Site name='"+((line1.substring(9, 61)).trim())+"'");
				log.debug("Species code='"+((line1.substring(61, 65)).trim())+"'");
			}
			if(line2.length()>75)
			{
				tucsonFields.getStringDefaultValue(TucsonField.STATE_COUNTRY).setValue( (line2.substring(9, 21)).trim());
				tucsonFields.getStringDefaultValue(TucsonField.SPECIES_NAME).setValue((line2.substring(22, 29)).trim());
				tucsonFields.getIntegerDefaultValue(TucsonField.ELEVATION).setValue(Integer.parseInt((line2.substring(40, 44)).trim()));
				tucsonFields.getStringDefaultValue(TucsonField.LATLONG).setValue((line2.substring(47, 56)).trim());
				//tucsonFields.getStringDefaultValue(TucsonField.RANGE).setValue((line2.substring(67, 75)).trim());
				log.debug("State country='"+tucsonFields.getStringDefaultValue(TucsonField.STATE_COUNTRY).getStringValue()+"'");
				log.debug("Species name='"+tucsonFields.getStringDefaultValue(TucsonField.SPECIES_NAME).getStringValue()+"'");
				log.debug("Elevation='"+tucsonFields.getIntegerDefaultValue(TucsonField.ELEVATION).getStringValue()+"'");
				log.debug("LatLong='"+tucsonFields.getStringDefaultValue(TucsonField.LATLONG).getStringValue()+"'");
				//log.debug("Range='"+tucsonFields.getStringDefaultValue(TucsonField.RANGE).getStringValue()+"'");
			}			
			if(line3.length()>79)
			{
				tucsonFields.getStringDefaultValue(TucsonField.INVESTIGATOR).setValue((line3.substring(9, 71)).trim());
				tucsonFields.getStringDefaultValue(TucsonField.COMP_DATE).setValue((line2.substring(72, 79)).trim());
				log.debug("Investigator='"+tucsonFields.getStringDefaultValue(TucsonField.INVESTIGATOR).getStringValue()+"'");
				log.debug("Comp date='"+tucsonFields.getStringDefaultValue(TucsonField.COMP_DATE).getStringValue()+"'");
			}
			
			// Reset last year marker
			this.lastYearMarker = null;
			
		

		}
		else
		{		
			if(line1!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
					I18n.getText("tucson.nonstandardHeaderLine")+": "+line1));}
			if(line2!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
					I18n.getText("tucson.nonstandardHeaderLine")+": "+line2));}
			if(line3!=null) {addWarningToList(new ConversionWarning(WarningType.IGNORED, 
					I18n.getText("tucson.nonstandardHeaderLine")+": "+line3));}
	
			
		}
		
		populateProjectMetadata();
		
		
	}
	
	protected void populateProjectMetadata()
	{
		// Populate our TridasObject
		TridasObject o;
		if (project!=null)
		{
			// Project already exists so let's create a new object 
			o = defaults.getObjectWithDefaults(true);
			
		}
		else
		{
			project = defaults.getProjectWithDefaults(false);
			o = defaults.getObjectWithDefaults(true);
		}
			
		o.setTitle(tucsonFields.getDefaultValue(TucsonField.SITE_NAME).getStringValue().trim());
		
		TridasIdentifier objIdentifier = new TridasIdentifier();
		objIdentifier.setValue(tucsonFields.getDefaultValue(TucsonField.SITE_CODE).getStringValue().trim());
		objIdentifier.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		o.setIdentifier(objIdentifier);
		
		ControlledVoc taxon = new ControlledVoc();
		taxon.setNormalStd("ITRDB/WSL Dendrochronology Species Database");
		taxon.setNormalId(tucsonFields.getDefaultValue(TucsonField.SPECIES_CODE).getStringValue().trim());
		//taxon.setNormal(tucsonFields.getDefaultValue(TucsonField.SPECIES_NAME).trim());
		taxon.setValue(tucsonFields.getDefaultValue(TucsonField.SPECIES_NAME).getStringValue().trim());
		o.getElements().get(0).setTaxon(taxon);
		
		// Extract and set location fields
		TridasLocation loc = new TridasLocation();
		ArrayList<Double> points = new ArrayList<Double>();
		// TODO Extract lat longs from latlong string
		points.add(50d);
		points.add(70d);
		Pos pos = new Pos();
		pos.setValues(points);
		PointType pt = new PointType();
		pt.setPos(pos);
		pt.setSrsName("urn:ogc:def:crs:EPSG:6.6:4326");
		TridasLocationGeometry geom = new TridasLocationGeometry();
		geom.setPoint(pt);
		loc.setLocationGeometry(geom);
		TridasAddress adr = new TridasAddress();
		adr.setStateProvinceRegion(tucsonFields.getDefaultValue(TucsonField.STATE_COUNTRY).getStringValue().trim());
		loc.setAddress(adr);
		o.setLocation(loc);
		
		project.setInvestigator(tucsonFields.getDefaultValue(TucsonField.INVESTIGATOR).getStringValue().trim());
		// TODO compdate, range
		
		List<TridasObject> objlist = project.getObjects();
		objlist.add(o);
		project.setObjects(objlist);
		
	}
	
	
	
	/**
	 * Attempt to determine the type of line this is through regexes.  Note
	 * this is not perfect, especially for headers where there is little to 
	 * distinguish them from random text
	 * 
	 * @param line
	 * @return
	 */
	protected TucsonLineType getLineType(String line)
	{
		if(matchesLineType(TucsonLineType.CRN_DATA_COMPLETE, line))
		{
			return TucsonLineType.CRN_DATA_COMPLETE;
		}
		else if(matchesLineType(TucsonLineType.CRN_DATA_PARTIAL, line))
		{
			return TucsonLineType.CRN_DATA_PARTIAL;
		}
		else if (matchesLineType(TucsonLineType.RWL_DATA_COMPLETE, line))
		{
			return TucsonLineType.RWL_DATA_COMPLETE;
		}
		else if (matchesLineType(TucsonLineType.RWL_DATA_PARTIAL, line))
		{
			return TucsonLineType.RWL_DATA_PARTIAL;
		}
		else if(matchesLineType(TucsonLineType.HEADER_LINE1, line))
		{
			return TucsonLineType.HEADER_LINE1;
		}
		else if (matchesLineType(TucsonLineType.HEADER_LINE2, line))
		{
			return TucsonLineType.HEADER_LINE2;
		}
		else if (matchesLineType(TucsonLineType.HEADER_LINE3, line))
		{
			return TucsonLineType.HEADER_LINE3;
		}
		else
		{
			return TucsonLineType.NON_DATA;
		}
	}

	private enum TucsonLineType{
		
		HEADER,				// A metadata line in a 3 line block
		HEADER_LINE1,
		HEADER_LINE2,
		HEADER_LINE3,
		NON_DATA,			// Any non-data line, likely metdata or comment
		DATA,				// Contains some sort of ring width measurements
		RWL_DATA,			// Line with standard measurement series data (not chronology)
		RWL_DATA_PARTIAL,	// Standard measurement series data but not a full decadal block
		RWL_DATA_COMPLETE,  // Complete decadal block of measurements
		CRN_DATA,			// Line with some chronology data in it
		CRN_DATA_PARTIAL,	// Line with chronology data in it but not a full decadal block
		CRN_DATA_COMPLETE;  // Complete decadal block of chronology values
		
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"crn"};
	}
	
	@Override
	public TridasProject getProject() {
		
		try{
		TridasObject o = project.getObjects().get(0);
		TridasElement e = o.getElements().get(0);
		TridasSample s = e.getSamples().get(0);
		
		if(mseriesList.size()>0)
		{
			TridasRadius r = s.getRadiuses().get(0);
			r.setMeasurementSeries(mseriesList);
		}
		
		if(dseriesList.size()>0)
		{
			project.setDerivedSeries(dseriesList);
		}
		
		} catch (NullPointerException e){
			
		} catch (IndexOutOfBoundsException e2){
			
		}
		
		
		return project;
	}
	
	/**
	 * This checks to see if the file is a valid Tucson file and at the same
	 * time sets whether its an RWL or CRN style file. 
	 * 
	 * @param argFileString
	 * @return
	 * @throws InvalidDendroFileException
	 */
	public boolean isValidFile(String[] argFileString) throws InvalidDendroFileException
	{
		 /** @todo This function is computationally expensive as it does a complete regex
		 * parse of the file.  We could dramatically speed things up with more thorough
		 * regular expressions.
		 * */
		int index = 0;
		int crnLines = 0;
		int rwlLines = 0;
		int headerLines = 0;
		int otherLines = 0;
		
		for( ; index < argFileString.length; index++)
		{
			String line = argFileString[index];
			currentLineNumber = index+1;
			
			if(this.matchesLineType(TucsonLineType.CRN_DATA, line))
			{
				crnLines++;
			}
			
			if(this.matchesLineType(TucsonLineType.RWL_DATA, line))
			{
				rwlLines++;
			}
			
			if(this.matchesLineType(TucsonLineType.HEADER, line))
			{
				headerLines++;
			}
		
		}
		
		if(crnLines==0 && rwlLines==0)
		{
			log.debug("No data lines so file is invalid");
			return false;
		}
		
		if(crnLines==rwlLines)
		{
			log.debug("same number of crn and rwl lines so files is invalid");
			return false;
		}
				
		if(crnLines>rwlLines)
		{
			this.isChronology = true;
		}
		else
		{
			this.isChronology = false;
		}
		
		
		return true;
		
		
		
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
		return currentLineNumber;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tucson.about.description");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tucson.about.fullName");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tucson.about.shortName");
	}
}
