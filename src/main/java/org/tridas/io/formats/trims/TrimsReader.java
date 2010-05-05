package org.tridas.io.formats.trims;

import java.util.ArrayList;
import java.util.UUID;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.TridasVariableDefaultValue;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.catras.CatrasToTridasDefaults;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.InvalidDendroFileException;
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
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**
 * Reader for the TRIMS data format.  These were files popular in the 1980's and 1990's
 * and were typically produced by the Henson rotary stages.  The file is simply:
 * 
 * Line 1 = Technician id
 * Line 2 = Date of measurement
 * Line 3 = Beginning year
 * Line 4+ = Ring width integer beginning with space
 * 
 * @author peterbrewer
 *
 */
public class TrimsReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(TrimsReader.class);
	private TridasProject project = null;
	// defaults given by user
	private TrimsToTridasDefaults defaults = new TrimsToTridasDefaults();
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	
	public TrimsReader() {
		super(TrimsToTridasDefaults.class);
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		// Extract 'metadata' ;-)
		String userid = argFileString[0].trim();
		String createdTimestamp = argFileString[1].trim();
		SafeIntYear startYear = null;
		
		try{
			startYear = new SafeIntYear(argFileString[2].trim());
		} catch (NumberFormatException e) { }
		
		// Extract data
		ArrayList<TridasValue> ringWidthValues = new ArrayList<TridasValue>();
		for(int i=3; i<argFileString.length; i++)
		{
			TridasValue v = new TridasValue();
			
			if(argFileString[i].startsWith(" "))
			{	
				v.setValue(argFileString[i].trim());
				ringWidthValues.add(v);
				log.debug("value = "+String.valueOf(argFileString[i]));
			}
			else
			{
				throw new InvalidDendroFileException(I18n.getText("trims.expectingSpace"), i);
			}
		}
		
		// Now build up our measurementSeries
		
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		TridasUnit units = new TridasUnit();
		
		// Set units to 1/100th mm.  Is this always the case?
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		
		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(UUID.randomUUID().toString());
		seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		
		// Build interpretation group for series
		TridasInterpretation interp = new TridasInterpretation();
		interp.setFirstYear(startYear.toTridasYear(DatingSuffix.AD));
		interp.setLastYear(startYear.add(ringWidthValues.size()).toTridasYear(DatingSuffix.AD));

		// Add values to nested value(s) tags
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setValues(ringWidthValues);
		valuesGroup.setUnit(units);
		TridasVariableDefaultValue variable = (TridasVariableDefaultValue) defaults.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
		valuesGroupList.add(valuesGroup);	
		
		// Add all the data to the series
		series.setValues(valuesGroupList);
		series.setInterpretation(interp);
		series.setIdentifier(seriesId);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime() );
		series.setDendrochronologist(userid);

		// Add series to our list
		mseriesList.add(series);
		
		

	}

	@Override
	public String[] getFileExtensions() {
		return new String[] {"rw"};
	}

	@Override
	public TridasProject getProject() {
		TridasProject project = null;
		
		try{
			project = defaults.getProjectWithDefaults(true);
			TridasObject o = project.getObjects().get(0);
			TridasElement e = o.getElements().get(0);
			TridasSample s = e.getSamples().get(0);
			
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
	 * @see org.tridas.io.IDendroFileReader#getName()
	 */
	@Override
	public String getName() {
		return "Trims";
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
		// TODO Auto-generated method stub
		return 0;
	}

}
