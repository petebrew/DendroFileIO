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
package org.tridas.io.formats.tucsoncompact;

import java.util.ArrayList;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class TucsonCompactToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	
	public static enum DefaultFields {
		
		SERIES_TITLE,
		RING_COUNT,
		START_YEAR,
		DIVFACTOR;

	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.START_YEAR, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DIVFACTOR, new IntegerDefaultValue());

		
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		series.setTitle(getStringDefaultValue(DefaultFields.SERIES_TITLE).getValue());
		
		// Set first year
		if(getIntegerDefaultValue(DefaultFields.START_YEAR).getValue()!=null)
		{
			TridasInterpretation interp = new TridasInterpretation();
			SafeIntYear firstYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.START_YEAR).getValue());
			interp.setFirstYear(firstYear.toTridasYear(DatingSuffix.AD));
			series.setInterpretation(interp);		
		}
		
		
		return series;
		
		
	}
	
	public TridasValues getDefaultTridasValues(){
		TridasUnit units = new TridasUnit();
		
		if(getIntegerDefaultValue(DefaultFields.DIVFACTOR).getValue()!=null)
		{
			Integer divFactor = getIntegerDefaultValue(DefaultFields.DIVFACTOR).getValue();
			if(divFactor.compareTo(-4)<=0)
			{
				units.setNormalTridas(NormalTridasUnit.MICROMETRES);
			}
		}
		
		if(units.getNormalTridas()==null)
		{
			units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		}		
		
		TridasVariable variable = new TridasVariable();
		variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		
		TridasValues valuesGroup = new TridasValues();
		valuesGroup.setUnit(units);
		valuesGroup.setVariable(variable);
		
		
		return valuesGroup;
	}
}
