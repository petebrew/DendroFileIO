/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io.formats.tucson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.tucson.TucsonToTridasDefaults.TucsonDefaultField;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.YearRange;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.SeriesLinks;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.SeriesLink.IdRef;

/**
 * Reader for the Tucson file format.
 * 
 * @author peterbrewer
 */
public class TucsonReader extends AbstractDendroFileReader {
	
	private static final Logger log = LoggerFactory.getLogger(TucsonReader.class);	
	private ArrayList<TucsonSeries> seriesList = new ArrayList<TucsonSeries>();

	private TucsonToTridasDefaults defaults = null; // defaults given by user
	private Integer keycodeLen6 = 0; // lines which have 6 char keycodes
	private Integer keycodeLen8 = 0; // lines which have 8 char keycodes
	private Boolean warningAboutNegativeDates = false; // keep track of it we've
														// warned already
	private Boolean warningAboutNonStandardHeader = false; // keep track of it
															// we've warned
															// already
	private int currentLineNumber = 0; // the current line we're reading
	private Boolean isChronology = null; // is this file a chronology?
	private Integer numYearMarkerChars = 4; // no. of chars used in year markers
	private Boolean lastYearReached = false; // Did we reach a 'end of data'
												// marker?

	/**
	 * Officially Tucson format uses Astronomical Date format for all years.
	 * However many people working solely BC ignore this and use BC/AD calendar
	 * so we need to be able to turn Astronomical dating on and off.
	 */
	private Boolean usingAstronomicalDates = true;
	private Boolean isAstronomicalDatingOverriden = false;

	public TucsonReader() {
		super(TucsonToTridasDefaults.class);
	}

