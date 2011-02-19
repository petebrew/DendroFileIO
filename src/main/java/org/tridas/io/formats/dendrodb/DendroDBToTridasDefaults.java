package org.tridas.io.formats.dendrodb;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.oxford.OxfordToTridasDefaults.OxDefaultFields;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldDateType;
import org.tridas.io.util.CoordinatesUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class DendroDBToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	public static enum DDBDefaultFields {
		SITE,
		CONTACT,
		SPECIES,
		PARAMETER,
		LATITUDE,
		LONGITUDE,
		ELEVATION,
		STARTYEAR,
		TREE,
		CORE;
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DDBDefaultFields.SITE, new StringDefaultValue(I18n.getText("unnamed.object")));
		setDefaultValue(DDBDefaultFields.CONTACT, new StringDefaultValue());
		setDefaultValue(DDBDefaultFields.SPECIES, new StringDefaultValue());
		setDefaultValue(DDBDefaultFields.PARAMETER, new GenericDefaultValue<DendroDBParameter>());
		setDefaultValue(DDBDefaultFields.LATITUDE, new DoubleDefaultValue(null, -90.0, 90.0));
		setDefaultValue(DDBDefaultFields.LONGITUDE, new DoubleDefaultValue(null, -180.0, 180.0));
		setDefaultValue(DDBDefaultFields.ELEVATION, new DoubleDefaultValue());
		setDefaultValue(DDBDefaultFields.STARTYEAR, new SafeIntYearDefaultValue());
		setDefaultValue(DDBDefaultFields.TREE, new StringDefaultValue(I18n.getText("unnamed.element")));
		setDefaultValue(DDBDefaultFields.CORE, new StringDefaultValue(I18n.getText("unnamed.sample")));

	}
		
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasProject()
	 */
	@Override
	protected TridasProject getDefaultTridasProject() {
		TridasProject p = super.getDefaultTridasProject();
		
		if(getDefaultValue(DDBDefaultFields.CONTACT).getValue()!=null)
		{
			p.setInvestigator(getStringDefaultValue(DDBDefaultFields.CONTACT).getStringValue());
		}
		
		return p;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasObject()
	 */
	@Override
	protected TridasObject getDefaultTridasObject() {
		TridasObject o = super.getDefaultTridasObject();
		
		if(getDefaultValue(DDBDefaultFields.SITE).getValue()!=null)
		{
			o.setTitle(getStringDefaultValue(DDBDefaultFields.SITE).getStringValue());
		}
		
		return o;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		
		if(getDefaultValue(DDBDefaultFields.SPECIES).getValue()!=null)
		{
			ControlledVoc taxon = new ControlledVoc();
			taxon.setValue(getStringDefaultValue(DDBDefaultFields.SPECIES).getStringValue());
			e.setTaxon(taxon);
		}
		
		if(getDefaultValue(DDBDefaultFields.ELEVATION).getValue()!=null)
		{
			e.setAltitude(getDoubleDefaultValue(DDBDefaultFields.ELEVATION).getValue());
		}
		
		if((getDefaultValue(DDBDefaultFields.LATITUDE).getValue()!=null &&
				getDefaultValue(DDBDefaultFields.LONGITUDE).getValue()!=null))
		{
			TridasLocation loc = new TridasLocation();
			loc.setLocationGeometry(CoordinatesUtils.getLocationGeometry(
					getDoubleDefaultValue(DDBDefaultFields.LATITUDE).getValue(),
					getDoubleDefaultValue(DDBDefaultFields.LONGITUDE).getValue()));
			e.setLocation(loc);
		}
		
		if(getDefaultValue(DDBDefaultFields.TREE).getValue()!=null)
		{
			e.setTitle(getStringDefaultValue(DDBDefaultFields.TREE).getStringValue());
		}
		
		return e;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasSample()
	 */
	@Override
	protected TridasSample getDefaultTridasSample() {
		TridasSample s = super.getDefaultTridasSample();
		
		if(getDefaultValue(DDBDefaultFields.CORE).getValue()!=null)
		{
			s.setTitle(getStringDefaultValue(DDBDefaultFields.CORE).getStringValue());
		}
		
		return s;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasRadius()
	 */
	@Override
	protected TridasRadius getDefaultTridasRadius() {
		TridasRadius r = super.getDefaultTridasRadius();
				
		return r;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries ms = super.getDefaultTridasMeasurementSeries();
		
		if(getDefaultValue(DDBDefaultFields.STARTYEAR).getValue()!=null)
		{
			TridasInterpretation interp = new TridasInterpretation();
			interp.setFirstYear(getSafeIntYearDefaultValue(
					DDBDefaultFields.STARTYEAR).getValue().toTridasYear(DatingSuffix.AD));
			ms.setInterpretation(interp);
		}
		
		
		return ms;
	}
	

	@SuppressWarnings("unchecked")
	protected TridasValues getDefaultTridasValues(){
		
		TridasValues valuesGroup = new TridasValues();
		
		if(getDefaultValue(DDBDefaultFields.PARAMETER).getValue()!=null)
		{
			GenericDefaultValue<DendroDBParameter> dateTypeField = (GenericDefaultValue<DendroDBParameter>) getDefaultValue(DDBDefaultFields.PARAMETER);
			TridasVariable var = dateTypeField.getValue().toTridasVariable();
			
			valuesGroup.setVariable(var);
			
			TridasUnit unit = new TridasUnit();
			if(dateTypeField.getValue().toString().toLowerCase().contains("density"))
			{
				unit.setValue("Unknown");
			}
			else if(dateTypeField.getValue().toString().toLowerCase().contains("width"))
			{
				unit.setNormalTridas(NormalTridasUnit.MICROMETRES);
			}
			else
			{
				unit.setValue("Unknown");
			}

			valuesGroup.setUnit(unit);
		}
		else
		{
			
			TridasUnit unit = new TridasUnit();
			unit.setValue("Unknown");
			valuesGroup.setUnit(unit);
			
			TridasVariable var = new TridasVariable();
			var.setValue("Unknown");
			valuesGroup.setVariable(var);
		}
		
		return valuesGroup;
		
	}
	
	
	public enum DendroDBParameter {
		TOTALWIDTH("Total width"), 
		EARLYWIDTH("Earlywood width"), 
		LATEWIDTH("Latewood width"), 
		MINDENSITY("Min. Density"), 
		MAXDENSITY("Max. Density"),
		EARLYDENSITY("Earlywood density"), 
		LATEDENSITY("Latewood density"),
		AVERAGEDENSITY("Average density");

		
		private String code;
		
		DendroDBParameter(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public final TridasVariable toTridasVariable()
		{
			TridasVariable var = new TridasVariable();
			if(code.equals(DendroDBParameter.TOTALWIDTH.toString()))
			{
				var.setNormalTridas(NormalTridasVariable.RING_WIDTH);
			}
			else if(code.equals(DendroDBParameter.EARLYWIDTH.toString()))
			{
				var.setNormalTridas(NormalTridasVariable.EARLYWOOD_WIDTH);
			}
			else if(code.equals(DendroDBParameter.LATEWIDTH.toString()))
			{
				var.setNormalTridas(NormalTridasVariable.LATEWOOD_WIDTH);
			}
			else if(code.equals(DendroDBParameter.MINDENSITY.toString()))
			{
				var.setValue("Minimum density");
			}
			else if(code.equals(DendroDBParameter.MAXDENSITY.toString()))
			{
				var.setNormalTridas(NormalTridasVariable.MAXIMUM_DENSITY);
			}
			else if(code.equals(DendroDBParameter.EARLYDENSITY.toString()))
			{
				var.setNormalTridas(NormalTridasVariable.EARLYWOOD_DENSITY);
			}
			else if(code.equals(DendroDBParameter.LATEDENSITY.toString()))
			{
				var.setNormalTridas(NormalTridasVariable.LATEWOOD_DENSITY);
			}
			else if(code.equals(DendroDBParameter.AVERAGEDENSITY.toString()))
			{
				var.setNormalTridas(NormalTridasVariable.RING_DENSITY);
			}
			return var;
		}
		
		public static DendroDBParameter fromCode(String code) {
			for (DendroDBParameter val : DendroDBParameter.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
}
