package org.tridas.io.formats.cracow;

import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.BooleanDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.catras.CatrasToTridasDefaults.DefaultFields;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;

public class CracowToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	public static enum DefaultFields {
		
		SAPWOOD_COUNT,
		RING_COUNT,
		KEYCODE,
		IS_CHRONO;
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SAPWOOD_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.KEYCODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.IS_CHRONO, new BooleanDefaultValue(false));
	}

	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
		
		// Set units to 1/100th mm. Is this always the case?
		TridasUnit units = new TridasUnit();
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		valuesGroup.setUnit(units);
		
		// Set variable to ringwidth.  Is this always the case?
		TridasVariable variable = new TridasVariable();
	
		variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);		
		valuesGroup.setVariable(variable);
		

		return valuesGroup;
	}
	
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		

		
		if(getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).getValue()!=null)
		{
			if(getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).getValue()>0)
			{
				TridasWoodCompleteness wc = new TridasWoodCompleteness();
				
				TridasSapwood sap = new TridasSapwood();
				sap.setPresence(ComplexPresenceAbsence.UNKNOWN);
				sap.setNrOfSapwoodRings(getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).getValue());
				wc.setSapwood(sap);
				
				TridasPith pith = new TridasPith();
				pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
				wc.setPith(pith);
				
				TridasHeartwood hw = new TridasHeartwood();
				hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
				wc.setHeartwood(hw);
				
				TridasBark bark = new TridasBark();
				bark.setPresence(PresenceAbsence.UNKNOWN);
				wc.setBark(bark);		
				
				if(getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()!=null)
				{
					if(getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()>0)
					{
						wc.setRingCount(getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue());
					}
				}
				
				series.setWoodCompleteness(wc);
			}
		}
		
		
		
		
		return series;
	}
}
