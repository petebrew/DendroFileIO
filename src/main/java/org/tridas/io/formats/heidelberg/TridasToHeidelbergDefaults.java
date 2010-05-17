package org.tridas.io.formats.heidelberg;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;

public class TridasToHeidelbergDefaults extends AbstractMetadataFieldSet implements IMetadataFieldSet {

	private static final SimpleLogger log = new SimpleLogger(TridasToHeidelbergDefaults.class);

	public enum HeidelbergField{
		KEY_CODE,
		DATA_FORMAT,
		SERIES_TYPE,
		LENGTH,
		DATEBEGIN,
		DATEEND,
		DATED,
		SPECIES,
		UNIT,
		PROJECT
	}
	
	@Override
	protected void initDefaultValues() {
		setDefaultValue(HeidelbergField.KEY_CODE, new StringDefaultValue("Unknown"));
		setDefaultValue(HeidelbergField.DATA_FORMAT, new StringDefaultValue("Tree"));
		setDefaultValue(HeidelbergField.SERIES_TYPE, new StringDefaultValue());
		setDefaultValue(HeidelbergField.LENGTH, new IntegerDefaultValue());
		setDefaultValue(HeidelbergField.DATEBEGIN, new IntegerDefaultValue());
		setDefaultValue(HeidelbergField.DATEEND, new IntegerDefaultValue());
		setDefaultValue(HeidelbergField.DATED, new StringDefaultValue());
		setDefaultValue(HeidelbergField.SPECIES, new StringDefaultValue());
		setDefaultValue(HeidelbergField.UNIT, new StringDefaultValue());
		setDefaultValue(HeidelbergField.PROJECT, new StringDefaultValue());
	}
	
	public void populateFromTridasProject(TridasProject argProject){
		getStringDefaultValue(HeidelbergField.PROJECT).setValue(argProject.getTitle());
	}
	
	public void populateFromTridasElement(TridasElement argElement){
		if(argElement.isSetTaxon()){
			getStringDefaultValue(HeidelbergField.SPECIES).setValue(argElement.getTaxon().getValue());
		}
	}
	
	public void populateFromTridasValues(TridasValues argValues){
		if(argValues.isSetUnitless() || !argValues.isSetUnit()){
			return;
		}
		TridasUnit units = argValues.getUnit();
		StringDefaultValue val = getStringDefaultValue(HeidelbergField.UNIT);
		switch(units.getNormalTridas()){
		case HUNDREDTH_MM:
			val.setValue("1/100 mm");
			break;
		case MICROMETRES:
			val.setValue("1/1000 mm");
			break;
		case MILLIMETRES:
			val.setValue("mm");
			break;
		case TENTH_MM:
			val.setValue("1/10 mm");
			break;
		default:
			addIgnoredWarning(HeidelbergField.UNIT, "Units not in range of Heidelberg unit range.");
		}
	}
	
	
	public void populateFromMS(TridasMeasurementSeries argSeries){
		populateFromSeries(argSeries);
	}
	
	
	public void populateFromDerivedSeries(TridasDerivedSeries argSeries){
		populateFromSeries(argSeries);
		
		if(argSeries.isSetStandardizingMethod()){
			getStringDefaultValue(HeidelbergField.SERIES_TYPE).setValue(argSeries.getStandardizingMethod());
		}
	}
	
	private void populateFromSeries(ITridasSeries argSeries){
		TridasIdentifier id = argSeries.getIdentifier();
		if(id.isSetValue()){
			getStringDefaultValue(HeidelbergField.KEY_CODE).setValue(id.getValue());
		}
		
		TridasInterpretation interp = argSeries.getInterpretation();
		if(interp != null){
			if(interp.isSetFirstYear()){
				getIntegerDefaultValue(HeidelbergField.DATEBEGIN).setValue(interp.getFirstYear().getValue().intValue());
			}
			if(interp.isSetLastYear()){
				getIntegerDefaultValue(HeidelbergField.DATEEND).setValue(interp.getLastYear().getValue().intValue());
			}
			else if(interp.isSetDeathYear()){
				getIntegerDefaultValue(HeidelbergField.DATEEND).setValue(interp.getDeathYear().getValue().intValue());
			}
		}
	}
}
