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
package org.tridas.io.formats.csvmatrix;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValues;

public class TridasToMatrixDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {

	public static enum DefaultFields {
		
		SERIES_TITLE,
		VARIABLE,
		DATING_TYPE;

	}
	
	
	@Override
	protected void initDefaultValues() {

		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed")));
		setDefaultValue(DefaultFields.VARIABLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATING_TYPE, new StringDefaultValue());
	}

	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries argSeries) {
		
		populateFromTridasSeries(argSeries);
		
	}
	
	public void populateFromTridasDerivedSeries(TridasDerivedSeries argSeries)
	{
		populateFromTridasSeries(argSeries);
	}
	
	public void populateFromTridasSeries(ITridasSeries argSeries)
	{
		if(argSeries.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SERIES_TITLE).setValue(argSeries.getTitle());
		}
	}
	
	public void populateFromTridasValues(TridasValues values)
	{
		if(!values.isSetVariable()) return;
		
		if(values.getVariable().isSetNormalTridas())
		{
			getStringDefaultValue(DefaultFields.VARIABLE).setValue(values.getVariable().getNormalTridas().value().toString());
		}
		else if (values.getVariable().isSetNormal())
		{
			getStringDefaultValue(DefaultFields.VARIABLE).setValue(values.getVariable().getNormal().toString());
		}
		else if (values.getVariable().isSetValue())
		{
			getStringDefaultValue(DefaultFields.VARIABLE).setValue(values.getVariable().getValue());
		}
	}

	
}
