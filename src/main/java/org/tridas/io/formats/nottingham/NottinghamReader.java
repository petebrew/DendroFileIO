package org.tridas.io.formats.nottingham;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.nottingham.NottinghamToTridasDefaults;
import org.tridas.io.formats.nottingham.NottinghamToTridasDefaults.DefaultFields;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class NottinghamReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(NottinghamReader.class);
	private NottinghamToTridasDefaults defaults = null;
	private int currentLineNumber = -1;
	
	private ArrayList<TridasValue> dataVals = new ArrayList<TridasValue>();

	public NottinghamReader() {
		super(NottinghamToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}


	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("nottingham.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("nottingham.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("nottingham.about.shortName");
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"txt"};
	}
	
	@Override
	protected void resetReader() {
		
		dataVals.clear();

	}


	@Override
	public TridasProject getProject() {
		
		TridasProject project = defaults.getProjectWithDefaults(true);
		
		TridasMeasurementSeries ms = project.getObjects().get(0).getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
		TridasUnit units = new TridasUnit();
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		TridasVariable variable = new TridasVariable();
		variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		
		ArrayList<TridasValues> valuesList = new ArrayList<TridasValues>();
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setUnit(units);
		valuesGroup.setVariable(variable);
		valuesGroup.setValues(dataVals);
		valuesList.add(valuesGroup);
		
		ms.setValues(valuesList);

		return project;
		
	}


	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		log.debug("Parsing: " + argFileString);
		defaults = (NottinghamToTridasDefaults) argDefaultFields;
		
		checkFileIsValid(argFileString);
		
		// Series Title	
		defaults.getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(argFileString[0].substring(0,10).trim());
		
				
		// Copy each value into the data array
		for (int linenum=1; linenum<argFileString.length; linenum++)
		{
			String line = argFileString[linenum];
			
			for (int charpos=0; charpos+4<=line.length(); charpos = charpos+4)
			{
				TridasValue val = new TridasValue();
				String strval = line.substring(charpos, charpos+4).trim();
				val.setValue(strval);
				dataVals.add(val);
			}
			
		}	
		
		// Set ring count		
		if(dataVals!=null)
		{
			defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(dataVals.size());
		}
		
		// Check that the ring count matches what is in the header
		try{
			Integer ringcount = Integer.parseInt(argFileString[0].substring(10));
			if(ringcount!=dataVals.size())
			{
				addWarning(new ConversionWarning(WarningType.INVALID, 
						I18n.getText("nottingham.valuesAndRingCountMismatch")));
			}
		} catch (NumberFormatException e){}

	}

	
	/**
	 * Check that the file contains just one double on each line
	 * 
	 * @param argFileString
	 * @throws InvalidDendroFileException
	 */
	private void checkFileIsValid(String[] argFileString) throws InvalidDendroFileException
	{
		if(argFileString[0].length()<=10)
		{
			throw new InvalidDendroFileException(I18n.getText("nottingham.headerLineTooShort"));
		}
			
		for (int linenum=1; linenum<argFileString.length; linenum++)
		{
			String line = argFileString[linenum];

			if(line.length()!=40)
			{
				throw new InvalidDendroFileException(I18n.getText("nottingham.dataLineWrongLength"), linenum);
			}
			
			String regex = "^((\\d\\d\\d\\d)|( \\d\\d\\d)|(  \\d\\d)|(   \\d)){1,20}";
			Pattern p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m1 = p1.matcher(line);
			if (!m1.find()) {
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"));
			}
		}		
	}


}
