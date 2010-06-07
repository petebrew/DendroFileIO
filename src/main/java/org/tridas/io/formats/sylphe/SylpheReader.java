package org.tridas.io.formats.sylphe;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.sylphe.SylpheToTridasDefaults.DefaultFields;
import org.tridas.io.formats.sylphe.TridasToSylpheDefaults.SylpheCambiumType;
import org.tridas.io.util.DateUtils;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.DateTime;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRemark;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

/**
 * @deprecated Use BesanconReader instead.
 * @author peterbrewer
 */
@Deprecated
public class SylpheReader extends AbstractDendroFileReader {
	private static final SimpleLogger log = new SimpleLogger(SylpheReader.class);
	private Integer currentLine = 0;
	
	private SylpheToTridasDefaults defaults = null;
	private ArrayList<SylpheMeasurementSeries> seriesList = new ArrayList<SylpheMeasurementSeries>();
	private DateTime fileLastUpdated;
	
	public SylpheReader() {
		super(SylpheToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		return currentLine;
	}
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		defaults = (SylpheToTridasDefaults) argDefaultFields;
		
		// Check the file is valid
		checkFile(argFileString);
		
		Integer startLineIndex = null;
		Integer startDataBlockIndex = null;
		Integer endLineIndex = null;
		int i = -1;
		
		for (String lineString : argFileString) {
			i++;
			lineString = lineString.trim();
			
			// Blank line so skip
			if (lineString.matches("\\s")) {
				continue;
			}
			
			// A single updated date for whole file
			if (lineString.trim().startsWith("date")) {
				String regex = "^[\\d]{2}/[\\d]{2}/([\\d]{4})";
				Pattern p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher m1 = p1.matcher(lineString.substring(7));
				if (m1.find()) {
					try {
						String sub = lineString.substring(13, 17);
						Integer day = Integer.parseInt(lineString.substring(7, 9));
						Integer month = Integer.parseInt(lineString.substring(10, 12));
						Integer year = Integer.parseInt(lineString.substring(13, 17));
						fileLastUpdated = DateUtils.getDateTime(day, month, year);
					} catch (Exception e2) {
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("sylphe.dateInvalid")));
					}
					
				}
			}
			
			// Is this the first line of data block?
			if (lineString.trim().startsWith(". ")) {
				if (startLineIndex != null || startDataBlockIndex != null || endLineIndex != null) {
					throw new InvalidDendroFileException(I18n.getText("sylphe.startLineOutOfSequence"), i);
				}
				else {
					startLineIndex = i;
					continue;
				}
			}
			
			// Is this the start of the values block?
			if (lineString.startsWith("VAL")) {
				if (startLineIndex == null || startDataBlockIndex != null || endLineIndex != null) {
					throw new InvalidDendroFileException(I18n.getText("sylphe.startDataBlockOutOfSequence"), i);
				}
				else {
					startDataBlockIndex = i;
					continue;
				}
			}
			
			// Is this the end of the series?
			if (startDataBlockIndex != null && startDataBlockIndex != null
					&& (lineString.contains(";") || lineString.contains(":"))) {
				endLineIndex = i;
			}
			
