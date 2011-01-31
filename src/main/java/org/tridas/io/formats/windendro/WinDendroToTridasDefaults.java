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
package org.tridas.io.formats.windendro;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.NormalTridasMeasuringMethod;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasDimensions;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasuringMethod;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class WinDendroToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	public enum WDDefaultField {
		TREE_NAME, PATH_ID, SITE_ID, LAST_RING_YEAR, SAPWOOD_DISTANCE, TREE_HEIGHT, TREE_AGE, SECTION_HEIGHT,
		USER_VARIABLE, RING_COUNT, WD_DATA_TYPE, OFFSET_TO_NEXT, IMAGE_NAME, ANALYSIS_TIMESTAMP, ACQUISITION_TIMESTAMP,
		MODIFIED_DATE, IMAGE_SIZE_PIXELS, CALIB_METHOD, IMAGEING_HARDWARE, LENS_FOC_LENGTH, DISK_AVG_DIAM, PATH_LENGTH;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		super.initDefaultValues();
		
		// Version 3 fields
		setDefaultValue(WDDefaultField.TREE_NAME, new StringDefaultValue(I18n.getText("unnamed.element")));
		setDefaultValue(WDDefaultField.PATH_ID, new StringDefaultValue());
		setDefaultValue(WDDefaultField.SITE_ID, new StringDefaultValue(I18n.getText("unnamed.object")));
		setDefaultValue(WDDefaultField.LAST_RING_YEAR, new SafeIntYearDefaultValue());
		setDefaultValue(WDDefaultField.SAPWOOD_DISTANCE, new DoubleDefaultValue(null, Double.MIN_VALUE, Double.MAX_VALUE));
		setDefaultValue(WDDefaultField.TREE_HEIGHT, new DoubleDefaultValue(null, 1.0, 150.0));  // Tallest tree is ~115m
		setDefaultValue(WDDefaultField.TREE_AGE, new IntegerDefaultValue());
		setDefaultValue(WDDefaultField.SECTION_HEIGHT, new DoubleDefaultValue(null, Double.MIN_VALUE, 150.0));
		setDefaultValue(WDDefaultField.USER_VARIABLE, new StringDefaultValue());
		setDefaultValue(WDDefaultField.RING_COUNT, new IntegerDefaultValue(null, 1, Integer.MAX_VALUE));
		setDefaultValue(WDDefaultField.WD_DATA_TYPE, new GenericDefaultValue<WinDendroDataType>());
		setDefaultValue(WDDefaultField.OFFSET_TO_NEXT, new IntegerDefaultValue(null, 0, 99));
		
		
		// Version 4 fields
		// TODO
		setDefaultValue(WDDefaultField.ANALYSIS_TIMESTAMP, new DateTimeDefaultValue(DateUtils.getTodaysDateTime()));
		setDefaultValue(WDDefaultField.DISK_AVG_DIAM, new DoubleDefaultValue());
		setDefaultValue(WDDefaultField.PATH_LENGTH, new DoubleDefaultValue());

	}
	
	@Override
	public TridasObject getDefaultTridasObject()
	{
		TridasObject o = super.getDefaultTridasObject();
		
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(getStringDefaultValue(WDDefaultField.SITE_ID).getValue());
		o.setIdentifier(id);
		o.setTitle(getStringDefaultValue(WDDefaultField.SITE_ID).getValue());
		
		return o;
		
	}
	
	@Override
	public TridasElement getDefaultTridasElement()
	{
		TridasElement e = super.getDefaultTridasElement();
		
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(getStringDefaultValue(WDDefaultField.TREE_NAME).getValue());
		e.setIdentifier(id);
		e.setTitle(getStringDefaultValue(WDDefaultField.TREE_NAME).getValue());
		
		// Set dimensions
		if (getDoubleDefaultValue(WDDefaultField.DISK_AVG_DIAM).getValue()!=null && 
			getDoubleDefaultValue(WDDefaultField.TREE_HEIGHT).getValue()!=null)
		{
			TridasDimensions dim = new TridasDimensions();
			TridasUnit unit = new TridasUnit();
			unit.setNormalTridas(NormalTridasUnit.METRES);
			dim.setUnit(unit);
			
			dim.setDiameter(BigDecimal.valueOf(getDoubleDefaultValue(WDDefaultField.DISK_AVG_DIAM).getValue()));
			dim.setHeight(BigDecimal.valueOf(getDoubleDefaultValue(WDDefaultField.TREE_HEIGHT).getValue()));
			e.setDimensions(dim);
		}
		
		return e;
		
	}
	
	
	@Override
	public TridasSample getDefaultTridasSample()
	{
		TridasSample s = super.getDefaultTridasSample();
		
		//
		if(getDoubleDefaultValue(WDDefaultField.SECTION_HEIGHT).getValue()!=null && 
		  !getDoubleDefaultValue(WDDefaultField.SECTION_HEIGHT).getValue().equals(0.0)	)
		{
			s.setPosition(String.valueOf(getDoubleDefaultValue(WDDefaultField.SECTION_HEIGHT).getValue())+" "+I18n.getText("windendro.metresFromGround"));
		}
		
		return s;
		
	}
	
	@Override
	public TridasMeasurementSeries getDefaultTridasMeasurementSeries()
	{
		TridasMeasurementSeries ms = super.getDefaultTridasMeasurementSeries();
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
	
		if(getStringDefaultValue(WDDefaultField.PATH_ID).getValue()!=null)
		{
			// Try and use path id 
			id.setValue(getStringDefaultValue(WDDefaultField.PATH_ID).getValue());
			ms.setTitle(getStringDefaultValue(WDDefaultField.PATH_ID).getValue());
		}
		else
		{
			// Path id is null so use tree name instead
			id.setValue(getStringDefaultValue(WDDefaultField.TREE_NAME).getValue());
			ms.setTitle(getStringDefaultValue(WDDefaultField.TREE_NAME).getValue());
		}
		ms.setIdentifier(id);
		
		
		// Analysis timestamp
		ms.setCreatedTimestamp(getDateTimeDefaultValue(WDDefaultField.ANALYSIS_TIMESTAMP).getValue());
		
		// Set measuring method 
		TridasMeasuringMethod method = new TridasMeasuringMethod();
		method.setNormalTridas(NormalTridasMeasuringMethod.ONSCREEN_MEASURING);
		ms.setMeasuringMethod(method);
		
		SafeIntYear lastYear = getSafeIntYearDefaultValue(WDDefaultField.LAST_RING_YEAR).getValue();
		if (lastYear!=null)
		{
			// Dating type
			TridasDating dating = new TridasDating();
			dating.setType(NormalTridasDatingType.ABSOLUTE);
			ms.getInterpretation().setDating(dating);
			
			// Set year of last ring
			ms.getInterpretation().setLastYear(lastYear.toTridasYear(DatingSuffix.AD));

			if(getIntegerDefaultValue(WDDefaultField.RING_COUNT).getValue()!=null)
			{
				// Set first year by using ring count and last ring year
				SafeIntYear firstYear =lastYear.add(0-(getIntegerDefaultValue(WDDefaultField.RING_COUNT).getValue()));
				ms.getInterpretation().setFirstYear(firstYear.toTridasYear(DatingSuffix.AD));
				
				if(getIntegerDefaultValue(WDDefaultField.TREE_AGE).getValue()==0)
				{
					// If tree age = 0 then spec says to use ring count to decide pith year
					SafeIntYear pithAge =lastYear.add(0-(getIntegerDefaultValue(WDDefaultField.RING_COUNT).getValue()));
					ms.getInterpretation().setPithYear(pithAge.toTridasYear(DatingSuffix.AD));
				}
				else
				{
					// tree age has been set, so use it to calculate pith year
					SafeIntYear pithAge =lastYear.add(0-(getIntegerDefaultValue(WDDefaultField.TREE_AGE).getValue()));
					ms.getInterpretation().setPithYear(pithAge.toTridasYear(DatingSuffix.AD));
				}
				
			}
			

			

		}
	
		ArrayList<TridasGenericField> gflist = new ArrayList<TridasGenericField>();
		
		if(getDoubleDefaultValue(WDDefaultField.SAPWOOD_DISTANCE).getValue()!=null &&
		   !getDoubleDefaultValue(WDDefaultField.SAPWOOD_DISTANCE).getValue().equals(0.0))
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("windendro.sapwoodDistance");
			gf.setType("xs:float");
			gf.setValue(String.valueOf(getDoubleDefaultValue(WDDefaultField.SAPWOOD_DISTANCE).getValue()));
			gflist.add(gf);
		}
		if(getStringDefaultValue(WDDefaultField.USER_VARIABLE).getValue()!=null && 
		   !getStringDefaultValue(WDDefaultField.USER_VARIABLE).getValue().equals("0"))
				{
					TridasGenericField gf = new TridasGenericField();
					gf.setName("windendro.userVariable");
					gf.setType("xs:string");
					gf.setValue(getStringDefaultValue(WDDefaultField.USER_VARIABLE).getValue());
					gflist.add(gf);
				}
		
		
		ms.setGenericFields(gflist);		
				
		
		return ms;
		
	}
	
	
	protected TridasValues getDefaultTridasValues()
	{
		TridasValues values = new TridasValues();
		TridasVariable variable = new TridasVariable();
		TridasUnit unit = new TridasUnit();
		
		
		
		// Variable and Units
		
		if(getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue()==null)
		{
			variable.setValue("Unknown");
			unit.setValue("Unknown");
		}
		else if(getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.RINGWIDTH))
		{
			variable.setNormalTridas((NormalTridasVariable.RING_WIDTH));
			unit.setNormalTridas(NormalTridasUnit.MILLIMETRES);
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.EARLYWIDTH))
		{
			variable.setNormalTridas((NormalTridasVariable.EARLYWOOD_WIDTH));
			unit.setNormalTridas(NormalTridasUnit.MILLIMETRES);
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.LATEWIDTH))
		{
			variable.setNormalTridas((NormalTridasVariable.LATEWOOD_WIDTH));
			unit.setNormalTridas(NormalTridasUnit.MILLIMETRES);
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.DENSITY))
		{
			variable.setNormalTridas((NormalTridasVariable.RING_DENSITY));
			unit.setValue("g/cm3");
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.EARLYDENSITY))
		{
			variable.setNormalTridas((NormalTridasVariable.EARLYWOOD_DENSITY));
			unit.setValue("g/cm3");
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.EARLYWIDTHPERC))
		{
			variable.setValue("Earlywood width in percentage of ring width");
			unit.setValue("Percent");
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.LATEDENSITY))
		{
			variable.setNormalTridas((NormalTridasVariable.LATEWOOD_DENSITY));
			unit.setValue("g/cm3");
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.LATEWIDTHPERC))
		{
			variable.setValue("Latewood width in percentage of ring width");
			unit.setValue("Percent");
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.MAXDENSITY))
		{
			variable.setNormalTridas((NormalTridasVariable.MAXIMUM_DENSITY));
			unit.setValue("g/cm3");
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.MINDENSITY))
		{
			variable.setValue("Minimum density");
			unit.setValue("g/cm3");
		}
		else if (getDefaultValue(WDDefaultField.WD_DATA_TYPE).getValue().equals(WinDendroDataType.RINGANGLE))
		{
			variable.setValue("Ring angle");
			unit.setValue("Radians");
		}
		
		values.setVariable(variable);
		values.setUnit(unit);
		return values;
	}
	
	
	
	
	public enum WinDendroDataType {
		RINGWIDTH("RINGWIDTH"), EARLYWIDTH("EARLYWIDTH"), LATEWIDTH("LATEWIDTH"), EARLYWIDTHPERC("EARLYWIDTH%"),
		LATEWIDTHPERC("LATEWIDTH%"), DENSITY("DENSITY"), EARLYDENSITY("EARLYDENSITY"), LATEDENSITY("LATEDENSITY"),
		MAXDENSITY("MAXDENSITY"), MINDENSITY("MINDENSITY"), RINGANGLE("RINGANGLE");
		
		private String code;
		
		WinDendroDataType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static WinDendroDataType fromCode(String code) {
			for (WinDendroDataType val : WinDendroDataType.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
}
