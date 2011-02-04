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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger log = LoggerFactory.getLogger(HeidelbergFile.class);
	
	
	
	private ArrayList<HeidelbergSeries> seriesList = new ArrayList<HeidelbergSeries>();
	private TridasToHeidelbergDefaults defaults; 



	
	public HeidelbergFile(TridasToHeidelbergDefaults argDefaults) {
		defaults = argDefaults;
	}
	
	@Override
	public String getExtension() {
		return "fh";
	}
	
	public void addSeries(ITridasSeries argSeries, TridasValues argValues, 
			TridasToHeidelbergDefaults argDefaults) {
		
		Boolean chrono;
		if (argSeries instanceof TridasDerivedSeries) {
			chrono = true;
		}
		else {
			chrono = false;
		}
		
		HeidelbergSeries thisSeries = new HeidelbergSeries(argSeries, argValues, argDefaults, chrono);
		seriesList.add(thisSeries);

	}

	@Override
	public ITridasSeries[] getSeries() {
		
		ArrayList<ITridasSeries> series = new ArrayList<ITridasSeries>();
		for(HeidelbergSeries ser : seriesList)
		{
			series.add(ser.series);
		}
		
		ITridasSeries[] seriesArray;
		return seriesArray = (ITridasSeries[]) series.toArray();
	}
	
	@Override
	public String[] saveToString() {
		
		ArrayList<String> file = new ArrayList<String>();
		
		for (HeidelbergSeries ser : seriesList)
		{

			file.add("HEADER:");
			
			addIfNotNull(ser.defaults, "Bark", DefaultFields.BARK, file);
			addIfNotNull(ser.defaults, "CoreNo", DefaultFields.CORE_NUMBER, file);
			addIfNotNull(ser.defaults, "Country", DefaultFields.COUNTRY, file);
			addIfNotNull(ser.defaults, "DataFormat", DefaultFields.DATA_FORMAT, file);
			addIfNotNull(ser.defaults, "DataType", DefaultFields.DATA_TYPE, file);
			addIfNotNull(ser.defaults, "DateBegin", DefaultFields.DATE_BEGIN, file);
			addIfNotNull(ser.defaults, "Dated", DefaultFields.DATED, file);
			addIfNotNull(ser.defaults, "DateEnd", DefaultFields.DATE_END, file);
			addIfNotNull(ser.defaults, "DateOfSampling", DefaultFields.DATE_OF_SAMPLING, file);
			addIfNotNull(ser.defaults, "District", DefaultFields.DISTRICT, file);
			addIfNotNull(ser.defaults, "Elevation", DefaultFields.ELEVATION, file);
			addIfNotNull(ser.defaults, "EstimatedTimePeriod", DefaultFields.ESTIMATED_TIME_PERIOD, file);
			addIfNotNull(ser.defaults, "FirstMeasurementDate", DefaultFields.FIRST_MEASUREMENT_DATE, file);
			addIfNotNull(ser.defaults, "HouseName", DefaultFields.HOUSE_NAME, file);
			addIfNotNull(ser.defaults, "HouseNumber", DefaultFields.HOUSE_NUMBER, file);	
			addIfNotNull(ser.defaults, "KeyCode", DefaultFields.KEYCODE, file);	
			addIfNotNull(ser.defaults, "LaboratoryCode", DefaultFields.LAB_CODE, file);
			addIfNotNull(ser.defaults, "LastRevisionDate", DefaultFields.LAST_REVISION_DATE, file);
			addIfNotNull(ser.defaults, "LastRevisionPersID", DefaultFields.LAST_REVISION_PERS_ID, file);
			addIfNotNull(ser.defaults, "Latitude", DefaultFields.LATITUDE, file);
			addIfNotNull(ser.defaults, "Length", DefaultFields.LENGTH, file);
			addIfNotNull(ser.defaults, "Location", DefaultFields.LOCATION, file);
			addIfNotNull(ser.defaults, "LocationCharacteristics", DefaultFields.LOCATION_CHARACTERISTICS, file);
			addIfNotNull(ser.defaults, "Longitude", DefaultFields.LONGITUDE, file);
			addIfNotNull(ser.defaults, "MissingRingsAfter", DefaultFields.MISSING_RINGS_AFTER, file);
			addIfNotNull(ser.defaults, "MissingRingsBefore", DefaultFields.MISSING_RINGS_BEFORE, file);
			addIfNotNull(ser.defaults, "PersID", DefaultFields.PERS_ID, file);
			addIfNotNull(ser.defaults, "Pith", DefaultFields.PITH, file);
			addIfNotNull(ser.defaults, "Project", DefaultFields.PROJECT, file);
			addIfNotNull(ser.defaults, "Province", DefaultFields.PROVINCE, file);
			addIfNotNull(ser.defaults, "RadiusNo", DefaultFields.RADIUS_NUMBER, file);
			addIfNotNull(ser.defaults, "SamplingHeight", DefaultFields.SAMPLING_HEIGHT, file);
			addIfNotNull(ser.defaults, "SamplingPoint", DefaultFields.SAMPLING_POINT, file);
			addIfNotNull(ser.defaults, "SapwoodRings", DefaultFields.SAPWOOD_RINGS, file);
			addIfNotNull(ser.defaults, "SeriesEnd", DefaultFields.SERIES_END, file);
			addIfNotNull(ser.defaults, "SeriesStart", DefaultFields.SERIES_START, file);
			addIfNotNull(ser.defaults, "SeriesType", DefaultFields.SERIES_TYPE, file);
			addIfNotNull(ser.defaults, "ShapeOfSample", DefaultFields.SHAPE_OF_SAMPLE, file);
			addIfNotNull(ser.defaults, "SiteCode", DefaultFields.SITE_CODE, file);
			addIfNotNull(ser.defaults, "SoilType", DefaultFields.SOIL_TYPE, file);
			addIfNotNull(ser.defaults, "Species", DefaultFields.SPECIES, file);
			addIfNotNull(ser.defaults, "SpeciesName", DefaultFields.SPECIES_NAME, file);
			addIfNotNull(ser.defaults, "State", DefaultFields.STATE, file);
			addIfNotNull(ser.defaults, "StemDiskNo", DefaultFields.STEM_DISK_NUMBER, file);
			addIfNotNull(ser.defaults, "Street", DefaultFields.STREET, file);
			addIfNotNull(ser.defaults, "TimberHeight", DefaultFields.TIMBER_HEIGHT, file);
			addIfNotNull(ser.defaults, "TimberWidth", DefaultFields.TIMBER_WIDTH, file);
			addIfNotNull(ser.defaults, "Town", DefaultFields.TOWN, file);
			addIfNotNull(ser.defaults, "TownZipCode", DefaultFields.TOWN_ZIP_CODE, file);
			addIfNotNull(ser.defaults, "TreeHeight", DefaultFields.TREE_HEIGHT, file);
			addIfNotNull(ser.defaults, "TreeNo", DefaultFields.TREE_NUMBER, file);
			addIfNotNull(ser.defaults, "Unit", DefaultFields.UNIT, file);
			addIfNotNull(ser.defaults, "WaldKante", DefaultFields.WALDKANTE, file);
					
			if (ser.chrono) {
				file.add("DATA:HalfChrono");
			}
			else {
				file.add("DATA:Tree");
			}
			
			int j = 0;
			int lines = (int) Math.ceil(ser.dataInts.length * 1.0 / 10.0);
			for (int i = 0; i < lines; i++) {
				StringBuilder line = new StringBuilder();
				for (int k = 0; k < 10; k++) {
					if (j < ser.getDataInts().length) {
						line.append(StringUtils.leftPad(ser.getDataInts()[j] + "", ser.dataCharsPerNumber));
					}
					else {
						line.append(StringUtils.leftPad("0", ser.dataCharsPerNumber));
					}
					j++;
				}
				file.add(line.toString());
			}
		}
			
			
		return file.toArray(new String[0]);
	}
	
	private void addIfNotNull(TridasToHeidelbergDefaults defaults, String argKeyString, DefaultFields argEnum, ArrayList<String> argList) {
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
	
	
	public class HeidelbergSeries
	{
		public final ITridasSeries series;
		public final TridasValues dataValues;
		public final TridasToHeidelbergDefaults defaults; 
		public final boolean chrono;
		public final int dataCharsPerNumber;
		
		private Integer[] dataInts;
		
		public HeidelbergSeries(ITridasSeries series, TridasValues dataValues, 
				TridasToHeidelbergDefaults defaults, Boolean chrono)
		{
				this.series = series;
				this.dataValues = dataValues;
				this.defaults = defaults;
				this.chrono = chrono;
				dataCharsPerNumber = 5;
				
				extractData();
				verifyData(false);	
		}
		
		public HeidelbergSeries(ITridasSeries series, TridasValues dataValues, 
				TridasToHeidelbergDefaults defaults, Boolean chrono, Boolean isQuad)
		{
				this.series = series;
				this.dataValues = dataValues;
				this.defaults = defaults;
				this.chrono = chrono;
				if(isQuad) 
				{
					dataCharsPerNumber = 5;
				}
				else
				{
					dataCharsPerNumber = 6;
				}

				extractData();
				verifyData(isQuad);	
				populateDefaults();
		}
	
		public Integer[] getDataInts()
		{
			return dataInts;
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
		
		
		private void verifyData(Boolean isQuad) {
			
			
			int maximumLength = dataCharsPerNumber;
			for (Integer i : dataInts) {
				String si = i + "";
				if (si.length() > maximumLength) {
					maximumLength = si.length();
				}
			}
			if (maximumLength > dataCharsPerNumber) {
				log.warn(I18n.getText("heidelberg.numbersTooLarge", dataCharsPerNumber + ""));
				defaults.addConversionWarning(new ConversionWarning(WarningType.WORK_AROUND, I18n.getText(
						"heidelberg.numbersTooLarge", String.valueOf(dataCharsPerNumber))));
				reduceUnits();
				for (int i = 0; i < dataInts.length; i++) {
					for (int j = 0; j < maximumLength - dataCharsPerNumber; j++) {
						dataInts[i] /= 10;
					}
				}
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
				dataFormatField.setValue(FHDataFormat.Tree);
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
		
	}
	
}
