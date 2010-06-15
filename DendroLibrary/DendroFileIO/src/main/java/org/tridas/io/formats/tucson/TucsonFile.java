package org.tridas.io.formats.tucson;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults.TucsonField;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.io.util.YearRange;
import org.tridas.io.warningsandexceptions.ConversionWarning;
import org.tridas.io.warningsandexceptions.UnrepresentableTridasDataException;
import org.tridas.io.warningsandexceptions.ConversionWarning.WarningType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasValue;

/**
 * The Tucson tree ring laboratory's file format. This has been the canonical
 * file format since the punchcard days. Unfortunately, since it's just the
 * punchcard format in a text file, it has numerous shortcomings:
 * <ul>
 * <li>minimal metadata support just three 80 character lines in a rigid format that 
 * many ignore
 * <li>relative/absolute flag starts in column 80 of the title, making it inconvenient to
 * edit with most text editors
 * <li>officially uses astronomical dates rather than BC/AD so 0 = 1BC etc however most
 * people enter data as if it <b>does</b> use BC/AD.  This can cause confusion and data 
 * errors.
 * <li>only has space for 4 characters to represent years so officially only allows data from
 * 1000BC (-999) to 9999AD (9999).  People tend either to add 8000 to all years or pinch the
 * last character from the keycode to allow them to represent years with 5 characters. Neither
 * method is official or documented though.
 * <li>no way to store auxiliary numerical data like Weiserjahre
 * <li>no way to indicate the variable that has been measured so often people use filenames 
 * to indicate early/late wood files
 * <li>no way to distinguish with certainty if a summed file is indexed
 * <li>no MIME type (even an application/x- one), standard file extension, or telltale
 * opening signature
 * <li>variety of no data and stop characters used by different programs
 * <li>different number of characters used for keycode in different programs (6 or 8)
 * </ul>
 * 
 * <p>
 * What it does do is store dendro data, readable by most dendro programs. 
 * </p>
 * 
 * <h3>File Format</h3>
 * <p>
 * There are two variants of Tucson files: raw and chronologies. Raw files (RWL) contain
 * just ring width values, whereas chronologies also contain 'sample depth/count' values
 * alongwide each ring width.
 * </p>
 * <p>
 * Due to there being a minimal specification for the format, implementations over the years
 * have introduced many subtle variations which need to be handled.  For instance, the 
 * number of keycode characters, the number of characters for the year marker, whether there
 * should be any placeholder values after the 'end of data' marker, the units of measurement
 * etc etc. This class should handle all popular variants of the Tucon format correctly.
 * </p>
 * 
 * <p>
 * See NOAA's <a href="http://www.ngdc.noaa.gov/paleo/treeinfo.html">Tree Ring Data
 * Description</a>. The <a href="http://www.cybis.se/wiki/index.php?title=Tucson_format"
 * >CDendro wiki</a> also contains useful information.
 * </p>
 */
public class TucsonFile implements IDendroFile {
	
	/**
	 * Tucson only has space for 4 characters to represent years so
	 * is limited to -999 to 9999.  One method to work around this
	 * is to add 8000 to all years but this makes years difficult 
	 * to read.
	 */
	private Boolean useEightThousandYearOffsetBodge = false;
	
	/**
	 * The alternative work around is to pinch the last character from
	 * the keycode so that there are then 5 characters available to 
	 * represent years.  
	 */
	private Integer numYearMarkerChars = 4;
		
