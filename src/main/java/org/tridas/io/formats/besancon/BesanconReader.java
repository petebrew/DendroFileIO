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
package org.tridas.io.formats.besancon;

import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.besancon.BesanconToTridasDefaults.BesanconCambiumType;
import org.tridas.io.formats.besancon.BesanconToTridasDefaults.DefaultFields;
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

public class BesanconReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(BesanconReader.class);
	// defaults given by user
	private BesanconToTridasDefaults defaults = null;
	private Integer currentLine = 0;
	
	private ArrayList<BesanconMeasurementSeries> seriesList = new ArrayList<BesanconMeasurementSeries>();
	private DateTime fileLastUpdated;
	
	public BesanconReader() {
		super(BesanconToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		defaults = (BesanconToTridasDefaults) argDefaultFields;
		
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
			
			// Is this the first line of meta block?
			if (lineString.trim().startsWith(". ")) {
				if (startLineIndex != null || startDataBlockIndex != null || endLineIndex != null) {
					throw new InvalidDendroFileException(I18n.getText("besancon.startLineOutOfSequence"), i);
				}
				else {
					startLineIndex = i;
					continue;
				}
			}
			
			// Is this the start of the values block?
			if (lineString.toUpperCase().startsWith("VAL")) {
				if (startLineIndex == null || startDataBlockIndex != null || endLineIndex != null) {
					throw new InvalidDendroFileException(I18n.getText("besancon.startDataBlockOutOfSequence"), i);
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
				
				// Create BesanconMeasurementSeries to hold this data block
				BesanconMeasurementSeries currseries = new BesanconMeasurementSeries();
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
		for (BesanconMeasurementSeries series : seriesList) {
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
						remark.setValue(I18n.getText("besancon.notMeasured"));
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
		for (BesanconMeasurementSeries series : seriesList) {
			series.defaults = (BesanconToTridasDefaults) defaults.clone();
			
			if (fileLastUpdated != null) {
				series.defaults.getDateTimeDefaultValue(DefaultFields.DATE).setValue(fileLastUpdated);
			}
			
			// Loop through metadata lines extracting info
			for (String lineString : series.metadataBlock) 
			{
				// Title - use the string up to the first space. Any chars after that
				// should be ignored
				if (lineString.startsWith(". ")) {
					// Trim off '. '
					lineString = lineString.substring(2);
					// Ignore chars after first space
					if (lineString.contains(" ")) {
						lineString = lineString.substring(0, lineString.indexOf(" "));
					}
					series.defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(lineString);
					continue;
				}
				
				// Split line using space delimiter
				String[] parts = lineString.trim().split("[\\s]+");
				
				// Loop through extract any key/values on this line
				for (int i = 0; i < parts.length; i = i + 2) {
					// Try and extract key and value
					String key = "";
					String value = "";
					try {
						key = parts[i].toUpperCase().trim();
					} catch (Exception e) {}
					try {
						value = parts[i + 1].trim();
					} catch (Exception e) {}
					
					// Length of series
					if (key.startsWith("LON")) {
						try {
							series.defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(
									Integer.parseInt(value));
						} catch (Exception e) {
							addWarning(new ConversionWarning(WarningType.INVALID, I18n
									.getText("besancon.invalidRingCount")));
						}
					}
					
					// Species name
					else if (key.startsWith("ESP")) {
						if (parts.length >= 2) {
							series.defaults.getStringDefaultValue(DefaultFields.SPECIES).setValue(value);
						}
					}
					
					// Pith presence
					else if (key.startsWith("MOE")) {
						series.defaults.getBooleanDefaultValue(DefaultFields.PITH).setValue(true);
					}
					
					// Ring where sapwood begins
					else if (key.startsWith("AUB")) {
						try {
							series.defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_START).setValue(
									Integer.parseInt(value));
						} catch (Exception e) {
							addWarning(new ConversionWarning(WarningType.INVALID, I18n
									.getText("besancon.invalidSapwoodStart"), "Aubier"));
						}
					}
					
					// Cambium presence and season if noted
					else if (key.startsWith("CAM")) {
						GenericDefaultValue<BesanconCambiumType> cambium = (GenericDefaultValue<BesanconCambiumType>) series.defaults
								.getDefaultValue(DefaultFields.CAMBIUM);
						
						if (value.equals("")) {
							cambium.setValue(BesanconCambiumType.CAMBIUM_PRESENT_SEASON_UNKOWN);
						}
						else {
							cambium.setValue(BesanconCambiumType.fromCode(value));
						}
						
						if (cambium.getValue() == null) {
							addWarning(new ConversionWarning(WarningType.INVALID, I18n
									.getText("besancon.invalidCambiumField"), "Cambium"));
						}
					}
					
					// Bark presence
					else if (key.startsWith("ECO")) {
						series.defaults.getBooleanDefaultValue(DefaultFields.BARK).setValue(true);
					}
					
					// First Year
					else if (key.startsWith("ORI")) {						
						try {
							Integer intval = Integer.parseInt(value);
							series.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).setValue(intval);
							series.defaults.getBooleanDefaultValue(DefaultFields.DATED).setValue(true);
						} catch (Exception e) {
							addWarning(new ConversionWarning(WarningType.INVALID, I18n
									.getText("besancon.invalidStartYear"), "Origine"));
						}
					}
					
					// This key is ignore and set by calculating ORI + number of data values
					// Last Year
					/*else if (key.startsWith("TER")) {
						try {
							series.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).setValue(
									Integer.parseInt(value));
						} catch (Exception e) {
							addWarning(new ConversionWarning(WarningType.INVALID, I18n
									.getText("besancon.invalidLastYear"), "Terme"));
						}
					}*/
					
					// Position in a mean
					else if (key.startsWith("POS")) {
						try {
							series.defaults.getIntegerDefaultValue(DefaultFields.POSITION_IN_MEAN).setValue(
									Integer.parseInt(parts[1].trim()));
						} catch (Exception e) {
							addWarning(new ConversionWarning(WarningType.INVALID, I18n
									.getText("besancon.invalidPositionInMean"), "Position"));
						}
					}
				}	
			}
			
			
			
			
		}
		
	}
	
	private void checkFile(String[] argStrings) throws InvalidDendroFileException {
		log.debug("Checking file to see if it looks like a Besançon file");
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
				if (line.toLowerCase().startsWith("val")) {
					containsVals = true;
					continue;
				}
			}
		}
		
		if (containsTitle == false) {
			throw new InvalidDendroFileException(I18n.getText("besancon.noTitle"));
		}
		
		if (containsVals == false) {
			throw new InvalidDendroFileException(I18n.getText("besancon.noVals"));
		}
		
	}
	
	@Override
	public TridasProject getProject() {
		TridasProject project = defaults.getDefaultTridasProject();
		
		for (BesanconMeasurementSeries thisseries : seriesList) {
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
			
			// Override last year with first year + number of values
			try {
				thisseries.defaults.getSafeIntYearDefaultValue(DefaultFields.LAST_YEAR).setValue(
						thisseries.defaults.getSafeIntYearDefaultValue(DefaultFields.FIRST_YEAR).getValue()
						.add(thisseries.dataValues.size()));
			} catch (Exception e) {	}
			
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
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"txt"};
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("besancon.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("besancon.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("besancon.about.shortName");
	}
	
	/**
	 * Class to store the measurement series data
	 * 
	 * @author peter
	 */
	private static class BesanconMeasurementSeries {
		public ArrayList<String> metadataBlock;
		public ArrayList<String> dataBlock;
		public BesanconToTridasDefaults defaults;
		public ArrayList<TridasValue> dataValues = new ArrayList<TridasValue>();
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		currentLine = -1;
		defaults = null;
		fileLastUpdated = null;
		seriesList.clear();
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"txt"};
		
		return new DendroFileFilter(exts, getShortName());

	}
}
