package org.tridas.io.formats.sheffield;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldDataType;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.Year;

public class SheffieldToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	public static enum DefaultFields{
		OBJECT_NAME,
		RING_COUNT,
		START_YEAR,
		SERIES_TITLE,
		SERIES_COMMENT,
		LAT_LONG;
	}
	
	public void initDefaultValues(){
		super.initDefaultValues();
		setDefaultValue(DefaultFields.OBJECT_NAME, new StringDefaultValue(I18n.getText("unnamed.object")));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.START_YEAR, new GenericDefaultValue<SafeIntYear>());
		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.SERIES_COMMENT, new StringDefaultValue());
		setDefaultValue(DefaultFields.LAT_LONG, new GenericDefaultValue<Pos>());



	}

	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasObject()
	 */
	@Override
	protected TridasObject getDefaultTridasObject() {
		TridasObject o = super.getDefaultTridasObject();
		
		o.setTitle(getStringDefaultValue(DefaultFields.OBJECT_NAME).getStringValue());
		
		// If Lat Long is available use it
		if(getDefaultValue(DefaultFields.LAT_LONG)!=null)
		{
			TridasLocation location = new TridasLocation();
			TridasLocationGeometry geometry = new TridasLocationGeometry();
			PointType point = new PointType();
			GenericDefaultValue<Pos> posField = (GenericDefaultValue<Pos>) getDefaultValue(DefaultFields.LAT_LONG); 
			Pos pos = posField.getValue();
			point.setPos(pos);
			geometry.setPoint(point);
			location.setLocationGeometry(geometry);
			o.setLocation(location);
		}
		
		return o;
	}

	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		
		return e;
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries ms = super.getDefaultTridasMeasurementSeries();
		ms.setTitle(getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue());
		ms.setComments(getStringDefaultValue(DefaultFields.SERIES_COMMENT).getStringValue());
		
		try{
			GenericDefaultValue<SafeIntYear> startYearField = (GenericDefaultValue<SafeIntYear>) getDefaultValue(DefaultFields.START_YEAR); 		
			ms.getInterpretation().setFirstYear(startYearField.getValue().toTridasYear(DatingSuffix.AD));
		} catch (NullPointerException e){}
		
		return ms;
	}
	
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasDerivedSeries()
	 */
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
		TridasDerivedSeries ds = super.getDefaultTridasDerivedSeries();
		
		ds.setTitle(getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue());
		
		return ds;
		
	}
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults(){
		TridasValues valuesGroup = new TridasValues();
		
		
		TridasUnit units = new TridasUnit();
		
		// Set units to 1/100th mm.  Is this always the case?
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		
		valuesGroup.setUnit(units);
		
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
		
		
		return valuesGroup;
	}
}
