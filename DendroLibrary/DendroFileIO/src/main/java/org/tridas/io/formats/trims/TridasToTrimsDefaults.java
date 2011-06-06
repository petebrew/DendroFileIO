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
package org.tridas.io.formats.trims;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;

/**
 * Place to hold and change default fields for the TRIMS filetype
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TridasToTrimsDefaults extends TridasMetadataFieldSet {

	public enum TrimsField {
		MEASURING_DATE, AUTHOR, START_YEAR;
	}


	protected void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(TrimsField.MEASURING_DATE, new StringDefaultValue(
				DateUtils.getDateTimeTRIMSStyle(null)));
		setDefaultValue(TrimsField.AUTHOR, new StringDefaultValue("XX", 2, 2));
		setDefaultValue(TrimsField.START_YEAR, new IntegerDefaultValue(1001));
	}

	protected void populateFromTridasMeasurementSeries(TridasMeasurementSeries ms)
	{
		if(ms.isSetAnalyst())
		{
			getStringDefaultValue(TrimsField.AUTHOR).setValue(ms.getAnalyst());
		}
		else if (ms.isSetDendrochronologist())
		{
			getStringDefaultValue(TrimsField.AUTHOR).setValue(ms.getDendrochronologist());
		}

		if(ms.isSetMeasuringDate())
		{
			
			getStringDefaultValue(TrimsField.MEASURING_DATE)
					.setValue(DateUtils.getDateTimeTRIMSStyle(
							ms.getMeasuringDate().getValue().toGregorianCalendar().getTime()));
		}
		
		if(ms.isSetInterpretation())
		{
			if(ms.getInterpretation().isSetFirstYear())
			{
				SafeIntYear startyear = new SafeIntYear(ms.getInterpretation().getFirstYear());
				getIntegerDefaultValue(TrimsField.START_YEAR).setValue(Integer.parseInt(startyear.toString()));
				
			}
		}
		
	}



	protected void populateFromTridasDerivedSeries(TridasDerivedSeries ds) {

		if (ds.isSetAuthor()) {
			getStringDefaultValue(TrimsField.AUTHOR).setValue(
					StringUtils.parseInitials(ds.getAuthor()));
		}
		
		if(ds.isSetCreatedTimestamp())
		{
			
			getStringDefaultValue(TrimsField.MEASURING_DATE)
					.setValue(DateUtils.getDateTimeTRIMSStyle(
							ds.getCreatedTimestamp().getValue().toGregorianCalendar().getTime()));
		}

		if(ds.isSetInterpretation())
		{
			if(ds.getInterpretation().isSetFirstYear())
			{
				SafeIntYear startyear = new SafeIntYear(ds.getInterpretation().getFirstYear());
				getIntegerDefaultValue(TrimsField.START_YEAR).setValue(Integer.parseInt(startyear.toString()));
				
			}
		}
		
	}

}