	/****************
	 * OVERRIDES
	 ****************/

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getCurrentLineNumber()
	 */
	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
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

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		// TODO make sure all vars are reset
		currentLineNumber = -1;
		defaults = null;
		seriesList.clear();
		numYearMarkerChars = 4;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[] { "crn", "rwl" };
	}

	private TridasProject getProject() {

		TridasProject project = defaults.getDefaultTridasProject();
		ArrayList<TridasObject> olist = new ArrayList<TridasObject>();
		ArrayList<TridasDerivedSeries> dslist = new ArrayList<TridasDerivedSeries>();

		for (TucsonSeries series : seriesList) {
			TridasObject o = series.defaults.getObjectWithDefaults();
			TridasElement e = series.defaults.getElementWithDefaults();
			TridasSample s = series.defaults.getSampleWithDefaults();

			if (series.countInts.size() > 0) {
				// Derived Series

				TridasDerivedSeries ds = series.defaults
						.getDerivedSeriesWithDefaults();
				ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
				TridasValues values = series.defaults.getDefaultTridasValues();
				ArrayList<TridasValue> valuesList = new ArrayList<TridasValue>();

				for (int i = 0; i < series.dataInts.size(); i++) {
					TridasValue value = new TridasValue();
					value.setValue(String.valueOf(series.dataInts.get(i)));
					value.setCount(series.countInts.get(i));

					valuesList.add(value);
				}
				values.setValues(valuesList);
				valuesGroupList.add(values);
				ds.setValues(valuesGroupList);

				// Set link series
				/*
				 * TridasRadiusPlaceholder rph= new TridasRadiusPlaceholder();
				 * TridasMeasurementSeriesPlaceholder msph =
				 * series.defaults.getDefaultTridasMeasurementSeriesPlaceholder
				 * (); rph.setMeasurementSeriesPlaceholder(msph);
				 * //s.setRadiusPlaceholder(rph);
				 */
				ArrayList<TridasSample> slist = new ArrayList<TridasSample>();
				slist.add(s);
				e.setSamples(slist);
				ArrayList<TridasElement> elist = new ArrayList<TridasElement>();
				elist.add(e);
				o.setElements(elist);
				olist.add(o);

				// Do Link to measurementSeriesPlaceholder
				SeriesLink link = new ObjectFactory().createSeriesLink();
				IdRef ref = new ObjectFactory().createSeriesLinkIdRef();
				ArrayList<SeriesLink> linkList = new ArrayList<SeriesLink>();
				ref.setRef(s);
				link.setIdRef(ref);
				linkList.add(link);
				SeriesLinks linkseries = new SeriesLinks();
				linkseries.setSeries(linkList);
				ds.setLinkSeries(linkseries);

				TridasInterpretation interp = new TridasInterpretation();
				interp.setFirstYear(series.firstYear
						.toTridasYear(DatingSuffix.AD));
				interp.setLastYear(series.firstYear.add(
						series.dataInts.size() - 1).toTridasYear(
						DatingSuffix.AD));
				ds.setInterpretation(interp);

				dslist.add(ds);
			} else {
				// Measurement Series

				TridasRadius r = series.defaults.getRadiusWithDefaults(false);
				TridasMeasurementSeries ms = series.defaults
						.getMeasurementSeriesWithDefaults();
				ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
				TridasValues values = series.defaults.getDefaultTridasValues();
				ArrayList<TridasValue> valuesList = new ArrayList<TridasValue>();

				for (Integer val : series.dataInts) {
					TridasValue value = new TridasValue();
					value.setValue(String.valueOf(val));
					valuesList.add(value);
				}

				values.setValues(valuesList);
				valuesGroupList.add(values);
				ms.setValues(valuesGroupList);

				TridasInterpretation interp = new TridasInterpretation();
				interp.setFirstYear(series.firstYear
						.toTridasYear(DatingSuffix.AD));
				interp.setLastYear(series.firstYear.add(series.dataInts.size()-1)
						.toTridasYear(DatingSuffix.AD));
				ms.setInterpretation(interp);

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

		}

		project.setObjects(olist);
		project.setDerivedSeries(dslist);
		return project;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#parseFile()
	 */
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {

		defaults = (TucsonToTridasDefaults) argDefaultFields;
		log.debug("starting tucson file parsing");

		SafeIntYear lastYearMarker = null; // Year marker from previous line
		String currentSeriesCode = ""; // Series code from previous line
		TucsonSeries currentSeries = null; // This holds all the info for the
											// current data block
		String headercache1 = null; // Strings to cache potential header lines
		String headercache2 = null; // Strings to cache potential header lines
		String headercache3 = null; // Strings to cache potential header lines
		Boolean withinChronologyBlock = false; // Whether we're in a chronology
												// block or not

		// Check that the file is valid
		checkValidFile(argFileString);

		// Loop through each line in file
		for (int index = 0; index < argFileString.length; index++) {

			// Increment line number
			currentLineNumber = index + 1;

			// Extract line from array
			String line = argFileString[index];

			// Skip blank lines
			if ((line == null) || (line.equals(""))) {
				continue;
			}

			// Handle line depending on type
			switch (getLineType(line)) {
			case HEADER_LINE1:
				if (withinChronologyBlock) {
					break;
				}
				headercache1 = line;
				headercache2 = null;
				headercache3 = null;
				break;

			case HEADER_LINE2:
				if (withinChronologyBlock) {
					break;
				}
				if (headercache1 != null && headercache2 == null) {
					// This is the second header line following the first so
					// as expected
					headercache2 = line;
					headercache3 = null;
				} else {
					// Header out of order so warn

					/*
					 * if (headercache1 != null) { addWarning(new
					 * ConversionWarning(WarningType.IGNORED, I18n
					 * .getText("tucson.nonstandardHeaderLine") + ": " +
					 * headercache1)); } if (headercache2 != null) {
					 * addWarning(new ConversionWarning(WarningType.IGNORED,
					 * I18n .getText("tucson.nonstandardHeaderLine") + ": " +
					 * headercache2)); } if (headercache3 != null) {
					 * addWarning(new ConversionWarning(WarningType.IGNORED,
					 * I18n .getText("tucson.nonstandardHeaderLine") + ": " +
					 * headercache3)); }
					 */

					// Reset header caches and continue
					headercache1 = null;
					headercache2 = null;
					headercache3 = null;
				}
				break;

			case HEADER_LINE3:
				if (withinChronologyBlock) {
					break;
				}
				if (headercache2 != null && headercache3 == null) {
					// Header line 3 after headers 1 and 2 so all as expected
					headercache3 = line;
				} else {
					// Header line out of order so warn

					/*
					 * if (headercache1 != null) { addWarning(new
					 * ConversionWarning(WarningType.IGNORED, I18n
					 * .getText("tucson.nonstandardHeaderLine") + ": " +
					 * headercache1)); } if (headercache2 != null) {
					 * addWarning(new ConversionWarning(WarningType.IGNORED,
					 * I18n .getText("tucson.nonstandardHeaderLine") + ": " +
					 * headercache2)); } if (headercache3 != null) {
					 * addWarning(new ConversionWarning(WarningType.IGNORED,
					 * I18n .getText("tucson.nonstandardHeaderLine") + ": " +
					 * headercache3)); }
					 */

					// Reset header cache
					headercache1 = null;
					headercache2 = null;
					headercache3 = null;
				}
				break;

			case CRN_DATA:
			case CRN_DATA_PARTIAL_6:
			case CRN_DATA_COMPLETE_6:
			case CRN_DATA_PARTIAL_8:
			case CRN_DATA_COMPLETE_8:
			case RWL_DATA:
			case RWL_DATA_PARTIAL_6:
			case RWL_DATA_COMPLETE_6:
			case RWL_DATA_PARTIAL_8:
			case RWL_DATA_COMPLETE_8:

				if (headercache1 != null && headercache2 != null
						&& headercache3 != null) {
					// NEW SERIES WITH A COMPLETE HEADER

					// Add current series to list if applicable
					if (currentSeries != null) {
						this.seriesList.add(currentSeries);
					}

					// Create new series
					currentSeries = new TucsonSeries(
							(TucsonToTridasDefaults) defaults.clone());

					// Extract metadata from header
					loadMetadata(currentSeries, headercache1, headercache2,
							headercache3);

					// Clear header, yearMarker and series code caches
					headercache1 = null;
					headercache2 = null;
					headercache3 = null;
					lastYearMarker = null;
					currentSeriesCode = null;
					lastYearReached = false;

					// Check year marker is valid
					checkYearMarkerIsValid(lastYearMarker,
							getYearMarkerFromLine(line));
					lastYearMarker = getYearMarkerFromLine(line);

					// Set the first year for this series
					currentSeries.firstYear = new SafeIntYear(String
							.valueOf(getYearMarkerFromLine(line)), true);

					// Store series code
					currentSeriesCode = getSeriesCodeFromLine(line);
					currentSeries.defaults.getStringDefaultValue(
							TucsonDefaultField.SERIES_CODE).setValue(
							currentSeriesCode);
				} else if (!currentSeriesCode
						.equals(getSeriesCodeFromLine(line))
						|| lastYearReached) {
					// NEW SERIES WITHOUT A HEADER

					warnAboutNonStandardHeader();

					// Reset 'end of data' and last Year markers flag
					lastYearReached = false;
					lastYearMarker = null;

					// Add current series to list if applicable
					if (currentSeries != null) {
						this.seriesList.add(currentSeries);
					}

					// Create new series
					currentSeries = new TucsonSeries(
							(TucsonToTridasDefaults) defaults.clone());

					// Check year marker is valid
					checkYearMarkerIsValid(lastYearMarker,
							getYearMarkerFromLine(line));
					lastYearMarker = getYearMarkerFromLine(line);

					// Set the first year for this series
					currentSeries.firstYear = new SafeIntYear(String
							.valueOf(getYearMarkerFromLine(line)), true);

					// Store series code
					currentSeriesCode = getSeriesCodeFromLine(line);
					currentSeries.defaults.getStringDefaultValue(
							TucsonDefaultField.SERIES_CODE).setValue(
							currentSeriesCode);

				}

				// Extract actual data listening for last year flag
				if (isChronology) {
					loadCRNDataFromDataLine(line, currentSeries);
				} else {
					loadRWLDataFromDataLine(line, currentSeries);
				}
				break;

			default:
				// Line is not a standard header or data line so warn
				addWarning(new ConversionWarning(WarningType.IGNORED, I18n
						.getText("tucson.nonstandardHeaderLine")
						+ ": " + line));
				break;
			}
		}

		// Add remaining series to list
		this.seriesList.add(currentSeries);
	}

	/**
	 * Called when a non-standard header line found so that the user can be
	 * warned. If called repeatedly the warning is only issued once.
	 * 
	 */
	private void warnAboutNonStandardHeader() {
		if (warningAboutNonStandardHeader == false) {
			warningAboutNonStandardHeader = true;
			addWarning(new ConversionWarning(WarningType.IGNORED, I18n
					.getText("tucson.nonstandardHeader")));

		}
	}

	/**
	 * Extract the series code from this data line. Validity of line should have
	 * been checked previously.
	 * 
	 * @param line
	 * @return
	 */
	private String getSeriesCodeFromLine(String line) {
		return line.substring(0, getKeycodeLength()).trim();
	}

	/**
	 * Extract the year marker from this data line. Validity of line should have
	 * been checked previously.
	 * 
	 * @param line
	 * @return
	 * @throws InvalidDendroFileException
	 */
	private SafeIntYear getYearMarkerFromLine(String line)
			throws InvalidDendroFileException {
		SafeIntYear yearMarker;
		try {
			String yearString = line.substring(getKeycodeLength(),
					getKeycodeLength() + numYearMarkerChars).trim();
			yearMarker = new SafeIntYear(yearString, usingAstronomicalDates);

			// Warn if years use negative numbers
			if (yearMarker.compareTo(new SafeIntYear("0", true)) <= 0) {
				negativeDateFound();
			}

			return yearMarker;

		} catch (NumberFormatException e) {
			throw new InvalidDendroFileException(I18n.getText(
					"tucson.invalidDecadeMarker", line.substring(0,
							numYearMarkerChars)), currentLineNumber);
		}
	}

	/**
	 * This checks to see if the file is a valid Tucson file and at the same
	 * time sets whether its an RWL or CRN style file.
	 * 
	 * @param argFileString
	 * @return
	 * @throws InvalidDendroFileException
	 */
	public void checkValidFile(String[] argFileString)
			throws InvalidDendroFileException {
		/**
		 * @todo This function is computationally expensive as it does a
		 *       complete regex parse of the file. We could dramatically speed
		 *       things up with more thorough regular expressions.
		 */
		int index = 0;
		int crnLines = 0;
		int rwlLines = 0;
		int headerLines = 0;

		for (; index < argFileString.length; index++) {
			String line = argFileString[index];
			currentLineNumber = index + 1;

			if (matchesLineType(TucsonLineType.CRN_DATA, line)) {
				crnLines++;
			}
			if (matchesLineType(TucsonLineType.RWL_DATA, line)) {
				rwlLines++;
			}
			if (matchesLineType(TucsonLineType.HEADER, line)) {
				headerLines++;
			}
		}

		if (crnLines == 0 && rwlLines == 0) {
			log.debug("No data lines so file is invalid");
			throw new InvalidDendroFileException(I18n.getText("fileio.noData"));
		}

		if (keycodeLen6 == 0 && keycodeLen8 == 0) {
			log
					.debug("No spaces at chars 15 or 18 in data lines.  File can't be valid");
			throw new InvalidDendroFileException(I18n
					.getText("tucson.unableToDetermineKeycodeSize"));
		}

		if (crnLines == rwlLines) {
			log.debug("same number of crn and rwl lines");
			throw new InvalidDendroFileException(I18n
					.getText("tucson.unableToDetermineCRNorRWL"));
		}

		if (crnLines > rwlLines) {
			isChronology = true;
		} else {
			isChronology = false;
		}
	}

	/**
	 * Attempts to load metadata from a three line header
	 * 
	 * @param line1
	 * @param line2
	 * @param line3
	 */
	protected void loadMetadata(TucsonSeries series, String line1,
			String line2, String line3) {

		series.defaults = (TucsonToTridasDefaults) defaults.clone();

		// First check whether the three lines are likely to be a header
		if (isLikelyHeader(line1, line2, line3)) {
			if (line1.length() > 64) {
				// Attempt to extract data from line 1
				series.defaults.getStringDefaultValue(
						TucsonDefaultField.SITE_CODE).setValue(
						(line1.substring(0, 6)).trim());
				series.defaults.getStringDefaultValue(
						TucsonDefaultField.SITE_NAME).setValue(
						(line1.substring(9, 61)).trim());
				series.defaults.getStringDefaultValue(
						TucsonDefaultField.SPECIES_CODE).setValue(
						(line1.substring(61, 65)).trim());
			}
			if (line2.length() > 75) {
				// Attempt to extract data from line 2
				series.defaults.getStringDefaultValue(
						TucsonDefaultField.STATE_COUNTRY).setValue(
						(line2.substring(9, 22)).trim());
				series.defaults.getStringDefaultValue(
						TucsonDefaultField.SPECIES_NAME).setValue(
						(line2.substring(22, 30)).trim());
				try {
					series.defaults.getDoubleDefaultValue(
							TucsonDefaultField.ELEVATION).setValue(
							Double
									.parseDouble((line2.substring(40, 45))
											.trim()));
				} catch (Exception e) {
					addWarning(new ConversionWarning(WarningType.IGNORED, I18n
							.getText("tucson.invalidElevation")));
				}
				series.defaults.getStringDefaultValue(
						TucsonDefaultField.LATLONG).setValue(
						(line2.substring(47, 57)).trim());
				try {
					series.defaults.getSafeIntYearDefaultValue(
							TucsonDefaultField.FIRST_YEAR).setValue(
							new SafeIntYear((line2.substring(67, 71)).trim()));
				} catch (Exception e) {
					addWarning(new ConversionWarning(WarningType.IGNORED, I18n
							.getText("tucson.invalidFirstYear")));
				}

			}
			if (line3.length() > 79) {
				// Attempt to extract data from line 3
				series.defaults.getStringDefaultValue(
						TucsonDefaultField.INVESTIGATOR).setValue(
						(line3.substring(9, 71)).trim());
				try {
					int day = Integer.parseInt(line3.substring(78, 80));
					int month = Integer.parseInt(line3.substring(75, 78));
					int year = Integer.parseInt(line3.substring(72, 76));
					series.defaults.getDateTimeDefaultValue(
							TucsonDefaultField.COMP_DATE).setValue(
							DateUtils.getDateTime(day, month, year));
				} catch (Exception e) {
					addWarning(new ConversionWarning(WarningType.IGNORED, I18n
							.getText("tucson.invalidCompDate")));
				}
			}

		} else {
			warnAboutNonStandardHeader();
		}

	}

	/**
	 * This replaces the radius (typically created using default values) in a
	 * project with a radiusPlaceholder.
	 * 
	 * @param project
	 * @return
	 */
	/*
	 * private TridasProject replaceRadiusWithPlaceholder(TridasProject project)
	 * { try { TridasObject o = project.getObjects().get(0); TridasElement e =
	 * o.getElements().get(0); TridasSample s = e.getSamples().get(0);
	 * s.setRadiuses(null);
	 * 
	 * TridasRadiusPlaceholder rph = new TridasRadiusPlaceholder();
	 * TridasMeasurementSeriesPlaceholder msph = new
	 * TridasMeasurementSeriesPlaceholder(); msph.setId("XREF-" +
	 * UUID.randomUUID().toString()); rph.setMeasurementSeriesPlaceholder(msph);
	 * 
	 * //s.setRadiusPlaceholder(rph); ArrayList<TridasSample> samplist = new
	 * ArrayList<TridasSample>(); samplist.add(s);
	 * 
	 * project.getObjects().get(0).getElements().get(0).setSamples(samplist);
	 * 
	 * } catch (NullPointerException e) {
	 * 
	 * } return project; }
	 */

	/**
	 * Attempts to read a line of standard RWL format data and add to the
	 * dataInts of the series. The validity of the line's format should have
	 * been checked previously using matchesLineType().
	 * 
	 * @param line
	 * @param series
	 * @return
	 * @throws InvalidDendroFileException
	 */
	@SuppressWarnings("unchecked")
	private void loadRWLDataFromDataLine(String line, TucsonSeries series)
			throws InvalidDendroFileException {

		ArrayList<Integer> dataValues = new ArrayList<Integer>();
		ArrayList<String> vals = new ArrayList<String>();

		// Remove keycode from line
		line = line.substring(getKeycodeLength());

		// Remove year marker from line
		line = line.substring(numYearMarkerChars);

		// Split values into string array. Limiting to 10 values (decade).
		for (int i = 0; i + 6 <= line.length(); i = i + 6) {
			if (!line.substring(i, i + 6).trim().equals("")) {
				vals.add(line.substring(i, i + 6).trim());
			}
		}

		GenericDefaultValue<NormalTridasUnit> unitField = (GenericDefaultValue<NormalTridasUnit>) series.defaults
				.getDefaultValue(TucsonDefaultField.UNITS);

		// Intercept no data values and stop markers
		for (String value : vals) {

			if (value.equals("999")) {
				// 0.01mm stop marker
				unitField.setValue(NormalTridasUnit.HUNDREDTH_MM);
				lastYearReached = true;
				break;
			} else if (value.equals("-9999")) {
				// 0.001mm stop marker
				unitField.setValue(NormalTridasUnit.MICROMETRES);
				lastYearReached = true;
				break;
			} else if (value.equals("-999")) {
				// Missing data value - override to zero
				dataValues.add(0);
				break;
			} else if (value.matches("[.]")) {
				// This is a non-standard placeholder used after the
				// stop marker to indicate where a value would go
				break;
			} else {
				// Standard numerical ring width value
				try {
					dataValues.add(Integer.parseInt(value));
				} catch (NumberFormatException e) {
					// addWarning(new ConversionWarning();
					throw new InvalidDendroFileException(I18n
							.getText("fileio.invalidDataValue"),
							currentLineNumber);
				}
			}

		}

		// Add these values to the series
		series.dataInts.addAll(dataValues);
	}

	/**
	 * Attempts to read a line of CRN style chronology data. The validity of the
	 * line's format should have been checked previously using
	 * matchesLineType().
	 * 
	 * @param line
	 */
	private void loadCRNDataFromDataLine(String line, TucsonSeries series)
			throws InvalidDendroFileException {

		ArrayList<Integer> dataValues = new ArrayList<Integer>();
		ArrayList<Integer> countValues = new ArrayList<Integer>();

		ArrayList<Integer> vals = new ArrayList<Integer>();
		ArrayList<Integer> counts = new ArrayList<Integer>();

		// Remove keycode from line
		line = line.substring(getKeycodeLength());

		// Remove year marker from line
		line = line.substring(numYearMarkerChars);

		// Calculate the limit of where we're going to read to
		Integer lineLenLimit;
		if (line.length() - 7 > 63) {
			// Line is longer than expected so there must be extra chars
			// at the end which we want to ignore.
			lineLenLimit = 63;
		} else {
			lineLenLimit = line.length() - 7;
		}

		Boolean seenDataValue = false;

		for (int i = 0; i <= lineLenLimit; i = i + 7) {
			Integer value = null;
			Integer count = null;

			try {
				value = Integer.valueOf(line.substring(i, i + 4).trim());
				count = Integer.valueOf(line.substring(i + 4, i + 7).trim());
			} catch (NumberFormatException e) {
				throw new InvalidDendroFileException(I18n
						.getText("fileio.invalidDataValue"),
						getCurrentLineNumber());
			}

			if (value.equals(Integer.valueOf("9990"))) {
				if (seenDataValue == false) {
					// Must be lead in no-data value
					series.firstYear.add(1);
					continue;
				} else {
					// Must be lead out no-data value
					series.lastYear.add(0 - (i + 1));
					break;
				}
			} else {
				// Normal value
				seenDataValue = true;
				vals.add(value);
				counts.add(count);
			}
		}

		// number of values and counts *must* be the same
		if (vals.size() != counts.size()) {
			throw new InvalidDendroFileException(I18n.getText(
					"fileio.countsAndValuesDontMatch", new String[] {
							String.valueOf(vals.size()),
							String.valueOf(counts.size()) }),
					getCurrentLineNumber());
		}

		// Add these values and counts to the series
		series.dataInts.addAll(vals);
		series.countInts.addAll(counts);

	}

	/**
	 * Returns the length of the keycode for this file. The standard sizes are
	 * either 6 or 8. However, the file may pinch the last character of the
	 * keycode to enable the storage of 5 character year markers. If so, the
	 * keycode is reduced by 1.
	 * 
	 * @return
	 */
	private Integer getKeycodeLength() {

		if (keycodeLen6 > keycodeLen8) {

			if (numYearMarkerChars == 4) {
				return 6;
			} else {
				return 5;
			}
		} else {
			if (numYearMarkerChars == 4) {
				return 8;
			} else {
				return 7;
			}
		}
	}

	/**
	 * Called when a negative date is found so that the user can be warned. If
	 * called repeatedly the warning is only issued once.
	 * 
	 */
	private void negativeDateFound() {
		if (warningAboutNegativeDates == false) {
			warningAboutNegativeDates = true;
			addWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n
					.getText("general.astronomicalWarning")));

		}
	}

	/**
	 * Checks whether three strings are likely to be the header portion of a
	 * standard tucson file. This only returns true if the three lines match the
	 * standard definition from the NOAA website
	 * 
	 * @param line1
	 * @param line2
	 * @param line3
	 * @return
	 */
	private boolean isLikelyHeader(String line1, String line2, String line3) {
		// Check lines match the headerline1, 2 and 3 format respectively
		if ((matchesLineType(TucsonLineType.HEADER_LINE1, line1))
				&& (matchesLineType(TucsonLineType.HEADER_LINE2, line2))
				&& (matchesLineType(TucsonLineType.HEADER_LINE3, line3))) {
			// Check the first 6 characters are all the same
			if ((line1.substring(0, 6).equals(line2.substring(0, 6)))
					&& (line2.substring(0, 6).equals(line3.substring(0, 6)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempt to determine the type of line this is through regexes. Note this
	 * is not perfect, especially for headers where there is little to
	 * distinguish them from random text
	 * 
	 * @param line
	 * @return
	 */
	protected TucsonLineType getLineType(String line) {
		if (matchesLineType(TucsonLineType.CRN_DATA_COMPLETE_8, line)) {
			return TucsonLineType.CRN_DATA_COMPLETE_8;
		} else if (matchesLineType(TucsonLineType.CRN_DATA_COMPLETE_6, line)) {
			return TucsonLineType.CRN_DATA_COMPLETE_6;
		} else if (matchesLineType(TucsonLineType.CRN_DATA_PARTIAL_8, line)) {
			return TucsonLineType.CRN_DATA_PARTIAL_8;
		} else if (matchesLineType(TucsonLineType.CRN_DATA_PARTIAL_6, line)) {
			return TucsonLineType.CRN_DATA_PARTIAL_6;
		}

		else if (matchesLineType(TucsonLineType.RWL_DATA_COMPLETE_8, line)) {
			return TucsonLineType.RWL_DATA_COMPLETE_8;
		} else if (matchesLineType(TucsonLineType.RWL_DATA_COMPLETE_6, line)) {
			return TucsonLineType.RWL_DATA_COMPLETE_6;
		} else if (matchesLineType(TucsonLineType.RWL_DATA_PARTIAL_8, line)) {
			return TucsonLineType.RWL_DATA_PARTIAL_8;
		} else if (matchesLineType(TucsonLineType.RWL_DATA_PARTIAL_6, line)) {
			return TucsonLineType.RWL_DATA_PARTIAL_6;
		} else if (matchesLineType(TucsonLineType.HEADER_LINE1, line)) {
			return TucsonLineType.HEADER_LINE1;
		} else if (matchesLineType(TucsonLineType.HEADER_LINE2, line)) {
			return TucsonLineType.HEADER_LINE2;
		} else if (matchesLineType(TucsonLineType.HEADER_LINE3, line)) {
			return TucsonLineType.HEADER_LINE3;
		} else {
			return TucsonLineType.NON_DATA;
		}
	}

	/**
	 * Check whether the current year marker is within a decade of the previous
	 * marker
	 * 
	 * @param lastYearMarker
	 * @param currentYearMarker
	 * @return
	 * @throws InvalidDendroFileException
	 */
	private void checkYearMarkerIsValid(SafeIntYear lastYearMarker,
			SafeIntYear currentYearMarker) throws InvalidDendroFileException {

		if (lastYearMarker == null) {
			// This is the first marker in the series so fine.
			return;
		} else {
			YearRange expectedRange = new YearRange(lastYearMarker.add(1),
					lastYearMarker.add(10));
			if (expectedRange.contains(currentYearMarker)) {
				return;
			} else {
				// This year marker is not within a decade of the last year
				// marker
				throw new InvalidDendroFileException(I18n.getText(
						"fileio.invalidDecadeMarker", currentYearMarker
								.toString(), lastYearMarker.toString()),
						getCurrentLineNumber());
			}
		}
	}

	/**
	 * Changes the reader to assume 5 characters for year markers. The first
	 * time this function is called a warning is added to the list. All
	 * subsequent times the request is ignored to ensure the user isn't flooded
	 * with repeat warnings.
	 */
	private void turnOnFiveCharYears() {
		if (numYearMarkerChars != 5) {
			addWarning(new ConversionWarning(WarningType.WORK_AROUND, I18n
					.getText("tucson.fiveCharYears")));
			numYearMarkerChars = 5;
		}
	}

	/**
	 * Check whether a line matches a specific line type. This is simple
	 * regexing so it isn't perfect, especially for headers. The HEADER_LINE3
	 * inparticular is very likely to give false positives.
	 * 
	 * @param type
	 * @param line
	 * @return
	 */
	private boolean matchesLineType(TucsonLineType type, String line) {
		String regex = null;
		Pattern p1;
		Matcher m1;

		// If line is empty or very short save ourselves the hassle and return
		// now
		if (line == null) {
			return false;
		}
		if (line.length() <= 6) {
			if (type.equals(TucsonLineType.NON_DATA)) {
				return true;
			} else {
				return false;
			}
		}

		String regexKeycode6 = "[\\w\\t -.]{6}";
		String regexKeycode8 = "[\\w\\t -.]{8}";
		String regexYear = "[\\t\\d -]{3}[\\d]{1}";
		String regexRWLVal = "[ -]{1}[\\t\\d- ]{4}[\\d]{1}";
		String regexCRNVal = "[\\d ]{4}((\\d\\d\\d)|( \\d\\d)|(  \\d))";

		switch (type) {

		// RWL DATA TYPES
		case RWL_DATA_COMPLETE_6:
			regex = "^" + regexKeycode6 + regexYear + "(" + regexRWLVal
					+ "){10}";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				keycodeLen6++;
				if (line.substring(5, 6).equals("-")) {
					turnOnFiveCharYears();
				}
				return true;
			}
			return false;
		case RWL_DATA_PARTIAL_6:
			regex = "^" + regexKeycode6 + regexYear + regexRWLVal;
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				if (!matchesLineType(TucsonLineType.RWL_DATA_COMPLETE_6, line)) {
					keycodeLen6++;
					if (line.substring(5, 6).equals("-")) {
						turnOnFiveCharYears();
					}
					return true;
				}
			}
			return false;
		case RWL_DATA_COMPLETE_8:
			regex = "^" + regexKeycode8 + regexYear + "(" + regexRWLVal
					+ "){10}";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				keycodeLen8++;
				if (line.substring(7, 8).equals("-")) {
					turnOnFiveCharYears();
				}
				return true;
			}
			return false;
		case RWL_DATA_PARTIAL_8:
			regex = "^" + regexKeycode8 + regexYear + regexRWLVal;
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				if (!matchesLineType(TucsonLineType.RWL_DATA_COMPLETE_8, line)) {
					keycodeLen8++;
					if (line.substring(7, 8).equals("-")) {
						turnOnFiveCharYears();
					}
					return true;
				}
			}
		case RWL_DATA_6:
			return matchesLineType(TucsonLineType.RWL_DATA_PARTIAL_6, line)
					|| matchesLineType(TucsonLineType.RWL_DATA_COMPLETE_6, line);
		case RWL_DATA_8:
			return matchesLineType(TucsonLineType.RWL_DATA_PARTIAL_8, line)
					|| matchesLineType(TucsonLineType.RWL_DATA_COMPLETE_8, line);
		case RWL_DATA:
			return matchesLineType(TucsonLineType.RWL_DATA_6, line)
					|| matchesLineType(TucsonLineType.RWL_DATA_8, line);

			// CRN DATA TYPES
		case CRN_DATA_COMPLETE_6:
			regex = "^" + regexKeycode6 + regexYear + "(" + regexCRNVal
					+ "){10}";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				keycodeLen6++;
				if (line.substring(5, 6).equals("-")) {
					turnOnFiveCharYears();
				}
				return true;
			}
			return false;
		case CRN_DATA_PARTIAL_6:
			// I don't think this should exist. NOAA webpage says all CRN data
			// lines
			// should contain 10 values
			return false;
		case CRN_DATA_COMPLETE_8:
			regex = "^" + regexKeycode8 + regexYear + "(" + regexCRNVal
					+ "){10}";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			if (m1.find()) {
				keycodeLen8++;
				if (line.substring(7, 8).equals("-")) {
					turnOnFiveCharYears();
				}
				return true;
			}
			return false;
		case CRN_DATA_PARTIAL_8:
			// I don't think this should exist. NOAA webpage says all CRN data
			// lines
			// should contain 10 values
			return false;
		case CRN_DATA_6:
			return matchesLineType(TucsonLineType.CRN_DATA_PARTIAL_6, line)
					|| matchesLineType(TucsonLineType.CRN_DATA_COMPLETE_6, line);
		case CRN_DATA_8:
			return matchesLineType(TucsonLineType.CRN_DATA_PARTIAL_8, line)
					|| matchesLineType(TucsonLineType.CRN_DATA_COMPLETE_8, line);
		case CRN_DATA:
			return matchesLineType(TucsonLineType.CRN_DATA_6, line)
					|| matchesLineType(TucsonLineType.CRN_DATA_8, line);

			// ANY DATA
		case DATA:
			if ((matchesLineType(TucsonLineType.RWL_DATA, line))
					|| (matchesLineType(TucsonLineType.CRN_DATA, line))) {
				return true;
			} else {
				return false;
			}

			// ANY NON-DATA
		case NON_DATA:
			if ((!matchesLineType(TucsonLineType.RWL_DATA, line))
					&& (!matchesLineType(TucsonLineType.CRN_DATA, line))) {
				return true;
			} else {
				return false;
			}

			// HEADER TYPES
		case HEADER_LINE1:
			regex = "^[^\\n]{9}[^\\n]{52}[A-Z]{4}";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			return m1.find();
		case HEADER_LINE2:
			regex = "^[^\\n]{9}[^\\n]{21}[\\t ]{10}[0-9mMft.]{5}[\\s]{2}[0-9\\t+\\- ]{10}[\\t ]{10}[\\d\\t ]{9}";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			return m1.find();
		case HEADER_LINE3:
			regex = "^[^\\n]{9}[\\w\\t\\. ,-]{63}[\\d\\t ]{8}";
			p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
			m1 = p1.matcher(line);
			return m1.find();
		case HEADER:
			if ((matchesLineType(TucsonLineType.HEADER_LINE1, line))
					|| (matchesLineType(TucsonLineType.HEADER_LINE2, line))
					|| (matchesLineType(TucsonLineType.HEADER_LINE3, line))) {
				return true;
			} else {
				return false;
			}

			// ANYTHING ELSE
		default:
			return false;
		}

	}

	/**
	 * Enum of the types of lines found in Tucson files. These types are
	 * hierarchical so some lines will often match with both specific and more
	 * general lines types
	 * 
	 * @author peterbrewer
	 * 
	 */
	private enum TucsonLineType {
		HEADER, // One of the standard header lines
		HEADER_LINE1, // Standard header line 1
		HEADER_LINE2, // Standard header line 2
		HEADER_LINE3, // Standard header line 3
		DATA, // Any type of ring width measurements
		RWL_DATA, // Any type of RWL measurement data
		RWL_DATA_6, // Any type of RWL data with 6 char keycode
		RWL_DATA_PARTIAL_6, // RWL measurement data but not a full decadal (6
							// char keycode)
		RWL_DATA_COMPLETE_6, // Complete decadal block of RWL measurements (6
								// char keycode)
		RWL_DATA_8, // Any type of RWL data line with 8 char keycode
		RWL_DATA_PARTIAL_8, // RWL measurement data but not a full decadal (8
							// char keycode)
		RWL_DATA_COMPLETE_8, // Complete decadal block of RWL measurements (8
								// char keycode)
		CRN_DATA, // Any type of CRN measurement data
		CRN_DATA_6, // Any type of CRN data with 6 char keycode
		CRN_DATA_PARTIAL_6, // CRN measurement data but not a full decadal (6
							// char keycode)
		CRN_DATA_COMPLETE_6, // Complete decadal block of CRN measurements (6
								// char keycode)
		CRN_DATA_8, // Any type of CRN data line with 8 char keycode
		CRN_DATA_PARTIAL_8, // CRN measurement data but not a full decadal (8
							// char keycode)
		CRN_DATA_COMPLETE_8, // Complete decadal block of CRN measurements (8
								// char keycode)
		NON_DATA; // Any non-data line, likely metadata or non-standard comment
	}

	/**
	 * Class to store the measurement series data
	 * 
	 * @author peterbrewer
	 */
	private static class TucsonSeries {
		public TucsonToTridasDefaults defaults;
		public final ArrayList<Integer> dataInts = new ArrayList<Integer>();
		public final ArrayList<Integer> countInts = new ArrayList<Integer>();
		public SafeIntYear firstYear = new SafeIntYear();
		public SafeIntYear lastYear = new SafeIntYear();

		private TucsonSeries(TucsonToTridasDefaults df) {
			defaults = df;
		}
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"tuc", "rwl", "dec", "crn", "txt"};
		
		return new DendroFileFilter(exts, getShortName());

	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		TridasProject projects[] = new TridasProject[1];
		projects[0] = this.getProject();
		return projects;
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

}
