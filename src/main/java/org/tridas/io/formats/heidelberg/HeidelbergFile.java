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
package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.FHDataFormat;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class HeidelbergFile implements IDendroFile {
	private static final SimpleLogger log = new SimpleLogger(HeidelbergFile.class);
	
	public static final int DATA_CHARS_PER_NUMBER = 6;
	
	private ITridasSeries series = null;
	private TridasValues dataValues;
	private TridasToHeidelbergDefaults defaults;
	
	private boolean chrono;
	private Integer[] dataInts;
	private int valuesIndex;
	
	public HeidelbergFile(TridasToHeidelbergDefaults argDefaults) {
		defaults = argDefaults;
	}
	
	@Override
	public String getExtension() {
		return "fh";
	}
	
	public void setSeries(ITridasSeries argSeries, int argValuesIndex) {
		series = argSeries;
		valuesIndex = argValuesIndex;
		
		if (argSeries instanceof TridasDerivedSeries) {
			chrono = true;
		}
		else {
			chrono = false;
		}
		
		populateDefaults();
	}
	
	/**
	 * 
	 * @param vals
	 */
	public void setDataValues(TridasValues vals)
	{	
		dataValues = vals;
		extractData();
		verifyData();
	}
	
	private void extractData() {
		ArrayList<Integer> ints = new ArrayList<Integer>();
		

		for (TridasValue v : dataValues.getValues()) {
			
			try{
			ints.add(Integer.parseInt(v.getValue()));
			} catch (NumberFormatException e)
			{
				defaults.addConversionWarning(new ConversionWarning(WarningType.INVALID, I18n.getText(
						"fileio.invalidDataValue")));
			}
			if (chrono) {
				if(v.getCount()==null)
				{
					ints.add(0);
				}
				else
				{
					ints.add(v.getCount());
				}
			}
		}
		dataInts = ints.toArray(new Integer[0]);
	}
	
	private void verifyData() {
		int maximumLength = DATA_CHARS_PER_NUMBER;
		for (Integer i : dataInts) {
			String si = i + "";
			if (si.length() > maximumLength) {
				maximumLength = si.length();
			}
		}
		if (maximumLength > DATA_CHARS_PER_NUMBER) {
			log.warn(I18n.getText("heidelberg.numbersTooLarge", DATA_CHARS_PER_NUMBER + ""));
			defaults.addConversionWarning(new ConversionWarning(WarningType.WORK_AROUND, I18n.getText(
					"heidelberg.numbersTooLarge", String.valueOf(DATA_CHARS_PER_NUMBER))));
			reduceUnits();
			for (int i = 0; i < dataInts.length; i++) {
				for (int j = 0; j < maximumLength - DATA_CHARS_PER_NUMBER; j++) {
					dataInts[i] /= 10;
				}
			}
		}
	}
	
	private void reduceUnits() {
		log.debug(I18n.getText("heidelberg.reducingUnits"));
		StringDefaultValue sdv = defaults.getStringDefaultValue(DefaultFields.UNIT);
		
		if (sdv.getStringValue() == "") {
			log.error(I18n.getText("heidelberg.couldNotReduceUnits"));
			return;
		}
		if (sdv.getStringValue().equals("mm")) {
			log.error(I18n.getText("heidelberg.couldNotReduceUnits"));
			defaults.addConversionWarning(new ConversionWarning(WarningType.IGNORED, I18n
					.getText("heidelberg.couldNotReduceUnits")));
			sdv.setValue(null);
		}
		else if (sdv.getStringValue().equals("1/10 mm")) {
			sdv.setValue("mm");
		}
		else if (sdv.getStringValue().equals("1/100 mm")) {
			sdv.setValue("1/10 mm");
		}
		else if (sdv.getStringValue().equals("1/1000 mm")) {
			sdv.setValue("1/100 mm");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void populateDefaults() {
		
		GenericDefaultValue<FHDataFormat> dataFormatField = (GenericDefaultValue<FHDataFormat>)
					defaults.getDefaultValue(DefaultFields.DATA_FORMAT);		

		if (chrono) {
			dataFormatField.setValue(FHDataFormat.HalfChrono);
			/*String standardizationMethod = ((TridasDerivedSeries) series).getStandardizingMethod();
			if(standardizationMethod!=null)
			{
				defaults.getStringDefaultValue(DefaultFields.SERIES_TYPE).setValue(standardizationMethod);
			}*/
		}
		else {
			dataFormatField.setValue(FHDataFormat.Single);
		}

	}
	
	@Override
	public ITridasSeries[] getSeries() {
		return new ITridasSeries[]{series};
	}
	
	@Override
	public String[] saveToString() {
		ArrayList<String> file = new ArrayList<String>();
		file.add("HEADER:");
		
		addIfNotNull("Bark", DefaultFields.BARK, file);
		addIfNotNull("CoreNo", DefaultFields.CORE_NUMBER, file);
		addIfNotNull("Country", DefaultFields.COUNTRY, file);
		addIfNotNull("DataFormat", DefaultFields.DATA_FORMAT, file);
		addIfNotNull("DataType", DefaultFields.DATA_TYPE, file);
		addIfNotNull("DateBegin", DefaultFields.DATE_BEGIN, file);
		addIfNotNull("Dated", DefaultFields.DATED, file);
		addIfNotNull("DateEnd", DefaultFields.DATE_END, file);
		addIfNotNull("DateOfSampling", DefaultFields.DATE_OF_SAMPLING, file);
		addIfNotNull("District", DefaultFields.DISTRICT, file);
		addIfNotNull("Elevation", DefaultFields.ELEVATION, file);
		addIfNotNull("EstimatedTimePeriod", DefaultFields.ESTIMATED_TIME_PERIOD, file);
		addIfNotNull("FirstMeasurementDate", DefaultFields.FIRST_MEASUREMENT_DATE, file);
		addIfNotNull("HouseName", DefaultFields.HOUSE_NAME, file);
		addIfNotNull("HouseNumber", DefaultFields.HOUSE_NUMBER, file);	
		addIfNotNull("KeyCode", DefaultFields.KEYCODE, file);	
		addIfNotNull("LaboratoryCode", DefaultFields.LAB_CODE, file);
		addIfNotNull("LastRevisionDate", DefaultFields.LAST_REVISION_DATE, file);
		addIfNotNull("LastRevisionPersID", DefaultFields.LAST_REVISION_PERS_ID, file);
		addIfNotNull("Latitude", DefaultFields.LATITUDE, file);
		addIfNotNull("Length", DefaultFields.LENGTH, file);
		addIfNotNull("Location", DefaultFields.LOCATION, file);
		addIfNotNull("LocationCharacteristics", DefaultFields.LOCATION_CHARACTERISTICS, file);
		addIfNotNull("Longitude", DefaultFields.LONGITUDE, file);
		addIfNotNull("MissingRingsAfter", DefaultFields.MISSING_RINGS_AFTER, file);
		addIfNotNull("MissingRingsBefore", DefaultFields.MISSING_RINGS_BEFORE, file);
		addIfNotNull("PersID", DefaultFields.PERS_ID, file);
		addIfNotNull("Pith", DefaultFields.PITH, file);
		addIfNotNull("Project", DefaultFields.PROJECT, file);
		addIfNotNull("Province", DefaultFields.PROVINCE, file);
		addIfNotNull("RadiusNo", DefaultFields.RADIUS_NUMBER, file);
		addIfNotNull("SamplingHeight", DefaultFields.SAMPLING_HEIGHT, file);
		addIfNotNull("SamplingPoint", DefaultFields.SAMPLING_POINT, file);
		addIfNotNull("SapwoodRings", DefaultFields.SAPWOOD_RINGS, file);
		addIfNotNull("SeriesEnd", DefaultFields.SERIES_END, file);
		addIfNotNull("SeriesStart", DefaultFields.SERIES_START, file);
		addIfNotNull("SeriesType", DefaultFields.SERIES_TYPE, file);
		addIfNotNull("ShapeOfSample", DefaultFields.SHAPE_OF_SAMPLE, file);
		addIfNotNull("SiteCode", DefaultFields.SITE_CODE, file);
		addIfNotNull("SoilType", DefaultFields.SOIL_TYPE, file);
		addIfNotNull("Species", DefaultFields.SPECIES, file);
		addIfNotNull("SpeciesName", DefaultFields.SPECIES_NAME, file);
		addIfNotNull("State", DefaultFields.STATE, file);
		addIfNotNull("StemDiskNo", DefaultFields.STEM_DISK_NUMBER, file);
		addIfNotNull("Street", DefaultFields.STREET, file);
		addIfNotNull("TimberHeight", DefaultFields.TIMBER_HEIGHT, file);
		addIfNotNull("TimberWidth", DefaultFields.TIMBER_WIDTH, file);
		addIfNotNull("Town", DefaultFields.TOWN, file);
		addIfNotNull("TownZipCode", DefaultFields.TOWN_ZIP_CODE, file);
		addIfNotNull("TreeHeight", DefaultFields.TREE_HEIGHT, file);
		addIfNotNull("TreeNo", DefaultFields.TREE_NUMBER, file);
		addIfNotNull("Unit", DefaultFields.UNIT, file);
		addIfNotNull("WaldKante", DefaultFields.WALDKANTE, file);
				
		if (chrono) {
			file.add("DATA:HalfChrono");
		}
		else {
			file.add("DATA:Single");
		}
		
		int j = 0;
		int lines = (int) Math.ceil(dataInts.length * 1.0 / 10.0);
		for (int i = 0; i < lines; i++) {
			StringBuilder line = new StringBuilder();
			for (int k = 0; k < 10; k++) {
				if (j < dataInts.length) {
					line.append(StringUtils.leftPad(dataInts[j] + "", DATA_CHARS_PER_NUMBER));
				}
				else {
					line.append(StringUtils.leftPad("0", DATA_CHARS_PER_NUMBER));
				}
				j++;
			}
			file.add(line.toString());
		}
		return file.toArray(new String[0]);
	}
	
	private void addIfNotNull(String argKeyString, DefaultFields argEnum, ArrayList<String> argList) {
		if (defaults.getDefaultValue(argEnum).getStringValue().equals("")) {
			return;
		}
		argList.add(argKeyString + "=" + defaults.getDefaultValue(argEnum).getStringValue().replaceAll("\\n", "; "));
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
}
