package org.tridas.io.formats.tucsonnew;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
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
 * <p>
 * What it does do well is store raw data, readable by every dendro program ever written
 * (yet another win for open standards). Unfortunately, they never bothered to make an
 * improved format after they stopped using punchcards, so every other program also has a
 * (better) native format. Thus we have a dozen completely incompatible file formats
 * today.
 * </p>
 * <h3>File Format</h3>
 * <p>
 * There are two variants of Tucson files: raw and processed. Processed files hold data
 * that is indexed, summed, or both.
 * </p>
 * <p>
 * There appear to be subtle variations on each type, like whether to fill out the last
 * decade with extra 0's after the 999-terminator, that different programs have introduced
 * over the years. I think this class can read any type of Tucson file correctly.
 * </p>
 * 
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
		
	private TridasToTucsonDefaults defaults; 	 //Contains the defaults for the fields
	private ArrayList<ITridasSeries> seriesList; // List of series represented by this file
	private String extension; 	                 // The file extension to use for this file
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
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String[] saveToString() {
		// TODO Auto-generated method stub
		return null;
	}
}
