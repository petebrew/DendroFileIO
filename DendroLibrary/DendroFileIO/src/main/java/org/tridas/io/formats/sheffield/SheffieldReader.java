package org.tridas.io.formats.sheffield;

import java.util.ArrayList;
import java.util.UUID;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.formats.trims.TrimsReader;
import org.tridas.io.formats.trims.TrimsToTridasDefaults;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
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
import org.tridas.schema.TridasVariable;

/**
 * Reader for the file format produced by Ian Tyers' 
 * Dendro for Windows software.
 * 
 * @author peterbrewer
 *
 */
public class SheffieldReader extends AbstractDendroFileReader {
	private static final SimpleLogger log = new SimpleLogger(SheffieldReader.class);
	private TridasProject project = null;
	private SheffieldToTridasDefaults defaults = new SheffieldToTridasDefaults();
	private ArrayList<TridasMeasurementSeries> mseriesList = new ArrayList<TridasMeasurementSeries>();

	String sitename;
	int ringCount;
	String dateType;
	SafeIntYear startDate;
	String dataType;
	int sapwoodCount;
	int timberCount;
	String edgeCode;
	String chronologyType;
	String comment;
	String ukCoords;
	String latLong;
	String pithCode;
	String shapeCode;
	String majorDim;
	String minorDim;
	String innerRingCode;
	String outerRingCode;
	String phase;
	String shortTitle;
	String period;
	String taxon;
	String interpretationComment;
	String variableType;
	
	
	
	public SheffieldReader() {
		super(SheffieldToTridasDefaults.class);
	}
	
	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {

		ArrayList<TridasValue> ringWidthValues = new ArrayList<TridasValue>();
		
		// Check none of the header lines are empty
		for (int i=0; i<24; i++)
		{
			if(argFileString[i]=="" || argFileString[i]==null)
			{
				throw new InvalidDendroFileException(I18n.getText("sheffield.blankLine"), i+1);
			}
			
			
			// Line 1 - Site name/sample number
			if(i==0)
			{
				this.sitename = argFileString[i];
				if (sitename.length()>64)
				{
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.lineOneTooBig")));
				}
				if (SheffieldFile.containsSpecialChars(sitename))
				{
					sitename = SheffieldFile.stripSpecialChars(sitename);
					addWarningToList(new ConversionWarning(WarningType.NOT_STRICT, 
							I18n.getText("sheffield.specialCharWarning")));
				}		
			}
			
			// Line 2 - Number of rings
			if(i==1)
			{
				this.ringCount = Integer.valueOf(argFileString[i]);		
			}		
			
			// Line 3 - Date type 
			if(i==3)
			{
				this.dateType = argFileString[i].toUpperCase();
				if (!dateType.equals("A") && (!dateType.equals("R")))
				{
					dateType = "R";
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("sheffield.invalidDateType")));
				}
			}
			
			// Line 4 - Start date
			if(i==4)
			{
				try{
					startDate = new SafeIntYear(argFileString[i].trim());
				} catch (NumberFormatException e) { 
					addWarningToList(new ConversionWarning(WarningType.INVALID, 
							I18n.getText("fileio.invalidStartYear")));	
				}
			}
			
			// Line 18 - Short title 
			if (i==17)
			{
				this.shortTitle = argFileString[i];
			}

			
		}
		
		// Check none of the header lines are empty
		for (int i=23; i<argFileString.length; i++)
		{

			TridasValue v = new TridasValue();
			
			if(!argFileString[i].trim().equals("H") && 
			   !argFileString[i].trim().equals("R") &&
			   !argFileString[i].trim().equals("F"))
			{
			
			v.setValue(argFileString[i].trim());
			ringWidthValues.add(v);
			log.debug("value = "+String.valueOf(argFileString[i]));
			}
				
			
		}
		
		// Check ring count matches number of values in file
		if(ringCount!=ringWidthValues.size())
		{
			this.addWarningToList(new ConversionWarning(
					WarningType.INVALID, 
					I18n.getText("fileio.valueCountMismatch")));
			ringCount = ringWidthValues.size();	
		}
		
		// Now build up our measurementSeries
		
		TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();
		TridasUnit units = new TridasUnit();
		
		// Set units to 1/100th mm.  Is this always the case?
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		
		// Build identifier for series
		TridasIdentifier seriesId = new ObjectFactory().createTridasIdentifier();
		seriesId.setValue(shortTitle);
		seriesId.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		
		// Build interpretation group for series
		TridasInterpretation interp = new TridasInterpretation();
		//interp.setFirstYear(startYear.toTridasYear(DatingSuffix.AD));
		//interp.setLastYear(startYear.add(ringWidthValues.size()).toTridasYear(DatingSuffix.AD));

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
		//series.setInterpretation(interp);
		series.setTitle(shortTitle);
		series.setIdentifier(seriesId);
		series.setLastModifiedTimestamp(DateUtils.getTodaysDateTime() );
		//series.setDendrochronologist(userid);

		// Add series to our list
		mseriesList.add(series);


	}


	
	
	
	@Override
	public String[] getFileExtensions() {
		return new String[] {"d"};
		
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

	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("sheffield.about.description");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("sheffield.about.fullName");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("sheffield.about.shortName");
	}
}
