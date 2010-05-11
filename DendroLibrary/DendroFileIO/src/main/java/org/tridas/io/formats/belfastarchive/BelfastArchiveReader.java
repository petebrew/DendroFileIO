package org.tridas.io.formats.belfastarchive;

import java.util.ArrayList;
import java.util.UUID;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.catras.CatrasToTridasDefaults;
import org.tridas.io.formats.trims.TrimsReader;
import org.tridas.io.formats.trims.TrimsToTridasDefaults;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class BelfastArchiveReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(BelfastArchiveReader.class);
	private TridasProject project = null;
	// defaults given by user
	private BelfastArchiveToTridasDefaults defaults = new BelfastArchiveToTridasDefaults();
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	String objectname;
	String samplename;
	SafeIntYear startYear;
	
	public BelfastArchiveReader() {
		super("belfastarchive", BelfastArchiveToTridasDefaults.class);
	}
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		
		// Extract 'metadata' ;-)
		objectname = argFileString[0].trim();
		samplename = argFileString[1].trim();
		
		// Extract data
		ArrayList<TridasValue> ringWidthValues = new ArrayList<TridasValue>();
		int footerStartInd = 0;
		for(int i=2; i<argFileString.length; i++)
		{
			TridasValue v = new TridasValue();
			int val;
			
			if(argFileString[i].contains("[[ARCHIVE"))
			{
				// Reached footer block
				footerStartInd = i;
				break;
			}
			
			try{
				val = Integer.valueOf(argFileString[i].trim());
			} catch (NumberFormatException e) 
			{ 
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
			}
			
			v.setValue(argFileString[i].trim());
			ringWidthValues.add(v);
			log.debug("value = "+String.valueOf(argFileString[i]));
		}
		
		// Extract metadata from footer

			// TODO - implement
			/*"[[ARCHIVE]]"       
			1277            <- Start year
			9177            <- ??
			.01            <- Resolution = hundredsmm
			1.035795        <- ??
			0.212144        <- ??
			IAN 21/01/96        <- User id and date
			TWYNING CHURCH #01    <- Title
			Pith F Sap 32        <- ??
			""            <- ??
			"[[ END OF TEXT ]]"*/
			
		// Line 1 - Start year
		try{
			startYear = new SafeIntYear(Integer.valueOf(argFileString[footerStartInd+1]));
		} catch (NumberFormatException e){}
		
		// Line 2 - ?
		
		// Line 3 - Resolution
		TridasUnit units = new TridasUnit();
		if(argFileString[footerStartInd+3].equals("0.01"))
		{
			// Set units to 1/100th mm. 
			units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		}
		else if (argFileString[footerStartInd+3].equals("0.001"))
		{
			// Set units to microns
			units.setNormalTridas(NormalTridasUnit.MICROMETRES);
		}
		else if (argFileString[footerStartInd+3].equals("0.1"))
		{
			// Set units to microns
			units.setNormalTridas(NormalTridasUnit.TENTH_MM);
		}		
		else
		{
			units = null;
		}
			
		// Lines 4,5,6 - ?
		
		// Line 7  - Series title
		series.setTitle(argFileString[footerStartInd+7]);
		
		// Lines 8,9 - ?
		
		
		
		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(UUID.randomUUID().toString());
		seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		
		// Add values to nested value(s) tags
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setValues(ringWidthValues);
		if(units!=null)
		{
			valuesGroup.setUnit(units);
		}
		else
		{
			valuesGroup.setUnitless(new TridasUnitless());
		}
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) defaults.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
		valuesGroupList.add(valuesGroup);	
		
		// Add all the data to the series
		series.setValues(valuesGroupList);
		series.setIdentifier(seriesId);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime() );

		// Add series to our list
		mseriesList.add(series);
		

	}

	@Override
	public String[] getFileExtensions() {
		return new String[] {"arx"};
	}

	@Override
	public TridasProject getProject() {
		TridasProject project = null;
		
		try{
			project = defaults.getProjectWithDefaults(true);
			TridasObject o = project.getObjects().get(0);
			
			// Override object name if found in file 
			if (this.objectname!=null)
			{
				project.getObjects().get(0).setTitle(objectname);
			}
			
			TridasElement e = o.getElements().get(0);
			TridasSample s = e.getSamples().get(0);
			
			// Override element name if found in file
			if (this.samplename!=null)
			{
				project.getObjects().get(0).getElements().get(0).getSamples().get(0).setTitle(samplename);
			}
			
			if(mseriesList.size()>0)
			{
				TridasRadius r = s.getRadiuses().get(0);
				r.setMeasurementSeries(mseriesList);
			}

			} catch (NullPointerException e){
				
			} catch (IndexOutOfBoundsException e2){
				
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
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getCurrentLineNumber()
	 */
	@Override
	public int getCurrentLineNumber() {
		// TODO track this
		return 0;
	}

}
