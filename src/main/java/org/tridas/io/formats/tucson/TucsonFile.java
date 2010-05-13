package org.tridas.io.formats.tucson;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults.TucsonField;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.io.util.YearRange;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasValue;

/**
 * The Tucson tree ring laboratory's file format. This has been the canonical
 * file format since the punchcard days. Unfortunately, since it's just the
 * punchcard format in a text file, it has numerous shortcomings:
 *
 * <ul>
 *
 * <li>minimal metadata support (only 80-character ASCII title, 6-digit ID, and
 * relative/absolute flag)
 *
 * <li>relative/absolute flag starts in column 80 of the title, making it
 * inconvenient to edit with most text editors
 *
 * <li>no way to store BC(E) samples; some labs add 8000 to the absolute year as
 * a hack to get around this, but unfortunately they've run into a Y10K problem
 * (it can't store 5-digit years, either)
 *
 * <li>no way to store auxiliary numerical data (like Weiserjahre, or
 * earlywood/latewood)
 *
 * <li>no way to distinguish with certainty if a summed file is indexed 
 *
 * <li>no MIME type (even an application/x- one), standard file extension, or
 * telltale opening signature
 *
 * </ul>
 *
 * <p>
 * What it does do well is store raw data, readable by every dendro program ever
 * written (yet another win for open standards). Unfortunately, they never
 * bothered to make an improved format after they stopped using punchcards, so
 * every other program also has a (better) native format. Thus we have a dozen
 * completely incompatible file formats today.
 * </p>
 *
 * <h3>File Format</h3>
 *
 * <p>
 * There are two variants of Tucson files: raw and processed. Processed files
 * hold data that is indexed, summed, or both.
 * </p>
 *
 * <p>
 * There appear to be subtle variations on each type, like whether to fill out
 * the last decade with extra 0's after the 999-terminator, that different
 * programs have introduced over the years. I think this class can read any type
 * of Tucson file correctly.
 * </p>
 *
 * <h3>Raw Samples</h3>
 *
 * <pre>
 * xxxxxx  yyyy
 * </pre>
 *
 * <table border="0">
 * <tr>
 * <th>letter</th>
 * <th>description</th>
 * </tr>
 * <tr>
 * <td>x</td>
 * <td>id number (6 digits)</td>
 * <tr>
 * <td>y</td>
 * <td>decade starting year (2 spaces + 4 digits)</td>
 * </table>
 *
 *
 * <h3>Processed Samples</h3>
 *
 * //TODO WRITE ME
 *
 *
 * <h3>Reference</h3>
 *
 * <p>
 * See NOAA's <a href="http://www.ngdc.noaa.gov/paleo/treeinfo.html">Tree Ring
 * Data Description</a>.  The <a href="http://www.cybis.se/wiki/index.php?title=Tucson_format"
 * >CDendro wiki</a> also contains useful information.
 * </p>
 * */
public class TucsonFile implements IDendroFile{

	/**
	 * Tucson doesn't support negative years (e.g. BC) so a standard
	 * workaround is to add 8000 to all years.  With this flag set to
	 * true 8000 is added to all year values in the file if any data
	 * is BC, otherwise it outputs with negative numbers which are 
	 * likely to make the output file invalid for use in other programs
	 */
	private Boolean useEightThousandYearOffsetBodge = true;
	
	/**
	 * Contains the defaults for the fields
	 */
	private TridasToTucsonDefaults defaults;
	private ArrayList<ITridasSeries> seriesList;
	
	private String extension;

	/**
	 * Total range of years for data in this file
	 */
	private YearRange range = null;
	
	private final IDendroCollectionWriter writer;
	
	public TucsonFile(IMetadataFieldSet argDefaults, IDendroCollectionWriter argWriter){
		this.defaults = (TridasToTucsonDefaults) argDefaults;
		seriesList = new ArrayList<ITridasSeries>();
		writer = argWriter;
	}
	
	/**
	 * Set the site code for this file.  Should strictly be <=6 chars long 
	 * but relaxed Tucson requirements allow 8 chars.  Any longer and it
	 * will be truncated.  Warnings issued if greater than 6 chars.
	 * 
	 * @param sc
	 * @throws ConversionWarningException
	 */
	public void setSiteCode(String sc){
		if (sc==null){
			return;
		}

		defaults.getStringDefaultValue(TucsonField.SITE_CODE).setValue(sc);
	}
	
