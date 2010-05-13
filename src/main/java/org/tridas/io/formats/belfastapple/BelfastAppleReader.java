package org.tridas.io.formats.belfastapple;

import java.util.ArrayList;
import java.util.UUID;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class BelfastAppleReader extends AbstractDendroFileReader {
	
	private static final SimpleLogger log = new SimpleLogger(BelfastAppleReader.class);
	private TridasProject project = null;
	// defaults given by user
	private BelfastAppleToTridasDefaults defaults = new BelfastAppleToTridasDefaults();
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();
	String objectname;
	String samplename;
	
	public BelfastAppleReader() {
		super(BelfastAppleToTridasDefaults.class);
	}
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		// Extract 'metadata' ;-)
		objectname = argFileString[0].trim();
		samplename = argFileString[1].trim();
		
		// Extract data
		ArrayList<TridasValue> ringWidthValues = new ArrayList<TridasValue>();
		for(int i=2; i<argFileString.length-1; i++)
		{
			TridasValue v = new TridasValue();
			int val;
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
		
		// Get last line which contains comments
		String comments = argFileString[argFileString.length-1].trim();
		
		// Now build up our measurementSeries
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		TridasUnit units = new TridasUnit();
		
		// Set units to 1/100th mm.  Is this always the case?
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		
		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(UUID.randomUUID().toString());
		seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		
		// Add values to nested value(s) tags
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setValues(ringWidthValues);
		valuesGroup.setUnit(units);
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) defaults.getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
		valuesGroupList.add(valuesGroup);	
		
		// Add all the data to the series
		series.setValues(valuesGroupList);
		series.setIdentifier(seriesId);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime() );
		series.setComments(comments);

		// Add series to our list
		mseriesList.add(series);
		

	}

	@Override
	public String[] getFileExtensions() {
		return new String[] {"txt"};
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
	
	@Override
	public int getCurrentLineNumber() {
		// TODO keep track of this
		return 0;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("belfastapple.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("belfastapple.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("belfastapple.about.shortName");

	}



}
