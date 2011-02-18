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

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.Certainty;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;



public class OxfordToTridasDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet{
	
	public static enum OxDefaultFields {
		SERIESCODE,
		DESCRIPTION,
		FIRSTYEAR,
		LASTYEAR,
		SERIESLENGTH,
		STARTYEAR,
		COMMENTS;
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(OxDefaultFields.SERIESCODE, new StringDefaultValue(I18n.getText("unnamed.series"), 8, 8));
		setDefaultValue(OxDefaultFields.DESCRIPTION, new StringDefaultValue());
		setDefaultValue(OxDefaultFields.FIRSTYEAR, new SafeIntYearDefaultValue(new SafeIntYear(-999)));
		setDefaultValue(OxDefaultFields.LASTYEAR, new SafeIntYearDefaultValue(new SafeIntYear(-999)));
		setDefaultValue(OxDefaultFields.SERIESLENGTH, new IntegerDefaultValue(0));
		setDefaultValue(OxDefaultFields.STARTYEAR, new SafeIntYearDefaultValue(new SafeIntYear(-999)));
		setDefaultValue(OxDefaultFields.COMMENTS, new StringDefaultValue());

	}
	
	private ITridasSeries populateSeriesDetails(ITridasSeries ser)
	{
		ser.setTitle(getStringDefaultValue(OxDefaultFields.SERIESCODE).getValue().trim());
		
		// Combine comments from first and last lines of file
		String comments = "";
		if(getStringDefaultValue(OxDefaultFields.DESCRIPTION).getValue()!=null)
		{
			comments = getStringDefaultValue(OxDefaultFields.DESCRIPTION).getValue();
		}
		if(getStringDefaultValue(OxDefaultFields.COMMENTS).getValue()!=null)
		{
			if (!comments.equals("")) comments += "; ";
			comments += getStringDefaultValue(OxDefaultFields.COMMENTS).getValue();
		}				
		if (comments!=null) ser.setComments(comments);
		
		
		TridasInterpretation interp = new TridasInterpretation();
		SafeIntYear firstYearLine1 = getSafeIntYearDefaultValue(OxDefaultFields.FIRSTYEAR).getValue();
		SafeIntYear lastYearLine1 = getSafeIntYearDefaultValue(OxDefaultFields.LASTYEAR).getValue();

		Integer seriesLength = getIntegerDefaultValue(OxDefaultFields.SERIESLENGTH).getValue();
		SafeIntYear startYearLine2 = getSafeIntYearDefaultValue(OxDefaultFields.STARTYEAR).getValue();
		SafeIntYear lastYearLine2 = startYearLine2.add(seriesLength-1);
		
		Boolean isTentative = false;
		
		if(firstYearLine1.equals(new SafeIntYear(-999))) firstYearLine1 = null;
		if(startYearLine2.equals(new SafeIntYear(-999))) startYearLine2 = null;
		if(lastYearLine1.equals(new SafeIntYear(-999))) lastYearLine1 = null;
		
		if(firstYearLine1==null || lastYearLine1==null || startYearLine2==null) isTentative = true;
		
		if(startYearLine2!=null)
		{
			interp.setFirstYear(startYearLine2.toTridasYear(DatingSuffix.AD));
			if(isTentative)
			{
				interp.getFirstYear().setCertainty(Certainty.APPROXIMATELY);
			}
		}
		else if (firstYearLine1!=null)
		{
			interp.setFirstYear(firstYearLine1.toTridasYear(DatingSuffix.AD));
			if(isTentative)
			{
				interp.getFirstYear().setCertainty(Certainty.APPROXIMATELY);
			}
		}
		
		if(lastYearLine2!=null)
		{
			interp.setLastYear(lastYearLine2.toTridasYear(DatingSuffix.AD));
			if(isTentative)
			{
				interp.getLastYear().setCertainty(Certainty.APPROXIMATELY);
			}
		}
		
		ser.setInterpretation(interp);
		
		return ser;
		
	}
	
	@Override
	public TridasMeasurementSeries getMeasurementSeriesWithDefaults()
	{
		TridasMeasurementSeries ser = super.getMeasurementSeriesWithDefaults();
		ser = (TridasMeasurementSeries) populateSeriesDetails(ser);
		return ser;
	}
	
	@Override
	public TridasDerivedSeries getDerivedSeriesWithDefaults()
	{
		TridasDerivedSeries ser = super.getDerivedSeriesWithDefaults();
		ser = (TridasDerivedSeries) populateSeriesDetails(ser);
		return ser;
	}
	
	public TridasValues getDefaultTridasValues()
	{
		TridasValues valuesGroup = new TridasValues();
		TridasUnit units = new TridasUnit();
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		valuesGroup.setUnit(units);
		
		TridasVariable var = new TridasVariable();
		var.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		valuesGroup.setVariable(var);
		
		return valuesGroup;
		
	}
	
}