	/**
	 * Set the name of this site.  Should be <=50 chars long, any longer and it
	 * will be truncated.
	 * 
	 * @param name
	 * @throws ConversionWarningException
	 */
	public void setSiteName(String name) throws ConversionWarningException{
		if(name == null){
			return;
		}
		defaults.getStringDefaultValue(TucsonField.SITE_NAME).setValue(name);
	}
	
	/**
	 * Set the ITRDB species code for this file.  Code should be 4 chars long.
	 * 
	 * TODO Implement ITRDBTaxonConverter class
	 * 
	 * @param code
	 * @throws ConversionWarningException
	 */
	public void setSpeciesCode(String code) throws ConversionWarningException{
		defaults.getStringDefaultValue(TucsonField.SPECIES_CODE).setValue(code);
	}
	
	/**
	 * Set the latin name for the species being investigated.  It should be no
	 * more than 8 chars long, otherwise it will be truncated.
	 * 
	 * @param name
	 * @throws ConversionWarningException
	 */
	public void setSpeciesName(String name) throws ConversionWarningException{
		defaults.getStringDefaultValue(TucsonField.SPECIES_NAME).setValue(name);
	}
	
	/**
	 * Set the elevation for this site in metres.  
	 * 
	 * @param el
	 * @throws ConversionWarningException
	 */
	public void setElevation(Integer el) throws ConversionWarningException{
		defaults.getIntegerDefaultValue(TucsonField.ELEVATION).setValue(el);
	}
	
	/**
	 * Set the name of the principle investigator for this data.  It should be
	 * no more than 61 chars long.  If it is longer, it will be truncated.
	 * 
	 * @param name
	 * @throws ConversionWarningException
	 */
	public void setInvestigator(String name) throws ConversionWarningException{
		defaults.getStringDefaultValue(TucsonField.INVESTIGATOR).setValue(name);
	}
	
	/**
	 * Set the state/country where this data was collected.  It should be 
	 * no more than 13 chars long.  If it is longer, it will be truncated.
	 * 
	 * @param name
	 * @throws ConversionWarningException
	 */
	public void setStateCountry(String name) throws ConversionWarningException{
		defaults.getStringDefaultValue(TucsonField.STATE_COUNTRY).setValue(name);  
	}
	
	/**
	 * Set the state/country where this data was collected.  It should be 
	 * no more than 13 chars long.  If it is longer, it will be truncated.
	 * 
	 * @param name
	 * @throws ConversionWarningException
	 */
	public void setCompDate(XMLGregorianCalendar date) throws ConversionWarningException{
		if(date!=null){
			
			
			Date dt = date.toGregorianCalendar().getTime();	
	        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	        String d = format.format(dt);
	        
			defaults.getStringDefaultValue(TucsonField.COMP_DATE).setValue(d);
		}
	}
	
	/**
	 * Set the lat long for this data.  Latitude and longitude should be decimal degrees and in
	 * standard WGS84 coordinate system.
	 * 
	 * @param latitude
	 * @param longitude
	 * @throws ConversionWarningException
	 */
	public void setLatLong(Double latitude, Double longitude) throws ConversionWarningException{
		
		if(latitude==null || longitude==null)
		{
			defaults.getStringDefaultValue(TucsonField.LATLONG).setValue("");
			throw new ConversionWarningException(new ConversionWarning(
					WarningType.NULL_VALUE,
					I18n.getText("tucson.latlong.notnull"),
					I18n.getText("tucson")+"."+I18n.getText("tucson.latlong"))
					);
		}
		if(latitude.compareTo(Double.valueOf("90"))>0 || latitude.compareTo(Double.valueOf("-90"))<0)
		{
			defaults.getStringDefaultValue(TucsonField.LATLONG).setValue("");
			throw new ConversionWarningException(new ConversionWarning(
					WarningType.INVALID,
					I18n.getText("tucson.latitude.invalid", String.valueOf(latitude)),
					I18n.getText("tucson")+"."+I18n.getText("tucson.latlong"))
					);
		}
		if(longitude.compareTo(Double.valueOf("180"))>0 || longitude.compareTo(Double.valueOf("-180"))<0)
		{
			defaults.getStringDefaultValue(TucsonField.LATLONG).setValue("");
			throw new ConversionWarningException(new ConversionWarning(
					WarningType.INVALID,
					I18n.getText("tucson.longitude.invalid", String.valueOf(longitude)),
					I18n.getText("tucson")+"."+I18n.getText("tucson.latlong"))
					);	
		}
	
		defaults.getStringDefaultValue(TucsonField.LATLONG).setValue(ddToDDMMString(latitude, LatLong.LATITUDE) + ddToDDMMString(longitude, LatLong.LONGITUDE));
	}
	
