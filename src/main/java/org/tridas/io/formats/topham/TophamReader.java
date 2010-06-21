package org.tridas.io.formats.topham;

import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.catras.CatrasToTridasDefaults;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class TophamReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(TophamReader.class);
	private TophamToTridasDefaults defaults = null;
	private int currentLineNumber = -1;
	
	private ArrayList<TridasValue> dataVals = new ArrayList<TridasValue>();

	public TophamReader() {
		super(TophamToTridasDefaults.class);
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
		return I18n.getText("topham.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("topham.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("topham.about.shortName");
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
		units.setNormalTridas(NormalTridasUnit.MILLIMETRES);
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
		defaults = (TophamToTridasDefaults) argDefaultFields;
		
		checkFileIsValid(argFileString);
		
		// Copy each value into the data array
		for(String line : argFileString)
		{
			if (line.trim().equals("")) continue;
			TridasValue tval = new TridasValue();
			tval.setValue(line);
			
			dataVals.add(tval);
			
		}

	}

	
	/**
	 * Check that the file contains just one double on each line
	 * 
	 * @param argFileString
	 * @throws InvalidDendroFileException
	 */
	private void checkFileIsValid(String[] argFileString) throws InvalidDendroFileException
	{
		
		for(String line : argFileString)
		{
			if (line.trim().equals("")) continue;
			
			try{
				Double.parseDouble(line);
			} catch (NumberFormatException e){
				throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"));
			}
		}
		
	}


}
