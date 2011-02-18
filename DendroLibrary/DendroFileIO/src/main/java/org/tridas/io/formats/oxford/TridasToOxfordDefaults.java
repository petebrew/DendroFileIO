/*******************************************************************************
 * Copyright 2011 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.tridas.io.formats.oxford;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.oxford.OxfordToTridasDefaults.OxDefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

public class TridasToOxfordDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {
	
	@Override
	protected void initDefaultValues() {

		setDefaultValue(OxDefaultFields.SERIESCODE, new StringDefaultValue(I18n.getText("unnamed.series"), 8, 8));
		setDefaultValue(OxDefaultFields.DESCRIPTION, new StringDefaultValue());
		setDefaultValue(OxDefaultFields.FIRSTYEAR, new IntegerDefaultValue(null, 1, getCurrentYear(), 4, 4));
		setDefaultValue(OxDefaultFields.LASTYEAR, new IntegerDefaultValue(null, 1, getCurrentYear(), 4, 4));
		setDefaultValue(OxDefaultFields.SERIESLENGTH, new IntegerDefaultValue(0));
		setDefaultValue(OxDefaultFields.STARTYEAR, new IntegerDefaultValue(1001, 1, getCurrentYear(), 4, 4));
		setDefaultValue(OxDefaultFields.COMMENTS, new StringDefaultValue());

	}

	/**
	 * Get current year as integer 
	 * 
	 * @return
	 */
	private static Integer getCurrentYear()
	{
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date date = new Date();
    
        return Integer.parseInt(dateFormat.format(date));
	}

	public void populateFromTridasProject(TridasProject argProject) {
		// TODO Auto-generated method stub
		
	}
	
	public void populateFromTridasObject(TridasObject argObject)
	{
		getStringDefaultValue(OxDefaultFields.DESCRIPTION).setValue(argObject.getTitle());

	}
	
	public void populateFromTridasElement(TridasElement argElement)
	{
		
	}
	
	public void populateFromTridasSample(TridasSample argSample)
	{
		
	}
	
	public void populateFromTridasRadius(TridasRadius argRadius)
	{
		
	}
	
	public void populateFromTridasSeries(ITridasSeries argSeries) throws ConversionWarningException
	{
		getStringDefaultValue(OxDefaultFields.SERIESCODE).setValue(argSeries.getTitle());
		
		if(argSeries.isSetInterpretation())
		{
			if(argSeries.getInterpretation().isSetFirstYear())
			{
				SafeIntYear firstYear = new SafeIntYear(argSeries.getInterpretation().getFirstYear());
				
				if(firstYear.compareTo(new SafeIntYear(1))<0)
				{
					throw new ConversionWarningException(new ConversionWarning(WarningType.UNREPRESENTABLE,
							I18n.getText("oxford.noBCDates")));
				}
				getIntegerDefaultValue(OxDefaultFields.FIRSTYEAR).setValue(Integer.parseInt(firstYear.toString()));
				getIntegerDefaultValue(OxDefaultFields.STARTYEAR).setValue(Integer.parseInt(firstYear.toString()));
			}
			if(argSeries.getInterpretation().isSetLastYear())
			{
				SafeIntYear lastYear = new SafeIntYear(argSeries.getInterpretation().getLastYear());

				if(lastYear.compareTo(new SafeIntYear(getCurrentYear()))>0)
				{
					throw new ConversionWarningException(new ConversionWarning(WarningType.UNREPRESENTABLE,
							I18n.getText("oxford.noFutureDates")));
				}
				getIntegerDefaultValue(OxDefaultFields.LASTYEAR).setValue(Integer.parseInt(lastYear.toString()));
			}
		}
		
		getStringDefaultValue(OxDefaultFields.COMMENTS).setValue(argSeries.getComments());

	}
}