	public void addSeries(ITridasSeries series) throws ConversionWarningException {
		
		// Add this series to our list
		seriesList.add(series);

		// If list contains derivedseries then its a chronology and should have the
		// crn file extension
		if(series instanceof TridasDerivedSeries){
			extension = "crn";
		}else{
			extension = "rwl";
		}
				
		
		// Extract and Check the range and warn if there are problems
		// **********************************************************		
		YearRange rng = null;
		SafeIntYear firstYear = null;
		SafeIntYear lastYear = null;
		try{
			// Try to set range using first/last year info from interpretation section
			firstYear = new SafeIntYear(series.getInterpretation().getFirstYear());
			lastYear = new SafeIntYear(series.getInterpretation().getLastYear());
		
		} catch (NullPointerException e){
			// Otherwise set to 1001 relative year and use count of values
			if(firstYear==null) 
			{
				// First year is null so just use 1001 relative year and count of values
				firstYear = new SafeIntYear(1001);
				lastYear = new SafeIntYear(1001+series.getValues().get(0).getValues().size());
			}
			else if (lastYear==null)
			{
				// We have firstYear but not last, so calculate last from count of values
				BigInteger intfirstyear = BigInteger.valueOf(Integer.parseInt(firstYear.toString()));
				BigInteger numofvalues = BigInteger.valueOf(series.getValues().get(0).getValues().size());
				BigInteger intlastyear = intfirstyear.add(numofvalues);
				lastYear = new SafeIntYear(intlastyear.intValue());
			}
		}
		
		rng = new YearRange(firstYear, lastYear); 		
		if (rng!=null) 
		{			
			if (range==null) range = rng;
			else range.union(rng);
			
			// Warn if using +8000 bodge and some years are > 2000AD whilst others are BC
			if (SafeIntYear.max(rng.getEnd(), new SafeIntYear(2000))==rng.getEnd() 
					&& SafeIntYear.min(rng.getStart(), new SafeIntYear(1))==rng.getStart()
					&& this.useEightThousandYearOffsetBodge)
			{
				throw new ConversionWarningException(new ConversionWarning(
						WarningType.UNREPRESENTABLE,
						I18n.getText("tucson.range.8000BCand2000AD"),
						I18n.getText("tucson")+"."+I18n.getText("tucson.range"))
						);
			}
			
			// Warn sternly if years are before 8000BC
			if (SafeIntYear.min(rng.getStart(), new SafeIntYear(-8001))==rng.getStart())
			{
				throw new ConversionWarningException(new ConversionWarning(
						WarningType.UNREPRESENTABLE,
						I18n.getText("tucson.range.8000BC"),
						I18n.getText("tucson")+"."+I18n.getText("tucson.range"))
						);	
			}
			
			// Warn if BC and using +8000 bodge
			if (SafeIntYear.min(rng.getStart(), new SafeIntYear(1))==rng.getStart() 
					&& this.useEightThousandYearOffsetBodge)
			{
				throw new ConversionWarningException(new ConversionWarning(
						WarningType.WORK_AROUND,
						I18n.getText("tucson.range.usingBodge"),
						I18n.getText("tucson")+"."+I18n.getText("tucson.range"))
						);	
			}
			
			// Warn if BC and not using +8000 bodge
			if (SafeIntYear.min(rng.getStart(), new SafeIntYear(1))==rng.getStart() 
					&& !this.useEightThousandYearOffsetBodge)
			{
				throw new ConversionWarningException(new ConversionWarning(WarningType.UNREPRESENTABLE,
						I18n.getText("tucson.range.noBodge"),
						I18n.getText("tucson")+"."+I18n.getText("tucson.range"))
						);
			}
			
		}
		
	}
	