	private TridasToTucsonDefaults defaults; 	 // Contains the defaults for the fields
	private ArrayList<ITridasSeries> seriesList; // List of series represented by this file
	private YearRange range = null;              // Total range of years for data in this file
	
	
	public TucsonFile(IMetadataFieldSet argDefaults) {
		defaults = (TridasToTucsonDefaults) argDefaults;
		seriesList = new ArrayList<ITridasSeries>();
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		
		for (ITridasSeries series : seriesList)
		{
			if(series instanceof TridasDerivedSeries) return "crn";
		}
		return "rwl";
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		return seriesList.toArray(new ITridasSeries[0]);
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String[] saveToString() {
		
		StringBuilder string = new StringBuilder();
		
		writeFileHeader(string);
		writeSeriesData(string);
		return string.toString().split("\n");
	}
	
	
	
	/**
	 * Writes the data portion of this file
	 * TODO Implement support and conversion of units
	 * 
	 * @param string
	 * @throws IOException
	 */
	private void writeSeriesData(StringBuilder string) {
		// Default the series identifier to the same as the site code
		String code = defaults.getStringDefaultValue(TucsonField.SITE_CODE).getStringValue();
		
		// Loop through each series in our list
		for (ITridasSeries series : seriesList) {
			// Extract all values from series
			List<TridasValue> data = series.getValues().get(0).getValues();
			
			// if it's summed, we print spaces instead of [1]'s later
			boolean isSummed = false; // s.isSummed();
			boolean isChronology = false; // s.isIndexed() || s.isSummed();
			if (series instanceof TridasDerivedSeries){isChronology = true;}
			
			// Check if units are microns as we need to use a different EOF marker
			String EOFFlag = "999";
			try{
				if(series.getValues().get(0).getUnit().getNormalTridas().equals(NormalTridasUnit.MICROMETRES))
				{
					EOFFlag = "-9999";
				}
			} catch (Exception e){}
			
			// If its a chronology the EOF is different!
			if (isChronology)
			{
				EOFFlag = "9990";
			}
			
			try {
				// Try and get the unique identifier
				code = StringUtils.rightPadWithTrim(series.getIdentifier().getValue().toString(), 8);
			} catch (NullPointerException e) {
				try {
					// That failed, so try and get title instead
					code = StringUtils.rightPadWithTrim(series.getTitle().toString(), 8);
				} catch (NullPointerException e2) {
					// That also failed so try site code
					code = StringUtils.rightPadWithTrim(defaults.getStringDefaultValue(TucsonField.SITE_CODE)
							.getStringValue(), 8);
				}
			}
			
			// Calculate start and end years
			SafeIntYear start;
			SafeIntYear end;
			try {
				start = new SafeIntYear(series.getInterpretation().getFirstYear());
			} catch (Exception e) {
				start = new SafeIntYear("1001");
			}
			try {
				end = start.add(series.getValues().get(0).getValues().size());
			} catch (Exception e) {
				end = start.add(0);
			}
			

			
			// start year; processed files always start on the decade
			SafeIntYear y = start;
			if (isChronology) {
				y = y.add(-start.column());
			}
			
			// Infinite loop until we reach end of data
			for (;;) {
				
				// Row header column
				if (y.column() == 0 || (y.equals(start) && !isChronology)) {
					writeRowHeader(string, code, (isChronology ? 4 : 6), y);
				}
				
				// Reached end of data so print stop code
				if (y.compareTo(end) >= 0 || (isChronology && y.compareTo(start) < 0)) {
					if (!isChronology) {
						// "   999", and STOP
						string.append(StringUtils.leftPad(EOFFlag, 6));
						break;
					}
					else {
						// "9990   " or "9990  0"
						string.append(isSummed ? StringUtils.rightPad(EOFFlag, 6)+"0": StringUtils.rightPad(EOFFlag, 7));
					}
				}
				else {
					// Print data value, eith "%4d" / %6d
					string.append(StringUtils.leftPad(data.get(y.diff(start)).getValue().toString(), (isChronology
							? 4
							: 6)));
					
					// Include count if applicable: "%3d" (right-align)
					if (isSummed) {
						string.append(StringUtils.leftPad(data.get(y.diff(start)).toString(), 3));
					}
					else if (isChronology) {
						string.append("   ");
					}
				}
				
				// processed files end only after 9cols+eoln
				if (isChronology && y.compareTo(end) > 0 && y.column() == 9) {
					break;
				}
				
				// eoln
				if (y.column() == 9) {
					string.append("\n");
				}
				
				// increment year counter
				y = y.add(+1);
			}
			
			string.append("\n");
			
		}
	}
	
	/**
	 * Writes the row header
	 * 
	 * @param string
	 *            BufferedWriter to write to
	 * @param code
	 *            Series code
	 * @param colWidth
	 *            How many characters should the code be?
	 * @param y
	 *            The year we're at
	 * @throws IOException
	 */
	private void writeRowHeader(StringBuilder string, String code, int colWidth, SafeIntYear y) {
		String prefix; // don't print the decade for the first one
		if (y.compareTo(range.getStart()) < 0) {
			prefix = range.getStart().toAstronomicalYear().toString();
		}
		else {
			prefix = y.toAstronomicalYear().toString();
		}
		while (prefix.length() < colWidth) {
			prefix = " " + prefix;
		}
		string.append(code + prefix);
	}
	
	
	/**
	 * Writes the header for this file
	 * 
	 * @param string
	 */
	private void writeFileHeader(StringBuilder string) {
		// Write header info
		String siteCode = defaults.getStringDefaultValue(TucsonField.SITE_CODE).getStringValue();
		String siteName = defaults.getStringDefaultValue(TucsonField.SITE_NAME).getStringValue();
		String speciesCode = defaults.getStringDefaultValue(TucsonField.SPECIES_CODE).getStringValue();
		String stateCountry = defaults.getStringDefaultValue(TucsonField.STATE_COUNTRY).getStringValue();
		String speciesName = defaults.getStringDefaultValue(TucsonField.SPECIES_NAME).getStringValue();
		String elevation = defaults.getDoubleDefaultValue(TucsonField.ELEVATION).getStringValue();
		String latlong = defaults.getStringDefaultValue(TucsonField.LATLONG).getStringValue();
		String investigator = defaults.getStringDefaultValue(TucsonField.INVESTIGATOR).getStringValue();
		String compDate = defaults.getStringDefaultValue(TucsonField.COMP_DATE).getStringValue();
		
		string.append(siteCode + StringUtils.getSpaces(3) + siteName + speciesCode + "\n");
		string.append(siteCode + StringUtils.getSpaces(3) + stateCountry + speciesName + elevation + latlong
				+ StringUtils.getSpaces(10) + getRangeAsString() + "\n");
		string.append(siteCode + StringUtils.getSpaces(3) + investigator + compDate + "\n");
	}
	
	
	/**
	 * Get the range as a string 'XXXX XXXX'. The Tucson format
	 * requires this field to be 9 chars long but this function
	 * *may* return more. I've tried (using +8000 fix) to handle
	 * this, but what can you do if years are before 8000BC?
	 * 
	 * @return
	 */
	private String getRangeAsString() {
		
		// Range is null so just return spaces
		if (range == null) {
			return StringUtils.getSpaces(9);
		}
		
		// If years are BC (negative) then add 8000
		// if 'bodge' flag is turned on
		if ((Integer.parseInt(range.getStart().toString()) < 0 || Integer.parseInt(range.getEnd().toString()) < 0)
				&& useEightThousandYearOffsetBodge) {
			return String.valueOf((Integer.parseInt(range.getStart().toAstronomicalYear().toString()) + 8000)) + " "
					+ String.valueOf((Integer.parseInt(range.getEnd().toAstronomicalYear().toString()) + 8000));
			
		}
		
		// return range
		return range.getStart() + " " + range.getEnd();
	}
	
	/**
	 * Add a Tridas series to the list of series to be written to this file
	 * 
	 * @param series
	 */
	public void addSeries(ITridasSeries series) throws UnrepresentableTridasDataException{
		
		// Add this series to our list
		seriesList.add(series);
		
	
		// Extract and Check the range and warn if there are problems
		// **********************************************************
		YearRange rng = null;
		SafeIntYear firstYear = null;
		SafeIntYear lastYear = null;
		try {
			// Try to set range using first/last year info from interpretation section
			firstYear = new SafeIntYear(series.getInterpretation().getFirstYear());
			lastYear = new SafeIntYear(series.getInterpretation().getLastYear());
			
		} catch (NullPointerException e) {
			// Otherwise set to 1001 relative year and use count of values
			if (firstYear == null) {
				// First year is null so just use 1001 relative year and count of values
				firstYear = new SafeIntYear(1001);
				lastYear = new SafeIntYear(1001 + series.getValues().get(0).getValues().size());
			}
			else if (lastYear == null) {
				// We have firstYear but not last, so calculate last from count of values
				BigInteger intfirstyear = BigInteger.valueOf(Integer.parseInt(firstYear.toString()));
				BigInteger numofvalues = BigInteger.valueOf(series.getValues().get(0).getValues().size());
				BigInteger intlastyear = intfirstyear.add(numofvalues);
				lastYear = new SafeIntYear(intlastyear.intValue());
			}
		}
		
		rng = new YearRange(firstYear, lastYear);
		if (rng != null) {
			if (range == null) {
				range = rng;
			}
			else {
				range.union(rng);
			}
			
			/*
			// Warn if using +8000 bodge and some years are > 2000AD whilst others are BC
			if (SafeIntYear.max(rng.getEnd(), new SafeIntYear(2000)) == rng.getEnd()
					&& SafeIntYear.min(rng.getStart(), new SafeIntYear(1)) == rng.getStart()
					&& useEightThousandYearOffsetBodge) {
				defaults.addConversionWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, I18n
						.getText("tucson.range.8000BCand2000AD"), I18n.getText("tucson") + "."
						+ I18n.getText("tucson.range")));
			}*/
			
			// Throw error if years are before 1000BC
			if (SafeIntYear.min(rng.getStart(), new SafeIntYear(-1001)) == rng.getStart()) {
				throw new UnrepresentableTridasDataException(I18n.getText("tucson.before1000BC"));
			}
			
			/*
			// Warn if BC and using +8000 bodge
			if (SafeIntYear.min(rng.getStart(), new SafeIntYear(1)) == rng.getStart()
					&& useEightThousandYearOffsetBodge) {
				defaults.addConversionWarning(new ConversionWarning(WarningType.WORK_AROUND, I18n
						.getText("tucson.range.usingBodge"), I18n.getText("tucson") + "."
						+ I18n.getText("tucson.range")));
			}
			
			// Warn if BC and not using +8000 bodge
			if (SafeIntYear.min(rng.getStart(), new SafeIntYear(1)) == rng.getStart()
					&& !useEightThousandYearOffsetBodge) {
				defaults.addConversionWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, I18n
						.getText("tucson.range.noBodge"), I18n.getText("tucson") + "." + I18n.getText("tucson.range")));
			}*/
			
		}
		
	}
}
