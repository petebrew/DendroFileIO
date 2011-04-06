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
import java.util.List;

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
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToOxfordDefaults extends AbstractMetadataFieldSet implements
		IMetadataFieldSet {
	
	@Override
	protected void initDefaultValues() {

		setDefaultValue(OxDefaultFields.SERIESCODE, new StringDefaultValue(null, 8, 8));
		setDefaultValue(OxDefaultFields.DESCRIPTION, new StringDefaultValue());
		setDefaultValue(OxDefaultFields.FIRSTYEAR, new IntegerDefaultValue(null, 1, getCurrentYear(), 4, 4));
		setDefaultValue(OxDefaultFields.LASTYEAR, new IntegerDefaultValue(null, 1, getCurrentYear(), 4, 4));
		setDefaultValue(OxDefaultFields.SERIESLENGTH, new IntegerDefaultValue(0));
		setDefaultValue(OxDefaultFields.STARTYEAR, new IntegerDefaultValue(1001, 1, getCurrentYear(), 4, 4));
		setDefaultValue(OxDefaultFields.COMMENTS, new StringDefaultValue(""));
		getDefaultValue(OxDefaultFields.FIRSTYEAR).setPadRight(false);
		getDefaultValue(OxDefaultFields.LASTYEAR).setPadRight(false);

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
		try{
			if(argObject.isSetLocation())
			{
				if(argObject.getLocation().isSetLocationGeometry())
				{
					if (argObject.getLocation().getLocationGeometry().isSetPoint())
					{
						List<Double> coords = argObject.getLocation().getLocationGeometry().getPoint().getPos().getValues();
						addToComments("Coordinates: "+coords.get(0)+ ", "+coords.get(1));
					}
				}
			}
		} catch (Exception e)
		{}
		
		if(argObject.isSetGenericFields())
		{
			for(TridasGenericField gf : argObject.getGenericFields())
			{
				if(gf.getName().equals("sheffield.UKCoords"))
				{
					addToComments("British National Grid Coordinates: "+gf.getValue());
				}
			}
		}
		
	}
	
	public void populateFromTridasElement(TridasElement argElement)
	{
		
	}
	
	public void populateFromTridasSample(TridasSample argSample)
	{
		
	}
	
	public void populateFromTridasRadius(TridasRadius argRadius)
	{
		if(argRadius.isSetWoodCompleteness())
		{
			populateFromWoodCompleteness(argRadius.getWoodCompleteness());
		}
	}
	
	public void populateFromTridasSeries(ITridasSeries argSeries) throws ConversionWarningException
	{
		
		if(argSeries instanceof TridasMeasurementSeries)
		{
			if(((TridasMeasurementSeries)argSeries).isSetWoodCompleteness())
			{
				populateFromWoodCompleteness(((TridasMeasurementSeries)argSeries).getWoodCompleteness());
			}
		}
		
		getStringDefaultValue(OxDefaultFields.DESCRIPTION).setValue(argSeries.getTitle());
		
		if(argSeries.isSetGenericFields())
		{
			List<TridasGenericField> genFields = argSeries.getGenericFields();
			for(TridasGenericField gf : genFields)
			{
				if(gf.getName().equals("keycode"))
				{
					getStringDefaultValue(OxDefaultFields.SERIESCODE).setValue(gf.getValue());
				}				
			}
		}
		
		if(getStringDefaultValue(OxDefaultFields.SERIESCODE).getValue()==null)
		{
			getStringDefaultValue(OxDefaultFields.SERIESCODE).setValue(argSeries.getTitle());
		}
		
		
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
		
		addToComments(argSeries.getComments());

	}
	
	private void addToComments(String comment)
	{
		String existingComments = getStringDefaultValue(OxDefaultFields.COMMENTS).getValue();
		
		getStringDefaultValue(OxDefaultFields.COMMENTS).setValue(existingComments+comment+"\n");
	}
	
	private void populateFromWoodCompleteness(TridasWoodCompleteness wc)
	{
		
		if(wc.isSetSapwood())
		{
			TridasSapwood sap = wc.getSapwood();
			String sapwoodcomment = "";
			
			if(sap.isSetPresence())
			{
				sapwoodcomment += "Completeness of sapwood is noted as "+sap.getPresence().toString().toLowerCase()+"; ";
			}
			if(sap.isSetNrOfSapwoodRings())
			{
				sapwoodcomment += "There are "+ sap.getNrOfSapwoodRings()+ " sapwood rings recorded; ";
			}
			if(sap.isSetLastRingUnderBark())
			{
				if(sap.getLastRingUnderBark().isSetPresence())
				{
					sapwoodcomment += "Last ring under bark is "+sap.getLastRingUnderBark().getPresence().toString().toLowerCase()+"; ";
				}
				if(sap.getLastRingUnderBark().isSetContent())
				{
					sapwoodcomment += "Note about last ring: "+sap.getLastRingUnderBark().getContent() + "; ";
				}
			}
			if(sap.isSetMissingSapwoodRingsToBark())
			{
				sapwoodcomment += sap.getMissingSapwoodRingsToBark() + " missing sapwood rings to bark";
				if(sap.isSetMissingSapwoodRingsToBarkFoundation())
				{
					sapwoodcomment += " based upon: "+sap.getMissingSapwoodRingsToBarkFoundation();
				}
				sapwoodcomment +="; ";
			}
			
			
			if(!sapwoodcomment.equals("")) addToComments(sapwoodcomment.substring(0, sapwoodcomment.length()-2)+".");
		}
		
		if(wc.isSetNrOfUnmeasuredOuterRings())
		{
			if(wc.getNrOfUnmeasuredOuterRings()>0)
			{
				addToComments(wc.getNrOfUnmeasuredOuterRings()+ " unmeasured outer rings.");
			}
		}
		
		if(wc.isSetNrOfUnmeasuredInnerRings())
		{
			if(wc.getNrOfUnmeasuredInnerRings()>0)
			{
				addToComments(wc.getNrOfUnmeasuredInnerRings()+ " unmeasured inner rings.");
			}
		}
		
		if(wc.isSetHeartwood())
		{
			TridasHeartwood hw = wc.getHeartwood();
			String hwcomment = "";
			if(hw.isSetPresence())
			{
				hwcomment += "Completeness of heartwood is noted as " + hw.getPresence().toString().toLowerCase() + "; ";
			}
			
			if(hw.isSetMissingHeartwoodRingsToPith())
			{
				hwcomment += hw.getMissingHeartwoodRingsToPith() + " missing heartwood rings to pith";
				if(hw.isSetMissingHeartwoodRingsToPithFoundation())
				{
					hwcomment += " based upon: "+hw.getMissingHeartwoodRingsToPithFoundation();
				}
				hwcomment +="; ";
			}
			
			if(!hwcomment.equals("")) addToComments(hwcomment.substring(0, hwcomment.length()-2)+".");
		}
				
	}
}