	/**
	 * Tucson format is unable to represent years BC so the +8000 year workaround is used
	 * by many.  This makes years from 8000BC onwards positive.  It however has the side 
	 * effect of making years after 2000AD invalid as Tucson only allows four chars for each
	 * year representation.  
	 * 
	 * Set the workaround on or off using this function.
	 * 
	 * @param b
	 */
	public void setUsePlus8000WorkAround(Boolean b)
	{
		this.useEightThousandYearOffsetBodge = b;
	}
	
	
	@Override
	public String[] saveToString() {
		
		StringBuilder string = new StringBuilder();
		
		writeFileHeader(string);
		writeSeriesData(string);
		return string.toString().split("\n");
	}

	/**
	 * Get the range as a string 'XXXX XXXX'.  The Tucson format
	 * requires this field to be 9 chars long but this function 
	 * *may* return more.  I've tried (using +8000 fix) to handle
	 * this, but what can you do if years are before 8000BC?  
	 * 
	 * @return
	 */
	private String getRangeAsString(){
				
		// Range is null so just return spaces
		if (range==null) 
		{
			return StringUtils.getSpaces(9);
			}
		
		// If years are BC (negative) then add 8000
		// if 'bodge' flag is turned on
		if ((Integer.parseInt(range.getStart().toString())<0 || 
			Integer.parseInt(range.getEnd().toString())<0) 
			&& useEightThousandYearOffsetBodge)
		{
			return String.valueOf((Integer.parseInt(range.getStart().toString())+8000)) + " " +
				   String.valueOf((Integer.parseInt(range.getEnd().toString())+8000));
			
		}
		
		// return range
		return range.getStart() + " " + range.getEnd();
	}
	
	
	/**
	 * Writes the header for this file
	 * 
	 * @param string
	 */
	private void writeFileHeader(StringBuilder string)
	{
		// Write header info
		String siteCode = defaults.getStringDefaultValue(TucsonField.SITE_CODE).getStringValue();
		String siteName = defaults.getStringDefaultValue(TucsonField.SITE_NAME).getStringValue();
		String speciesCode = defaults.getStringDefaultValue(TucsonField.SPECIES_CODE).getStringValue();
		String stateCountry = defaults.getStringDefaultValue(TucsonField.STATE_COUNTRY).getStringValue();
		String speciesName = defaults.getStringDefaultValue(TucsonField.SPECIES_NAME).getStringValue();
		String elevation = defaults.getIntegerDefaultValue(TucsonField.ELEVATION).getStringValue();
		String latlong = defaults.getStringDefaultValue(TucsonField.LATLONG).getStringValue();
		String investigator = defaults.getStringDefaultValue(TucsonField.INVESTIGATOR).getStringValue();
		String compDate = defaults.getStringDefaultValue(TucsonField.COMP_DATE).getStringValue();
		
		string.append(siteCode + StringUtils.getSpaces(3) + siteName + speciesCode +"\n");
		string.append(siteCode + StringUtils.getSpaces(3) + stateCountry + speciesName + elevation + latlong + StringUtils.getSpaces(10) + getRangeAsString() + "\n");
		string.append(siteCode + StringUtils.getSpaces(3) + investigator + compDate + "\n");
	}
	