			// If we have start and end indices then we can extract our series
			if (startLineIndex != null && endLineIndex != null && startDataBlockIndex != null) {
				ArrayList<String> dataBlockLines = new ArrayList<String>();
				ArrayList<String> metadataBlockLines = new ArrayList<String>();
				
				// Grab metadata lines for this block
				for (int j = startLineIndex; j < startDataBlockIndex; j++) {
					metadataBlockLines.add(argFileString[j]);
				}
				
				// Grab data lines for this block
				for (int j = startDataBlockIndex + 1; j <= endLineIndex; j++) {
					dataBlockLines.add(argFileString[j]);
				}
				
				// Create SylpheMeasurementSeries to hold this data block
				SylpheMeasurementSeries currseries = new SylpheMeasurementSeries();
				currseries.dataBlock = dataBlockLines;
				currseries.metadataBlock = metadataBlockLines;
				
				// Add to our list of series
				seriesList.add(currseries);
				
				// Reset indices
				startLineIndex = null;
				endLineIndex = null;
				startDataBlockIndex = null;
			}
			
		}
		
		extractDataFromBlocks();
		extractMetadataFromBlocks();
		
	}
	
	private void extractDataFromBlocks() {
		for (SylpheMeasurementSeries series : seriesList) {
			for (String lineString : series.dataBlock) {
				String[] parts = lineString.split("\\s");
				
				for (String val : parts) {
					TridasValue tv = new TridasValue();
					
					if (val.equals(":")) {
						// End of file
						return;
					}
					else if (val.equals(";")) {
						// End of data block
						break;
					}
					else if (val.equals(",")) {
						tv.setValue("0");
						TridasRemark remark = new TridasRemark();
						remark.setValue(I18n.getText("sylphe.notMeasured"));
						ArrayList<TridasRemark> remarksList = new ArrayList<TridasRemark>();
						remarksList.add(remark);
						tv.setRemarks(remarksList);
						series.dataValues.add(tv);
					}
					try {
						Integer.parseInt(val);
						tv.setValue(val);
						series.dataValues.add(tv);
					} catch (NumberFormatException e) {
						log.debug("Ring width value is not a number! Value was '" + val + "'");
					}
				}
				
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void extractMetadataFromBlocks() {
		for (SylpheMeasurementSeries series : seriesList) {
			series.defaults = (SylpheToTridasDefaults) defaults.clone();
			
			if (fileLastUpdated != null) {
				series.defaults.getDateTimeDefaultValue(DefaultFields.DATE).setValue(fileLastUpdated);
			}
			
			// Loop through metadata lines extracting info
			for (String lineString : series.metadataBlock) {
				// Title
				if (lineString.startsWith(". ")) {
					series.defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(lineString.substring(2));
					continue;
				}
				
				// Split line using space delimiter
				String[] parts = lineString.trim().split("[\\s]+");
				if (parts.length == 0) {
					continue;
				}
				
				// Length of series
				if (parts[0].equals("LON")) {
					try {
						series.defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(
								Integer.parseInt(parts[1]));
					} catch (Exception e) {
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("sylphe.invalidRingCount")));
					}
				}
				
				// Length of series
				if (parts[0].equals("date")) {
					try {
						series.defaults.getStringDefaultValue(DefaultFields.DATE).setValue(parts[3]);
					} catch (Exception e) {

					}
				}
				
				// Species name
				else if (parts[0].equals("ESP")) {
					if (parts.length >= 2) {
						String value = parts[1];
						series.defaults.getStringDefaultValue(DefaultFields.SPECIES).setValue(value);
					}
				}
				
				// Pith presence
				else if (parts[0].equals("MOE")) {
					series.defaults.getBooleanDefaultValue(DefaultFields.PITH).setValue(true);
				}
				
				// Ring where sapwood begins
				else if (parts[0].equals("AUB")) {
					try {
						series.defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_START).setValue(
								Integer.parseInt(parts[1]));
					} catch (Exception e) {
						addWarning(new ConversionWarning(WarningType.INVALID, I18n
								.getText("sylphe.invalidSapwoodStart"), "AUB"));
					}
				}
				
				// Cambium presence and season if noted
				else if (parts[0].equals("CAM")) {
					GenericDefaultValue<SylpheCambiumType> cambium = (GenericDefaultValue<SylpheCambiumType>) series.defaults
							.getDefaultValue(DefaultFields.CAMBIUM);
					
					if (parts.length < 2) {
						cambium.setValue(SylpheCambiumType.CAMBIUM_PRESENT_SEASON_UNKOWN);
					}
					else {
						cambium.setValue(SylpheCambiumType.fromCode(parts[1]));
					}
					
					if (cambium.getValue() == null) {
						addWarning(new ConversionWarning(WarningType.INVALID, I18n
								.getText("sylphe.invalidCambiumField"), "CAM"));
					}
				}
				
				// Bark presence
				else if (parts[0].equals("ECO")) {
					series.defaults.getBooleanDefaultValue(DefaultFields.BARK).setValue(true);
				}
				
				// First Year
				else if (parts[0].equals("ORI")) {
					try {
						Integer intval = Integer.parseInt(parts[1]);
						series.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).setValue(intval);
					} catch (Exception e) {
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("sylphe.invalidStartYear"),
								"ORI"));
					}
				}
				
				// Last Year
				else if (parts[0].equals("TER")) {
					try {
						series.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).setValue(
								Integer.parseInt(parts[1]));
					} catch (Exception e) {
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("sylphe.invalidLastYear"),
								"TER"));
					}
				}
				
				// Position in a mean
				else if (parts[0].equals("POS")) {
					try {
						series.defaults.getIntegerDefaultValue(DefaultFields.POSITION_IN_MEAN).setValue(
								Integer.parseInt(parts[1]));
					} catch (Exception e) {
						addWarning(new ConversionWarning(WarningType.INVALID, I18n
								.getText("sylphe.invalidPositionInMean")));
					}
				}
				
			}
			
		}
		
	}
	
	private void checkFile(String[] argStrings) throws InvalidDendroFileException {
		log.debug("Checking file to see if it looks like a SYLPHE file");
		Boolean containsTitle = false;
		Boolean containsVals = false;
		
		for (String line : argStrings) {
			if (containsTitle == false) {
				if (line.startsWith(". ")) {
					containsTitle = true;
					continue;
				}
			}
			
			if (containsVals == false) {
				if (line.startsWith("VAL")) {
					containsVals = true;
					continue;
				}
			}
		}
		
		if (containsTitle == false) {
			throw new InvalidDendroFileException(I18n.getText("sylphe.noTitle"), 0);
		}
		
		if (containsVals == false) {
			throw new InvalidDendroFileException(I18n.getText("sylphe.noVals"), 0);
		}
		
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	@Override
	public String getDescription() {
		return I18n.getText("sylphe.about.description");
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"txt"};
	}
	
	@Override
	public String getFullName() {
		return I18n.getText("sylphe.about.fullName");
	}
	
	@Override
	public TridasProject getProject() {
		TridasProject project = defaults.getDefaultTridasProject();
		
		for (SylpheMeasurementSeries thisseries : seriesList) {
			TridasValues values = new TridasValues();
			TridasVariable variable = new TridasVariable();
			variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
			values.setVariable(variable);
			TridasUnit unit = new TridasUnit();
			unit.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
			values.setUnit(unit);
			values.setValues(thisseries.dataValues);
			
			ArrayList<TridasValues> valuesGroup = new ArrayList<TridasValues>();
			valuesGroup.add(values);
			
			TridasMeasurementSeries ser = thisseries.defaults.getDefaultMeasurementSeries();
			ser.setValues(valuesGroup);
			
			TridasRadius r = thisseries.defaults.getDefaultTridasRadius();
			TridasSample s = thisseries.defaults.getDefaultTridasSample();
			TridasElement e = thisseries.defaults.getDefaultTridasElement();
			TridasObject o = thisseries.defaults.getDefaultTridasObject();
			
			r.getMeasurementSeries().add(ser);
			s.getRadiuses().add(r);
			e.getSamples().add(s);
			o.getElements().add(e);
			project.getObjects().add(o);
		}
		
		return project;
	}
	
	@Override
	public String getShortName() {
		return I18n.getText("sylphe.about.shortName");
	}
	
	/**
	 * Class to store the measurement series data
	 * 
	 * @author peter
	 */
	private static class SylpheMeasurementSeries {
		public ArrayList<String> metadataBlock;
		public ArrayList<String> dataBlock;
		public SylpheToTridasDefaults defaults;
		public ArrayList<TridasValue> dataValues = new ArrayList<TridasValue>();
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		currentLine = 0;
		defaults = null;
		fileLastUpdated = null;
		seriesList.clear();
	}
	
}