	/**
	 * Writes the data portion of this file
	 * 
	 * TODO Implement support and conversion of units
	 * 
	 * @param string
	 * @throws IOException
	 */
	private void writeSeriesData(StringBuilder string){
		// Default the series identifier to the same as the site code
		String code = defaults.getStringDefaultValue(TucsonField.SITE_CODE).getStringValue();
	
		// Loop through each series in our list
		for (ITridasSeries series : seriesList){
			// Extract all values from series
			List<TridasValue> data = series.getValues().get(0).getValues();
			
			
			try{
				// Try and get the unique identifier 
				code = StringUtils.rightPadWithTrim(series.getIdentifier().getValue().toString(), 8);
			   } catch(NullPointerException e){
			   try{
					// That failed, so try and get title instead
					code = StringUtils.rightPadWithTrim(series.getTitle().toString(), 8);
				   } catch (NullPointerException e2){
					// That also failed so try site code
					code = StringUtils.rightPadWithTrim(defaults.getStringDefaultValue(TucsonField.SITE_CODE).getStringValue(), 8);
				   }
			   }
		
			
			// Calculate start and end years
			SafeIntYear start;
			SafeIntYear end;
			try{
				start= new SafeIntYear(series.getInterpretation().getFirstYear());
			} catch (Exception e){
				start = new SafeIntYear("1001");
			}
			try{
				end = start.add(series.getValues().get(0).getValues().size());
			} catch (Exception e){
				end = start.add(0);
			}
			
			// if it's summed, we print spaces instead of [1]'s later
			boolean isSummed = false; //s.isSummed();
			boolean isProcessed = false; // s.isIndexed() || s.isSummed();
			
			// start year; processed files always start on the decade
			SafeIntYear y = start;
			if (isProcessed)
				y = y.add(-start.column());

			// Infinite loop until we reach end of data
			for (;;) {
		
				// Row header column
				if (y.column() == 0 || (y.equals(start) && !isProcessed))
					writeRowHeader(string, code, (isProcessed ? 4 : 6), y);
				
				// Reached end of data so print stop code
				if (y.compareTo(end) >= 0 || (isProcessed && y.compareTo(start) < 0)) {
					if (!isProcessed) {
						// "   999", and STOP
						string.append("   999");
						break;
					} else {
						// "9990   " or "9990  0"
						string.append(isSummed ? "9990  0" : "9990   ");
					}
				} 
				else 
				{ 	
					// Print data value, eith "%4d" / %6d
					string.append(StringUtils.leftPad(data.get(y.diff(start)).getValue().toString(), 
							(isProcessed ? 4 : 6)));
			
					// Include count of applicable: "%3d" (right-align)
					if (isSummed)
						string.append(StringUtils.leftPad(
								data.get(y.diff(start)).toString(), 
								3));
					else if (isProcessed) // which is really isIndexed
						string.append("   ");
				}
			
				// processed files end only after 9cols+eoln
				if (isProcessed && y.compareTo(end) > 0 && y.column() == 9)
					break;
			
				// eoln
				if (y.column() == 9)
					string.append("\n");
			
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
	 * 			BufferedWriter to write to
	 * @param code 
	 * 			Series code
	 * @param colWidth
	 * 			How many characters should the code be?
	 * @param y
	 * 			The year we're at
	 * @throws IOException
	 */
	private void writeRowHeader(StringBuilder string, String code,
			int colWidth, SafeIntYear y) {
		String prefix; // don't print the decade for the first one
		if (y.compareTo(range.getStart()) < 0)
			prefix = range.getStart().toString();
		else
			prefix = y.toString();
		while (prefix.length() < colWidth)
			prefix = " " + prefix;
		string.append(code + prefix);
	}


	enum LatLong{
		LATITUDE,
		LONGITUDE,
	}
	
	/**
	 * Translate a decimal latitude or longitude value to the degrees and
	 * minutes notation required by the Tucson format.
	 * 
	 * @param value
	 * 			Decimal degree value
	 * @param type
	 * 			Is this a latitude or longitude?
	 * @return
	 */
	private String ddToDDMMString(double value, LatLong type)
	{
		double sign = Math.signum(value);
		long dd = Math.round(((Math.floor(value) * sign)));
		long mm = Math.round(Math.floor(((value) - Math.floor(value)) * 60));
		
		String out;
		if(sign>=0) out = "+"; 
		else out = "-";
		
		if(type==LatLong.LONGITUDE)
		{
			if (String.valueOf(dd).length()==0)	return null;
			if (String.valueOf(dd).length()==1)	out +="00";
			if (String.valueOf(dd).length()==2)	out +="0";
		}
		else if (type==LatLong.LATITUDE)
		{
			if (String.valueOf(dd).length()==0)	return null;
			if (String.valueOf(dd).length()==1)	out +="0";
		}
		
		out +=String.valueOf(dd)+String.valueOf(mm);
		return out;
	}

	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		return extension;
	}

	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		return seriesList.toArray(new ITridasSeries[0]);
	}

	/**
	 * @see org.tridas.io.IDendroFile#getWriter()
	 */
	@Override
	public IDendroCollectionWriter getWriter() {
		return writer;
	}
}
